package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.entity.ClienteEntity;
import com.uniremington.cspizza.model.entity.DetallePedidoEntity;
import com.uniremington.cspizza.model.entity.PedidoEntity;
import com.uniremington.cspizza.model.entity.ProductoEntity;
import com.uniremington.cspizza.model.entity.UsuarioEntity;
import com.uniremington.cspizza.model.vo.PedidoNuevoVO;
import com.uniremington.cspizza.model.vo.ItemPedidoVO;
import com.uniremington.cspizza.model.vo.PedidoVO;
import com.uniremington.cspizza.model.repository.ClienteRepository;
import com.uniremington.cspizza.model.repository.PedidoRepository;
import com.uniremington.cspizza.model.repository.UsuarioRepository;
import com.uniremington.cspizza.model.repository.DetallePedidoRepository;
import com.uniremington.cspizza.model.repository.ProductoRepository;
import com.uniremington.cspizza.model.repository.InventarioRepository;
import com.uniremington.cspizza.service.exception.ConflictException;
import com.uniremington.cspizza.service.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas para {@link PedidoService#crearConDetalles(PedidoNuevoVO, String)} y
 * {@link PedidoService#cambiarEstado(Long, String)}.
 */
class PedidoServicePosTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /* ==================== crearConDetalles ==================== */

    @Test
    void crearConDetallesCalculaTotalCorrectamente() {
        // 1 Pizza Pepperoni ($30000 x 1) + 2 Gaseosas ($4500 x 2) = 39000
        PedidoNuevoVO entrada = new PedidoNuevoVO();
        entrada.setIdCliente(1L);
        entrada.setTipoEntrega("DOMICILIO");
        entrada.setDireccionEntrega("Calle 1 #2-3");
        entrada.setObservacion("Sin cebolla");

        ItemPedidoVO linea1 = new ItemPedidoVO();
        linea1.setIdProducto(2L); // Pizza Pepperoni
        linea1.setCantidad(1);

        ItemPedidoVO linea2 = new ItemPedidoVO();
        linea2.setIdProducto(3L); // Gaseosa 600ml
        linea2.setCantidad(2);

        entrada.setItems(List.of(linea1, linea2));

        ClienteEntity cliente = new ClienteEntity();
        cliente.setIdCliente(1L);
        cliente.setNombre("Cliente");
        cliente.setApellido("Test");

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);
        usuario.setNombreUsuario("empleado");

        ProductoEntity pepperoni = new ProductoEntity();
        pepperoni.setIdProducto(2L);
        pepperoni.setNombre("Pizza Pepperoni");
        pepperoni.setPrecioUnitario(new BigDecimal("30000.00"));
        pepperoni.setStock(18);
        pepperoni.setEstado("ACTIVO");

        ProductoEntity gaseosa = new ProductoEntity();
        gaseosa.setIdProducto(3L);
        gaseosa.setNombre("Gaseosa 600ml");
        gaseosa.setPrecioUnitario(new BigDecimal("4500.00"));
        gaseosa.setStock(40);
        gaseosa.setEstado("ACTIVO");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findFirstByNombreUsuario("empleado")).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(pepperoni));
        when(productoRepository.findById(3L)).thenReturn(Optional.of(gaseosa));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(inv -> {
            PedidoEntity p = inv.getArgument(0);
            p.setIdPedido(100L);
            return p;
        });
        when(detallePedidoRepository.save(any(DetallePedidoEntity.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(inventarioRepository.findAllByProducto_IdProducto(anyLong()))
            .thenReturn(List.of());

        PedidoVO resultado = pedidoService.crearConDetalles(entrada, "empleado");

        assertEquals(100L, resultado.getIdPedido());
        assertEquals(new BigDecimal("39000.00"), resultado.getTotalPedido());
        assertEquals("PENDIENTE", resultado.getEstado());
        assertEquals("empleado", resultado.getUsuarioNombre());

        // Verificar que se guardaron 2 detalles
        verify(detallePedidoRepository, times(2)).save(any(DetallePedidoEntity.class));

        // Verificar que se descontó stock
        verify(productoRepository).save(pepperoni);
        verify(productoRepository).save(gaseosa);
        assertEquals(17, pepperoni.getStock());  // 18 - 1
        assertEquals(38, gaseosa.getStock());    // 40 - 2
    }

    @Test
    void crearConDetallesTomaPrecioDeLaBaseDeDatosNoDelCuerpo() {
        PedidoNuevoVO entrada = new PedidoNuevoVO();
        entrada.setIdCliente(1L);
        entrada.setTipoEntrega("LOCAL");
        ItemPedidoVO linea = new ItemPedidoVO();
        linea.setIdProducto(2L);
        linea.setCantidad(1);
        entrada.setItems(List.of(linea));

        ClienteEntity cliente = new ClienteEntity();
        cliente.setIdCliente(1L);
        cliente.setNombre("Cliente");
        cliente.setApellido("Test");
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);
        usuario.setNombreUsuario("empleado");

        ProductoEntity producto = new ProductoEntity();
        producto.setIdProducto(2L);
        producto.setNombre("Pizza Pepperoni");
        producto.setPrecioUnitario(new BigDecimal("30000.00")); // Precio real de BD
        producto.setStock(18);
        producto.setEstado("ACTIVO");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findFirstByNombreUsuario("empleado")).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(inv -> {
            PedidoEntity p = inv.getArgument(0);
            p.setIdPedido(101L);
            return p;
        });
        when(detallePedidoRepository.save(any(DetallePedidoEntity.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(inventarioRepository.findAllByProducto_IdProducto(anyLong()))
            .thenReturn(List.of());

        PedidoVO resultado = pedidoService.crearConDetalles(entrada, "empleado");

        // El precio debe ser 30000 (de la BD), no cualquier otro
        assertEquals(new BigDecimal("30000.00"), resultado.getTotalPedido());
    }

    @Test
    void crearConDetallesRechazaSinItems() {
        PedidoNuevoVO entrada = new PedidoNuevoVO();
        entrada.setIdCliente(1L);
        entrada.setTipoEntrega("LOCAL");
        entrada.setItems(List.of());

        assertThrows(IllegalArgumentException.class,
            () -> pedidoService.crearConDetalles(entrada, "empleado"));
    }

    @Test
    void crearConDetallesRechazaProductoInexistente() {
        PedidoNuevoVO entrada = new PedidoNuevoVO();
        entrada.setIdCliente(1L);
        entrada.setTipoEntrega("LOCAL");
        ItemPedidoVO linea = new ItemPedidoVO();
        linea.setIdProducto(999L);
        linea.setCantidad(1);
        entrada.setItems(List.of(linea));

        ClienteEntity cliente = new ClienteEntity();
        cliente.setIdCliente(1L);
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);
        usuario.setNombreUsuario("empleado");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findFirstByNombreUsuario("empleado")).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> pedidoService.crearConDetalles(entrada, "empleado"));
    }

    @Test
    void crearConDetallesRechazaProductoInactivo() {
        PedidoNuevoVO entrada = new PedidoNuevoVO();
        entrada.setIdCliente(1L);
        entrada.setTipoEntrega("LOCAL");
        ItemPedidoVO linea = new ItemPedidoVO();
        linea.setIdProducto(2L);
        linea.setCantidad(1);
        entrada.setItems(List.of(linea));

        ClienteEntity cliente = new ClienteEntity();
        cliente.setIdCliente(1L);
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);
        usuario.setNombreUsuario("empleado");

        ProductoEntity producto = new ProductoEntity();
        producto.setIdProducto(2L);
        producto.setNombre("Pizza Descontinuada");
        producto.setPrecioUnitario(new BigDecimal("10000.00"));
        producto.setStock(10);
        producto.setEstado("INACTIVO");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findFirstByNombreUsuario("empleado")).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto));

        assertThrows(ConflictException.class,
            () -> pedidoService.crearConDetalles(entrada, "empleado"));
    }

    @Test
    void crearConDetallesRechazaStockInsuficiente() {
        PedidoNuevoVO entrada = new PedidoNuevoVO();
        entrada.setIdCliente(1L);
        entrada.setTipoEntrega("LOCAL");
        ItemPedidoVO linea = new ItemPedidoVO();
        linea.setIdProducto(2L);
        linea.setCantidad(100); // más de lo que hay
        entrada.setItems(List.of(linea));

        ClienteEntity cliente = new ClienteEntity();
        cliente.setIdCliente(1L);
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);
        usuario.setNombreUsuario("empleado");

        ProductoEntity producto = new ProductoEntity();
        producto.setIdProducto(2L);
        producto.setNombre("Pizza Margarita");
        producto.setPrecioUnitario(new BigDecimal("26000.00"));
        producto.setStock(5); // solo 5 en stock
        producto.setEstado("ACTIVO");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findFirstByNombreUsuario("empleado")).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto));

        assertThrows(ConflictException.class,
            () -> pedidoService.crearConDetalles(entrada, "empleado"));
    }

    @Test
    void crearConDetallesUsaClienteMostradorPorDefecto() {
        PedidoNuevoVO entrada = new PedidoNuevoVO();
        // sin idCliente
        entrada.setTipoEntrega("LOCAL");
        ItemPedidoVO linea = new ItemPedidoVO();
        linea.setIdProducto(2L);
        linea.setCantidad(1);
        entrada.setItems(List.of(linea));

        ClienteEntity mostrador = new ClienteEntity();
        mostrador.setIdCliente(99L);
        mostrador.setNombre("Cliente");
        mostrador.setApellido("Mostrador");

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);
        usuario.setNombreUsuario("empleado");

        ProductoEntity producto = new ProductoEntity();
        producto.setIdProducto(2L);
        producto.setNombre("Pizza Pepperoni");
        producto.setPrecioUnitario(new BigDecimal("30000.00"));
        producto.setStock(18);
        producto.setEstado("ACTIVO");

        when(clienteRepository.findByNombre("Cliente Mostrador"))
            .thenReturn(Optional.of(mostrador));
        when(clienteRepository.findById(99L)).thenReturn(Optional.of(mostrador));
        when(usuarioRepository.findFirstByNombreUsuario("empleado")).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(inv -> {
            PedidoEntity p = inv.getArgument(0);
            p.setIdPedido(200L);
            return p;
        });
        when(detallePedidoRepository.save(any(DetallePedidoEntity.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(inventarioRepository.findAllByProducto_IdProducto(anyLong()))
            .thenReturn(List.of());

        PedidoVO resultado = pedidoService.crearConDetalles(entrada, "empleado");

        assertEquals(99L, resultado.getIdCliente());
    }

    /* ==================== cambiarEstado ==================== */

    @Test
    void cambiarEstadoPendienteAAEnPreparacion() {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setIdPedido(1L);
        pedido.setEstado("PENDIENTE");
        pedido.setTotalPedido(new BigDecimal("10000.00"));

        ClienteEntity cliente = new ClienteEntity();
        cliente.setIdCliente(1L);
        cliente.setNombre("Cliente");
        cliente.setApellido("Test");
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);
        usuario.setNombreUsuario("empleado");
        pedido.setCliente(cliente);
        pedido.setUsuario(usuario);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoVO resultado = pedidoService.cambiarEstado(1L, "EN_PREPARACION");

        assertEquals("EN_PREPARACION", resultado.getEstado());
    }

    @Test
    void cambiarEstadoRechazaTransicionInvalida() {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setIdPedido(1L);
        pedido.setEstado("ENTREGADO");

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(ConflictException.class,
            () -> pedidoService.cambiarEstado(1L, "PENDIENTE"));
    }

    @Test
    void cambiarEstadoCancelarReponeStock() {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setIdPedido(1L);
        pedido.setEstado("PENDIENTE");
        pedido.setTotalPedido(new BigDecimal("30000.00"));

        ClienteEntity cliente = new ClienteEntity();
        cliente.setIdCliente(1L);
        cliente.setNombre("Cliente");
        cliente.setApellido("Test");
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);
        usuario.setNombreUsuario("empleado");
        pedido.setCliente(cliente);
        pedido.setUsuario(usuario);

        ProductoEntity producto = new ProductoEntity();
        producto.setIdProducto(2L);
        producto.setNombre("Pizza Pepperoni");
        producto.setStock(15); // 18 - 3 = 15, al cancelar vuelve a 18

        DetallePedidoEntity detalle = new DetallePedidoEntity();
        detalle.setIdDetallePedido(10L);
        detalle.setCantidad(3);
        detalle.setProducto(producto);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(detallePedidoRepository.findAllByPedido_IdPedido(1L))
            .thenReturn(List.of(detalle));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productoRepository.save(any(ProductoEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventarioRepository.findAllByProducto_IdProducto(2L))
            .thenReturn(List.of());

        PedidoVO resultado = pedidoService.cambiarEstado(1L, "CANCELADO");

        assertEquals("CANCELADO", resultado.getEstado());
        assertEquals(18, producto.getStock()); // 15 + 3 = 18 (repuesto)
        verify(productoRepository).save(producto);
    }
}

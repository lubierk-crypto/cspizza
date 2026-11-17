package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.entity.DetallePedidoEntity;
import com.uniremington.cspizza.model.entity.PedidoEntity;
import com.uniremington.cspizza.model.entity.ProductoEntity;
import com.uniremington.cspizza.model.repository.DetallePedidoRepository;
import com.uniremington.cspizza.model.repository.PedidoRepository;
import com.uniremington.cspizza.model.repository.ProductoRepository;
import com.uniremington.cspizza.model.repository.InventarioRepository;
import com.uniremington.cspizza.model.vo.DetallePedidoVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DetallePedidoServiceTest {

    @Mock private DetallePedidoRepository detallePedidoRepository;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private InventarioRepository inventarioRepository;
    @InjectMocks private DetallePedidoService detallePedidoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void guardarDetalleValidoCalculaSubtotalConPrecioDeBD() {
        DetallePedidoVO entrada = new DetallePedidoVO();
        entrada.setIdPedido(1L);
        entrada.setIdProducto(2L);
        entrada.setCantidad(2);

        PedidoEntity pedido = new PedidoEntity();
        pedido.setIdPedido(1L);

        ProductoEntity producto = new ProductoEntity();
        producto.setIdProducto(2L);
        producto.setNombre("Pizza Pepperoni");
        producto.setPrecioUnitario(new BigDecimal("30000.00"));
        producto.setStock(18);
        producto.setEstado("ACTIVO");

        DetallePedidoEntity guardado = new DetallePedidoEntity();
        guardado.setIdDetallePedido(10L);
        guardado.setPedido(pedido);
        guardado.setProducto(producto);
        guardado.setCantidad(2);
        guardado.setPrecioUnitario(new BigDecimal("30000.00"));
        guardado.setSubtotal(new BigDecimal("60000.00"));

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto));
        when(detallePedidoRepository.save(any(DetallePedidoEntity.class))).thenReturn(guardado);
        when(detallePedidoRepository.findAllByPedido_IdPedido(1L)).thenReturn(List.of(guardado));
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        DetallePedidoVO resultado = detallePedidoService.guardar(entrada);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getIdDetallePedido());
        assertEquals(new BigDecimal("30000.00"), resultado.getPrecioUnitario());
        assertEquals(new BigDecimal("60000.00"), resultado.getSubtotal());
    }

    @Test
    void guardarDetalleSinPedidoLanzaExcepcion() {
        DetallePedidoVO entrada = new DetallePedidoVO();
        entrada.setIdProducto(2L);
        entrada.setCantidad(1);
        assertThrows(IllegalArgumentException.class, () -> detallePedidoService.guardar(entrada));
    }

    @Test
    void guardarDetalleSinProductoLanzaExcepcion() {
        DetallePedidoVO entrada = new DetallePedidoVO();
        entrada.setIdPedido(1L);
        entrada.setCantidad(1);

        PedidoEntity pedido = new PedidoEntity();
        pedido.setIdPedido(1L);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalArgumentException.class, () -> detallePedidoService.guardar(entrada));
    }

    @Test
    void guardarDetalleCantidadCeroLanzaExcepcion() {
        DetallePedidoVO entrada = new DetallePedidoVO();
        entrada.setIdPedido(1L);
        entrada.setIdProducto(2L);
        entrada.setCantidad(0);

        PedidoEntity pedido = new PedidoEntity();
        pedido.setIdPedido(1L);
        ProductoEntity producto = new ProductoEntity();
        producto.setIdProducto(2L);
        producto.setNombre("Pizza");
        producto.setPrecioUnitario(new java.math.BigDecimal("10000"));
        producto.setStock(5);
        producto.setEstado("ACTIVO");

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto));

        assertThrows(IllegalArgumentException.class, () -> detallePedidoService.guardar(entrada));
    }

    @Test
    void guardarDetallePedidoInexistenteLanzaNotFound() {
        DetallePedidoVO entrada = new DetallePedidoVO();
        entrada.setIdPedido(999L);
        entrada.setIdProducto(2L);
        entrada.setCantidad(1);

        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(com.uniremington.cspizza.service.exception.NotFoundException.class,
                () -> detallePedidoService.guardar(entrada));
    }

    @Test
    void guardarDetalleProductoInexistenteLanzaNotFound() {
        DetallePedidoVO entrada = new DetallePedidoVO();
        entrada.setIdPedido(1L);
        entrada.setIdProducto(999L);
        entrada.setCantidad(1);

        PedidoEntity pedido = new PedidoEntity();
        pedido.setIdPedido(1L);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(com.uniremington.cspizza.service.exception.NotFoundException.class,
                () -> detallePedidoService.guardar(entrada));
    }

    @Test
    void eliminarDetalleInexistenteLanzaNotFound() {
        when(detallePedidoRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(com.uniremington.cspizza.service.exception.NotFoundException.class,
                () -> detallePedidoService.eliminar(999L));
    }

    @Test
    void buscarPorIdInexistenteRetornaVacio() {
        when(detallePedidoRepository.findById(999L)).thenReturn(Optional.empty());
        assertTrue(detallePedidoService.buscarPorId(999L).isEmpty());
    }
}

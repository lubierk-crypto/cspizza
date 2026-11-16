package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.vo.CategoriaVO;
import com.uniremington.cspizza.model.vo.ClienteVO;
import com.uniremington.cspizza.model.vo.DetallePedidoVO;
import com.uniremington.cspizza.model.vo.FacturaVO;
import com.uniremington.cspizza.model.vo.InventarioVO;
import com.uniremington.cspizza.model.vo.PedidoVO;
import com.uniremington.cspizza.model.vo.ProductoVO;
import com.uniremington.cspizza.model.vo.UsuarioVO;
import com.uniremington.cspizza.service.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ServicePersistenceTests {

    @Autowired private CategoriaService categoriaService;
    @Autowired private ClienteService clienteService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private ProductoService productoService;
    @Autowired private InventarioService inventarioService;
    @Autowired private PedidoService pedidoService;
    @Autowired private DetallePedidoService detallePedidoService;
    @Autowired private FacturaService facturaService;

    @Test
    void guardarCategoriaPersisteIdentificadorYEstado() {
        CategoriaVO category = new CategoriaVO();
        category.setNombre("Categoria " + System.nanoTime());
        category.setEstado("ACTIVO");

        CategoriaVO saved = categoriaService.guardar(category);

        assertNotNull(saved.getIdCategoria());
        assertEquals("ACTIVO", categoriaService.buscarPorId(saved.getIdCategoria()).orElseThrow().getEstado());
    }

    @Test
    void listarCategoriasDevuelveTodosLosRegistrosCreados() {
        CategoriaVO first = categoriaService.guardar(categoria("Primera"));
        CategoriaVO second = categoriaService.guardar(categoria("Segunda"));
        CategoriaVO third = categoriaService.guardar(categoria("Tercera"));

        var ids = categoriaService.listarTodos().stream().map(CategoriaVO::getIdCategoria).toList();

        assertTrue(ids.contains(first.getIdCategoria()));
        assertTrue(ids.contains(second.getIdCategoria()));
        assertTrue(ids.contains(third.getIdCategoria()));
    }

    @Test
    void serviciosJPAConservanCrudParaLosOtrosSieteModulos() {
        Long categoryId = categoriaService.guardar(categoria("CRUD")).getIdCategoria();
        ClienteVO referenceClient = clienteService.guardar(cliente("Referencia"));
        UsuarioVO referenceUser = usuarioService.guardar(usuario("referencia"));
        ProductoVO referenceProduct = productoService.guardar(producto(categoryId, "Referencia", 5));
        PedidoVO referenceOrder = pedidoService.guardar(pedido(referenceClient.getIdCliente(), referenceUser.getIdUsuario()));

        verifyCrud(clienteService, cliente("Temporal"), ClienteVO::getIdCliente,
                value -> value.setNombre("Cliente actualizado"));
        verifyCrud(usuarioService, usuario("temporal"), UsuarioVO::getIdUsuario,
                value -> value.setNombre("Usuario actualizado"));
        verifyCrud(productoService, producto(categoryId, "Temporal", 2), ProductoVO::getIdProducto,
                value -> value.setNombre("Producto actualizado"));
        verifyCrud(pedidoService, pedido(referenceClient.getIdCliente(), referenceUser.getIdUsuario()),
                PedidoVO::getIdPedido, value -> value.setObservacion("Actualizado"));

        DetallePedidoVO detail = new DetallePedidoVO();
        detail.setIdPedido(referenceOrder.getIdPedido());
        detail.setIdProducto(referenceProduct.getIdProducto());
        detail.setCantidad(1);
        verifyCrud(detallePedidoService, detail, DetallePedidoVO::getIdDetallePedido,
                value -> value.setCantidad(2));

        FacturaVO invoice = new FacturaVO();
        invoice.setIdPedido(referenceOrder.getIdPedido());
        invoice.setNumeroFactura("CRUD-" + System.nanoTime());
        invoice.setIva(new BigDecimal("1.00"));
        verifyCrud(facturaService, invoice, FacturaVO::getIdFactura,
                value -> value.setIva(new BigDecimal("2.00")));

        InventarioVO inventory = inventarioService.buscarPorProducto(referenceProduct.getIdProducto()).get(0);
        inventory.setCantidadMinima(1);
        inventory.setCantidadMaxima(10);
        inventarioService.actualizar(inventory);
        assertEquals(5, inventarioService.buscarPorId(inventory.getIdInventario()).orElseThrow().getCantidadActual());

        inventarioService.eliminar(inventory.getIdInventario());
        assertTrue(inventarioService.buscarPorProducto(referenceProduct.getIdProducto()).isEmpty());
        assertEquals(5, productoService.buscarPorId(referenceProduct.getIdProducto()).orElseThrow().getStock());

        InventarioVO recreatedInventory = new InventarioVO();
        recreatedInventory.setIdProducto(referenceProduct.getIdProducto());
        recreatedInventory.setCantidadActual(999);
        recreatedInventory.setCantidadMinima(1);
        recreatedInventory.setCantidadMaxima(10);
        recreatedInventory = inventarioService.guardar(recreatedInventory);
        assertEquals(5, recreatedInventory.getCantidadActual());
    }

    @Test
    void usuarioRechazaNombresDeUsuarioOCorreosDuplicados() {
        UsuarioVO original = usuarioService.guardar(usuario("duplicado"));
        UsuarioVO duplicateUsername = usuario("duplicado");
        duplicateUsername.setNombreUsuario(original.getNombreUsuario());
        duplicateUsername.setCorreo("otro-" + System.nanoTime() + "@example.test");

        assertThrows(ConflictException.class, () -> usuarioService.guardar(duplicateUsername));

        UsuarioVO duplicateEmail = usuario("otro");
        duplicateEmail.setCorreo(original.getCorreo());
        assertThrows(ConflictException.class, () -> usuarioService.guardar(duplicateEmail));
    }

    private static CategoriaVO categoria(String name) {
        CategoriaVO value = new CategoriaVO();
        value.setNombre(name + " " + System.nanoTime());
        value.setEstado("ACTIVO");
        return value;
    }

    private static ClienteVO cliente(String name) {
        ClienteVO value = new ClienteVO();
        value.setNombre(name);
        value.setApellido("Prueba");
        return value;
    }

    private static UsuarioVO usuario(String username) {
        UsuarioVO value = new UsuarioVO();
        value.setNombreUsuario(username + "-" + System.nanoTime());
        value.setNombre("Usuario");
        value.setApellido("Prueba");
        value.setRol("EMPLEADO");
        value.setCorreo(value.getNombreUsuario() + "@example.test");
        value.setPassword("test-password");
        return value;
    }

    private static ProductoVO producto(Long categoryId, String name, int stock) {
        ProductoVO value = new ProductoVO();
        value.setIdCategoria(categoryId);
        value.setNombre(name + " " + System.nanoTime());
        value.setPrecioUnitario(new BigDecimal("4.50"));
        value.setStock(stock);
        return value;
    }

    private static PedidoVO pedido(Long clientId, Long userId) {
        PedidoVO value = new PedidoVO();
        value.setIdCliente(clientId);
        value.setIdUsuario(userId);
        return value;
    }

    private static <T> void verifyCrud(CrudService<T> service, T value, Function<T, Long> idOf,
                                       Consumer<T> update) {
        T saved = service.guardar(value);
        Long id = idOf.apply(saved);
        assertNotNull(id);
        assertTrue(service.buscarPorId(id).isPresent());

        update.accept(saved);
        service.actualizar(saved);
        assertTrue(service.listarTodos().stream().map(idOf).anyMatch(id::equals));

        service.eliminar(id);
        assertFalse(service.buscarPorId(id).isPresent());
    }
}

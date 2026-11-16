package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.service.*;
import com.uniremington.cspizza.service.CrudService;
import com.uniremington.cspizza.model.vo.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class RestControllersTest {

    @Test
    void categoriaRestControllerManejaOperacionesCrud() {
        CategoriaService dao = mock(CategoriaService.class);
        CategoriaRestController controller = new CategoriaRestController();
        controller.setCategoriaService(dao);
        verifyCrud(dao, controller::listar, controller::buscarPorId, controller::guardar,
                controller::actualizar, controller::eliminar, CategoriaVO::new,
                CategoriaVO::getIdCategoria);
    }

    @Test
    void clienteRestControllerManejaOperacionesCrud() {
        ClienteService dao = mock(ClienteService.class);
        ClienteRestController controller = new ClienteRestController();
        controller.setClienteService(dao);
        verifyCrud(dao, controller::listar, controller::buscarPorId, controller::guardar,
                controller::actualizar, controller::eliminar, ClienteVO::new,
                ClienteVO::getIdCliente);
    }

    @Test
    void detallePedidoRestControllerManejaOperacionesCrud() {
        DetallePedidoService dao = mock(DetallePedidoService.class);
        DetallePedidoRestController controller = new DetallePedidoRestController();
        controller.setDetallePedidoService(dao);
        verifyCrud(dao, controller::listar, controller::buscarPorId, controller::guardar,
                controller::actualizar, controller::eliminar, DetallePedidoVO::new,
                DetallePedidoVO::getIdDetallePedido);
    }

    @Test
    void facturaRestControllerManejaOperacionesCrud() {
        FacturaService dao = mock(FacturaService.class);
        FacturaRestController controller = new FacturaRestController();
        controller.setFacturaService(dao);
        verifyCrud(dao, controller::listar, controller::buscarPorId, controller::guardar,
                controller::actualizar, controller::eliminar, FacturaVO::new,
                FacturaVO::getIdFactura);
    }

    @Test
    void inventarioRestControllerManejaOperacionesCrud() {
        InventarioService dao = mock(InventarioService.class);
        InventarioRestController controller = new InventarioRestController();
        controller.setInventarioService(dao);
        verifyCrud(dao, controller::listar, controller::buscarPorId, controller::guardar,
                controller::actualizar, controller::eliminar, InventarioVO::new,
                InventarioVO::getIdInventario);
    }

    @Test
    void pedidoRestControllerManejaOperacionesCrud() {
        PedidoService dao = mock(PedidoService.class);
        PedidoRestController controller = new PedidoRestController();
        controller.setPedidoService(dao);
        verifyCrud(dao, controller::listar, controller::buscarPorId, controller::guardar,
                controller::actualizar, controller::eliminar, PedidoVO::new,
                PedidoVO::getIdPedido);
    }

    @Test
    void productoRestControllerManejaOperacionesCrud() {
        ProductoService dao = mock(ProductoService.class);
        ProductoRestController controller = new ProductoRestController();
        controller.setProductoService(dao);
        verifyCrud(dao, controller::listar, controller::buscarPorId, controller::guardar,
                controller::actualizar, controller::eliminar, ProductoVO::new,
                ProductoVO::getIdProducto);
    }

    @Test
    void usuarioRestControllerManejaOperacionesCrud() {
        UsuarioService dao = mock(UsuarioService.class);
        UsuarioRestController controller = new UsuarioRestController();
        controller.setUsuarioService(dao);
        verifyCrud(dao, controller::listar, controller::buscarPorId, controller::guardar,
                controller::actualizar, controller::eliminar, UsuarioVO::new,
                UsuarioVO::getIdUsuario);
    }

    private <T> void verifyCrud(CrudService<T> dao, Supplier<List<T>> list,
                                Function<Long, ResponseEntity<T>> find,
                                Function<T, ResponseEntity<T>> create,
                                BiFunctionWithId<T> update,
                                Function<Long, ResponseEntity<Void>> delete,
                                Supplier<T> entityFactory, Function<T, Long> getId) {
        Long id = 42L;
        T entity = entityFactory.get();
        when(dao.listarTodos()).thenReturn(List.of(entity));
        when(dao.buscarPorId(id)).thenReturn(Optional.of(entity));
        when(dao.guardar(entity)).thenReturn(entity);
        when(dao.actualizar(entity)).thenReturn(entity);

        assertEquals(List.of(entity), list.get());
        assertEquals(HttpStatus.OK, find.apply(id).getStatusCode());
        assertSame(entity, find.apply(id).getBody());
        when(dao.buscarPorId(404L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, find.apply(404L).getStatusCode());
        assertEquals(HttpStatus.CREATED, create.apply(entity).getStatusCode());
        assertSame(entity, create.apply(entity).getBody());

        assertEquals(HttpStatus.OK, update.apply(id, entity).getStatusCode());
        assertEquals(id, getId.apply(entity));
        verify(dao).actualizar(entity);

        assertEquals(HttpStatus.NO_CONTENT, delete.apply(id).getStatusCode());
        verify(dao).eliminar(id);
    }

    @FunctionalInterface
    private interface BiFunctionWithId<T> {
        ResponseEntity<T> apply(Long id, T entity);
    }
}
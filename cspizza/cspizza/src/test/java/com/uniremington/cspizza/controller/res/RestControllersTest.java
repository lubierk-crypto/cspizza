package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.model.dao.*;
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
        CategoriaDAO dao = mock(CategoriaDAO.class);
        CategoriaRestController controller = new CategoriaRestController();
        controller.setCategoriaDAO(dao);
        verifyCrud(dao, controller::listar, controller::buscarPorId, controller::guardar,
                controller::actualizar, controller::eliminar, CategoriaVO::new,
                CategoriaVO::getIdCategoria);
    }

    @Test
    void clienteRestControllerManejaOperacionesCrud() {
        ClienteDAO dao = mock(ClienteDAO.class);
        ClienteRestController controller = new ClienteRestController();
        controller.setClienteDAO(dao);
        verifyCrud(dao, controller::listar, controller::buscarPorId, controller::guardar,
                controller::actualizar, controller::eliminar, ClienteVO::new,
                ClienteVO::getIdCliente);
    }

    @Test
    void detallePedidoRestControllerManejaOperacionesCrud() {
        DetallePedidoDAO dao = mock(DetallePedidoDAO.class);
        DetallePedidoRestController controller = new DetallePedidoRestController();
        controller.setDetallePedidoDAO(dao);
        verifyCrud(dao, controller::listar, controller::buscarPorId, controller::guardar,
                controller::actualizar, controller::eliminar, DetallePedidoVO::new,
                DetallePedidoVO::getIdDetallePedido);
    }

    @Test
    void facturaRestControllerManejaOperacionesCrud() {
        FacturaDAO dao = mock(FacturaDAO.class);
        FacturaRestController controller = new FacturaRestController();
        controller.setFacturaDAO(dao);
        verifyCrud(dao, controller::listar, controller::buscarPorId, controller::guardar,
                controller::actualizar, controller::eliminar, FacturaVO::new,
                FacturaVO::getIdFactura);
    }

    @Test
    void inventarioRestControllerManejaOperacionesCrud() {
        InventarioDAO dao = mock(InventarioDAO.class);
        InventarioRestController controller = new InventarioRestController();
        controller.setInventarioDAO(dao);
        verifyCrud(dao, controller::listar, controller::buscarPorId, controller::guardar,
                controller::actualizar, controller::eliminar, InventarioVO::new,
                InventarioVO::getIdInventario);
    }

    @Test
    void pedidoRestControllerManejaOperacionesCrud() {
        PedidoDAO dao = mock(PedidoDAO.class);
        PedidoRestController controller = new PedidoRestController();
        controller.setPedidoDAO(dao);
        verifyCrud(dao, controller::listar, controller::buscarPorId, controller::guardar,
                controller::actualizar, controller::eliminar, PedidoVO::new,
                PedidoVO::getIdPedido);
    }

    @Test
    void productoRestControllerManejaOperacionesCrud() {
        ProductoDAO dao = mock(ProductoDAO.class);
        ProductoRestController controller = new ProductoRestController();
        controller.setProductoDAO(dao);
        verifyCrud(dao, controller::listar, controller::buscarPorId, controller::guardar,
                controller::actualizar, controller::eliminar, ProductoVO::new,
                ProductoVO::getIdProducto);
    }

    @Test
    void usuarioRestControllerManejaOperacionesCrud() {
        UsuarioDAO dao = mock(UsuarioDAO.class);
        UsuarioRestController controller = new UsuarioRestController();
        controller.setUsuarioDAO(dao);
        verifyCrud(dao, controller::listar, controller::buscarPorId, controller::guardar,
                controller::actualizar, controller::eliminar, UsuarioVO::new,
                UsuarioVO::getIdUsuario);
    }

    private <T> void verifyCrud(GenericDAO<T, Long> dao, Supplier<List<T>> list,
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
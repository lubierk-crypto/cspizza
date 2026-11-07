package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.model.dao.*;
import com.uniremington.cspizza.model.vo.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MvcControllersTest {

    @Test
    void categoriaMvcControllerManejaOperacionesCrud() {
        CategoriaDAO dao = mock(CategoriaDAO.class);
        CategoriaMvcController controller = new CategoriaMvcController();
        controller.setCategoriaDAO(dao);
        verifyCrud(dao, controller::listar, controller::nuevo, controller::guardar,
                controller::editar, controller::eliminar, "categorias", "categorias",
                "categoria", CategoriaVO.class, CategoriaVO::new,
                CategoriaVO::getIdCategoria, CategoriaVO::setIdCategoria);
    }

    @Test
    void clienteMvcControllerManejaOperacionesCrud() {
        ClienteDAO dao = mock(ClienteDAO.class);
        ClienteMvcController controller = new ClienteMvcController();
        controller.setClienteDAO(dao);
        verifyCrud(dao, controller::listar, controller::nuevo, controller::guardar,
                controller::editar, controller::eliminar, "clientes", "clientes",
                "cliente", ClienteVO.class, ClienteVO::new,
                ClienteVO::getIdCliente, ClienteVO::setIdCliente);
    }

    @Test
    void detallePedidoMvcControllerManejaOperacionesCrud() {
        DetallePedidoDAO dao = mock(DetallePedidoDAO.class);
        DetallePedidoMvcController controller = new DetallePedidoMvcController();
        controller.setDetallePedidoDAO(dao);
        verifyCrud(dao, controller::listar, controller::nuevo, controller::guardar,
                controller::editar, controller::eliminar, "detalle-pedidos", "detallePedidos",
                "detallePedido", DetallePedidoVO.class, DetallePedidoVO::new,
                DetallePedidoVO::getIdDetallePedido, DetallePedidoVO::setIdDetallePedido);
    }

    @Test
    void facturaMvcControllerManejaOperacionesCrud() {
        FacturaDAO dao = mock(FacturaDAO.class);
        FacturaMvcController controller = new FacturaMvcController();
        controller.setFacturaDAO(dao);
        verifyCrud(dao, controller::listar, controller::nuevo, controller::guardar,
                controller::editar, controller::eliminar, "facturas", "facturas",
                "factura", FacturaVO.class, FacturaVO::new,
                FacturaVO::getIdFactura, FacturaVO::setIdFactura);
    }

    @Test
    void inventarioMvcControllerManejaOperacionesCrud() {
        InventarioDAO dao = mock(InventarioDAO.class);
        InventarioMvcController controller = new InventarioMvcController();
        controller.setInventarioDAO(dao);
        verifyCrud(dao, controller::listar, controller::nuevo, controller::guardar,
                controller::editar, controller::eliminar, "inventarios", "inventarios",
                "inventario", InventarioVO.class, InventarioVO::new,
                InventarioVO::getIdInventario, InventarioVO::setIdInventario);
    }

    @Test
    void pedidoMvcControllerManejaOperacionesCrud() {
        PedidoDAO dao = mock(PedidoDAO.class);
        PedidoMvcController controller = new PedidoMvcController();
        controller.setPedidoDAO(dao);
        verifyCrud(dao, controller::listar, controller::nuevo, controller::guardar,
                controller::editar, controller::eliminar, "pedidos", "pedidos",
                "pedido", PedidoVO.class, PedidoVO::new,
                PedidoVO::getIdPedido, PedidoVO::setIdPedido);
    }

    @Test
    void productoMvcControllerManejaOperacionesCrud() {
        ProductoDAO dao = mock(ProductoDAO.class);
        ProductoMvcController controller = new ProductoMvcController();
        controller.setProductoDAO(dao);
        verifyCrud(dao, controller::listar, controller::nuevo, controller::guardar,
                controller::editar, controller::eliminar, "productos", "productos",
                "producto", ProductoVO.class, ProductoVO::new,
                ProductoVO::getIdProducto, ProductoVO::setIdProducto);
    }

    @Test
    void usuarioMvcControllerManejaOperacionesCrud() {
        UsuarioDAO dao = mock(UsuarioDAO.class);
        UsuarioMvcController controller = new UsuarioMvcController();
        controller.setUsuarioDAO(dao);
        verifyCrud(dao, controller::listar, controller::nuevo, controller::guardar,
                controller::editar, controller::eliminar, "usuarios", "usuarios",
                "usuario", UsuarioVO.class, UsuarioVO::new,
                UsuarioVO::getIdUsuario, UsuarioVO::setIdUsuario);
    }

    @Test
    void authMvcControllerSeleccionaVistasDeAutenticacionYRoles() {
        AuthMvcController controller = new AuthMvcController();

        assertEquals("auth/login", controller.login());
        assertEquals("redirect:/login", controller.index());
        assertEquals("error/403", controller.accessDenied());

        assertDashboard(controller, "ROLE_ADMIN", "dashboard/admin");
        assertDashboard(controller, "ROLE_GERENTE", "dashboard/gerencia");
        assertDashboard(controller, "ROLE_EMPLEADO", "dashboard/empleado");
    }

    private void assertDashboard(AuthMvcController controller, String role, String expectedView) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "usuario-prueba", "", List.of(new SimpleGrantedAuthority(role)));
        Model model = mock(Model.class);

        assertEquals(expectedView, controller.dashboard(authentication, model));
        verify(model).addAttribute("username", "usuario-prueba");
    }

    private <T> void verifyCrud(GenericDAO<T, Long> dao, Function<Model, String> list,
                                Function<Model, String> newForm, Function<T, String> save,
                                BiFunction<Long, Model, String> edit, Function<Long, String> delete,
                                String resource, String listAttribute, String formAttribute,
                                Class<T> entityClass, Supplier<T> entityFactory,
                                Function<T, Long> getId, BiConsumer<T, Long> setId) {
        Long id = 42L;
        T entity = entityFactory.get();
        when(dao.listarTodos()).thenReturn(List.of(entity));
        when(dao.buscarPorId(id)).thenReturn(Optional.of(entity));
        when(dao.guardar(entity)).thenReturn(entity);
        when(dao.actualizar(entity)).thenReturn(entity);

        Model listModel = mock(Model.class);
        assertEquals(resource + "/lista", list.apply(listModel));
        verify(listModel).addAttribute(listAttribute, List.of(entity));

        Model newModel = mock(Model.class);
        assertEquals(resource + "/form", newForm.apply(newModel));
        verify(newModel).addAttribute(eq(formAttribute), any(entityClass));

        T newEntity = entityFactory.get();
        assertEquals("redirect:/" + resource, save.apply(newEntity));
        verify(dao).guardar(newEntity);

        setId.accept(entity, id);
        assertEquals("redirect:/" + resource, save.apply(entity));
        assertEquals(id, getId.apply(entity));
        verify(dao).actualizar(entity);

        Model editModel = mock(Model.class);
        assertEquals(resource + "/form", edit.apply(id, editModel));
        verify(editModel).addAttribute(formAttribute, entity);

        assertEquals("redirect:/" + resource, delete.apply(id));
        verify(dao).eliminar(id);
    }
}
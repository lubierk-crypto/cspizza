package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.service.*;
import com.uniremington.cspizza.service.CrudService;
import com.uniremington.cspizza.model.vo.*;
import com.uniremington.cspizza.service.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MvcControllersTest {

    @Test
    void categoriaMvcControllerManejaOperacionesCrud() {
        CategoriaService dao = mock(CategoriaService.class);
        CategoriaMvcController controller = new CategoriaMvcController();
        controller.setCategoriaService(dao);
        verifyCrud(dao, controller::listar, controller::nuevo, controller::guardar,
                controller::editar, controller::eliminar, "categorias", "categorias",
                "categoria", CategoriaVO.class, CategoriaVO::new,
                CategoriaVO::getIdCategoria, CategoriaVO::setIdCategoria);
    }

    @Test
    void clienteMvcControllerManejaOperacionesCrud() {
        ClienteService dao = mock(ClienteService.class);
        ClienteMvcController controller = new ClienteMvcController();
        controller.setClienteService(dao);
        verifyCrud(dao, controller::listar, controller::nuevo, controller::guardar,
                controller::editar, controller::eliminar, "clientes", "clientes",
                "cliente", ClienteVO.class, ClienteVO::new,
                ClienteVO::getIdCliente, ClienteVO::setIdCliente);
    }

    @Test
    void detallePedidoMvcControllerManejaOperacionesCrud() {
        DetallePedidoService dao = mock(DetallePedidoService.class);
        DetallePedidoMvcController controller = new DetallePedidoMvcController();
        controller.setDetallePedidoService(dao);
        verifyCrud(dao, controller::listar, controller::nuevo, controller::guardar,
                controller::editar, controller::eliminar, "detalle-pedidos", "detallePedidos",
                "detallePedido", DetallePedidoVO.class, DetallePedidoVO::new,
                DetallePedidoVO::getIdDetallePedido, DetallePedidoVO::setIdDetallePedido);
    }

    @Test
    void facturaMvcControllerManejaOperacionesCrud() {
        FacturaService dao = mock(FacturaService.class);
        FacturaMvcController controller = new FacturaMvcController();
        controller.setFacturaService(dao);
        verifyCrud(dao, controller::listar, controller::nuevo, controller::guardar,
                controller::editar, controller::eliminar, "facturas", "facturas",
                "factura", FacturaVO.class, FacturaVO::new,
                FacturaVO::getIdFactura, FacturaVO::setIdFactura);
    }

    @Test
    void inventarioMvcControllerManejaOperacionesCrud() {
        InventarioService dao = mock(InventarioService.class);
        InventarioMvcController controller = new InventarioMvcController();
        controller.setInventarioService(dao);
        verifyCrud(dao, controller::listar, controller::nuevo, controller::guardar,
                controller::editar, controller::eliminar, "inventarios", "inventarios",
                "inventario", InventarioVO.class, InventarioVO::new,
                InventarioVO::getIdInventario, InventarioVO::setIdInventario);
    }

    @Test
    void pedidoMvcControllerManejaOperacionesCrud() {
        PedidoService dao = mock(PedidoService.class);
        PedidoMvcController controller = new PedidoMvcController();
        controller.setPedidoService(dao);
        verifyCrud(dao, controller::listar, controller::nuevo, controller::guardar,
                controller::editar, controller::eliminar, "pedidos", "pedidos",
                "pedido", PedidoVO.class, PedidoVO::new,
                PedidoVO::getIdPedido, PedidoVO::setIdPedido);
    }

    @Test
    void productoMvcControllerManejaOperacionesCrud() {
        ProductoService dao = mock(ProductoService.class);
        ProductoMvcController controller = new ProductoMvcController();
        controller.setProductoService(dao);
        verifyCrud(dao, controller::listar, controller::nuevo, controller::guardar,
                controller::editar, controller::eliminar, "productos", "productos",
                "producto", ProductoVO.class, ProductoVO::new,
                ProductoVO::getIdProducto, ProductoVO::setIdProducto);
    }

    @Test
    void usuarioMvcControllerManejaOperacionesCrud() {
        UsuarioService dao = mock(UsuarioService.class);
        UsuarioMvcController controller = new UsuarioMvcController();
        controller.setUsuarioService(dao);

        UsuarioVO nuevo = usuario("nuevo", "nuevo@cspizza.test", "nueva-clave");
        when(dao.buscarPorNombreUsuario("nuevo")).thenReturn(Optional.empty());
        when(dao.buscarPorCorreo("nuevo@cspizza.test")).thenReturn(Optional.empty());
        when(dao.guardar(nuevo)).thenReturn(nuevo);

        assertEquals("redirect:/usuarios",
                controller.guardar(nuevo, new BeanPropertyBindingResult(nuevo, "usuario")));
        verify(dao).guardar(nuevo);

        UsuarioVO actual = usuario("existente", "existente@cspizza.test", "$2a$10$hash");
        actual.setIdUsuario(42L);
        actual.setFechaRegistro(LocalDateTime.of(2025, 1, 1, 10, 0));
        when(dao.buscarPorId(42L)).thenReturn(Optional.of(actual));
        when(dao.buscarPorNombreUsuario("existente")).thenReturn(Optional.of(actual));
        when(dao.buscarPorCorreo("existente@cspizza.test")).thenReturn(Optional.of(actual));

        UsuarioVO editado = usuario("existente", "existente@cspizza.test", "");
        editado.setIdUsuario(42L);
        when(dao.actualizar(editado)).thenReturn(editado);
        assertEquals("redirect:/usuarios",
                controller.guardar(editado, new BeanPropertyBindingResult(editado, "usuario")));
        assertEquals(actual.getPassword(), editado.getPassword());
        assertEquals(actual.getFechaRegistro(), editado.getFechaRegistro());
        verify(dao).actualizar(editado);

        Model listModel = mock(Model.class);
        when(dao.listarTodos()).thenReturn(List.of(actual));
        assertEquals("usuarios/lista", controller.listar(listModel));
        verify(listModel).addAttribute("usuarios", List.of(actual));

        Model newModel = mock(Model.class);
        assertEquals("usuarios/form", controller.nuevo(newModel));
        verify(newModel).addAttribute(eq("usuario"), any(UsuarioVO.class));

        Model editModel = mock(Model.class);
        assertEquals("usuarios/form", controller.editar(42L, editModel, new RedirectAttributesModelMap()));
        verify(editModel).addAttribute("usuario", actual);

        assertEquals("redirect:/usuarios", controller.eliminar(42L, new RedirectAttributesModelMap()));
        verify(dao).eliminar(42L);
    }

    @Test
    void usuarioMvcControllerRechazaNombreDuplicadoYContrasenaVaciaAlCrear() {
        UsuarioService dao = mock(UsuarioService.class);
        UsuarioMvcController controller = new UsuarioMvcController();
        controller.setUsuarioService(dao);

        UsuarioVO existente = usuario("admin", "admin@cspizza.test", "hash");
        existente.setIdUsuario(1L);
        when(dao.buscarPorNombreUsuario("admin")).thenReturn(Optional.of(existente));
        when(dao.buscarPorCorreo("nuevo@cspizza.test")).thenReturn(Optional.empty());

        UsuarioVO duplicado = usuario("admin", "nuevo@cspizza.test", "clave");
        BeanPropertyBindingResult duplicateErrors = new BeanPropertyBindingResult(duplicado, "usuario");
        assertEquals("usuarios/form", controller.guardar(duplicado, duplicateErrors));
        assertTrue(duplicateErrors.hasFieldErrors("nombreUsuario"));
        verify(dao, never()).guardar(any(UsuarioVO.class));

        UsuarioVO sinContrasena = usuario("otro", "otro@cspizza.test", "");
        when(dao.buscarPorNombreUsuario("otro")).thenReturn(Optional.empty());
        when(dao.buscarPorCorreo("otro@cspizza.test")).thenReturn(Optional.empty());
        BeanPropertyBindingResult passwordErrors = new BeanPropertyBindingResult(sinContrasena, "usuario");
        assertEquals("usuarios/form", controller.guardar(sinContrasena, passwordErrors));
        assertTrue(passwordErrors.hasFieldErrors("password"));
        verify(dao, never()).guardar(sinContrasena);
    }

    @Test
    void usuarioMvcControllerMuestraErrorLegibleSiLaBaseRechazaElRegistro() {
        UsuarioService dao = mock(UsuarioService.class);
        UsuarioMvcController controller = new UsuarioMvcController();
        controller.setUsuarioService(dao);
        UsuarioVO usuario = usuario("nuevo", "nuevo@cspizza.test", "clave");
        when(dao.buscarPorNombreUsuario("nuevo")).thenReturn(Optional.empty());
        when(dao.buscarPorCorreo("nuevo@cspizza.test")).thenReturn(Optional.empty());
        when(dao.guardar(usuario)).thenThrow(new DataIntegrityViolationException("database constraint"));

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(usuario, "usuario");
        assertEquals("usuarios/form", controller.guardar(usuario, errors));
        assertTrue(errors.hasGlobalErrors());
    }

    @Test
    void editarConIdInexistenteRedirigeAlListadoConMensaje() {
        ProductoService dao = mock(ProductoService.class);
        ProductoMvcController controller = new ProductoMvcController();
        controller.setProductoService(dao);
        when(dao.buscarPorId(404L)).thenReturn(Optional.empty());

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        assertEquals("redirect:/productos", controller.editar(404L, mock(Model.class), redirectAttributes));
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("error"));
        verify(dao, never()).eliminar(any());
    }

    @Test
    void guardarConErrorDeNegocioVuelveAlFormularioConMensaje() {
        ProductoService dao = mock(ProductoService.class);
        ProductoMvcController controller = new ProductoMvcController();
        controller.setProductoService(dao);

        ProductoVO producto = new ProductoVO();
        producto.setIdCategoria(99L);
        producto.setNombre("Producto");
        producto.setPrecioUnitario(new java.math.BigDecimal("10.00"));
        when(dao.guardar(producto)).thenThrow(new NotFoundException("No existe la categoría solicitada."));

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(producto, "producto");
        assertEquals("productos/form", controller.guardar(producto, errors));
        assertTrue(errors.hasGlobalErrors());
    }

    @Test
    void eliminarUnRegistroInexistenteMuestraMensajeSinError() {
        ProductoService dao = mock(ProductoService.class);
        ProductoMvcController controller = new ProductoMvcController();
        controller.setProductoService(dao);
        doThrow(new NotFoundException("No existe el registro 7.")).when(dao).eliminar(7L);

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        assertEquals("redirect:/productos", controller.eliminar(7L, redirectAttributes));
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("error"));
    }

    private UsuarioVO usuario(String nombreUsuario, String correo, String password) {
        UsuarioVO usuario = new UsuarioVO();
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setNombre("Nombre");
        usuario.setApellido("Apellido");
        usuario.setRol("empleado");
        usuario.setCorreo(correo);
        usuario.setPassword(password);
        return usuario;
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

    private <T> void verifyCrud(CrudService<T> dao, Function<Model, String> list,
                                Function<Model, String> newForm, SaveFunction<T> save,
                                EditFunction edit, DeleteFunction delete,
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
        String expectedNewView = "pedidos".equals(resource) ? "pedidos/nuevo" : resource + "/form";
        assertEquals(expectedNewView, newForm.apply(newModel));
        verify(newModel).addAttribute(eq(formAttribute), any(entityClass));

        T newEntity = entityFactory.get();
        assertEquals("redirect:/" + resource, save.apply(newEntity, new BeanPropertyBindingResult(newEntity, formAttribute)));
        verify(dao).guardar(newEntity);

        setId.accept(entity, id);
        assertEquals("redirect:/" + resource, save.apply(entity, new BeanPropertyBindingResult(entity, formAttribute)));
        assertEquals(id, getId.apply(entity));
        verify(dao).actualizar(entity);

        Model editModel = mock(Model.class);
        assertEquals(resource + "/form", edit.apply(id, editModel, new RedirectAttributesModelMap()));
        verify(editModel).addAttribute(formAttribute, entity);

        assertEquals("redirect:/" + resource, delete.apply(id, new RedirectAttributesModelMap()));
        verify(dao).eliminar(id);
    }

    @FunctionalInterface
    private interface SaveFunction<T> {
        String apply(T entity, org.springframework.validation.BindingResult result);
    }

    @FunctionalInterface
    private interface EditFunction {
        String apply(Long id, Model model, RedirectAttributesModelMap redirectAttributes);
    }

    @FunctionalInterface
    private interface DeleteFunction {
        String apply(Long id, RedirectAttributesModelMap redirectAttributes);
    }
}

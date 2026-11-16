package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.model.vo.CategoriaVO;
import com.uniremington.cspizza.model.vo.InventarioVO;
import com.uniremington.cspizza.model.vo.ProductoVO;
import com.uniremington.cspizza.service.CategoriaService;
import com.uniremington.cspizza.service.InventarioService;
import com.uniremington.cspizza.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Verifica que los errores de las vistas web no producen paginas de error 500:
 * el usuario vuelve al formulario o al listado con un mensaje legible.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MvcErrorHandlingTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CategoriaService categoriaService;
    @Autowired private ProductoService productoService;
    @Autowired private InventarioService inventarioService;

    @Test
    void editarUnIdInexistenteRedirigeAlListadoSinErrorDelServidor() throws Exception {
        mockMvc.perform(get("/productos/editar/999999999").with(user("admin").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productos"));
    }

    @Test
    void elMensajeDelListadoMuestraElRegistroInexistente() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(get("/productos/editar/999999999")
                        .with(user("admin").roles("ADMIN"))
                        .session(session))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/productos")
                        .with(user("admin").roles("ADMIN"))
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no existe")));
    }

    @Test
    void guardarUnFormularioInvalidoVuelveAlFormularioConMensaje() throws Exception {
        mockMvc.perform(post("/productos/guardar")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("idCategoria", "1")
                        .param("nombre", "")
                        .param("precioUnitario", "1000")
                        .param("stock", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("productos/form"))
                .andExpect(content().string(containsString("El nombre es obligatorio.")));
    }

    @Test
    void guardarUnaRelacionInexistenteVuelveAlFormularioConMensaje() throws Exception {
        mockMvc.perform(post("/pedidos/guardar")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("idCliente", "999999")
                        .param("idUsuario", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("pedidos/form"))
                .andExpect(content().string(containsString("No existe cliente")));
    }

    @Test
    void crearUnInventarioSinCantidadActualUsaElStockDelProducto() throws Exception {
        ProductoVO producto = productoConStock(12);

        // El producto ya tiene inventario automatico; se elimina para crear uno por la API.
        InventarioVO automatico = inventarioService.buscarPorProducto(producto.getIdProducto()).get(0);
        inventarioService.eliminar(automatico.getIdInventario());

        mockMvc.perform(post("/api/inventarios")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idProducto\":" + producto.getIdProducto()
                                + ",\"cantidadMinima\":1,\"cantidadMaxima\":50,\"estado\":\"ACTIVO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidadActual").value(12));
    }

    @Test
    void actualizarUnInventarioSinCantidadActualConservaElStockDelProducto() throws Exception {
        ProductoVO producto = productoConStock(12);
        InventarioVO automatico = inventarioService.buscarPorProducto(producto.getIdProducto()).get(0);

        mockMvc.perform(put("/api/inventarios/" + automatico.getIdInventario())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idProducto\":" + producto.getIdProducto()
                                + ",\"cantidadMinima\":2,\"cantidadMaxima\":60,\"estado\":\"ACTIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadActual").value(12))
                .andExpect(jsonPath("$.cantidadMinima").value(2));
    }

    private ProductoVO productoConStock(int stock) {
        CategoriaVO categoria = new CategoriaVO();
        categoria.setNombre("Categoria de prueba " + System.nanoTime());
        ProductoVO producto = new ProductoVO();
        producto.setIdCategoria(categoriaService.guardar(categoria).getIdCategoria());
        producto.setNombre("Producto de prueba " + System.nanoTime());
        producto.setPrecioUnitario(new BigDecimal("15000.00"));
        producto.setStock(stock);
        return productoService.guardar(producto);
    }

    @Test
    void eliminarUnRegistroInexistenteMuestraMensajeEnElListado() throws Exception {
        mockMvc.perform(post("/categorias/eliminar/999999999")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categorias"));
    }
}

package com.uniremington.cspizza.config;

import com.uniremington.cspizza.model.vo.CategoriaVO;
import com.uniremington.cspizza.model.vo.ProductoVO;
import com.uniremington.cspizza.service.CategoriaService;
import com.uniremington.cspizza.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired private CategoriaService categoriaService;
    @Autowired private ProductoService productoService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void logoutFormUsesPostAndContainsCsrfToken() throws Exception {
        mockMvc.perform(get("/dashboard").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/admin"))
                .andExpect(content().string(containsString("action=\"/logout\"")))
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void eachRoleGetsItsDashboardAndUnauthenticatedUsersAreSentToLogin() throws Exception {
        mockMvc.perform(get("/dashboard").with(user("gerente").roles("GERENTE")))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/gerencia"));

        mockMvc.perform(get("/dashboard").with(user("empleado").roles("EMPLEADO")))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/empleado"));

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
    }

    @Test
    void logoutRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/logout").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidUserFieldsReturnTheFormWithValidationMessages() throws Exception {
        mockMvc.perform(post("/usuarios/guardar")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("nombreUsuario", "")
                        .param("nombre", "")
                        .param("apellido", "")
                        .param("rol", "")
                        .param("correo", "")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/form"))
                .andExpect(content().string(containsString("El nombre de usuario es obligatorio.")))
                .andExpect(content().string(containsString("La contraseña es obligatoria.")));
    }

    @Test
    void logoutInvalidatesSessionClearsCookieAndRedirectsToLogin() throws Exception {
        MockHttpSession session = new MockHttpSession();

        MvcResult result = mockMvc.perform(post("/logout")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login?logout"))
                .andReturn();

        assertTrue(session.isInvalid(), "logout must invalidate the HTTP session");
        assertTrue(Arrays.stream(result.getResponse().getCookies())
                        .anyMatch(cookie -> "JSESSIONID".equals(cookie.getName()) && cookie.getMaxAge() == 0),
                "logout must expire the JSESSIONID cookie");
    }

    @Test
    void protectedPagesAreNotCachedAndModuleAccessFollowsRoles() throws Exception {
        mockMvc.perform(get("/dashboard").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store")));

        mockMvc.perform(get("/facturas").with(user("empleado").roles("EMPLEADO")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/categorias").with(user("gerente").roles("GERENTE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/clientes").with(user("gerente").roles("GERENTE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/usuarios").with(user("empleado").roles("EMPLEADO")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void restValidationMissingRecordsAndUserPasswordSerializationAreSafe() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.nombre").exists());

        mockMvc.perform(post("/api/categorias")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"nombre\":\"API test category\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCategoria").exists());

        mockMvc.perform(put("/api/categorias/99999999")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"nombre\":\"Missing\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(get("/api/categorias").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/categorias/99999999")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        CategoriaVO category = new CategoriaVO();
        category.setNombre("REST conflict " + System.nanoTime());
        ProductoVO product = new ProductoVO();
        product.setIdCategoria(categoriaService.guardar(category).getIdCategoria());
        product.setNombre("REST conflict product " + System.nanoTime());
        product.setPrecioUnitario(java.math.BigDecimal.TEN);
        product.setStock(5);
        product = productoService.guardar(product);
        mockMvc.perform(post("/api/inventarios")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"idProducto\":" + product.getIdProducto()
                                + ",\"cantidadActual\":5,\"cantidadMinima\":0,"
                                + "\"cantidadMaxima\":10,\"estado\":\"ACTIVO\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        String username = "rest-test-" + System.nanoTime();
        mockMvc.perform(post("/api/usuarios")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"nombreUsuario\":\"" + username + "\",\"nombre\":\"REST\","
                                + "\"apellido\":\"Test\",\"rol\":\"EMPLEADO\","
                                + "\"correo\":\"" + username + "@example.test\","
                                + "\"password\":\"secret-test-password\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}

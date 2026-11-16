package com.uniremington.cspizza.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfFilter;

/**
 * Define la autenticacion con formulario, la autorizacion por roles y el
 * codificador de contrasenas utilizados por la aplicacion.
 * <p>
 * Las cuentas se leen de la tabla {@code usuarios} mediante {@code UsuarioDetailsService}
 * y se validan con el codificador delegado de contrasenas.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Configura las rutas publicas, las reglas por rol, el login y el logout. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/login", "/login/",
                    "/error", "/access-denied", "/access-denied/").permitAll()
                .requestMatchers("/admin/**", "/usuarios/**", "/categorias/**", "/detalle-pedidos/**",
                    "/api/usuarios/**", "/api/categorias/**", "/api/detalle-pedidos/**").hasRole("ADMIN")
                .requestMatchers("/gerencia/**", "/facturas/**", "/inventarios/**",
                    "/api/facturas/**", "/api/inventarios/**").hasAnyRole("ADMIN", "GERENTE")
                .requestMatchers("/empleado/**").hasRole("EMPLEADO")
                .requestMatchers("/clientes/**", "/api/clientes/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers("/dashboard", "/dashboard/**", "/productos/**", "/pedidos/**",
                    "/api/productos/**", "/api/pedidos/**")
                    .hasAnyRole("ADMIN", "GERENTE", "EMPLEADO")
                .anyRequest().hasAnyRole("ADMIN", "GERENTE", "EMPLEADO")
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .headers(headers -> headers.cacheControl(Customizer.withDefaults()))
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(responderDenegadoEnJsonParaLaApi()));

        SecurityFilterChain chain = http.build();
        configurarRespuestaDelFiltroCsrf(chain);
        return chain;
    }

    /**
     * El filtro CSRF responde con {@code sendError(403)}, que dispara la pagina de error de
     * Spring Boot y termina en un 405 porque esa pagina solo admite GET. Se sustituye por una
     * respuesta directa: 403 en JSON para la API y 403 con un mensaje breve en las vistas web.
     */
    private void configurarRespuestaDelFiltroCsrf(SecurityFilterChain chain) {
        chain.getFilters().stream()
                .filter(CsrfFilter.class::isInstance)
                .map(CsrfFilter.class::cast)
                .findFirst()
                .ifPresent(filtro -> filtro.setAccessDeniedHandler((request, response, exception) -> {
                    if (esRutaApi(request)) {
                        responderJson(response, HttpServletResponse.SC_FORBIDDEN,
                                "El token CSRF es ausente o no es valido.", request.getRequestURI());
                    } else {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType(MediaType.TEXT_HTML_VALUE);
                        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                        try (PrintWriter writer = response.getWriter()) {
                            writer.write("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">"
                                    + "<title>Acceso denegado</title></head><body>"
                                    + "<h1>403 - Acceso denegado</h1>"
                                    + "<p>El token CSRF es ausente o no es valido. Recargue la pagina e intente de nuevo.</p>"
                                    + "<p><a href=\"/dashboard\">Volver al inicio</a></p>"
                                    + "</body></html>");
                        }
                    }
                }));
    }

    /** Responde 403 en JSON cuando el recurso es de la API y 403 con mensaje breve en las vistas web. */
    private AccessDeniedHandler responderDenegadoEnJsonParaLaApi() {
        return (request, response, exception) -> {
            if (esRutaApi(request)) {
                responderJson(response, HttpServletResponse.SC_FORBIDDEN,
                        "No tiene permisos para acceder a este recurso.", request.getRequestURI());
                return;
            }
            responderHtmlConAccesoDenegado(response);
        };
    }

    /**
     * Responde en las vistas web con un mensaje breve cuando el usuario no tiene permisos.
     * Se responde directamente con 403 en lugar de reenviar a una vista, para no depender
     * de una plantilla que pueda no existir.
     */
    private void responderHtmlConAccesoDenegado(HttpServletResponse response) throws java.io.IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (PrintWriter writer = response.getWriter()) {
            writer.write("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">"
                    + "<title>Acceso denegado</title></head><body>"
                    + "<h1>403 - Acceso denegado</h1>"
                    + "<p>No tiene permisos para acceder a este recurso.</p>"
                    + "<p><a href=\"/dashboard\">Volver al inicio</a></p>"
                    + "</body></html>");
        }
    }

    private boolean esRutaApi(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    private void responderJson(HttpServletResponse response, int estado, String mensaje, String ruta)
            throws java.io.IOException {
        response.setStatus(estado);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (PrintWriter writer = response.getWriter()) {
            writer.write("{\"status\":" + estado + ",\"error\":\"Forbidden\",\"message\":\""
                    + mensaje + "\",\"path\":\"" + ruta + "\"}");
        }
    }

    /**
     * Proporciona el codificador de contrasenas: admite contrasenas en texto plano con
     * prefijo {@code {noop}} (las almacenadas en la tabla {@code usuarios} del prototipo) y
     * hashes BCrypt, de modo que el mismo codificador valide ambos formatos.
     *
     * @return codificador de contrasenas delegado
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return bcrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (rawPassword == null || encodedPassword == null) {
                    return false;
                }
                if (encodedPassword.startsWith("{noop}")) {
                    return encodedPassword.substring("{noop}".length()).equals(rawPassword.toString());
                }
                return bcrypt.matches(rawPassword, encodedPassword);
            }
        };
    }
}

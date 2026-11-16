package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.service.PedidoService;
import com.uniremington.cspizza.model.vo.PedidoVO;
import com.uniremington.cspizza.model.vo.InventarioVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * Atiende el inicio de sesion, la pagina inicial y la seleccion del dashboard
 * segun el rol de la autenticacion actual.
 */
@Controller
public class AuthMvcController {

    private PedidoService pedidoService;

    /** Constructor usado por Spring (inyección por constructor). */
    public AuthMvcController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /** Constructor sin argumentos mantenido para pruebas unitarias existentes. */
    public AuthMvcController() {
    }

    /** Setter mantenido para compatibilidad con pruebas unitarias existentes. */
    public void setPedidoService(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping({"/login", "/login/"})
    public String login() {
        return "auth/login";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping({"/dashboard", "/dashboard/"})
    public String dashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isGerente = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"));

        // KPIs del negocio
        if (pedidoService != null) {
            model.addAttribute("pedidosHoy", pedidoService.pedidosHoy());
            model.addAttribute("ventasHoy", pedidoService.ventasHoy());
            model.addAttribute("ticketPromedioHoy", pedidoService.ticketPromedioHoy());
            model.addAttribute("pedidosPendientes", pedidoService.pedidosPendientes());
            model.addAttribute("pedidosEnPreparacion", pedidoService.pedidosEnPreparacion());
            model.addAttribute("ultimosPedidos", pedidoService.ultimosCincoPedidos());
            model.addAttribute("productosBajoStock", pedidoService.productosBajoStock());
        } else {
            model.addAttribute("pedidosHoy", 0L);
            model.addAttribute("ventasHoy", java.math.BigDecimal.ZERO);
            model.addAttribute("ticketPromedioHoy", java.math.BigDecimal.ZERO);
            model.addAttribute("pedidosPendientes", java.util.List.of());
            model.addAttribute("pedidosEnPreparacion", java.util.List.of());
            model.addAttribute("ultimosPedidos", java.util.List.of());
            model.addAttribute("productosBajoStock", java.util.List.of());
        }

        if (isAdmin) {
            return "dashboard/admin";
        }

        if (isGerente) {
            return "dashboard/gerencia";
        }

        return "dashboard/empleado";
    }

    @GetMapping({"/access-denied", "/access-denied/"})
    public String accessDenied() {
        return "error/403";
    }
}

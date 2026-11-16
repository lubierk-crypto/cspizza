package com.uniremington.cspizza.controller.mvc;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;

@Controller
public class AuthMvcController {

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isGerente = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"));

        if (isAdmin) {
            return "dashboard/admin";
        }

        if (isGerente) {
            return "dashboard/gerencia";
        }

        return "dashboard/empleado";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
}

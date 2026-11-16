package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.service.ClienteService;
import com.uniremington.cspizza.model.vo.ClienteVO;
import com.uniremington.cspizza.service.exception.ConflictException;
import com.uniremington.cspizza.service.exception.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Gestiona las vistas CRUD de clientes bajo {@code /clientes}. */
@Controller
@RequestMapping("/clientes")
public class ClienteMvcController {

    private ClienteService clienteService;

    @Autowired
    public void setClienteService(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping({"", "/"})
    public String listar(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        return "clientes/lista";
    }

    @GetMapping({"/nuevo", "/nuevo/"})
    public String nuevo(Model model) {
        model.addAttribute("cliente", new ClienteVO());
        return "clientes/form";
    }

    @PostMapping({"/guardar", "/guardar/"})
    public String guardar(@Valid @ModelAttribute("cliente") ClienteVO cliente, BindingResult result) {
        if (result.hasErrors()) {
            return "clientes/form";
        }
        try {
            if (cliente.getIdCliente() == null) {
                clienteService.guardar(cliente);
            } else {
                clienteService.actualizar(cliente);
            }
        } catch (IllegalArgumentException | NotFoundException | ConflictException excepcion) {
            result.reject("cliente.error", excepcion.getMessage());
            return "clientes/form";
        }
        return "redirect:/clientes";
    }

    @GetMapping({"/editar/{id}", "/editar/{id}/"})
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return clienteService.buscarPorId(id)
                .map(cliente -> {
                    model.addAttribute("cliente", cliente);
                    return "clientes/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "El cliente solicitado no existe.");
                    return "redirect:/clientes";
                });
    }

    @PostMapping({"/eliminar/{id}", "/eliminar/{id}/"})
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.eliminar(id);
        } catch (NotFoundException excepcion) {
            redirectAttributes.addFlashAttribute("error", excepcion.getMessage());
        }
        return "redirect:/clientes";
    }
}

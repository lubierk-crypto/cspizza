package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.model.dao.ClienteDAO;
import com.uniremington.cspizza.model.vo.ClienteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/clientes")
public class ClienteMvcController {

    private ClienteDAO clienteDAO;

    @Autowired(required = false)
    public void setClienteDAO(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    @GetMapping
    public String listar(Model model) {
        if (clienteDAO != null) {
            model.addAttribute("clientes", clienteDAO.listarTodos());
        }
        return "clientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cliente", new ClienteVO());
        return "clientes/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute ClienteVO cliente) {
        if (clienteDAO != null) {
            if (cliente.getIdCliente() == null) {
                clienteDAO.guardar(cliente);
            } else {
                clienteDAO.actualizar(cliente);
            }
        }
        return "redirect:/clientes";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        if (clienteDAO != null) {
            clienteDAO.buscarPorId(id).ifPresent(cliente -> model.addAttribute("cliente", cliente));
        }
        return "clientes/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        if (clienteDAO != null) {
            clienteDAO.eliminar(id);
        }
        return "redirect:/clientes";
    }
}

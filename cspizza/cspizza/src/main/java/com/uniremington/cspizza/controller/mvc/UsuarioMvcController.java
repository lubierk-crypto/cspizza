package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.model.dao.UsuarioDAO;
import com.uniremington.cspizza.model.vo.UsuarioVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/usuarios")
public class UsuarioMvcController {

    private UsuarioDAO usuarioDAO;

    @Autowired(required = false)
    public void setUsuarioDAO(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @GetMapping
    public String listar(Model model) {
        if (usuarioDAO != null) {
            model.addAttribute("usuarios", usuarioDAO.listarTodos());
        }
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new UsuarioVO());
        return "usuarios/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute UsuarioVO usuario) {
        if (usuarioDAO != null) {
            if (usuario.getIdUsuario() == null) {
                usuarioDAO.guardar(usuario);
            } else {
                usuarioDAO.actualizar(usuario);
            }
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        if (usuarioDAO != null) {
            usuarioDAO.buscarPorId(id).ifPresent(usuario -> model.addAttribute("usuario", usuario));
        }
        return "usuarios/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        if (usuarioDAO != null) {
            usuarioDAO.eliminar(id);
        }
        return "redirect:/usuarios";
    }
}

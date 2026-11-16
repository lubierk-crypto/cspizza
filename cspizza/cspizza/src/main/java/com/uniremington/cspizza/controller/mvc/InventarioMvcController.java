package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.model.dao.InventarioDAO;
import com.uniremington.cspizza.model.vo.InventarioVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inventarios")
public class InventarioMvcController {

    private InventarioDAO inventarioDAO;

    @Autowired(required = false)
    public void setInventarioDAO(InventarioDAO inventarioDAO) {
        this.inventarioDAO = inventarioDAO;
    }

    @GetMapping
    public String listar(Model model) {
        if (inventarioDAO != null) {
            model.addAttribute("inventarios", inventarioDAO.listarTodos());
        }
        return "inventarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("inventario", new InventarioVO());
        return "inventarios/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute InventarioVO inventario) {
        if (inventarioDAO != null) {
            if (inventario.getIdInventario() == null) {
                inventarioDAO.guardar(inventario);
            } else {
                inventarioDAO.actualizar(inventario);
            }
        }
        return "redirect:/inventarios";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        if (inventarioDAO != null) {
            inventarioDAO.buscarPorId(id).ifPresent(inventario -> model.addAttribute("inventario", inventario));
        }
        return "inventarios/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        if (inventarioDAO != null) {
            inventarioDAO.eliminar(id);
        }
        return "redirect:/inventarios";
    }
}

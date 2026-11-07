package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.model.dao.CategoriaDAO;
import com.uniremington.cspizza.model.vo.CategoriaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/categorias")
public class CategoriaMvcController {

    private CategoriaDAO categoriaDAO;

    @Autowired(required = false)
    public void setCategoriaDAO(CategoriaDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    @GetMapping
    public String listar(Model model) {
        if (categoriaDAO != null) {
            model.addAttribute("categorias", categoriaDAO.listarTodos());
        }
        return "categorias/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("categoria", new CategoriaVO());
        return "categorias/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute CategoriaVO categoria) {
        if (categoriaDAO != null) {
            if (categoria.getIdCategoria() == null) {
                categoriaDAO.guardar(categoria);
            } else {
                categoriaDAO.actualizar(categoria);
            }
        }
        return "redirect:/categorias";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        if (categoriaDAO != null) {
            categoriaDAO.buscarPorId(id).ifPresent(categoria -> model.addAttribute("categoria", categoria));
        }
        return "categorias/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        if (categoriaDAO != null) {
            categoriaDAO.eliminar(id);
        }
        return "redirect:/categorias";
    }
}

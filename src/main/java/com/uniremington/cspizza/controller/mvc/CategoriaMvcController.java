package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.service.CategoriaService;
import com.uniremington.cspizza.model.vo.CategoriaVO;
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

/** Gestiona las vistas CRUD de categorias bajo {@code /categorias}. */
@Controller
@RequestMapping("/categorias")
public class CategoriaMvcController {

    private CategoriaService categoriaService;

    @Autowired
    public void setCategoriaService(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping({"", "/"})
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodos());
        return "categorias/lista";
    }

    @GetMapping({"/nuevo", "/nuevo/"})
    public String nuevo(Model model) {
        model.addAttribute("categoria", new CategoriaVO());
        return "categorias/form";
    }

    @PostMapping({"/guardar", "/guardar/"})
    public String guardar(@Valid @ModelAttribute("categoria") CategoriaVO categoria, BindingResult result) {
        if (result.hasErrors()) {
            return "categorias/form";
        }
        try {
            if (categoria.getIdCategoria() == null) {
                categoriaService.guardar(categoria);
            } else {
                categoriaService.actualizar(categoria);
            }
        } catch (IllegalArgumentException | NotFoundException | ConflictException excepcion) {
            result.reject("categoria.error", excepcion.getMessage());
            return "categorias/form";
        }
        return "redirect:/categorias";
    }

    @GetMapping({"/editar/{id}", "/editar/{id}/"})
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return categoriaService.buscarPorId(id)
                .map(categoria -> {
                    model.addAttribute("categoria", categoria);
                    return "categorias/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "La categoria solicitada no existe.");
                    return "redirect:/categorias";
                });
    }

    @PostMapping({"/eliminar/{id}", "/eliminar/{id}/"})
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoriaService.eliminar(id);
        } catch (NotFoundException excepcion) {
            redirectAttributes.addFlashAttribute("error", excepcion.getMessage());
        }
        return "redirect:/categorias";
    }
}

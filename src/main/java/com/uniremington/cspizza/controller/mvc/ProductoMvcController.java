package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.service.ProductoService;
import com.uniremington.cspizza.service.CategoriaService;
import com.uniremington.cspizza.model.vo.ProductoVO;
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

import java.util.List;

/** Gestiona las vistas CRUD de productos bajo {@code /productos}. */
@Controller
@RequestMapping("/productos")
public class ProductoMvcController {

    private ProductoService productoService;
    private CategoriaService categoriaService;

    /** Constructor usado por Spring (inyección por constructor). */
    @Autowired
    public ProductoMvcController(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    /** Constructor sin argumentos mantenido para pruebas unitarias existentes. */
    public ProductoMvcController() {
    }

    /** Setter mantenido para compatibilidad con pruebas unitarias existentes. */
    public void setProductoService(ProductoService productoService) {
        this.productoService = productoService;
    }

    /** Setter mantenido para compatibilidad con pruebas unitarias existentes. */
    public void setCategoriaService(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping({"", "/"})
    public String listar(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        return "productos/lista";
    }

    @GetMapping({"/nuevo", "/nuevo/"})
    public String nuevo(Model model) {
        model.addAttribute("producto", new ProductoVO());
        if (categoriaService != null) {
            model.addAttribute("categorias", categoriaService.listarTodos());
        }
        return "productos/form";
    }

    @PostMapping({"/guardar", "/guardar/"})
    public String guardar(@Valid @ModelAttribute("producto") ProductoVO producto, BindingResult result) {
        if (result.hasErrors()) {
            return "productos/form";
        }
        try {
            if (producto.getIdProducto() == null) {
                productoService.guardar(producto);
            } else {
                productoService.actualizar(producto);
            }
        } catch (IllegalArgumentException | NotFoundException | ConflictException excepcion) {
            result.reject("producto.error", excepcion.getMessage());
            return "productos/form";
        }
        return "redirect:/productos";
    }

    @GetMapping({"/editar/{id}", "/editar/{id}/"})
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return productoService.buscarPorId(id)
                .map(producto -> {
                    model.addAttribute("producto", producto);
                    if (categoriaService != null) {
                        model.addAttribute("categorias", categoriaService.listarTodos());
                    }
                    return "productos/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "El producto solicitado no existe.");
                    return "redirect:/productos";
                });
    }

    @PostMapping({"/eliminar/{id}", "/eliminar/{id}/"})
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminar(id);
        } catch (NotFoundException excepcion) {
            redirectAttributes.addFlashAttribute("error", excepcion.getMessage());
        }
        return "redirect:/productos";
    }

    @ModelAttribute("categorias")
    public List<CategoriaVO> categorias() {
        if (categoriaService == null) {
            return List.of();
        }
        return categoriaService.listarTodos();
    }
}

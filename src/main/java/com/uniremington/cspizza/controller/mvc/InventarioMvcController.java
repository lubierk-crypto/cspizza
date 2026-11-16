package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.service.InventarioService;
import com.uniremington.cspizza.service.ProductoService;
import com.uniremington.cspizza.model.vo.InventarioVO;
import com.uniremington.cspizza.model.vo.ProductoVO;
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

/** Gestiona las vistas CRUD de inventario bajo {@code /inventarios}. */
@Controller
@RequestMapping("/inventarios")
public class InventarioMvcController {

    private InventarioService inventarioService;
    private ProductoService productoService;

    /** Constructor usado por Spring (inyección por constructor). */
    @Autowired
    public InventarioMvcController(InventarioService inventarioService, ProductoService productoService) {
        this.inventarioService = inventarioService;
        this.productoService = productoService;
    }

    /** Constructor sin argumentos mantenido para pruebas unitarias existentes. */
    public InventarioMvcController() {
    }

    /** Setter mantenido para compatibilidad con pruebas unitarias existentes. */
    public void setInventarioService(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    /** Setter mantenido para compatibilidad con pruebas unitarias existentes. */
    public void setProductoService(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping({"", "/"})
    public String listar(Model model) {
        model.addAttribute("inventarios", inventarioService.listarTodos());
        return "inventarios/lista";
    }

    @GetMapping({"/nuevo", "/nuevo/"})
    public String nuevo(Model model) {
        model.addAttribute("inventario", new InventarioVO());
        if (productoService != null) {
            model.addAttribute("productos", productoService.listarTodos());
        }
        return "inventarios/form";
    }

    @PostMapping({"/guardar", "/guardar/"})
    public String guardar(@Valid @ModelAttribute("inventario") InventarioVO inventario, BindingResult result) {
        if (result.hasErrors()) {
            return "inventarios/form";
        }
        try {
            if (inventario.getIdInventario() == null) {
                inventarioService.guardar(inventario);
            } else {
                inventarioService.actualizar(inventario);
            }
        } catch (IllegalArgumentException | NotFoundException | ConflictException excepcion) {
            result.reject("inventario.error", excepcion.getMessage());
            return "inventarios/form";
        }
        return "redirect:/inventarios";
    }

    @GetMapping({"/editar/{id}", "/editar/{id}/"})
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return inventarioService.buscarPorId(id)
                .map(inventario -> {
                    model.addAttribute("inventario", inventario);
                    if (productoService != null) {
                        model.addAttribute("productos", productoService.listarTodos());
                    }
                    return "inventarios/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "El inventario solicitado no existe.");
                    return "redirect:/inventarios";
                });
    }

    @PostMapping({"/eliminar/{id}", "/eliminar/{id}/"})
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            inventarioService.eliminar(id);
        } catch (NotFoundException excepcion) {
            redirectAttributes.addFlashAttribute("error", excepcion.getMessage());
        }
        return "redirect:/inventarios";
    }

    @ModelAttribute("productos")
    public List<ProductoVO> productos() {
        if (productoService == null) {
            return List.of();
        }
        return productoService.listarTodos();
    }
}

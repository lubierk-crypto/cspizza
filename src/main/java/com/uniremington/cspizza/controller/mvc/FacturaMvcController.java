package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.service.FacturaService;
import com.uniremington.cspizza.service.PedidoService;
import com.uniremington.cspizza.model.vo.FacturaVO;
import com.uniremington.cspizza.model.vo.PedidoVO;
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

/** Gestiona las vistas CRUD de facturas bajo {@code /facturas}. */
@Controller
@RequestMapping("/facturas")
public class FacturaMvcController {

    private FacturaService facturaService;
    private PedidoService pedidoService;

    /** Constructor usado por Spring (inyección por constructor). */
    @Autowired
    public FacturaMvcController(FacturaService facturaService, PedidoService pedidoService) {
        this.facturaService = facturaService;
        this.pedidoService = pedidoService;
    }

    /** Constructor sin argumentos mantenido para pruebas unitarias existentes. */
    public FacturaMvcController() {
    }

    /** Setter mantenido para compatibilidad con pruebas unitarias existentes. */
    public void setFacturaService(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    /** Setter mantenido para compatibilidad con pruebas unitarias existentes. */
    public void setPedidoService(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping({"", "/"})
    public String listar(Model model) {
        model.addAttribute("facturas", facturaService.listarTodos());
        return "facturas/lista";
    }

    @GetMapping({"/nuevo", "/nuevo/"})
    public String nuevo(Model model) {
        model.addAttribute("factura", new FacturaVO());
        if (pedidoService != null) {
            model.addAttribute("pedidos", pedidoService.listarTodos());
        }
        return "facturas/form";
    }

    @PostMapping({"/guardar", "/guardar/"})
    public String guardar(@Valid @ModelAttribute("factura") FacturaVO factura, BindingResult result) {
        if (result.hasErrors()) {
            return "facturas/form";
        }
        try {
            if (factura.getIdFactura() == null) {
                facturaService.guardar(factura);
            } else {
                facturaService.actualizar(factura);
            }
        } catch (IllegalArgumentException | NotFoundException | ConflictException excepcion) {
            result.reject("factura.error", excepcion.getMessage());
            return "facturas/form";
        }
        return "redirect:/facturas";
    }

    @GetMapping({"/editar/{id}", "/editar/{id}/"})
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return facturaService.buscarPorId(id)
                .map(factura -> {
                    model.addAttribute("factura", factura);
                    if (pedidoService != null) {
                        model.addAttribute("pedidos", pedidoService.listarTodos());
                    }
                    return "facturas/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "La factura solicitada no existe.");
                    return "redirect:/facturas";
                });
    }

    @PostMapping({"/eliminar/{id}", "/eliminar/{id}/"})
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            facturaService.eliminar(id);
        } catch (NotFoundException excepcion) {
            redirectAttributes.addFlashAttribute("error", excepcion.getMessage());
        }
        return "redirect:/facturas";
    }

    @ModelAttribute("pedidos")
    public List<PedidoVO> pedidos() {
        if (pedidoService == null) {
            return List.of();
        }
        return pedidoService.listarTodos();
    }
}

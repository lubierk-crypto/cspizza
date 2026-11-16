package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.service.DetallePedidoService;
import com.uniremington.cspizza.model.vo.DetallePedidoVO;
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

/** Gestiona las vistas CRUD de detalles de pedido bajo {@code /detalle-pedidos}. */
@Controller
@RequestMapping("/detalle-pedidos")
public class DetallePedidoMvcController {

    private DetallePedidoService detallePedidoService;

    @Autowired
    public void setDetallePedidoService(DetallePedidoService detallePedidoService) {
        this.detallePedidoService = detallePedidoService;
    }

    @GetMapping({"", "/"})
    public String listar(Model model) {
        model.addAttribute("detallePedidos", detallePedidoService.listarTodos());
        return "detalle-pedidos/lista";
    }

    @GetMapping({"/nuevo", "/nuevo/"})
    public String nuevo(Model model) {
        model.addAttribute("detallePedido", new DetallePedidoVO());
        model.addAttribute("productos", detallePedidoService.listarProductos());
        model.addAttribute("pedidos", detallePedidoService.listarPedidos());
        return "detalle-pedidos/form";
    }

    @PostMapping({"/guardar", "/guardar/"})
    public String guardar(@Valid @ModelAttribute("detallePedido") DetallePedidoVO detallePedido,
                          BindingResult result) {
        if (result.hasErrors()) {
            return "detalle-pedidos/form";
        }
        try {
            if (detallePedido.getIdDetallePedido() == null) {
                detallePedidoService.guardar(detallePedido);
            } else {
                detallePedidoService.actualizar(detallePedido);
            }
        } catch (IllegalArgumentException | NotFoundException | ConflictException excepcion) {
            result.reject("detallePedido.error", excepcion.getMessage());
            return "detalle-pedidos/form";
        }
        return "redirect:/detalle-pedidos";
    }

    @GetMapping({"/editar/{id}", "/editar/{id}/"})
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return detallePedidoService.buscarPorId(id)
                .map(detallePedido -> {
                    model.addAttribute("detallePedido", detallePedido);
                    model.addAttribute("productos", detallePedidoService.listarProductos());
                    model.addAttribute("pedidos", detallePedidoService.listarPedidos());
                    return "detalle-pedidos/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "El detalle de pedido solicitado no existe.");
                    return "redirect:/detalle-pedidos";
                });
    }

    @PostMapping({"/eliminar/{id}", "/eliminar/{id}/"})
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            detallePedidoService.eliminar(id);
        } catch (NotFoundException excepcion) {
            redirectAttributes.addFlashAttribute("error", excepcion.getMessage());
        }
        return "redirect:/detalle-pedidos";
    }

    @GetMapping({"/buscar-por-pedido/{id}", "/buscar-por-pedido/{id}/"})
    public String buscarPorPedido(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("detallePedidos", detallePedidoService.buscarPorPedido(id));
            model.addAttribute("pedidoId", id);
            return "detalle-pedidos/por-pedido";
        } catch (Exception excepcion) {
            redirectAttributes.addFlashAttribute("error", "No se pudieron cargar los productos del pedido.");
            return "redirect:/pedidos";
        }
    }
}

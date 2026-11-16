package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.model.dao.DetallePedidoDAO;
import com.uniremington.cspizza.model.vo.DetallePedidoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/detalle-pedidos")
public class DetallePedidoMvcController {

    private DetallePedidoDAO detallePedidoDAO;

    @Autowired(required = false)
    public void setDetallePedidoDAO(DetallePedidoDAO detallePedidoDAO) {
        this.detallePedidoDAO = detallePedidoDAO;
    }

    @GetMapping
    public String listar(Model model) {
        if (detallePedidoDAO != null) {
            model.addAttribute("detallePedidos", detallePedidoDAO.listarTodos());
        }
        return "detalle-pedidos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("detallePedido", new DetallePedidoVO());
        return "detalle-pedidos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute DetallePedidoVO detallePedido) {
        if (detallePedidoDAO != null) {
            if (detallePedido.getIdDetallePedido() == null) {
                detallePedidoDAO.guardar(detallePedido);
            } else {
                detallePedidoDAO.actualizar(detallePedido);
            }
        }
        return "redirect:/detalle-pedidos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        if (detallePedidoDAO != null) {
            detallePedidoDAO.buscarPorId(id).ifPresent(detallePedido -> model.addAttribute("detallePedido", detallePedido));
        }
        return "detalle-pedidos/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        if (detallePedidoDAO != null) {
            detallePedidoDAO.eliminar(id);
        }
        return "redirect:/detalle-pedidos";
    }
}

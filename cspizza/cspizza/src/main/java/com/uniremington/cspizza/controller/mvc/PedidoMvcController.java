package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.model.dao.PedidoDAO;
import com.uniremington.cspizza.model.vo.PedidoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pedidos")
public class PedidoMvcController {

    private PedidoDAO pedidoDAO;

    @Autowired(required = false)
    public void setPedidoDAO(PedidoDAO pedidoDAO) {
        this.pedidoDAO = pedidoDAO;
    }

    @GetMapping
    public String listar(Model model) {
        if (pedidoDAO != null) {
            model.addAttribute("pedidos", pedidoDAO.listarTodos());
        }
        return "pedidos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("pedido", new PedidoVO());
        return "pedidos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute PedidoVO pedido) {
        if (pedidoDAO != null) {
            if (pedido.getIdPedido() == null) {
                pedidoDAO.guardar(pedido);
            } else {
                pedidoDAO.actualizar(pedido);
            }
        }
        return "redirect:/pedidos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        if (pedidoDAO != null) {
            pedidoDAO.buscarPorId(id).ifPresent(pedido -> model.addAttribute("pedido", pedido));
        }
        return "pedidos/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        if (pedidoDAO != null) {
            pedidoDAO.eliminar(id);
        }
        return "redirect:/pedidos";
    }
}

package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.model.dao.FacturaDAO;
import com.uniremington.cspizza.model.vo.FacturaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/facturas")
public class FacturaMvcController {

    private FacturaDAO facturaDAO;

    @Autowired(required = false)
    public void setFacturaDAO(FacturaDAO facturaDAO) {
        this.facturaDAO = facturaDAO;
    }

    @GetMapping
    public String listar(Model model) {
        if (facturaDAO != null) {
            model.addAttribute("facturas", facturaDAO.listarTodos());
        }
        return "facturas/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("factura", new FacturaVO());
        return "facturas/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute FacturaVO factura) {
        if (facturaDAO != null) {
            if (factura.getIdFactura() == null) {
                facturaDAO.guardar(factura);
            } else {
                facturaDAO.actualizar(factura);
            }
        }
        return "redirect:/facturas";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        if (facturaDAO != null) {
            facturaDAO.buscarPorId(id).ifPresent(factura -> model.addAttribute("factura", factura));
        }
        return "facturas/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        if (facturaDAO != null) {
            facturaDAO.eliminar(id);
        }
        return "redirect:/facturas";
    }
}

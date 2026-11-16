package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.model.dao.ProductoDAO;
import com.uniremington.cspizza.model.vo.ProductoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/productos")
public class ProductoMvcController {

    private ProductoDAO productoDAO;

    @Autowired(required = false)
    public void setProductoDAO(ProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
    }

    @GetMapping
    public String listar(Model model) {
        if (productoDAO != null) {
            model.addAttribute("productos", productoDAO.listarTodos());
        }
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("producto", new ProductoVO());
        return "productos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute ProductoVO producto) {
        if (productoDAO != null) {
            if (producto.getIdProducto() == null) {
                productoDAO.guardar(producto);
            } else {
                productoDAO.actualizar(producto);
            }
        }
        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        if (productoDAO != null) {
            productoDAO.buscarPorId(id).ifPresent(producto -> model.addAttribute("producto", producto));
        }
        return "productos/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        if (productoDAO != null) {
            productoDAO.eliminar(id);
        }
        return "redirect:/productos";
    }
}

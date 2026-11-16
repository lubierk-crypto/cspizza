package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.model.dao.ProductoDAO;
import com.uniremington.cspizza.model.vo.ProductoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    private ProductoDAO productoDAO;

    @Autowired(required = false)
    public void setProductoDAO(ProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
    }

    @GetMapping
    public List<ProductoVO> listar() {
        if (productoDAO == null) {
            return List.of();
        }
        return productoDAO.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoVO> buscarPorId(@PathVariable Long id) {
        if (productoDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return productoDAO.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductoVO> guardar(@RequestBody ProductoVO producto) {
        if (productoDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(productoDAO.guardar(producto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoVO> actualizar(@PathVariable Long id, @RequestBody ProductoVO producto) {
        if (productoDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        producto.setIdProducto(id);
        return ResponseEntity.ok(productoDAO.actualizar(producto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (productoDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        productoDAO.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

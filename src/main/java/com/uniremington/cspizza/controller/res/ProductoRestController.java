package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.service.ProductoService;
import com.uniremington.cspizza.model.vo.ProductoVO;
import jakarta.validation.Valid;
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

/** Expone operaciones REST CRUD de productos en {@code /api/productos}. */
@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    private ProductoService productoService;

    @Autowired
    public void setProductoService(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<ProductoVO> listar() {
        if (productoService == null) {
            return List.of();
        }
        return productoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoVO> buscarPorId(@PathVariable Long id) {
        if (productoService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return productoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductoVO> guardar(@Valid @RequestBody ProductoVO producto) {
        if (productoService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.guardar(producto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoVO> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoVO producto) {
        if (productoService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        producto.setIdProducto(id);
        return ResponseEntity.ok(productoService.actualizar(producto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (productoService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.service.CategoriaService;
import com.uniremington.cspizza.model.vo.CategoriaVO;
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

/** Expone operaciones REST CRUD de categorias en {@code /api/categorias}. */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaRestController {

    private CategoriaService categoriaService;

    @Autowired
    public void setCategoriaService(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaVO> listar() {
        if (categoriaService == null) {
            return List.of();
        }
        return categoriaService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaVO> buscarPorId(@PathVariable Long id) {
        if (categoriaService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return categoriaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoriaVO> guardar(@Valid @RequestBody CategoriaVO categoria) {
        if (categoriaService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.guardar(categoria));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaVO> actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaVO categoria) {
        if (categoriaService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        categoria.setIdCategoria(id);
        return ResponseEntity.ok(categoriaService.actualizar(categoria));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (categoriaService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

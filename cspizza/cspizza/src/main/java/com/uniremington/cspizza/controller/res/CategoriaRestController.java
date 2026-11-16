package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.model.dao.CategoriaDAO;
import com.uniremington.cspizza.model.vo.CategoriaVO;
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
@RequestMapping("/api/categorias")
public class CategoriaRestController {

    private CategoriaDAO categoriaDAO;

    @Autowired(required = false)
    public void setCategoriaDAO(CategoriaDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    @GetMapping
    public List<CategoriaVO> listar() {
        if (categoriaDAO == null) {
            return List.of();
        }
        return categoriaDAO.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaVO> buscarPorId(@PathVariable Long id) {
        if (categoriaDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return categoriaDAO.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoriaVO> guardar(@RequestBody CategoriaVO categoria) {
        if (categoriaDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaDAO.guardar(categoria));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaVO> actualizar(@PathVariable Long id, @RequestBody CategoriaVO categoria) {
        if (categoriaDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        categoria.setIdCategoria(id);
        return ResponseEntity.ok(categoriaDAO.actualizar(categoria));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (categoriaDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        categoriaDAO.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

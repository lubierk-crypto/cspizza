package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.service.InventarioService;
import com.uniremington.cspizza.model.vo.InventarioVO;
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

/** Expone operaciones REST CRUD de inventario en {@code /api/inventarios}. */
@RestController
@RequestMapping("/api/inventarios")
public class InventarioRestController {

    private InventarioService inventarioService;

    @Autowired
    public void setInventarioService(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public List<InventarioVO> listar() {
        if (inventarioService == null) {
            return List.of();
        }
        return inventarioService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioVO> buscarPorId(@PathVariable Long id) {
        if (inventarioService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return inventarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<InventarioVO> guardar(@Valid @RequestBody InventarioVO inventario) {
        if (inventarioService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.guardar(inventario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventarioVO> actualizar(@PathVariable Long id, @Valid @RequestBody InventarioVO inventario) {
        if (inventarioService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        inventario.setIdInventario(id);
        return ResponseEntity.ok(inventarioService.actualizar(inventario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (inventarioService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        inventarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

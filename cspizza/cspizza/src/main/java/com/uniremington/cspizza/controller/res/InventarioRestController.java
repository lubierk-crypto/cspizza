package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.model.dao.InventarioDAO;
import com.uniremington.cspizza.model.vo.InventarioVO;
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
@RequestMapping("/api/inventarios")
public class InventarioRestController {

    private InventarioDAO inventarioDAO;

    @Autowired(required = false)
    public void setInventarioDAO(InventarioDAO inventarioDAO) {
        this.inventarioDAO = inventarioDAO;
    }

    @GetMapping
    public List<InventarioVO> listar() {
        if (inventarioDAO == null) {
            return List.of();
        }
        return inventarioDAO.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioVO> buscarPorId(@PathVariable Long id) {
        if (inventarioDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return inventarioDAO.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<InventarioVO> guardar(@RequestBody InventarioVO inventario) {
        if (inventarioDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioDAO.guardar(inventario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventarioVO> actualizar(@PathVariable Long id, @RequestBody InventarioVO inventario) {
        if (inventarioDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        inventario.setIdInventario(id);
        return ResponseEntity.ok(inventarioDAO.actualizar(inventario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (inventarioDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        inventarioDAO.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

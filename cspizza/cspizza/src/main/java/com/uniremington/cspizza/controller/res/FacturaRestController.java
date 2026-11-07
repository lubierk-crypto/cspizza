package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.model.dao.FacturaDAO;
import com.uniremington.cspizza.model.vo.FacturaVO;
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
@RequestMapping("/api/facturas")
public class FacturaRestController {

    private FacturaDAO facturaDAO;

    @Autowired(required = false)
    public void setFacturaDAO(FacturaDAO facturaDAO) {
        this.facturaDAO = facturaDAO;
    }

    @GetMapping
    public List<FacturaVO> listar() {
        if (facturaDAO == null) {
            return List.of();
        }
        return facturaDAO.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaVO> buscarPorId(@PathVariable Long id) {
        if (facturaDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return facturaDAO.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FacturaVO> guardar(@RequestBody FacturaVO factura) {
        if (facturaDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaDAO.guardar(factura));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacturaVO> actualizar(@PathVariable Long id, @RequestBody FacturaVO factura) {
        if (facturaDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        factura.setIdFactura(id);
        return ResponseEntity.ok(facturaDAO.actualizar(factura));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (facturaDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        facturaDAO.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

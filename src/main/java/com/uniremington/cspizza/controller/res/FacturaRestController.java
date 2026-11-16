package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.service.FacturaService;
import com.uniremington.cspizza.model.vo.FacturaVO;
import com.uniremington.cspizza.service.exception.ConflictException;
import com.uniremington.cspizza.service.exception.NotFoundException;
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

/** Expone operaciones REST CRUD de facturas en {@code /api/facturas}. */
@RestController
@RequestMapping("/api/facturas")
public class FacturaRestController {

    private FacturaService facturaService;

    @Autowired
    public void setFacturaService(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @GetMapping
    public List<FacturaVO> listar() {
        if (facturaService == null) {
            return List.of();
        }
        return facturaService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaVO> buscarPorId(@PathVariable Long id) {
        if (facturaService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return facturaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FacturaVO> guardar(@Valid @RequestBody FacturaVO factura) {
        if (facturaService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaService.guardar(factura));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacturaVO> actualizar(@PathVariable Long id, @Valid @RequestBody FacturaVO factura) {
        if (facturaService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        factura.setIdFactura(id);
        return ResponseEntity.ok(facturaService.actualizar(factura));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (facturaService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        facturaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Genera una factura a partir de un pedido existente.
     * El número de factura se asigna automáticamente de forma consecutiva.
     */
    @PostMapping("/desde-pedido/{idPedido}")
    public ResponseEntity<FacturaVO> generarDesdePedido(@PathVariable Long idPedido) {
        if (facturaService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        try {
            FacturaVO factura = facturaService.generarDesdePedido(idPedido);
            return ResponseEntity.status(HttpStatus.CREATED).body(factura);
        } catch (NotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ConflictException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}

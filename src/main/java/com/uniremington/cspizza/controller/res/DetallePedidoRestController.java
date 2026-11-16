package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.service.DetallePedidoService;
import com.uniremington.cspizza.model.vo.DetallePedidoVO;
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

/** Expone operaciones REST CRUD de detalles de pedido en {@code /api/detalle-pedidos}. */
@RestController
@RequestMapping("/api/detalle-pedidos")
public class DetallePedidoRestController {

    private DetallePedidoService detallePedidoService;

    @Autowired
    public void setDetallePedidoService(DetallePedidoService detallePedidoService) {
        this.detallePedidoService = detallePedidoService;
    }

    @GetMapping
    public List<DetallePedidoVO> listar() {
        if (detallePedidoService == null) {
            return List.of();
        }
        return detallePedidoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetallePedidoVO> buscarPorId(@PathVariable Long id) {
        if (detallePedidoService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return detallePedidoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DetallePedidoVO> guardar(@Valid @RequestBody DetallePedidoVO detallePedido) {
        if (detallePedidoService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(detallePedidoService.guardar(detallePedido));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetallePedidoVO> actualizar(@PathVariable Long id, @Valid @RequestBody DetallePedidoVO detallePedido) {
        if (detallePedidoService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        detallePedido.setIdDetallePedido(id);
        return ResponseEntity.ok(detallePedidoService.actualizar(detallePedido));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (detallePedidoService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        detallePedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

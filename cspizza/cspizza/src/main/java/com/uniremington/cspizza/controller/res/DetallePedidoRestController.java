package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.model.dao.DetallePedidoDAO;
import com.uniremington.cspizza.model.vo.DetallePedidoVO;
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
@RequestMapping("/api/detalle-pedidos")
public class DetallePedidoRestController {

    private DetallePedidoDAO detallePedidoDAO;

    @Autowired(required = false)
    public void setDetallePedidoDAO(DetallePedidoDAO detallePedidoDAO) {
        this.detallePedidoDAO = detallePedidoDAO;
    }

    @GetMapping
    public List<DetallePedidoVO> listar() {
        if (detallePedidoDAO == null) {
            return List.of();
        }
        return detallePedidoDAO.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetallePedidoVO> buscarPorId(@PathVariable Long id) {
        if (detallePedidoDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return detallePedidoDAO.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DetallePedidoVO> guardar(@RequestBody DetallePedidoVO detallePedido) {
        if (detallePedidoDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(detallePedidoDAO.guardar(detallePedido));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetallePedidoVO> actualizar(@PathVariable Long id, @RequestBody DetallePedidoVO detallePedido) {
        if (detallePedidoDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        detallePedido.setIdDetallePedido(id);
        return ResponseEntity.ok(detallePedidoDAO.actualizar(detallePedido));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (detallePedidoDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        detallePedidoDAO.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

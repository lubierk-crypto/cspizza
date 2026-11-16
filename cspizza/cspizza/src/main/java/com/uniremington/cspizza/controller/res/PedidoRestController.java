package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.model.dao.PedidoDAO;
import com.uniremington.cspizza.model.vo.PedidoVO;
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
@RequestMapping("/api/pedidos")
public class PedidoRestController {

    private PedidoDAO pedidoDAO;

    @Autowired(required = false)
    public void setPedidoDAO(PedidoDAO pedidoDAO) {
        this.pedidoDAO = pedidoDAO;
    }

    @GetMapping
    public List<PedidoVO> listar() {
        if (pedidoDAO == null) {
            return List.of();
        }
        return pedidoDAO.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoVO> buscarPorId(@PathVariable Long id) {
        if (pedidoDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return pedidoDAO.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PedidoVO> guardar(@RequestBody PedidoVO pedido) {
        if (pedidoDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoDAO.guardar(pedido));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PedidoVO> actualizar(@PathVariable Long id, @RequestBody PedidoVO pedido) {
        if (pedidoDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        pedido.setIdPedido(id);
        return ResponseEntity.ok(pedidoDAO.actualizar(pedido));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (pedidoDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        pedidoDAO.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

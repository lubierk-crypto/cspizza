package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.model.dao.ClienteDAO;
import com.uniremington.cspizza.model.vo.ClienteVO;
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
@RequestMapping("/api/clientes")
public class ClienteRestController {

    private ClienteDAO clienteDAO;

    @Autowired(required = false)
    public void setClienteDAO(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    @GetMapping
    public List<ClienteVO> listar() {
        if (clienteDAO == null) {
            return List.of();
        }
        return clienteDAO.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteVO> buscarPorId(@PathVariable Long id) {
        if (clienteDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return clienteDAO.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ClienteVO> guardar(@RequestBody ClienteVO cliente) {
        if (clienteDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteDAO.guardar(cliente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteVO> actualizar(@PathVariable Long id, @RequestBody ClienteVO cliente) {
        if (clienteDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        cliente.setIdCliente(id);
        return ResponseEntity.ok(clienteDAO.actualizar(cliente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (clienteDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        clienteDAO.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

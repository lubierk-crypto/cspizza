package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.service.ClienteService;
import com.uniremington.cspizza.model.vo.ClienteVO;
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

/** Expone operaciones REST CRUD de clientes en {@code /api/clientes}. */
@RestController
@RequestMapping("/api/clientes")
public class ClienteRestController {

    private ClienteService clienteService;

    @Autowired
    public void setClienteService(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<ClienteVO> listar() {
        if (clienteService == null) {
            return List.of();
        }
        return clienteService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteVO> buscarPorId(@PathVariable Long id) {
        if (clienteService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return clienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ClienteVO> guardar(@Valid @RequestBody ClienteVO cliente) {
        if (clienteService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.guardar(cliente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteVO> actualizar(@PathVariable Long id, @Valid @RequestBody ClienteVO cliente) {
        if (clienteService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        cliente.setIdCliente(id);
        return ResponseEntity.ok(clienteService.actualizar(cliente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (clienteService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

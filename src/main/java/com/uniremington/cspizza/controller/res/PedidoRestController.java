package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.service.PedidoService;
import com.uniremington.cspizza.model.vo.PedidoVO;
import com.uniremington.cspizza.model.vo.PedidoNuevoVO;
import com.uniremington.cspizza.service.exception.ConflictException;
import com.uniremington.cspizza.service.exception.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Expone operaciones REST CRUD de pedidos en {@code /api/pedidos}. */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoRestController {

    private PedidoService pedidoService;

    @Autowired
    public void setPedidoService(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<PedidoVO> listar() {
        if (pedidoService == null) {
            return List.of();
        }
        return pedidoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoVO> buscarPorId(@PathVariable Long id) {
        if (pedidoService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return pedidoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PedidoVO> guardar(@Valid @RequestBody PedidoVO pedido) {
        if (pedidoService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.guardar(pedido));
    }

    /**
     * Crea un pedido completo con productos desde el punto de venta.
     * El usuario se resuelve desde la autenticación (no se envía en el cuerpo).
     */
    @PostMapping("/completo")
    public ResponseEntity<PedidoVO> completo(
            @Valid @RequestBody PedidoNuevoVO entrada,
            Authentication authentication) {
        if (pedidoService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        String nombreUsuario = authentication != null ? authentication.getName() : null;
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            PedidoVO creado = pedidoService.crearConDetalles(entrada, nombreUsuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException excepcion) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (NotFoundException excepcion) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ConflictException excepcion) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PedidoVO> actualizar(@PathVariable Long id, @Valid @RequestBody PedidoVO pedido) {
        if (pedidoService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        pedido.setIdPedido(id);
        return ResponseEntity.ok(pedidoService.actualizar(pedido));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (pedidoService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

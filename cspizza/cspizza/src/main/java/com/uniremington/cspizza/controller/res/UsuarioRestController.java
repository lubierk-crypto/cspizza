package com.uniremington.cspizza.controller.res;

import com.uniremington.cspizza.model.dao.UsuarioDAO;
import com.uniremington.cspizza.model.vo.UsuarioVO;
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
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    private UsuarioDAO usuarioDAO;

    @Autowired(required = false)
    public void setUsuarioDAO(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @GetMapping
    public List<UsuarioVO> listar() {
        if (usuarioDAO == null) {
            return List.of();
        }
        return usuarioDAO.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioVO> buscarPorId(@PathVariable Long id) {
        if (usuarioDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return usuarioDAO.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UsuarioVO> guardar(@RequestBody UsuarioVO usuario) {
        if (usuarioDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioDAO.guardar(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioVO> actualizar(@PathVariable Long id, @RequestBody UsuarioVO usuario) {
        if (usuarioDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        usuario.setIdUsuario(id);
        return ResponseEntity.ok(usuarioDAO.actualizar(usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (usuarioDAO == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        usuarioDAO.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

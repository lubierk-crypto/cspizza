package com.uniremington.cspizza.model.repository;

import com.uniremington.cspizza.model.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findFirstByNombreUsuario(String nombreUsuario);
    Optional<UsuarioEntity> findFirstByCorreoIgnoreCase(String correo);
    List<UsuarioEntity> findAllByRol(String rol);
    List<UsuarioEntity> findAllByEstado(String estado);
}

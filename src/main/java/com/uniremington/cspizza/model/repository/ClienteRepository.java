package com.uniremington.cspizza.model.repository;

import com.uniremington.cspizza.model.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {
    List<ClienteEntity> findAllByEstado(String estado);
    Optional<ClienteEntity> findFirstByCorreo(String correo);
    Optional<ClienteEntity> findFirstByTelefono(String telefono);
    Optional<ClienteEntity> findByNombre(String nombre);
    Optional<ClienteEntity> findFirstByNombreIgnoreCase(String nombre);
}

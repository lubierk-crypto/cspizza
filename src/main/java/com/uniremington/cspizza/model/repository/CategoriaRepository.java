package com.uniremington.cspizza.model.repository;

import com.uniremington.cspizza.model.entity.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Long> {
    Optional<CategoriaEntity> findFirstByNombre(String nombre);
    List<CategoriaEntity> findAllByEstado(String estado);
}

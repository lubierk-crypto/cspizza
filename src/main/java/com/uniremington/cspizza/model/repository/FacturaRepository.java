package com.uniremington.cspizza.model.repository;

import com.uniremington.cspizza.model.entity.FacturaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FacturaRepository extends JpaRepository<FacturaEntity, Long> {
    Optional<FacturaEntity> findFirstByNumeroFactura(String numeroFactura);
    Optional<FacturaEntity> findFirstByPedido_IdPedido(Long idPedido);
    List<FacturaEntity> findAllByEstado(String estado);
    List<FacturaEntity> findAllByFechaEmisionBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT f FROM FacturaEntity f ORDER BY f.idFactura DESC")
    Optional<FacturaEntity> findTopByOrderByIdFacturaDesc();
}

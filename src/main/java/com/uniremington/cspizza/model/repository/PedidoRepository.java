package com.uniremington.cspizza.model.repository;

import com.uniremington.cspizza.model.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {
    List<PedidoEntity> findAllByCliente_IdCliente(Long idCliente);
    List<PedidoEntity> findAllByUsuario_IdUsuario(Long idUsuario);
    List<PedidoEntity> findAllByEstado(String estado);
    List<PedidoEntity> findAllByFechaPedidoBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT COUNT(p) FROM PedidoEntity p WHERE p.fechaPedido >= CURRENT_DATE")
    Long countPedidosHoy();

    @Query("SELECT COALESCE(SUM(p.totalPedido), 0) FROM PedidoEntity p WHERE p.fechaPedido >= CURRENT_DATE")
    BigDecimal sumVentasHoy();

    @Query("SELECT AVG(p.totalPedido) FROM PedidoEntity p WHERE p.fechaPedido >= CURRENT_DATE")
    BigDecimal avgTicketHoy();

    @Query("SELECT p FROM PedidoEntity p WHERE p.estado = 'PENDIENTE' ORDER BY p.fechaPedido DESC")
    List<PedidoEntity> findAllByEstadoPendienteOrderByFechaPedidoDesc();

    @Query("SELECT p FROM PedidoEntity p WHERE p.estado = 'EN_PREPARACION' ORDER BY p.fechaPedido DESC")
    List<PedidoEntity> findAllByEstadoEnPreparacionOrderByFechaPedidoDesc();

    @Query("SELECT p FROM PedidoEntity p ORDER BY p.fechaPedido DESC")
    List<PedidoEntity> findAllOrderByFechaPedidoDesc();

    @Query("SELECT p FROM PedidoEntity p WHERE p.estado IN ('PENDIENTE', 'EN_PREPARACION', 'LISTO') ORDER BY p.fechaPedido ASC")
    List<PedidoEntity> findAllByEstadoInOrderByFechaPedidoAsc(List<String> estados);

    /**
     * Consulta pedidos con JOIN FETCH a detalles para evitar problema N+1.
     * Soporta filtros opcionales por estado, tipo de entrega, rango de fechas y nombre de cliente.
     */
    @Query("""
        SELECT DISTINCT p FROM PedidoEntity p
        LEFT JOIN FETCH p.detalles d
        LEFT JOIN FETCH d.producto
        WHERE (:estado IS NULL OR p.estado = :estado)
          AND (:tipoEntrega IS NULL OR p.tipoEntrega = :tipoEntrega)
          AND (:fechaDesde IS NULL OR p.fechaPedido >= :fechaDesde)
          AND (:fechaHasta IS NULL OR p.fechaPedido <= :fechaHasta)
          AND (:busca IS NULL OR LOWER(p.cliente.nombre) LIKE LOWER(CONCAT('%', :busca, '%'))
               OR LOWER(p.cliente.apellido) LIKE LOWER(CONCAT('%', :busca, '%')))
        ORDER BY p.fechaPedido DESC
    """)
    List<PedidoEntity> findConFiltros(
        @Param("estado") String estado,
        @Param("tipoEntrega") String tipoEntrega,
        @Param("fechaDesde") LocalDateTime fechaDesde,
        @Param("fechaHasta") LocalDateTime fechaHasta,
        @Param("busca") String busca
    );
}

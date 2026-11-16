package com.uniremington.cspizza.model.repository;

import com.uniremington.cspizza.model.entity.DetallePedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedidoEntity, Long> {
    List<DetallePedidoEntity> findAllByPedido_IdPedido(Long idPedido);
    List<DetallePedidoEntity> findAllByProducto_IdProducto(Long idProducto);
    List<DetallePedidoEntity> findAllByPedido_IdPedidoAndProducto_IdProducto(Long idPedido, Long idProducto);
}

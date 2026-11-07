package com.uniremington.cspizza.model.dao;

import com.uniremington.cspizza.model.vo.DetallePedidoVO;

import java.util.List;

public interface DetallePedidoDAO extends GenericDAO<DetallePedidoVO, Long> {
    List<DetallePedidoVO> buscarPorPedido(Long idPedido);

    List<DetallePedidoVO> buscarPorProducto(Long idProducto);

    List<DetallePedidoVO> buscarPorPedidoYProducto(Long idPedido, Long idProducto);
}

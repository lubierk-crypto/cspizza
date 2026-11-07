package com.uniremington.cspizza.model.dao;

import com.uniremington.cspizza.model.vo.PedidoVO;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoDAO extends GenericDAO<PedidoVO, Long> {
    List<PedidoVO> buscarPorCliente(Long idCliente);

    List<PedidoVO> buscarPorUsuario(Long idUsuario);

    List<PedidoVO> buscarPorEstado(String estado);

    List<PedidoVO> buscarPorFechaPedidoEntre(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}

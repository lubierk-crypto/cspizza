package com.uniremington.cspizza.model.dao;

import com.uniremington.cspizza.model.vo.FacturaVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FacturaDAO extends GenericDAO<FacturaVO, Long> {
    Optional<FacturaVO> buscarPorNumeroFactura(String numeroFactura);

    Optional<FacturaVO> buscarPorPedido(Long idPedido);

    List<FacturaVO> buscarPorEstado(String estado);

    List<FacturaVO> buscarPorFechaEmisionEntre(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}

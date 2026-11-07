package com.uniremington.cspizza.model.dao;

import com.uniremington.cspizza.model.vo.InventarioVO;

import java.util.List;

public interface InventarioDAO extends GenericDAO<InventarioVO, Long> {
    List<InventarioVO> buscarPorProducto(Long idProducto);

    List<InventarioVO> buscarPorEstado(String estado);

    List<InventarioVO> buscarPorUbicacion(String ubicacion);

    List<InventarioVO> buscarBajoStock();
}

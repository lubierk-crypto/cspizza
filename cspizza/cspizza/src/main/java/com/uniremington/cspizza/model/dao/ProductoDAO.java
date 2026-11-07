package com.uniremington.cspizza.model.dao;

import com.uniremington.cspizza.model.vo.ProductoVO;

import java.util.List;

public interface ProductoDAO extends GenericDAO<ProductoVO, Long> {
    List<ProductoVO> buscarPorCategoria(Long idCategoria);

    List<ProductoVO> buscarPorEstado(String estado);

    List<ProductoVO> buscarPorNombreContiene(String nombre);

    List<ProductoVO> buscarProductosDisponibles();
}

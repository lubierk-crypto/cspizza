package com.uniremington.cspizza.model.dao;

import com.uniremington.cspizza.model.vo.CategoriaVO;

import java.util.List;
import java.util.Optional;

public interface CategoriaDAO extends GenericDAO<CategoriaVO, Long> {
    Optional<CategoriaVO> buscarPorNombre(String nombre);

    List<CategoriaVO> buscarPorEstado(String estado);
}

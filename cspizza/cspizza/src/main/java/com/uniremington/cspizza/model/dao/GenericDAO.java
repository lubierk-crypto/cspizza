package com.uniremington.cspizza.model.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDAO<T, ID> {
    T guardar(T entidad);

    T actualizar(T entidad);

    void eliminar(ID id);

    Optional<T> buscarPorId(ID id);

    List<T> listarTodos();
}

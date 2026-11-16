package com.uniremington.cspizza.service;

import java.util.List;
import java.util.Optional;

public interface CrudService<T> {
    T guardar(T entity);
    T actualizar(T entity);
    void eliminar(Long id);
    Optional<T> buscarPorId(Long id);
    List<T> listarTodos();
}

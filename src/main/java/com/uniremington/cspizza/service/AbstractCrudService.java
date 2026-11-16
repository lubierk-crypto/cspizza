package com.uniremington.cspizza.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import com.uniremington.cspizza.service.exception.NotFoundException;
import java.util.List;
import java.util.Optional;

@Transactional
public abstract class AbstractCrudService<V, E> implements CrudService<V> {
    protected final JpaRepository<E, Long> repository;
    protected AbstractCrudService(JpaRepository<E, Long> repository) { this.repository = repository; }
    protected abstract Long idOf(V value);
    protected abstract E map(V value, E current);
    protected abstract V view(E entity);
    @Override public V guardar(V value) { return view(repository.save(map(value, null))); }
    @Override public V actualizar(V value) {
        Long id = idOf(value);
        if (id == null) throw new IllegalArgumentException("El identificador es obligatorio para actualizar.");
        E current = repository.findById(id).orElseThrow(() -> new NotFoundException("No existe el registro " + id + "."));
        return view(repository.save(map(value, current)));
    }
    @Override public void eliminar(Long id) {
        E current = repository.findById(id).orElseThrow(() -> new NotFoundException("No existe el registro " + id + "."));
        repository.delete(current);
    }
    @Override @Transactional(readOnly = true) public Optional<V> buscarPorId(Long id) { return repository.findById(id).map(this::view); }
    @Override @Transactional(readOnly = true) public List<V> listarTodos() { return repository.findAll().stream().map(this::view).toList(); }
}

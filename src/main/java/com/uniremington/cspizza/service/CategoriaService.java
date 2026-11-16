package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.vo.CategoriaVO;
import com.uniremington.cspizza.model.entity.CategoriaEntity;
import com.uniremington.cspizza.model.repository.CategoriaRepository;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoriaService extends AbstractCrudService<CategoriaVO, CategoriaEntity> {
    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository repository) {
        super(repository);
        this.categoriaRepository = repository;
    }

    @Override protected Long idOf(CategoriaVO value) { return value.getIdCategoria(); }
    @Override protected CategoriaEntity map(CategoriaVO value, CategoriaEntity entity) {
        if (value == null) throw new IllegalArgumentException("El cuerpo de la solicitud es obligatorio.");
        if (entity == null) entity = new CategoriaEntity();
        if (value.getNombre() == null || value.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        entity.setNombre(value.getNombre().trim());
        entity.setDescripcion(value.getDescripcion());
        entity.setEstado(value.getEstado() == null || value.getEstado().isBlank() ? "ACTIVO" : value.getEstado());
        return entity;
    }
    @Override protected CategoriaVO view(CategoriaEntity entity) {
        CategoriaVO value = new CategoriaVO();
        value.setIdCategoria(entity.getIdCategoria());
        value.setNombre(entity.getNombre());
        value.setDescripcion(entity.getDescripcion());
        value.setEstado(entity.getEstado());
        return value;
    }
    @Transactional(readOnly=true) public Optional<CategoriaVO> buscarPorNombre(String nombre) { return categoriaRepository.findFirstByNombre(nombre).map(this::view); }
    @Transactional(readOnly=true) public List<CategoriaVO> buscarPorEstado(String estado) { return categoriaRepository.findAllByEstado(estado).stream().map(this::view).toList(); }
    protected List<CategoriaVO> filtered(Predicate<CategoriaEntity> predicate) { return categoriaRepository.findAll().stream().filter(predicate).map(this::view).toList(); }
}

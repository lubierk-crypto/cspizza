package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.entity.InventarioEntity;
import com.uniremington.cspizza.model.entity.ProductoEntity;
import com.uniremington.cspizza.model.repository.InventarioRepository;
import com.uniremington.cspizza.model.repository.ProductoRepository;
import com.uniremington.cspizza.model.vo.InventarioVO;
import com.uniremington.cspizza.service.exception.ConflictException;
import com.uniremington.cspizza.service.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class InventarioService extends AbstractCrudService<InventarioVO, InventarioEntity> {
    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;

    public InventarioService(InventarioRepository inventarioRepository, ProductoRepository productoRepository) {
        super(inventarioRepository);
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    protected Long idOf(InventarioVO value) {
        return value.getIdInventario();
    }

    @Override
    protected InventarioEntity map(InventarioVO value, InventarioEntity entity) {
        if (value == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es obligatorio.");
        }
        if (value.getIdProducto() == null) {
            throw new IllegalArgumentException("El producto es obligatorio.");
        }
        if (value.getCantidadActual() != null && value.getCantidadActual() < 0) {
            throw new IllegalArgumentException("La cantidad actual no puede ser negativa.");
        }
        if (value.getCantidadMinima() == null || value.getCantidadMinima() < 0) {
            throw new IllegalArgumentException("La cantidad minima no puede ser negativa.");
        }
        if (value.getCantidadMaxima() == null || value.getCantidadMaxima() < value.getCantidadMinima()) {
            throw new IllegalArgumentException("La cantidad maxima debe ser igual o mayor que la minima.");
        }

        if (entity == null) {
            entity = new InventarioEntity();
        }
        ProductoEntity product = productoRepository.findById(value.getIdProducto())
                .orElseThrow(() -> new NotFoundException("No existe el producto solicitado."));
        Long currentId = entity.getIdInventario();
        boolean anotherInventoryExists = inventarioRepository
                .findAllByProducto_IdProducto(value.getIdProducto()).stream()
                .anyMatch(existing -> !Objects.equals(existing.getIdInventario(), currentId));
        if (anotherInventoryExists) {
            throw new ConflictException("El producto ya tiene un registro de inventario.");
        }

        entity.setProducto(product);
        entity.setCantidadActual(product.getStock() == null ? 0 : product.getStock());
        entity.setCantidadMinima(value.getCantidadMinima());
        entity.setCantidadMaxima(value.getCantidadMaxima());
        entity.setUbicacion(value.getUbicacion());
        entity.setEstado(value.getEstado() == null || value.getEstado().isBlank()
                ? "ACTIVO" : value.getEstado());
        entity.setFechaActualizacion(LocalDateTime.now());
        return entity;
    }

    @Override
    protected InventarioVO view(InventarioEntity entity) {
        InventarioVO value = new InventarioVO();
        value.setIdInventario(entity.getIdInventario());
        value.setIdProducto(entity.getProducto().getIdProducto());
        value.setCantidadActual(entity.getCantidadActual());
        value.setCantidadMinima(entity.getCantidadMinima());
        value.setCantidadMaxima(entity.getCantidadMaxima());
        value.setUbicacion(entity.getUbicacion());
        value.setEstado(entity.getEstado());
        value.setFechaActualizacion(entity.getFechaActualizacion());
        return value;
    }

    @Override
    public void eliminar(Long id) {
        InventarioEntity inventory = inventarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el inventario solicitado."));
        inventarioRepository.delete(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventarioVO> buscarPorProducto(Long id) {
        return inventarioRepository.findAllByProducto_IdProducto(id).stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<InventarioVO> buscarPorEstado(String estado) {
        return inventarioRepository.findAllByEstado(estado).stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<InventarioVO> buscarPorUbicacion(String ubicacion) {
        return inventarioRepository.findAllByUbicacion(ubicacion).stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<InventarioVO> buscarBajoStock() {
        return inventarioRepository.findAllBajoStock().stream().map(this::view).toList();
    }
}

package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.entity.InventarioEntity;
import com.uniremington.cspizza.model.entity.ProductoEntity;
import com.uniremington.cspizza.model.repository.CategoriaRepository;
import com.uniremington.cspizza.model.repository.InventarioRepository;
import com.uniremington.cspizza.model.repository.ProductoRepository;
import com.uniremington.cspizza.model.vo.ProductoVO;
import com.uniremington.cspizza.service.exception.ConflictException;
import com.uniremington.cspizza.service.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ProductoService extends AbstractCrudService<ProductoVO, ProductoEntity> {
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final InventarioRepository inventarioRepository;

    public ProductoService(ProductoRepository productoRepository,
                          CategoriaRepository categoriaRepository,
                          InventarioRepository inventarioRepository) {
        super(productoRepository);
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.inventarioRepository = inventarioRepository;
    }

    @Override
    protected Long idOf(ProductoVO value) {
        return value.getIdProducto();
    }

    @Override
    protected ProductoEntity map(ProductoVO value, ProductoEntity entity) {
        if (value == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es obligatorio.");
        }
        if (value.getIdCategoria() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }
        if (value.getNombre() == null || value.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (value.getPrecioUnitario() == null || value.getPrecioUnitario().signum() < 0) {
            throw new IllegalArgumentException("El precio debe ser mayor o igual a cero.");
        }
        if (value.getStock() != null && value.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }

        if (entity == null) {
            entity = new ProductoEntity();
        }
        if (categoriaRepository != null) {
            entity.setCategoria(categoriaRepository.findById(value.getIdCategoria())
                    .orElseThrow(() -> new NotFoundException("No existe la categoría solicitada.")));
        } else if (value.getIdCategoria() != null) {
            com.uniremington.cspizza.model.entity.CategoriaEntity categoria = new com.uniremington.cspizza.model.entity.CategoriaEntity();
            categoria.setIdCategoria(value.getIdCategoria());
            entity.setCategoria(categoria);
        }
        entity.setNombre(value.getNombre().trim());
        entity.setDescripcion(value.getDescripcion());
        entity.setPrecioUnitario(value.getPrecioUnitario());
        entity.setStock(value.getStock() == null ? 0 : value.getStock());
        entity.setEstado(value.getEstado() == null || value.getEstado().isBlank()
                ? "ACTIVO" : value.getEstado());
        entity.setImagenUrl(value.getImagenUrl());
        if (entity.getFechaRegistro() == null) {
            entity.setFechaRegistro(value.getFechaRegistro() == null
                    ? LocalDateTime.now() : value.getFechaRegistro());
        }
        return entity;
    }

    @Override
    protected ProductoVO view(ProductoEntity entity) {
        ProductoVO value = new ProductoVO();
        value.setIdProducto(entity.getIdProducto());
        value.setIdCategoria(entity.getCategoria() == null ? null : entity.getCategoria().getIdCategoria());
        value.setNombre(entity.getNombre());
        value.setDescripcion(entity.getDescripcion());
        value.setPrecioUnitario(entity.getPrecioUnitario());
        value.setStock(entity.getStock());
        value.setEstado(entity.getEstado());
        value.setImagenUrl(entity.getImagenUrl());
        value.setFechaRegistro(entity.getFechaRegistro());
        return value;
    }

    @Override
    public ProductoVO guardar(ProductoVO value) {
        ProductoVO saved = super.guardar(value);
        sincronizarInventario(saved, true);
        return saved;
    }

    @Override
    public ProductoVO actualizar(ProductoVO value) {
        ProductoVO saved = super.actualizar(value);
        sincronizarInventario(saved, false);
        return saved;
    }

    private void sincronizarInventario(ProductoVO product, boolean crearSiNoExiste) {
        if (inventarioRepository == null) {
            return;
        }
        List<InventarioEntity> records =
                inventarioRepository.findAllByProducto_IdProducto(product.getIdProducto());
        if (records.size() > 1) {
            throw new ConflictException(
                    "El producto tiene inventarios duplicados; reconcilie los datos manualmente.");
        }

        if (records.isEmpty() && !crearSiNoExiste) {
            return;
        }
        InventarioEntity inventory = records.isEmpty() ? new InventarioEntity() : records.get(0);
        inventory.setProducto(productoRepository.getReferenceById(product.getIdProducto()));
        inventory.setCantidadActual(product.getStock());
        if (inventory.getCantidadMinima() == null) {
            inventory.setCantidadMinima(0);
        }
        if (inventory.getCantidadMaxima() == null) {
            inventory.setCantidadMaxima(0);
        }
        if (inventory.getEstado() == null) {
            inventory.setEstado("ACTIVO");
        }
        inventory.setFechaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventory);
    }

    @Override
    public void eliminar(Long id) {
        ProductoEntity product = productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el producto solicitado."));
        List<InventarioEntity> records = inventarioRepository.findAllByProducto_IdProducto(id);
        if (records.size() > 1) {
            throw new ConflictException(
                    "El producto tiene inventarios duplicados; reconcilie los datos antes de eliminarlo.");
        }
        records.forEach(inventarioRepository::delete);
        productoRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public List<ProductoVO> buscarPorCategoria(Long id) {
        return productoRepository.findAllByCategoria_IdCategoria(id).stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoVO> buscarPorEstado(String estado) {
        return productoRepository.findAllByEstado(estado).stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoVO> buscarPorNombreContiene(String nombre) {
        String query = nombre == null ? "" : nombre;
        return productoRepository.findAllByNombreContainingIgnoreCase(query).stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoVO> buscarProductosDisponibles() {
        return productoRepository.findAllByEstadoAndStockGreaterThan("ACTIVO", 0).stream()
                .map(this::view).toList();
    }
}

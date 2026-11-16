package com.uniremington.cspizza.model.repository;

import com.uniremington.cspizza.model.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {
    List<ProductoEntity> findAllByCategoria_IdCategoria(Long idCategoria);
    List<ProductoEntity> findAllByEstado(String estado);
    List<ProductoEntity> findAllByNombreContainingIgnoreCase(String nombre);
    List<ProductoEntity> findAllByEstadoAndStockGreaterThan(String estado, Integer stock);

    @Query("SELECT p FROM ProductoEntity p ORDER BY p.nombre ASC")
    List<ProductoEntity> findAllOrderByNombreAsc();

    @Query("SELECT p FROM ProductoEntity p WHERE p.estado = 'ACTIVO' AND p.stock > 0 ORDER BY p.nombre ASC")
    List<ProductoEntity> findByEstadoActivoAndStockGreaterThanOrderByNombreAsc();

    @Query(value = """
        SELECT p FROM ProductoEntity p
        WHERE p.idProducto IN (
            SELECT d.producto.idProducto FROM DetallePedidoEntity d
        )
        ORDER BY p.nombre ASC
    """)
    List<ProductoEntity> findProductosConVentas();

    @Query("SELECT d.producto.idProducto, d.producto.nombre, SUM(d.cantidad) FROM DetallePedidoEntity d GROUP BY d.producto.idProducto, d.producto.nombre ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> topProductosVendidos();
}

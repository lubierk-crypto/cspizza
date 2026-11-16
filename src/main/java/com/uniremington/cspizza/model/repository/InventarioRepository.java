package com.uniremington.cspizza.model.repository;

import com.uniremington.cspizza.model.entity.InventarioEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InventarioRepository extends JpaRepository<InventarioEntity, Long> {
    List<InventarioEntity> findAllByProducto_IdProducto(Long idProducto);

    List<InventarioEntity> findAllByEstado(String estado);

    List<InventarioEntity> findAllByUbicacion(String ubicacion);

    @Query("select inventory from InventarioEntity inventory "
            + "where inventory.cantidadActual < inventory.cantidadMinima")
    List<InventarioEntity> findAllBajoStock();
}

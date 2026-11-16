package com.uniremington.cspizza.model.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** Existencias, limites y ubicacion de inventario de un producto. */
public class InventarioVO {
    private Long idInventario;

    @NotNull(message = "El producto es obligatorio.")
    private Long idProducto;

    @PositiveOrZero(message = "La cantidad no puede ser negativa.")
    private Integer cantidadActual;

    @NotNull(message = "La cantidad mínima es obligatoria.")
    @PositiveOrZero(message = "La cantidad mínima no puede ser negativa.")
    private Integer cantidadMinima;

    @NotNull(message = "La cantidad máxima es obligatoria.")
    @PositiveOrZero(message = "La cantidad máxima no puede ser negativa.")
    private Integer cantidadMaxima;

    @Size(max = 150)
    private String ubicacion;
    private String estado;
    private LocalDateTime fechaActualizacion;

    public Long getIdInventario() {
        return idInventario;
    }

    public void setIdInventario(Long idInventario) {
        this.idInventario = idInventario;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getCantidadActual() {
        return cantidadActual;
    }

    public void setCantidadActual(Integer cantidadActual) {
        this.cantidadActual = cantidadActual;
    }

    public Integer getCantidadMinima() {
        return cantidadMinima;
    }

    public void setCantidadMinima(Integer cantidadMinima) {
        this.cantidadMinima = cantidadMinima;
    }

    public Integer getCantidadMaxima() {
        return cantidadMaxima;
    }

    public void setCantidadMaxima(Integer cantidadMaxima) {
        this.cantidadMaxima = cantidadMaxima;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}

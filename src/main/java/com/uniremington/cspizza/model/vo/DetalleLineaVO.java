package com.uniremington.cspizza.model.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Línea de producto dentro de un pedido (usada solo en el formulario de pedido). */
public class DetalleLineaVO {
    private Long idProducto;

    @NotNull(message = "El producto es obligatorio.")
    @Positive(message = "La cantidad debe ser mayor que cero.")
    private Integer cantidad;

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}

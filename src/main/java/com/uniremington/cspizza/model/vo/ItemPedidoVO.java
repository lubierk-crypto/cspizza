package com.uniremington.cspizza.model.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Línea de producto dentro de un pedido nuevo (usada solo en el formulario POS). */
public class ItemPedidoVO {

    @NotNull(message = "El producto es obligatorio.")
    private Long idProducto;

    @NotNull(message = "La cantidad es obligatoria.")
    @Positive(message = "La cantidad debe ser mayor que cero.")
    private Integer cantidad;

    @jakarta.validation.constraints.Size(max = 255)
    private String observacion;

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

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}

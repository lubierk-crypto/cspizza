package com.uniremington.cspizza.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/** Datos de entrada para registrar un pedido completo con varios productos (punto de venta). */
public class PedidoNuevoVO {

    /** Cliente del pedido. Puede ser nulo si se usa "Cliente Mostrador". */
    private Long idCliente;

/** Nombre del cliente escrito manualmente (crea el cliente si no existe). */
private String clienteNombre;

    @NotBlank(message = "El tipo de entrega es obligatorio.")
    @Size(max = 30)
    private String tipoEntrega;

    @Size(max = 255)
    private String direccionEntrega;

    @Size(max = 255)
    private String observacion;

    /** Líneas del pedido: al menos una es obligatoria. */
    private List<ItemPedidoVO> items = new ArrayList<>();

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getTipoEntrega() {
        return tipoEntrega;
    }

    public void setTipoEntrega(String tipoEntrega) {
        this.tipoEntrega = tipoEntrega;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public List<ItemPedidoVO> getItems() {
        return items;
    }

    public void setItems(List<ItemPedidoVO> items) {
        this.items = items;
    }
}

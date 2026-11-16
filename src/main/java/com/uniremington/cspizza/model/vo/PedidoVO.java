package com.uniremington.cspizza.model.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Datos de cabecera, entrega y estado de un pedido. */
public class PedidoVO {
    private Long idPedido;

    @NotNull(message = "El cliente es obligatorio.")
    private Long idCliente;

    @NotNull(message = "El usuario es obligatorio.")
    private Long idUsuario;
    private LocalDateTime fechaPedido;
    private String estado;
    private BigDecimal totalPedido;

    @Size(max = 255)
    private String observacion;

    @Size(max = 30)
    private String tipoEntrega;

    @Size(max = 255)
    private String direccionEntrega;

    /** Líneas de pedido: productos con cantidad que se agregarán al detalle. */
    private List<DetalleLineaVO> detalleLineas = new ArrayList<>();

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getTotalPedido() {
        return totalPedido;
    }

    public void setTotalPedido(BigDecimal totalPedido) {
        this.totalPedido = totalPedido;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
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

    /** Nombre del cliente (solo para mostrar en listados). */
    private String clienteNombre;

    /** Nombre del usuario (solo para mostrar en listados). */
    private String usuarioNombre;

    /** Resumen de productos para mostrar en la lista (ej. "1× Pepperoni, 2× Gaseosa"). */
    private String resumenProductos;

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getResumenProductos() {
        return resumenProductos;
    }

    public void setResumenProductos(String resumenProductos) {
        this.resumenProductos = resumenProductos;
    }

    public List<DetalleLineaVO> getDetalleLineas() {
        return detalleLineas;
    }

    public void setDetalleLineas(List<DetalleLineaVO> detalleLineas) {
        this.detalleLineas = detalleLineas;
    }
}

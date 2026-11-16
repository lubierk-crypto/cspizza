package com.uniremington.cspizza.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos de una categoria de productos. */
public class CategoriaVO {
    private Long idCategoria;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres.")
    private String descripcion;

    @Size(max = 30, message = "El estado no puede superar 30 caracteres.")
    private String estado;

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

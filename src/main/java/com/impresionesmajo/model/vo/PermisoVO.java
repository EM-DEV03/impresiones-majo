package com.impresionesmajo.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo de la tabla permiso. No expone datos personales ni secretos en logs. */
public class PermisoVO {
    private Long id;
    private String codigo;
    private String descripcion;
    public PermisoVO() {}
    public PermisoVO(Long id, String codigo, String descripcion) {
        this.id = id;
        this.codigo = codigo;
        this.descripcion = descripcion;
    }
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String value) { this.codigo = value; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String value) { this.descripcion = value; }
    @Override public String toString() { return "PermisoVO[" + "id=" + id + "]"; }
}
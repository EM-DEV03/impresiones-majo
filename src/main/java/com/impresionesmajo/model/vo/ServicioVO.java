package com.impresionesmajo.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo de la tabla servicio. No expone datos personales ni secretos en logs. */
public class ServicioVO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String tipo;
    private String unidadCobro;
    private BigDecimal precioUnitario;
    private BigDecimal costoUnitarioEstimado;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    public ServicioVO() {}
    public ServicioVO(Long id, String codigo, String nombre, String descripcion, String tipo, String unidadCobro, BigDecimal precioUnitario, BigDecimal costoUnitarioEstimado, Boolean activo, LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.unidadCobro = unidadCobro;
        this.precioUnitario = precioUnitario;
        this.costoUnitarioEstimado = costoUnitarioEstimado;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String value) { this.codigo = value; }
    public String getNombre() { return nombre; }
    public void setNombre(String value) { this.nombre = value; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String value) { this.descripcion = value; }
    public String getTipo() { return tipo; }
    public void setTipo(String value) { this.tipo = value; }
    public String getUnidadCobro() { return unidadCobro; }
    public void setUnidadCobro(String value) { this.unidadCobro = value; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal value) { this.precioUnitario = value; }
    public BigDecimal getCostoUnitarioEstimado() { return costoUnitarioEstimado; }
    public void setCostoUnitarioEstimado(BigDecimal value) { this.costoUnitarioEstimado = value; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean value) { this.activo = value; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime value) { this.fechaCreacion = value; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime value) { this.fechaActualizacion = value; }
    @Override public String toString() { return "ServicioVO[" + "id=" + id + "]"; }
}
package com.impresionesmajo.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo de la tabla inversion_equipo. No expone datos personales ni secretos en logs. */
public class InversionEquipoVO {
    private Long id;
    private Long usuarioId;
    private Long proveedorId;
    private String equipo;
    private String descripcion;
    private LocalDate fechaAdquisicion;
    private BigDecimal costo;
    private String metodoPago;
    private Integer vidaUtilMeses;
    private String estado;
    private String motivoAnulacion;
    private LocalDateTime fechaAnulacion;
    public InversionEquipoVO() {}
    public InversionEquipoVO(Long id, Long usuarioId, Long proveedorId, String equipo, String descripcion, LocalDate fechaAdquisicion, BigDecimal costo, String metodoPago, Integer vidaUtilMeses, String estado, String motivoAnulacion, LocalDateTime fechaAnulacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.proveedorId = proveedorId;
        this.equipo = equipo;
        this.descripcion = descripcion;
        this.fechaAdquisicion = fechaAdquisicion;
        this.costo = costo;
        this.metodoPago = metodoPago;
        this.vidaUtilMeses = vidaUtilMeses;
        this.estado = estado;
        this.motivoAnulacion = motivoAnulacion;
        this.fechaAnulacion = fechaAnulacion;
    }
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long value) { this.usuarioId = value; }
    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long value) { this.proveedorId = value; }
    public String getEquipo() { return equipo; }
    public void setEquipo(String value) { this.equipo = value; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String value) { this.descripcion = value; }
    public LocalDate getFechaAdquisicion() { return fechaAdquisicion; }
    public void setFechaAdquisicion(LocalDate value) { this.fechaAdquisicion = value; }
    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal value) { this.costo = value; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String value) { this.metodoPago = value; }
    public Integer getVidaUtilMeses() { return vidaUtilMeses; }
    public void setVidaUtilMeses(Integer value) { this.vidaUtilMeses = value; }
    public String getEstado() { return estado; }
    public void setEstado(String value) { this.estado = value; }
    public String getMotivoAnulacion() { return motivoAnulacion; }
    public void setMotivoAnulacion(String value) { this.motivoAnulacion = value; }
    public LocalDateTime getFechaAnulacion() { return fechaAnulacion; }
    public void setFechaAnulacion(LocalDateTime value) { this.fechaAnulacion = value; }
    @Override public String toString() { return "InversionEquipoVO[" + "id=" + id + "]"; }
}
package com.impresionesmajo.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo de la tabla aporte_capital. No expone datos personales ni secretos en logs. */
public class AporteCapitalVO {
    private Long id;
    private Long usuarioId;
    private String origen;
    private BigDecimal valor;
    private String metodo;
    private LocalDateTime fecha;
    private String observaciones;
    private String estado;
    private String motivoAnulacion;
    private LocalDateTime fechaAnulacion;
    public AporteCapitalVO() {}
    public AporteCapitalVO(Long id, Long usuarioId, String origen, BigDecimal valor, String metodo, LocalDateTime fecha, String observaciones, String estado, String motivoAnulacion, LocalDateTime fechaAnulacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.origen = origen;
        this.valor = valor;
        this.metodo = metodo;
        this.fecha = fecha;
        this.observaciones = observaciones;
        this.estado = estado;
        this.motivoAnulacion = motivoAnulacion;
        this.fechaAnulacion = fechaAnulacion;
    }
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long value) { this.usuarioId = value; }
    public String getOrigen() { return origen; }
    public void setOrigen(String value) { this.origen = value; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal value) { this.valor = value; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String value) { this.metodo = value; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime value) { this.fecha = value; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String value) { this.observaciones = value; }
    public String getEstado() { return estado; }
    public void setEstado(String value) { this.estado = value; }
    public String getMotivoAnulacion() { return motivoAnulacion; }
    public void setMotivoAnulacion(String value) { this.motivoAnulacion = value; }
    public LocalDateTime getFechaAnulacion() { return fechaAnulacion; }
    public void setFechaAnulacion(LocalDateTime value) { this.fechaAnulacion = value; }
    @Override public String toString() { return "AporteCapitalVO[" + "id=" + id + "]"; }
}
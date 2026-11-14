package com.impresionesmajo.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo de la tabla gasto. No expone datos personales ni secretos en logs. */
public class GastoVO {
    private Long id;
    private Long usuarioId;
    private String categoria;
    private String concepto;
    private String descripcion;
    private BigDecimal valor;
    private String metodoPago;
    private LocalDateTime fecha;
    private String estado;
    private String motivoAnulacion;
    private LocalDateTime fechaAnulacion;
    public GastoVO() {}
    public GastoVO(Long id, Long usuarioId, String categoria, String concepto, String descripcion, BigDecimal valor, String metodoPago, LocalDateTime fecha, String estado, String motivoAnulacion, LocalDateTime fechaAnulacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.categoria = categoria;
        this.concepto = concepto;
        this.descripcion = descripcion;
        this.valor = valor;
        this.metodoPago = metodoPago;
        this.fecha = fecha;
        this.estado = estado;
        this.motivoAnulacion = motivoAnulacion;
        this.fechaAnulacion = fechaAnulacion;
    }
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long value) { this.usuarioId = value; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String value) { this.categoria = value; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String value) { this.concepto = value; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String value) { this.descripcion = value; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal value) { this.valor = value; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String value) { this.metodoPago = value; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime value) { this.fecha = value; }
    public String getEstado() { return estado; }
    public void setEstado(String value) { this.estado = value; }
    public String getMotivoAnulacion() { return motivoAnulacion; }
    public void setMotivoAnulacion(String value) { this.motivoAnulacion = value; }
    public LocalDateTime getFechaAnulacion() { return fechaAnulacion; }
    public void setFechaAnulacion(LocalDateTime value) { this.fechaAnulacion = value; }
    @Override public String toString() { return "GastoVO[" + "id=" + id + "]"; }
}
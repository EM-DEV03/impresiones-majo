package com.impresionesmajo.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo de la tabla venta. No expone datos personales ni secretos en logs. */
public class VentaVO {
    private Long id;
    private Long usuarioId;
    private LocalDateTime fecha;
    private String estado;
    private String metodoPago;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal total;
    private BigDecimal valorRecibido;
    private BigDecimal cambio;
    private String observaciones;
    private LocalDateTime fechaConfirmacion;
    private LocalDateTime fechaAnulacion;
    private String motivoAnulacion;
    public VentaVO() {}
    public VentaVO(Long id, Long usuarioId, LocalDateTime fecha, String estado, String metodoPago, BigDecimal subtotal, BigDecimal descuento, BigDecimal total, BigDecimal valorRecibido, BigDecimal cambio, String observaciones, LocalDateTime fechaConfirmacion, LocalDateTime fechaAnulacion, String motivoAnulacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.estado = estado;
        this.metodoPago = metodoPago;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.total = total;
        this.valorRecibido = valorRecibido;
        this.cambio = cambio;
        this.observaciones = observaciones;
        this.fechaConfirmacion = fechaConfirmacion;
        this.fechaAnulacion = fechaAnulacion;
        this.motivoAnulacion = motivoAnulacion;
    }
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long value) { this.usuarioId = value; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime value) { this.fecha = value; }
    public String getEstado() { return estado; }
    public void setEstado(String value) { this.estado = value; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String value) { this.metodoPago = value; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal value) { this.subtotal = value; }
    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal value) { this.descuento = value; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal value) { this.total = value; }
    public BigDecimal getValorRecibido() { return valorRecibido; }
    public void setValorRecibido(BigDecimal value) { this.valorRecibido = value; }
    public BigDecimal getCambio() { return cambio; }
    public void setCambio(BigDecimal value) { this.cambio = value; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String value) { this.observaciones = value; }
    public LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; }
    public void setFechaConfirmacion(LocalDateTime value) { this.fechaConfirmacion = value; }
    public LocalDateTime getFechaAnulacion() { return fechaAnulacion; }
    public void setFechaAnulacion(LocalDateTime value) { this.fechaAnulacion = value; }
    public String getMotivoAnulacion() { return motivoAnulacion; }
    public void setMotivoAnulacion(String value) { this.motivoAnulacion = value; }
    @Override public String toString() { return "VentaVO[" + "id=" + id + "]"; }
}
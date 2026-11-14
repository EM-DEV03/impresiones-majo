package com.impresionesmajo.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo de la tabla compra. No expone datos personales ni secretos en logs. */
public class CompraVO {
    private Long id;
    private Long proveedorId;
    private Long usuarioId;
    private LocalDateTime fecha;
    private String estado;
    private String metodoPago;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal total;
    private String observaciones;
    private LocalDateTime fechaConfirmacion;
    public CompraVO() {}
    public CompraVO(Long id, Long proveedorId, Long usuarioId, LocalDateTime fecha, String estado, String metodoPago, BigDecimal subtotal, BigDecimal descuento, BigDecimal total, String observaciones, LocalDateTime fechaConfirmacion) {
        this.id = id;
        this.proveedorId = proveedorId;
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.estado = estado;
        this.metodoPago = metodoPago;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.total = total;
        this.observaciones = observaciones;
        this.fechaConfirmacion = fechaConfirmacion;
    }
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long value) { this.proveedorId = value; }
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
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String value) { this.observaciones = value; }
    public LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; }
    public void setFechaConfirmacion(LocalDateTime value) { this.fechaConfirmacion = value; }
    @Override public String toString() { return "CompraVO[" + "id=" + id + "]"; }
}
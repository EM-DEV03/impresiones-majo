package com.impresionesmajo.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo de la tabla detalle_venta. No expone datos personales ni secretos en logs. */
public class DetalleVentaVO {
    private Long id;
    private Long ventaId;
    private Long productoId;
    private Long servicioId;
    private String descripcionSnapshot;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal costoUnitarioSnapshot;
    private BigDecimal subtotal;
    public DetalleVentaVO() {}
    public DetalleVentaVO(Long id, Long ventaId, Long productoId, Long servicioId, String descripcionSnapshot, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal costoUnitarioSnapshot, BigDecimal subtotal) {
        this.id = id;
        this.ventaId = ventaId;
        this.productoId = productoId;
        this.servicioId = servicioId;
        this.descripcionSnapshot = descripcionSnapshot;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.costoUnitarioSnapshot = costoUnitarioSnapshot;
        this.subtotal = subtotal;
    }
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public Long getVentaId() { return ventaId; }
    public void setVentaId(Long value) { this.ventaId = value; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long value) { this.productoId = value; }
    public Long getServicioId() { return servicioId; }
    public void setServicioId(Long value) { this.servicioId = value; }
    public String getDescripcionSnapshot() { return descripcionSnapshot; }
    public void setDescripcionSnapshot(String value) { this.descripcionSnapshot = value; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal value) { this.cantidad = value; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal value) { this.precioUnitario = value; }
    public BigDecimal getCostoUnitarioSnapshot() { return costoUnitarioSnapshot; }
    public void setCostoUnitarioSnapshot(BigDecimal value) { this.costoUnitarioSnapshot = value; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal value) { this.subtotal = value; }
    @Override public String toString() { return "DetalleVentaVO[" + "id=" + id + "]"; }
}
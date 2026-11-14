package com.impresionesmajo.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo de la tabla detalle_compra. No expone datos personales ni secretos en logs. */
public class DetalleCompraVO {
    private Long id;
    private Long compraId;
    private Long productoId;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal subtotal;
    public DetalleCompraVO() {}
    public DetalleCompraVO(Long id, Long compraId, Long productoId, BigDecimal cantidad, BigDecimal costoUnitario, BigDecimal subtotal) {
        this.id = id;
        this.compraId = compraId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.subtotal = subtotal;
    }
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public Long getCompraId() { return compraId; }
    public void setCompraId(Long value) { this.compraId = value; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long value) { this.productoId = value; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal value) { this.cantidad = value; }
    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal value) { this.costoUnitario = value; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal value) { this.subtotal = value; }
    @Override public String toString() { return "DetalleCompraVO[" + "id=" + id + "]"; }
}
package com.impresionesmajo.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo de la tabla movimiento_inventario. No expone datos personales ni secretos en logs. */
public class MovimientoInventarioVO {
    private Long id;
    private Long productoId;
    private Long usuarioId;
    private Long compraId;
    private Long ventaId;
    private String tipo;
    private BigDecimal cantidad;
    private BigDecimal stockAnterior;
    private BigDecimal stockPosterior;
    private String motivo;
    private LocalDateTime fecha;
    public MovimientoInventarioVO() {}
    public MovimientoInventarioVO(Long id, Long productoId, Long usuarioId, Long compraId, Long ventaId, String tipo, BigDecimal cantidad, BigDecimal stockAnterior, BigDecimal stockPosterior, String motivo, LocalDateTime fecha) {
        this.id = id;
        this.productoId = productoId;
        this.usuarioId = usuarioId;
        this.compraId = compraId;
        this.ventaId = ventaId;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.stockAnterior = stockAnterior;
        this.stockPosterior = stockPosterior;
        this.motivo = motivo;
        this.fecha = fecha;
    }
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long value) { this.productoId = value; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long value) { this.usuarioId = value; }
    public Long getCompraId() { return compraId; }
    public void setCompraId(Long value) { this.compraId = value; }
    public Long getVentaId() { return ventaId; }
    public void setVentaId(Long value) { this.ventaId = value; }
    public String getTipo() { return tipo; }
    public void setTipo(String value) { this.tipo = value; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal value) { this.cantidad = value; }
    public BigDecimal getStockAnterior() { return stockAnterior; }
    public void setStockAnterior(BigDecimal value) { this.stockAnterior = value; }
    public BigDecimal getStockPosterior() { return stockPosterior; }
    public void setStockPosterior(BigDecimal value) { this.stockPosterior = value; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String value) { this.motivo = value; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime value) { this.fecha = value; }
    @Override public String toString() { return "MovimientoInventarioVO[" + "id=" + id + "]"; }
}
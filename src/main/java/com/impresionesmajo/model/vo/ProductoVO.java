package com.impresionesmajo.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo de la tabla producto. No expone datos personales ni secretos en logs. */
public class ProductoVO {
    private Long id;
    private Long categoriaId;
    private Long proveedorPreferidoId;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String tipo;
    private String unidadMedida;
    private BigDecimal costoPromedio;
    private BigDecimal precioVenta;
    private BigDecimal stockActual;
    private BigDecimal stockMinimo;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    public ProductoVO() {}
    public ProductoVO(Long id, Long categoriaId, Long proveedorPreferidoId, String codigo, String nombre, String descripcion, String tipo, String unidadMedida, BigDecimal costoPromedio, BigDecimal precioVenta, BigDecimal stockActual, BigDecimal stockMinimo, Boolean activo, LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion) {
        this.id = id;
        this.categoriaId = categoriaId;
        this.proveedorPreferidoId = proveedorPreferidoId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.unidadMedida = unidadMedida;
        this.costoPromedio = costoPromedio;
        this.precioVenta = precioVenta;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long value) { this.categoriaId = value; }
    public Long getProveedorPreferidoId() { return proveedorPreferidoId; }
    public void setProveedorPreferidoId(Long value) { this.proveedorPreferidoId = value; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String value) { this.codigo = value; }
    public String getNombre() { return nombre; }
    public void setNombre(String value) { this.nombre = value; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String value) { this.descripcion = value; }
    public String getTipo() { return tipo; }
    public void setTipo(String value) { this.tipo = value; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String value) { this.unidadMedida = value; }
    public BigDecimal getCostoPromedio() { return costoPromedio; }
    public void setCostoPromedio(BigDecimal value) { this.costoPromedio = value; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal value) { this.precioVenta = value; }
    public BigDecimal getStockActual() { return stockActual; }
    public void setStockActual(BigDecimal value) { this.stockActual = value; }
    public BigDecimal getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(BigDecimal value) { this.stockMinimo = value; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean value) { this.activo = value; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime value) { this.fechaCreacion = value; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime value) { this.fechaActualizacion = value; }
    @Override public String toString() { return "ProductoVO[" + "id=" + id + "]"; }
}
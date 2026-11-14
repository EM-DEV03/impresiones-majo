package com.impresionesmajo.service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Comando de formulario para compras y ventas.
 * Los totales, el estado, el costo histórico y el usuario se calculan en el
 * servicio; nunca se aceptan como autoridad desde el cliente.
 */
public class OperacionForm {
    private Long proveedorId;
    private String metodoPago="EFECTIVO";
    private BigDecimal descuento=BigDecimal.ZERO;
    private BigDecimal valorRecibido;
    private String observaciones;
    private List<Linea> lineas=new ArrayList<>(List.of(new Linea()));
    public Long getProveedorId(){return proveedorId;} public void setProveedorId(Long v){proveedorId=v;}
    public String getMetodoPago(){return metodoPago;} public void setMetodoPago(String v){metodoPago=v;}
    public BigDecimal getDescuento(){return descuento;} public void setDescuento(BigDecimal v){descuento=v;}
    public BigDecimal getValorRecibido(){return valorRecibido;} public void setValorRecibido(BigDecimal v){valorRecibido=v;}
    public String getObservaciones(){return observaciones;} public void setObservaciones(String v){observaciones=v;}
    public List<Linea> getLineas(){return lineas;} public void setLineas(List<Linea> v){lineas=v;}
    public static class Linea {
        private Long productoId,servicioId;
        private BigDecimal cantidad=BigDecimal.ONE,costoUnitario;
        public Long getProductoId(){return productoId;} public void setProductoId(Long v){productoId=v;}
        public Long getServicioId(){return servicioId;} public void setServicioId(Long v){servicioId=v;}
        public BigDecimal getCantidad(){return cantidad;} public void setCantidad(BigDecimal v){cantidad=v;}
        public BigDecimal getCostoUnitario(){return costoUnitario;} public void setCostoUnitario(BigDecimal v){costoUnitario=v;}
    }
    /** Valida la estructura común antes de que CompraService o VentaService calculen totales. */
    public void validar() {
        Reglas.pago(metodoPago); descuento=Reglas.dinero(descuento);
        if(observaciones!=null && observaciones.length()>500) throw new IllegalArgumentException("Observaciones: máximo 500 caracteres.");
        if(lineas==null || lineas.isEmpty() || lineas.size()>100) throw new IllegalArgumentException("Incluye entre 1 y 100 líneas.");
        for(var l:lineas) {
            if(l==null || (l.productoId==null)==(l.servicioId==null)) throw new IllegalArgumentException("Selecciona un producto o un servicio por línea.");
            l.cantidad=Reglas.cantidad(l.cantidad);
        }
    }
}

package com.impresionesmajo.service;

import java.math.*;
import java.time.LocalDateTime;
import java.util.*;
import com.impresionesmajo.model.dao.*;
import com.impresionesmajo.model.vo.*;
import com.impresionesmajo.security.Acceso;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestiona el ciclo BORRADOR -> CONFIRMADA y sus entradas de inventario. */
@Service
public class CompraService {
    private final CompraDAO compras;private final DetalleCompraDAO detalles;private final ProductoDAO productos;
    private final InventarioService inventario;private final JdbcTemplate jdbc;private final Acceso acceso;
    public CompraService(CompraDAO compras,DetalleCompraDAO detalles,ProductoDAO productos,InventarioService inventario,JdbcTemplate jdbc,Acceso acceso) {
        this.compras=compras;this.detalles=detalles;this.productos=productos;this.inventario=inventario;this.jdbc=jdbc;this.acceso=acceso;
    }
    /** Lista las últimas compras para el módulo administrativo. */
    public List<Map<String,Object>> listar() {acceso.exigir("COMPRAS_GESTIONAR");return jdbc.queryForList("SELECT c.*,p.nombre AS proveedor FROM compra c JOIN proveedor p ON p.id=c.proveedor_id ORDER BY c.id DESC LIMIT 500");}
    /** Obtiene una compra y aplica el permiso de gestión. */
    public CompraVO obtener(long id) {acceso.exigir("COMPRAS_GESTIONAR");return compras.findById(id).orElseThrow(()->new IllegalArgumentException("Compra inexistente."));}
    /** Carga el detalle para la vista de una compra. */
    public List<Map<String,Object>> lineas(long id) {obtener(id);return jdbc.queryForList("SELECT d.*,p.nombre AS nombre FROM detalle_compra d JOIN producto p ON p.id=d.producto_id WHERE d.compra_id=? ORDER BY d.producto_id",id);}
    /** Crea el borrador y conserva los precios/costos capturados en sus detalles. */
    @Transactional public long crear(OperacionForm form) {
        acceso.exigir("COMPRAS_GESTIONAR");form.validar();
        if(form.getProveedorId()==null || jdbc.queryForList("SELECT id FROM proveedor WHERE id=? AND activo=1",form.getProveedorId()).isEmpty()) throw new IllegalArgumentException("Selecciona un proveedor activo.");
        var lines=new ArrayList<DetalleCompraVO>();var seen=new HashSet<Long>();BigDecimal subtotal=BigDecimal.ZERO;
        for(var l:form.getLineas()) {
            if(l.getProductoId()==null || !seen.add(l.getProductoId())) throw new IllegalArgumentException("Incluye productos distintos en la compra.");
            var p=productos.findById(l.getProductoId()).orElseThrow(()->new IllegalArgumentException("Producto inexistente."));
            if(!Boolean.TRUE.equals(p.getActivo())) throw new IllegalArgumentException("Producto inactivo.");
            var d=new DetalleCompraVO();d.setProductoId(p.getId());d.setCantidad(l.getCantidad());d.setCostoUnitario(Reglas.dinero(l.getCostoUnitario()));
            d.setSubtotal(Reglas.redondear(d.getCantidad().multiply(d.getCostoUnitario())));subtotal=subtotal.add(d.getSubtotal());lines.add(d);
        }
        if(form.getDescuento().compareTo(subtotal)>0) throw new IllegalArgumentException("El descuento supera el subtotal.");
        var c=new CompraVO();c.setProveedorId(form.getProveedorId());c.setUsuarioId(acceso.usuarioId());c.setEstado("BORRADOR");
        c.setMetodoPago(form.getMetodoPago());c.setSubtotal(Reglas.dinero(subtotal));c.setDescuento(form.getDescuento());c.setTotal(subtotal.subtract(form.getDescuento()));c.setObservaciones(form.getObservaciones());
        long id=compras.insert(c);for(var d:lines){d.setCompraId(id);detalles.insert(d);}return id;
    }
    /** Confirma una compra, bloquea productos y registra ENTRADA_COMPRA por línea. */
    @Transactional public void confirmar(long id) {
        acceso.exigir("COMPRAS_GESTIONAR");var c=compras.lock(id);
        if(!"BORRADOR".equals(c.getEstado())) throw new IllegalArgumentException("La compra ya está confirmada.");
        if(jdbc.queryForList("SELECT id FROM proveedor WHERE id=? AND activo=1",c.getProveedorId()).isEmpty()) throw new IllegalArgumentException("Proveedor inactivo.");
        var lines=jdbc.query("SELECT * FROM detalle_compra WHERE compra_id=? ORDER BY producto_id",new BeanPropertyRowMapper<>(DetalleCompraVO.class),id);
        if(lines.isEmpty()) throw new IllegalArgumentException("La compra no tiene detalle.");
        BigDecimal factor=c.getSubtotal().signum()==0?BigDecimal.ONE:c.getTotal().divide(c.getSubtotal(),12,RoundingMode.HALF_UP);
        long uid=acceso.usuarioId();
        for(var d:lines) {
            var p=productos.lock(d.getProductoId());if(!Boolean.TRUE.equals(p.getActivo())) throw new IllegalArgumentException("Producto inactivo.");
            inventario.mover(p,d.getCantidad(),"ENTRADA_COMPRA",id,null,"Confirmación de compra",d.getCostoUnitario().multiply(factor),uid);
        }
        c.setEstado("CONFIRMADA");c.setFechaConfirmacion(LocalDateTime.now());compras.update(c);
    }
}

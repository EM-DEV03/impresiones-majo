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
import org.springframework.security.access.AccessDeniedException;

/** Gestiona borradores, confirmaciones y anulaciones de ventas. */
@Service
public class VentaService {
    private final VentaDAO ventas;private final DetalleVentaDAO detalles;private final ProductoDAO productos;private final ServicioDAO servicios;
    private final InventarioService inventario;private final JdbcTemplate jdbc;private final Acceso acceso;
    public VentaService(VentaDAO ventas,DetalleVentaDAO detalles,ProductoDAO productos,ServicioDAO servicios,InventarioService inventario,JdbcTemplate jdbc,Acceso acceso) {
        this.ventas=ventas;this.detalles=detalles;this.productos=productos;this.servicios=servicios;this.inventario=inventario;this.jdbc=jdbc;this.acceso=acceso;
    }
    /** Lista ventas propias o todas según el permiso del usuario autenticado. */
    public List<Map<String,Object>> listar() {
        if(!acceso.tiene("VENTAS_VER_TODAS")) acceso.exigir("VENTAS_VER_PROPIAS");
        return jdbc.queryForList("SELECT v.*,u.nombre_usuario AS usuario FROM venta v JOIN usuario u ON u.id=v.usuario_id WHERE (?=1 OR v.usuario_id=?) ORDER BY v.id DESC LIMIT 500",acceso.tiene("VENTAS_VER_TODAS"),acceso.usuarioId());
    }
    /** Obtiene una venta después de verificar si el usuario puede verla. */
    public VentaVO obtener(long id) {var v=ventas.findById(id).orElseThrow(()->new IllegalArgumentException("Venta inexistente."));autorizarLectura(v);return v;}
    private void autorizarLectura(VentaVO v) {
        if(!acceso.tiene("VENTAS_VER_TODAS") && !(acceso.tiene("VENTAS_VER_PROPIAS") && v.getUsuarioId()==acceso.usuarioId())) throw new AccessDeniedException("No puedes consultar esta venta.");
    }
    /** Devuelve las líneas históricas de una venta. */
    public List<DetalleVentaVO> lineas(long id) {obtener(id);return cargarLineas(id);}
    private List<DetalleVentaVO> cargarLineas(long id) {return jdbc.query("SELECT * FROM detalle_venta WHERE venta_id=? ORDER BY producto_id,servicio_id,id",new BeanPropertyRowMapper<>(DetalleVentaVO.class),id);}
    /** Crea un borrador con snapshots de descripción, precio y costo del catálogo. */
    @Transactional public long crear(OperacionForm form) {
        acceso.exigir("VENTAS_CREAR");form.validar();
        form.getLineas().forEach(l -> Reglas.cantidadVenta(l.getCantidad()));
        var v=new VentaVO();v.setUsuarioId(acceso.usuarioId());v.setEstado("BORRADOR");v.setMetodoPago(form.getMetodoPago());v.setObservaciones(form.getObservaciones());
        var lines=new ArrayList<DetalleVentaVO>();BigDecimal subtotal=BigDecimal.ZERO;
        var keys=new HashSet<String>();
        // El mismo orden de bloqueo que la confirmación evita cruces entre ventas simultáneas.
        for(var l:form.getLineas().stream().sorted(Comparator.comparing(OperacionForm.Linea::getProductoId,Comparator.nullsLast(Comparator.naturalOrder()))).toList()) {
            var d=new DetalleVentaVO();d.setCantidad(l.getCantidad());
            String key=(l.getProductoId()!=null?"P"+l.getProductoId():"S"+l.getServicioId());
            if(!keys.add(key)) throw new IllegalArgumentException("Unifica las cantidades de líneas repetidas.");
            if(l.getProductoId()!=null) {
                var p=productos.lock(l.getProductoId());
                if(!Boolean.TRUE.equals(p.getActivo()) || !Set.of("MERCANCIA","SERVICIO").contains(p.getTipo())) throw new IllegalArgumentException("Solo se vende mercancía o servicios activos.");
                d.setProductoId(p.getId());d.setDescripcionSnapshot(p.getNombre());d.setPrecioUnitario(p.getPrecioVenta());d.setCostoUnitarioSnapshot("SERVICIO".equals(p.getTipo())?BigDecimal.ZERO:p.getCostoPromedio());
            } else {
                var s=servicios.findById(l.getServicioId()).orElseThrow(()->new IllegalArgumentException("Servicio inexistente."));
                if(!Boolean.TRUE.equals(s.getActivo())) throw new IllegalArgumentException("Servicio inactivo.");
                d.setServicioId(s.getId());d.setDescripcionSnapshot(s.getNombre());d.setPrecioUnitario(s.getPrecioUnitario());d.setCostoUnitarioSnapshot(s.getCostoUnitarioEstimado());
            }
            d.setSubtotal(Reglas.redondear(d.getCantidad().multiply(d.getPrecioUnitario())));subtotal=subtotal.add(d.getSubtotal());lines.add(d);
        }
        v.setSubtotal(Reglas.dinero(subtotal));v.setDescuento(form.getDescuento());
        if(v.getDescuento().compareTo(subtotal)>0) throw new IllegalArgumentException("El descuento supera el subtotal.");
        v.setTotal(subtotal.subtract(v.getDescuento()));v.setCambio(BigDecimal.ZERO);
        if("EFECTIVO".equals(v.getMetodoPago())) {
            v.setValorRecibido(Reglas.dinero(form.getValorRecibido()));
            if(v.getValorRecibido().compareTo(v.getTotal())<0) throw new IllegalArgumentException("El efectivo recibido no cubre el total.");
            v.setCambio(v.getValorRecibido().subtract(v.getTotal()));
        } else if(form.getValorRecibido()!=null) v.setValorRecibido(Reglas.dinero(form.getValorRecibido()));
        long id=ventas.insert(v);for(var d:lines){d.setVentaId(id);detalles.insert(d);}return id;
    }
    /** Confirma la venta, recalcula costos, bloquea productos y descuenta stock. */
    @Transactional public void confirmar(long id) {
        acceso.exigir("VENTAS_CREAR");var v=ventas.lock(id);autorizarLectura(v);
        if(!"BORRADOR".equals(v.getEstado())) throw new IllegalArgumentException("La venta ya fue procesada.");
        var lines=cargarLineas(id);if(lines.isEmpty()) throw new IllegalArgumentException("La venta no tiene detalle.");
        lines.forEach(d -> Reglas.cantidadVenta(d.getCantidad()));
        long uid=acceso.usuarioId();
        for(var d:lines) {
            if(d.getProductoId()!=null) {
                var p=productos.lock(d.getProductoId());
                if(!Boolean.TRUE.equals(p.getActivo()) || !Set.of("MERCANCIA","SERVICIO").contains(p.getTipo())) throw new IllegalArgumentException("Producto o servicio no disponible para venta.");
                boolean esServicio="SERVICIO".equals(p.getTipo());
                d.setCostoUnitarioSnapshot(esServicio?BigDecimal.ZERO:p.getCostoPromedio());detalles.update(d);
                if(!esServicio) inventario.mover(p,d.getCantidad(),"SALIDA_VENTA",null,id,"Confirmación de venta",null,uid);
            } else {
                var s=servicios.lock(d.getServicioId());if(!Boolean.TRUE.equals(s.getActivo())) throw new IllegalArgumentException("Servicio inactivo.");
                d.setCostoUnitarioSnapshot(s.getCostoUnitarioEstimado());detalles.update(d);
            }
        }
        v.setEstado("CONFIRMADA");v.setFechaConfirmacion(LocalDateTime.now());ventas.update(v);
    }
    /** Anula la venta y revierte exactamente las cantidades que salieron. */
    @Transactional public void anular(long id,String motivo) {
        acceso.exigir("VENTAS_ANULAR");motivo=Reglas.texto(motivo,500,"Motivo");var v=ventas.lock(id);
        if("ANULADA".equals(v.getEstado())) throw new IllegalArgumentException("La venta ya está anulada.");
        long uid=acceso.usuarioId();
        if("CONFIRMADA".equals(v.getEstado())) for(var d:cargarLineas(id)) if(d.getProductoId()!=null) {
            var p=productos.lock(d.getProductoId());
            if(!"SERVICIO".equals(p.getTipo())) inventario.mover(p,d.getCantidad(),"REVERSION_VENTA",null,id,"Anulación de venta #"+id,d.getCostoUnitarioSnapshot(),uid);
        }
        v.setEstado("ANULADA");v.setMotivoAnulacion("Usuario #"+uid+": "+Reglas.texto(motivo,470,"Motivo"));v.setFechaAnulacion(LocalDateTime.now());ventas.update(v);
    }
}

package com.impresionesmajo.service;

import java.math.*;
import java.util.*;
import com.impresionesmajo.model.dao.*;
import com.impresionesmajo.model.vo.*;
import com.impresionesmajo.security.Acceso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Mantiene stock y movimientos como una única operación transaccional. */
@Service
public class InventarioService {
    private final ProductoDAO productos; private final MovimientoInventarioDAO movimientos;
    private final Acceso acceso; private final JdbcTemplate jdbc;
    public InventarioService(ProductoDAO productos,MovimientoInventarioDAO movimientos,Acceso acceso,JdbcTemplate jdbc) {
        this.productos=productos;this.movimientos=movimientos;this.acceso=acceso;this.jdbc=jdbc;
    }
    /** Devuelve el saldo actual de todos los productos visibles para el usuario. */
    public List<Map<String,Object>> existencias() {
        return disponibles().stream().filter(p->!"SERVICIO".equals(p.get("tipo").toString())).toList();
    }
    /** Incluye servicios para actualizar el catálogo y las opciones de venta. */
    public List<Map<String,Object>> disponibles() {
        acceso.exigir("PRODUCTOS_VER");
        return jdbc.queryForList("SELECT id,codigo,nombre,tipo,unidad_medida,stock_actual,stock_minimo,activo FROM producto ORDER BY nombre");
    }
    /** Consulta el historial reciente, opcionalmente filtrado por producto. */
    public List<Map<String,Object>> movimientos(Long productoId) {
        acceso.exigir("MOVIMIENTOS_VER");
        return jdbc.queryForList("SELECT m.*,p.nombre AS producto,u.nombre_usuario AS usuario FROM movimiento_inventario m JOIN producto p ON p.id=m.producto_id JOIN usuario u ON u.id=m.usuario_id WHERE (? IS NULL OR m.producto_id=?) ORDER BY m.id DESC LIMIT 500",productoId,productoId);
    }
    /** Registra una entrada o salida manual después de bloquear el producto. */
    @Transactional public void ajustar(long productoId,BigDecimal cantidad,String tipo,String motivo,BigDecimal costo) {
        acceso.exigir("STOCK_AJUSTAR"); Reglas.opcion(tipo,"AJUSTE_ENTRADA","AJUSTE_SALIDA");
        motivo=Reglas.texto(motivo,255,"Motivo"); cantidad=Reglas.cantidad(cantidad);
        var producto=productos.lock(productoId);
        if("SERVICIO".equals(producto.getTipo())) throw new IllegalArgumentException("Los servicios no admiten ajustes de inventario.");
        if(!Boolean.TRUE.equals(producto.getActivo())) throw new IllegalArgumentException("El producto está inactivo.");
        if(tipo.equals("AJUSTE_ENTRADA")) costo=Reglas.dinero(costo);
        mover(producto,cantidad,tipo,null,null,motivo,costo,acceso.usuarioId());
    }
    /**
     * Aplica el movimiento y actualiza el costo promedio.
     * Solo debe invocarse desde una transacción con el producto bloqueado.
     */
    void mover(ProductoVO producto,BigDecimal cantidad,String tipo,Long compraId,Long ventaId,String motivo,BigDecimal costoEntrada,long usuarioId) {
        if("SERVICIO".equals(producto.getTipo())) throw new IllegalArgumentException("Los servicios no generan movimientos de inventario.");
        // Una anulación debe devolver exactamente lo vendido, incluso en registros antiguos.
        cantidad="REVERSION_VENTA".equals(tipo)?Reglas.decimal(cantidad,3,true,"Cantidad"):Reglas.cantidad(cantidad);
        boolean entrada=Set.of("ENTRADA_COMPRA","REVERSION_VENTA","AJUSTE_ENTRADA").contains(tipo);
        BigDecimal anterior=producto.getStockActual();
        BigDecimal posterior=anterior.add(entrada?cantidad:cantidad.negate());
        if(posterior.signum()<0) throw new IllegalArgumentException("Existencias insuficientes para "+producto.getNombre()+". Disponible: "+anterior);
        Reglas.decimal(posterior,3,false,"Stock");
        if(entrada && costoEntrada!=null) {
            BigDecimal valor=anterior.multiply(producto.getCostoPromedio()).add(cantidad.multiply(costoEntrada));
            producto.setCostoPromedio(Reglas.redondear(valor.divide(posterior,8,RoundingMode.HALF_UP)));
        }
        producto.setStockActual(posterior); productos.update(producto);
        var m=new MovimientoInventarioVO(); m.setProductoId(producto.getId());m.setUsuarioId(usuarioId);
        m.setCompraId(compraId);m.setVentaId(ventaId);m.setTipo(tipo);m.setCantidad(cantidad);
        m.setStockAnterior(anterior);m.setStockPosterior(posterior);m.setMotivo(motivo);
        movimientos.insert(m);
    }
}

package com.impresionesmajo.service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import com.impresionesmajo.security.Acceso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Ejecuta las consultas agregadas del dashboard y los reportes financieros. */
@Service
public class ReporteService {
    private final JdbcTemplate jdbc;private final Acceso acceso;
    public ReporteService(JdbcTemplate jdbc,Acceso acceso){this.jdbc=jdbc;this.acceso=acceso;}
    /** Calcula indicadores financieros para un intervalo inclusivo de fechas. */
    public Map<String,Object> resumen(LocalDate desde,LocalDate hasta) {
        acceso.exigir("REPORTES_VER");validarFechas(desde,hasta);
        var start=desde.atStartOfDay();var end=hasta.plusDays(1).atStartOfDay();
        var m=new LinkedHashMap<String,Object>();
        BigDecimal ventas=sum("SELECT COALESCE(SUM(total),0) FROM venta WHERE estado='CONFIRMADA' AND fecha_confirmacion>=? AND fecha_confirmacion<?",start,end);
        BigDecimal compras=sum("SELECT COALESCE(SUM(total),0) FROM compra WHERE estado='CONFIRMADA' AND fecha_confirmacion>=? AND fecha_confirmacion<?",start,end);
        BigDecimal gastos=sum("SELECT COALESCE(SUM(valor),0) FROM gasto WHERE estado='REGISTRADO' AND fecha>=? AND fecha<?",start,end);
        BigDecimal aportes=sum("SELECT COALESCE(SUM(valor),0) FROM aporte_capital WHERE estado='REGISTRADO' AND fecha>=? AND fecha<?",start,end);
        BigDecimal inversiones=sum("SELECT COALESCE(SUM(costo),0) FROM inversion_equipo WHERE estado='REGISTRADA' AND fecha_adquisicion>=? AND fecha_adquisicion<?",desde,hasta.plusDays(1));
        BigDecimal costo=sum("SELECT COALESCE(SUM(d.cantidad*d.costo_unitario_snapshot),0) FROM detalle_venta d JOIN venta v ON v.id=d.venta_id WHERE v.estado='CONFIRMADA' AND v.fecha_confirmacion>=? AND v.fecha_confirmacion<?",start,end);
        m.put("Ventas netas",ventas);m.put("Compras",compras);m.put("Gastos",gastos);m.put("Aportes de capital",aportes);m.put("Inversiones en equipos",inversiones);m.put("Costo vendido estimado",costo);
        m.put("Margen bruto estimado",ventas.subtract(costo));m.put("Resultado operativo estimado",ventas.subtract(costo).subtract(gastos));
        m.put("Flujo neto de operaciones vigentes",ventas.add(aportes).subtract(compras).subtract(gastos).subtract(inversiones));
        m.put("Valor actual del inventario",sum("SELECT COALESCE(SUM(stock_actual*costo_promedio),0) FROM producto WHERE tipo<>'SERVICIO'"));return m;
    }
    /** Agrupa las ventas confirmadas por día de confirmación. */
    public List<Map<String,Object>> diarios(LocalDate desde,LocalDate hasta) {
        acceso.exigir("REPORTES_VER");validarFechas(desde,hasta);
        return jdbc.queryForList("SELECT CAST(fecha_confirmacion AS DATE) AS dia,COUNT(*) AS operaciones,SUM(total) AS total FROM venta WHERE estado='CONFIRMADA' AND fecha_confirmacion>=? AND fecha_confirmacion<? GROUP BY CAST(fecha_confirmacion AS DATE) ORDER BY dia",desde.atStartOfDay(),hasta.plusDays(1).atStartOfDay());
    }
    /** Obtiene los diez artículos con mayor subtotal en el intervalo. */
    public List<Map<String,Object>> masVendidos(LocalDate desde,LocalDate hasta) {
        acceso.exigir("REPORTES_VER");validarFechas(desde,hasta);
        return jdbc.queryForList("SELECT d.descripcion_snapshot AS nombre,SUM(d.cantidad) AS cantidad,SUM(d.subtotal) AS subtotal FROM detalle_venta d JOIN venta v ON v.id=d.venta_id WHERE v.estado='CONFIRMADA' AND v.fecha_confirmacion>=? AND v.fecha_confirmacion<? GROUP BY d.descripcion_snapshot ORDER BY SUM(d.subtotal) DESC LIMIT 10",desde.atStartOfDay(),hasta.plusDays(1).atStartOfDay());
    }
    /** Construye los indicadores visibles en la pantalla inicial. */
    public Map<String,Object> dashboard() {
        acceso.exigir("DASHBOARD_VER");var m=new HashMap<String,Object>();boolean todas=acceso.tiene("VENTAS_VER_TODAS");long uid=acceso.usuarioId();
        m.put("ventasHoy",sum("SELECT COALESCE(SUM(total),0) FROM venta WHERE estado='CONFIRMADA' AND fecha_confirmacion>=? AND fecha_confirmacion<? AND (?=1 OR usuario_id=?)",LocalDate.now().atStartOfDay(),LocalDate.now().plusDays(1).atStartOfDay(),todas,uid));
        m.put("productos",jdbc.queryForObject("SELECT COUNT(*) FROM producto WHERE activo=1 AND tipo<>'SERVICIO'",Long.class));
        m.put("servicios",jdbc.queryForObject("SELECT (SELECT COUNT(*) FROM servicio WHERE activo=1)+(SELECT COUNT(*) FROM producto WHERE activo=1 AND tipo='SERVICIO')",Long.class));
        m.put("alertas",jdbc.queryForList("SELECT codigo,nombre,stock_actual,stock_minimo FROM producto WHERE activo=1 AND tipo<>'SERVICIO' AND stock_actual<=stock_minimo ORDER BY stock_actual,nombre LIMIT 20"));
        m.put("recientes",jdbc.queryForList("SELECT id,fecha,estado,total FROM venta WHERE (?=1 OR usuario_id=?) ORDER BY id DESC LIMIT 8",todas,uid));return m;
    }
    private BigDecimal sum(String sql,Object... args){return jdbc.queryForObject(sql,BigDecimal.class,args);}
    private void validarFechas(LocalDate a,LocalDate b){if(a==null || b==null || b.isBefore(a) || b.isAfter(a.plusYears(5))) throw new IllegalArgumentException("Selecciona un intervalo válido de máximo cinco años.");}
}

package com.impresionesmajo.service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import com.impresionesmajo.security.Acceso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** Implementación común para gastos, aportes de capital e inversiones. */
public abstract class FinanzasService {
    public record Definicion(String ruta,String tabla,String titulo,String permiso,String concepto,String valor,String fecha,String metodo,String registrado,String anulado) {}
    protected final JdbcTemplate jdbc;private final Acceso acceso;private final Definicion def;
    protected FinanzasService(JdbcTemplate jdbc,Acceso acceso,Definicion def){this.jdbc=jdbc;this.acceso=acceso;this.def=def;}
    /** Expone los metadatos que usa el controller para seleccionar el submódulo. */
    public Definicion definicion(){return def;}
    /** Lista los registros recientes del módulo financiero. */
    public List<Map<String,Object>> listar() {acceso.exigir(def.permiso());return jdbc.queryForList("SELECT * FROM "+def.tabla()+" ORDER BY id DESC LIMIT 500");}
    /** Valida y registra un movimiento financiero según la definición concreta. */
    @Transactional public void registrar(Map<String,String> form) {
        acceso.exigir(def.permiso());
        var cols=new ArrayList<>(List.of("usuario_id",def.concepto(),def.valor(),def.fecha(),def.metodo(),"estado"));
        var args=new ArrayList<Object>();args.add(acceso.usuarioId());args.add(Reglas.texto(form.get(def.concepto()),180,"Concepto"));
        args.add(Reglas.decimal(Reglas.importe(form.get(def.valor())),2,true,"Valor"));
        LocalDate fecha=LocalDate.parse(Reglas.texto(form.get("fecha"),10,"Fecha"));if(fecha.isAfter(LocalDate.now())) throw new IllegalArgumentException("La fecha no puede ser futura.");
        args.add(def.tabla().equals("inversion_equipo")?fecha:fecha.atTime(LocalTime.now()));
        String metodo=Reglas.pago(form.get(def.metodo()));
        if(def.tabla().equals("aporte_capital")) Reglas.opcion(metodo,"EFECTIVO","TRANSFERENCIA","OTRO");
        args.add(metodo);args.add(def.registrado());
        String descripcion=form.get("descripcion");if(descripcion!=null && descripcion.length()>500) throw new IllegalArgumentException("Descripción: máximo 500 caracteres.");
        cols.add(def.tabla().equals("aporte_capital")?"observaciones":"descripcion");args.add(descripcion);
        if(def.tabla().equals("gasto")) {cols.add("categoria");args.add(Reglas.opcion(form.get("categoria"),"ENERGIA","INTERNET","ARRIENDO","TRANSPORTE","MANTENIMIENTO","OTRO"));}
        if(def.tabla().equals("inversion_equipo")) {
            cols.add("proveedor_id");String raw=form.get("proveedor_id");Long proveedor=raw==null || raw.isBlank()?null:Long.valueOf(raw);
            if(proveedor!=null && jdbc.queryForList("SELECT id FROM proveedor WHERE id=? AND activo=1",proveedor).isEmpty()) throw new IllegalArgumentException("Proveedor no válido.");
            args.add(proveedor);cols.add("vida_util_meses");String vida=form.get("vida_util_meses");Integer meses=vida==null || vida.isBlank()?null:Integer.valueOf(vida);
            if(meses!=null && meses<=0) throw new IllegalArgumentException("La vida útil debe ser positiva.");args.add(meses);
        }
        jdbc.update("INSERT INTO "+def.tabla()+" ("+String.join(",",cols)+") VALUES ("+String.join(",",Collections.nCopies(cols.size(),"?"))+")",args.toArray());
    }
    /** Cambia el estado a anulado sin borrar el registro histórico. */
    @Transactional public void anular(long id,String motivo) {
        acceso.exigir(def.permiso());motivo=Reglas.texto(motivo,470,"Motivo");
        int n=jdbc.update("UPDATE "+def.tabla()+" SET estado=?,motivo_anulacion=?,fecha_anulacion=CURRENT_TIMESTAMP WHERE id=? AND estado=?",def.anulado(),"Usuario #"+acceso.usuarioId()+": "+motivo,id,def.registrado());
        if(n!=1) throw new IllegalArgumentException("El registro no existe o ya está anulado.");
    }
}

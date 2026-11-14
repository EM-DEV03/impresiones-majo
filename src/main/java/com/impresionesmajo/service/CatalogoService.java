package com.impresionesmajo.service;

import java.math.BigDecimal;
import java.util.*;
import com.impresionesmajo.security.Acceso;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** Servicio parametrizado para los cuatro catálogos administrables. */
public abstract class CatalogoService {
    public record Campo(String nombre,String etiqueta,String tipo,boolean requerido,int max,String opciones,String referencia) {
        /** Convierte las opciones declaradas en metadatos a valores de formulario. */
        public List<String> valores() {return opciones.isBlank()?List.of():List.of(opciones.split(","));}
    }
    public record Definicion(String ruta,String tabla,String titulo,String permisoVer,String permisoGestion,List<Campo> campos) {}
    protected final JdbcTemplate jdbc; protected final Acceso acceso;
    private final Definicion def;
    protected CatalogoService(JdbcTemplate jdbc,Acceso acceso,Definicion def) {this.jdbc=jdbc;this.acceso=acceso;this.def=def;}
    /** Devuelve la definición que conecta ruta, tabla, permisos y campos. */
    public Definicion definicion() {return def;}
    public List<Map<String,Object>> listar(String q) {
        return listar(q,"TODOS");
    }
    /** Lista registros por nombre y estado, limitando el resultado a 500 filas. */
    public List<Map<String,Object>> listar(String q,String estado) {
        acceso.exigir(def.permisoVer());
        Reglas.opcion(estado,"ACTIVOS","INACTIVOS","TODOS");
        String term=q==null?"":q.trim();
        return jdbc.queryForList("SELECT * FROM "+def.tabla()+" WHERE nombre LIKE ? AND (?='TODOS' OR activo=?) ORDER BY nombre LIMIT 500","%"+term+"%",estado,estado.equals("ACTIVOS"));
    }
    /** Calcula si cada registro se puede eliminar o debe desactivarse para conservar historial. */
    public List<Map<String,Object>> revisarRetiro(List<Long> ids) {
        acceso.exigir(def.permisoGestion());
        var rows=new ArrayList<Map<String,Object>>();
        for(long id:validarIds(ids)) {
            var row=new LinkedHashMap<>(obtener(id));
            row.put("accion",conservaHistorial(row)?"DESACTIVAR":"ELIMINAR");
            rows.add(row);
        }
        return rows;
    }
    private SortedSet<Long> validarIds(List<Long> ids) {
        if(ids==null || ids.isEmpty() || ids.size()>500 || ids.stream().anyMatch(id->id==null || id<=0))
            throw new IllegalArgumentException("Selecciona entre 1 y 500 registros.");
        return new TreeSet<>(ids);
    }
    private boolean conservaHistorial(Map<String,Object> registro) {
        if(def.tabla().equals("producto") && ((BigDecimal)registro.get("stock_actual")).signum()!=0) return true;
        long id=((Number)registro.get("id")).longValue();
        String sql=switch(def.tabla()) {
            case "producto" -> "SELECT (SELECT COUNT(*) FROM detalle_compra WHERE producto_id=?)+(SELECT COUNT(*) FROM detalle_venta WHERE producto_id=?)+(SELECT COUNT(*) FROM movimiento_inventario WHERE producto_id=?)";
            case "proveedor" -> "SELECT (SELECT COUNT(*) FROM producto WHERE proveedor_preferido_id=?)+(SELECT COUNT(*) FROM compra WHERE proveedor_id=?)+(SELECT COUNT(*) FROM inversion_equipo WHERE proveedor_id=?)";
            case "categoria" -> "SELECT COUNT(*) FROM producto WHERE categoria_id=?";
            case "servicio" -> "SELECT COUNT(*) FROM detalle_venta WHERE servicio_id=?";
            default -> throw new IllegalArgumentException("Catálogo no permitido.");
        };
        Object[] args=(def.tabla().equals("producto") || def.tabla().equals("proveedor"))?new Object[]{id,id,id}:new Object[]{id};
        return jdbc.queryForObject(sql,Long.class,args)>0;
    }
    /** Ejecuta el retiro masivo decidido por la revisión previa. */
    @Transactional public String retirar(List<Long> ids) {
        acceso.exigir(def.permisoGestion());
        int eliminados=0,desactivados=0;
        for(long id:validarIds(ids)) {
            var row=bloquear(id);
            boolean conservar=conservaHistorial(row);
            if(!conservar) {
                try {jdbc.update("DELETE FROM "+def.tabla()+" WHERE id=?",id);eliminados++;}
                catch(DataIntegrityViolationException ex) {conservar=true;}
            }
            if(conservar) {jdbc.update("UPDATE "+def.tabla()+" SET activo=0 WHERE id=?",id);desactivados++;}
        }
        return "Retirados del catálogo activo: "+eliminados+" eliminados sin uso y "+desactivados+" desactivados conservando sus datos.";
    }
    /** Obtiene un registro para edición, aplicando el permiso de gestión. */
    public Map<String,Object> obtener(long id) {
        acceso.exigir(def.permisoGestion());
        var list=jdbc.queryForList("SELECT * FROM "+def.tabla()+" WHERE id=?",id);
        if(list.isEmpty()) throw new IllegalArgumentException("Registro inexistente.");
        return list.get(0);
    }
    /** Carga opciones activas para campos que referencian otro catálogo. */
    public Map<String,List<Map<String,Object>>> opciones() {
        var result=new HashMap<String,List<Map<String,Object>>>();
        for(var c:def.campos()) if(!c.referencia().isBlank())
            result.put(c.nombre(),jdbc.queryForList("SELECT id,nombre FROM "+c.referencia()+" WHERE activo=1 ORDER BY nombre"));
        return result;
    }
    /** Activa o desactiva un registro sin eliminar referencias históricas. */
    @Transactional public void cambiarEstado(long id,boolean activo) {
        acceso.exigir(def.permisoGestion());
        bloquear(id);
        jdbc.update("UPDATE "+def.tabla()+" SET activo=? WHERE id=?",activo,id);
    }
    /** Elimina únicamente registros sin stock ni referencias protegidas por FK. */
    @Transactional public void eliminar(long id) {
        acceso.exigir(def.permisoGestion());
        var registro=bloquear(id);
        if(def.tabla().equals("producto") && ((BigDecimal)registro.get("stock_actual")).signum()!=0)
            throw new IllegalArgumentException("No puedes eliminar un producto con existencias. Puedes desactivarlo para conservar su inventario.");
        try {
            // Las FK RESTRICT del esquema protegen también referencias creadas concurrentemente.
            jdbc.update("DELETE FROM "+def.tabla()+" WHERE id=?",id);
        } catch(DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("No se puede eliminar: este registro está relacionado con productos, compras, ventas u otros movimientos. Puedes desactivarlo para conservar el historial.",ex);
        }
    }
    private Map<String,Object> bloquear(long id) {
        var registros=jdbc.queryForList("SELECT * FROM "+def.tabla()+" WHERE id=? FOR UPDATE",id);
        if(registros.isEmpty()) throw new IllegalArgumentException("El registro ya no existe.");
        return registros.get(0);
    }
    /** Convierte campos del formulario, valida referencias y persiste el catálogo. */
    @Transactional public void guardar(Long id,Map<String,String> values) {
        acceso.exigir(def.permisoGestion());
        if(id!=null) obtener(id);
        var args=new ArrayList<Object>(); var cols=new ArrayList<String>();
        for(var c:def.campos()) {
            String raw=values.get(c.nombre()); Object value=null;
            if(c.tipo().equals("checkbox")) value="true".equals(raw);
            else if(raw==null || raw.isBlank()) {
                if(c.requerido()) throw new IllegalArgumentException(c.etiqueta()+" es obligatorio.");
            } else {
                raw=raw.trim();
                value=switch(c.tipo()) {
                    case "money" -> Reglas.importe(raw);
                    case "quantity" -> Reglas.entero(new BigDecimal(raw),false,c.etiqueta());
                    case "select" -> Reglas.opcion(raw,c.valores().toArray(String[]::new));
                    case "reference" -> {
                        long key=Long.parseLong(raw);
                        if(jdbc.queryForList("SELECT id FROM "+c.referencia()+" WHERE id=? AND activo=1",key).isEmpty()) throw new IllegalArgumentException(c.etiqueta()+" no está activo o no existe.");
                        yield key;
                    }
                    default -> Reglas.texto(raw,c.max(),c.etiqueta());
                };
                if(c.tipo().equals("email") && !raw.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new IllegalArgumentException("Correo no válido.");
            }
            cols.add(c.nombre());args.add(value);
        }
        if(id==null) jdbc.update("INSERT INTO "+def.tabla()+" ("+String.join(",",cols)+") VALUES ("+String.join(",",Collections.nCopies(cols.size(),"?"))+")",args.toArray());
        else {args.add(id);jdbc.update("UPDATE "+def.tabla()+" SET "+String.join(",",cols.stream().map(c->c+"=?").toList())+" WHERE id=?",args.toArray());}
    }
    protected static Campo text(String n,String l,int max,boolean required) {return new Campo(n,l,"text",required,max,"","");}
    protected static Campo option(String n,String l,String options) {return new Campo(n,l,"select",true,0,options,"");}
    protected static Campo ref(String n,String l,String table,boolean required) {return new Campo(n,l,"reference",required,0,"",table);}
    protected static Campo money(String n,String l) {return new Campo(n,l,"money",true,0,"","");}
    protected static Campo active() {return new Campo("activo","Activo","checkbox",false,0,"","");}
}

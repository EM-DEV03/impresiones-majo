package com.impresionesmajo.service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.math.BigDecimal;
import org.springframework.transaction.annotation.Transactional;
import com.impresionesmajo.security.Acceso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductoService extends CatalogoService {
    private final InventarioService inventario;
    public ProductoService(JdbcTemplate jdbc,Acceso acceso,InventarioService inventario) {
        super(jdbc,acceso,new Definicion("productos","producto","Productos","PRODUCTOS_VER","PRODUCTOS_GESTIONAR",List.of(ref("categoria_id","Categoría","categoria",true),ref("proveedor_preferido_id","Proveedor preferido","proveedor",false),text("codigo","Código",60,true),text("nombre","Nombre",160,true),option("tipo","Tipo","MERCANCIA,INSUMO,SERVICIO"),option("unidad_medida","¿Cómo lo vendes?","UNIDAD,PAQUETE"),money("precio_venta","Precio por presentación (COP)"),new Campo("stock_minimo","Mínimo para reponer","quantity",true,0,"",""),active())));
        this.inventario=inventario;
    }
    @Override @Transactional public void guardar(Long id,Map<String,String> values) {
        acceso.exigir("PRODUCTOS_GESTIONAR");
        values=new HashMap<>(values);
        String tipo=Reglas.opcion(values.get("tipo"),"MERCANCIA","INSUMO","SERVICIO");
        boolean esServicio="SERVICIO".equals(tipo);
        String presentacion=Reglas.opcion(values.getOrDefault("unidad_medida","").trim().toUpperCase(Locale.ROOT),"UNIDAD","PAQUETE");
        values.put("unidad_medida",presentacion);
        if(id!=null) {
            var actual=jdbc.queryForList("SELECT tipo,unidad_medida,stock_actual FROM producto WHERE id=? FOR UPDATE",id);
            if(actual.isEmpty()) throw new IllegalArgumentException("Producto inexistente.");
            String anterior=actual.get(0).get("unidad_medida").toString().trim().toUpperCase(Locale.ROOT);
            boolean cambiaInventario=esServicio!= "SERVICIO".equals(actual.get(0).get("tipo").toString());
            boolean cambiaPresentacion=!anterior.equals(presentacion) && List.of("UNIDAD","PAQUETE").contains(anterior);
            if(cambiaInventario || cambiaPresentacion) {
                boolean usado=((BigDecimal)actual.get(0).get("stock_actual")).signum()!=0 || jdbc.queryForObject(
                    "SELECT (SELECT COUNT(*) FROM movimiento_inventario WHERE producto_id=?)+(SELECT COUNT(*) FROM detalle_venta WHERE producto_id=?)+(SELECT COUNT(*) FROM detalle_compra WHERE producto_id=?)",
                    Long.class,id,id,id)>0;
                if(usado) throw new IllegalArgumentException(cambiaInventario
                    ? "Este registro ya tiene existencias u operaciones. Crea otro registro para cambiar entre producto físico y servicio."
                    : "Este producto ya tiene existencias u operaciones. Crea otro producto para venderlo con una presentación diferente.");
            }
        }
        String raw=values.get("unidades_iniciales");
        BigDecimal inicial=raw==null || raw.isBlank()?BigDecimal.ZERO:Reglas.entero(new BigDecimal(raw),false,"Unidades iniciales");
        if(esServicio) {
            if(inicial.signum()>0) throw new IllegalArgumentException("Los servicios no tienen existencias iniciales.");
            values.put("proveedor_preferido_id","");
            values.put("stock_minimo","0");
        }
        if(id!=null && inicial.signum()>0)
            throw new IllegalArgumentException("Para cambiar las existencias utiliza Añadir o retirar unidades.");
        BigDecimal costo=null;
        if(inicial.signum()>0) {
            acceso.exigir("STOCK_AJUSTAR");
            if(!"true".equals(values.get("activo"))) throw new IllegalArgumentException("Activa el producto para registrar sus unidades iniciales.");
            costo=Reglas.importe(values.get("costo_inicial"));
        }
        super.guardar(id,values);
        if(esServicio && id!=null) jdbc.update("UPDATE producto SET costo_promedio=0 WHERE id=?",id);
        if(id==null && inicial.signum()>0) {
            Long nuevoId=jdbc.queryForObject("SELECT id FROM producto WHERE codigo=?",Long.class,values.get("codigo").trim());
            inventario.ajustar(nuevoId,inicial,"AJUSTE_ENTRADA","Existencias iniciales al crear el producto",costo);
        }
    }
}


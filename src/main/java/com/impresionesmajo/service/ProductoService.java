package com.impresionesmajo.service;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import org.springframework.transaction.annotation.Transactional;
import com.impresionesmajo.security.Acceso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductoService extends CatalogoService {
    private final InventarioService inventario;
    public ProductoService(JdbcTemplate jdbc,Acceso acceso,InventarioService inventario) {
        super(jdbc,acceso,new Definicion("productos","producto","Productos","PRODUCTOS_VER","PRODUCTOS_GESTIONAR",List.of(ref("categoria_id","Categoría","categoria",true),ref("proveedor_preferido_id","Proveedor preferido","proveedor",false),text("codigo","Código",60,true),text("nombre","Nombre",160,true),text("descripcion","Descripción",255,false),option("tipo","Tipo","MERCANCIA,INSUMO"),text("unidad_medida","¿Cómo lo vendes?",30,true),money("precio_venta","Precio de venta (COP)"),new Campo("stock_minimo","Avisarme cuando queden…","quantity",true,0,"",""),active())));
        this.inventario=inventario;
    }
    @Override @Transactional public void guardar(Long id,Map<String,String> values) {
        acceso.exigir("PRODUCTOS_GESTIONAR");
        String raw=values.get("unidades_iniciales");
        BigDecimal inicial=raw==null || raw.isBlank()?BigDecimal.ZERO:Reglas.entero(new BigDecimal(raw),false,"Unidades iniciales");
        if(id!=null && inicial.signum()>0)
            throw new IllegalArgumentException("Para cambiar las existencias utiliza Añadir o retirar unidades.");
        BigDecimal costo=null;
        if(inicial.signum()>0) {
            acceso.exigir("STOCK_AJUSTAR");
            if(!"true".equals(values.get("activo"))) throw new IllegalArgumentException("Activa el producto para registrar sus unidades iniciales.");
            costo=Reglas.importe(values.get("costo_inicial"));
        }
        super.guardar(id,values);
        if(id==null && inicial.signum()>0) {
            Long nuevoId=jdbc.queryForObject("SELECT id FROM producto WHERE codigo=?",Long.class,values.get("codigo").trim());
            inventario.ajustar(nuevoId,inicial,"AJUSTE_ENTRADA","Existencias iniciales al crear el producto",costo);
        }
    }
}


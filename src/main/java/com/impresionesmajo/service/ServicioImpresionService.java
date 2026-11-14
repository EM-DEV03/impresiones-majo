package com.impresionesmajo.service;

import java.util.List;
import com.impresionesmajo.security.Acceso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ServicioImpresionService extends CatalogoService {
    public ServicioImpresionService(JdbcTemplate jdbc,Acceso acceso) {
        super(jdbc,acceso,new Definicion("servicios","servicio","Servicios de impresión","SERVICIOS_VER","SERVICIOS_GESTIONAR",List.of(text("codigo","Código",60,true),text("nombre","Nombre",160,true),text("descripcion","Descripción",255,false),option("tipo","Tipo","IMPRESION,FOTOCOPIA,ESCANEO,AMPLIACION,OTRO"),option("unidad_cobro","Unidad de cobro","HOJA,CARA,SERVICIO,UNIDAD"),money("precio_unitario","Tarifa (COP)"),money("costo_unitario_estimado","Costo estimado (COP)"),active())));
    }
}


package com.impresionesmajo.service;

import java.util.List;
import com.impresionesmajo.security.Acceso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProveedorService extends CatalogoService {
    public ProveedorService(JdbcTemplate jdbc,Acceso acceso) {
        super(jdbc,acceso,new Definicion("proveedores","proveedor","Proveedores","PROVEEDORES_GESTIONAR","PROVEEDORES_GESTIONAR",List.of(text("nombre","Nombre",160,true),text("contacto","Contacto",120,false),text("telefono","Teléfono",30,false),new Campo("email","Correo","email",false,160,"",""),text("direccion","Dirección",255,false),active())));
    }
}


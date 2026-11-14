package com.impresionesmajo.service;

import java.util.List;
import com.impresionesmajo.security.Acceso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CategoriaService extends CatalogoService {
    public CategoriaService(JdbcTemplate jdbc,Acceso acceso) {
        super(jdbc,acceso,new Definicion("categorias","categoria","Categorías","CATEGORIAS_GESTIONAR","CATEGORIAS_GESTIONAR",List.of(text("nombre","Nombre",100,true),text("descripcion","Descripción",255,false),active())));
    }
}


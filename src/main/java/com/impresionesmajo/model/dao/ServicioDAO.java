package com.impresionesmajo.model.dao;

import com.impresionesmajo.model.vo.ServicioVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ServicioDAO extends BaseDAO<ServicioVO> {
    public ServicioDAO(JdbcTemplate jdbc) {
        super(jdbc, "servicio", ServicioVO.class, "id,codigo,nombre,descripcion,tipo,unidad_cobro,precio_unitario,costo_unitario_estimado,activo,fecha_creacion,fecha_actualizacion");
    }
}
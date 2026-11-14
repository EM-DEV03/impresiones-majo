package com.impresionesmajo.model.dao;

import com.impresionesmajo.model.vo.ProveedorVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProveedorDAO extends BaseDAO<ProveedorVO> {
    public ProveedorDAO(JdbcTemplate jdbc) {
        super(jdbc, "proveedor", ProveedorVO.class, "id,nombre,contacto,telefono,email,direccion,activo,fecha_creacion");
    }
}
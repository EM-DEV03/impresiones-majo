package com.impresionesmajo.model.dao;

import com.impresionesmajo.model.vo.PermisoVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PermisoDAO extends BaseDAO<PermisoVO> {
    public PermisoDAO(JdbcTemplate jdbc) {
        super(jdbc, "permiso", PermisoVO.class, "id,codigo,descripcion");
    }
}
package com.impresionesmajo.model.dao;

import com.impresionesmajo.model.vo.RolVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RolDAO extends BaseDAO<RolVO> {
    public RolDAO(JdbcTemplate jdbc) {
        super(jdbc, "rol", RolVO.class, "id,codigo,nombre,descripcion,activo");
    }
}
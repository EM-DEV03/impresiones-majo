package com.impresionesmajo.model.dao;

import com.impresionesmajo.model.vo.CategoriaVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CategoriaDAO extends BaseDAO<CategoriaVO> {
    public CategoriaDAO(JdbcTemplate jdbc) {
        super(jdbc, "categoria", CategoriaVO.class, "id,nombre,descripcion,activo,fecha_creacion");
    }
}
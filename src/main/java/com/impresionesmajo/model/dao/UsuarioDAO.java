package com.impresionesmajo.model.dao;

import com.impresionesmajo.model.vo.UsuarioVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioDAO extends BaseDAO<UsuarioVO> {
    public UsuarioDAO(JdbcTemplate jdbc) {
        super(jdbc, "usuario", UsuarioVO.class, "id,nombre_usuario,nombre_completo,email,password_hash,activo,fecha_creacion");
    }
}
package com.impresionesmajo.model.dao;

import java.util.List;
import com.impresionesmajo.model.vo.UsuarioRolVO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRolDAO {
    private final JdbcTemplate jdbc;
    public UsuarioRolDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public List<UsuarioRolVO> findAll() {
        return jdbc.query("SELECT usuario_id, rol_id FROM usuario_rol", new BeanPropertyRowMapper<>(UsuarioRolVO.class));
    }
    public void insert(UsuarioRolVO value) {
        jdbc.update("INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (?, ?)", value.getUsuarioId(), value.getRolId());
    }
    public void remove(Long first, Long second) {
        jdbc.update("DELETE FROM usuario_rol WHERE usuario_id=? AND rol_id=?", first, second);
    }
}
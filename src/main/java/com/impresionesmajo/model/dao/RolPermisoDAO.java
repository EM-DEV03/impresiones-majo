package com.impresionesmajo.model.dao;

import java.util.List;
import com.impresionesmajo.model.vo.RolPermisoVO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RolPermisoDAO {
    private final JdbcTemplate jdbc;
    public RolPermisoDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public List<RolPermisoVO> findAll() {
        return jdbc.query("SELECT rol_id, permiso_id FROM rol_permiso", new BeanPropertyRowMapper<>(RolPermisoVO.class));
    }
    public void insert(RolPermisoVO value) {
        jdbc.update("INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (?, ?)", value.getRolId(), value.getPermisoId());
    }
    public void remove(Long first, Long second) {
        jdbc.update("DELETE FROM rol_permiso WHERE rol_id=? AND permiso_id=?", first, second);
    }
}
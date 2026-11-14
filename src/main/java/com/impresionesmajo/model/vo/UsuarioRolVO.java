package com.impresionesmajo.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo de la tabla usuario_rol. No expone datos personales ni secretos en logs. */
public class UsuarioRolVO {
    private Long usuarioId;
    private Long rolId;
    public UsuarioRolVO() {}
    public UsuarioRolVO(Long usuarioId, Long rolId) {
        this.usuarioId = usuarioId;
        this.rolId = rolId;
    }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long value) { this.usuarioId = value; }
    public Long getRolId() { return rolId; }
    public void setRolId(Long value) { this.rolId = value; }
    @Override public String toString() { return "UsuarioRolVO[" + "relacion" + "]"; }
}
package com.impresionesmajo.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo de la tabla rol_permiso. No expone datos personales ni secretos en logs. */
public class RolPermisoVO {
    private Long rolId;
    private Long permisoId;
    public RolPermisoVO() {}
    public RolPermisoVO(Long rolId, Long permisoId) {
        this.rolId = rolId;
        this.permisoId = permisoId;
    }
    public Long getRolId() { return rolId; }
    public void setRolId(Long value) { this.rolId = value; }
    public Long getPermisoId() { return permisoId; }
    public void setPermisoId(Long value) { this.permisoId = value; }
    @Override public String toString() { return "RolPermisoVO[" + "relacion" + "]"; }
}
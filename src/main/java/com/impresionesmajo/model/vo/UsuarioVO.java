package com.impresionesmajo.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo de la tabla usuario. No expone datos personales ni secretos en logs. */
public class UsuarioVO {
    private Long id;
    private String nombreUsuario;
    private String nombreCompleto;
    private String email;
    private String passwordHash;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    public UsuarioVO() {}
    public UsuarioVO(Long id, String nombreUsuario, String nombreCompleto, String email, String passwordHash, Boolean activo, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.passwordHash = passwordHash;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String value) { this.nombreUsuario = value; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String value) { this.nombreCompleto = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { this.email = value; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String value) { this.passwordHash = value; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean value) { this.activo = value; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime value) { this.fechaCreacion = value; }
    @Override public String toString() { return "UsuarioVO[" + "id=" + id + "]"; }
}
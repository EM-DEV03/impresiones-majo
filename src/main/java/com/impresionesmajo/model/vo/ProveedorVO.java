package com.impresionesmajo.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo de la tabla proveedor. No expone datos personales ni secretos en logs. */
public class ProveedorVO {
    private Long id;
    private String nombre;
    private String contacto;
    private String telefono;
    private String email;
    private String direccion;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    public ProveedorVO() {}
    public ProveedorVO(Long id, String nombre, String contacto, String telefono, String email, String direccion, Boolean activo, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.contacto = contacto;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public String getNombre() { return nombre; }
    public void setNombre(String value) { this.nombre = value; }
    public String getContacto() { return contacto; }
    public void setContacto(String value) { this.contacto = value; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String value) { this.telefono = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { this.email = value; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String value) { this.direccion = value; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean value) { this.activo = value; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime value) { this.fechaCreacion = value; }
    @Override public String toString() { return "ProveedorVO[" + "id=" + id + "]"; }
}
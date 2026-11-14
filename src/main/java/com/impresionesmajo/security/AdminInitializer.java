package com.impresionesmajo.security;

import com.impresionesmajo.service.UsuarioService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Arranca el aprovisionamiento opcional del primer administrador. */
@Component
public class AdminInitializer implements ApplicationRunner {
    private final UsuarioService usuarios;
    @Value("${app.admin.username:}") private String username;
    @Value("${app.admin.password:}") private String password;
    @Value("${app.admin.name:Administrador}") private String nombre;
    public AdminInitializer(UsuarioService usuarios) {this.usuarios=usuarios;}
    /** Delega la creación condicional al servicio transaccional de usuarios. */
    @Override public void run(ApplicationArguments args) {usuarios.aprovisionar(username,password,nombre);}
}

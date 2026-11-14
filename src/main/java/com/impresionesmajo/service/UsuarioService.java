package com.impresionesmajo.service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import com.impresionesmajo.security.Acceso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestiona cuentas, roles y el aprovisionamiento del primer administrador. */
@Service
public class UsuarioService {
    private final JdbcTemplate jdbc; private final PasswordEncoder encoder; private final Acceso acceso;
    public UsuarioService(JdbcTemplate jdbc,PasswordEncoder encoder,Acceso acceso) {this.jdbc=jdbc;this.encoder=encoder;this.acceso=acceso;}
    /** Lista usuarios y su rol actual; nunca incluye el hash de contraseña. */
    public List<Map<String,Object>> listar() {
        acceso.exigir("USUARIOS_GESTIONAR");
        return jdbc.queryForList("SELECT u.id,u.nombre_usuario,u.nombre_completo,u.email,u.activo,r.codigo AS rol FROM usuario u LEFT JOIN usuario_rol ur ON ur.usuario_id=u.id LEFT JOIN rol r ON r.id=ur.rol_id ORDER BY u.id");
    }
    /** Crea una cuenta y la vincula con un rol activo. */
    @Transactional public void crear(String username,String nombre,String email,String password,String rol) {
        acceso.exigir("USUARIOS_GESTIONAR"); crearInterno(username,nombre,email,password,rol);
    }
    /** Variante interna usada por el panel y por el aprovisionamiento inicial. */
    private void crearInterno(String username,String nombre,String email,String password,String rol) {
        username=Reglas.texto(username,60,"Usuario"); nombre=Reglas.texto(nombre,160,"Nombre");
        if(!username.matches("[a-zA-Z0-9._-]{3,60}")) throw new IllegalArgumentException("Usuario: utiliza entre 3 y 60 letras, números, puntos o guiones.");
        validarPassword(password); Reglas.opcion(rol,"ADMINISTRADOR","VENDEDOR");
        if(email!=null && !email.isBlank() && (email.length()>160 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))) throw new IllegalArgumentException("Correo no válido.");
        var roles=jdbc.queryForList("SELECT id FROM rol WHERE codigo=? AND activo=1",Long.class,rol);
        if(roles.isEmpty()) throw new IllegalArgumentException("Falta el rol activo en los catálogos del SQL.");
        jdbc.update("INSERT INTO usuario(nombre_usuario,nombre_completo,email,password_hash) VALUES (?,?,?,?)",username,nombre,email==null || email.isBlank()?null:email.trim(),encoder.encode(password));
        Long id=jdbc.queryForObject("SELECT id FROM usuario WHERE nombre_usuario=?",Long.class,username);
        jdbc.update("INSERT INTO usuario_rol(usuario_id,rol_id) VALUES (?,?)",id,roles.get(0));
    }
    /** Aplica los límites compatibles con BCrypt antes de codificar la contraseña. */
    public static void validarPassword(String password) {
        if(password==null || password.length()<12 || password.getBytes(StandardCharsets.UTF_8).length>72)
            throw new IllegalArgumentException("La contraseña debe tener al menos 12 caracteres y máximo 72 bytes UTF-8.");
    }
    /** Actualiza estado, rol y opcionalmente contraseña sin dejar el sistema sin admin. */
    @Transactional public void actualizar(long id,boolean activo,String rol,String password) {
        acceso.exigir("USUARIOS_GESTIONAR"); Reglas.opcion(rol,"ADMINISTRADOR","VENDEDOR");
        // Serializa los cambios de cuentas para impedir desactivar simultáneamente los últimos administradores.
        jdbc.queryForList("SELECT id FROM usuario ORDER BY id FOR UPDATE");
        if(jdbc.queryForList("SELECT id FROM usuario WHERE id=?",id).isEmpty()) throw new IllegalArgumentException("Usuario inexistente.");
        if(id==acceso.usuarioId() && (!activo || !rol.equals("ADMINISTRADOR"))) throw new IllegalArgumentException("No puedes quitarte el acceso de administrador.");
        Long roleId=jdbc.queryForObject("SELECT id FROM rol WHERE codigo=? AND activo=1",Long.class,rol);
        jdbc.update("UPDATE usuario SET activo=? WHERE id=?",activo,id);
        jdbc.update("DELETE FROM usuario_rol WHERE usuario_id=?",id);
        jdbc.update("INSERT INTO usuario_rol(usuario_id,rol_id) VALUES (?,?)",id,roleId);
        if(password!=null && !password.isBlank()) {validarPassword(password);jdbc.update("UPDATE usuario SET password_hash=? WHERE id=?",encoder.encode(password),id);}
        // Lectura actual, no snapshot anterior: importante bajo REPEATABLE READ de MySQL.
        var administradores=jdbc.queryForList("SELECT u.id FROM usuario u JOIN usuario_rol ur ON ur.usuario_id=u.id JOIN rol r ON r.id=ur.rol_id WHERE u.activo=1 AND r.activo=1 AND r.codigo='ADMINISTRADOR' FOR UPDATE",Long.class);
        if(administradores.isEmpty()) throw new IllegalArgumentException("Debe permanecer un administrador activo.");
    }
    /** Crea el primer administrador solo cuando la tabla usuario está vacía. */
    @Transactional public void aprovisionar(String username,String password,String nombre) {
        if(username==null || username.isBlank() || password==null || password.isBlank()) return;
        // Bloqueo del rol estable; no modifica contraseñas ni cuentas existentes.
        jdbc.queryForList("SELECT id FROM rol WHERE codigo='ADMINISTRADOR' FOR UPDATE");
        if(jdbc.queryForObject("SELECT COUNT(*) FROM usuario",Long.class)>0) return;
        crearInterno(username,nombre,null,password,"ADMINISTRADOR");
    }
}

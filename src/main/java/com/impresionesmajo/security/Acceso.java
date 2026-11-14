package com.impresionesmajo.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Fachada de autorización que consulta el estado vigente en la base de datos. */
@Component("acceso")
public class Acceso {
    private final JdbcTemplate jdbc;
    public Acceso(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    /** Devuelve si la sesión actual conserva un permiso activo. */
    public boolean tiene(String permiso) {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth==null || !auth.isAuthenticated() || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) return false;
        // Verifica el estado actual para revocar permisos incluso en sesiones existentes.
        return jdbc.queryForObject("SELECT COUNT(*) FROM usuario u JOIN usuario_rol ur ON ur.usuario_id=u.id JOIN rol r ON r.id=ur.rol_id JOIN rol_permiso rp ON rp.rol_id=r.id JOIN permiso p ON p.id=rp.permiso_id WHERE u.nombre_usuario=? AND u.activo=1 AND r.activo=1 AND p.codigo=?",Long.class,auth.getName(),permiso)>0;
    }
    /** Interrumpe la operación con 403 cuando el permiso no está vigente. */
    public void exigir(String permiso) { if(!tiene(permiso)) throw new AccessDeniedException("No tienes permiso para esta operación."); }
    /** Resuelve el id de la cuenta autenticada y activa. */
    public long usuarioId() {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth==null) throw new AccessDeniedException("Inicia sesión.");
        var ids=jdbc.queryForList("SELECT id FROM usuario WHERE nombre_usuario=? AND activo=1",Long.class,auth.getName());
        if(ids.isEmpty()) throw new AccessDeniedException("La cuenta no está activa.");
        return ids.get(0);
    }
}

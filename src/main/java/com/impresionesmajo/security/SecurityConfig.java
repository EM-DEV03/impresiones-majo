package com.impresionesmajo.security;

import java.util.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    /** BCrypt factor 12 para los hashes almacenados en usuario.password_hash. */
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    /** Carga roles y permisos desde las tablas del modelo, sin usuarios en memoria. */
    @Bean UserDetailsService userDetailsService(JdbcTemplate jdbc) {
        return username -> {
            var rows=jdbc.queryForList("SELECT id,nombre_usuario,password_hash,activo FROM usuario WHERE nombre_usuario=?",username);
            if(rows.isEmpty()) throw new UsernameNotFoundException("Credenciales incorrectas.");
            var row=rows.get(0); Object id=row.get("id");
            var roles=jdbc.queryForList("SELECT r.codigo FROM rol r JOIN usuario_rol ur ON ur.rol_id=r.id WHERE ur.usuario_id=? AND r.activo=1",String.class,id);
            var permissions=jdbc.queryForList("SELECT DISTINCT p.codigo FROM permiso p JOIN rol_permiso rp ON rp.permiso_id=p.id JOIN rol r ON r.id=rp.rol_id JOIN usuario_rol ur ON ur.rol_id=r.id WHERE ur.usuario_id=? AND r.activo=1",String.class,id);
            var authorities=new ArrayList<SimpleGrantedAuthority>();
            roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_"+r)));
            permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
            boolean active=Boolean.TRUE.equals(row.get("activo")) || "1".equals(String.valueOf(row.get("activo")));
            return User.withUsername((String)row.get("nombre_usuario")).password((String)row.get("password_hash"))
                .disabled(!active || roles.isEmpty()).authorities(authorities).build();
        };
    }
    /** Configura login, logout, CSRF y la política de recursos permitidos. */
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a.requestMatchers("/login","/css/**","/js/**","/images/**","/webjars/**","/error").permitAll().anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/",true).failureUrl("/login?error").permitAll())
            .logout(l -> l.logoutSuccessUrl("/login?logout"))
            .headers(h -> h.contentSecurityPolicy(c -> c.policyDirectives("default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self' data:; frame-ancestors 'none'; form-action 'self'")));
        return http.build();
    }
}

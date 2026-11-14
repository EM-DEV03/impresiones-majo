package com.impresionesmajo.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Convierte errores de dominio, permisos, concurrencia y base en respuestas seguras. */
@ControllerAdvice
public class ErrorHandler {
    @ModelAttribute("usuarioActual")
    /** Añade el nombre de sesión a las vistas sin exponer credenciales. */
    public String usuarioActual(java.security.Principal principal) {return principal==null?"":principal.getName();}
    @ExceptionHandler({IllegalArgumentException.class,java.time.DateTimeException.class,MethodArgumentTypeMismatchException.class})
    /** Devuelve 400 para datos de formulario inválidos. */
    public String validacion(Exception e,Model m,HttpServletResponse r){r.setStatus(400);m.addAttribute("mensaje",e instanceof MethodArgumentTypeMismatchException?"Revisa los datos del formulario.":e.getMessage());return "error";}
    @ExceptionHandler(AccessDeniedException.class)
    /** Devuelve 403 sin filtrar detalles del permiso requerido. */
    public String permiso(Exception e,Model m,HttpServletResponse r){r.setStatus(403);m.addAttribute("mensaje","No tienes permiso para realizar esta operación.");return "error";}
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String integridad(Exception e,Model m,HttpServletResponse r){r.setStatus(409);m.addAttribute("mensaje","No se guardó el registro. Revisa los códigos, nombres o correos duplicados y las referencias seleccionadas.");return "error";}
    @ExceptionHandler(ConcurrencyFailureException.class)
    public String concurrencia(Exception e,Model m,HttpServletResponse r){r.setStatus(409);m.addAttribute("mensaje","Otra operación está actualizando estos datos. No se aplicó esta operación; vuelve a intentarlo.");return "error";}
    @ExceptionHandler(DataAccessException.class)
    public String datos(Exception e,Model m,HttpServletResponse r){r.setStatus(503);m.addAttribute("mensaje","No fue posible completar la operación en la base de datos. Comprueba que MySQL esté disponible.");return "error";}
}

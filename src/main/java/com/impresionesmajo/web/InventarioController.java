package com.impresionesmajo.web;

import java.math.BigDecimal;
import com.impresionesmajo.service.InventarioService;
import com.impresionesmajo.security.Acceso;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Endpoints de consulta y ajuste auditado de existencias. */
@Controller
@RequestMapping("/inventario")
public class InventarioController {
    private final InventarioService inventario;private final Acceso acceso;
    public InventarioController(InventarioService inventario,Acceso acceso){this.inventario=inventario;this.acceso=acceso;}
    /** Endpoint JSON sin caché usado por los formularios de operación. */
    @GetMapping("/disponibles") @ResponseBody
    public org.springframework.http.ResponseEntity<java.util.List<java.util.Map<String,Object>>> disponibles(){
        return org.springframework.http.ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noStore()).body(inventario.disponibles());
    }
    /** Renderiza saldos y movimientos cuando el usuario tiene permiso de auditoría. */
    @GetMapping public String index(@RequestParam(required=false) Long productoId,Model model){model.addAttribute("productos",inventario.existencias());model.addAttribute("productoId",productoId);if(acceso.tiene("MOVIMIENTOS_VER")) model.addAttribute("movimientos",inventario.movimientos(productoId));return "inventario";}
    /** Convierte el importe localizado y delega el ajuste al servicio transaccional. */
    @PostMapping("/ajustar") public String ajustar(@RequestParam long productoId,@RequestParam BigDecimal cantidad,@RequestParam String tipo,@RequestParam String motivo,@RequestParam(required=false) String costo,RedirectAttributes flash){
        BigDecimal importe=costo==null || costo.isBlank()?null:com.impresionesmajo.service.Reglas.importe(costo);
        inventario.ajustar(productoId,cantidad,tipo,motivo,importe);flash.addFlashAttribute("exito","Ajuste registrado con trazabilidad.");return "redirect:/inventario";
    }
}

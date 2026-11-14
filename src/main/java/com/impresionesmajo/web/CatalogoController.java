package com.impresionesmajo.web;

import java.util.*;
import com.impresionesmajo.service.CatalogoService;
import com.impresionesmajo.security.Acceso;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Dirige las rutas de catálogo a servicios definidos por metadatos. */
@Controller
@RequestMapping("/catalogos/{modulo:categorias|proveedores|productos|servicios}")
public class CatalogoController {
    private final Map<String,CatalogoService> services=new HashMap<>();private final Acceso acceso;
    public CatalogoController(List<CatalogoService> list,Acceso acceso){list.forEach(s->services.put(s.definicion().ruta(),s));this.acceso=acceso;}
    @GetMapping public String lista(@PathVariable String modulo,@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="ACTIVOS") String estado,Model model){var s=services.get(modulo);model.addAttribute("def",s.definicion());model.addAttribute("filas",s.listar(q,estado));model.addAttribute("q",q);model.addAttribute("estado",estado);return "catalogo/lista";}
    @GetMapping("/nuevo") public String nuevo(@PathVariable String modulo,Model model){return form(modulo,null,model);}
    @GetMapping("/{id}/editar") public String editar(@PathVariable String modulo,@PathVariable long id,Model model){return form(modulo,id,model);}
    private String form(String modulo,Long id,Model model){var s=services.get(modulo);acceso.exigir(s.definicion().permisoGestion());model.addAttribute("def",s.definicion());model.addAttribute("registro",id==null?Map.of("activo",true):s.obtener(id));model.addAttribute("id",id);model.addAttribute("opciones",s.opciones());return "catalogo/form";}
    @PostMapping("/guardar") public String guardar(@PathVariable String modulo,@RequestParam(required=false) Long id,@RequestParam Map<String,String> values,RedirectAttributes flash){services.get(modulo).guardar(id,values);flash.addFlashAttribute("exito","Registro guardado.");return "redirect:/catalogos/"+modulo;}
    @PostMapping("/{id}/estado") public String estado(@PathVariable String modulo,@PathVariable long id,@RequestParam boolean activo,RedirectAttributes flash){
        services.get(modulo).cambiarEstado(id,activo);
        flash.addFlashAttribute("exito",activo?"Registro activado.":"Registro desactivado. Se conserva su historial.");
        return "redirect:/catalogos/"+modulo;
    }
    @PostMapping("/{id}/eliminar") public String eliminar(@PathVariable String modulo,@PathVariable long id,RedirectAttributes flash){
        try {services.get(modulo).eliminar(id);flash.addFlashAttribute("exito","Registro eliminado.");}
        catch(IllegalArgumentException ex) {flash.addFlashAttribute("aviso",ex.getMessage());}
        return "redirect:/catalogos/"+modulo;
    }
    @PostMapping("/retirar/revisar") public String revisar(@PathVariable String modulo,@RequestParam List<Long> ids,Model model){
        var s=services.get(modulo);model.addAttribute("def",s.definicion());model.addAttribute("filas",s.revisarRetiro(ids));return "catalogo/retirar";
    }
    @PostMapping("/retirar") public String retirar(@PathVariable String modulo,@RequestParam List<Long> ids,@RequestParam(defaultValue="false") boolean confirmado,RedirectAttributes flash){
        acceso.exigir(services.get(modulo).definicion().permisoGestion());
        if(!confirmado) throw new IllegalArgumentException("Debes confirmar el retiro de los registros seleccionados.");
        flash.addFlashAttribute("exito",services.get(modulo).retirar(ids));return "redirect:/catalogos/"+modulo;
    }
}

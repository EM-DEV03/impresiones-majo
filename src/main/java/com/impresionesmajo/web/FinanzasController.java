package com.impresionesmajo.web;

import java.util.*;
import java.time.LocalDate;
import com.impresionesmajo.service.FinanzasService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Dirige los tres submódulos financieros mediante definiciones de FinanzasService. */
@Controller
@RequestMapping("/finanzas/{modulo:gastos|capital|inversiones}")
public class FinanzasController {
    private final Map<String,FinanzasService> services=new HashMap<>();private final JdbcTemplate jdbc;
    public FinanzasController(List<FinanzasService> list,JdbcTemplate jdbc){list.forEach(s->services.put(s.definicion().ruta(),s));this.jdbc=jdbc;}
    @GetMapping public String lista(@PathVariable String modulo,Model model){var s=services.get(modulo);model.addAttribute("def",s.definicion());model.addAttribute("filas",s.listar());model.addAttribute("hoy",LocalDate.now());model.addAttribute("proveedores",modulo.equals("inversiones")?jdbc.queryForList("SELECT id,nombre FROM proveedor WHERE activo=1 ORDER BY nombre"):List.of());return "finanzas";}
    /** Registra un movimiento usando el servicio del módulo seleccionado. */
    @PostMapping("/registrar") public String registrar(@PathVariable String modulo,@RequestParam Map<String,String> form,RedirectAttributes flash){services.get(modulo).registrar(form);flash.addFlashAttribute("exito","Registro financiero guardado.");return "redirect:/finanzas/"+modulo;}
    @PostMapping("/{id}/anular") public String anular(@PathVariable String modulo,@PathVariable long id,@RequestParam String motivo,RedirectAttributes flash){services.get(modulo).anular(id,motivo);flash.addFlashAttribute("exito","Registro anulado; se conserva el historial.");return "redirect:/finanzas/"+modulo;}
}

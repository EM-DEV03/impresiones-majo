package com.impresionesmajo.web;

import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import com.impresionesmajo.service.ReporteService;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** Endpoints de login, dashboard y reportes. */
@Controller
public class InicioController {
    private final ReporteService reportes;
    public InicioController(ReporteService reportes){this.reportes=reportes;}
    @GetMapping("/login") public String login(){return "login";}
    /** Renderiza indicadores del dashboard según los permisos actuales. */
    @GetMapping("/") public String inicio(Model model){model.addAttribute("panel",reportes.dashboard());return "dashboard";}
    @GetMapping("/reportes") public String reportes(@RequestParam(required=false) LocalDate desde,@RequestParam(required=false) LocalDate hasta,Model model) {
        if(desde==null) desde=LocalDate.now().withDayOfMonth(1);if(hasta==null) hasta=LocalDate.now();
        model.addAttribute("desde",desde);model.addAttribute("hasta",hasta);model.addAttribute("resumen",reportes.resumen(desde,hasta));
        model.addAttribute("diarios",reportes.diarios(desde,hasta));model.addAttribute("destacados",reportes.masVendidos(desde,hasta));return "reportes";
    }
    @GetMapping("/reportes/exportar") public ResponseEntity<byte[]> exportar(@RequestParam LocalDate desde,@RequestParam LocalDate hasta) {
        var m=reportes.resumen(desde,hasta);var csv=new StringBuilder("\uFEFFIndicador;Valor COP\r\n");
        m.forEach((k,v)->csv.append(k).append(';').append(v).append("\r\n"));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=impresiones-majo-resumen.csv")
            .contentType(new MediaType("text","csv",StandardCharsets.UTF_8)).body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }
}

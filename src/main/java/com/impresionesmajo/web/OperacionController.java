package com.impresionesmajo.web;

import com.impresionesmajo.service.*;
import com.impresionesmajo.security.Acceso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Adaptador HTTP para el flujo común de compras y ventas. */
@Controller
public class OperacionController {
    private final VentaService ventas;private final CompraService compras;private final JdbcTemplate jdbc;private final Acceso acceso;
    public OperacionController(VentaService ventas,CompraService compras,JdbcTemplate jdbc,Acceso acceso){this.ventas=ventas;this.compras=compras;this.jdbc=jdbc;this.acceso=acceso;}
    /** Evita asignación masiva y convierte importes con el formato COP. */
    @InitBinder("operacion") public void binder(WebDataBinder binder){
        binder.setAutoGrowCollectionLimit(100);
        binder.setAllowedFields("proveedorId","metodoPago","descuento","valorRecibido","observaciones","lineas[*].productoId","lineas[*].servicioId","lineas[*].cantidad","lineas[*].costoUnitario");
        binder.registerCustomEditor(java.math.BigDecimal.class,"descuento",new ImporteEditor());
        binder.registerCustomEditor(java.math.BigDecimal.class,"valorRecibido",new ImporteEditor());
        binder.registerCustomEditor(java.math.BigDecimal.class,"lineas.costoUnitario",new ImporteEditor());
    }
    @GetMapping("/{tipo:ventas|compras}") public String lista(@PathVariable String tipo,Model model){model.addAttribute("tipo",tipo);model.addAttribute("filas",tipo.equals("ventas")?ventas.listar():compras.listar());return "operacion/lista";}
    @GetMapping("/{tipo:ventas|compras}/nueva") public String nueva(@PathVariable String tipo,Model model){
        acceso.exigir(tipo.equals("ventas")?"VENTAS_CREAR":"COMPRAS_GESTIONAR");
        model.addAttribute("tipo",tipo);model.addAttribute("operacion",new OperacionForm());
        model.addAttribute("productos",jdbc.queryForList("SELECT id,nombre,codigo,tipo,precio_venta,stock_actual,unidad_medida FROM producto WHERE activo=1"+(tipo.equals("ventas")?" AND tipo IN ('MERCANCIA','SERVICIO')":" AND tipo<>'SERVICIO'")+" ORDER BY nombre"));
        model.addAttribute("servicios",tipo.equals("ventas")?jdbc.queryForList("SELECT id,nombre,precio_unitario,unidad_cobro FROM servicio WHERE activo=1 ORDER BY nombre"):java.util.List.of());
        model.addAttribute("proveedores",tipo.equals("compras")?jdbc.queryForList("SELECT id,nombre FROM proveedor WHERE activo=1 ORDER BY nombre"):java.util.List.of());return "operacion/form";
    }
    @PostMapping("/{tipo:ventas|compras}/crear") public String crear(@PathVariable String tipo,@ModelAttribute("operacion") OperacionForm form,BindingResult result){
        if(result.hasErrors()) throw new IllegalArgumentException("Revisa las cantidades y los importes del formulario.");
        long id=tipo.equals("ventas")?ventas.crear(form):compras.crear(form);return "redirect:/"+tipo+"/"+id;
    }
    @GetMapping("/{tipo:ventas|compras}/{id}") public String detalle(@PathVariable String tipo,@PathVariable long id,Model model){model.addAttribute("tipo",tipo);model.addAttribute("registro",tipo.equals("ventas")?ventas.obtener(id):compras.obtener(id));model.addAttribute("lineas",tipo.equals("ventas")?ventas.lineas(id):compras.lineas(id));return "operacion/detalle";}
    @PostMapping("/{tipo:ventas|compras}/{id}/confirmar") public String confirmar(@PathVariable String tipo,@PathVariable long id,RedirectAttributes flash){if(tipo.equals("ventas")) ventas.confirmar(id);else compras.confirmar(id);flash.addFlashAttribute("exito","Operación confirmada e inventario actualizado.");return "redirect:/"+tipo+"/"+id;}
    @PostMapping("/ventas/{id}/anular") public String anular(@PathVariable long id,@RequestParam String motivo,RedirectAttributes flash){ventas.anular(id,motivo);flash.addFlashAttribute("exito","Venta anulada. Se revirtió el inventario correspondiente.");return "redirect:/ventas/"+id;}
}

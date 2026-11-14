package com.impresionesmajo.web;

import com.impresionesmajo.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Endpoints administrativos para cuentas y roles. */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService usuarios;public UsuarioController(UsuarioService usuarios){this.usuarios=usuarios;}
    @GetMapping public String lista(Model model){model.addAttribute("usuarios",usuarios.listar());return "usuarios";}
    /** Crea una cuenta; el hash y la validación del rol quedan en UsuarioService. */
    @PostMapping("/crear") public String crear(@RequestParam String username,@RequestParam String nombre,@RequestParam(required=false) String email,@RequestParam String password,@RequestParam String rol,RedirectAttributes flash){usuarios.crear(username,nombre,email,password,rol);flash.addFlashAttribute("exito","Usuario creado.");return "redirect:/usuarios";}
    @PostMapping("/{id}/actualizar") public String actualizar(@PathVariable long id,@RequestParam(defaultValue="false") boolean activo,@RequestParam String rol,@RequestParam(required=false) String password,RedirectAttributes flash){usuarios.actualizar(id,activo,rol,password);flash.addFlashAttribute("exito","Usuario actualizado. Los permisos se aplican también a sus sesiones abiertas.");return "redirect:/usuarios";}
}

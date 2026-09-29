package pe.automarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.automarket.exception.ReglaNegocioException;
import pe.automarket.model.Usuario;
import pe.automarket.service.UsuarioService;

/** Módulo Personal (solo Supervisor, ver AuthInterceptor / RN-06). */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarios;

    public UsuarioController(UsuarioService usuarios) { this.usuarios = usuarios; }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("activo", "usuarios");
        model.addAttribute("usuarios", usuarios.listar());
        return "usuarios/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        return formulario(model, new Usuario(), true, "Registrar Nuevo Colaborador", "/usuarios/nuevo", "Guardar registro");
    }

    @PostMapping("/nuevo")
    public String crear(@ModelAttribute("usuario") Usuario usuario, Model model, RedirectAttributes ra) {
        try {
            usuarios.crear(usuario);
            ra.addFlashAttribute("msg", "Colaborador registrado con ID " + usuario.getCodigo() + ".");
            return "redirect:/usuarios";
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, usuario, true, "Registrar Nuevo Colaborador", "/usuarios/nuevo", "Guardar registro");
        }
    }

    @GetMapping("/{codigo}/editar")
    public String editar(@PathVariable String codigo, Model model) {
        return formulario(model, usuarios.obtener(codigo), false, "Modificar Colaborador",
                "/usuarios/" + codigo + "/editar", "Guardar cambios");
    }

    @PostMapping("/{codigo}/editar")
    public String actualizar(@PathVariable String codigo, @ModelAttribute("usuario") Usuario usuario,
                             Model model, RedirectAttributes ra) {
        try {
            usuarios.actualizar(codigo, usuario);
            ra.addFlashAttribute("msg", "Colaborador actualizado.");
            return "redirect:/usuarios";
        } catch (ReglaNegocioException e) {
            usuario.setCodigo(codigo);
            model.addAttribute("error", e.getMessage());
            return formulario(model, usuario, false, "Modificar Colaborador", "/usuarios/" + codigo + "/editar", "Guardar cambios");
        }
    }

    @GetMapping("/{codigo}/bloquear")
    public String confirmar(@PathVariable String codigo, Model model) {
        Usuario u = usuarios.obtener(codigo);
        model.addAttribute("activo", "usuarios");
        model.addAttribute("titulo", "Confirmar bloqueo");
        model.addAttribute("mensaje", "¿Suspender el acceso de " + u.getNombre() + " (" + u.getCodigo() + ") al sistema?");
        model.addAttribute("accion", "/usuarios/" + codigo + "/bloquear");
        model.addAttribute("cancelar", "/usuarios");
        model.addAttribute("boton", "Bloquear acceso");
        return "confirmar";
    }

    @PostMapping("/{codigo}/bloquear")
    public String bloquear(@PathVariable String codigo, @RequestAttribute("usuarioActual") Usuario actual,
                           RedirectAttributes ra) {
        try {
            usuarios.bloquear(codigo, actual.getCodigo());
            ra.addFlashAttribute("msg", "Acceso de " + codigo + " suspendido.");
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/{codigo}/desbloquear")
    public String desbloquear(@PathVariable String codigo, RedirectAttributes ra) {
        usuarios.desbloquear(codigo);
        ra.addFlashAttribute("msg", "Acceso de " + codigo + " restablecido.");
        return "redirect:/usuarios";
    }

    private String formulario(Model model, Usuario u, boolean crear, String titulo, String accion, String boton) {
        model.addAttribute("activo", "usuarios");
        model.addAttribute("usuario", u);
        model.addAttribute("modoCrear", crear);
        model.addAttribute("titulo", titulo);
        model.addAttribute("accion", accion);
        model.addAttribute("boton", boton);
        return "usuarios/form";
    }
}

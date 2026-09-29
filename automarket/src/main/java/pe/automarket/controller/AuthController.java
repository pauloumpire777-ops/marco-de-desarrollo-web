package pe.automarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import pe.automarket.config.AuthInterceptor;
import pe.automarket.exception.ReglaNegocioException;
import pe.automarket.model.Usuario;
import pe.automarket.service.UsuarioService;

@Controller
public class AuthController {

    private final UsuarioService usuarios;

    public AuthController(UsuarioService usuarios) { this.usuarios = usuarios; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @PostMapping("/login")
    public String autenticar(@RequestParam String codigo, @RequestParam String password,
                             HttpServletRequest request, Model model) {
        try {
            Usuario u = usuarios.autenticar(codigo, password);
            HttpSession vieja = request.getSession(false);
            if (vieja != null) vieja.invalidate();                       // evita fijación de sesión
            request.getSession(true).setAttribute(AuthInterceptor.SESSION_USUARIO, u.getCodigo());
            return "redirect:/main";
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("codigo", codigo);
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

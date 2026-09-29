package pe.automarket.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.automarket.model.Usuario;
import pe.automarket.service.UsuarioService;

/**
 * Exige sesión iniciada en todo el sistema (RN-05: el usuario se revalida en cada petición,
 * así un bloqueo surte efecto de inmediato) y perfil Supervisor para Personal y Analítica (RN-06).
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_USUARIO = "USUARIO_CODIGO";
    public static final String REQ_USUARIO = "usuarioActual";

    private final UsuarioService usuarios;

    public AuthInterceptor(UsuarioService usuarios) { this.usuarios = usuarios; }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String ctx = req.getContextPath();
        HttpSession session = req.getSession(false);
        String codigo = session == null ? null : (String) session.getAttribute(SESSION_USUARIO);
        Usuario u = usuarios.buscar(codigo).orElse(null);

        if (u == null || !u.isActivo()) {
            if (session != null) session.invalidate();
            res.sendRedirect(ctx + "/login");
            return false;
        }
        req.setAttribute(REQ_USUARIO, u);

        String path = req.getRequestURI().substring(ctx.length());
        boolean soloSupervisor = path.startsWith("/usuarios") || path.startsWith("/metricas");
        if (soloSupervisor && !u.isSupervisor()) {                       // RN-06
            res.sendRedirect(ctx + "/main?denegado=1");
            return false;
        }
        return true;
    }
}

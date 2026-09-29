package pe.automarket.service;

import java.util.*;

import org.springframework.stereotype.Service;

import pe.automarket.exception.ReglaNegocioException;
import pe.automarket.model.Usuario;

/** Backend dummy del personal. Implementa RN-05 (solo activos ingresan) y RN-07 (auto-bloqueo). */
@Service
public class UsuarioService {

    private final List<Usuario> usuarios = new ArrayList<>();

    public UsuarioService() {
        usuarios.add(new Usuario("EMP-0001", "Satoru Gojo", "u23257285@automarket.pe", Usuario.CAJERO, "1234", true));
        usuarios.add(new Usuario("SUP-0001", "David Messi", "u23257286@automarket.pe", Usuario.SUPERVISOR, "1234", true));
        usuarios.add(new Usuario("EMP-0003", "Michael Newman", "u23257287@automarket.pe", Usuario.CAJERO, "1234", false));
    }

    private static boolean vacio(String s) { return s == null || s.isBlank(); }

    public List<Usuario> listar() { return new ArrayList<>(usuarios); }

    public Optional<Usuario> buscar(String codigo) {
        if (codigo == null) return Optional.empty();
        return usuarios.stream().filter(u -> u.getCodigo().equalsIgnoreCase(codigo.trim())).findFirst();
    }

    public Usuario obtener(String codigo) {
        return buscar(codigo).orElseThrow(() -> new ReglaNegocioException("El colaborador solicitado no existe."));
    }

    /** RN-05: un colaborador suspendido no puede iniciar sesión. */
    public Usuario autenticar(String codigo, String password) {
        Usuario u = buscar(codigo).orElse(null);
        if (u == null || password == null || !password.equals(u.getPassword()))
            throw new ReglaNegocioException("ID de colaborador o contraseña incorrectos.");
        if (!u.isActivo())
            throw new ReglaNegocioException("RN-05: su acceso está suspendido. Contacte a un supervisor.");
        return u;
    }

    private void validar(Usuario u, String codigoActual) {
        if (vacio(u.getNombre())) throw new ReglaNegocioException("El nombre completo es obligatorio.");
        if (vacio(u.getCorreo()) || !u.getCorreo().contains("@"))
            throw new ReglaNegocioException("Ingrese un correo corporativo válido.");
        if (!Usuario.CAJERO.equals(u.getRol()) && !Usuario.SUPERVISOR.equals(u.getRol()))
            throw new ReglaNegocioException("Seleccione un rol del sistema.");
        boolean correoDuplicado = usuarios.stream().anyMatch(x ->
                x.getCorreo().equalsIgnoreCase(u.getCorreo().trim()) && !x.getCodigo().equals(codigoActual));
        if (correoDuplicado) throw new ReglaNegocioException("El correo «" + u.getCorreo().trim() + "» ya está registrado.");
    }

    private String siguienteCodigo(String rol) {
        String prefijo = Usuario.SUPERVISOR.equals(rol) ? "SUP" : "EMP";
        int max = usuarios.stream().filter(u -> u.getCodigo().startsWith(prefijo))
                .mapToInt(u -> Integer.parseInt(u.getCodigo().substring(4))).max().orElse(0);
        return String.format("%s-%04d", prefijo, max + 1);
    }

    public synchronized Usuario crear(Usuario u) {
        validar(u, null);
        if (vacio(u.getPassword()) || u.getPassword().length() < 4)
            throw new ReglaNegocioException("La contraseña temporal debe tener al menos 4 caracteres.");
        u.setCodigo(siguienteCodigo(u.getRol()));
        u.setNombre(u.getNombre().trim());
        u.setCorreo(u.getCorreo().trim());
        u.setActivo(true);
        usuarios.add(u);
        return u;
    }

    public synchronized void actualizar(String codigo, Usuario datos) {
        Usuario actual = obtener(codigo);
        validar(datos, codigo);
        actual.setNombre(datos.getNombre().trim());
        actual.setCorreo(datos.getCorreo().trim());
        actual.setRol(datos.getRol());
    }

    /** RN-07: un colaborador no puede bloquear su propio acceso. */
    public synchronized void bloquear(String codigo, String codigoQuienBloquea) {
        if (codigo.equalsIgnoreCase(codigoQuienBloquea))
            throw new ReglaNegocioException("RN-07: no puede bloquear su propio acceso.");
        obtener(codigo).setActivo(false);
    }

    public synchronized void desbloquear(String codigo) { obtener(codigo).setActivo(true); }
}

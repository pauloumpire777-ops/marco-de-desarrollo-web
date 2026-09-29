package pe.automarket.model;

public class Usuario {
    public static final String CAJERO = "Cajero";
    public static final String SUPERVISOR = "Supervisor";

    private String codigo;      // EMP-0001 / SUP-0001
    private String nombre;
    private String correo;
    private String rol = CAJERO;
    private String password;
    private boolean activo = true;

    public Usuario() {}
    public Usuario(String codigo, String nombre, String correo, String rol, String password, boolean activo) {
        this.codigo = codigo; this.nombre = nombre; this.correo = correo; this.rol = rol;
        this.password = password; this.activo = activo;
    }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public boolean isSupervisor() { return SUPERVISOR.equals(rol); }
}

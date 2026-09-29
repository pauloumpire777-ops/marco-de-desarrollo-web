package pe.automarket.model;

import java.io.Serializable;

public class Categoria implements Serializable {
    private Long id;
    private String nombre;
    private String descripcion;
    private String icono = "📦";
    private boolean activo = true;

    public Categoria() {}
    public Categoria(Long id, String nombre, String descripcion, String icono, boolean activo) {
        this.id = id; this.nombre = nombre; this.descripcion = descripcion; this.icono = icono; this.activo = activo;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}

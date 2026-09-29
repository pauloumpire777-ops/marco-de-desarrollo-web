package pe.automarket.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Producto implements Serializable {
    private Long id;
    private String codigo;          // código de barras / SKU
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
    private Long categoriaId;
    private boolean activo = true;

    public Producto() {}
    public Producto(Long id, String codigo, String nombre, BigDecimal precio, Integer stock, Long categoriaId, boolean activo) {
        this.id = id; this.codigo = codigo; this.nombre = nombre; this.precio = precio;
        this.stock = stock; this.categoriaId = categoriaId; this.activo = activo;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}

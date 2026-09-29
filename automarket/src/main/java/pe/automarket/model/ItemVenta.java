package pe.automarket.model;

import java.math.BigDecimal;

public class ItemVenta {
    private final String nombre;
    private final String categoria;
    private final int cantidad;
    private final BigDecimal precioUnit;

    public ItemVenta(String nombre, String categoria, int cantidad, BigDecimal precioUnit) {
        this.nombre = nombre; this.categoria = categoria; this.cantidad = cantidad; this.precioUnit = precioUnit;
    }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnit() { return precioUnit; }
    public BigDecimal getImporte() { return precioUnit.multiply(BigDecimal.valueOf(cantidad)); }
}

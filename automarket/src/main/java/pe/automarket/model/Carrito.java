package pe.automarket.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Carrito de la caja de autopago (vive en la sesión HTTP). */
public class Carrito implements Serializable {
    private final List<ItemCarrito> items = new ArrayList<>();
    private Instant inicio = Instant.now();

    public List<ItemCarrito> getItems() { return items; }
    public Instant getInicio() { return inicio; }
    public void reiniciarInicio() { this.inicio = Instant.now(); }
    public boolean isVacio() { return items.isEmpty(); }
    public void vaciar() { items.clear(); }

    public ItemCarrito buscar(String codigo) {
        return items.stream().filter(i -> i.getProducto().getCodigo().equals(codigo)).findFirst().orElse(null);
    }
    public BigDecimal getTotal() {
        return items.stream().map(ItemCarrito::getImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public int getCantidadTotal() { return items.stream().mapToInt(ItemCarrito::getCantidad).sum(); }
}

package pe.automarket.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {
    private Long id;
    private LocalDateTime fecha = LocalDateTime.now();
    private String cajero;
    private String metodo;
    private long segundos;
    private final List<ItemVenta> items = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFecha() { return fecha; }
    public String getCajero() { return cajero; }
    public void setCajero(String cajero) { this.cajero = cajero; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public long getSegundos() { return segundos; }
    public void setSegundos(long segundos) { this.segundos = segundos; }
    public List<ItemVenta> getItems() { return items; }
    public BigDecimal getTotal() {
        return items.stream().map(ItemVenta::getImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

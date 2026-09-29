package pe.automarket.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import pe.automarket.exception.ReglaNegocioException;
import pe.automarket.model.*;

/**
 * Caja de autopago simulada.
 * RN-03: al confirmar la venta se descuenta el stock.
 * RN-04: no se puede vender más unidades que el stock disponible.
 * RN-02: solo se venden productos activos de categorías activas.
 */
@Service
public class VentaService {

    private final InventarioService inventario;
    private final List<Venta> ventas = new ArrayList<>();
    private long seq = 0;

    public VentaService(InventarioService inventario) { this.inventario = inventario; }

    public synchronized List<Venta> listar() { return new ArrayList<>(ventas); }

    public synchronized Venta buscar(Long id) {
        return ventas.stream().filter(v -> v.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ReglaNegocioException("La boleta solicitada no existe."));
    }

    public synchronized void agregar(Carrito carrito, String codigo, int cantidad) {
        if (cantidad < 1) throw new ReglaNegocioException("La cantidad debe ser al menos 1.");
        Producto p = inventario.buscarPorCodigo(codigo);
        if (p == null) throw new ReglaNegocioException("El código de barras «" + (codigo == null ? "" : codigo.trim()) + "» no está registrado.");
        if (!inventario.esVendible(p))
            throw new ReglaNegocioException("RN-02: «" + p.getNombre() + "» no está disponible para la venta (producto o categoría desactivados).");
        ItemCarrito item = carrito.buscar(p.getCodigo());
        int enCarrito = item == null ? 0 : item.getCantidad();
        if (enCarrito + cantidad > p.getStock())
            throw new ReglaNegocioException("RN-04: stock insuficiente de «" + p.getNombre() + "». Disponible: "
                    + p.getStock() + ", ya en el carrito: " + enCarrito + ".");
        if (carrito.isVacio()) carrito.reiniciarInicio();
        if (item == null) carrito.getItems().add(new ItemCarrito(p, cantidad));
        else item.setCantidad(enCarrito + cantidad);
    }

    public synchronized void restar(Carrito carrito, String codigo) {
        ItemCarrito item = carrito.buscar(codigo);
        if (item == null) return;
        if (item.getCantidad() <= 1) carrito.getItems().remove(item);
        else item.setCantidad(item.getCantidad() - 1);
    }

    public synchronized void quitar(Carrito carrito, String codigo) {
        ItemCarrito item = carrito.buscar(codigo);
        if (item != null) carrito.getItems().remove(item);
    }

    /** Confirma el pago: revalida reglas, descuenta stock (RN-03) y genera la boleta. */
    public synchronized Venta confirmar(Carrito carrito, String metodo, String cajero) {
        if (carrito.isVacio()) throw new ReglaNegocioException("El carrito está vacío.");
        if (metodo == null || metodo.isBlank()) throw new ReglaNegocioException("Seleccione un método de pago.");

        for (ItemCarrito it : carrito.getItems()) {
            Producto p = it.getProducto();
            if (!inventario.esVendible(p))
                throw new ReglaNegocioException("RN-02: «" + p.getNombre() + "» ya no está disponible para la venta. Quítelo del carrito.");
            if (it.getCantidad() > p.getStock())
                throw new ReglaNegocioException("RN-04: stock insuficiente de «" + p.getNombre() + "». Disponible: " + p.getStock() + ".");
        }

        Venta venta = new Venta();
        venta.setId(++seq);
        venta.setCajero(cajero);
        venta.setMetodo(metodo);
        long seg = Duration.between(carrito.getInicio(), Instant.now()).getSeconds();
        venta.setSegundos(Math.min(Math.max(seg, 1), 600));

        for (ItemCarrito it : carrito.getItems()) {
            Producto p = it.getProducto();
            p.setStock(p.getStock() - it.getCantidad());               // RN-03
            venta.getItems().add(new ItemVenta(p.getNombre(),
                    inventario.categoria(p.getCategoriaId()).getNombre(), it.getCantidad(), p.getPrecio()));
        }
        ventas.add(venta);
        carrito.vaciar();
        return venta;
    }
}

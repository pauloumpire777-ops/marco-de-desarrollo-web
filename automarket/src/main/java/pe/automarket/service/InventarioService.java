package pe.automarket.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import pe.automarket.exception.ReglaNegocioException;
import pe.automarket.model.Categoria;
import pe.automarket.model.Producto;

/**
 * Backend dummy: categorías y productos en listas Java en memoria.
 * Implementa RN-01 (desactivación lógica) y RN-02 (cascada / producto no vendible).
 */
@Service
public class InventarioService {

    private final List<Categoria> categorias = new ArrayList<>();
    private final List<Producto> productos = new ArrayList<>();
    private long catSeq = 0;
    private long prodSeq = 0;

    public InventarioService() {
        Categoria bebidas = cat("Bebidas", "Gaseosas, jugos y aguas embotelladas", "🥤", true);
        Categoria snacks = cat("Snacks", "Galletas, papas fritas y golosinas", "🍫", true);
        Categoria lacteos = cat("Lácteos", "Leche, yogurt y quesos", "🥛", true);
        Categoria limpieza = cat("Limpieza", "Detergentes y artículos del hogar", "🧴", false);

        prod("7750001", "Inca Kola 600ml", "3.50", 120, bebidas, true);
        prod("7750002", "Coca Cola 500ml", "3.50", 80, bebidas, true);
        prod("4901005", "Pocky de Matcha", "6.90", 12, snacks, true);
        prod("7750003", "Galletas Morochas", "1.50", 45, snacks, true);
        prod("7750004", "Papas Lays Clásicas", "4.90", 60, snacks, true);
        prod("7750005", "Leche Gloria 1L", "5.20", 40, lacteos, true);
        prod("7750006", "Yogurt Gloria 1L", "7.50", 25, lacteos, true);
        prod("7750007", "Detergente Ariel 500g", "8.90", 30, limpieza, false);
    }

    private Categoria cat(String nombre, String desc, String icono, boolean activo) {
        Categoria c = new Categoria(++catSeq, nombre, desc, icono, activo);
        categorias.add(c);
        return c;
    }

    private void prod(String codigo, String nombre, String precio, int stock, Categoria c, boolean activo) {
        productos.add(new Producto(++prodSeq, codigo, nombre, new BigDecimal(precio), stock, c.getId(), activo));
    }

    private static boolean vacio(String s) { return s == null || s.isBlank(); }

    // ======================= CATEGORÍAS =======================

    public List<Categoria> listarCategorias() { return new ArrayList<>(categorias); }

    public List<Categoria> categoriasActivas() {
        return categorias.stream().filter(Categoria::isActivo).collect(Collectors.toList());
    }

    public Map<Long, Categoria> mapaCategorias() {
        return categorias.stream().collect(Collectors.toMap(Categoria::getId, c -> c));
    }

    public Categoria categoria(Long id) {
        return categorias.stream().filter(c -> c.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ReglaNegocioException("La categoría solicitada no existe."));
    }

    private void validarCategoria(Categoria c, Long idActual) {
        if (vacio(c.getNombre())) throw new ReglaNegocioException("El nombre de la categoría es obligatorio.");
        boolean duplicada = categorias.stream().anyMatch(x ->
                x.getNombre().equalsIgnoreCase(c.getNombre().trim()) && !x.getId().equals(idActual));
        if (duplicada) throw new ReglaNegocioException("Ya existe una categoría con el nombre «" + c.getNombre().trim() + "».");
    }

    public synchronized Categoria crearCategoria(Categoria c) {
        validarCategoria(c, null);
        c.setId(++catSeq);
        c.setNombre(c.getNombre().trim());
        c.setActivo(true);
        if (vacio(c.getIcono())) c.setIcono("📦");
        categorias.add(c);
        return c;
    }

    public synchronized void actualizarCategoria(Long id, Categoria datos) {
        Categoria actual = categoria(id);
        validarCategoria(datos, id);
        actual.setNombre(datos.getNombre().trim());
        actual.setDescripcion(datos.getDescripcion());
        if (!vacio(datos.getIcono())) actual.setIcono(datos.getIcono());
    }

    /**
     * RN-01: las categorías nunca se eliminan físicamente, solo se desactivan.
     * RN-02: al desactivar una categoría se desactivan todos sus productos.
     */
    public synchronized int desactivarCategoria(Long id) {
        Categoria c = categoria(id);
        c.setActivo(false);
        int afectados = 0;
        for (Producto p : productos) {
            if (p.getCategoriaId().equals(id) && p.isActivo()) { p.setActivo(false); afectados++; }
        }
        return afectados;
    }

    public synchronized void activarCategoria(Long id) { categoria(id).setActivo(true); }

    // ======================= PRODUCTOS =======================

    public List<Producto> listarProductos(String q, Long categoriaId) {
        return productos.stream()
                .filter(p -> vacio(q) || p.getNombre().toLowerCase().contains(q.trim().toLowerCase())
                        || p.getCodigo().contains(q.trim()))
                .filter(p -> categoriaId == null || p.getCategoriaId().equals(categoriaId))
                .collect(Collectors.toList());
    }

    public Producto producto(Long id) {
        return productos.stream().filter(p -> p.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ReglaNegocioException("El producto solicitado no existe."));
    }

    public Producto buscarPorCodigo(String codigo) {
        if (vacio(codigo)) return null;
        return productos.stream().filter(p -> p.getCodigo().equalsIgnoreCase(codigo.trim())).findFirst().orElse(null);
    }

    /** RN-02: solo se vende un producto activo cuya categoría también está activa. */
    public boolean esVendible(Producto p) {
        return p.isActivo() && categoria(p.getCategoriaId()).isActivo();
    }

    public List<Producto> catalogoVendible() {
        return productos.stream().filter(p -> esVendible(p) && p.getStock() > 0).collect(Collectors.toList());
    }

    private void validarProducto(Producto p, Producto actual) {
        if (vacio(p.getNombre())) throw new ReglaNegocioException("El nombre del producto es obligatorio.");
        if (vacio(p.getCodigo())) throw new ReglaNegocioException("El código de barras es obligatorio.");
        if (p.getPrecio() == null || p.getPrecio().signum() <= 0)
            throw new ReglaNegocioException("El precio debe ser mayor a S/ 0.00.");
        if (p.getStock() == null || p.getStock() < 0)
            throw new ReglaNegocioException("El stock no puede ser negativo.");
        if (p.getCategoriaId() == null) throw new ReglaNegocioException("Seleccione una categoría.");
        Categoria c = categoria(p.getCategoriaId());
        boolean mismaCategoria = actual != null && actual.getCategoriaId().equals(p.getCategoriaId());
        if (!c.isActivo() && !mismaCategoria)
            throw new ReglaNegocioException("RN-02: no se pueden asignar productos a una categoría desactivada.");
        boolean codigoDuplicado = productos.stream().anyMatch(x ->
                x.getCodigo().equalsIgnoreCase(p.getCodigo().trim()) && (actual == null || !x.getId().equals(actual.getId())));
        if (codigoDuplicado) throw new ReglaNegocioException("El código de barras «" + p.getCodigo().trim() + "» ya está registrado.");
    }

    public synchronized Producto crearProducto(Producto p) {
        validarProducto(p, null);
        p.setId(++prodSeq);
        p.setCodigo(p.getCodigo().trim());
        p.setNombre(p.getNombre().trim());
        p.setActivo(true);
        productos.add(p);
        return p;
    }

    public synchronized void actualizarProducto(Long id, Producto datos) {
        Producto actual = producto(id);
        validarProducto(datos, actual);
        actual.setCodigo(datos.getCodigo().trim());
        actual.setNombre(datos.getNombre().trim());
        actual.setPrecio(datos.getPrecio());
        actual.setStock(datos.getStock());
        actual.setCategoriaId(datos.getCategoriaId());
    }

    public synchronized void desactivarProducto(Long id) { producto(id).setActivo(false); }

    /** RN-02: un producto no puede reactivarse mientras su categoría siga desactivada. */
    public synchronized void activarProducto(Long id) {
        Producto p = producto(id);
        if (!categoria(p.getCategoriaId()).isActivo())
            throw new ReglaNegocioException("RN-02: reactive primero la categoría «"
                    + categoria(p.getCategoriaId()).getNombre() + "» para poder activar este producto.");
        p.setActivo(true);
    }
}

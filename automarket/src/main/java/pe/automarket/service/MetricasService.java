package pe.automarket.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import pe.automarket.model.ItemVenta;
import pe.automarket.model.Venta;

/**
 * Métricas dummy: datos históricos simulados + las ventas reales hechas en la caja durante la ejecución,
 * de modo que el dashboard "se mueve" al vender durante la exposición.
 */
@Service
public class MetricasService {

    private static final Locale ES = new Locale("es", "PE");
    private static final double[] SEED_VENTAS = {18300, 17100, 15600, 14300, 12500, 13200, 16200, 14800};
    private static final int[] SEED_AFLUENCIA = {320, 290, 310, 340, 410, 520, 380};       // Lun..Dom
    private static final int[] SEED_TIEMPO = {95, 90, 88, 84, 80, 78, 74, 71};             // segundos
    private static final int PESO_HISTORICO = 20;                                          // tickets simulados del mes

    private final VentaService ventas;

    public MetricasService(VentaService ventas) { this.ventas = ventas; }

    public List<String> meses() {
        YearMonth ahora = YearMonth.now();
        List<String> l = new ArrayList<>();
        for (int i = SEED_VENTAS.length - 1; i >= 0; i--)
            l.add(cap(ahora.minusMonths(i).getMonth().getDisplayName(TextStyle.SHORT, ES).replace(".", "")));
        return l;
    }

    private static String cap(String s) { return s.substring(0, 1).toUpperCase() + s.substring(1); }

    public List<Double> ventasPorMes() {
        double[] v = SEED_VENTAS.clone();
        v[v.length - 1] += ingresosReales().doubleValue();
        return Arrays.stream(v).boxed().collect(Collectors.toList());
    }

    public List<String> dias() {
        List<String> l = new ArrayList<>();
        for (DayOfWeek d : DayOfWeek.values()) l.add(cap(d.getDisplayName(TextStyle.SHORT, ES).replace(".", "")));
        return l;
    }

    public List<Integer> afluenciaPorDia() {
        int[] a = SEED_AFLUENCIA.clone();
        for (Venta v : ventas.listar()) a[v.getFecha().getDayOfWeek().getValue() - 1]++;
        return Arrays.stream(a).boxed().collect(Collectors.toList());
    }

    private Map<String, Integer> topSeed() {
        Map<String, Integer> m = new HashMap<>();
        m.put("Inca Kola 600ml", 420); m.put("Coca Cola 500ml", 380); m.put("Papas Lays Clásicas", 310);
        m.put("Leche Gloria 1L", 270); m.put("Pocky de Matcha", 190); m.put("Galletas Morochas", 150);
        m.put("Yogurt Gloria 1L", 120);
        return m;
    }

    public Map<String, Integer> top5Productos() {
        Map<String, Integer> m = topSeed();
        for (Venta v : ventas.listar())
            for (ItemVenta i : v.getItems()) m.merge(i.getNombre(), i.getCantidad(), Integer::sum);
        Map<String, Integer> top = new LinkedHashMap<>();
        m.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(5)
                .forEach(e -> top.put(e.getKey(), e.getValue()));
        return top;
    }

    public List<Integer> tiempoPromedio() {
        int[] t = SEED_TIEMPO.clone();
        List<Venta> reales = ventas.listar();
        if (!reales.isEmpty()) {
            long suma = reales.stream().mapToLong(Venta::getSegundos).sum();
            t[t.length - 1] = (int) ((t[t.length - 1] * (long) PESO_HISTORICO + suma) / (PESO_HISTORICO + reales.size()));
        }
        return Arrays.stream(t).boxed().collect(Collectors.toList());
    }

    public Map<String, Integer> categoriasPopulares() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("Bebidas", 1240); m.put("Snacks", 980); m.put("Lácteos", 610); m.put("Limpieza", 240);
        for (Venta v : ventas.listar())
            for (ItemVenta i : v.getItems()) m.merge(i.getCategoria(), i.getCantidad(), Integer::sum);
        return m;
    }

    public int ticketsReales() { return ventas.listar().size(); }

    public BigDecimal ingresosReales() {
        return ventas.listar().stream().map(Venta::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

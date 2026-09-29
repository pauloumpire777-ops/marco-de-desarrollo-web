package pe.automarket.controller;

import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import pe.automarket.service.MetricasService;

/** Analítica (solo Supervisor, ver AuthInterceptor / RN-06). */
@Controller
public class MetricasController {

    private final MetricasService metricas;

    public MetricasController(MetricasService metricas) { this.metricas = metricas; }

    @GetMapping("/metricas")
    public String dashboard(Model model) {
        model.addAttribute("activo", "metricas");
        model.addAttribute("meses", metricas.meses());
        model.addAttribute("ventasMeses", metricas.ventasPorMes());
        model.addAttribute("dias", metricas.dias());
        model.addAttribute("afluencia", metricas.afluenciaPorDia());
        var top = metricas.top5Productos();
        model.addAttribute("topNombres", new ArrayList<>(top.keySet()));
        model.addAttribute("topValores", new ArrayList<>(top.values()));
        model.addAttribute("tiempos", metricas.tiempoPromedio());
        var cats = metricas.categoriasPopulares();
        model.addAttribute("catNombres", new ArrayList<>(cats.keySet()));
        model.addAttribute("catValores", new ArrayList<>(cats.values()));
        model.addAttribute("ticketsReales", metricas.ticketsReales());
        model.addAttribute("ingresosReales", metricas.ingresosReales());
        return "metricas";
    }
}

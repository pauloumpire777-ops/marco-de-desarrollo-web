package pe.automarket.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.automarket.model.Producto;
import pe.automarket.model.Usuario;
import pe.automarket.service.InventarioService;
import pe.automarket.service.VentaService;

/** Rutas base: inicio, panel principal, soporte (contacto) y promociones. */
@Controller
public class PaginaController {

    /** Ticket de soporte simulado (lista en memoria). */
    public record Ticket(int numero, String nombre, String correo, String detalle, LocalDateTime fecha) {}

    private final List<Ticket> tickets = new ArrayList<>();
    private final InventarioService inventario;
    private final VentaService ventas;

    public PaginaController(InventarioService inventario, VentaService ventas) {
        this.inventario = inventario;
        this.ventas = ventas;
    }

    @GetMapping("/")
    public String raiz() { return "redirect:/main"; }

    @GetMapping("/main")
    public String main(Model model) {
        List<Producto> todos = inventario.listarProductos(null, null);
        model.addAttribute("activo", "inicio");
        model.addAttribute("productosActivos", todos.stream().filter(Producto::isActivo).count());
        model.addAttribute("categoriasActivas", inventario.categoriasActivas().size());
        model.addAttribute("stockBajo", todos.stream().filter(p -> p.isActivo() && p.getStock() <= 15).count());
        model.addAttribute("ventasRealizadas", ventas.listar().size());
        return "main";
    }

    @GetMapping("/contacto")
    public String contacto(Model model) {
        model.addAttribute("activo", "contacto");
        return "contacto";
    }

    @PostMapping("/contacto")
    public String enviarTicket(@RequestParam String nombre, @RequestParam String correo,
                               @RequestParam String detalle, RedirectAttributes ra) {
        if (nombre.isBlank() || correo.isBlank() || detalle.isBlank()) {
            ra.addFlashAttribute("error", "Complete nombre, correo y detalle del incidente.");
            return "redirect:/contacto";
        }
        int numero;
        synchronized (tickets) {
            numero = tickets.size() + 1;
            tickets.add(new Ticket(numero, nombre.trim(), correo.trim(), detalle.trim(), LocalDateTime.now()));
        }
        ra.addFlashAttribute("msg", String.format("Ticket T-%03d generado. Soporte se comunicará con usted.", numero));
        return "redirect:/contacto";
    }

    @GetMapping("/publicidad")
    public String publicidad(Model model) {
        model.addAttribute("activo", "publicidad");
        // {titulo, descripcion, emoji, color de fondo}
        List<String[]> promos = List.of(
                new String[]{"Festival de Bebidas Locales", "Lleva 3 y paga 2 en gaseosas de 1 litro.", "🥤", "#36C9C6"},
                new String[]{"Semana del Snack", "20% de descuento en snacks salados seleccionados.", "🍫", "#ED6A5A"},
                new String[]{"Desayunos Nutritivos", "Precios especiales en yogurts y lácteos de marca nacional.", "🥛", "#2C3E50"});
        model.addAttribute("promos", promos);
        return "publicidad";
    }
}

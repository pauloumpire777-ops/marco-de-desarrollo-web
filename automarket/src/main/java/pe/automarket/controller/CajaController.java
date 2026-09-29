package pe.automarket.controller;

import java.time.LocalTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import pe.automarket.exception.ReglaNegocioException;
import pe.automarket.model.Carrito;
import pe.automarket.model.Usuario;
import pe.automarket.model.Venta;
import pe.automarket.service.InventarioService;
import pe.automarket.service.VentaService;

/** Caja rápida de autopago (simulada). El carrito vive en la sesión HTTP. */
@Controller
@RequestMapping("/caja")
public class CajaController {

    private static final String CARRITO = "CARRITO";

    private final InventarioService inventario;
    private final VentaService ventas;

    public CajaController(InventarioService inventario, VentaService ventas) {
        this.inventario = inventario;
        this.ventas = ventas;
    }

    private Carrito carrito(HttpSession session) {
        Carrito c = (Carrito) session.getAttribute(CARRITO);
        if (c == null) { c = new Carrito(); session.setAttribute(CARRITO, c); }
        return c;
    }

    @GetMapping
    public String caja(HttpSession session, Model model) {
        model.addAttribute("activo", "caja");
        model.addAttribute("carrito", carrito(session));
        model.addAttribute("catalogo", inventario.catalogoVendible());
        model.addAttribute("categoriasMap", inventario.mapaCategorias());
        model.addAttribute("turno", LocalTime.now().getHour() < 14 ? "Turno Mañana" : "Turno Tarde");
        return "caja";
    }

    @PostMapping("/agregar")
    public String agregar(@RequestParam String codigo, @RequestParam(defaultValue = "1") int cantidad,
                          HttpSession session, RedirectAttributes ra) {
        try {
            ventas.agregar(carrito(session), codigo, cantidad);
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/caja";
    }

    @PostMapping("/restar")
    public String restar(@RequestParam String codigo, HttpSession session) {
        ventas.restar(carrito(session), codigo);
        return "redirect:/caja";
    }

    @PostMapping("/quitar")
    public String quitar(@RequestParam String codigo, HttpSession session) {
        ventas.quitar(carrito(session), codigo);
        return "redirect:/caja";
    }

    @PostMapping("/vaciar")
    public String vaciar(HttpSession session) {
        carrito(session).vaciar();
        return "redirect:/caja";
    }

    @PostMapping("/pagar")
    public String pagar(@RequestParam String metodo, @RequestAttribute("usuarioActual") Usuario cajero,
                        HttpSession session, RedirectAttributes ra) {
        try {
            Venta v = ventas.confirmar(carrito(session), metodo, cajero.getNombre());
            return "redirect:/caja/boleta/" + v.getId();
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/caja";
        }
    }

    @GetMapping("/boleta/{id}")
    public String boleta(@PathVariable Long id, Model model) {
        model.addAttribute("activo", "caja");
        model.addAttribute("venta", ventas.buscar(id));
        return "boleta";
    }
}

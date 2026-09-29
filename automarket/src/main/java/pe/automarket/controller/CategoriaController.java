package pe.automarket.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.automarket.exception.ReglaNegocioException;
import pe.automarket.model.Categoria;
import pe.automarket.service.InventarioService;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private final InventarioService inventario;

    public CategoriaController(InventarioService inventario) { this.inventario = inventario; }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("activo", "categorias");
        model.addAttribute("categorias", inventario.listarCategorias());
        return "categorias/listar";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        return formulario(model, new Categoria(), "Nueva Categoría", "/categorias/nueva", "Guardar");
    }

    @PostMapping("/nueva")
    public String crear(@ModelAttribute("categoria") Categoria categoria, Model model, RedirectAttributes ra) {
        try {
            inventario.crearCategoria(categoria);
            ra.addFlashAttribute("msg", "Categoría «" + categoria.getNombre() + "» creada.");
            return "redirect:/categorias";
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, categoria, "Nueva Categoría", "/categorias/nueva", "Guardar");
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, inventario.categoria(id), "Editar Categoría", "/categorias/" + id + "/editar", "Guardar cambios");
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable Long id, @ModelAttribute("categoria") Categoria categoria,
                             Model model, RedirectAttributes ra) {
        try {
            inventario.actualizarCategoria(id, categoria);
            ra.addFlashAttribute("msg", "Categoría actualizada.");
            return "redirect:/categorias";
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, categoria, "Editar Categoría", "/categorias/" + id + "/editar", "Guardar cambios");
        }
    }

    @GetMapping("/{id}/desactivar")
    public String confirmar(@PathVariable Long id, Model model) {
        Categoria c = inventario.categoria(id);
        model.addAttribute("activo", "categorias");
        model.addAttribute("titulo", "Confirmar desactivación");
        model.addAttribute("mensaje", "¿Desactivar la categoría «" + c.getNombre()
                + "»? Sus productos también quedarán desactivados y no podrán venderse (RN-01, RN-02).");
        model.addAttribute("accion", "/categorias/" + id + "/desactivar");
        model.addAttribute("cancelar", "/categorias");
        model.addAttribute("boton", "Desactivar");
        return "confirmar";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id, RedirectAttributes ra) {
        int afectados = inventario.desactivarCategoria(id);
        ra.addFlashAttribute("msg", "Categoría desactivada. Productos desactivados en cascada: " + afectados + ".");
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, RedirectAttributes ra) {
        inventario.activarCategoria(id);
        ra.addFlashAttribute("msg", "Categoría reactivada. Sus productos deben reactivarse uno a uno.");
        return "redirect:/categorias";
    }

    private String formulario(Model model, Categoria c, String titulo, String accion, String boton) {
        model.addAttribute("activo", "categorias");
        model.addAttribute("categoria", c);
        model.addAttribute("iconos", List.of("🥤", "🍫", "🥛", "🧴", "🍞", "🥩", "🍎", "📦"));
        model.addAttribute("titulo", titulo);
        model.addAttribute("accion", accion);
        model.addAttribute("boton", boton);
        return "categorias/form";
    }
}

package pe.automarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.automarket.exception.ReglaNegocioException;
import pe.automarket.model.Producto;
import pe.automarket.service.InventarioService;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final InventarioService inventario;

    public ProductoController(InventarioService inventario) { this.inventario = inventario; }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @RequestParam(required = false) Long categoria, Model model) {
        model.addAttribute("activo", "productos");
        model.addAttribute("productos", inventario.listarProductos(q, categoria));
        model.addAttribute("categorias", inventario.listarCategorias());
        model.addAttribute("categoriasMap", inventario.mapaCategorias());
        model.addAttribute("q", q);
        model.addAttribute("categoriaSel", categoria);
        return "productos/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        return formulario(model, new Producto(), "Nuevo Producto", "/productos/nuevo", "Guardar");
    }

    @PostMapping("/nuevo")
    public String crear(@ModelAttribute("producto") Producto producto, Model model, RedirectAttributes ra) {
        try {
            inventario.crearProducto(producto);
            ra.addFlashAttribute("msg", "Producto «" + producto.getNombre() + "» registrado.");
            return "redirect:/productos";
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, producto, "Nuevo Producto", "/productos/nuevo", "Guardar");
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, inventario.producto(id), "Editar Producto", "/productos/" + id + "/editar", "Guardar cambios");
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable Long id, @ModelAttribute("producto") Producto producto,
                             Model model, RedirectAttributes ra) {
        try {
            inventario.actualizarProducto(id, producto);
            ra.addFlashAttribute("msg", "Producto actualizado.");
            return "redirect:/productos";
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, producto, "Editar Producto", "/productos/" + id + "/editar", "Guardar cambios");
        }
    }

    @GetMapping("/{id}/desactivar")
    public String confirmar(@PathVariable Long id, Model model) {
        Producto p = inventario.producto(id);
        model.addAttribute("activo", "productos");
        model.addAttribute("titulo", "Confirmar desactivación");
        model.addAttribute("mensaje", "¿Desactivar «" + p.getNombre() + "»? Dejará de estar disponible en la caja.");
        model.addAttribute("accion", "/productos/" + id + "/desactivar");
        model.addAttribute("cancelar", "/productos");
        model.addAttribute("boton", "Desactivar");
        return "confirmar";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id, RedirectAttributes ra) {
        inventario.desactivarProducto(id);
        ra.addFlashAttribute("msg", "Producto desactivado.");
        return "redirect:/productos";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            inventario.activarProducto(id);
            ra.addFlashAttribute("msg", "Producto reactivado.");
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }

    private String formulario(Model model, Producto p, String titulo, String accion, String boton) {
        model.addAttribute("activo", "productos");
        model.addAttribute("producto", p);
        model.addAttribute("categorias", inventario.categoriasActivas());
        model.addAttribute("titulo", titulo);
        model.addAttribute("accion", accion);
        model.addAttribute("boton", boton);
        return "productos/form";
    }
}

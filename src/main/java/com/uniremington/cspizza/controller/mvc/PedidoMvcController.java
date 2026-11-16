package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.service.PedidoService;
import com.uniremington.cspizza.service.ProductoService;
import com.uniremington.cspizza.service.CategoriaService;
import com.uniremington.cspizza.model.vo.PedidoVO;
import com.uniremington.cspizza.model.vo.PedidoNuevoVO;
import com.uniremington.cspizza.model.vo.ProductoVO;
import com.uniremington.cspizza.model.vo.ClienteVO;
import com.uniremington.cspizza.service.exception.ConflictException;
import com.uniremington.cspizza.service.exception.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

/** Gestiona las vistas CRUD de pedidos bajo {@code /pedidos} y el punto de venta (POS). */
@Controller
@RequestMapping("/pedidos")
public class PedidoMvcController {

    private PedidoService pedidoService;
    private ProductoService productoService;
    private CategoriaService categoriaService;

    /** Constructor usado por Spring (inyección por constructor). */
    @Autowired
    public PedidoMvcController(PedidoService pedidoService, ProductoService productoService,
                               CategoriaService categoriaService) {
        this.pedidoService = pedidoService;
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    /** Constructor sin argumentos mantenido para pruebas unitarias existentes. */
    public PedidoMvcController() {
    }

    /** Setter mantenido para compatibilidad con pruebas unitarias existentes. */
    public void setPedidoService(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /** Setter mantenido para compatibilidad con pruebas unitarias existentes. */
    public void setProductoService(ProductoService productoService) {
        this.productoService = productoService;
    }

    /** Setter mantenido para compatibilidad con pruebas unitarias existentes. */
    public void setCategoriaService(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    /* ==================== CRUD existente ==================== */

    /**
     * Lista pedidos con filtros opcionales: estado, tipo de entrega, rango de fechas y búsqueda por cliente.
     * Usa JOIN FETCH para evitar problema N+1.
     */
    @GetMapping({"", "/"})
    public String listar(Model model) {
        model.addAttribute("pedidos", pedidoService.listarTodos());
        model.addAttribute("filtroEstado", null);
        model.addAttribute("filtroTipoEntrega", null);
        model.addAttribute("filtroFechaDesde", null);
        model.addAttribute("filtroFechaHasta", null);
        model.addAttribute("filtroBusca", null);
        return "pedidos/lista";
    }

    /**
     * Aplica filtros a la lista de pedidos y JOIN FETCH a detalles para evitar N+1.
     */
    public String aplicarFiltros(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipoEntrega,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) String busca,
            Model model) {
        LocalDateTime fd = (fechaDesde != null && !fechaDesde.isBlank())
                ? LocalDate.parse(fechaDesde).atStartOfDay() : null;
        LocalDateTime fh = (fechaHasta != null && !fechaHasta.isBlank())
                ? LocalDate.parse(fechaHasta).atTime(23, 59, 59) : null;

        model.addAttribute("pedidos", pedidoService.listarConFiltros(estado, tipoEntrega, fd, fh, busca));
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroTipoEntrega", tipoEntrega);
        model.addAttribute("filtroFechaDesde", fechaDesde);
        model.addAttribute("filtroFechaHasta", fechaHasta);
        model.addAttribute("filtroBusca", busca);
        return "pedidos/lista";
    }

    @GetMapping({"/editar/{id}", "/editar/{id}/"})
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return pedidoService.buscarPorId(id)
                .map(pedido -> {
                    model.addAttribute("pedido", pedido);
                    model.addAttribute("clientes", pedidoService.listarClientes());
                    model.addAttribute("usuarios", pedidoService.listarUsuarios());
                    model.addAttribute("productos", pedidoService.listarProductos());
                    return "pedidos/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "El pedido solicitado no existe.");
                    return "redirect:/pedidos";
                });
    }

    @PostMapping({"/guardar", "/guardar/"})
    public String guardar(@Valid @ModelAttribute("pedido") PedidoVO pedido, BindingResult result) {
        if (result.hasErrors()) {
            return "pedidos/form";
        }
        try {
            if (pedido.getIdPedido() == null) {
                pedidoService.guardar(pedido);
            } else {
                pedidoService.actualizar(pedido);
            }
        } catch (IllegalArgumentException | NotFoundException | ConflictException excepcion) {
            result.reject("pedido.error", excepcion.getMessage());
            return "pedidos/form";
        }
        return "redirect:/pedidos";
    }

    /**
     * Anula un pedido (estado CANCELADO) y repone el stock.
     * Reemplaza el borrado físico para la mayoría de casos.
     */
    @PostMapping({"/anular/{id}", "/anular/{id}/"})
    public String anular(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pedidoService.cambiarEstado(id, "CANCELADO");
            redirectAttributes.addFlashAttribute("success", "Pedido #" + id + " anulado. Stock repuesto.");
        } catch (NotFoundException | ConflictException excepcion) {
            redirectAttributes.addFlashAttribute("error", excepcion.getMessage());
        }
        return "redirect:/pedidos";
    }

    /**
     * Cambia el estado de un pedido con validación de transiciones.
     * Accessible solo para ADMIN y EMPLEADO.
     */
    @PostMapping({"/{id}/estado", "/{id}/estado/"})
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam String estado,
                                RedirectAttributes redirectAttributes) {
        try {
            pedidoService.cambiarEstado(id, estado);
            redirectAttributes.addFlashAttribute("success",
                    "Pedido #" + id + " ahora está: " + estado);
        } catch (NotFoundException | ConflictException excepcion) {
            redirectAttributes.addFlashAttribute("error", excepcion.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    @PostMapping({"/eliminar/{id}", "/eliminar/{id}/"})
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pedidoService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Pedido #" + id + " eliminado.");
        } catch (NotFoundException excepcion) {
            redirectAttributes.addFlashAttribute("error", excepcion.getMessage());
        }
        return "redirect:/pedidos";
    }

    /* ==================== Punto de venta (POS) ==================== */

    @GetMapping({"/nuevo", "/nuevo/"})
    public String nuevo(Model model) {
        model.addAttribute("pedidoNuevo", new PedidoNuevoVO());
        model.addAttribute("pedido", new PedidoVO());
        if (pedidoService != null) {
            model.addAttribute("clientes", pedidoService.listarClientes());
        }
        if (productoService != null) {
            model.addAttribute("productos", productoService.buscarProductosDisponibles());
            model.addAttribute("categorias", categoriaService.listarTodos());
        }
        return "pedidos/nuevo";
    }

    @PostMapping({"/nuevo", "/nuevo/"})
    public String guardarPedidoCompleto(
            @Valid @ModelAttribute("pedidoNuevo") PedidoNuevoVO entrada,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            modelAttributePos(model(redirectAttributes), entrada);
            return "pedidos/nuevo";
        }

        String nombreUsuario = authentication != null ? authentication.getName() : null;
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            result.reject("pedido.error", "No se pudo identificar el usuario autenticado.");
            modelAttributePos(model(redirectAttributes), entrada);
            return "pedidos/nuevo";
        }

        try {
            PedidoVO creado = pedidoService.crearConDetalles(entrada, nombreUsuario);
            redirectAttributes.addFlashAttribute("success",
                    "Pedido #" + creado.getIdPedido() + " registrado correctamente. Total: $"
                            + creado.getTotalPedido());
            return "redirect:/pedidos/" + creado.getIdPedido();
        } catch (IllegalArgumentException | NotFoundException | ConflictException excepcion) {
            result.reject("pedido.error", excepcion.getMessage());
            modelAttributePos(model(redirectAttributes), entrada);
            return "pedidos/nuevo";
        }
    }

    /**
     * Muestra el detalle de un pedido con sus productos, total, estado y botón de imprimir ticket.
     */
    @GetMapping({"/{id}", "/{id}/"})
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return pedidoService.buscarPorId(id)
                .map(pedido -> {
                    model.addAttribute("pedido", pedido);
                    model.addAttribute("detalles",
                            pedidoService.buscarDetallesPorPedido(id));
                    if (productoService != null) {
                        model.addAttribute("productosDisponibles",
                                productoService.buscarProductosDisponibles());
                    }
                    return "pedidos/detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "El pedido solicitado no existe.");
                    return "redirect:/pedidos";
                });
    }

    /**
     * Agrega productos a un pedido mientras esté PENDIENTE o EN_PREPARACION.
     */
    @PostMapping({"/{id}/agregar", "/{id}/agregar/"})
    public String agregarProductos(
            @PathVariable Long id,
            @Valid @ModelAttribute("pedidoNuevo") PedidoNuevoVO entrada,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            modelAttributePos(model(redirectAttributes), entrada);
            return "redirect:/pedidos/" + id;
        }

        String nombreUsuario = authentication != null ? authentication.getName() : null;
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            redirectAttributes.addFlashAttribute("error",
                    "No se pudo identificar el usuario autenticado.");
            return "redirect:/pedidos/" + id;
        }

        try {
            pedidoService.agregarProductos(id, entrada, nombreUsuario);
            redirectAttributes.addFlashAttribute("success", "Producto(s) agregado(s) al pedido #" + id);
        } catch (IllegalArgumentException | NotFoundException | ConflictException excepcion) {
            redirectAttributes.addFlashAttribute("error", excepcion.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    /* ==================== Helpers ==================== */

    private Model model(RedirectAttributes redirectAttributes) {
        return null; // no se usa directamente; los flash attributes van por redirectAttributes
    }

    private void modelAttributePos(Model model, PedidoNuevoVO entrada) {
        if (model != null) {
            if (!model.containsAttribute("clientes")) {
                model.addAttribute("clientes", pedidoService.listarClientes());
            }
            if (!model.containsAttribute("productos")) {
                model.addAttribute("productos", productoService.buscarProductosDisponibles());
            }
            model.addAttribute("pedidoNuevo", entrada);
        }
    }

    /**
     * Devuelve los detalles de un pedido para mostrar en la vista de detalle.
     */
    public List<com.uniremington.cspizza.model.vo.DetallePedidoVO> buscarDetallesPorPedido(Long idPedido) {
        return pedidoService.buscarDetallesPorPedido(idPedido);
    }
}

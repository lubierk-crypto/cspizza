package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.vo.PedidoVO;
import com.uniremington.cspizza.model.vo.ClienteVO;
import com.uniremington.cspizza.model.vo.UsuarioVO;
import com.uniremington.cspizza.model.vo.ProductoVO;
import com.uniremington.cspizza.model.vo.InventarioVO;
import com.uniremington.cspizza.model.vo.DetalleLineaVO;
import com.uniremington.cspizza.model.vo.PedidoNuevoVO;
import com.uniremington.cspizza.model.vo.ItemPedidoVO;
import com.uniremington.cspizza.model.entity.PedidoEntity;
import com.uniremington.cspizza.model.entity.DetallePedidoEntity;
import com.uniremington.cspizza.model.entity.ProductoEntity;
import com.uniremington.cspizza.model.entity.ClienteEntity;
import com.uniremington.cspizza.model.entity.UsuarioEntity;
import com.uniremington.cspizza.model.repository.ClienteRepository;
import com.uniremington.cspizza.model.repository.PedidoRepository;
import com.uniremington.cspizza.model.repository.UsuarioRepository;
import com.uniremington.cspizza.model.repository.DetallePedidoRepository;
import com.uniremington.cspizza.model.repository.ProductoRepository;
import com.uniremington.cspizza.model.repository.InventarioRepository;
import com.uniremington.cspizza.service.exception.ConflictException;
import com.uniremington.cspizza.service.exception.NotFoundException;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PedidoService extends AbstractCrudService<PedidoVO, PedidoEntity> {
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    public PedidoService(PedidoRepository repository, ClienteRepository clienteRepository, UsuarioRepository usuarioRepository,
            DetallePedidoRepository detallePedidoRepository, ProductoRepository productoRepository,
            InventarioRepository inventarioRepository) {
        super(repository);
        this.pedidoRepository = repository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.productoRepository = productoRepository;
        this.inventarioRepository = inventarioRepository;
    }

    @Override protected Long idOf(PedidoVO value) { return value.getIdPedido(); }
    @Override protected PedidoEntity map(PedidoVO value, PedidoEntity entity) {
        if (value == null) throw new IllegalArgumentException("El cuerpo de la solicitud es obligatorio.");
        if (entity == null) entity = new PedidoEntity();
        if (value.getIdCliente() == null) throw new IllegalArgumentException("idCliente es obligatorio.");
        entity.setCliente(clienteRepository.findById(value.getIdCliente()).orElseThrow(() -> new NotFoundException("No existe cliente " + value.getIdCliente() + ".")));
        if (value.getIdUsuario() == null) throw new IllegalArgumentException("idUsuario es obligatorio.");
        entity.setUsuario(usuarioRepository.findById(value.getIdUsuario()).orElseThrow(() -> new NotFoundException("No existe usuario " + value.getIdUsuario() + ".")));
        if (entity.getFechaPedido() == null) {
            entity.setFechaPedido(value.getFechaPedido() == null ? LocalDateTime.now() : value.getFechaPedido());
        }
        entity.setEstado(value.getEstado() == null || value.getEstado().isBlank() ? "PENDIENTE" : value.getEstado());
        if (entity.getTotalPedido()==null) entity.setTotalPedido(BigDecimal.ZERO);
        entity.setObservacion(value.getObservacion());
        entity.setTipoEntrega(value.getTipoEntrega());
        entity.setDireccionEntrega(value.getDireccionEntrega());
        return entity;
    }
    @Override protected PedidoVO view(PedidoEntity entity) {
        PedidoVO value = new PedidoVO();
        value.setIdPedido(entity.getIdPedido());
        value.setFechaPedido(entity.getFechaPedido());
        value.setEstado(entity.getEstado());
        value.setTotalPedido(entity.getTotalPedido());
        value.setObservacion(entity.getObservacion());
        value.setTipoEntrega(entity.getTipoEntrega());
        value.setDireccionEntrega(entity.getDireccionEntrega());
        value.setIdCliente(entity.getCliente().getIdCliente());
        value.setIdUsuario(entity.getUsuario().getIdUsuario());
        value.setClienteNombre(entity.getCliente().getNombre() + " " + entity.getCliente().getApellido());
        value.setUsuarioNombre(entity.getUsuario().getNombreUsuario());
        return value;
    }

    @Override
    public PedidoVO guardar(PedidoVO value) {
        PedidoVO saved = super.guardar(value);
        PedidoEntity pedido = pedidoRepository.findById(saved.getIdPedido())
                .orElseThrow(() -> new NotFoundException("No existe el pedido guardado."));
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleLineaVO linea : value.getDetalleLineas()) {
            if (linea.getIdProducto() == null || linea.getCantidad() == null || linea.getCantidad() <= 0) {
                continue;
            }
            ProductoVO producto = productoRepository.findById(linea.getIdProducto())
                    .map(p -> {
                        ProductoVO v = new ProductoVO();
                        v.setIdProducto(p.getIdProducto());
                        v.setNombre(p.getNombre());
                        v.setPrecioUnitario(p.getPrecioUnitario());
                        return v;
                    })
                    .orElseThrow(() -> new NotFoundException("No existe producto " + linea.getIdProducto() + "."));
            DetallePedidoEntity detalle = new DetallePedidoEntity();
            detalle.setPedido(pedido);
            detalle.setProducto(productoRepository.findById(linea.getIdProducto()).orElseThrow(() -> new NotFoundException("No existe producto " + linea.getIdProducto() + ".")));
            detalle.setCantidad(linea.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioUnitario());
            detalle.setSubtotal(producto.getPrecioUnitario().multiply(BigDecimal.valueOf(linea.getCantidad())));
            detalle.setObservacion(value.getObservacion());
            detallePedidoRepository.save(detalle);
            total = total.add(detalle.getSubtotal());
        }

        saved.setTotalPedido(total);
        return saved;
    }

    @Transactional(readOnly=true) public List<PedidoVO> buscarPorCliente(Long id) { return pedidoRepository.findAllByCliente_IdCliente(id).stream().map(this::view).toList(); }
    @Transactional(readOnly=true) public List<PedidoVO> buscarPorUsuario(Long id) { return pedidoRepository.findAllByUsuario_IdUsuario(id).stream().map(this::view).toList(); }
    @Transactional(readOnly=true) public List<PedidoVO> buscarPorEstado(String s) { return pedidoRepository.findAllByEstado(s).stream().map(this::view).toList(); }
    @Transactional(readOnly=true) public List<PedidoVO> buscarPorFechaPedidoEntre(LocalDateTime a, LocalDateTime b) { return pedidoRepository.findAllByFechaPedidoBetween(a, b).stream().map(this::view).toList(); }
    protected List<PedidoVO> filtered(Predicate<PedidoEntity> predicate) { return pedidoRepository.findAll().stream().filter(predicate).map(this::view).toList(); }

    @Transactional(readOnly = true)
    public List<ClienteVO> listarClientes() {
        return clienteRepository.findAll().stream()
                .map(c -> {
                    ClienteVO v = new ClienteVO();
                    v.setIdCliente(c.getIdCliente());
                    v.setNombre(c.getNombre());
                    v.setApellido(c.getApellido());
                    return v;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioVO> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(u -> {
                    UsuarioVO v = new UsuarioVO();
                    v.setIdUsuario(u.getIdUsuario());
                    v.setNombre(u.getNombre());
                    v.setNombreUsuario(u.getNombreUsuario());
                    return v;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoVO> listarProductos() {
        return productoRepository.findAll().stream()
                .map(p -> {
                    ProductoVO v = new ProductoVO();
                    v.setIdProducto(p.getIdProducto());
                    v.setNombre(p.getNombre());
                    v.setPrecioUnitario(p.getPrecioUnitario());
                    return v;
                })
                .toList();
    }

    /* ==================== KPIs del dashboard ==================== */

    @Transactional(readOnly = true)
    public Long pedidosHoy() {
        return pedidoRepository.countPedidosHoy();
    }

    @Transactional(readOnly = true)
    public BigDecimal ventasHoy() {
        return pedidoRepository.sumVentasHoy();
    }

    @Transactional(readOnly = true)
    public BigDecimal ticketPromedioHoy() {
        return pedidoRepository.avgTicketHoy();
    }

    @Transactional(readOnly = true)
    public List<PedidoVO> pedidosPendientes() {
        return pedidoRepository.findAllByEstadoPendienteOrderByFechaPedidoDesc().stream()
                .map(this::viewConDetalles)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoVO> pedidosEnPreparacion() {
        return pedidoRepository.findAllByEstadoEnPreparacionOrderByFechaPedidoDesc().stream()
                .map(this::viewConDetalles)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoVO> ultimosCincoPedidos() {
        return pedidoRepository.findAllOrderByFechaPedidoDesc().stream()
                .limit(5)
                .map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<InventarioVO> productosBajoStock() {
        return inventarioRepository.findAllBajoStock().stream()
                .map(e -> {
                    InventarioVO v = new InventarioVO();
                    v.setIdInventario(e.getIdInventario());
                    v.setIdProducto(e.getProducto().getIdProducto());
                    v.setCantidadActual(e.getCantidadActual());
                    v.setCantidadMinima(e.getCantidadMinima());
                    v.setCantidadMaxima(e.getCantidadMaxima());
                    v.setUbicacion(e.getUbicacion());
                    v.setEstado(e.getEstado());
                    v.setFechaActualizacion(e.getFechaActualizacion());
                    return v;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<com.uniremington.cspizza.model.vo.DetallePedidoVO> buscarDetallesPorPedido(Long idPedido) {
        return detallePedidoRepository.findAllByPedido_IdPedido(idPedido).stream()
                .map(d -> {
                    com.uniremington.cspizza.model.vo.DetallePedidoVO v = new com.uniremington.cspizza.model.vo.DetallePedidoVO();
                    v.setIdDetallePedido(d.getIdDetallePedido());
                    v.setCantidad(d.getCantidad());
                    v.setPrecioUnitario(d.getPrecioUnitario());
                    v.setSubtotal(d.getSubtotal());
                    v.setObservacion(d.getObservacion());
                    v.setIdPedido(d.getPedido().getIdPedido());
                    v.setIdProducto(d.getProducto().getIdProducto());
                    v.setNombreProducto(d.getProducto().getNombre());
                    return v;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoVO> pedidosListo() {
        return pedidoRepository.findAllByEstado("LISTO").stream()
                .map(this::viewConDetalles)
                .toList();
    }

    /**
     * Lista pedidos con filtros opcionales y JOIN FETCH a detalles para evitar N+1.
     * Incluye el resumen de productos en cada PedidoVO ({@code resumenProductos}).
     */
    @Transactional(readOnly = true)
    public List<PedidoVO> listarConFiltros(String estado, String tipoEntrega,
            LocalDateTime fechaDesde, LocalDateTime fechaHasta, String busca) {
        return pedidoRepository.findConFiltros(estado, tipoEntrega, fechaDesde, fechaHasta, busca)
                .stream()
                .map(this::viewConDetalles)
                .toList();
    }

    private PedidoVO viewConDetalles(PedidoEntity entity) {
        PedidoVO value = new PedidoVO();
        value.setIdPedido(entity.getIdPedido());
        value.setFechaPedido(entity.getFechaPedido());
        value.setEstado(entity.getEstado());
        value.setTotalPedido(entity.getTotalPedido());
        value.setObservacion(entity.getObservacion());
        value.setTipoEntrega(entity.getTipoEntrega());
        value.setDireccionEntrega(entity.getDireccionEntrega());
        value.setIdCliente(entity.getCliente().getIdCliente());
        value.setIdUsuario(entity.getUsuario().getIdUsuario());
        value.setClienteNombre(entity.getCliente().getNombre() + " " + entity.getCliente().getApellido());
        value.setUsuarioNombre(entity.getUsuario().getNombreUsuario());

        StringBuilder resumen = new StringBuilder();
        List<DetallePedidoEntity> detalles = entity.getDetalles();
        if (detalles != null) {
            for (int i = 0; i < detalles.size(); i++) {
                DetallePedidoEntity d = detalles.get(i);
                if (i > 0) resumen.append(", ");
                resumen.append(d.getCantidad()).append("× ").append(d.getProducto().getNombre());
            }
        }
        value.setResumenProductos(resumen.toString());
        return value;
    }

    /* ==================== Pedido completo desde el punto de venta ==================== */

    /**
     * Crea un pedido con uno o más productos en una sola transacción.
     * <ul>
     *   <li>Valida al menos un ítem.</li>
     *   <li>Valida que cada producto exista, esté ACTIVO y tenga stock suficiente.</li>
     *   <li>Toma el precio SIEMPRE de la base de datos (ignora precios enviados).</li>
     *   <li>Crea el pedido en estado PENDIENTE con sus detalles y calcula el total.</li>
     *   <li>Descuenta {@code productos.stock} y sincroniza {@code inventarios.cantidad_actual}.</li>
     *   <li>Si algo falla, no se guarda nada.</li>
     * </ul>
     *
     * @param entrada datos del pedido y sus líneas de producto
     * @param idUsuario nombre de usuario autenticado (resuelto contra la tabla usuarios)
     * @return el pedido creado con sus detalles
     * @throws IllegalArgumentException si no hay ítems o faltan datos obligatorios
     * @throws NotFoundException si el cliente, usuario o producto no existen
     * @throws ConflictException si el producto está inactivo o no hay stock suficiente
     */
    @Transactional
    public PedidoVO crearConDetalles(PedidoNuevoVO entrada, String idUsuario) {
        if (entrada == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es obligatorio.");
        }
        if (entrada.getItems() == null || entrada.getItems().isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un producto al pedido.");
        }

        // Resolver cliente: por nombre escrito (crea si no existe), por id, o "Cliente Mostrador" por defecto
        ClienteEntity cliente;
        String nombreCliente = entrada.getClienteNombre();
        if (nombreCliente != null && !nombreCliente.isBlank()) {
            final String nombreFinal = nombreCliente.trim();
            cliente = clienteRepository.findFirstByNombreIgnoreCase(nombreFinal).orElseGet(() -> {
                ClienteEntity nuevo = new ClienteEntity();
                nuevo.setNombre(nombreFinal);
                nuevo.setApellido("");
                nuevo.setEstado("ACTIVO");
                nuevo.setFechaRegistro(java.time.LocalDateTime.now());
                return clienteRepository.save(nuevo);
            });
        } else if (entrada.getIdCliente() != null) {
            Long idCliente = entrada.getIdCliente();
            cliente = clienteRepository.findById(idCliente)
                    .orElseThrow(() -> new NotFoundException("No existe cliente " + idCliente + "."));
        } else {
            cliente = clienteRepository.findByNombre("Cliente Mostrador")
                    .orElseThrow(() -> new NotFoundException("No existe el cliente predeterminado 'Cliente Mostrador'."));
        }

        // Resolver usuario autenticado contra la tabla de usuarios
        UsuarioEntity usuarioEntity = usuarioRepository.findFirstByNombreUsuario(idUsuario)
                .orElseThrow(() -> new NotFoundException("No existe el usuario " + idUsuario + "."));

        // Crear el pedido cabecera
        PedidoEntity pedido = new PedidoEntity();
        pedido.setCliente(cliente);
        pedido.setUsuario(usuarioEntity);
        pedido.setFechaPedido(java.time.LocalDateTime.now());
        pedido.setEstado("PENDIENTE");
        pedido.setTotalPedido(BigDecimal.ZERO);
        pedido.setObservacion(entrada.getObservacion());
        pedido.setTipoEntrega(entrada.getTipoEntrega());
        pedido.setDireccionEntrega(entrada.getDireccionEntrega());
        pedido = pedidoRepository.save(pedido);

        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedidoVO item : entrada.getItems()) {
            if (item.getIdProducto() == null || item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new IllegalArgumentException("Cada ítem debe tener producto y cantidad válidos.");
            }

            // Producto ACTIVO y con stock suficiente (precio siempre de la BD)
            ProductoEntity producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new NotFoundException("No existe producto " + item.getIdProducto() + "."));
            if (!"ACTIVO".equalsIgnoreCase(producto.getEstado())) {
                throw new ConflictException("El producto '" + producto.getNombre() + "' no está activo.");
            }
            if (producto.getStock() == null || producto.getStock() < item.getCantidad()) {
                throw new ConflictException("Stock insuficiente para '" + producto.getNombre() + "'. Disponible: "
                        + (producto.getStock() == null ? 0 : producto.getStock()));
            }

            BigDecimal precio = producto.getPrecioUnitario();
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(item.getCantidad()));

            DetallePedidoEntity detalle = new DetallePedidoEntity();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(precio);
            detalle.setSubtotal(subtotal);
            detalle.setObservacion(item.getObservacion());
            detallePedidoRepository.save(detalle);

            total = total.add(subtotal);

            // Descontar stock del producto (canónico)
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            // Sincronizar inventario con el stock canónico
            ProductoEntity productoFinal = producto;
            inventarioRepository.findAllByProducto_IdProducto(item.getIdProducto()).stream().findFirst()
                    .ifPresent(inv -> {
                        inv.setCantidadActual(productoFinal.getStock());
                        inv.setFechaActualizacion(java.time.LocalDateTime.now());
                        inventarioRepository.save(inv);
                    });
        }

        pedido.setTotalPedido(total);
        pedidoRepository.save(pedido);

        PedidoVO resultado = view(pedido);
        return resultado;
    }

    /**
     * Agrega uno o más productos a un pedido existente mientras esté PENDIENTE o EN_PREPARACION.
     * Recalcula el total, descuenta stock y sincroniza inventario.
     *
     * @param idPedido identificador del pedido
     * @param entrada  nuevas líneas de producto a agregar
     * @param idUsuario nombre de usuario autenticado
     * @return el pedido actualizado
     */
    @Transactional
    public PedidoVO agregarProductos(Long idPedido, PedidoNuevoVO entrada, String idUsuario) {
        if (entrada == null || entrada.getItems() == null || entrada.getItems().isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un producto.");
        }

        PedidoEntity pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new NotFoundException("No existe el pedido " + idPedido + "."));

        if (!"PENDIENTE".equals(pedido.getEstado()) && !"EN_PREPARACION".equals(pedido.getEstado())) {
            throw new ConflictException("Solo se pueden agregar productos a pedidos PENDIENTE o EN_PREPARACION.");
        }

        BigDecimal total = pedido.getTotalPedido() == null ? BigDecimal.ZERO : pedido.getTotalPedido();

        for (ItemPedidoVO item : entrada.getItems()) {
            if (item.getIdProducto() == null || item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new IllegalArgumentException("Cada ítem debe tener producto y cantidad válidos.");
            }

            ProductoEntity producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new NotFoundException("No existe producto " + item.getIdProducto() + "."));
            if (!"ACTIVO".equalsIgnoreCase(producto.getEstado())) {
                throw new ConflictException("El producto '" + producto.getNombre() + "' no está activo.");
            }
            if (producto.getStock() == null || producto.getStock() < item.getCantidad()) {
                throw new ConflictException("Stock insuficiente para '" + producto.getNombre() + "'. Disponible: "
                        + (producto.getStock() == null ? 0 : producto.getStock()));
            }

            BigDecimal precio = producto.getPrecioUnitario();
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(item.getCantidad()));

            DetallePedidoEntity detalle = new DetallePedidoEntity();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(precio);
            detalle.setSubtotal(subtotal);
            detalle.setObservacion(item.getObservacion());
            detallePedidoRepository.save(detalle);

            total = total.add(subtotal);

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            inventarioRepository.findAllByProducto_IdProducto(item.getIdProducto()).stream().findFirst()
                    .ifPresent(inv -> {
                        ProductoEntity productoFinalAgregar = producto;
                        inv.setCantidadActual(productoFinalAgregar.getStock());
                        inv.setFechaActualizacion(java.time.LocalDateTime.now());
                        inventarioRepository.save(inv);
                    });
        }

        pedido.setTotalPedido(total);
        pedidoRepository.save(pedido);

        return view(pedido);
    }

    /**
     * Cambia el estado de un pedido validando la transición permitida.
     * <p>
     * Flujo válido: PENDIENTE → EN_PREPARACION → LISTO → ENTREGADO.
     * CANCELADO solo desde PENDIENTE o EN_PREPARACION (reponiendo el stock).
     *
     * @param idPedido identificador del pedido
     * @param nuevoEstado estado destino
     * @return el pedido actualizado
     * @throws ConflictException si la transición no es válida
     */
    @Transactional
    public PedidoVO cambiarEstado(Long idPedido, String nuevoEstado) {
        PedidoEntity pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new NotFoundException("No existe el pedido " + idPedido + "."));

        String actual = pedido.getEstado();

        boolean valido = switch (actual) {
            case "PENDIENTE" -> nuevoEstado.equals("EN_PREPARACION")
                    || nuevoEstado.equals("CANCELADO");
            case "EN_PREPARACION" -> nuevoEstado.equals("LISTO")
                    || nuevoEstado.equals("CANCELADO");
            case "LISTO" -> nuevoEstado.equals("ENTREGADO");
            default -> false;
        };

        if (!valido) {
            throw new ConflictException("No se puede pasar el pedido de '" + actual + "' a '" + nuevoEstado + "'.");
        }

        if ("CANCELADO".equals(nuevoEstado)) {
            // Reponer stock de todos los detalles
            List<DetallePedidoEntity> detalles = detallePedidoRepository.findAllByPedido_IdPedido(idPedido);
            for (DetallePedidoEntity d : detalles) {
                ProductoEntity p = d.getProducto();
                p.setStock(p.getStock() + d.getCantidad());
                productoRepository.save(p);
                inventarioRepository.findAllByProducto_IdProducto(p.getIdProducto()).stream().findFirst()
                        .ifPresent(inv -> {
                            inv.setCantidadActual(p.getStock());
                            inv.setFechaActualizacion(java.time.LocalDateTime.now());
                            inventarioRepository.save(inv);
                        });
            }
        }

        pedido.setEstado(nuevoEstado);
        pedidoRepository.save(pedido);
        return view(pedido);
    }
}

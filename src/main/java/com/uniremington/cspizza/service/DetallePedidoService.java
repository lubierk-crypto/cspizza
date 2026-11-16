package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.vo.DetallePedidoVO;
import com.uniremington.cspizza.model.vo.ProductoVO;
import com.uniremington.cspizza.model.vo.PedidoVO;
import com.uniremington.cspizza.model.entity.DetallePedidoEntity;
import com.uniremington.cspizza.model.entity.PedidoEntity;
import com.uniremington.cspizza.model.repository.DetallePedidoRepository;
import com.uniremington.cspizza.model.repository.PedidoRepository;
import com.uniremington.cspizza.model.repository.ProductoRepository;
import com.uniremington.cspizza.service.exception.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DetallePedidoService extends AbstractCrudService<DetallePedidoVO, DetallePedidoEntity> {
    private final DetallePedidoRepository detallePedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    public DetallePedidoService(DetallePedidoRepository repository, PedidoRepository pedidoRepository, ProductoRepository productoRepository) {
        super(repository);
        this.detallePedidoRepository = repository;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    @Override protected Long idOf(DetallePedidoVO value) { return value.getIdDetallePedido(); }
    @Override protected DetallePedidoEntity map(DetallePedidoVO value, DetallePedidoEntity entity) {
        if (value == null) throw new IllegalArgumentException("El cuerpo de la solicitud es obligatorio.");
        if (entity == null) entity = new DetallePedidoEntity();
        if (value.getIdPedido() == null) throw new IllegalArgumentException("idPedido es obligatorio.");
        entity.setPedido(pedidoRepository.findById(value.getIdPedido()).orElseThrow(() -> new NotFoundException("No existe pedido " + value.getIdPedido() + ".")));
        if (value.getIdProducto() == null) throw new IllegalArgumentException("idProducto es obligatorio.");
        entity.setProducto(productoRepository.findById(value.getIdProducto()).orElseThrow(() -> new NotFoundException("No existe producto " + value.getIdProducto() + ".")));
        if(value.getCantidad()==null||value.getCantidad()<=0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        entity.setCantidad(value.getCantidad());
        entity.setPrecioUnitario(entity.getProducto().getPrecioUnitario());
        entity.setSubtotal(entity.getProducto().getPrecioUnitario().multiply(BigDecimal.valueOf(value.getCantidad())));
        entity.setObservacion(value.getObservacion());
        return entity;
    }
    @Override protected DetallePedidoVO view(DetallePedidoEntity entity) {
        DetallePedidoVO value = new DetallePedidoVO();
        value.setIdDetallePedido(entity.getIdDetallePedido());
        value.setCantidad(entity.getCantidad());
        value.setPrecioUnitario(entity.getPrecioUnitario());
        value.setSubtotal(entity.getSubtotal());
        value.setObservacion(entity.getObservacion());
        value.setIdPedido(entity.getPedido().getIdPedido());
        value.setIdProducto(entity.getProducto().getIdProducto());
        value.setNombreProducto(entity.getProducto().getNombre());
        return value;
    }
    @Override public DetallePedidoVO guardar(DetallePedidoVO v) { DetallePedidoVO saved=super.guardar(v); recalcular(saved.getIdPedido()); return saved; }
    @Override public DetallePedidoVO actualizar(DetallePedidoVO v) { Long old=detallePedidoRepository.findById(v.getIdDetallePedido()).map(e -> e.getPedido().getIdPedido()).orElseThrow(() -> new NotFoundException("No existe el detalle solicitado.")); DetallePedidoVO saved=super.actualizar(v); recalcular(old); if(!Objects.equals(old,saved.getIdPedido())) recalcular(saved.getIdPedido()); return saved; }
    @Override public void eliminar(Long id) { Long pedido=detallePedidoRepository.findById(id).map(e -> e.getPedido().getIdPedido()).orElseThrow(() -> new NotFoundException("No existe el detalle solicitado.")); super.eliminar(id); recalcular(pedido); }
    private void recalcular(Long id) {
        PedidoEntity pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el pedido solicitado."));
        BigDecimal total = detallePedidoRepository.findAllByPedido_IdPedido(id).stream()
                .map(DetallePedidoEntity::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setTotalPedido(total);
        pedidoRepository.save(pedido);
    }
    @Transactional(readOnly=true) public List<DetallePedidoVO> buscarPorPedido(Long id) { return detallePedidoRepository.findAllByPedido_IdPedido(id).stream().map(this::view).toList(); }
    @Transactional(readOnly=true) public List<DetallePedidoVO> buscarPorProducto(Long id) { return detallePedidoRepository.findAllByProducto_IdProducto(id).stream().map(this::view).toList(); }
    @Transactional(readOnly=true) public List<DetallePedidoVO> buscarPorPedidoYProducto(Long p, Long pr) { return detallePedidoRepository.findAllByPedido_IdPedidoAndProducto_IdProducto(p, pr).stream().map(this::view).toList(); }
    protected List<DetallePedidoVO> filtered(Predicate<DetallePedidoEntity> predicate) { return detallePedidoRepository.findAll().stream().filter(predicate).map(this::view).toList(); }

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

    @Transactional(readOnly = true)
    public List<PedidoVO> listarPedidos() {
        return pedidoRepository.findAll().stream()
                .map(p -> {
                    PedidoVO v = new PedidoVO();
                    v.setIdPedido(p.getIdPedido());
                    v.setEstado(p.getEstado());
                    v.setTotalPedido(p.getTotalPedido());
                    return v;
                })
                .toList();
    }
}

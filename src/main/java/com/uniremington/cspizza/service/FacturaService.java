package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.vo.FacturaVO;
import com.uniremington.cspizza.model.entity.FacturaEntity;
import com.uniremington.cspizza.model.entity.PedidoEntity;
import com.uniremington.cspizza.model.repository.FacturaRepository;
import com.uniremington.cspizza.model.repository.PedidoRepository;
import com.uniremington.cspizza.service.exception.ConflictException;
import com.uniremington.cspizza.service.exception.NotFoundException;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FacturaService extends AbstractCrudService<FacturaVO, FacturaEntity> {
    private final FacturaRepository facturaRepository;
    private final PedidoRepository pedidoRepository;
    @Value("${factura.iva.porcentaje:19}")
    private BigDecimal ivaPorcentaje;
    public FacturaService(FacturaRepository repository, PedidoRepository pedidoRepository) {
        super(repository);
        this.facturaRepository = repository;
        this.pedidoRepository = pedidoRepository;
    }

    @Override protected Long idOf(FacturaVO value) { return value.getIdFactura(); }
    @Override protected FacturaEntity map(FacturaVO value, FacturaEntity entity) {
        if (value == null) throw new IllegalArgumentException("El cuerpo de la solicitud es obligatorio.");
        if (entity == null) entity = new FacturaEntity();
        if (value.getIdPedido() == null) throw new IllegalArgumentException("idPedido es obligatorio.");
        entity.setPedido(pedidoRepository.findById(value.getIdPedido()).orElseThrow(() -> new NotFoundException("No existe pedido " + value.getIdPedido() + ".")));
        if (value.getNumeroFactura() == null || value.getNumeroFactura().isBlank()) {
            throw new IllegalArgumentException("El número de factura es obligatorio.");
        }
        String invoiceNumber = value.getNumeroFactura().trim();
        Long currentId = entity.getIdFactura();
        boolean duplicate = facturaRepository.findFirstByNumeroFactura(invoiceNumber)
                .filter(f -> !Objects.equals(f.getIdFactura(), currentId)).isPresent();
        if (duplicate) throw new ConflictException("El número de factura ya está registrado.");
        entity.setNumeroFactura(invoiceNumber);
        if (entity.getFechaEmision() == null) {
            entity.setFechaEmision(value.getFechaEmision() == null ? LocalDateTime.now() : value.getFechaEmision());
        }
        entity.setSubtotal(entity.getPedido().getTotalPedido()==null?new BigDecimal("0.00"):entity.getPedido().getTotalPedido());
        if(value.getIva()!=null && value.getIva().signum()<0) throw new IllegalArgumentException("El IVA no puede ser negativo.");
        entity.setIva(value.getIva()==null?new BigDecimal("0.00"):value.getIva());
        entity.setTotal(entity.getSubtotal().add(entity.getIva()));
        entity.setEstado(value.getEstado() == null || value.getEstado().isBlank() ? "EMITIDA" : value.getEstado());
        return entity;
    }
    @Override protected FacturaVO view(FacturaEntity entity) {
        FacturaVO value = new FacturaVO();
        value.setIdFactura(entity.getIdFactura());
        value.setNumeroFactura(entity.getNumeroFactura());
        value.setFechaEmision(entity.getFechaEmision());
        value.setSubtotal(entity.getSubtotal());
        value.setIva(entity.getIva());
        value.setTotal(entity.getTotal());
        value.setEstado(entity.getEstado());
        value.setIdPedido(entity.getPedido().getIdPedido());
        return value;
    }
    @Transactional(readOnly=true) public Optional<FacturaVO> buscarPorNumeroFactura(String s) { return facturaRepository.findFirstByNumeroFactura(s).map(this::view); }
    @Transactional(readOnly=true) public Optional<FacturaVO> buscarPorPedido(Long id) { return facturaRepository.findFirstByPedido_IdPedido(id).map(this::view); }
    @Transactional(readOnly=true) public List<FacturaVO> buscarPorEstado(String s) { return facturaRepository.findAllByEstado(s).stream().map(this::view).toList(); }
    @Transactional(readOnly=true) public List<FacturaVO> buscarPorFechaEmisionEntre(LocalDateTime a, LocalDateTime b) { return facturaRepository.findAllByFechaEmisionBetween(a, b).stream().map(this::view).toList(); }
    protected List<FacturaVO> filtered(Predicate<FacturaEntity> predicate) { return facturaRepository.findAll().stream().filter(predicate).map(this::view).toList(); }

    /**
     * Genera una factura a partir de un pedido existente.
     * <ul>
     *   <li>Valida que el pedido exista.</li>
     *   <li>Valida que el pedido no tenga ya una factura asociada.</li>
     *   <li>Genera el número de factura de forma consecutiva (FAC-000001, FAC-000002, ...).</li>
     *   <li>Calcula subtotal, IVA y total usando el porcentaje configurado en {@code factura.iva.porcentaje}.</li>
     *   <li>El estado inicial es EMITIDA.</li>
     * </ul>
     *
     * @param idPedido identificador del pedido
     * @return la factura creada
     * @throws NotFoundException si el pedido no existe
     * @throws ConflictException si el pedido ya tiene factura
     */
    @Transactional
    public FacturaVO generarDesdePedido(Long idPedido) {
        PedidoEntity pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new NotFoundException("No existe pedido " + idPedido + "."));

        Optional<FacturaEntity> existente = facturaRepository.findFirstByPedido_IdPedido(idPedido);
        if (existente.isPresent()) {
            throw new ConflictException("El pedido " + idPedido + " ya tiene factura " + existente.get().getNumeroFactura() + ".");
        }

        // Generar número consecutivo: FAC-000001, FAC-000002, ...
        String ultimoNumero = facturaRepository.findTopByOrderByIdFacturaDesc()
                .map(FacturaEntity::getNumeroFactura)
                .orElse(null);
        Long siguienteNumero = 1L;
        if (ultimoNumero != null) {
            try {
                siguienteNumero = Long.parseLong(ultimoNumero.replaceAll("^FAC-0*", "")) + 1L;
            } catch (NumberFormatException e) {
                siguienteNumero = 1L;
            }
        }
        String numeroFactura = String.format("FAC-%06d", siguienteNumero);

        BigDecimal subtotal = pedido.getTotalPedido() == null ? new BigDecimal("0.00") : pedido.getTotalPedido();
        BigDecimal iva = subtotal.multiply(ivaPorcentaje).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(iva);

        FacturaEntity factura = new FacturaEntity();
        factura.setPedido(pedido);
        factura.setNumeroFactura(numeroFactura);
        factura.setFechaEmision(LocalDateTime.now());
        factura.setSubtotal(subtotal);
        factura.setIva(iva);
        factura.setTotal(total);
        factura.setEstado("EMITIDA");
        factura = facturaRepository.save(factura);

        return view(factura);
    }
}

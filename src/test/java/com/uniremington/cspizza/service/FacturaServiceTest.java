package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.entity.FacturaEntity;
import com.uniremington.cspizza.model.entity.PedidoEntity;
import com.uniremington.cspizza.model.repository.FacturaRepository;
import com.uniremington.cspizza.model.repository.PedidoRepository;
import com.uniremington.cspizza.model.vo.FacturaVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FacturaServiceTest {

    @Mock private FacturaRepository facturaRepository;
    @Mock private PedidoRepository pedidoRepository;
    @InjectMocks private FacturaService facturaService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        java.lang.reflect.Field f = FacturaService.class.getDeclaredField("ivaPorcentaje");
        f.setAccessible(true);
        f.set(facturaService, new BigDecimal("19"));
    }

    @Test
    void generarDesdePedidoValidoCreaFacturaEmitida() {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setIdPedido(1L);
        pedido.setTotalPedido(new BigDecimal("100.00"));

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.findFirstByPedido_IdPedido(1L)).thenReturn(Optional.empty());
        when(facturaRepository.findTopByOrderByIdFacturaDesc()).thenReturn(Optional.empty());
        when(facturaRepository.save(any(FacturaEntity.class))).thenAnswer(inv -> {
            FacturaEntity e = inv.getArgument(0);
            e.setIdFactura(10L);
            return e;
        });

        FacturaVO resultado = facturaService.generarDesdePedido(1L);

        assertNotNull(resultado);
        assertEquals("EMITIDA", resultado.getEstado());
        assertEquals(new BigDecimal("100.00"), resultado.getSubtotal());
        assertEquals(new BigDecimal("19.00"), resultado.getIva());
        assertEquals(new BigDecimal("119.00"), resultado.getTotal());
        verify(facturaRepository).save(any(FacturaEntity.class));
    }

    @Test
    void generarDesdePedidoInexistenteLanzaNotFound() {
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(com.uniremington.cspizza.service.exception.NotFoundException.class,
                () -> facturaService.generarDesdePedido(999L));
    }

    @Test
    void generarDesdePedidoYaFacturadoLanzaConflict() {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setIdPedido(1L);
        pedido.setTotalPedido(new BigDecimal("100.00"));

        FacturaEntity existente = new FacturaEntity();
        existente.setIdFactura(5L);
        existente.setNumeroFactura("FAC-000005");

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.findFirstByPedido_IdPedido(1L)).thenReturn(Optional.of(existente));

        assertThrows(com.uniremington.cspizza.service.exception.ConflictException.class,
                () -> facturaService.generarDesdePedido(1L));
    }

    @Test
    void generarDesdePedidoConTotalNuloUsaCero() {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setIdPedido(2L);
        pedido.setTotalPedido(null);

        when(pedidoRepository.findById(2L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.findFirstByPedido_IdPedido(2L)).thenReturn(Optional.empty());
        when(facturaRepository.findTopByOrderByIdFacturaDesc()).thenReturn(Optional.empty());
        when(facturaRepository.save(any(FacturaEntity.class))).thenAnswer(inv -> {
            FacturaEntity e = inv.getArgument(0);
            e.setIdFactura(11L);
            return e;
        });

        FacturaVO resultado = facturaService.generarDesdePedido(2L);

        assertEquals(new BigDecimal("0.00"), resultado.getSubtotal());
        assertEquals(new BigDecimal("0.00"), resultado.getTotal());
    }

    @Test
    void generarDesdePedidoNumeroConsecutivo() {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setIdPedido(3L);
        pedido.setTotalPedido(new BigDecimal("50.00"));

        FacturaEntity ultima = new FacturaEntity();
        ultima.setIdFactura(5L);
        ultima.setNumeroFactura("FAC-000005");

        when(pedidoRepository.findById(3L)).thenReturn(Optional.of(pedido));
        when(facturaRepository.findFirstByPedido_IdPedido(3L)).thenReturn(Optional.empty());
        when(facturaRepository.findTopByOrderByIdFacturaDesc()).thenReturn(Optional.of(ultima));
        when(facturaRepository.save(any(FacturaEntity.class))).thenAnswer(inv -> {
            FacturaEntity e = inv.getArgument(0);
            e.setIdFactura(6L);
            return e;
        });

        FacturaVO resultado = facturaService.generarDesdePedido(3L);

        assertEquals("FAC-000006", resultado.getNumeroFactura());
    }

    @Test
    void buscarPorIdInexistenteRetornaVacio() {
        when(facturaRepository.findById(999L)).thenReturn(Optional.empty());
        assertTrue(facturaService.buscarPorId(999L).isEmpty());
    }
}

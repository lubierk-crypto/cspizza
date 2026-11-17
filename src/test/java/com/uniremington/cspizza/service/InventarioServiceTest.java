package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.entity.InventarioEntity;
import com.uniremington.cspizza.model.entity.ProductoEntity;
import com.uniremington.cspizza.model.repository.InventarioRepository;
import com.uniremington.cspizza.model.repository.ProductoRepository;
import com.uniremington.cspizza.model.vo.InventarioVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventarioServiceTest {

    @Mock private InventarioRepository inventarioRepository;
    @Mock private ProductoRepository productoRepository;
    @InjectMocks private InventarioService inventarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void guardarInventarioValidoInicializaCantidadConStockDelProducto() {
        InventarioVO entrada = new InventarioVO();
        entrada.setIdProducto(1L);
        entrada.setCantidadMinima(5);
        entrada.setCantidadMaxima(50);
        entrada.setUbicacion("BODEGA-A");
        entrada.setEstado("ACTIVO");

        ProductoEntity producto = new ProductoEntity();
        producto.setIdProducto(1L);
        producto.setNombre("Pizza Margarita");
        producto.setStock(20);

        InventarioEntity guardado = new InventarioEntity();
        guardado.setIdInventario(10L);
        guardado.setProducto(producto);
        guardado.setCantidadActual(20);
        guardado.setCantidadMinima(5);
        guardado.setCantidadMaxima(50);
        guardado.setUbicacion("BODEGA-A");
        guardado.setEstado("ACTIVO");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(inventarioRepository.findAllByProducto_IdProducto(1L)).thenReturn(List.of());
        when(inventarioRepository.save(any(InventarioEntity.class))).thenReturn(guardado);

        InventarioVO resultado = inventarioService.guardar(entrada);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getIdInventario());
        assertEquals(20, resultado.getCantidadActual());
        assertEquals("BODEGA-A", resultado.getUbicacion());
        verify(inventarioRepository).save(any(InventarioEntity.class));
    }

    @Test
    void guardarInventarioSinProductoLanzaExcepcion() {
        InventarioVO entrada = new InventarioVO();
        entrada.setCantidadMinima(1);
        entrada.setCantidadMaxima(20);
        assertThrows(IllegalArgumentException.class, () -> inventarioService.guardar(entrada));
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void guardarInventarioConCantidadMaximaMenorQueMinimaLanzaExcepcion() {
        InventarioVO entrada = new InventarioVO();
        entrada.setIdProducto(1L);
        entrada.setCantidadMinima(10);
        entrada.setCantidadMaxima(5);
        assertThrows(IllegalArgumentException.class, () -> inventarioService.guardar(entrada));
    }

    @Test
    void guardarInventarioConCantidadActualNegativaLanzaExcepcion() {
        InventarioVO entrada = new InventarioVO();
        entrada.setIdProducto(1L);
        entrada.setCantidadActual(-1);
        entrada.setCantidadMinima(1);
        entrada.setCantidadMaxima(20);
        assertThrows(IllegalArgumentException.class, () -> inventarioService.guardar(entrada));
    }

    @Test
    void guardarInventarioProductoInexistenteLanzaNotFound() {
        InventarioVO entrada = new InventarioVO();
        entrada.setIdProducto(999L);
        entrada.setCantidadMinima(1);
        entrada.setCantidadMaxima(20);
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(com.uniremington.cspizza.service.exception.NotFoundException.class,
                () -> inventarioService.guardar(entrada));
    }

    @Test
    void guardarInventarioDuplicadoLanzaConflict() {
        InventarioVO entrada = new InventarioVO();
        entrada.setIdProducto(1L);
        entrada.setCantidadMinima(1);
        entrada.setCantidadMaxima(20);

        ProductoEntity producto = new ProductoEntity();
        producto.setIdProducto(1L);
        producto.setStock(10);

        InventarioEntity existente = new InventarioEntity();
        existente.setIdInventario(5L);
        existente.setProducto(producto);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(inventarioRepository.findAllByProducto_IdProducto(1L)).thenReturn(List.of(existente));

        assertThrows(com.uniremington.cspizza.service.exception.ConflictException.class,
                () -> inventarioService.guardar(entrada));
    }

    @Test
    void buscarPorIdInexistenteRetornaVacio() {
        when(inventarioRepository.findById(999L)).thenReturn(Optional.empty());
        assertTrue(inventarioService.buscarPorId(999L).isEmpty());
    }

    @Test
    void eliminarInventarioInexistenteLanzaNotFound() {
        when(inventarioRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(com.uniremington.cspizza.service.exception.NotFoundException.class,
                () -> inventarioService.eliminar(999L));
    }
}

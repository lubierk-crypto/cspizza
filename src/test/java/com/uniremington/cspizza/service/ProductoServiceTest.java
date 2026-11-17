package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.entity.ProductoEntity;
import com.uniremington.cspizza.model.repository.ProductoRepository;
import com.uniremington.cspizza.model.repository.CategoriaRepository;
import com.uniremington.cspizza.model.repository.InventarioRepository;
import com.uniremington.cspizza.model.vo.ProductoVO;
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

class ProductoServiceTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private InventarioRepository inventarioRepository;
    @InjectMocks private ProductoService productoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void guardarProductoValidoRetornaProductoConId() {
        ProductoVO entrada = new ProductoVO();
        entrada.setIdCategoria(1L);
        entrada.setNombre("Pizza Pepperoni");
        entrada.setPrecioUnitario(new BigDecimal("30000"));
        entrada.setStock(18);
        entrada.setEstado("ACTIVO");

        ProductoEntity guardada = new ProductoEntity();
        guardada.setIdProducto(2L);
        guardada.setNombre("Pizza Pepperoni");
        guardada.setPrecioUnitario(new BigDecimal("30000"));
        guardada.setStock(18);
        guardada.setEstado("ACTIVO");
        com.uniremington.cspizza.model.entity.CategoriaEntity catG = new com.uniremington.cspizza.model.entity.CategoriaEntity();
        catG.setIdCategoria(1L);
        guardada.setCategoria(catG);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(new com.uniremington.cspizza.model.entity.CategoriaEntity() {{ setIdCategoria(1L); }}));
        when(inventarioRepository.findAllByProducto_IdProducto(2L)).thenReturn(List.of());
        when(inventarioRepository.save(any(com.uniremington.cspizza.model.entity.InventarioEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productoRepository.save(any(ProductoEntity.class))).thenReturn(guardada);

        ProductoVO resultado = productoService.guardar(entrada);

        assertNotNull(resultado);
        assertEquals(2L, resultado.getIdProducto());
        assertEquals("Pizza Pepperoni", resultado.getNombre());
        verify(productoRepository).save(any(ProductoEntity.class));
    }

    @Test
    void guardarProductoSinNombreLanzaExcepcion() {
        ProductoVO entrada = new ProductoVO();
        entrada.setIdCategoria(1L);
        entrada.setNombre("");
        entrada.setPrecioUnitario(new BigDecimal("10000"));
        entrada.setStock(5);
        assertThrows(IllegalArgumentException.class, () -> productoService.guardar(entrada));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void guardarProductoSinCategoriaLanzaExcepcion() {
        ProductoVO entrada = new ProductoVO();
        entrada.setNombre("Pizza");
        entrada.setPrecioUnitario(new BigDecimal("10000"));
        entrada.setStock(5);
        assertThrows(IllegalArgumentException.class, () -> productoService.guardar(entrada));
    }

    @Test
    void guardarProductoNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> productoService.guardar(null));
    }

    @Test
    void buscarPorIdExistenteRetornaProducto() {
        ProductoEntity entity = new ProductoEntity();
        entity.setIdProducto(1L);
        entity.setNombre("Pizza Margarita");
        entity.setPrecioUnitario(new BigDecimal("26000"));
        entity.setStock(20);
        entity.setEstado("ACTIVO");
        com.uniremington.cspizza.model.entity.CategoriaEntity cat = new com.uniremington.cspizza.model.entity.CategoriaEntity();
        cat.setIdCategoria(1L);
        entity.setCategoria(cat);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(entity));

        var resultado = productoService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Pizza Margarita", resultado.get().getNombre());
    }

    @Test
    void buscarPorIdInexistenteRetornaVacio() {
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());
        assertTrue(productoService.buscarPorId(999L).isEmpty());
    }

    @Test
    void eliminarProductoInexistenteLanzaNotFound() {
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(com.uniremington.cspizza.service.exception.NotFoundException.class,
                () -> productoService.eliminar(999L));
    }

    @Test
    void actualizarProductoValidaActualizaCampos() {
        ProductoVO entrada = new ProductoVO();
        entrada.setIdProducto(1L);
        entrada.setIdCategoria(1L);
        entrada.setNombre("Pizza Margarita Premium");
        entrada.setPrecioUnitario(new BigDecimal("28000"));
        entrada.setStock(15);
        entrada.setEstado("ACTIVO");

        ProductoEntity existente = new ProductoEntity();
        existente.setIdProducto(1L);
        existente.setNombre("Pizza Margarita");
        existente.setPrecioUnitario(new BigDecimal("26000"));
        existente.setStock(20);
        existente.setEstado("ACTIVO");
        com.uniremington.cspizza.model.entity.CategoriaEntity catEx = new com.uniremington.cspizza.model.entity.CategoriaEntity();
        catEx.setIdCategoria(1L);
        existente.setCategoria(catEx);

        ProductoEntity actualizada = new ProductoEntity();
        actualizada.setIdProducto(1L);
        actualizada.setNombre("Pizza Margarita Premium");
        actualizada.setPrecioUnitario(new BigDecimal("28000"));
        actualizada.setStock(15);
        actualizada.setEstado("ACTIVO");
        com.uniremington.cspizza.model.entity.CategoriaEntity catAc = new com.uniremington.cspizza.model.entity.CategoriaEntity();
        catAc.setIdCategoria(1L);
        actualizada.setCategoria(catAc);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(catEx));
        when(inventarioRepository.findAllByProducto_IdProducto(1L)).thenReturn(List.of());
        when(inventarioRepository.save(any(com.uniremington.cspizza.model.entity.InventarioEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(ProductoEntity.class))).thenReturn(actualizada);

        ProductoVO resultado = productoService.actualizar(entrada);

        assertEquals("Pizza Margarita Premium", resultado.getNombre());
        verify(productoRepository).save(any(ProductoEntity.class));
    }
}

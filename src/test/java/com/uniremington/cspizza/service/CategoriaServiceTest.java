package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.entity.CategoriaEntity;
import com.uniremington.cspizza.model.repository.CategoriaRepository;
import com.uniremington.cspizza.model.vo.CategoriaVO;
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

class CategoriaServiceTest {

    @Mock private CategoriaRepository categoriaRepository;
    @InjectMocks private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void guardarCategoriaValidaRetornaCategoriaConId() {
        CategoriaVO entrada = new CategoriaVO();
        entrada.setNombre("Bebidas");
        entrada.setDescripcion("Bebidas frias");
        entrada.setEstado("ACTIVO");

        CategoriaEntity guardada = new CategoriaEntity();
        guardada.setIdCategoria(10L);
        guardada.setNombre("Bebidas");
        guardada.setDescripcion("Bebidas frias");
        guardada.setEstado("ACTIVO");

        when(categoriaRepository.save(any(CategoriaEntity.class))).thenReturn(guardada);

        CategoriaVO resultado = categoriaService.guardar(entrada);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getIdCategoria());
        assertEquals("Bebidas", resultado.getNombre());
        assertEquals("ACTIVO", resultado.getEstado());
        verify(categoriaRepository).save(any(CategoriaEntity.class));
    }

    @Test
    void guardarCategoriaSinNombreLanzaExcepcion() {
        CategoriaVO entrada = new CategoriaVO();
        entrada.setNombre("");
        entrada.setEstado("ACTIVO");

        assertThrows(IllegalArgumentException.class, () -> categoriaService.guardar(entrada));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void guardarCategoriaNulaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> categoriaService.guardar(null));
    }

    @Test
    void buscarPorIdExistenteRetornaCategoria() {
        CategoriaEntity entity = new CategoriaEntity();
        entity.setIdCategoria(5L);
        entity.setNombre("Pizzas");
        entity.setEstado("ACTIVO");

        when(categoriaRepository.findById(5L)).thenReturn(Optional.of(entity));

        var resultado = categoriaService.buscarPorId(5L);

        assertTrue(resultado.isPresent());
        assertEquals("Pizzas", resultado.get().getNombre());
    }

    @Test
    void buscarPorIdInexistenteRetornaVacio() {
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        var resultado = categoriaService.buscarPorId(999L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void eliminarCategoriaInexistenteLanzaNotFound() {
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(com.uniremington.cspizza.service.exception.NotFoundException.class,
                () -> categoriaService.eliminar(999L));
    }

    @Test
    void actualizarCategoriaValidaActualizaCampos() {
        CategoriaVO entrada = new CategoriaVO();
        entrada.setIdCategoria(5L);
        entrada.setNombre("Pizzas Premium");
        entrada.setEstado("ACTIVO");

        CategoriaEntity existente = new CategoriaEntity();
        existente.setIdCategoria(5L);
        existente.setNombre("Pizzas");
        existente.setEstado("ACTIVO");

        CategoriaEntity actualizada = new CategoriaEntity();
        actualizada.setIdCategoria(5L);
        actualizada.setNombre("Pizzas Premium");
        actualizada.setEstado("ACTIVO");

        when(categoriaRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.save(any(CategoriaEntity.class))).thenReturn(actualizada);

        CategoriaVO resultado = categoriaService.actualizar(entrada);

        assertEquals("Pizzas Premium", resultado.getNombre());
        verify(categoriaRepository).save(any(CategoriaEntity.class));
    }
}

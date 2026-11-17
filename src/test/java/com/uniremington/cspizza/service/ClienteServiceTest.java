package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.entity.ClienteEntity;
import com.uniremington.cspizza.model.repository.ClienteRepository;
import com.uniremington.cspizza.model.vo.ClienteVO;
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

class ClienteServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @InjectMocks private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void guardarClienteValidoRetornaClienteConId() {
        ClienteVO entrada = new ClienteVO();
        entrada.setNombre("Ana");
        entrada.setApellido("Garcia");
        entrada.setTelefono("3001112233");
        entrada.setCorreo("ana@correo.com");
        entrada.setEstado("ACTIVO");

        ClienteEntity guardada = new ClienteEntity();
        guardada.setIdCliente(1L);
        guardada.setNombre("Ana");
        guardada.setApellido("Garcia");
        guardada.setEstado("ACTIVO");

        when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(guardada);

        ClienteVO resultado = clienteService.guardar(entrada);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdCliente());
        assertEquals("Ana", resultado.getNombre());
        verify(clienteRepository).save(any(ClienteEntity.class));
    }

    @Test
    void guardarClienteSinNombreLanzaExcepcion() {
        ClienteVO entrada = new ClienteVO();
        entrada.setNombre("");
        entrada.setApellido("Garcia");
        assertThrows(IllegalArgumentException.class, () -> clienteService.guardar(entrada));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void guardarClienteSinApellidoLanzaExcepcion() {
        ClienteVO entrada = new ClienteVO();
        entrada.setNombre("Ana");
        entrada.setApellido("");
        assertThrows(IllegalArgumentException.class, () -> clienteService.guardar(entrada));
    }

    @Test
    void guardarClienteNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> clienteService.guardar(null));
    }

    @Test
    void buscarPorIdExistenteRetornaCliente() {
        ClienteEntity entity = new ClienteEntity();
        entity.setIdCliente(2L);
        entity.setNombre("Luis");
        entity.setApellido("Perez");
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(entity));

        var resultado = clienteService.buscarPorId(2L);

        assertTrue(resultado.isPresent());
        assertEquals("Luis", resultado.get().getNombre());
    }

    @Test
    void buscarPorIdInexistenteRetornaVacio() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());
        assertTrue(clienteService.buscarPorId(999L).isEmpty());
    }

    @Test
    void eliminarClienteInexistenteLanzaNotFound() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(com.uniremington.cspizza.service.exception.NotFoundException.class,
                () -> clienteService.eliminar(999L));
    }

    @Test
    void actualizarClienteValidaActualizaCampos() {
        ClienteVO entrada = new ClienteVO();
        entrada.setIdCliente(1L);
        entrada.setNombre("Ana Maria");
        entrada.setApellido("Garcia");

        ClienteEntity existente = new ClienteEntity();
        existente.setIdCliente(1L);
        existente.setNombre("Ana");
        existente.setApellido("Garcia");

        ClienteEntity actualizada = new ClienteEntity();
        actualizada.setIdCliente(1L);
        actualizada.setNombre("Ana Maria");
        actualizada.setApellido("Garcia");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(actualizada);

        ClienteVO resultado = clienteService.actualizar(entrada);

        assertEquals("Ana Maria", resultado.getNombre());
        verify(clienteRepository).save(any(ClienteEntity.class));
    }
}

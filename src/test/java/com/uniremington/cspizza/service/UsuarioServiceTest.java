package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.entity.UsuarioEntity;
import com.uniremington.cspizza.model.repository.UsuarioRepository;
import com.uniremington.cspizza.model.vo.UsuarioVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void guardarUsuarioValidoCodificaPasswordYRetornaUsuario() {
        UsuarioVO entrada = new UsuarioVO();
        entrada.setNombreUsuario("nuevo_user");
        entrada.setNombre("Nuevo");
        entrada.setApellido("Usuario");
        entrada.setRol("EMPLEADO");
        entrada.setCorreo("nuevo@example.test");
        entrada.setPassword("clave123");

        when(usuarioRepository.findFirstByNombreUsuario("nuevo_user")).thenReturn(Optional.empty());
        when(usuarioRepository.findFirstByCorreoIgnoreCase("nuevo@example.test")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("clave123")).thenReturn("$2a$10$hash");
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(inv -> {
            UsuarioEntity e = inv.getArgument(0);
            e.setIdUsuario(50L);
            return e;
        });

        UsuarioVO resultado = usuarioService.guardar(entrada);

        assertNotNull(resultado);
        assertEquals(50L, resultado.getIdUsuario());
        assertEquals("nuevo_user", resultado.getNombreUsuario());
        assertEquals("EMPLEADO", resultado.getRol());
        verify(passwordEncoder).encode("clave123");
        verify(usuarioRepository).save(any(UsuarioEntity.class));
    }

    @Test
    void guardarUsuarioSinNombreUsuarioLanzaExcepcion() {
        UsuarioVO entrada = new UsuarioVO();
        entrada.setNombreUsuario("");
        entrada.setNombre("Nuevo");
        entrada.setApellido("Usuario");
        entrada.setRol("EMPLEADO");
        entrada.setCorreo("nuevo@example.test");
        entrada.setPassword("clave123");

        assertThrows(IllegalArgumentException.class, () -> usuarioService.guardar(entrada));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void guardarUsuarioConRolInvalidoLanzaExcepcion() {
        UsuarioVO entrada = new UsuarioVO();
        entrada.setNombreUsuario("nuevo_user");
        entrada.setNombre("Nuevo");
        entrada.setApellido("Usuario");
        entrada.setRol("SUPERADMIN");
        entrada.setCorreo("nuevo@example.test");
        entrada.setPassword("clave123");

        when(usuarioRepository.findFirstByNombreUsuario("nuevo_user")).thenReturn(Optional.empty());
        when(usuarioRepository.findFirstByCorreoIgnoreCase("nuevo@example.test")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> usuarioService.guardar(entrada));
    }

    @Test
    void guardarUsuarioUsernameDuplicadoLanzaConflict() {
        UsuarioVO entrada = new UsuarioVO();
        entrada.setNombreUsuario("admin");
        entrada.setNombre("Admin");
        entrada.setApellido("Dup");
        entrada.setRol("ADMIN");
        entrada.setCorreo("admin2@example.test");
        entrada.setPassword("clave123");

        UsuarioEntity existente = new UsuarioEntity();
        existente.setIdUsuario(1L);
        existente.setNombreUsuario("admin");
        when(usuarioRepository.findFirstByNombreUsuario("admin")).thenReturn(Optional.of(existente));

        assertThrows(com.uniremington.cspizza.service.exception.ConflictException.class,
                () -> usuarioService.guardar(entrada));
    }

    @Test
    void guardarUsuarioSinPasswordLanzaExcepcion() {
        UsuarioVO entrada = new UsuarioVO();
        entrada.setNombreUsuario("nuevo_user");
        entrada.setNombre("Nuevo");
        entrada.setApellido("Usuario");
        entrada.setRol("EMPLEADO");
        entrada.setCorreo("nuevo@example.test");
        entrada.setPassword("");

        when(usuarioRepository.findFirstByNombreUsuario("nuevo_user")).thenReturn(Optional.empty());
        when(usuarioRepository.findFirstByCorreoIgnoreCase("nuevo@example.test")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> usuarioService.guardar(entrada));
    }

    @Test
    void guardarUsuarioNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> usuarioService.guardar(null));
    }

    @Test
    void buscarPorIdInexistenteRetornaVacio() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());
        assertTrue(usuarioService.buscarPorId(999L).isEmpty());
    }

    @Test
    void eliminarUsuarioInexistenteLanzaNotFound() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(com.uniremington.cspizza.service.exception.NotFoundException.class,
                () -> usuarioService.eliminar(999L));
    }
}

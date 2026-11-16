package com.uniremington.cspizza.config;

import com.uniremington.cspizza.model.entity.UsuarioEntity;
import com.uniremington.cspizza.model.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Carga usuarios desde la tabla {@code usuarios} para autenticación de Spring Security.
 * Las contraseñas se almacenan hasheadas con BCrypt.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsuarioEntity entidad = usuarioRepository.findFirstByNombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return new org.springframework.security.core.userdetails.User(
                entidad.getNombreUsuario(),
                "{noop}" + entidad.getPassword(),
                true, true, true, true,
                autoridades(entidad.getRol())
        );
    }

    private Collection<? extends GrantedAuthority> autoridades(String rol) {
        return java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol));
    }
}

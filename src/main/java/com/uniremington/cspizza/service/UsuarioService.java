package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.entity.UsuarioEntity;
import com.uniremington.cspizza.model.repository.UsuarioRepository;
import com.uniremington.cspizza.model.vo.UsuarioVO;
import com.uniremington.cspizza.service.exception.ConflictException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UsuarioService extends AbstractCrudService<UsuarioVO, UsuarioEntity> {
    private static final Set<String> ROLES_VALIDOS = Set.of("ADMIN", "GERENTE", "EMPLEADO");
    private static final String BCRYPT_PATTERN = "^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        super(usuarioRepository);
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected Long idOf(UsuarioVO value) {
        return value.getIdUsuario();
    }

    @Override
    protected UsuarioEntity map(UsuarioVO value, UsuarioEntity entity) {
        if (value == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es obligatorio.");
        }
        if (entity == null) {
            entity = new UsuarioEntity();
        }

        String username = text(value.getNombreUsuario(), "El nombre de usuario es obligatorio.");
        String email = text(value.getCorreo(), "El correo es obligatorio.");
        Long currentId = entity.getIdUsuario();
        Optional<UsuarioEntity> usernameOwner = usuarioRepository.findFirstByNombreUsuario(username);
        if (usernameOwner.filter(user -> !Objects.equals(user.getIdUsuario(), currentId)).isPresent()) {
            throw new ConflictException("El nombre de usuario ya está registrado.");
        }
        Optional<UsuarioEntity> emailOwner = usuarioRepository.findFirstByCorreoIgnoreCase(email);
        if (emailOwner.filter(user -> !Objects.equals(user.getIdUsuario(), currentId)).isPresent()) {
            throw new ConflictException("El correo ya está registrado.");
        }

        String role = text(value.getRol(), "El rol es obligatorio.").toUpperCase(Locale.ROOT);
        if (!ROLES_VALIDOS.contains(role)) {
            throw new IllegalArgumentException("El rol debe ser ADMIN, GERENTE o EMPLEADO.");
        }

        entity.setNombreUsuario(username);
        entity.setNombre(text(value.getNombre(), "El nombre es obligatorio."));
        entity.setApellido(text(value.getApellido(), "El apellido es obligatorio."));
        entity.setRol(role);
        entity.setCorreo(email);
        actualizarPassword(entity, value.getPassword());
        entity.setEstado(value.getEstado() == null || value.getEstado().isBlank()
                ? "ACTIVO" : value.getEstado());
        if (entity.getFechaRegistro() == null) {
            entity.setFechaRegistro(LocalDateTime.now());
        }
        return entity;
    }

    private void actualizarPassword(UsuarioEntity entity, String password) {
        if (password != null && !password.isBlank()) {
            entity.setPassword(password.matches(BCRYPT_PATTERN) ? password : passwordEncoder.encode(password));
        }
        if (entity.getPassword() == null || entity.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
    }

    @Override
    protected UsuarioVO view(UsuarioEntity entity) {
        UsuarioVO value = new UsuarioVO();
        value.setIdUsuario(entity.getIdUsuario());
        value.setNombreUsuario(entity.getNombreUsuario());
        value.setNombre(entity.getNombre());
        value.setApellido(entity.getApellido());
        value.setRol(entity.getRol());
        value.setCorreo(entity.getCorreo());
        value.setPassword(entity.getPassword());
        value.setEstado(entity.getEstado());
        value.setFechaRegistro(entity.getFechaRegistro());
        return value;
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioVO> buscarPorNombreUsuario(String username) {
        return usuarioRepository.findFirstByNombreUsuario(username).map(this::view);
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioVO> buscarPorCorreo(String email) {
        return usuarioRepository.findFirstByCorreoIgnoreCase(email).map(this::view);
    }

    @Transactional(readOnly = true)
    public List<UsuarioVO> buscarPorRol(String role) {
        return usuarioRepository.findAllByRol(role).stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioVO> buscarPorEstado(String state) {
        return usuarioRepository.findAllByEstado(state).stream().map(this::view).toList();
    }

    private String text(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}

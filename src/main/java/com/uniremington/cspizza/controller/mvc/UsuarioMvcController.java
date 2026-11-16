package com.uniremington.cspizza.controller.mvc;

import com.uniremington.cspizza.service.UsuarioService;
import com.uniremington.cspizza.model.vo.UsuarioVO;
import com.uniremington.cspizza.service.exception.ConflictException;
import com.uniremington.cspizza.service.exception.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Gestiona las vistas CRUD de usuarios bajo {@code /usuarios}. */
@Controller
@RequestMapping("/usuarios")
public class UsuarioMvcController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsuarioMvcController.class);
    private static final Set<String> ROLES_VALIDOS = Set.of("ADMIN", "GERENTE", "EMPLEADO");

    private UsuarioService usuarioService;

    @Autowired
    public void setUsuarioService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping({"", "/"})
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping({"/nuevo", "/nuevo/"})
    public String nuevo(Model model) {
        model.addAttribute("usuario", new UsuarioVO());
        return "usuarios/form";
    }

    @PostMapping({"/guardar", "/guardar/"})
    public String guardar(@Valid @ModelAttribute("usuario") UsuarioVO usuario, BindingResult result) {
        usuario.setNombreUsuario(trim(usuario.getNombreUsuario()));
        usuario.setNombre(trim(usuario.getNombre()));
        usuario.setApellido(trim(usuario.getApellido()));
        usuario.setCorreo(trim(usuario.getCorreo()));
        if (usuario.getRol() != null) {
            usuario.setRol(usuario.getRol().trim().toUpperCase(Locale.ROOT));
        }

        if (StringUtils.hasText(usuario.getRol()) && !ROLES_VALIDOS.contains(usuario.getRol())) {
            result.rejectValue("rol", "usuario.rol.invalido", "Selecciona ADMIN, GERENTE o EMPLEADO.");
        }

        boolean esNuevo = usuario.getIdUsuario() == null;
        if (esNuevo && !StringUtils.hasText(usuario.getPassword())) {
            result.rejectValue("password", "usuario.password.obligatoria", "La contraseña es obligatoria.");
        }

        UsuarioVO usuarioActual = null;
        if (!esNuevo) {
            Optional<UsuarioVO> encontrado = usuarioService.buscarPorId(usuario.getIdUsuario());
            if (encontrado.isEmpty()) {
                result.reject("usuario.no.encontrado", "El usuario que intentas editar ya no existe.");
            } else {
                usuarioActual = encontrado.get();
                if (!StringUtils.hasText(usuario.getPassword())) {
                    usuario.setPassword(usuarioActual.getPassword());
                }
                LocalDateTime fechaRegistro = usuarioActual.getFechaRegistro();
                if (usuario.getFechaRegistro() == null) {
                    usuario.setFechaRegistro(fechaRegistro);
                }
            }
        }

        validarUnicidad(usuario, result);
        if (result.hasErrors()) {
            return "usuarios/form";
        }

        try {
            if (esNuevo) {
                usuarioService.guardar(usuario);
            } else {
                usuarioService.actualizar(usuario);
            }
        } catch (DuplicateKeyException exception) {
            result.reject("usuario.duplicado",
                    "Ya existe un usuario o correo registrado con esos datos. Verifica ambos campos.");
            return "usuarios/form";
        } catch (ConflictException exception) {
            result.reject("usuario.duplicado", exception.getMessage());
            return "usuarios/form";
        } catch (IllegalArgumentException exception) {
            result.reject("usuario.error", exception.getMessage());
            return "usuarios/form";
        } catch (DataIntegrityViolationException exception) {
            LOGGER.error("La base de datos rechazó el guardado de un usuario por integridad de datos.", exception);
            result.reject("usuario.integridad",
                    "No se pudo guardar el usuario. Revisa los datos obligatorios y sus restricciones.");
            return "usuarios/form";
        }
        return "redirect:/usuarios";
    }

    private void validarUnicidad(UsuarioVO usuario, BindingResult result) {
        if (StringUtils.hasText(usuario.getNombreUsuario())) {
            usuarioService.buscarPorNombreUsuario(usuario.getNombreUsuario())
                    .filter(encontrado -> !Objects.equals(encontrado.getIdUsuario(), usuario.getIdUsuario()))
                    .ifPresent(encontrado -> result.rejectValue("nombreUsuario", "usuario.duplicado",
                            "Ese nombre de usuario ya está registrado."));
        }
        if (StringUtils.hasText(usuario.getCorreo())) {
            usuarioService.buscarPorCorreo(usuario.getCorreo())
                    .filter(encontrado -> !Objects.equals(encontrado.getIdUsuario(), usuario.getIdUsuario()))
                    .ifPresent(encontrado -> result.rejectValue("correo", "usuario.correo.duplicado",
                            "Ese correo ya está registrado."));
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    @GetMapping({"/editar/{id}", "/editar/{id}/"})
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return usuarioService.buscarPorId(id)
                .map(usuario -> {
                    model.addAttribute("usuario", usuario);
                    return "usuarios/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "El usuario solicitado no existe.");
                    return "redirect:/usuarios";
                });
    }

    @PostMapping({"/eliminar/{id}", "/eliminar/{id}/"})
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminar(id);
        } catch (NotFoundException excepcion) {
            redirectAttributes.addFlashAttribute("error", excepcion.getMessage());
        }
        return "redirect:/usuarios";
    }
}

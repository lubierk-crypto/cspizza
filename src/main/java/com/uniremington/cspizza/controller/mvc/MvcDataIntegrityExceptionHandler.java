package com.uniremington.cspizza.controller.mvc;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import com.uniremington.cspizza.service.exception.ConflictException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Presenta un mensaje entendible cuando una operacion viola una relacion de la base de datos
 * o una regla de negocio, en lugar de mostrar una pagina de error del servidor.
 */
@ControllerAdvice(basePackages = "com.uniremington.cspizza.controller.mvc")
public class MvcDataIntegrityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MvcDataIntegrityExceptionHandler.class);
    private static final Pattern DELETE_PATH =
            Pattern.compile("^/(categorias|clientes|detalle-pedidos|facturas|inventarios|pedidos|productos|usuarios)/eliminar/[^/]+/?$");

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Object manejarIntegridadReferencial(DataIntegrityViolationException exception,
                                               HttpServletRequest request,
                                               RedirectAttributes redirectAttributes) {
        Matcher matcher = DELETE_PATH.matcher(request.getServletPath());
        if (matcher.matches()) {
            LOGGER.warn("No se pudo completar el borrado en {}", request.getServletPath(), exception);
            redirectAttributes.addFlashAttribute("error",
                    "No se puede borrar este registro porque está relacionado con otros datos. "
                            + "Elimina primero los registros asociados.");
            return "redirect:/" + matcher.group(1);
        }

        LOGGER.error("Error de integridad de datos en {}", request.getServletPath(), exception);
        redirectAttributes.addFlashAttribute("error",
                "No se pudo guardar la información debido a una restricción de la base de datos.");
        return "redirect:/" + recursoDe(request);
    }

    @ExceptionHandler(ConflictException.class)
    public Object manejarConflictoNegocio(ConflictException exception, HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        Matcher matcher = DELETE_PATH.matcher(request.getServletPath());
        if (matcher.matches()) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/" + matcher.group(1);
        }
        LOGGER.warn("No se pudo completar la operación en {}", request.getServletPath(), exception);
        redirectAttributes.addFlashAttribute("error", exception.getMessage());
        return "redirect:/" + recursoDe(request);
    }

    /** Extrae el modulo de la ruta solicitada, por ejemplo {@code productos} en {@code /productos/guardar}. */
    private String recursoDe(HttpServletRequest request) {
        String[] segmentos = request.getServletPath().split("/");
        return segmentos.length > 1 && !segmentos[1].isBlank() ? segmentos[1] : "dashboard";
    }
}

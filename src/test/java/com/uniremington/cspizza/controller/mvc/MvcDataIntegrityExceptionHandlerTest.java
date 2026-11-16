package com.uniremington.cspizza.controller.mvc;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MvcDataIntegrityExceptionHandlerTest {

    @Test
    void redirigeAlListadoCuandoUnaRelacionImpideBorrar() {
        MvcDataIntegrityExceptionHandler handler = new MvcDataIntegrityExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/productos/eliminar/42");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String result = (String) handler.manejarIntegridadReferencial(
                new DataIntegrityViolationException("foreign key"), request, redirectAttributes);

        assertEquals("redirect:/productos", result);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("error"));
    }

    @Test
    void redirigeAlListadoCuandoLaRutaDeBorradoTieneBarraFinal() {
        MvcDataIntegrityExceptionHandler handler = new MvcDataIntegrityExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/usuarios/eliminar/42/");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String result = (String) handler.manejarIntegridadReferencial(
                new DataIntegrityViolationException("foreign key"), request, redirectAttributes);

        assertEquals("redirect:/usuarios", result);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("error"));
    }
}

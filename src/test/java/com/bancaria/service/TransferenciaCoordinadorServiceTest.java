
package com.bancaria.service;

import com.bancaria.dto.ResultadoTransferencia;
import com.bancaria.dto.TransferenciaRequest;
import com.bancaria.dto.TransferenciaResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransferenciaCoordinadorServiceTest {

    @Mock
    private TransferenciaService transferenciaService;

    @Mock
    private TransferenciaIdempotenciaService idempotenciaService;

    private TransferenciaCoordinadorService coordinadorService;

    @BeforeEach
    void configurar() {
        MockitoAnnotations.openMocks(this);

        coordinadorService = new TransferenciaCoordinadorService(
                transferenciaService,
                idempotenciaService
        );
    }

    @Test
    void recuperaTransferenciaCuandoExisteColisionDeClave() {

        TransferenciaRequest request = new TransferenciaRequest();
        request.setCuentaOrigenId(1L);
        request.setCuentaDestinoId(2L);
        request.setImporte(new BigDecimal("10.00"));
        request.setClaveIdempotencia("TRF-TEST-COLISION-001");
        request.setDescripcion("Prueba de colision");

        TransferenciaResponse respuesta = new TransferenciaResponse();
        ResultadoTransferencia recuperada =
                new ResultadoTransferencia(respuesta, false);

        when(idempotenciaService.buscarExistente(request))
                .thenReturn(
                        Optional.empty(),
                        Optional.of(recuperada)
                );

        when(transferenciaService.realizarTransferencia(request))
                .thenThrow(
                        new DataIntegrityViolationException(
                                "Duplicate entry"
                        )
                );

        ResultadoTransferencia resultado =
                coordinadorService.realizarTransferencia(request);

        assertSame(recuperada, resultado);
        assertFalse(resultado.isCreada());

        verify(idempotenciaService, times(2))
                .buscarExistente(request);

        verify(transferenciaService, times(1))
                .realizarTransferencia(request);
    }

    @Test
    void propagaErrorCuandoNoExisteTransferenciaRecuperable() {

        TransferenciaRequest request = new TransferenciaRequest();
        request.setCuentaOrigenId(1L);
        request.setCuentaDestinoId(2L);
        request.setImporte(new BigDecimal("10.00"));
        request.setClaveIdempotencia("TRF-TEST-COLISION-002");

        when(idempotenciaService.buscarExistente(request))
                .thenReturn(Optional.empty());

        when(transferenciaService.realizarTransferencia(request))
                .thenThrow(
                        new DataIntegrityViolationException(
                                "Error de integridad"
                        )
                );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> coordinadorService.realizarTransferencia(request)
        );

        verify(idempotenciaService, times(2))
                .buscarExistente(request);
    }
}

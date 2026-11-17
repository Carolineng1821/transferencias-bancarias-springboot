
package com.bancaria.service;

import com.bancaria.dto.TransferenciaRequest;
import com.bancaria.entity.Cuenta;
import com.bancaria.repository.CuentaRepository;
import com.bancaria.repository.TransferenciaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private TransferenciaRepository transferenciaRepository;

    @InjectMocks
    private TransferenciaService transferenciaService;

    private Cuenta origen;
    private Cuenta destino;
    private TransferenciaRequest request;

    @BeforeEach
    void prepararDatos() {


        origen = new Cuenta();
        ReflectionTestUtils.setField(origen, "id", 1L);
        origen.setSaldo(new BigDecimal("1000.00"));

        destino = new Cuenta();
        ReflectionTestUtils.setField(destino, "id", 2L);
        destino.setSaldo(new BigDecimal("500.00"));


        request = new TransferenciaRequest();
        request.setCuentaOrigenId(1L);
        request.setCuentaDestinoId(2L);
        request.setImporte(new BigDecimal("200.00"));
        request.setClaveIdempotencia("TRF-TEST-ROLLBACK-001");
        request.setDescripcion("Prueba de rollback");
    }

    @Test
    void debePropagarErrorCuandoFallaGuardarTransferencia() {

        when(transferenciaRepository.findByClaveIdempotencia(
                "TRF-TEST-ROLLBACK-001"
        )).thenReturn(Optional.empty());

        when(cuentaRepository.buscarPorIdConBloqueo(1L))
                .thenReturn(Optional.of(origen));

        when(cuentaRepository.buscarPorIdConBloqueo(2L))
                .thenReturn(Optional.of(destino));

        when(transferenciaRepository.saveAndFlush(any()))
                .thenThrow(new RuntimeException("Error simulado de BD"));

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> transferenciaService.realizarTransferencia(request)
        );

        assertEquals("Error simulado de BD", error.getMessage());

        verify(cuentaRepository).save(origen);
        verify(cuentaRepository).save(destino);

        verify(transferenciaRepository).saveAndFlush(any());
    }
}


package com.bancaria.service;

import com.bancaria.dto.TransferenciaRequest;
import com.bancaria.entity.Cuenta;
import com.bancaria.repository.CuentaRepository;
import com.bancaria.repository.TransferenciaRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
class TransferenciaRollbackIntegrationTest {

    @Autowired
    private TransferenciaService transferenciaService;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private TransferenciaRepository transferenciaRepository;

    @SpyBean
    private TransferenciaRepository transferenciaRepositorySpy;

    @Test
    void debeRevertirSaldosCuandoFallaGuardarTransferencia() {

        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta("ROLLBACK-ORIGEN-001");
        origen.setTitular("Cuenta prueba origen");
        origen.setSaldo(new BigDecimal("1000.00"));
        origen = cuentaRepository.saveAndFlush(origen);

        Cuenta destino = new Cuenta();
        destino.setNumeroCuenta("ROLLBACK-DESTINO-001");
        destino.setTitular("Cuenta prueba destino");
        destino.setSaldo(new BigDecimal("500.00"));
        destino = cuentaRepository.saveAndFlush(destino);

        Long origenId = origen.getId();
        Long destinoId = destino.getId();

        TransferenciaRequest request = new TransferenciaRequest();
        request.setCuentaOrigenId(origenId);
        request.setCuentaDestinoId(destinoId);
        request.setImporte(new BigDecimal("200.00"));
        request.setClaveIdempotencia("ROLLBACK-TEST-001");
        request.setDescripcion("Prueba de rollback real");

        doThrow(new RuntimeException("Fallo simulado"))
                .when(transferenciaRepositorySpy)
                .saveAndFlush(any());

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> transferenciaService.realizarTransferencia(request)
        );

        assertEquals("Fallo simulado", error.getMessage());

        Cuenta origenFinal = cuentaRepository.findById(origenId)
                .orElseThrow(AssertionError::new);

        Cuenta destinoFinal = cuentaRepository.findById(destinoId)
                .orElseThrow(AssertionError::new);

        assertEquals(
                0,
                new BigDecimal("1000.00")
                        .compareTo(origenFinal.getSaldo())
        );

        assertEquals(
                0,
                new BigDecimal("500.00")
                        .compareTo(destinoFinal.getSaldo())
        );

        assertFalse(
                transferenciaRepository.existsByClaveIdempotencia(
                        "ROLLBACK-TEST-001"
                )
        );
    }
}

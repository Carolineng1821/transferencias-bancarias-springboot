
package com.bancaria.service;

import com.bancaria.entity.Cuenta;
import com.bancaria.entity.Transferencia;
import com.bancaria.repository.CuentaRepository;
import com.bancaria.repository.TransferenciaRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TransferenciaUniqueIntegrationTest {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private TransferenciaRepository transferenciaRepository;

    @Test
    void rechazaDosTransferenciasConLaMismaClave() {

        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta("T" + UUID.randomUUID().toString().substring(0, 9));
        origen.setTitular("Cuenta origen");
        origen.setSaldo(new BigDecimal("1000.00"));

        Cuenta destino = new Cuenta();
        destino.setNumeroCuenta("T" + UUID.randomUUID().toString().substring(0, 9));
        destino.setTitular("Cuenta destino");
        destino.setSaldo(new BigDecimal("500.00"));

        origen = cuentaRepository.saveAndFlush(origen);
        destino = cuentaRepository.saveAndFlush(destino);

        String clave = "UNIQUE-" + UUID.randomUUID();

        Transferencia primera = crearTransferencia(
                origen, destino, clave
        );

        transferenciaRepository.saveAndFlush(primera);

        Transferencia segunda = crearTransferencia(
                origen, destino, clave
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> transferenciaRepository.saveAndFlush(segunda)
        );

        long registros = transferenciaRepository.findAll()
                .stream()
                .filter(t -> clave.equals(t.getClaveIdempotencia()))
                .count();

        assertEquals(1, registros);
    }

    private Transferencia crearTransferencia(
            Cuenta origen,
            Cuenta destino,
            String clave) {

        Transferencia transferencia = new Transferencia();

        transferencia.setCuentaOrigen(origen);
        transferencia.setCuentaDestino(destino);
        transferencia.setImporte(new BigDecimal("10.00"));
        transferencia.setFecha(LocalDateTime.now());
        transferencia.setEstado("PROCESADA");
        transferencia.setClaveIdempotencia(clave);
        transferencia.setDescripcion("Prueba UNIQUE");

        return transferencia;
    }
}

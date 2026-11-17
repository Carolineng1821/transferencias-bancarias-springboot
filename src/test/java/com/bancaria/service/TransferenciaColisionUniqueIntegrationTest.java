
package com.bancaria.service;

import com.bancaria.entity.Cuenta;
import com.bancaria.entity.Transferencia;
import com.bancaria.repository.CuentaRepository;
import com.bancaria.repository.TransferenciaRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TransferenciaColisionUniqueIntegrationTest {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private TransferenciaRepository transferenciaRepository;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager
            transactionManager;

    @Test
    void mysqlRechazaInsercionConcurrenteConMismaClave()
            throws Exception {

        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta(
                "T" + UUID.randomUUID().toString().substring(0, 9));
        origen.setTitular("Origen");
        origen.setSaldo(new BigDecimal("1000.00"));
        origen = cuentaRepository.saveAndFlush(origen);

        Cuenta destino = new Cuenta();
        destino.setNumeroCuenta(
                "T" + UUID.randomUUID().toString().substring(0, 9));
        destino.setTitular("Destino");
        destino.setSaldo(new BigDecimal("500.00"));
        destino = cuentaRepository.saveAndFlush(destino);

        final Cuenta cuentaOrigen = origen;
        final Cuenta cuentaDestino = destino;
        final String clave = "COL-" + UUID.randomUUID();

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch primeraInsertada = new CountDownLatch(1);
        CountDownLatch liberarPrimera = new CountDownLatch(1);

        TransactionTemplate transaccion =
                new TransactionTemplate(transactionManager);

        try {
            Future<Void> primera = executor.submit(() -> {
                transaccion.execute(status -> {
                    Transferencia t = crearTransferencia(
                            cuentaOrigen, cuentaDestino, clave);

                    transferenciaRepository.saveAndFlush(t);
                    primeraInsertada.countDown();

                    try {
                        if (!liberarPrimera.await(
                                10, TimeUnit.SECONDS)) {
                            throw new IllegalStateException(
                                    "Tiempo de espera agotado");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }

                    return null;
                });
                return null;
            });

            assertTrue(
                    primeraInsertada.await(10, TimeUnit.SECONDS),
                    "La primera transacción no insertó a tiempo"
            );

            Future<Boolean> segunda = executor.submit(() -> {
                try {
                    transaccion.execute(status -> {
                        Transferencia t = crearTransferencia(
                                cuentaOrigen, cuentaDestino, clave);

                        transferenciaRepository.saveAndFlush(t);
                        return null;
                    });

                    return false;
                } catch (org.springframework.dao
                                 .DataIntegrityViolationException ex) {
                    return true;
                }
            });

            // Permitimos que la segunda transacción intente insertar.
            // La primera conserva temporalmente el bloqueo.
            Thread.sleep(500);
            liberarPrimera.countDown();

            primera.get(15, TimeUnit.SECONDS);

            assertTrue(
                    segunda.get(15, TimeUnit.SECONDS),
                    "MySQL debió rechazar la clave duplicada"
            );

            long registros = transferenciaRepository.findAll()
                    .stream()
                    .filter(t -> clave.equals(t.getClaveIdempotencia()))
                    .count();

            assertEquals(1, registros);

        } finally {
            liberarPrimera.countDown();
            executor.shutdownNow();
        }
    }

    private Transferencia crearTransferencia(
            Cuenta origen, Cuenta destino, String clave) {

        Transferencia t = new Transferencia();
        t.setCuentaOrigen(origen);
        t.setCuentaDestino(destino);
        t.setImporte(new BigDecimal("100.00"));
        t.setFecha(LocalDateTime.now());
        t.setEstado("PROCESADA");
        t.setClaveIdempotencia(clave);
        t.setDescripcion("Prueba de colisión UNIQUE");

        return t;
    }
}

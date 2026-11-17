
package com.bancaria.service;

import com.bancaria.dto.ResultadoTransferencia;
import com.bancaria.dto.TransferenciaRequest;
import com.bancaria.entity.Cuenta;
import com.bancaria.repository.CuentaRepository;
import com.bancaria.repository.TransferenciaRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TransferenciaConcurrenciaIntegrationTest {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private TransferenciaRepository transferenciaRepository;

    @Autowired
    private TransferenciaCoordinadorService coordinadorService;

    @Test
    void dosSolicitudesConcurrentesNoDuplicanTransferencia()
            throws Exception {

        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta("T" + UUID.randomUUID()
                .toString().substring(0, 9));
        origen.setTitular("Origen concurrente");
        origen.setSaldo(new BigDecimal("1000.00"));
        origen = cuentaRepository.saveAndFlush(origen);

        Cuenta destino = new Cuenta();
        destino.setNumeroCuenta("T" + UUID.randomUUID()
                .toString().substring(0, 9));
        destino.setTitular("Destino concurrente");
        destino.setSaldo(new BigDecimal("500.00"));
        destino = cuentaRepository.saveAndFlush(destino);

        TransferenciaRequest request = new TransferenciaRequest();
        request.setCuentaOrigenId(origen.getId());
        request.setCuentaDestinoId(destino.getId());
        request.setImporte(new BigDecimal("100.00"));
        request.setClaveIdempotencia(
                "CONC-" + UUID.randomUUID()
        );
        request.setDescripcion("Prueba concurrente");

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch inicio = new CountDownLatch(1);

        try {
            Callable<ResultadoTransferencia> tarea = () -> {
                inicio.await();
                return coordinadorService.realizarTransferencia(request);
            };

            Future<ResultadoTransferencia> primera =
                    executor.submit(tarea);

            Future<ResultadoTransferencia> segunda =
                    executor.submit(tarea);

            inicio.countDown();

            ResultadoTransferencia resultado1 =
                    primera.get(15, TimeUnit.SECONDS);

            ResultadoTransferencia resultado2 =
                    segunda.get(15, TimeUnit.SECONDS);

            assertEquals(
                    resultado1.getTransferencia().getId(),
                    resultado2.getTransferencia().getId()
            );

            assertTrue(
                    resultado1.isCreada() != resultado2.isCreada()
            );

            long registros = transferenciaRepository.findAll()
                    .stream()
                    .filter(t -> request.getClaveIdempotencia()
                            .equals(t.getClaveIdempotencia()))
                    .count();

            assertEquals(1, registros);

            Cuenta origenActualizado = cuentaRepository
                    .findById(origen.getId()).get();

            Cuenta destinoActualizado = cuentaRepository
                    .findById(destino.getId()).get();

            assertEquals(
                    0,
                    new BigDecimal("900.00")
                            .compareTo(origenActualizado.getSaldo())
            );

            assertEquals(
                    0,
                    new BigDecimal("600.00")
                            .compareTo(destinoActualizado.getSaldo())
            );

        } finally {
            executor.shutdownNow();
        }
    }
}

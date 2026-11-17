package com.bancaria.service;

import com.bancaria.dto.ResultadoTransferencia;
import com.bancaria.dto.TransferenciaRequest;
import com.bancaria.dto.TransferenciaResponse;
import com.bancaria.entity.Cuenta;
import com.bancaria.entity.Transferencia;
import com.bancaria.exception.RecursoNoEncontradoException;
import com.bancaria.exception.SaldoInsuficienteException;
import com.bancaria.exception.SolicitudInvalidaException;
import com.bancaria.repository.CuentaRepository;
import com.bancaria.repository.TransferenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransferenciaService {

    private final CuentaRepository cuentaRepository;
    private final TransferenciaRepository transferenciaRepository;

    public TransferenciaService(
            CuentaRepository cuentaRepository,
            TransferenciaRepository transferenciaRepository) {
        this.cuentaRepository = cuentaRepository;
        this.transferenciaRepository = transferenciaRepository;
    }

    @Transactional
    public ResultadoTransferencia realizarTransferencia(
            TransferenciaRequest request) {
        Optional<Transferencia> existente =
                transferenciaRepository.findByClaveIdempotencia(
                        request.getClaveIdempotencia());

        if (existente.isPresent()) {
            Transferencia anterior = existente.get();

            boolean mismosDatos =
                    anterior.getCuentaOrigen().getId()
                            .equals(request.getCuentaOrigenId())
                            && anterior.getCuentaDestino().getId()
                            .equals(request.getCuentaDestinoId())
                            && anterior.getImporte().compareTo(request.getImporte()) == 0
                            && java.util.Objects.equals(
                            anterior.getDescripcion(), request.getDescripcion());

            if (!mismosDatos) {
                throw new IllegalArgumentException(
                        "La clave de idempotencia ya fue utilizada con otros datos");
            }
            return new ResultadoTransferencia(convertirAResponse(anterior), false);
        }

        if (request.getCuentaOrigenId().equals(
                request.getCuentaDestinoId())) {
            throw new SolicitudInvalidaException(
                    "La cuenta de origen y destino deben ser diferentes"
            );
        }

        if (request.getImporte().scale() > 2) {
            throw new IllegalArgumentException(
                    "El importe admite máximo dos decimales"
            );
        }

        Long origenId = request.getCuentaOrigenId();
        Long destinoId = request.getCuentaDestinoId();

        //Bloquear siempre en orden ascendente para reducir deadlocks.
        Long primerId = Math.min(origenId, destinoId);
        Long segundoId = Math.max(origenId, destinoId);

        Cuenta primera = cuentaRepository.buscarPorIdConBloqueo(primerId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cuenta no encontrada: " + primerId
                ));

        Cuenta segunda = cuentaRepository.buscarPorIdConBloqueo(segundoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cuenta no encontrada: " + segundoId
                ));

        Cuenta origen = primera.getId().equals(origenId)
                ? primera : segunda;

        Cuenta destino = primera.getId().equals(destinoId)
                ? primera : segunda;

        BigDecimal importe = request.getImporte();

        if (origen.getSaldo().compareTo(importe) < 0) {
            throw new SaldoInsuficienteException(
                    "La cuenta de origen no tiene saldo suficiente"
            );
        }
            origen.setSaldo(origen.getSaldo().subtract(importe));
            destino.setSaldo(destino.getSaldo().add(importe));

            cuentaRepository.save(origen);
            cuentaRepository.save(destino);

            Transferencia transferencia = new Transferencia();

            transferencia.setCuentaOrigen(origen);
            transferencia.setCuentaDestino(destino);
            transferencia.setImporte(importe);
            transferencia.setFecha(LocalDateTime.now());
            transferencia.setEstado("PROCESADA");
            transferencia.setClaveIdempotencia(
                    request.getClaveIdempotencia());
            transferencia.setDescripcion(request.getDescripcion());

            Transferencia guardada = transferenciaRepository.saveAndFlush(transferencia);

            return new ResultadoTransferencia(convertirAResponse(guardada), true);
        }


        @Transactional(readOnly = true)
        public List<TransferenciaResponse> listarTranferencia () {
            return transferenciaRepository.findAll()
                    .stream()
                    .map(this::convertirAResponse)
                    .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public TransferenciaResponse buscarPorId (Long id){
            Transferencia transferencia = transferenciaRepository.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Transferencia no encontrada: " + id
                    ));
            return convertirAResponse(transferencia);
        }

        private TransferenciaResponse convertirAResponse(
                Transferencia transferencia) {
            return  new TransferenciaResponse(
              transferencia.getId(),
                    transferencia.getCuentaOrigen().getId(),
                    transferencia.getCuentaDestino().getId(),
                    transferencia.getImporte(),
                    transferencia.getFecha(),
                    transferencia.getEstado(),
                    transferencia.getClaveIdempotencia(),
                    transferencia.getDescripcion()
            );
        }
}

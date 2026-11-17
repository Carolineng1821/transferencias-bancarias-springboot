package com.bancaria.service;

import com.bancaria.dto.ResultadoTransferencia;
import com.bancaria.dto.TransferenciaRequest;
import com.bancaria.dto.TransferenciaResponse;
import com.bancaria.entity.Transferencia;
import com.bancaria.exception.SolicitudInvalidaException;
import com.bancaria.repository.TransferenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
public class TransferenciaIdempotenciaService {

    private final TransferenciaRepository transferenciaRepository;

    public TransferenciaIdempotenciaService(TransferenciaRepository transferenciaRepository) {
        this.transferenciaRepository = transferenciaRepository;
    }

    @Transactional(readOnly = true)
    public Optional<ResultadoTransferencia> buscarExistente(
            TransferenciaRequest request) {

        Optional <Transferencia> existente =
                transferenciaRepository.findByClaveIdempotencia(
                        request.getClaveIdempotencia()
                );

        if (!existente.isPresent()) {
            return Optional.empty();
        }

        Transferencia anterior = existente.get();

        boolean mismosDatos =
                anterior.getCuentaOrigen().getId()
                        .equals(request.getCuentaOrigenId())
                && anterior.getCuentaDestino().getId()
                        .equals(request.getCuentaDestinoId())
                && anterior.getImporte().compareTo(request.getImporte()) == 0
                && Objects.equals(
                        anterior.getDescripcion(),
                        request.getDescripcion()
                );
        if (!mismosDatos) {
            throw new IllegalArgumentException(
                    "La clave de idempotencia ya fue utilizada con otros datos"
            );
        }
        TransferenciaResponse respuesta = new TransferenciaResponse(
                anterior.getId(),
                anterior.getCuentaOrigen().getId(),
                anterior.getCuentaDestino().getId(),
                anterior.getImporte(),
                anterior.getFecha(),
                anterior.getEstado(),
                anterior.getClaveIdempotencia(),
                anterior.getDescripcion()
        );

        return Optional.of(
                new ResultadoTransferencia(respuesta, false)
        );
    }
}

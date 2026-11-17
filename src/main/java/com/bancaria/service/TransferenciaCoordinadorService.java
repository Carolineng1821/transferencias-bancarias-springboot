package com.bancaria.service;

import com.bancaria.dto.ResultadoTransferencia;
import com.bancaria.dto.TransferenciaRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TransferenciaCoordinadorService {

    private final TransferenciaService transferenciaService;
    private final TransferenciaIdempotenciaService idempotenciaService;

    public TransferenciaCoordinadorService(
            TransferenciaService transferenciaService,
            TransferenciaIdempotenciaService idempotenciaService
    ) {
        this.transferenciaService = transferenciaService;
        this.idempotenciaService = idempotenciaService;
    }

    public ResultadoTransferencia realizarTransferencia(
            TransferenciaRequest request) {
        Optional<ResultadoTransferencia> existente =
                idempotenciaService.buscarExistente(request);

        if (existente.isPresent()) {
            return existente.get();
        }

        try {
            return transferenciaService.realizarTransferencia(request);
        } catch (DataIntegrityViolationException ex) {

            Optional<ResultadoTransferencia> recuperada =
                    idempotenciaService.buscarExistente(request);

            if (recuperada.isPresent()) {
                return recuperada.get();
            }

            throw ex;
        }
    }
}

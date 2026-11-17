
package com.bancaria.controller;

import com.bancaria.dto.ResultadoTransferencia;
import com.bancaria.dto.TransferenciaRequest;
import com.bancaria.dto.TransferenciaResponse;
import com.bancaria.service.TransferenciaCoordinadorService;
import com.bancaria.service.TransferenciaService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/transferencias")
public class TransferenciaController {

    private final TransferenciaCoordinadorService coordinadorService;
    private final TransferenciaService transferenciaService;

    public TransferenciaController(
            TransferenciaCoordinadorService coordinadorService,
            TransferenciaService transferenciaService) {

        this.coordinadorService = coordinadorService;
        this.transferenciaService = transferenciaService;
    }

    @PostMapping
    public ResponseEntity<TransferenciaResponse> realizarTransferencia(
            @Valid @RequestBody TransferenciaRequest request) {

        ResultadoTransferencia resultado =
                coordinadorService.realizarTransferencia(request);

        TransferenciaResponse respuesta = resultado.getTransferencia();

        if (!resultado.isCreada()) {
            return ResponseEntity.ok(respuesta);
        }

        URI ubicacion = URI.create("/transferencias/" + respuesta.getId());

        return ResponseEntity.created(ubicacion).body(respuesta);
    }

    @GetMapping
    public ResponseEntity<List<TransferenciaResponse>> listar() {
        return ResponseEntity.ok(
                transferenciaService.listarTranferencia()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferenciaResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                transferenciaService.buscarPorId(id)
        );
    }
}

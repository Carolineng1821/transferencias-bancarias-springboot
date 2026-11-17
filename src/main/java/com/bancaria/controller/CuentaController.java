package com.bancaria.controller;

import com.bancaria.dto.CuentaRequest;
import com.bancaria.dto.CuentaResponse;
import com.bancaria.service.CuentaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.validation.Valid;
import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    private CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping
    public ResponseEntity<List<CuentaResponse>> listarCuentas(){
        return ResponseEntity.ok(cuentaService.listarCuentas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaResponse> buscarPorId(
            @PathVariable long id
    ) {
        return cuentaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CuentaResponse> registrarCuenta(
            @Valid @RequestBody CuentaRequest request) {

        CuentaResponse cuenta = cuentaService.registrarCuenta(request);

        URI ubicacion = URI.create("/cuentas/" + cuenta.getId());

        return ResponseEntity
                .created(ubicacion)
                .body(cuenta);
    }
}


package com.bancaria.service;

import com.bancaria.dto.CuentaRequest;
import com.bancaria.dto.CuentaResponse;
import com.bancaria.entity.Cuenta;
import com.bancaria.repository.CuentaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;

    public CuentaService(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @Transactional(readOnly = true)
    public List<CuentaResponse> listarCuentas() {
        return cuentaRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CuentaResponse> buscarPorId(Long id) {
        return cuentaRepository.findById(id)
                .map(this::convertirAResponse);
    }

    @Transactional(readOnly = true)
    public Optional<CuentaResponse> buscarPorNumero(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .map(this::convertirAResponse);
    }

    @Transactional
    public CuentaResponse registrarCuenta(CuentaRequest request) {

        if (cuentaRepository.existsByNumeroCuenta(
                request.getNumeroCuenta())) {

            throw new IllegalArgumentException(
                    "El número de cuenta ya está registrado"
            );
        }

        Cuenta cuenta = new Cuenta(
                request.getNumeroCuenta(),
                request.getTitular(),
                BigDecimal.ZERO.setScale(2)
        );

        Cuenta cuentaGuardada = cuentaRepository.save(cuenta);

        return convertirAResponse(cuentaGuardada);
    }

    private CuentaResponse convertirAResponse(Cuenta cuenta) {
        return new CuentaResponse(
                cuenta.getId(),
                cuenta.getNumeroCuenta(),
                cuenta.getTitular(),
                cuenta.getSaldo()
        );
    }
}

package com.bancaria.repository;

import com.bancaria.entity.Transferencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferenciaRepository
        extends JpaRepository<Transferencia, Long> {

    Optional<Transferencia> findByClaveIdempotencia(
            String claveIdempotencia);

    boolean existsByClaveIdempotencia(
            String claveIdempotencia
    );

}

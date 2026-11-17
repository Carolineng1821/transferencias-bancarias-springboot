package com.bancaria.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferenciaResponse {

    private Long id;
    private Long cuentaOrigenId;
    private Long cuentaDestinoId;
    private BigDecimal importe;
    private LocalDateTime fecha;
    private String estado;
    private String claveIdempotencia;
    private String descripcion;

    public TransferenciaResponse() {
    }

    public TransferenciaResponse(
            Long id,
            Long cuentaOrigenId,
            Long cuentaDestinoId,
            BigDecimal importe,
            LocalDateTime fecha,
            String estado,
            String claveIdempotencia,
            String descripcion
    ) {
        this.id = id;
        this.cuentaOrigenId = cuentaOrigenId;
        this.cuentaDestinoId = cuentaDestinoId;
        this.importe = importe;
        this.fecha = fecha;
        this.estado = estado;
        this.claveIdempotencia = claveIdempotencia;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }
    public Long getCuentaOrigenId() {
        return cuentaOrigenId;
    }
    public Long getCuentaDestinoId() {
        return cuentaDestinoId;
    }
    public BigDecimal getImporte() {
        return importe;
    }
    public LocalDateTime getFecha() {
        return fecha;
    }
    public String getEstado() {
        return estado;
    }
    public String getClaveIdempotencia() {
        return claveIdempotencia;
    }
    public String getDescripcion() {
        return descripcion;
    }
}

package com.bancaria.dto;

import javax.validation.constraints.*;
import java.math.BigDecimal;

public class TransferenciaRequest {

    @NotNull(message = "La cuenta de origen es obligatoria")
    private Long cuentaOrigenId;

    @NotNull(message = "La cuenta de destino es obligatoria")
    private Long cuentaDestinoId;

    @NotNull(message = "El importe es obligatorio")
    @DecimalMin(value = "0.01", message = "El importe debe ser mayor a cero")
    @Digits(integer = 13, fraction = 2, message = "El importe admite máximo dos decimales")
    private BigDecimal importe;

    @NotBlank(message = "La clave de idempotencia es obligatoria")
    @Size(max = 255, message = "Máximo 255 caracteres")
    private String claveIdempotencia;

    private String descripcion;

    public TransferenciaRequest() {
    }

    public Long getCuentaOrigenId() {
        return cuentaOrigenId;
    }
    public void setCuentaOrigenId(Long cuentaOrigenId) {
        this.cuentaOrigenId = cuentaOrigenId;
    }
    public Long getCuentaDestinoId() {
        return cuentaDestinoId;
    }
    public void setCuentaDestinoId(Long cuentaDestinoId) {
        this.cuentaDestinoId = cuentaDestinoId;
    }
    public BigDecimal getImporte() {
        return importe;
    }
    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }
    public String getClaveIdempotencia() {
        return claveIdempotencia;
    }
    public void setClaveIdempotencia(String claveIdempotencia) {
        this.claveIdempotencia = claveIdempotencia;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

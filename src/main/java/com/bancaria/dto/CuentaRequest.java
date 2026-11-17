package com.bancaria.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CuentaRequest {
    @NotBlank(message = "El número de cuenta es obligatorio")
    @Size(max = 20, message = "Máximo 20 caracteres")
    private String numeroCuenta;
    @NotBlank(message = "El titular es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String titular;

    public CuentaRequest(){
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }
    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }
    public String getTitular() {
        return titular;
    }
    public void setTitular(String titular) {
        this.titular = titular;
    }
}

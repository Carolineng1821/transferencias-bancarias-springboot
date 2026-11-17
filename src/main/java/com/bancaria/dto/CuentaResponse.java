package com.bancaria.dto;

import java.math.BigDecimal;

public class CuentaResponse {
    private Long id;
    private String numeroCuenta;
    private String titular;
    private BigDecimal saldo;

    public CuentaResponse() {
    }

    public CuentaResponse(Long id, String numeroCuenta, String titular, BigDecimal saldo) {
        this.id = id;
        this.numeroCuenta = numeroCuenta;
        this.titular = titular;
        this.saldo = saldo;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
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
    public BigDecimal getSaldo() {
        return saldo;
    }
    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
}

package com.bancaria.entity;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "cuentas")
public class Cuenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "numero_cuenta", nullable = false,unique = true, length = 20)
    private String numeroCuenta;

    @NotBlank
    @Column(name = "titular", nullable = false,length = 100)
    private String titular;

    @NotNull
    @DecimalMin(value = "0.00")
    @Column(name = "saldo", nullable = false,precision = 15, scale = 2)
    private BigDecimal saldo;

    public Cuenta() {
    }

    public Cuenta(String numeroCuenta, String titular, BigDecimal saldo) {
        this.numeroCuenta = numeroCuenta;
        this.titular = titular;
        this.saldo = saldo;
    }

    public Long getId() {
        return id;
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
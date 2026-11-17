package com.bancaria.dto;

public class ResultadoTransferencia {

    private final TransferenciaResponse transferencia;
    private final boolean creada;

    public ResultadoTransferencia (TransferenciaResponse transferencia, boolean creada) {
        this.transferencia = transferencia;
        this.creada = creada;
    }

    public TransferenciaResponse getTransferencia() {
        return transferencia;
    }
    public boolean isCreada() {
        return creada;
    }
}
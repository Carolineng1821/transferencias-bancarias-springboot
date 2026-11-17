package com.bancaria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarConflicto(
            IllegalArgumentException ex) {

        return crearError(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(
            RecursoNoEncontradoException ex) {
        return crearError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> manejarSaldoInsuficiente(
            SaldoInsuficienteException ex) {
        return crearError(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidaciones(
            MethodArgumentNotValidException ex) {
        Map<String, Object> detalles = new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(campo -> detalles.put(
                        campo.getField(),
                        campo.getDefaultMessage()
                ));

        Map<String, Object> respuesta = new LinkedHashMap<>();

        respuesta.put("status", HttpStatus.BAD_REQUEST.value());
        respuesta.put("error", "Bad Request");
        respuesta.put("message", "Datos de entrada inválidos");
        respuesta.put("detalles", detalles);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta);
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<Map<String, Object>> manejarSolicitudInvalida(
            SolicitudInvalidaException ex) {
        return crearError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> crearError(
            HttpStatus estado, String mensaje
    ) {
        Map<String, Object> error = new LinkedHashMap<>();

        error.put("status", estado.value());
        error.put("error", estado.getReasonPhrase());
        error.put("message", mensaje);
        return ResponseEntity
                .status(estado)
                .body(error);
    }
}

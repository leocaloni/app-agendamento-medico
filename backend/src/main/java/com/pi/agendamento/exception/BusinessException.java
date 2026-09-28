package com.pi.agendamento.exception;

// violacao de regra de negocio, vira 400
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}

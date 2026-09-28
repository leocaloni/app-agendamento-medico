package com.pi.agendamento.exception;

// conflito de estado, vira 409
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message);
    }
}
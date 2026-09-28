package com.pi.agendamento.exception;

// recurso inexistente, vira 404
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
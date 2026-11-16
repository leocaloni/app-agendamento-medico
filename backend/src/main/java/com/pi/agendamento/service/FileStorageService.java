package com.pi.agendamento.service;

import org.springframework.core.io.Resource;

// onde os arquivos ficam; o banco so guarda a storageKey
public interface FileStorageService {

    // grava os bytes na chave informada
    void store(byte[] content, String storageKey);

    // le o arquivo da chave
    Resource load(String storageKey);

    // apaga o arquivo da chave, sem erro se ja nao existir
    void delete(String storageKey);
}

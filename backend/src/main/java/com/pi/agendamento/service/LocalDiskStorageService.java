package com.pi.agendamento.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.pi.agendamento.exception.ResourceNotFoundException;

// guarda os arquivos numa pasta local, sem infra extra
@Service
public class LocalDiskStorageService implements FileStorageService {

    private final Path root;

    public LocalDiskStorageService(@Value("${app.storage.local-path}") String localPath) {
        this.root = Path.of(localPath).toAbsolutePath().normalize();
    }

    @Override
    public void store(byte[] content, String storageKey) {
        Path target = resolve(storageKey);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException ex) {
            throw new UncheckedIOException("Falha ao gravar arquivo", ex);
        }
    }

    @Override
    public Resource load(String storageKey) {
        Path target = resolve(storageKey);
        if (!Files.isRegularFile(target)) {
            throw new ResourceNotFoundException("Arquivo nao encontrado no storage");
        }
        return new FileSystemResource(target);
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException ex) {
            throw new UncheckedIOException("Falha ao apagar arquivo", ex);
        }
    }

    // a chave eh gerada pelo sistema, mas o resolve ainda barra sair da pasta raiz
    private Path resolve(String storageKey) {
        Path target = root.resolve(storageKey).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("storageKey invalida");
        }
        return target;
    }
}

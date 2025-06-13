package com.example.videoapp.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

public interface StorageService {
    
    /**
     * Inizializza la directory di upload
     */
    void init() throws IOException;
    
    /**
     * Salva un file e restituisce il nome del file salvato
     */
    String store(MultipartFile file) throws IOException;
    
    /**
     * Legge il contenuto di un file
     */
    byte[] loadFile(String filename) throws IOException;
    
    /**
     * Elimina un file
     */
    void deleteFile(String filename) throws IOException;
    
    /**
     * Verifica se un file esiste
     */
    boolean exists(String filename);
    
    /**
     * Ottiene il content type di un file
     */
    String getContentType(String filename) throws IOException;
    
    /**
     * Valida se il file è consentito (estensione e dimensione)
     */
    void validateFile(MultipartFile file) throws IOException;
    
    /**
     * Sanitizza il nome del file
     */
    String sanitizeFileName(String fileName);
    
    /**
     * Ottiene l'estensione del file
     */
    String getFileExtension(String fileName);
}
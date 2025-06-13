package com.example.videoapp.service.impl;

import com.example.videoapp.config.StorageConfiguration;
import com.example.videoapp.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class StorageServiceImpl implements StorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(StorageServiceImpl.class);
    
    @Autowired
    private StorageConfiguration storageConfig;
    
    private Path uploadPath;
    
    @PostConstruct
    public void init() throws IOException {
        uploadPath = Paths.get(storageConfig.getUploadDir());
        
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            } catch (IOException e) {
                throw new IOException("Could not create upload directory: " + uploadPath, e);
            }
        }
        
        if (!Files.isWritable(uploadPath)) {
            throw new IOException("Upload directory is not writable: " + uploadPath);
        }
        
        logger.info("Storage service initialized with directory: {}", uploadPath.toAbsolutePath());
    }
    
    @Override
    public String store(MultipartFile file) throws IOException {
        validateFile(file);
        
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(originalFilename, fileExtension);
        
        Path destinationFile = uploadPath.resolve(uniqueFilename).normalize();
        
        // Security check: ensure the file is within the upload directory
        if (!destinationFile.getParent().equals(uploadPath)) {
            throw new IOException("Cannot store file outside current directory");
        }
        
        try {
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File stored successfully: {}", uniqueFilename);
            return uniqueFilename;
        } catch (IOException e) {
            throw new IOException("Failed to store file: " + uniqueFilename, e);
        }
    }
    
    @Override
    public byte[] loadFile(String filename) throws IOException {
        if (!StringUtils.hasText(filename)) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }
        
        Path filePath = uploadPath.resolve(filename).normalize();
        
        // Security check
        if (!filePath.getParent().equals(uploadPath)) {
            throw new IOException("Cannot read file outside upload directory");
        }
        
        if (!Files.exists(filePath)) {
            throw new NoSuchFileException("File not found: " + filename);
        }
        
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new IOException("Failed to read file: " + filename, e);
        }
    }
    
    @Override
    public void deleteFile(String filename) throws IOException {
        if (!StringUtils.hasText(filename)) {
            return; // Nothing to delete
        }
        
        Path filePath = uploadPath.resolve(filename).normalize();
        
        // Security check
        if (!filePath.getParent().equals(uploadPath)) {
            throw new IOException("Cannot delete file outside upload directory");
        }
        
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                logger.info("File deleted successfully: {}", filename);
            } else {
                logger.warn("File not found for deletion: {}", filename);
            }
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", filename, e);
            throw new IOException("Failed to delete file: " + filename, e);
        }
    }
    
    @Override
    public boolean exists(String filename) {
        if (!StringUtils.hasText(filename)) {
            return false;
        }
        
        Path filePath = uploadPath.resolve(filename).normalize();
        
        // Security check
        if (!filePath.getParent().equals(uploadPath)) {
            return false;
        }
        
        return Files.exists(filePath);
    }
    
    @Override
    public String getContentType(String filename) throws IOException {
        if (!StringUtils.hasText(filename)) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }
        
        Path filePath = uploadPath.resolve(filename).normalize();
        
        if (!exists(filename)) {
            throw new NoSuchFileException("File not found: " + filename);
        }
        
        String contentType = Files.probeContentType(filePath);
        
        // Fallback per video files se probeContentType non funziona
        if (contentType == null) {
            String extension = getFileExtension(filename).toLowerCase();
            contentType = switch (extension) {
                case ".mp4" -> "video/mp4";
                case ".avi" -> "video/x-msvideo";
                case ".mov" -> "video/quicktime";
                case ".wmv" -> "video/x-ms-wmv";
                case ".flv" -> "video/x-flv";
                case ".webm" -> "video/webm";
                case ".mkv" -> "video/x-matroska";
                default -> "application/octet-stream";
            };
        }
        
        return contentType;
    }
    
    @Override
    public void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty or null");
        }
        
        // Validate file size
        if (file.getSize() > storageConfig.getMaxFileSize()) {
            throw new IOException("File size exceeds maximum allowed size: " + 
                formatFileSize(storageConfig.getMaxFileSize()));
        }
        
        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new IOException("Original filename is missing");
        }
        
        String fileExtension = getFileExtension(originalFilename).toLowerCase().substring(1); // Remove the dot
        List<String> allowedExtensions = Arrays.asList(storageConfig.getAllowedExtensions().toLowerCase().split(","));
        
        if (!allowedExtensions.contains(fileExtension)) {
            throw new IOException("File extension not allowed. Allowed extensions: " + 
                storageConfig.getAllowedExtensions());
        }
    }
    
    @Override
    public String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "video";
        }
        
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_")
                      .replaceAll("_{2,}", "_")
                      .trim();
    }
    
    @Override
    public String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex != -1 ? fileName.substring(lastDotIndex) : "";
    }
    
    private String generateUniqueFilename(String originalFilename, String extension) {
        String baseName = sanitizeFileName(originalFilename.substring(0, 
            Math.max(0, originalFilename.lastIndexOf('.'))));
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return String.format("%s_%s_%s%s", baseName, timestamp, uuid, extension);
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)) + " MB";
        return (bytes / (1024 * 1024 * 1024)) + " GB";
    }
}
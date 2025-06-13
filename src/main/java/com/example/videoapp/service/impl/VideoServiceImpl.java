package com.example.videoapp.service.impl;

import com.example.videoapp.dto.NuovoVideoInputDto;
import com.example.videoapp.model.User;
import com.example.videoapp.model.Video;
import com.example.videoapp.repository.UserRepository;
import com.example.videoapp.repository.VideoRepository;
import com.example.videoapp.service.StorageService;
import com.example.videoapp.service.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.*;

@Service
public class VideoServiceImpl implements VideoService {
    
    private static final Logger logger = LoggerFactory.getLogger(VideoServiceImpl.class);

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StorageService storageService;

    @Override
    public Page<Video> getVideos(int page, int size, Long userId, Boolean isPublic) {
        if (size > 10) size = 10;
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);

        if (userId != null && isPublic != null) {
            return videoRepository.findByUserIdAndIsPublic(userId, isPublic, pageable);
        } else if (userId != null) {
            return videoRepository.findByUserId(userId, pageable);
        } else if (isPublic != null) {
            return videoRepository.findByIsPublic(isPublic, pageable);
        } else {
            return videoRepository.findAll(pageable);
        }
    }

    @Override
    public Video getVideoById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Video con id " + id + " non trovato"));
    }

    @Override
    public Video uploadVideo(NuovoVideoInputDto dto, MultipartFile file) throws IOException {
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("User ID è obbligatorio");
        }
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File video è obbligatorio");
        }
        
        User user = userRepository.findById(dto.getUserId())
            .orElseThrow(() -> new NoSuchElementException("User con id " + dto.getUserId() + " non trovato"));

        try {
            // Utilizza il StorageService per salvare il file
            String savedFilename = storageService.store(file);
            
            Video video = new Video(dto.getTitle(), dto.getDescription(), dto.isPublic(), user);
            video.setFilePath(savedFilename);
            
            Video savedVideo = videoRepository.save(video);
            logger.info("Video uploaded successfully with ID: {} and file: {}", savedVideo.getId(), savedFilename);
            
            return savedVideo;
        } catch (IOException e) {
            logger.error("Error uploading video for user {}: {}", dto.getUserId(), e.getMessage());
            throw new IOException("Errore durante il caricamento del video: " + e.getMessage(), e);
        }
    }

    @Override
    public Video updateVideoWithFile(Long id, String title, String description, boolean isPublic, MultipartFile file) throws IOException {
        Video video = videoRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Video con id " + id + " non trovato"));
        
        video.setTitle(title);
        video.setDescription(description);
        video.setPublic(isPublic);

        if (file != null && !file.isEmpty()) {
            try {
                // Elimina il file precedente se esiste
                if (video.getFilePath() != null) {
                    storageService.deleteFile(video.getFilePath());
                }

                // Salva il nuovo file
                String savedFilename = storageService.store(file);
                video.setFilePath(savedFilename);
                
                logger.info("Video file updated for video ID: {} with new file: {}", id, savedFilename);
            } catch (IOException e) {
                logger.error("Error updating video file for video ID {}: {}", id, e.getMessage());
                throw new IOException("Errore durante l'aggiornamento del file: " + e.getMessage(), e);
            }
        }

        return videoRepository.save(video);
    }

    @Override
    public void deleteVideo(Long id) {
        Video video = videoRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Video con id " + id + " non trovato"));

        // Elimina il file dal filesystem
        if (video.getFilePath() != null) {
            try {
                storageService.deleteFile(video.getFilePath());
                logger.info("File deleted for video ID: {}", id);
            } catch (IOException e) {
                logger.error("Error deleting file for video ID {}: {}", id, e.getMessage());
                // Non interrompiamo l'eliminazione del video anche se il file non può essere eliminato
            }
        }

        videoRepository.delete(video);
        logger.info("Video deleted with ID: {}", id);
    }

    @Override
    public byte[] getVideoFile(Long id) throws IOException {
        Video video = videoRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Video con id " + id + " non trovato"));

        if (video.getFilePath() == null) {
            throw new IllegalStateException("Il video non ha un file associato");
        }

        try {
            return storageService.loadFile(video.getFilePath());
        } catch (NoSuchFileException e) {
            logger.error("File not found for video ID {}: {}", id, video.getFilePath());
            throw new NoSuchFileException("File video non trovato: " + video.getFilePath());
        } catch (IOException e) {
            logger.error("Error reading file for video ID {}: {}", id, e.getMessage());
            throw new IOException("Errore durante la lettura del file: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadVideoFile(Long id) throws IOException {
        // Per il download, la logica è la stessa del getVideoFile
        // La differenza sarà gestita nel controller con gli headers appropriati
        return getVideoFile(id);
    }

    @Override
    public String getVideoContentType(Long id) throws IOException {
        Video video = videoRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Video con id " + id + " non trovato"));

        if (video.getFilePath() == null) {
            throw new IllegalStateException("Il video non ha un file associato");
        }

        try {
            return storageService.getContentType(video.getFilePath());
        } catch (NoSuchFileException e) {
            logger.error("File not found for video ID {}: {}", id, video.getFilePath());
            throw new NoSuchFileException("File video non trovato: " + video.getFilePath());
        } catch (IOException e) {
            logger.error("Error getting content type for video ID {}: {}", id, e.getMessage());
            throw new IOException("Errore durante il recupero del tipo di contenuto: " + e.getMessage(), e);
        }
    }

    @Override
    public String getVideoFileName(Long id) {
        Video video = videoRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Video con id " + id + " non trovato"));

        if (video.getTitle() != null && !video.getTitle().trim().isEmpty()) {
            String sanitizedTitle = storageService.sanitizeFileName(video.getTitle());
            String extension = storageService.getFileExtension(video.getFilePath());
            return sanitizedTitle + extension;
        }
        
        return video.getFilePath();
    }

    @Override
    public Video updateVideo(Long id, Map<String, Object> updates) {
        Video video = videoRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Video con id " + id + " non trovato"));

        if (updates.isEmpty()) {
            throw new IllegalArgumentException("Nessun campo da aggiornare fornito");
        }

        updates.forEach((key, value) -> {
            switch (key) {
                case "title":
                    if (value != null) {
                        video.setTitle((String) value);
                    }
                    break;
                case "description":
                    if (value != null) {
                        video.setDescription((String) value);
                    }
                    break;
                case "isPublic":
                    if (value != null) {
                        video.setPublic((Boolean) value);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Campo non supportato: " + key);
            }
        });

        Video updatedVideo = videoRepository.save(video);
        logger.info("Video metadata updated for ID: {}", id);
        return updatedVideo;
    }
}
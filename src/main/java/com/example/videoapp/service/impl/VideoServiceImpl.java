package com.example.videoapp.service.impl;

import com.example.videoapp.converter.VideoMapper;
import com.example.videoapp.dto.NuovoVideoInputDto;
import com.example.videoapp.dto.VideoOutputDto;
import com.example.videoapp.model.User;
import com.example.videoapp.model.Video;
import com.example.videoapp.repository.UserRepository;
import com.example.videoapp.repository.VideoRepository;
import com.example.videoapp.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoMapper videoMapper;

    @Value("${videoapp.upload.dir}")
    private String uploadDir;

    @Override
    public ResponseEntity<Page<VideoOutputDto>> getVideos(int page, int size, Long userId, Boolean isPublic) {
        if (size > 10) size = 10;
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        Page<Video> videos;

        if (userId != null && isPublic != null) {
            videos = videoRepository.findByUserIdAndIsPublic(userId, isPublic, pageable);
        } else if (userId != null) {
            videos = videoRepository.findByUserId(userId, pageable);
        } else if (isPublic != null) {
            videos = videoRepository.findByIsPublic(isPublic, pageable);
        } else {
            videos = videoRepository.findAll(pageable);
        }

        Page<VideoOutputDto> dtoPage = videos.map(videoMapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @Override
    public ResponseEntity<VideoOutputDto> getVideoById(Long id) {
        return videoRepository.findById(id)
                .map(video -> ResponseEntity.ok(videoMapper.toDto(video)))
                .orElseThrow(() -> new NoSuchElementException("Video con id " + id + " non trovato"));
    }

    @Override
    public ResponseEntity<String> uploadVideo(NuovoVideoInputDto dto, MultipartFile file) throws IOException {
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("User ID è obbligatorio");
        }
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File video è obbligatorio");
        }
        
        User user = userRepository.findById(dto.getUserId())
            .orElseThrow(() -> new NoSuchElementException("User con id " + dto.getUserId() + " non trovato"));

        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);
        
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            Files.write(filePath, file.getBytes());

            Video video = new Video(dto.getTitle(), dto.getDescription(), dto.isPublic(), user);
            video.setFilePath(filename);
            videoRepository.save(video);

            return ResponseEntity.ok("Video caricato con successo");
        } catch (IOException e) {
            throw new IOException("Errore durante il salvataggio del file: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<VideoOutputDto> updateVideoWithFile(Long id, String title, String description, boolean isPublic, MultipartFile file) throws IOException {
        Video video = videoRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Video con id " + id + " non trovato"));
        
        video.setTitle(title);
        video.setDescription(description);
        video.setPublic(isPublic);

        if (file != null && !file.isEmpty()) {
            try {
                if (video.getFilePath() != null) {
                    Path oldFilePath = Paths.get(uploadDir, video.getFilePath());
                    Files.deleteIfExists(oldFilePath);
                }

                String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(filename);
                Files.write(filePath, file.getBytes());
                video.setFilePath(filename);
            } catch (IOException e) {
                throw new IOException("Errore durante l'aggiornamento del file: " + e.getMessage(), e);
            }
        }

        videoRepository.save(video);
        return ResponseEntity.ok(videoMapper.toDto(video));
    }

    @Override
    public ResponseEntity<Void> deleteVideo(Long id) {
        Video video = videoRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Video con id " + id + " non trovato"));

        if (video.getFilePath() != null) {
            Path videoPath = Paths.get(uploadDir, video.getFilePath());
            try {
                Files.deleteIfExists(videoPath);
            } catch (IOException e) {
                System.err.println("Errore durante l'eliminazione del file: " + e.getMessage());
            }
        }

        videoRepository.delete(video);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<byte[]> getVideoFile(Long id) throws IOException {
        Video video = videoRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Video con id " + id + " non trovato"));

        if (video.getFilePath() == null) {
            throw new IllegalStateException("Il video non ha un file associato");
        }

        Path videoPath = Paths.get(uploadDir, video.getFilePath());
        
        if (!Files.exists(videoPath)) {
            throw new NoSuchFileException("File video non trovato: " + video.getFilePath());
        }

        try {
            byte[] content = Files.readAllBytes(videoPath);
            String contentType = Files.probeContentType(videoPath);
            if (contentType == null) {
                contentType = "video/mp4";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + video.getFilePath() + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content);
        } catch (IOException e) {
            throw new IOException("Errore durante la lettura del file: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<byte[]> downloadVideoFile(Long id) throws IOException {
        Video video = videoRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Video con id " + id + " non trovato"));

        if (video.getFilePath() == null) {
            throw new IllegalStateException("Il video non ha un file associato");
        }

        Path videoPath = Paths.get(uploadDir, video.getFilePath());
        
        if (!Files.exists(videoPath)) {
            throw new NoSuchFileException("File video non trovato: " + video.getFilePath());
        }

        try {
            byte[] content = Files.readAllBytes(videoPath);
            String contentType = Files.probeContentType(videoPath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            String downloadFileName = video.getTitle() != null && !video.getTitle().trim().isEmpty() 
                ? sanitizeFileName(video.getTitle()) + getFileExtension(video.getFilePath())
                : video.getFilePath();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(content.length)
                    .body(content);
        } catch (IOException e) {
            throw new IOException("Errore durante la lettura del file per il download: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<VideoOutputDto> updateVideo(Long id, Map<String, Object> updates) {
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

        videoRepository.save(video);
        return ResponseEntity.ok(videoMapper.toDto(video));
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "video";
        
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_")
                      .replaceAll("_{2,}", "_")
                      .trim();
    }

    private String getFileExtension(String filePath) {
        if (filePath == null) return "";
        
        int lastDotIndex = filePath.lastIndexOf('.');
        return lastDotIndex != -1 ? filePath.substring(lastDotIndex) : "";
    }
}
package com.example.videoapp.controller;

import com.example.videoapp.dto.NuovoVideoInputDto;
import com.example.videoapp.dto.VideoOutputDto;
import com.example.videoapp.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @GetMapping
    public ResponseEntity<?> getVideos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean isPublic
    ) {
        return videoService.getVideos(page, size, userId, isPublic);
    }

    @GetMapping("/public")
    public ResponseEntity<?> getPublicVideos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return videoService.getVideos(page, size, null, true);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<String> uploadVideo(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("isPublic") boolean isPublic,
            @RequestParam("userId") Long userId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        NuovoVideoInputDto dto = new NuovoVideoInputDto();
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setPublic(isPublic);
        dto.setUserId(userId);
        
        return videoService.uploadVideo(dto, file);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        return videoService.deleteVideo(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoOutputDto> getVideo(@PathVariable Long id) {
        return videoService.getVideoById(id);
    }

    // Endpoint PUT per aggiornare video con multipart/form-data
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<VideoOutputDto> updateVideoWithFile(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("isPublic") boolean isPublic,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) throws IOException {
        return videoService.updateVideoWithFile(id, title, description, isPublic, file);
    }

    // Endpoint PUT per aggiornare solo i metadati (senza file)
    @PutMapping("/{id}")
    public ResponseEntity<VideoOutputDto> updateVideo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        return videoService.updateVideo(id, updates);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> getVideoFile(@PathVariable Long id) throws IOException {
        return videoService.getVideoFile(id);
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<byte[]> streamVideo(@PathVariable Long id) throws IOException {
        return videoService.getVideoFile(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadVideo(@PathVariable Long id) throws IOException {
        return videoService.downloadVideoFile(id);
    }
}
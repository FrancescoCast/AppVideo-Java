package com.example.videoapp.controller;

import com.example.videoapp.converter.VideoMapper;
import com.example.videoapp.dto.NuovoVideoInputDto;
import com.example.videoapp.dto.VideoOutputDto;
import com.example.videoapp.model.Video;
import com.example.videoapp.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    @Autowired
    private VideoMapper videoMapper;

    @GetMapping
    public ResponseEntity<Page<VideoOutputDto>> getVideos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean isPublic
    ) {
        Page<Video> videos = videoService.getVideos(page, size, userId, isPublic);
        Page<VideoOutputDto> dtoPage = videos.map(videoMapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/public")
    public ResponseEntity<Page<VideoOutputDto>> getPublicVideos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Video> videos = videoService.getVideos(page, size, null, true);
        Page<VideoOutputDto> dtoPage = videos.map(videoMapper::toDto);
        return ResponseEntity.ok(dtoPage);
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
        
        videoService.uploadVideo(dto, file);
        return ResponseEntity.ok("Video caricato con successo");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoOutputDto> getVideo(@PathVariable Long id) {
        Video video = videoService.getVideoById(id);
        return ResponseEntity.ok(videoMapper.toDto(video));
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
        Video video = videoService.updateVideoWithFile(id, title, description, isPublic, file);
        return ResponseEntity.ok(videoMapper.toDto(video));
    }

    // Endpoint PUT per aggiornare solo i metadati (senza file)
    @PutMapping("/{id}")
    public ResponseEntity<VideoOutputDto> updateVideo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        Video video = videoService.updateVideo(id, updates);
        return ResponseEntity.ok(videoMapper.toDto(video));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> getVideoFile(@PathVariable Long id) throws IOException {
        byte[] content = videoService.getVideoFile(id);
        String contentType = videoService.getVideoContentType(id);
        Video video = videoService.getVideoById(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + video.getFilePath() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(content);
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<byte[]> streamVideo(@PathVariable Long id) throws IOException {
        byte[] content = videoService.getVideoFile(id);
        String contentType = videoService.getVideoContentType(id);
        Video video = videoService.getVideoById(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + video.getFilePath() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(content);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadVideo(@PathVariable Long id) throws IOException {
        byte[] content = videoService.downloadVideoFile(id);
        String contentType = videoService.getVideoContentType(id);
        String downloadFileName = videoService.getVideoFileName(id);

        // Per il download, imposta content-type come application/octet-stream
        // per forzare il download invece della visualizzazione nel browser
        String downloadContentType = "application/octet-stream";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName + "\"")
                .contentType(MediaType.parseMediaType(downloadContentType))
                .contentLength(content.length)
                .body(content);
    }
}
package com.example.videoapp.service;

import com.example.videoapp.dto.NuovoVideoInputDto;
import com.example.videoapp.dto.VideoOutputDto;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface VideoService {

    ResponseEntity<Page<VideoOutputDto>> getVideos(int page, int size, Long userId, Boolean isPublic);

    ResponseEntity<VideoOutputDto> getVideoById(Long id);

    ResponseEntity<String> uploadVideo(NuovoVideoInputDto dto, MultipartFile file) throws IOException;

    ResponseEntity<Void> deleteVideo(Long id);

    ResponseEntity<byte[]> getVideoFile(Long id) throws IOException;

    ResponseEntity<VideoOutputDto> updateVideo(Long id, Map<String, Object> updates);

    ResponseEntity<VideoOutputDto> updateVideoWithFile(Long id, String title, String description, boolean isPublic, MultipartFile file) throws IOException;

    ResponseEntity<byte[]> downloadVideoFile(Long id) throws IOException;
}

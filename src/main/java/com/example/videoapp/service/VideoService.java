package com.example.videoapp.service;

import com.example.videoapp.dto.NuovoVideoInputDto;
import com.example.videoapp.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface VideoService {

    Page<Video> getVideos(int page, int size, Long userId, Boolean isPublic);

    Video getVideoById(Long id);

    Video uploadVideo(NuovoVideoInputDto dto, MultipartFile file) throws IOException;

    void deleteVideo(Long id);

    byte[] getVideoFile(Long id) throws IOException;

    Video updateVideo(Long id, Map<String, Object> updates);

    Video updateVideoWithFile(Long id, String title, String description, boolean isPublic, MultipartFile file) throws IOException;

    byte[] downloadVideoFile(Long id) throws IOException;
    
    String getVideoContentType(Long id) throws IOException;
    
    String getVideoFileName(Long id);
}
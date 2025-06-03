// INIZIO VideoRepository.java
package com.example.videoapp.repository;

import com.example.videoapp.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Page<Video> findByIsPublic(Boolean isPublic, Pageable pageable);
    Page<Video> findByUserId(Long userId, Pageable pageable);  
    Page<Video> findByUserIdAndIsPublic(Long userId, Boolean isPublic, Pageable pageable);  
}
// FINE VideoRepository.java
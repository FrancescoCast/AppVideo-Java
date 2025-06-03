// INIZIO VideoConverter.java
package com.example.videoapp.converter;

import com.example.videoapp.dto.VideoOutputDto;
import com.example.videoapp.model.Video;
import org.springframework.stereotype.Component;

@Component
public class VideoConverter {

    public VideoOutputDto toDto(Video video) {
        VideoOutputDto dto = new VideoOutputDto();
        dto.setId(video.getId());
        dto.setTitle(video.getTitle());
        dto.setDescription(video.getDescription());
        dto.setPublic(video.isPublic());

        VideoOutputDto.UserDto userDto = new VideoOutputDto.UserDto();  
        userDto.setUserId(video.getUser().getId());  
        userDto.setUsername(video.getUser().getUsername());  

        dto.setUser(userDto);  
        return dto;
    }
}
// FINE VideoConverter.java
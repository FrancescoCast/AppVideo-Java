package com.example.videoapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "videoapp.storage")
public class StorageConfiguration {
    
    private String uploadDir;
    private long maxFileSize = 1073741824L; // 1GB in bytes
    private String allowedExtensions = "mp4,avi,mov,wmv,flv,webm,mkv";
    
    public String getUploadDir() {
        return uploadDir;
    }
    
    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
    
    public long getMaxFileSize() {
        return maxFileSize;
    }
    
    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
    
    public String getAllowedExtensions() {
        return allowedExtensions;
    }
    
    public void setAllowedExtensions(String allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }
}
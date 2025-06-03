// INIZIO VideoappApplication.java
package com.example.videoapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class VideoappApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoappApplication.class, args);
    }

    @Bean
    CommandLineRunner init(@Value("${videoapp.upload.dir}") String uploadDir) {
        return args -> {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created upload directory: " + uploadPath);
            }
            System.out.println("Upload directory: " + uploadPath.toAbsolutePath());
        };
    }
}
// FINE VideoappApplication.java
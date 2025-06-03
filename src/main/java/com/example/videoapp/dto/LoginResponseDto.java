package com.example.videoapp.dto;

public class LoginResponseDto {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;

    public LoginResponseDto(String token, Long id, String username) {
        this.token = token;
        this.id = id;
        this.username = username;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
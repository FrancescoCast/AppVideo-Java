package com.example.videoapp.dto;

public class AuthResponse {
    private String token;

    public AuthResponse(String token) {
        this.token = token;
    }

    // Aggiungi getter e setter se necessario
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
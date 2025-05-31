package com.carrental.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AuthResponseDto {
    private UUID userId;
    private String token;

    public AuthResponseDto(UUID userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    // Геттеры и сеттеры
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
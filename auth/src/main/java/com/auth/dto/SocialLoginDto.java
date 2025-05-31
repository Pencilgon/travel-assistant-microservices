package com.auth.dto;

public class SocialLoginDto {
    private String id_token;
    // + возможно device_id, push_token и т.д.
    public String getId_token() { return id_token; }
    public void setId_token(String id_token) { this.id_token = id_token; }
}

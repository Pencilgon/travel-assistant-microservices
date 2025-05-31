package com.carrental.dto;

public class AuthDto {
    private String username;
    private String password;
    private String email;
    private String first_name;
    private String last_name;
    private String date_of_birth;
    private String phone_number;
    private String preferred_language;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public String getPreferred_language() {
        return preferred_language;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }
}

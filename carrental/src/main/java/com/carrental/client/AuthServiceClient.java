package com.carrental.client;

import com.carrental.dto.UserProfileDto;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
public class AuthServiceClient {
    private final WebClient client;

    public AuthServiceClient(WebClient.Builder builder) {
        this.client = builder
                .baseUrl("lb://auth-service")
                .build();
    }

    public UserProfileDto getUserProfile(UUID userId, String token) {
        return client.get()
                .uri("/auth/userData/{id}", userId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(UserProfileDto.class)
                .block();
    }
}

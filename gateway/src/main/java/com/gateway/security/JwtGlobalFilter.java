package com.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.*;

@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtGlobalFilter.class);

    private final WebClient.Builder webClientBuilder;

    private static final String AUTH_URL = "lb://auth-service";

    private static final List<String> WHITELIST = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/auth/social-login",
            "/rental/auth/login",
            "/rental/auth/register",
            "/auth/username"
    );

    private static final List<String> RENTAL_OLD_KEY_ENDPOINTS = Arrays.asList(
            "/rental/getall/cars",
            "/rental/get",
            "/rental/my-requests",
            "/rental/request",
            "/rental/locations/countries",
            "/rental/locations/cities"
    );

    private static final String OLD_SECRET = "32j3vh4j2b3hj423fgc4f2g3c42k3nj4kj23k4bh32gv4g2c3f42vh3j42n34vg23h4";
    private static final String NEW_SECRET = "fh378fh3fh82367g2ff9859065j8g94h758gjhgkhhui56773gds5d6";

    private static final Map<String, String> LANGUAGE_MAP = Map.of(
            "English", "en",
            "Russian", "ru",
            "German", "de",
            "Spanish", "es",
            "Kazakh", "kk"
    );

    public JwtGlobalFilter(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    private Key getSigningKey(String secret) {
        byte[] key = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(key);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("🚀 JwtGlobalFilter executing...");
        String requestPath = exchange.getRequest().getURI().getPath();
        logger.info("📌 Incoming request path: {}", requestPath);

        if (WHITELIST.contains(requestPath)) {
            logger.info("✅ Whitelisted path. Skipping auth: {}", requestPath);
            return chain.filter(exchange);
        }

        boolean isRentalOld = RENTAL_OLD_KEY_ENDPOINTS.stream().anyMatch(requestPath::startsWith);
        boolean isRental = requestPath.startsWith("/rental/");

        String jwtSecretToUse;
        if (isRentalOld) {
            jwtSecretToUse = OLD_SECRET;
        } else if (isRental) {
            jwtSecretToUse = NEW_SECRET;
        } else {
            jwtSecretToUse = OLD_SECRET;
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        logger.info("🛂 Authorization Header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("❌ Missing or malformed Authorization header.");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(jwtSecretToUse))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            logger.info("✅ JWT validated for user: {}", username);

            return webClientBuilder.build()
                    .get()
                    .uri(AUTH_URL + "/auth/username/{username}/preferred-language", username)
                    .retrieve()
                    .bodyToMono(String.class)
                    .defaultIfEmpty("English")
                    .map(langName -> LANGUAGE_MAP.getOrDefault(langName, "en"))
                    .flatMap(langCode -> {
                        ServerWebExchange mutatedExchange = exchange.mutate().request(
                                exchange.getRequest().mutate()
                                        .header("X-User-Id", username)
                                        .header("X-User-Language", langCode)
                                        .build()
                        ).build();
                        return chain.filter(mutatedExchange);
                    });

        } catch (Exception e) {
            logger.error("❌ JWT validation failed: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

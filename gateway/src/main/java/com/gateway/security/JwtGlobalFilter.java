package com.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;
import java.util.Map;

@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(JwtGlobalFilter.class);
    private final WebClient.Builder webClientBuilder;

    private static final String AUTH_URL = "lb://auth-service";

    private static final List<String> WHITELIST = List.of(
            "/auth/user/login",
            "/auth/user/register",
            "/auth/owner/login",
            "/auth/owner/register",
            "/auth/user/social-login",
            "/auth/owner/social-login",
            "/auth/username/"
    );

    private static final List<String> OWNER_ENDPOINT_PREFIXES = List.of(
            "/rental/add/cars",
            "/rental/owner/cars",
            "/rental/delete/car",
            "/rental/update/car",
            "/rental/owner-requests",
            "/rental/owner/request"
    );

    private static final List<String> USER_ENDPOINT_PREFIXES = List.of(
            "/rental/get/cars",
            "/rental/locations",
            "/rental/user/request",
            "/rental/my-requests"
    );

    private static final Map<String, String> LANGUAGE_MAP = Map.of(
            "English", "en",
            "Russian", "ru",
            "German", "de",
            "Spanish", "es",
            "Kazakh", "kk"
    );

    private final String jwtSecret = "32j3vh4j2b3hj423fgc4f2g3c42k3nj4kj23k4bh32gv4g2c3f42vh3j42n34vg23h4";

    public JwtGlobalFilter(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean pathStartsWithAny(String path, List<String> prefixes) {
        return prefixes.stream().anyMatch(path::startsWith);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        logger.info("➡ Incoming request: {}", path);

        if (WHITELIST.stream().anyMatch(path::startsWith)) {
            logger.info("✅ Public endpoint, skipping auth check.");
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("❌ Missing or malformed Authorization header.");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("❌ JWT validation failed: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String username = claims.getSubject();
        String userId = claims.get("userId", String.class);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        logger.info("🔐 Authenticated as: {}, Roles: {}", username, roles);

        if (pathStartsWithAny(path, OWNER_ENDPOINT_PREFIXES)) {
            if (roles == null || !roles.contains("OWNER")) {
                logger.warn("🚫 Access denied: OWNER role required.");
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            logger.info("✅ OWNER access granted.");
        } else if (pathStartsWithAny(path, USER_ENDPOINT_PREFIXES)) {
            if (roles == null || !roles.contains("USER")) {
                logger.warn("🚫 Access denied: USER role required.");
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            logger.info("✅ USER access granted.");
        }

        // Получение языка
        return webClientBuilder.build()
                .get()
                .uri(AUTH_URL + "/auth/username/{username}/preferred-language", username)
                .retrieve()
                .bodyToMono(String.class)
                .defaultIfEmpty("English")
                .map(langName -> LANGUAGE_MAP.getOrDefault(langName, "en"))
                .flatMap(langCode -> {
                    logger.info("🌐 Setting headers: X-User-Id={}, X-Username={}, X-User-Language={}, Using token={}", userId, username, langCode, token);
                    var mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-Username", username)
                            .header("X-User-Language", langCode)
                            .header("Authorization", "Bearer " + token)
                            .build();
                    var mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                    return chain.filter(mutatedExchange);
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

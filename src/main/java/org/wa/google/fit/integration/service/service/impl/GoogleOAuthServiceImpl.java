package org.wa.google.fit.integration.service.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wa.google.fit.integration.service.service.GoogleOAuthService;
import org.wa.google.fit.integration.service.service.InMemoryTokenStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements GoogleOAuthService {

    private final InMemoryTokenStorageService tokenStorage;
    private final WebClient webClient = WebClient.create("https://oauth2.googleapis.com");

    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;

    public Mono<Map<String, String>> exchangeCodeForTokens(String code, String email) {
        String redirectUri = "http://localhost:8080/v1/oauth/callback";

        return webClient.post()
                .uri("/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("code", code)
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("redirect_uri", redirectUri)
                        .with("grant_type", "authorization_code"))
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> Mono.error(new RuntimeException("Failed to exchange code for tokens: " + clientResponse.statusCode() + " - " + errorBody)))
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .map(tokens -> {
                    String refreshToken = tokens.get("refresh_token");
                    if (email != null && refreshToken != null) {
                        tokenStorage.saveToken(email, refreshToken);
                    }
                    return tokens;
                });
    }

    public Mono<Map<String, String>> refreshAccessToken(String email) {
        String refreshToken = tokenStorage.getUserToken(email);
        if (refreshToken == null) {
            return Mono.error(new RuntimeException("No refresh token found for email: " + email));
        }

        String scopes = "https://www.googleapis.com/auth/fitness.activity.read " +
                "https://www.googleapis.com/auth/fitness.location.read " +
                "https://www.googleapis.com/auth/fitness.body.read";

        return webClient.post()
                .uri("/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("refresh_token", refreshToken)
                        .with("grant_type", "refresh_token")
                        .with("scope", scopes))
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> Mono.error(new RuntimeException("Failed to refresh token: " + clientResponse.statusCode() + " - " + errorBody)))
                )
                .bodyToMono(String.class)
                .map(this::parseJson);
    }

    private Map<String, String> parseJson(String json) {
        try {
            return new ObjectMapper().readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token response", e);
        }
    }
}

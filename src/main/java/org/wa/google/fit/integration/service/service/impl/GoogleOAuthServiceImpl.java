package org.wa.google.fit.integration.service.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.wa.google.fit.integration.service.constants.OAuthConstants;
import org.wa.google.fit.integration.service.exception.ParseTokenException;
import org.wa.google.fit.integration.service.exception.TokenNotFoundException;
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

import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements GoogleOAuthService {
    @Value("${google-fit.googleapis-base-url}")
    private String googleApiBaseUrl;

    @Value("${google-fit.redirect-url}")
    private String redirectUrl;

    @Value("${google-fit.token-endpoint}")
    private String tokenEndpoint;

    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;

    private final InMemoryTokenStorageService tokenStorage;
    private final OAuthConstants constants;

    private WebClient webClient;

    @PostConstruct
    private void init() {
        this.webClient = WebClient.builder()
                .baseUrl(googleApiBaseUrl)
                .build();
    }

    public Mono<Map<String, String>> exchangeCodeForTokens(String code) {
        return webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("code", code)
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("redirect_uri", redirectUrl)
                        .with("grant_type", "authorization_code"))
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> Mono.error(new ParseTokenException("Не удалось прочитать токен")))
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .map(tokens -> {
                    String refreshToken = tokens.get("refresh_token");
                    String idToken = tokens.get("id_token");
                    String email = extractEmailFromIdToken(idToken);
                    if (email != null && refreshToken != null) {
                        tokenStorage.saveToken(email, refreshToken);
                    }
                    return tokens;
                });
    }

    private String extractEmailFromIdToken(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) throw new IllegalArgumentException("Invalid id_token");

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode payload = mapper.readTree(payloadJson);

            return payload.get("email").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse id_token", e);
        }
    }

    public Mono<Map<String, String>> refreshAccessToken(String email) {
        return Mono.fromCallable(() -> tokenStorage.getUserToken(email))
                .switchIfEmpty(Mono.error(new TokenNotFoundException("Не найден токен для email: " + email)))
                .flatMap(refreshToken -> webClient.post()
                        .uri(tokenEndpoint)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(BodyInserters.fromFormData("client_id", clientId)
                                .with("client_secret", clientSecret)
                                .with("refresh_token", refreshToken)
                                .with("grant_type", "refresh_token")
                                .with("scope", constants.getScope()))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, clientResponse ->
                                clientResponse.bodyToMono(String.class).flatMap(errorBody ->
                                        Mono.error(new ParseTokenException("Не удалось обновить токен: " + clientResponse.statusCode() + " - " + errorBody)))
                        )
                        .bodyToMono(String.class)
                        .map(this::parseJson));
    }

    private Map<String, String> parseJson(String json) {
        try {
            return new ObjectMapper().readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new ParseTokenException("Не удалось прочитать токен");
        }
    }
}

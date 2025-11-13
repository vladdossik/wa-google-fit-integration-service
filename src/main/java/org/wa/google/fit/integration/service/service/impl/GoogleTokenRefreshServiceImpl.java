package org.wa.google.fit.integration.service.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wa.google.fit.integration.service.service.GoogleTokenRefreshService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class GoogleTokenRefreshServiceImpl implements GoogleTokenRefreshService {

    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://oauth2.googleapis.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();

    public Mono<String> refreshAccessToken(String refreshToken) {
        String scopes = "https://www.googleapis.com/auth/fitness.activity.read " +
                "https://www.googleapis.com/auth/fitness.location.read " +
                "https://www.googleapis.com/auth/fitness.body.read " +
                "https://www.googleapis.com/auth/fitness.heart_rate.read " +
                "https://www.googleapis.com/auth/fitness.sleep.read";

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
                .map(json -> {
                    try {
                        Map<String, Object> map = new ObjectMapper().readValue(json, new TypeReference<>() {
                        });
                        String accessToken = (String) map.get("access_token");
                        if (accessToken == null) {
                            throw new RuntimeException("No access_token in response: " + json);
                        }
                        return accessToken;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse access token from response: " + json, e);
                    }
                });
    }

}

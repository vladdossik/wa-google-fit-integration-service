package org.wa.google.fit.integration.service.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.wa.google.fit.integration.service.constants.OAuthConstants;
import org.wa.google.fit.integration.service.exception.ParseTokenException;
import org.wa.google.fit.integration.service.exception.TokenNotFoundException;
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
@RequiredArgsConstructor
public class GoogleTokenRefreshServiceImpl implements GoogleTokenRefreshService {

    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${google-fit.token-endpoint}")
    private String tokenEndpoint;

    @Value("${google-fit.googleapis-base-url}")
    private String googleApiBaseUrl;

    private final OAuthConstants constants;

    private WebClient webClient;

    @PostConstruct
    private void init() {
        this.webClient = WebClient.builder()
                .baseUrl(googleApiBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    public Mono<String> refreshAccessToken(String refreshToken) {
        return webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("refresh_token", refreshToken)
                        .with("grant_type", "refresh_token")
                        .with("scope", constants.getScope()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> Mono.error(new ParseTokenException("Не удалось обновить токен: " + clientResponse.statusCode() + " - " + errorBody)))
                )
                .bodyToMono(String.class)
                .map(json -> {
                    try {
                        Map<String, Object> map = new ObjectMapper().readValue(json, new TypeReference<>() {
                        });
                        String accessToken = (String) map.get("access_token");
                        if (accessToken == null) {
                            throw new TokenNotFoundException("В ответе нет нужного токена: " + json);
                        }
                        return accessToken;
                    } catch (Exception e) {
                        throw new ParseTokenException("Не удалось прочитать токен: " + json);
                    }
                });
    }

}

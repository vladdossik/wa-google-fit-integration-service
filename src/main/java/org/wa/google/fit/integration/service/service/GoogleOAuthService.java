package org.wa.google.fit.integration.service.service;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface GoogleOAuthService {
    Mono<Map<String, String>> exchangeCodeForTokens(String code, String email);
    Mono<Map<String, String>> refreshAccessToken(String email);
}

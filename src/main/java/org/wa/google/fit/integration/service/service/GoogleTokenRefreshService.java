package org.wa.google.fit.integration.service.service;

import reactor.core.publisher.Mono;

public interface GoogleTokenRefreshService {
    Mono<String> refreshAccessToken(String refreshToken);
}

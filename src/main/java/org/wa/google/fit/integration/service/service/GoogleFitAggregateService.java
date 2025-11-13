package org.wa.google.fit.integration.service.service;

import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public interface GoogleFitAggregateService {
    Mono<String> aggregate(String accessToken, OffsetDateTime date, String... dataTypes);
    Mono<String> getSleepSessions(String accessToken, OffsetDateTime date, OffsetDateTime end);
}

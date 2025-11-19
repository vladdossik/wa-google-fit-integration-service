package org.wa.google.fit.integration.service.service.impl;

import org.wa.google.fit.integration.service.client.GoogleFitClient;
import org.wa.google.fit.integration.service.service.GoogleFitAggregateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class GoogleFitAggregateServiceImpl implements GoogleFitAggregateService {

    private final GoogleFitClient client;

    public Mono<String> aggregate(String accessToken, OffsetDateTime date, String... dataTypes) {
        return client.aggregate(accessToken, date, dataTypes);
    }

    public Mono<String> getSleepSessions(String accessToken, OffsetDateTime start, OffsetDateTime end) {
        return client.getSleepSession(accessToken, start, end);
    }
}

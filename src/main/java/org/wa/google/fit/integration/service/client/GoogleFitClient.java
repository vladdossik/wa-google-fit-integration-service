package org.wa.google.fit.integration.service.client;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.wa.google.fit.integration.service.dto.AggregateByDto;
import org.wa.google.fit.integration.service.dto.AggregateRequestDto;
import org.wa.google.fit.integration.service.dto.BucketByTimeDto;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GoogleFitClient {

    private final WebClient googleFitWebClient;

    @Value("${google-fit.aggregate-endpoint}")
    private String aggregateEndpoint;

    @Value("${google-fit.sessions-endpoint}")
    private String sessionsEndpoint;

    @Value("${google-fit.sleep-activity-type}")
    private int sleepActivityType;

    @Retry(name = "googleFitApi")
    public Mono<String> aggregate(String accessToken, OffsetDateTime date, String... dataTypes) {
        long startMillis = date.toInstant().toEpochMilli();
        long endMillis = date.plusDays(1).toInstant().toEpochMilli() - 1;

        List<AggregateByDto> aggregateByList = Arrays.stream(dataTypes)
                .map(AggregateByDto::new)
                .toList();

        AggregateRequestDto body = new AggregateRequestDto(
                aggregateByList,
                new BucketByTimeDto(86400000),
                startMillis,
                endMillis
        );

        return googleFitWebClient.post()
                .uri(aggregateEndpoint)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToMono(String.class);
    }

    @Retry(name = "googleFitApi")
    public Mono<String> getSleepSession(String accessToken, OffsetDateTime start, OffsetDateTime end) {
        return googleFitWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(sessionsEndpoint)
                        .queryParam("startTime", start.toInstant().toString())
                        .queryParam("endTime", end.toInstant().toString())
                        .queryParam("activityType", sleepActivityType)
                        .build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToMono(String.class);
    }
}

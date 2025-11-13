package org.wa.google.fit.integration.service.client;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class GoogleFitClient {

    private final WebClient googleFitWebClient;

    @Retry(name = "googleFitApi")
    public Mono<String> aggregate(String accessToken, OffsetDateTime date, String... dataTypes) {
        long startMillis = date.toInstant().toEpochMilli();
        long endMillis = date.plusDays(1).toInstant().toEpochMilli() - 1;

        StringBuilder aggregateBy = new StringBuilder("[");
        for (int i = 0; i < dataTypes.length; i++) {
            aggregateBy.append("{\"dataTypeName\":\"").append(dataTypes[i]).append("\"}");
            if (i < dataTypes.length - 1) aggregateBy.append(",");
        }
        aggregateBy.append("]");

        String body = """
                    {
                      "aggregateBy": %s,
                      "bucketByTime": { "durationMillis": 86400000 },
                      "startTimeMillis": %d,
                      "endTimeMillis": %d
                    }
                """.formatted(aggregateBy, startMillis, endMillis);

        return googleFitWebClient.post()
                .uri("/dataset:aggregate")
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
                        .path("/sessions")
                        .queryParam("startTime", start.toInstant().toString())
                        .queryParam("endTime", end.toInstant().toString())
                        .queryParam("activityType", 72)
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

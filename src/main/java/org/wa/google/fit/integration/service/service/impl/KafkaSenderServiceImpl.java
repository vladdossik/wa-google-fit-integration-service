package org.wa.google.fit.integration.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.wa.google.fit.integration.service.dto.GoogleRefreshTokenEvent;
import org.wa.google.fit.integration.service.service.KafkaSenderService;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaSenderServiceImpl implements KafkaSenderService {
    @Value("${kafka.topics.google-refresh-token}")
    private String googleRefreshTokenTopic;

    private final KafkaTemplate<String, GoogleRefreshTokenEvent> kafkaTemplate;

    @Override
    public void sendRefreshTokenToKafka(String email, Map<String, String> tokens) {
        try {
            GoogleRefreshTokenEvent event = new GoogleRefreshTokenEvent(
                    email,
                    tokens.get("refresh_token"),
                    Instant.now().atOffset(ZoneOffset.UTC),
                    Integer.parseInt(tokens.get("expires_in"))
            );

            kafkaTemplate.send(googleRefreshTokenTopic, email, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Google refresh token sent to Kafka for user: {}", email);
                        } else {
                            log.error("Failed to send refresh token");
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to create or send Kafka event", e);
        }
    }
}

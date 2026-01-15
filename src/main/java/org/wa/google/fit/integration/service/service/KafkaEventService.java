package org.wa.google.fit.integration.service.service;

import java.util.Map;

public interface KafkaEventService {
    void sendRefreshTokenToKafka(String email, Map<String, String> tokens);
}

package org.wa.google.fit.integration.service.service;

import java.util.Map;

public interface KafkaSenderService {
    void sendRefreshTokenToKafka(String email, Map<String, String> tokens);
}

package org.wa.google.fit.integration.service.service.impl;

import org.wa.google.fit.integration.service.service.InMemoryTokenStorageService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTokenStorageServiceImpl implements InMemoryTokenStorageService {

    private final Map<String, String> userTokens = new ConcurrentHashMap<>();

    public void saveToken(String email, String token) {
        userTokens.put(email, token);
    }

    public String getUserToken(String email) {
        return userTokens.get(email);
    }
}

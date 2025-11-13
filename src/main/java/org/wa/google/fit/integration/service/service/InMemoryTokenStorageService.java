package org.wa.google.fit.integration.service.service;

public interface InMemoryTokenStorageService {
    void saveToken(String email, String token);
    String getUserToken(String email);
}

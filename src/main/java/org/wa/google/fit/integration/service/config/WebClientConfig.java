package org.wa.google.fit.integration.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.wa.google.fit.integration.service.exception.GoogleFitException;

@Configuration
public class WebClientConfig {

    @Value("${google-fit.base-url}")
    private String baseUrl;

    @Bean
    public WebClient googleFitWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter(ExchangeFilterFunctions.statusError(
                        HttpStatusCode::isError,
                        response -> new GoogleFitException("Ошибка Google Fit: " + response.statusCode())
                ))
                .build();
    }

}

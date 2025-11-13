package org.wa.google.fit.integration.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient googleFitWebClient() {
        return WebClient.builder()
                .baseUrl("https://www.googleapis.com/fitness/v1/users/me")
                .filter(ExchangeFilterFunctions.statusError(
                        HttpStatusCode::isError,
                        response -> new RuntimeException("Google Fit error:" + response.statusCode())
                ))
                .build();
    }

}

package org.wa.google.fit.integration.service.config;

import io.netty.channel.ChannelOption;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.wa.google.fit.integration.service.exception.GoogleFitException;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;


@Configuration
@ConfigurationProperties(prefix = "integration.google-fit")
@Getter
@Setter
public class WebClientConfig {
    private String baseUrl;
    private int timeout;

    @Bean
    public WebClient googleFitWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(timeout))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout);

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(ExchangeFilterFunctions.statusError(
                        HttpStatusCode::isError,
                        response -> new GoogleFitException("Ошибка Google Fit: " + response.statusCode())
                ))
                .build();
    }
}

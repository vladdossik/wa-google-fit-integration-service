package org.wa.google.fit.integration.service.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "google-fit")
@Getter
@Setter
public class OAuthConstants {
    private String redirectUrl;
    private String authorizeBaseUrl;
    private String scope;

    private final String clientId = "67562261385-2eq7md7aerocv77i6059q16mg9caeso8.apps.googleusercontent.com";

    public String getAuthorizeUrl() {
        return authorizeBaseUrl
                + "?client_id=" + clientId
                + "&redirect_uri=" + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&access_type=offline"
                + "&prompt=consent"
                + "&scope=" + URLEncoder.encode(scope.replace("\n", " "), StandardCharsets.UTF_8);
    }
}

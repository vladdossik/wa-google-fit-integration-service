package org.wa.google.fit.integration.service.controller;

import org.wa.google.fit.integration.service.service.GoogleOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/v1/oauth")
@RequiredArgsConstructor
public class GoogleOAuthController {

    private final GoogleOAuthService googleOAuthService;

    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize() {
        String clientId = "67562261385-2eq7md7aerocv77i6059q16mg9caeso8.apps.googleusercontent.com";
        String redirectUri = "http://localhost:8080/v1/oauth/callback";
        String scope = "https://www.googleapis.com/auth/fitness.activity.read " +
                "https://www.googleapis.com/auth/fitness.location.read " +
                "https://www.googleapis.com/auth/fitness.body.read " +
                "https://www.googleapis.com/auth/fitness.heart_rate.read " +
                "https://www.googleapis.com/auth/fitness.sleep.read";

        String url = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&access_type=offline"
                + "&prompt=consent"
                + "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }

    @GetMapping("/callback")
    public Mono<ResponseEntity<Map<String, String>>> handleGoogleCallback(@RequestParam String code, @RequestParam(required = false) String email) {
        return googleOAuthService.exchangeCodeForTokens(code, email)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(Map.of("error", e.getMessage()))));
    }

    @GetMapping("/refresh")
    public Mono<ResponseEntity<Map<String, String>>> refreshToken(
            @RequestParam String email
    ) {
        return googleOAuthService.refreshAccessToken(email)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body(Map.of("error", e.getMessage()))));
    }

    @GetMapping("/me")
    public Mono<Object> me(@AuthenticationPrincipal OidcUser user) {
        return Mono.justOrEmpty(user == null ? "Not logged in" : user.getClaims());
    }

    @GetMapping("/token/google")
    public Mono<Object> getGoogleToken(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client) {
        if (client == null) {
            return Mono.just("Not authorized");
        }
        return Mono.just("Access Token: " + client.getAccessToken().getTokenValue());
    }
}

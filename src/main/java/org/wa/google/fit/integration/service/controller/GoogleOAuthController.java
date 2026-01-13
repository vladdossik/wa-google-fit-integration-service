package org.wa.google.fit.integration.service.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.wa.google.fit.integration.service.constants.OAuthConstants;
import org.wa.google.fit.integration.service.dto.GoogleAccessTokenResponse;
import org.wa.google.fit.integration.service.dto.GoogleUserResponse;
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

import java.util.Map;

@RestController
@RequestMapping("/v1/oauth")
@RequiredArgsConstructor
@Tag(name = "Google OAuth", description = "Работа с Google OAuth авторизацией")
public class GoogleOAuthController {
    private final GoogleOAuthService googleOAuthService;
    private final OAuthConstants constants;

    @Operation(
            summary = "Авторизация в Google OAuth (перенаправляет на форму google)"
    )
    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, constants.getAuthorizeUrl())
                .build();
    }

    @Hidden
    @GetMapping("/callback")
    public Mono<ResponseEntity<Map<String, String>>> handleGoogleCallback(
            @RequestParam String code) {
        return googleOAuthService.exchangeCodeForTokens(code)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body(Map.of("error", e.getMessage()))));
    }

    @Hidden
    @GetMapping("/refresh")
    public Mono<ResponseEntity<Map<String, String>>> refreshToken(
            @RequestParam String email
    ) {
        return googleOAuthService.refreshAccessToken(email)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body(Map.of("error", e.getMessage()))));
    }

    @Hidden
    @GetMapping("/me")
    public Mono<GoogleUserResponse> me(@AuthenticationPrincipal OidcUser user) {
        return Mono.just(new GoogleUserResponse(
                user == null ? Map.of() : user.getClaims()
        ));
    }

    @Hidden
    @GetMapping("/token/google")
    public Mono<GoogleAccessTokenResponse> getGoogleToken(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client) {
        if (client == null) {
            return Mono.just(new GoogleAccessTokenResponse(null));
        }
        return Mono.just(new GoogleAccessTokenResponse(
                client.getAccessToken().getTokenValue()
        ));
    }
}

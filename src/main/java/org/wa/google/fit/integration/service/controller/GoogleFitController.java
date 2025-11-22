package org.wa.google.fit.integration.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.wa.google.fit.integration.service.model.ActivityDataResponse;
import org.wa.google.fit.integration.service.model.HeartRateDataResponse;
import org.wa.google.fit.integration.service.model.SleepDataResponse;
import org.wa.google.fit.integration.service.service.GoogleFitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/v1/google-fit")
@RequiredArgsConstructor
@Tag(name = "Google Fit", description = "Получение данных пользователя из приложения Google Fit")
public class GoogleFitController {
    private final GoogleFitService googleFitService;

    @Operation(
            summary = "Получить данные об активности",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/activity")
    public Mono<ActivityDataResponse> getDailyActivity(
            @RequestParam String email,
            @RequestParam String refreshToken,
            @RequestParam OffsetDateTime date) {
        return googleFitService.getActivity(email, date, refreshToken);
    }

    @Operation(
            summary = "Получить данные о сне",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/sleep")
    public Mono<SleepDataResponse> getSleep(
            @RequestParam String email,
            @RequestParam String refreshToken,
            @RequestParam OffsetDateTime date
    ) {
        return googleFitService.getSleep(email, date, refreshToken);
    }

    @Operation(
            summary = "Получить данные о сердечном ритме",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/heart-rate")
    public Mono<HeartRateDataResponse> getHeartRate(
            @RequestParam String email,
            @RequestParam String refreshToken,
            @RequestParam OffsetDateTime date
    ) {
        return googleFitService.getHeartRate(email, date, refreshToken);
    }
}

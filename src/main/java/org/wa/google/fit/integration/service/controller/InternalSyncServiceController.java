package org.wa.google.fit.integration.service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wa.google.fit.integration.service.model.ActivityDataResponse;
import org.wa.google.fit.integration.service.model.HeartRateDataResponse;
import org.wa.google.fit.integration.service.model.SleepDataResponse;
import org.wa.google.fit.integration.service.service.GoogleFitService;
import reactor.core.publisher.Mono;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/v1/internal/google-fit/sync")
@RequiredArgsConstructor
public class InternalSyncServiceController {
    private final GoogleFitService googleFitService;

    @GetMapping("/activity")
    public Mono<ActivityDataResponse> getActivity(
            @RequestParam String email,
            @RequestParam String refreshToken,
            @RequestParam OffsetDateTime date) {
        return googleFitService.getActivity(email, date, refreshToken);
    }

    @GetMapping("/sleep")
    public Mono<SleepDataResponse> getSleep(
            @RequestParam String email,
            @RequestParam String refreshToken,
            @RequestParam OffsetDateTime date
    ) {
        return googleFitService.getSleep(email, date, refreshToken);
    }

    @GetMapping("/heart-rate")
    public Mono<HeartRateDataResponse> getHeartRate(
            @RequestParam String email,
            @RequestParam String refreshToken,
            @RequestParam OffsetDateTime date
    ) {
        return googleFitService.getHeartRate(email, date, refreshToken);
    }
}

package org.wa.google.fit.integration.service.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.wa.google.fit.integration.service.mapper.ActivityDataMapper;
import org.wa.google.fit.integration.service.mapper.GoogleJsonMapper;
import org.wa.google.fit.integration.service.mapper.HeartRateDataMapper;
import org.wa.google.fit.integration.service.mapper.SleepDataMapper;
import org.wa.google.fit.integration.service.model.ActivityDataResponse;
import org.wa.google.fit.integration.service.model.HeartRateDataResponse;
import org.wa.google.fit.integration.service.model.SleepDataResponse;
import org.wa.google.fit.integration.service.service.GoogleFitAggregateService;
import org.wa.google.fit.integration.service.service.GoogleFitService;
import org.wa.google.fit.integration.service.service.GoogleTokenRefreshService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class GoogleFitServiceImpl implements GoogleFitService {

    private final GoogleJsonMapper jsonMapper;
    private final GoogleTokenRefreshService googleTokenRefreshService;
    private final GoogleFitAggregateService aggregateService;

    private final ActivityDataMapper activityDataMapper;
    private final SleepDataMapper sleepDataMapper;
    private final HeartRateDataMapper heartRateDataMapper;

    @Value("${google-fit.data-type-steps}")
    private String dataTypeSteps;

    @Value("${google-fit.data-type-calories}")
    private String dataTypeCalories;

    @Value("${google-fit.data-type-distance}")
    private String dataTypeDistance;

    public Mono<ActivityDataResponse> getActivity(String email, OffsetDateTime date, String refreshToken) {
        return googleTokenRefreshService.refreshAccessToken(refreshToken)
                .flatMap(token -> aggregateService.aggregate(token, date,
                        dataTypeSteps, dataTypeCalories, dataTypeDistance
                )).map(json -> activityDataMapper.toResponse(email, jsonMapper.extractSteps(json),
                        jsonMapper.extractDistance(json),
                        jsonMapper.extractCalories(json),
                        date
                ));
    }

    public Mono<SleepDataResponse> getSleep(String email, OffsetDateTime date, String refreshToken) {
        OffsetDateTime start = date.minusDays(1);

        return googleTokenRefreshService.refreshAccessToken(refreshToken)
                .flatMap(token -> aggregateService.getSleepSessions(token, start, date))
                .map(json -> {
                    double totalHours = jsonMapper.extractTotalSleepHoursFromSessions(json);

                    return sleepDataMapper.toResponse(
                            email,
                            totalHours,
                            date
                    );
                });
    }

    public Mono<HeartRateDataResponse> getHeartRate(String email, OffsetDateTime date, String refreshToken) {
        return googleTokenRefreshService.refreshAccessToken(refreshToken)
                .flatMap(token -> aggregateService.aggregate(token, date, "com.google.heart_rate.bpm"))
                .map(json -> heartRateDataMapper.toResponse(
                        email,
                        jsonMapper.extractAverageBpm(json),
                        jsonMapper.extractRestingBpm(json),
                        jsonMapper.extractHeartRateSeries(json),
                        date
                ));
    }
}

package org.wa.google.fit.integration.service.service;

import org.wa.google.fit.integration.service.model.ActivityDataResponse;
import org.wa.google.fit.integration.service.model.HeartRateDataResponse;
import org.wa.google.fit.integration.service.model.SleepDataResponse;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public interface GoogleFitService {
    Mono<ActivityDataResponse> getActivity(String email, OffsetDateTime date, String refreshToken);
    Mono<SleepDataResponse> getSleep(String email, OffsetDateTime date, String refreshToken);
    Mono<HeartRateDataResponse> getHeartRate(String email, OffsetDateTime date, String refreshToken);
}

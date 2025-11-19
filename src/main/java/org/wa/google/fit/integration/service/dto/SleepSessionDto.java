package org.wa.google.fit.integration.service.dto;

import lombok.Data;

@Data
public class SleepSessionDto {
    private Integer activityType;
    private String startTimeMillis;
    private String endTimeMillis;
}

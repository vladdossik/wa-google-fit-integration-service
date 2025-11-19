package org.wa.google.fit.integration.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class SleepSessionsResponseDto {
    private List<SleepSessionDto> session;
}

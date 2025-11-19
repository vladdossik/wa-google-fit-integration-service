package org.wa.google.fit.integration.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregateRequestDto {
    private List<AggregateByDto> aggregateBy;
    private BucketByTimeDto bucketByTime;
    private long startTimeMillis;
    private long endTimeMillis;
}

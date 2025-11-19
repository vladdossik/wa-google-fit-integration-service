package org.wa.google.fit.integration.service.mapper;

import org.wa.google.fit.integration.service.model.HeartRateDataResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper(componentModel = "spring", imports = OffsetDateTime.class)
public interface HeartRateDataMapper {
    @Mapping(target = "date", expression = "java(dateUtc)")
    HeartRateDataResponse toResponse(
            String email,
            double averageBpm,
            double restingBpm,
            List<Double> bpmSeries,
            OffsetDateTime dateUtc);
}

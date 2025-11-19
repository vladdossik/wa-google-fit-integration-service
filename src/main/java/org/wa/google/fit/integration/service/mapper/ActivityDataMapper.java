package org.wa.google.fit.integration.service.mapper;

import org.wa.google.fit.integration.service.model.ActivityDataResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring", imports = OffsetDateTime.class)
public interface ActivityDataMapper {
    @Mapping(target = "date", expression = "java(dateUtc)")
    ActivityDataResponse toResponse(
            String email,
            int steps,
            double distanceKm,
            double calories,
            OffsetDateTime dateUtc);
}

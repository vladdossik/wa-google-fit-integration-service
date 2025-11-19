package org.wa.google.fit.integration.service.mapper;

import org.wa.google.fit.integration.service.model.SleepDataResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring", imports = OffsetDateTime.class)
public interface SleepDataMapper {

    @Mapping(target = "email", source = "email")
    @Mapping(target = "totalSleepHours", source = "totalSleepHours")
    @Mapping(target = "date", source = "date")
    SleepDataResponse toResponse(
            String email,
            double totalSleepHours,
            OffsetDateTime date
    );
}

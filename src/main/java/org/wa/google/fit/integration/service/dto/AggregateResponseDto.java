package org.wa.google.fit.integration.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.wa.google.fit.integration.service.dto.jsonTree.BucketDto;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AggregateResponseDto {
    private List<BucketDto> bucket;
}

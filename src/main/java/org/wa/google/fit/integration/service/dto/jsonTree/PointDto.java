package org.wa.google.fit.integration.service.dto.jsonTree;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PointDto {
    private List<ValueDto> value;
}

package org.wa.google.fit.integration.service.dto.jsonTree;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValueDto {
    private Integer intVal;
    private Double fpVal;
}

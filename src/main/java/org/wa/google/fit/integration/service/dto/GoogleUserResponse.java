package org.wa.google.fit.integration.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserResponse {
    private Map<String, Object> claims;
}

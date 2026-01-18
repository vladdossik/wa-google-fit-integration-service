package org.wa.google.fit.integration.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleRefreshTokenEvent {
    private String email;
    private String refreshToken;
    private OffsetDateTime timestamp;
    private Integer expiresIn;
}

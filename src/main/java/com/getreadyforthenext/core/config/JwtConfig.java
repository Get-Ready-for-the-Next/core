package com.getreadyforthenext.core.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    @NotBlank
    private String accessSecret;

    @Min(1)
    private int accessExpirationTime;

    @NotBlank
    private String refreshSecret;

    @Min(1)
    private int refreshExpirationTime;
}
package com.example.emailreader.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "outlook")
public class ApplicationProperties {
    private String email;
    private String password;
    private String exchangeUrl;
}

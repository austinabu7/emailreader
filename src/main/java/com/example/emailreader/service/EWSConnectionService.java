package com.example.emailreader.service;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.credentials.ExchangeCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class EWSConnectionService {
    private static final Logger logger = LoggerFactory.getLogger(EWSConnectionService.class);

    @Value("${outlook.email}")
    private String email;

    @Value("${outlook.app-password}")
    private String appPassword;

    private ExchangeService exchangeService;

    @PostConstruct
    public void initializeConnection() {
        try {
            logger.info("Initializing EWS connection for Office 365: {}", email);

            // Create exchange service for Exchange 2016 (Office 365)
            exchangeService = new ExchangeService(ExchangeVersion.Exchange2016);

            // Set credentials with Basic Authentication
            exchangeService.setCredentials(new ExchangeCredentials() {
                @Override
                public String getAuthorizationHeader(String host) {
                    // Create Basic Auth header with Base64 encoding
                    String credentials = email + ":" + appPassword;
                    String encodedCredentials = Base64.getEncoder()
                            .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
                    return "Basic " + encodedCredentials;
                }
            });

            // Set Office 365 EWS endpoint
            exchangeService.setUrl(new URI("https://outlook.office365.com/EWS/Exchange.asmx"));

            logger.info("✓ EWS connection to Office 365 established successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize EWS connection", e);
            throw new RuntimeException("EWS initialization failed", e);
        }
    }

    public ExchangeService getExchangeService() {
        return exchangeService;
    }
}
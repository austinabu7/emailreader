package com.example.emailreader.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String clientId;
    private String clientSecret;
    private String tenantId;
}
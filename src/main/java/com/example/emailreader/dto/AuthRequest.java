package com.example.emailreader.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String token; // OAuth2 token from Azure AD
}

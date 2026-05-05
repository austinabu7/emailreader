package com.example.emailreader.controller;

import com.example.emailreader.dto.AuthRequest;
import com.example.emailreader.dto.EmailDTO;
import com.example.emailreader.service.OutlookEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    @Autowired
    private OutlookEmailService outlookEmailService;

    // Read from environment variables (application.properties)
    @Value("${azure.clientId}")
    private String clientId;

    @Value("${azure.clientSecret}")
    private String clientSecret;

    @Value("${azure.tenantId}")
    private String tenantId;

    // 1️⃣ Authenticate using credentials from environment variables
    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate() {
        try {
            outlookEmailService.authenticateWithClientSecret(clientId, clientSecret, tenantId);
            return ResponseEntity.ok("Authenticated with Microsoft Graph successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Authentication failed: " + e.getMessage());
        }
    }

    // 2️⃣ OR Authenticate with custom request body (if needed)
    @PostMapping("/authenticate-custom")
    public ResponseEntity<String> authenticateCustom(@RequestBody AuthRequest request) {
        try {
            outlookEmailService.authenticateWithClientSecret(
                    request.getClientId(),
                    request.getClientSecret(),
                    request.getTenantId()
            );
            return ResponseEntity.ok("Authenticated with Microsoft Graph successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Authentication failed: " + e.getMessage());
        }
    }

    // 3️⃣ Get inbox emails
    @GetMapping("/inbox")
    public ResponseEntity<List<EmailDTO>> getInboxEmails(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<EmailDTO> emails = outlookEmailService.getInboxEmails(limit);
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // 4️⃣ Search emails
    @GetMapping("/search")
    public ResponseEntity<List<EmailDTO>> searchEmails(@RequestParam String query) {
        try {
            List<EmailDTO> emails = outlookEmailService.searchEmails(query);
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
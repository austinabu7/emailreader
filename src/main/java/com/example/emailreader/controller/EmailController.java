package com.example.emailreader.controller;

import com.example.emailreader.dto.AuthRequest;
import com.example.emailreader.dto.EmailDTO;
import com.example.emailreader.service.OutlookEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    @Autowired
    private OutlookEmailService outlookEmailService;

    @PostMapping("/authenticate-basic")
    public ResponseEntity<String> authenticateBasic(@RequestBody AuthRequest request) {
        try {
            outlookEmailService.authenticateBasic(request.getEmail(), request.getPassword());
            return ResponseEntity.ok("Authenticated with EWS successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody AuthRequest request) {
        return authenticateBasic(request);
    }

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

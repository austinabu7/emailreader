package com.example.emailreader.controller;

import com.example.emailreader.dto.EmailDTO;
import com.example.emailreader.service.EmailService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/emails")
    public List<EmailDTO> getEmails() {
        return emailService.getAllEmails();
    }

    @GetMapping(value = "/emails/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EmailDTO> streamEmails() {
        return emailService.getNewEmailsStream();
    }
}

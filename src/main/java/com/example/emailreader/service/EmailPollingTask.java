package com.example.emailreader.service;

import com.example.emailreader.dto.EmailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailPollingTask {

    @Autowired
    private OutlookEmailService outlookEmailService;

    @Autowired
    private EmailFilterService emailFilterService;

    @Autowired
    private EmailBodyParser emailBodyParser;

    @Autowired
    private WebhookService webhookService;

    @Value("${polling.subject:Change banner}")
    private String targetSubject;

    private Instant lastPollingTime = Instant.now();

    // Run every 1 minute (60000 ms)
    @Scheduled(fixedRate = 60000)
    public void pollEmails() {
        System.out.println("🔄 Starting email polling at UTC: " + Instant.now());
        System.out.println("⏱️ Last polling time: " + lastPollingTime);

        try {
            // Fetch emails
            List<EmailDTO> allEmails = outlookEmailService.getInboxEmails(50);

            // ✅ FILTER BY TIME - Only emails received after last polling
            List<EmailDTO> emailsSinceLastPolling = allEmails.stream()
                    .filter(email -> {

                        Instant emailTime = email.getReceived().toInstant();

                        boolean isAfterLastPolling =
                                emailTime.isAfter(lastPollingTime);

                        System.out.println(
                                "📧 Email: " + email.getSubject() +
                                        " | UTC Time: " + emailTime +
                                        " | After last polling: " + isAfterLastPolling
                        );

                        return isAfterLastPolling;
                    })
                    .collect(Collectors.toList());

            System.out.println("📊 Total emails since last polling: " + emailsSinceLastPolling.size());

            // Filter emails by subject + from + to
            List<EmailDTO> filteredEmails = emailFilterService.filterEmails(
                    emailsSinceLastPolling,
                    targetSubject
            );

            System.out.println("✅ Found " + filteredEmails.size() + " matching emails");

            // Process each filtered email
            for (EmailDTO email : filteredEmails) {
                try {
                    // Parse email body to extract key:value pairs
                    String jsonBody = emailBodyParser.parseEmailBody(email.getBody());

                    // Send to webhook
                    webhookService.sendToWebhook(jsonBody, email);

                    System.out.println("✅ Sent to webhook: " + email.getSubject());
                } catch (Exception e) {
                    System.out.println("❌ Error processing email: " + e.getMessage());
                }
            }

            // Update last polling time AFTER processing
            lastPollingTime = Instant.now();
            System.out.println("✅ Updated last polling time to: " + lastPollingTime);

        } catch (Exception e) {
            System.out.println("❌ Polling error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Instant getLastPollingTime() {
        return lastPollingTime;
    }
}
package com.example.emailreader.service;

import com.example.emailreader.dto.EmailDTO;
import com.example.emailreader.entity.WebhookData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {

    @Autowired
    private WebhookDataStore webhookDataStore;

    @Value("${webhook.url:http://localhost:3000/api/process-data}")
    private String webhookUrl;

    public void sendToWebhook(String jsonBody, EmailDTO email) {
        try {
            System.out.println("📤 Data to be sent to webhook:");
            System.out.println(jsonBody);

            // ✅ Store in memory for dashboard display
            WebhookData data = new WebhookData(
                    jsonBody,
                    email.getSubject(),
                    email.getFrom()
            );
            webhookDataStore.addData(data);

            System.out.println("✅ Data stored for dashboard");

        } catch (Exception e) {
            System.out.println("❌ Webhook error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
package com.example.emailreader.entity;

import java.time.LocalDateTime;

public class WebhookData {
    private String id;
    private String jsonData;
    private String emailSubject;
    private String fromAddress;
    private LocalDateTime receivedAt;
    private LocalDateTime sentAt;

    public WebhookData(String jsonData, String emailSubject, String fromAddress) {
        this.jsonData = jsonData;
        this.emailSubject = emailSubject;
        this.fromAddress = fromAddress;
        this.receivedAt = LocalDateTime.now();
        this.sentAt = LocalDateTime.now();
        this.id = System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }

    // Getters
    public String getId() { return id; }
    public String getJsonData() { return jsonData; }
    public String getEmailSubject() { return emailSubject; }
    public String getFromAddress() { return fromAddress; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public LocalDateTime getSentAt() { return sentAt; }
}
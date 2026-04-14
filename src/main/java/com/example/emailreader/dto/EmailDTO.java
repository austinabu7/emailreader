package com.example.emailreader.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailDTO {
    private String id;
    private String sender;
    private String subject;
    private String body;
    private String receivedTime;
    private List<String> attachmentNames;
    private boolean hasAttachments;
    private String conversationId;
}
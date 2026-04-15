package com.example.emailreader.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

// src/main/java/com/example/dto/EmailDTO.java
@Data
@Builder
public class EmailDTO {
    private String subject;
    private String from;
    private Date received;
    private String body;
}


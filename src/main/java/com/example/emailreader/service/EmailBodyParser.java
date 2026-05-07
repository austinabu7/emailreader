package com.example.emailreader.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailBodyParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Words to ignore (signature lines)


    public String parseEmailBody(String emailBody) {
        try {
            Map<String, String> dataMap = new HashMap<>();

            // ✅ Find "banner{" (case insensitive, with any spaces)
            String lowerBody = emailBody.toLowerCase();
            int bannerStart = lowerBody.indexOf("banner");

            if (bannerStart == -1) {
                System.out.println("❌ No 'banner' found");
                return "{}";
            }

            // Find opening "{" after "banner"
            int openBrace = emailBody.indexOf("{", bannerStart);
            if (openBrace == -1) {
                System.out.println("❌ No '{' found after banner");
                return "{}";
            }

            // Find closing "}"
            int closeBrace = emailBody.indexOf("}", openBrace);
            if (closeBrace == -1) {
                System.out.println("❌ No '}' found");
                return "{}";
            }

            // ✅ Extract content between { }
            String bannerContent = emailBody.substring(openBrace + 1, closeBrace);
            System.out.println("📦 Banner content: " + bannerContent);

            // Split by comma to get key:value pairs
            String[] pairs = bannerContent.split(",");

            for (String pair : pairs) {
                pair = pair.trim();

                if (pair.contains(":")) {
                    String[] parts = pair.split(":", 2);

                    if (parts.length == 2) {
                        String key = parts[0].trim().toLowerCase();
                        String value = parts[1].trim();

                        if (!key.isEmpty() && !value.isEmpty()) {
                            dataMap.put(key, value);
                            System.out.println("✅ " + key + " = " + value);
                        }
                    }
                }
            }

            return objectMapper.writeValueAsString(dataMap);

        } catch (Exception e) {
            System.out.println("❌ Parse error: " + e.getMessage());
            return "{}";
        }
    }




}
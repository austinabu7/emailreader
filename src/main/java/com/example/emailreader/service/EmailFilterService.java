package com.example.emailreader.service;

import com.example.emailreader.dto.EmailDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class EmailFilterService {

    @Value("${filter.allowedFromAddresses:cmpadmindev@ibasis.net,admin@ibasis.net}")
    private String allowedFromAddresses;

    @Value("${filter.allowedToAddresses:cmpadmindev@ibasis.net,receiver@ibasis.net}")
    private String allowedToAddresses;

    public List<EmailDTO> filterEmails(List<EmailDTO> emails, String targetSubject) {
        List<EmailDTO> filteredEmails = new ArrayList<>();

        List<String> fromWhitelist = Arrays.asList(allowedFromAddresses.split(","));
        List<String> toWhitelist = Arrays.asList(allowedToAddresses.split(","));

        for (EmailDTO email : emails) {
            // Check subject
            if (email.getSubject() != null && email.getSubject().equalsIgnoreCase(targetSubject)) {

                // Check FROM address
                if (email.getFrom() != null && isInWhitelist(email.getFrom(), fromWhitelist)) {

                    // Check TO address (you need to add this to EmailDTO)
                    // For now, just add if subject + from match
                    filteredEmails.add(email);
                    System.out.println("✅ Email matched: " + email.getSubject() + " from " + email.getFrom());
                }
            }
        }

        return filteredEmails;
    }

    private boolean isInWhitelist(String email, List<String> whitelist) {
        return whitelist.stream()
                .anyMatch(allowed -> email.toLowerCase().contains(allowed.toLowerCase().trim()));
    }
}
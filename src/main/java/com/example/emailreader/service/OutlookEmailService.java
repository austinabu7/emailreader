package com.example.emailreader.service;

import com.example.emailreader.dto.EmailDTO;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.MessageCollectionPage;

import java.util.ArrayList;
import java.util.List;

// src/main/java/com/example/service/OutlookEmailService.java
@Service
public class OutlookEmailService {

    private GraphServiceClient graphClient;
    private String userEmail;

    // Initialize with OAuth2 token
    public void authenticateWithToken(String token) {
        List<String> scopes = new ArrayList<>();
        scopes.add("https://graph.microsoft.com/.default");

        final TokenCredentialAuthProvider authProvider =
                new TokenCredentialAuthProvider(scopes,
                        new StaticTokenCredentialProvider(new AccessToken(token, OffsetDateTime.now().plusHours(1))));

        this.graphClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .buildClient();
    }

    public List<EmailDTO> getInboxEmails(int limit) throws Exception {
        MessageCollectionPage messages = graphClient.me()
                .mailFolders("inbox")
                .messages()
                .buildRequest()
                .top(limit)
                .select("subject,from,receivedDateTime,bodyPreview,body")
                .orderBy("receivedDateTime DESC")
                .get();

        List<EmailDTO> emailList = new ArrayList<>();
        for (Message msg : messages.getCurrentPage()) {
            emailList.add(mapToDTO(msg));
        }
        return emailList;
    }

    public List<EmailDTO> searchEmails(String query) throws Exception {
        MessageCollectionPage messages = graphClient.me()
                .messages()
                .buildRequest()
                .search("\"" + query + "\"")
                .get();

        List<EmailDTO> emailList = new ArrayList<>();
        for (Message msg : messages.getCurrentPage()) {
            emailList.add(mapToDTO(msg));
        }
        return emailList;
    }

    private EmailDTO mapToDTO(Message msg) {
        return EmailDTO.builder()
                .subject(msg.subject)
                .from(msg.from != null ? msg.from.emailAddress.address : "Unknown")
                .received(msg.receivedDateTime)
                .body(msg.body != null ? msg.body.content : "")
                .build();
    }
}
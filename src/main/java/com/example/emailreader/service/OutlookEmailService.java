package com.example.emailreader.service;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.requests.MessageCollectionPage;
import com.example.emailreader.dto.EmailDTO;
import org.springframework.stereotype.Service;
import java.util.*;
import com.microsoft.graph.authentication.BearerTokenAuthenticationProvider;

@Service
public class OutlookEmailService {

    private GraphServiceClient graphClient;
    private String userEmail;

    // Option 1: Authenticate with Bearer Token
    public void authenticateWithToken(String token) {
        final BearerTokenAuthenticationProvider authProvider =
                new BearerTokenAuthenticationProvider(() -> token);

        this.graphClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .buildClient();
    }

    // Option 2: Authenticate with Client Secret (Recommended)
    public void authenticateWithAzure(String clientId, String clientSecret, String tenantId) {
        final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();

        final List<String> scopes = new ArrayList<>();
        scopes.add("https://graph.microsoft.com/.default");

        final TokenCredentialAuthProvider authProvider =
                new TokenCredentialAuthProvider(scopes, clientSecretCredential);

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
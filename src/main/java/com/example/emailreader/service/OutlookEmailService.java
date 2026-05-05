package com.example.emailreader.service;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.example.emailreader.dto.EmailDTO;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.MessageCollectionPage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OutlookEmailService {

    private GraphServiceClient graphClient;

    // Authenticate with Client Secret (from environment variables)
    public void authenticateWithClientSecret(String clientId, String clientSecret, String tenantId) throws Exception {
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

    // Get inbox emails
    public List<EmailDTO> getInboxEmails(int limit) throws Exception {
        if (graphClient == null) {
            throw new Exception("Not authenticated. Call authenticateWithClientSecret() first.");
        }

        MessageCollectionPage messages = graphClient.me()
                .mailFolders("inbox")
                .messages()
                .buildRequest()
                .top(limit)
                .select("subject,from,receivedDateTime,body")
                .orderBy("receivedDateTime DESC")
                .get();

        List<EmailDTO> emailList = new ArrayList<>();
        for (Message msg : messages.getCurrentPage()) {
            emailList.add(mapToDTO(msg));
        }
        return emailList;
    }

    // Search emails
    public List<EmailDTO> searchEmails(String query) throws Exception {
        if (graphClient == null) {
            throw new Exception("Not authenticated. Call authenticateWithClientSecret() first.");
        }

        // Search in subject OR body
        String filterQuery = "contains(subject,'" + query + "') or contains(body,'" + query + "')";

        MessageCollectionPage messages = graphClient.me()
                .messages()
                .buildRequest()
                .filter(filterQuery)
                .top(10)
                .select("subject,from,receivedDateTime,body")
                .orderBy("receivedDateTime DESC")
                .get();

        List<EmailDTO> emailList = new ArrayList<>();
        for (Message msg : messages.getCurrentPage()) {
            emailList.add(mapToDTO(msg));
        }
        return emailList;
    }

    // Convert Message to EmailDTO
    private EmailDTO mapToDTO(Message msg) {
        return EmailDTO.builder()
                .subject(msg.subject != null ? msg.subject : "No Subject")
                .from(msg.from != null ? msg.from.emailAddress.address : "Unknown")
                .received(msg.receivedDateTime)
                .body(msg.body != null ? msg.body.content : "")
                .build();
    }
}
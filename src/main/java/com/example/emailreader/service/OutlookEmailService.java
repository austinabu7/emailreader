package com.example.emailreader.service;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.example.emailreader.dto.EmailDTO;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.MessageCollectionPage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

@Service
public class OutlookEmailService {

    private GraphServiceClient graphClient;
    private Instant lastPollingTime = Instant.now();;

    // Authenticate with Client Secret
    public void authenticateWithClientSecret(String clientId, String clientSecret, String tenantId) throws Exception {
        try {
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

            graphClient.users("cmpadmindev@ibasis.net")
                    .messages()
                    .buildRequest()
                    .get();

        } catch (Exception e) {
            throw new Exception("Authentication failed: " + e.getMessage(), e);
        }
    }

    // ✅ GET ONLY NEW EMAILS SINCE LAST POLLING
    public List<EmailDTO> getNewEmails(int limit) throws Exception {
        if (graphClient == null) {
            throw new Exception("Not authenticated. Call authenticateWithClientSecret() first.");
        }

        // Format time to ISO 8601 (Graph API format)
        String filterTime = lastPollingTime.toString();

        System.out.println("🔍 Fetching emails after: " + filterTime);

        // ✅ Filter: Only emails received AFTER lastPollingTime
        String filter = "receivedDateTime gt " + filterTime;

        MessageCollectionPage messages = graphClient.users("cmpadmindev@ibasis.net")
                .mailFolders("inbox")
                .messages()
                .buildRequest()
                .filter(filter)  // ✅ Only new emails
                .top(limit)
                .select("subject,from,toRecipients,receivedDateTime,body")
                .orderBy("receivedDateTime DESC")
                .get();

        List<EmailDTO> emailList = new ArrayList<>();
        for (Message msg : messages.getCurrentPage()) {
            emailList.add(mapToDTO(msg));
        }

        // Update last polling time
        lastPollingTime = Instant.now();

        return emailList;
    }

    // Get all inbox emails (for manual checking)
    public List<EmailDTO> getInboxEmails(int limit) throws Exception {
        if (graphClient == null) {
            throw new Exception("Not authenticated. Call authenticateWithClientSecret() first.");
        }

        MessageCollectionPage messages = graphClient.users("cmpadmindev@ibasis.net")
                .mailFolders("inbox")
                .messages()
                .buildRequest()
                .top(limit)
                .select("subject,from,toRecipients,receivedDateTime,body")
                .orderBy("receivedDateTime DESC")
                .get();

        List<EmailDTO> emailList = new ArrayList<>();
        for (Message msg : messages.getCurrentPage()) {
            emailList.add(mapToDTO(msg));
        }
        return emailList;
    }

    public List<EmailDTO> searchEmails(String query) throws Exception {
        if (graphClient == null) {
            throw new Exception("Not authenticated. Call authenticateWithClientSecret() first.");
        }

        String filterQuery = "contains(subject,'" + query + "') or contains(body,'" + query + "')";

        MessageCollectionPage messages = graphClient.users("cmpadmindev@ibasis.net")  // ✅ CHANGE HERE
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
        String toAddress = "Unknown";
        if (msg.toRecipients != null && msg.toRecipients.size() > 0) {
            toAddress = msg.toRecipients.get(0).emailAddress.address;
        }

        return EmailDTO.builder()
                .subject(msg.subject != null ? msg.subject : "No Subject")
                .from(msg.from != null ? msg.from.emailAddress.address : "Unknown")
                .received(msg.receivedDateTime)
                .body(msg.body != null ? msg.body.content : "")
                .build();
    }
}
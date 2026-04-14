package com.example.emailreader.service;


import com.example.emailreader.dto.EmailDTO;
import microsoft.exchange.webservices.data.core.enumeration.misc.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.ItemCollection;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import jakarta.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final EWSConnectionService ewsConnectionService;
    private final List<EmailDTO> emails = new CopyOnWriteArrayList<>();
    private Flux<EmailDTO> emailFlux;

    public EmailService(EWSConnectionService ewsConnectionService) {
        this.ewsConnectionService = ewsConnectionService;
    }

    @PostConstruct
    public void initialize() {
        // Load initial emails
        loadEmails();

        // Setup streaming for real-time updates
        setupEmailStream();

        // Start EWS subscription
        ewsConnectionService.startStreamingSubscription();
    }

    private void setupEmailStream() {
        emailFlux = Flux.create(sink -> {
            ewsConnectionService.setEventSink(sink);

            // Listen for new email notifications and fetch emails
            Flux.interval(java.time.Duration.ofSeconds(5))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(tick -> {
                        try {
                            loadEmails();
                        } catch (Exception e) {
                            logger.error("Error loading emails", e);
                        }
                    });
        }).share();
    }

    public void loadEmails() {
        try {
            logger.info("Loading emails from inbox");

            var exchangeService = ewsConnectionService.getExchangeService();

            // Create search filter for unread emails (optional)
            SearchFilter.SearchFilterCollection searchFilter = new SearchFilter.SearchFilterCollection(
                    LogicalOperator.And,
                    new SearchFilter.IsEqualTo(
                            microsoft.exchange.webservices.data.core.PropertySet.FirstClassProperties,
                            "true"
                    )
            );

            // Find items
            FindItemsResults<com.microsoft.exchange.webservices.data.core.service.item.Item> findResults =
                    exchangeService.findItems(
                            WellKnownFolderName.Inbox,
                            new SearchFilter.SearchFilterCollection(LogicalOperator.And),
                            null,
                            0,
                            100
                    );

            emails.clear();

            for (com.microsoft.exchange.webservices.data.core.service.item.Item item : findResults) {
                if (item instanceof EmailMessage) {
                    EmailMessage emailMessage = (EmailMessage) item;
                    emailMessage.load();

                    EmailDTO dto = mapEmailToDTO(emailMessage);
                    emails.add(0, dto); // Add to front
                }
            }

            logger.info("Loaded {} emails", emails.size());
        } catch (Exception e) {
            logger.error("Error loading emails", e);
        }
    }

    private EmailDTO mapEmailToDTO(EmailMessage emailMessage) throws Exception {
        List<String> attachmentNames = new ArrayList<>();

        if (emailMessage.getHasAttachments()) {
            for (var attachment : emailMessage.getAttachments()) {
                attachmentNames.add(attachment.getName());
            }
        }

        String sender = emailMessage.getFrom() != null ?
                emailMessage.getFrom().getAddress() : "Unknown";

        String body = emailMessage.getBody().toString();
        if (body.length() > 500) {
            body = body.substring(0, 500) + "...";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String receivedTime = sdf.format(emailMessage.getDateTimeReceived());

        return EmailDTO.builder()
                .id(emailMessage.getId().getUniqueId())
                .sender(sender)
                .subject(emailMessage.getSubject())
                .body(body)
                .receivedTime(receivedTime)
                .attachmentNames(attachmentNames)
                .hasAttachments(emailMessage.getHasAttachments())
                .conversationId(emailMessage.getConversationId() != null ?
                        emailMessage.getConversationId().toString() : null)
                .build();
    }

    public List<EmailDTO> getAllEmails() {
        return new ArrayList<>(emails);
    }

    public Flux<EmailDTO> getNewEmailsStream() {
        return emailFlux.map(event -> {
            loadEmails();
            return emails.isEmpty() ? null : emails.get(0);
        }).filter(Objects::nonNull);
    }
}

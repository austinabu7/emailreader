package com.example.emailreader.service;

import com.example.emailreader.config.ApplicationProperties;
import com.example.emailreader.dto.EmailDTO;
import jakarta.annotation.PostConstruct;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class OutlookEmailService {

    private static final int DEFAULT_SEARCH_LIMIT = 50;

    private static final Logger logger = LoggerFactory.getLogger(OutlookEmailService.class);

    @Autowired
    private ApplicationProperties properties;

    private ExchangeService exchangeService;

    @PostConstruct
    public void init() {
        try {
            if (properties.getEmail() != null && properties.getPassword() != null) {
                authenticateBasic(properties.getEmail(), properties.getPassword());
            }
        } catch (Exception e) {
            logger.warn("Auto-authentication from properties failed; manual authentication required via /authenticate-basic", e);
        }
    }

    public void authenticateBasic(String email, String password) throws Exception {
        ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        service.setCredentials(new WebCredentials(email, password));
        String ewsUrl = (properties.getExchangeUrl() != null && !properties.getExchangeUrl().isEmpty())
                ? properties.getExchangeUrl()
                : "https://outlook.office365.com/ews/exchange.asmx";
        service.setUrl(new URI(ewsUrl));
        this.exchangeService = service;
    }

    public List<EmailDTO> getInboxEmails(int limit) throws Exception {
        if (exchangeService == null) {
            throw new IllegalStateException(
                    "Not authenticated. Call /authenticate-basic with valid Exchange credentials, " +
                    "or set outlook.email and outlook.password in application.properties.");
        }
        FindItemsResults<Item> findResults = exchangeService.findItems(
                WellKnownFolderName.Inbox, new ItemView(limit));
        exchangeService.loadPropertiesForItems(findResults, PropertySet.FirstClassProperties);

        List<EmailDTO> emailList = new ArrayList<>();
        for (Item item : findResults) {
            if (item instanceof EmailMessage) {
                emailList.add(mapToDTO((EmailMessage) item));
            }
        }
        return emailList;
    }

    /**
     * Searches inbox emails whose subject contains the given query string.
     * Note: search is limited to the Subject field; body/sender searches
     * are not performed to keep the implementation simple.
     */
    public List<EmailDTO> searchEmails(String query) throws Exception {
        if (exchangeService == null) {
            throw new IllegalStateException(
                    "Not authenticated. Call /authenticate-basic with valid Exchange credentials, " +
                    "or set outlook.email and outlook.password in application.properties.");
        }
        SearchFilter searchFilter = new SearchFilter.ContainsSubstring(ItemSchema.Subject, query);
        FindItemsResults<Item> findResults = exchangeService.findItems(
                WellKnownFolderName.Inbox, searchFilter, new ItemView(DEFAULT_SEARCH_LIMIT));
        exchangeService.loadPropertiesForItems(findResults, PropertySet.FirstClassProperties);

        List<EmailDTO> emailList = new ArrayList<>();
        for (Item item : findResults) {
            if (item instanceof EmailMessage) {
                emailList.add(mapToDTO((EmailMessage) item));
            }
        }
        return emailList;
    }

    private EmailDTO mapToDTO(EmailMessage msg) throws Exception {
        return EmailDTO.builder()
                .subject(msg.getSubject())
                .from(msg.getFrom() != null ? msg.getFrom().getAddress() : "Unknown")
                .received(msg.getDateTimeReceived())
                .body(msg.getBody() != null ? MessageBody.getStringFromMessageBody(msg.getBody()) : "")
                .build();
    }
}
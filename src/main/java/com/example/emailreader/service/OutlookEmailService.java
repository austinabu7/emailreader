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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class OutlookEmailService {

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
            // Service will be authenticated manually via /authenticate-basic if auto-init fails
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
            throw new IllegalStateException("Not authenticated. Please call /authenticate-basic first.");
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

    public List<EmailDTO> searchEmails(String query) throws Exception {
        if (exchangeService == null) {
            throw new IllegalStateException("Not authenticated. Please call /authenticate-basic first.");
        }
        SearchFilter searchFilter = new SearchFilter.ContainsSubstring(ItemSchema.Subject, query);
        FindItemsResults<Item> findResults = exchangeService.findItems(
                WellKnownFolderName.Inbox, searchFilter, new ItemView(50));
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
                .body(MessageBody.getStringFromMessageBody(msg.getBody()))
                .build();
    }
}
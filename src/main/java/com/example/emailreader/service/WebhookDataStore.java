package com.example.emailreader.service;

import com.example.emailreader.entity.WebhookData;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class WebhookDataStore {

    private static final List<WebhookData> dataList = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_SIZE = 100; // Keep last 100 entries

    public void addData(WebhookData data) {
        dataList.add(0, data); // Add to beginning

        // Keep only last 100
        if (dataList.size() > MAX_SIZE) {
            dataList.remove(dataList.size() - 1);
        }
    }

    public List<WebhookData> getAllData() {
        return new ArrayList<>(dataList);
    }

    public List<WebhookData> getRecentData(int limit) {
        return dataList.stream()
                .limit(limit)
                .toList();
    }
}
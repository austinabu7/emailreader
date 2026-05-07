package com.example.emailreader.controller;

import com.example.emailreader.entity.WebhookData;
import com.example.emailreader.service.EmailPollingTask;
import com.example.emailreader.service.WebhookDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private EmailPollingTask emailPollingTask;

    @Autowired
    private WebhookDataStore webhookDataStore;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "✅ Running");
        response.put("lastPollingTime", emailPollingTask.getLastPollingTime());
        response.put("currentTime", Instant.now());
        response.put("message", "Email polling is active. Checking every 1 minute.");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/data")
    public ResponseEntity<List<WebhookData>> getWebhookData() {
        List<WebhookData> data = webhookDataStore.getRecentData(20);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/")
    public String getDashboard() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Email Polling Dashboard</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial; margin: 20px; background: #f5f5f5; }\n" +
                "        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }\n" +
                "        h1 { color: #333; border-bottom: 3px solid #007bff; padding-bottom: 10px; }\n" +
                "        .status { padding: 15px; background: #d4edda; border: 1px solid #c3e6cb; border-radius: 5px; margin: 20px 0; }\n" +
                "        .info-box { padding: 15px; background: #e7f3ff; border-left: 4px solid #007bff; margin: 10px 0; }\n" +
                "        .data-item { padding: 15px; background: #f9f9f9; border-left: 4px solid #28a745; margin: 10px 0; border-radius: 5px; }\n" +
                "        .data-item strong { color: #333; }\n" +
                "        #data { background: #f9f9f9; padding: 15px; border-radius: 5px; max-height: 600px; overflow-y: auto; }\n" +
                "        pre { background: #282c34; color: #abb2bf; padding: 15px; border-radius: 5px; overflow-x: auto; font-size: 12px; }\n" +
                "        button { padding: 10px 20px; background: #007bff; color: white; border: none; border-radius: 5px; cursor: pointer; margin: 10px 0; }\n" +
                "        button:hover { background: #0056b3; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='container'>\n" +
                "        <h1>📧 Email Polling Dashboard</h1>\n" +
                "        \n" +
                "        <div class='status'>\n" +
                "            <strong>✅ Status:</strong> <span id='status'>Loading...</span>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='info-box'>\n" +
                "            <strong>⏰ Last Polling:</strong> <span id='lastPolling'>Loading...</span>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='info-box'>\n" +
                "            <strong>🔄 Current Time:</strong> <span id='currentTime'>Loading...</span>\n" +
                "        </div>\n" +
                "        \n" +
                "        <button onclick='refreshStatus()'>🔄 Refresh Status</button>\n" +
                "        \n" +
                "        <h2>📊 Recent Data Sent to Webhook:</h2>\n" +
                "        <div id='data'>\n" +
                "            <p style='color: #999;'>Waiting for data...</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "\n" +
                "    <script>\n" +
                "        // Fetch status\n" +
                "        function refreshStatus() {\n" +
                "            fetch('/api/dashboard/status')\n" +
                "                .then(res => res.json())\n" +
                "                .then(data => {\n" +
                "                    document.getElementById('status').textContent = data.status;\n" +
                "                    document.getElementById('lastPolling').textContent = data.lastPollingTime;\n" +
                "                    document.getElementById('currentTime').textContent = data.currentTime;\n" +
                "                    loadData();\n" +
                "                })\n" +
                "                .catch(err => console.error('Error:', err));\n" +
                "        }\n" +
                "\n" +
                "        // Load webhook data\n" +
                "        function loadData() {\n" +
                "            fetch('/api/dashboard/data')\n" +
                "                .then(res => res.json())\n" +
                "                .then(data => {\n" +
                "                    if (data.length === 0) {\n" +
                "                        document.getElementById('data').innerHTML = '<p style=\"color: #999;\">No data received yet...</p>';\n" +
                "                        return;\n" +
                "                    }\n" +
                "                    \n" +
                "                    let html = '';\n" +
                "                    data.forEach(item => {\n" +
                "                        html += `\n" +
                "                            <div class='data-item'>\n" +
                "                                <strong>📬 Subject:</strong> ${item.emailSubject}<br>\n" +
                "                                <strong>👤 From:</strong> ${item.fromAddress}<br>\n" +
                "                                <strong>⏰ Sent:</strong> ${item.sentAt}<br>\n" +
                "                                <strong>📄 Data:</strong>\n" +
                "                                <pre>${JSON.stringify(JSON.parse(item.jsonData), null, 2)}</pre>\n" +
                "                            </div>\n" +
                "                        `;\n" +
                "                    });\n" +
                "                    document.getElementById('data').innerHTML = html;\n" +
                "                })\n" +
                "                .catch(err => console.error('Error loading data:', err));\n" +
                "        }\n" +
                "\n" +
                "        // Auto-refresh every 5 seconds\n" +
                "        setInterval(refreshStatus, 5000);\n" +
                "\n" +
                "        // Initial load\n" +
                "        refreshStatus();\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
}
package com.example.emoify_javafx;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpPollingService extends ScheduledService<String> {
    private final String apiUrl;
    private final Duration pollInterval;

    public HttpPollingService(String apiUrl, Duration pollInterval) {
        this.apiUrl = apiUrl;
        this.pollInterval = pollInterval;
        setPeriod(javafx.util.Duration.millis(pollInterval.toMillis()));
    }

    @Override
    protected Task<String> createTask() {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                HttpResponse<String> response = HttpClient.newHttpClient()
                        .send(HttpRequest.newBuilder()
                                        .uri(URI.create(apiUrl))
                                        .timeout(Duration.ofSeconds(10))
                                        .build(),
                                HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return response.body();
                } else {
                    throw new RuntimeException("HTTP error: " + response.statusCode());
                }
            }
        };
    }
}

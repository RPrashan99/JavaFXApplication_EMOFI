package com.example.emoify_javafx;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.concurrent.CompletableFuture;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:5000/api";
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static CompletableFuture<String> getSystemStatus() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/state"))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

//    public static String sendRequest(String input) throws Exception {
//        String jsonInput = String.format("{\"input\": \"%s\"}", input);
//
//        HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(API_URL))
//                .header("Content-Type", "application/json")
//                .POST(BodyPublishers.ofString(jsonInput))
//                .build();
//
//        HttpResponse<String> response = client.send(
//                request, HttpResponse.BodyHandlers.ofString());
//
//        return response.body();
//    }
}

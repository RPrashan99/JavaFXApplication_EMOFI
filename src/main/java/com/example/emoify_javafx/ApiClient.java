package com.example.emoify_javafx;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.List;
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

    public static CompletableFuture<String> getLastRecords() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/recentRecords"))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    public static CompletableFuture<String> getAppDetails() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/apps"))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    public static CompletableFuture<Integer> saveUserData(String userName, String password, String phoneNumber) {
        String json = String.format("""
        {
            "userName": "%s",
            "password": "%s",
            "phoneNumber": "%s"
        }
    """, userName, password, phoneNumber);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/saveUserData"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply( response ->{
                        int status = response.statusCode();

                        return status;
                    }
                );
    }

    public static CompletableFuture<String> getRecommendations() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/getRecommendationOptionsState"))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    public static CompletableFuture<Integer> saveRecommendationState(String recommendation, String recommendedApp, String searchQuery){

        String json = String.format("""
        {
            "executed": %b,
            "recommendation": "%s",
            "recommendedApp": "%s",
            "searchQuery": "%s"
        }
    """, true, recommendation, recommendedApp, searchQuery);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/setExecutedState"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply( response ->{
                            int status = response.statusCode();
                            return status;
                        }
                );
    }

    public static CompletableFuture<Integer> setSelectedApp(String app) {
        String json = String.format("""
        {
            "selectedApp": "%s"
        }
        """, app);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/setSelectedApp"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply( response ->{
                            int status = response.statusCode();

                            return status;
                        }
                );
    }

    public static CompletableFuture<Integer> setStateInit() {
        String state = "init";
        String json = String.format("""
        {
            "state": "%s"
        }
        """, state);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/setStateInit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply( response ->{
                            int status = response.statusCode();

                            return status;
                        }
                );
    }

    public static CompletableFuture<String> getLogin() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/getLogin"))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    public static CompletableFuture<Integer> openAddApp(String userName) {

        String json = String.format("""
        {
            "user": "%s"
        }
        """, userName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/addApp"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply( response ->{
                            int status = response.statusCode();

                            return status;
                        }
                );
    }

    public static CompletableFuture<Integer> setSettings(Integer userID, List<String> settings, List<String> values) {

        StringBuilder settingsJson = new StringBuilder("[");
        for (int i = 0; i < settings.size(); i++) {
            if (i > 0) {
                settingsJson.append(",");
            }
            settingsJson.append(String.format(
                    "{\"name\":\"%s\",\"value\":\"%s\"}",
                    escapeJson(settings.get(i)),
                    escapeJson(values.get(i))
            ));
        }
        settingsJson.append("]");

        // Build the complete JSON payload
        String json = String.format(
                "{\"userID\":\"%s\",\"settings\":%s}",
                userID,
                settingsJson
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/editSettings"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply( response ->{
                            int status = response.statusCode();

                            return status;
                        }
                );
    }

    private static String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static CompletableFuture<String> getAppSettings(){
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/getSettings"))
            .GET()
            .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}

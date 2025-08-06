package com.example.emoify_javafx;

import org.json.JSONObject;

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

    public static CompletableFuture<Integer> saveRecommendationState(String recommendation, String recommendedApp){

        String json = String.format("""
        {
            "executed": %b,
            "recommendation": "%s",
            "recommendedApp": "%s"
        }
    """, true, recommendation, recommendedApp);

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

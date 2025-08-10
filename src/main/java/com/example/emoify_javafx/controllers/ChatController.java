package com.example.emoify_javafx.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.util.Duration;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChatController {

    @FXML
    private ListView<HBox> chatList;

    @FXML
    private TextField userInput;

//    @FXML
//    private void handleSend() {
//        String message = userInput.getText().trim();
//        if (message.isEmpty()) return;
//
//        // Add user bubble
//        chatList.getItems().add(createBubble(message, Pos.CENTER_RIGHT, "#DCF8C6"));
//        userInput.clear();
//
//        // Send to backend
//        new Thread(() -> {
//            String botReply = sendMessageToBackend(message);
//
//            javafx.application.Platform.runLater(() -> {
//                chatList.getItems().add(createBubble(botReply, Pos.CENTER_LEFT, "#FFFFFF"));
//            });
//        }).start();
//    }

    @FXML
    private void handleSend() {
        String message = userInput.getText().trim();
        if (message.isEmpty()) return;

        // Add user bubble with animation
        HBox userBubble = createBubble(message, Pos.CENTER_RIGHT, "#DCF8C6");
        userBubble.setOpacity(0);
        chatList.getItems().add(userBubble);

        // Animate user message appearance
        FadeTransition userFade = new FadeTransition(Duration.millis(200), userBubble);
        userFade.setFromValue(0);
        userFade.setToValue(1);
        userFade.play();

        userInput.clear();

        // Create and show typing indicator
        Label typingIndicator = new Label("...");
        typingIndicator.setStyle("-fx-text-fill: #999999; -fx-font-style: italic;");
        HBox typingBubble = createBubble("", Pos.CENTER_LEFT, "#F5F5F5");
        typingBubble.getChildren().get(0).setStyle("-fx-padding: 8 15;");

        Platform.runLater(() -> {
            chatList.getItems().add(typingBubble);
        });

        // Send to backend in background thread
        new Thread(() -> {
            String botReply = sendMessageToBackend(message);

            Platform.runLater(() -> {
                // Remove typing indicator
                chatList.getItems().remove(typingBubble);

                // Add bot response with animation
                HBox botBubble = createBubble(botReply, Pos.CENTER_LEFT, "#FFFFFF");
                botBubble.setOpacity(0);
                chatList.getItems().add(botBubble);

                // Animate bot message appearance
                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), botBubble);
                scaleIn.setFromX(0.95);
                scaleIn.setFromY(0.95);
                scaleIn.setToX(1.0);
                scaleIn.setToY(1.0);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(250), botBubble);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                // Play animations together
                ParallelTransition parallelTransition = new ParallelTransition(scaleIn, fadeIn);
                parallelTransition.setInterpolator(Interpolator.EASE_OUT);
                parallelTransition.play();

                // Auto-scroll to the new message
                chatList.scrollTo(chatList.getItems().size() - 1);
            });
        }).start();
    }

//    private HBox createBubble(String message, Pos alignment, String color) {
//        Text text = new Text(message);
//        text.wrappingWidthProperty().set(400);
//
//        Label label = new Label();
//        label.setGraphic(text);
//        label.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15; -fx-padding: 10;");
//
//        HBox bubble = new HBox(label);
//        bubble.setMaxWidth(500);
//        bubble.setAlignment(alignment);
//        bubble.setSpacing(5);
//        bubble.setPadding(new Insets(5, 10, 5, 10));
//
//        return bubble;
//    }

    private HBox createBubble(String message, Pos alignment, String color) {
        // Text styling
        Text text = new Text(message);
        text.setFont(Font.font("Segoe UI", 14));
        text.setFill(Color.web("#333333")); // Darker text for better readability
        text.wrappingWidthProperty().set(350); // Slightly narrower for better readability

        // Label (bubble container)
        Label label = new Label();
        label.setGraphic(text);
        label.setContentDisplay(ContentDisplay.RIGHT);
        label.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 18;" + // More rounded corners
                        "-fx-padding: 12 15;" + // More horizontal padding
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.2, 0, 1);" + // Subtle shadow
                        "-fx-border-radius: 18;" +
                        "-fx-border-color: derive(" + color + ", -10%);" + // Slightly darker border
                        "-fx-border-width: 1;"
        );

        // HBox container
        HBox bubble = new HBox(label);
        bubble.setMaxWidth(450); // Slightly smaller max width
        bubble.setAlignment(alignment);
        bubble.setPadding(new Insets(5, 15, 5, 15)); // More side padding

        // Add smooth entrance animation
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), bubble);
        scaleIn.setFromX(0.9);
        scaleIn.setFromY(0.9);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);
        scaleIn.play();

        return bubble;
    }

    private String sendMessageToBackend(String message) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://localhost:5000/chat");
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000); // 15s to connect
            conn.setReadTimeout(60000);    // 30s to read

            JSONObject jsonInput = new JSONObject();
            jsonInput.put("prompt", message);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return "Error: HTTP " + responseCode;
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                // Read the full response
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                // Debug: Print raw response (for troubleshooting)
                System.out.println("Raw response: " + response.toString());

                // Parse JSON safely
                try {
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    if (jsonResponse.has("reply")) {
                        return jsonResponse.getString("reply");
                    } else if (jsonResponse.has("error")) {
                        return "API Error: " + jsonResponse.getString("error");
                    }
                } catch (Exception e) {
                    return "Invalid JSON: " + response.toString(); // Return raw response if parsing fails
                }
            }
        } catch (Exception e) {
            return "Connection Error: " + e.getMessage();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return "Error: No valid response";
    }
}

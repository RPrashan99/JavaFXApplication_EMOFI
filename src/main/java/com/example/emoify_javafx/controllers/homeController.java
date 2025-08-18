package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class homeController implements Initializable {

    @FXML
    private Label systemStatus;

    @FXML
    private Label emotionState;

    @FXML
    private Label lastResponseTime;

    @FXML
    private ImageView emotionImage;

    @FXML
    private Label lastActionApp;

    @FXML
    private Label lastRecommendation;

    private Map<String, String> emotionImagePaths;

    private String previousEmotion = "";
    private String previousSystemStatus = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        imagesLoad();

        systemStatus.setText("Loading");
        emotionState.setText("Loading");
        lastResponseTime.setText("Loading");
        lastActionApp.setText("--------");
        lastRecommendation.setText("--------");
        //refreshStates();
        updateLastRecords();
    }

    private void imagesLoad(){
        emotionImagePaths = new HashMap<>();
        emotionImagePaths.put("Happy", "happy_gif.gif");
        emotionImagePaths.put("Sad", "sad_gif.gif");
        emotionImagePaths.put("Angry", "angry_gif.gif");
        emotionImagePaths.put("Disgust", "disgust_gif.gif");
        emotionImagePaths.put("Sleepy", "sleepy_gif.gif");
        emotionImagePaths.put("Boring", "boring_gif.gif");
        emotionImagePaths.put("Neutral", "neutral_gif.gif");
        emotionImagePaths.put("Surprise", "surprise_gif.gif");
        emotionImagePaths.put("Fear", "fear_gif.gif");
        emotionImagePaths.put("Stress", "stress_gif.gif");
    }

    public void refreshStates() {
        ApiClient.getSystemStatus().thenAccept(response -> {
            JSONObject status = new JSONObject(response);

            String sysStatus = status.getString("system_status");
            String emoState = status.getString("emotion_state");
            String lastResponse = status.getString("last_response_time");

            updateStateLabels(sysStatus, emoState, lastResponse);
            updateEmotionImage(emoState);
        });
    }

    public void updateStateLabels(String status, String emoState, String lastTime) {

        if(!status.isEmpty() && !emoState.isEmpty() && !lastTime.isEmpty()){
            if(!previousSystemStatus.equals(status)){
                systemStatus.setText(status);
                previousSystemStatus = status;
            }
            if(!previousEmotion.equals(emoState)){
                emotionState.setText(emoState);
            }
            lastResponseTime.setText(lastTime);
        }
    }

    public void updateEmotionImage(String emotion) {

        if(!emotionImagePaths.isEmpty()){
            String imagePath = "/com/example/emoify_javafx/icons/emotions/" + emotionImagePaths.get(emotion);
            if(!emotion.equals(previousEmotion)){
                try{
                    previousEmotion = emotion;
                    Platform.runLater(() -> {
                        Image newImage = new Image(getClass().getResourceAsStream(imagePath));
                        emotionImage.setImage(newImage);
                    });
                } catch (Exception e){
                    System.err.println("Error loading image: " + e.getMessage());
                    String defaultPath = "/com/example/emoify_javafx/icons/emotions/neutral.png";
                    Image newImage = new Image(getClass().getResourceAsStream(defaultPath));
                    emotionImage.setImage(newImage);
                }
            }

//            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), myImageView);
//            fadeOut.setFromValue(1.0);
//            fadeOut.setToValue(0.0);
//
//            fadeOut.setOnFinished(e -> {
//                // Change the image when invisible
//                Image newImage = new Image(getClass().getResourceAsStream(newImagePath));
//                myImageView.setImage(newImage);
//
//                // Fade back in
//                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), myImageView);
//                fadeIn.setFromValue(0.0);
//                fadeIn.setToValue(1.0);
//                fadeIn.play();
//            });
//
//            fadeOut.play();
        }
    }

    private void updateLastRecords (){
//        ApiClient.getLastRecords().thenAccept(response -> {
//            JSONObject status = new JSONObject(response);
//
//            String sysRec = status.getString("recommendation");
//            String sysAct = status.getString("action");
//
//            updateLastRecordsLabels(sysRec, sysAct);
//        });
    }

    public void updateLastRecordsLabels(String recommendation, String action){
        if(!recommendation.isEmpty() && !action.isEmpty()){
            Platform.runLater(() -> {
                lastRecommendation.setText(recommendation);
                lastActionApp.setText(action);
            });
        }
    }
}

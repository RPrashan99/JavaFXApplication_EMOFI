package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.ApiClient;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.json.JSONObject;

import java.net.URL;
import java.util.ResourceBundle;

public class homeController implements Initializable {

    @FXML
    private Label systemStatus;

    @FXML
    private Label emotionState;

    @FXML
    private Label lastResponseTime;

//    @FXML
//    private void initialize() {
//        // Set initial label texts
//
//
//        // Set up event handler for the button
//        //updateButton.setOnAction(event -> updateLabels());
//    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        systemStatus.setText("Loading");
        emotionState.setText("Loading");
        lastResponseTime.setText("Loading");

        refreshUserList();
    }

    private void refreshUserList() {
        ApiClient.getSystemStatus().thenAccept(response -> {
            JSONObject status = new JSONObject(response);

            String sysStatus = status.getString("system_status");
            String emoState = status.getString("emotion_state");
            String lastResponse = status.getString("last_response_time");

            updateLabels(sysStatus, emoState, lastResponse);
        });
    }

    private void updateLabels(String status, String emoState, String lastTime) {

        if(!status.isEmpty() && !emoState.isEmpty() && !lastTime.isEmpty()){
            systemStatus.setText(status);
            emotionState.setText(emoState);
            lastResponseTime.setText(lastTime);
        }
    }

    // Public method to update labels from outside the controller
//    public void setMainLabelText(String text) {
//        mainLabel.setText(text);
//    }

//    public void setStatusLabelText(String text) {
//        statusLabel.setText(text);
//    }
}

package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.ApiClient;
import com.example.emoify_javafx.HttpPollingService;
import com.example.emoify_javafx.models.BackendConnector;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class systemStartController implements Initializable {

    @FXML
    private ImageView statusImage;

    @FXML
    private Label statusLabel;

    @FXML
    private Button againBtn;
    HttpPollingService pollingServiceSystemStartStatus;

    Consumer<String> loadingHandler;

    public void statusChange(Boolean isStarted){

        if(isStarted){
            statusLabel.setText("Agent system starting..");
            //checkLogin();
            startAgentSystem();
        }else{
            statusLabel.setText("System operational");
        }

    }

    public void checkLogin(){
        ApiClient.getLogin().thenAccept(response -> {

            JSONObject jsonObject = new JSONObject(response);

            String message = jsonObject.getString("message");

            if(!message.equals("No users available")){
                JSONArray jsonArray = jsonObject.getJSONArray("user");

                List<Object> list = jsonArray.toList();

                String userName = list.get(1).toString();

                startAgentSystem();

            }else{
                if(loadingHandler != null){
                    loadingHandler.accept("Register");
                }
            }
        });
    }

    private void startAgentSystem() {

        // Initialize backend connection
        BackendConnector.startBackend();

        if(loadingHandler != null){
            loadingHandler.accept("Start");
        }
    }

    @FXML
    void handleAgain(MouseEvent event) {
        String response = BackendConnector.getStatus();

        System.out.println("Server response: " + response);
    }

    private void getSystemStartStatus(){
        pollingServiceSystemStartStatus = new HttpPollingService(
                "http://localhost:5050/api/status",
                Duration.ofSeconds(2)// Poll every 2 seconds
        );

        // Handle successful updates
        pollingServiceSystemStartStatus.setOnSucceeded(event -> {

            System.out.println("Start running..");

            if(pollingServiceSystemStartStatus.getValue() != null) {

                JSONObject value = new JSONObject(pollingServiceSystemStartStatus.getValue());

                System.out.println("Response: " + value);

                if(value.getString("status").equals("running")){
                    statusChange(true);
                    pollingServiceSystemStartStatus.cancel();
                }
            }

        });

        // Handle errors
        pollingServiceSystemStartStatus.setOnFailed(event -> {
            System.err.println("Start failed: " + event.getSource().getException());
        });

        // Start the service
        pollingServiceSystemStartStatus.start();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        getSystemStartStatus();
    }

    public void setLoadingHandler(Consumer<String> handler) {
        this.loadingHandler = handler;
    }
}

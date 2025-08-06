package com.example.emoify_javafx;

import com.example.emoify_javafx.controllers.exAppController;
import com.example.emoify_javafx.controllers.homeController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.time.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private Button appsButton;

    @FXML
    private Button closeBtn;

    @FXML
    private AnchorPane contentPane;

    @FXML
    private Button homeButton;

    @FXML
    private Button settingsButton;


    private HttpPollingService pollingService;
    private exAppController exAppControllerClass;
    private homeController homeControllerClass;

    private String userName = "";

    @FXML
    void handleCloseBtn(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    void setContent(String fxmlPath, String userName){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            //AnchorPane newContent = FXMLLoader.load(getClass().getResource(fxmlPath));
            AnchorPane newContent = loader.load();

            if(fxmlPath.equals("fxmls/homeSubWindow.fxml")){
                homeControllerClass = loader.getController();
            }else if(fxmlPath.equals("fxmls/exAppWindow.fxml")){
                exAppControllerClass = loader.getController();
                exAppControllerClass.setUserName(userName);
            }else if(fxmlPath.equals("fxmls/settingsWindow.fxml")){

            }

            contentPane.getChildren().setAll(newContent);
            AnchorPane.setTopAnchor(newContent, 0.0);
            AnchorPane.setBottomAnchor(newContent, 0.0);
            AnchorPane.setLeftAnchor(newContent, 0.0);
            AnchorPane.setRightAnchor(newContent, 0.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void loadHomeWindow() {
        setContent("fxmls/homeSubWindow.fxml", userName);
    }

    @FXML
    private void loadAppsWindow() {

        setContent("fxmls/exAppWindow.fxml", userName);
    }

    @FXML
    private void loadSettingsWindow() {

        setContent("fxmls/settingsWindow.fxml", userName);
    }

    private void initializeStream(){
        pollingService = new HttpPollingService(
                "http://localhost:5000/api/state",
                Duration.ofSeconds(2)  // Poll every 2 seconds
        );

        // Handle successful updates
        pollingService.setOnSucceeded(event -> {

            JSONObject status = new JSONObject(pollingService.getValue());

            String sysStatus = status.getString("system_status");
            String emoState = status.getString("emotion_state");
            String lastResponse = status.getString("last_response_time");
            updateHomeUI(sysStatus, emoState, lastResponse);
        });

        // Handle errors
        pollingService.setOnFailed(event -> {
            System.err.println("Polling failed: " + event.getSource().getException());
        });

        // Start the service
        pollingService.start();
    }

    public void updateHomeUI(String status, String emotion, String lastRes){

        homeControllerClass.updateStateLabels(status, emotion, lastRes);
        homeControllerClass.updateEmotionImage(emotion);
    }

    public void setUserName(String user){
        userName = user;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeStream();
        loadHomeWindow();
    }
}
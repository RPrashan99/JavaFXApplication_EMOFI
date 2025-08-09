package com.example.emoify_javafx;

import com.example.emoify_javafx.controllers.exAppController;
import com.example.emoify_javafx.controllers.homeController;
import com.example.emoify_javafx.controllers.settingsController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;

import java.net.URL;
import java.util.*;

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
    private settingsController settingsControllerClass;

    private final Map<String, AnchorPane> loadedViews = new HashMap<>();
    private final Map<String, Object> controllers = new HashMap<>();

    private String userName = "";
    private Integer userID;

    @FXML
    void handleCloseBtn(MouseEvent event) {

        pollingService.cancel();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    void setContent(String fxmlPath, String userName){
        try {

            AnchorPane newContent;

            // If already loaded before → reuse it
            if (loadedViews.containsKey(fxmlPath)) {
                newContent = loadedViews.get(fxmlPath);

                // If controller needs updates (e.g., username changes)
                Object controller = controllers.get(fxmlPath);
                if (controller instanceof exAppController && userName != null) {
                    ((exAppController) controller).setUserName(userName);
                } else if (controller instanceof settingsController) {
                    //((settingsController) controller).setInitialValues(false, "Mid", 10, 10, 10);
                }

            } else {
                // Load FXML for the first time
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                newContent = loader.load();
                Object controller = loader.getController();

                // Save controller reference
                controllers.put(fxmlPath, controller);

                // Special initialization only on first load
                if (controller instanceof homeController) {
                    homeControllerClass = (homeController) controller;
                } else if (controller instanceof exAppController) {
                    exAppControllerClass = (exAppController) controller;
                    exAppControllerClass.setUserName(userName);
                } else if (controller instanceof settingsController) {
                    settingsControllerClass = (settingsController) controller;
                    //settingsControllerClass.setInitialValues(false, "Mid", 10, 10, 10);
                }

                // Cache the loaded view
                loadedViews.put(fxmlPath, newContent);
            }


//            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
//            //AnchorPane newContent = FXMLLoader.load(getClass().getResource(fxmlPath));
//            AnchorPane newContent = loader.load();
//
//            if(fxmlPath.equals("fxmls/homeSubWindow.fxml")){
//                homeControllerClass = loader.getController();
//            }else if(fxmlPath.equals("fxmls/exAppWindow.fxml")){
//                exAppControllerClass = loader.getController();
//                exAppControllerClass.setUserName(userName);
//            }else if(fxmlPath.equals("fxmls/settingsWindow.fxml")){
//                settingsControllerClass = loader.getController();
//                settingsControllerClass.setInitialValues(false, "Mid", 10, 10, 10);
//            }

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
                "http://localhost:5000/api/stateUI",
                Duration.ofSeconds(2)  // Poll every 2 seconds
        );

        // Handle successful updates
        pollingService.setOnSucceeded(event -> {

            //System.out.println("Response: " + pollingService.getValue());

            JSONObject status = new JSONObject(pollingService.getValue());

            String sysStatus = status.getString("system_status");
            String emoState = status.getString("emotion_state");
            String lastResponse = status.getString("last_response_time");

            System.out.println("Emotion: " + emoState);

            Platform.runLater(() -> {
                updateHomeUI(sysStatus, emoState, lastResponse);
            });
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

    public void updateSettingsUI(Map<String, String> settings){

        String valueTheme = settings.get("theme");
        System.out.println("Theme: " + valueTheme);

        String valueSysDisable = settings.get("systemDisable");
        boolean disableBoolean;
        if(Objects.equals(valueSysDisable, "false")){
            disableBoolean = false;
        }else{
            disableBoolean = true;
        }
        System.out.println("System disable: " + disableBoolean);

        String valueRecTime = settings.get("recommendationTime");
        Integer recTimeInt = Integer.parseInt(valueRecTime);
        System.out.println("Recommendation Time: " + recTimeInt);

        String valueRestTime = settings.get("restTime");
        Integer restTimeInt = Integer.parseInt(valueRestTime);
        System.out.println("Rest time: " + restTimeInt);

        String valueExecTime = settings.get("appExecuteTime");
        Integer execTimeInt = Integer.parseInt(valueExecTime);
        System.out.println("App Execute Time: " + execTimeInt);

        String valueSoundLevel = settings.get("soundLevel");
        System.out.println("Sound Level: " + valueSoundLevel);

        if(settingsControllerClass != null){
            settingsControllerClass.setInitialValues(disableBoolean, valueSoundLevel, recTimeInt, restTimeInt, execTimeInt);
        }

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
package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.ApiClient;
import com.example.emoify_javafx.HttpPollingService;
import com.example.emoify_javafx.models.ExApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class recommendationController implements Initializable {

    @FXML
    private Button rec1Btn;

    @FXML
    private Button rec2Btn;

    @FXML
    private Button rec3Btn;

    @FXML
    private VBox recommendationPane;

    private List<String> recommendations = new ArrayList<>();

    private Map<String, List<String>> recommendationsApps = new HashMap<>();

    private Map<String, List<String>> recommendationsAppIcons = new HashMap<>();

    private HttpPollingService pollingServiceRecommendation;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //appNames = new ArrayList<>();
        //appIcons = new ArrayList<>();
        //getRecommendationPolling();
        fetchRecommendationsFromApi();
    }

    private void setRecommendation(){

//        recommendationPane.getChildren().clear();
//
//        for (int i = 0; i < appNames.size(); i++) {
//            AnchorPane appButton = createRecommendedButton_2(appNames.get(i), appIcons.get(i));
//            recommendationPane.getChildren().add(appButton);
//
//            appButton.applyCss();
//            appButton.layout();
//        }
    }

    private void setRecommendationPane(){
        rec1Btn.setText(recommendations.get(0));
        rec1Btn.setOnMouseClicked(event -> {
            handleRecommendationClick(event, recommendations.get(0));
        });

        rec2Btn.setText(recommendations.get(1));
        rec2Btn.setOnMouseClicked(event -> {
            handleRecommendationClick(event, recommendations.get(1));
        });

        rec3Btn.setText(recommendations.get(2));
        rec3Btn.setOnMouseClicked(event -> {
            handleRecommendationClick(event, recommendations.get(2));
        });
    }

    private void fetchRecommendationsFromApi() {
        ApiClient.getRecommendations().thenAccept(this::getRecommendations);
    }

    public void getRecommendations(String response){
        Platform.runLater(() -> {
            JSONArray appsArray = new JSONArray(response);

            for (int i = 0; i < appsArray.length(); i++) {
                JSONObject recObject = appsArray.getJSONObject(i);

                String recommendation = recObject.getString("recommendation");
                recommendations.add(recommendation);

                JSONArray appRec = recObject.getJSONArray("apps");

                List<String> appNames = new ArrayList<>();
                List<String> appIcons = new ArrayList<>();

                for(int j = 0; j < appRec.length(); j++){
                    JSONObject appObject = appRec.getJSONObject(j);

                    String appName = appObject.getString("app");
                    String path = appObject.getString("iconPath");

                    appNames.add(appName);
                    appIcons.add(path);
                }

                recommendationsApps.put(recommendation, appNames);
                recommendationsAppIcons.put(recommendation, appIcons);

            }
            //setRecommendation();
            setRecommendationPane();
        });
    }

    public AnchorPane createRecommendedButton_2(String appName, String iconPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/emoify_javafx/fxmls/recommendedApp.fxml"));
            AnchorPane appPane = loader.load();

            // Wait for layout before lookup
            appPane.applyCss();
            appPane.layout();

            // Get the button first
            Node node = appPane.lookup("#recommendedApp");
            if (!(node instanceof Button)) {
                System.err.println("Button not found or wrong type");
                return appPane;
            }
            Button btn = (Button) node;

            // Get the ImageView from the button's graphic
            Node graphic = btn.getGraphic();
            if (!(graphic instanceof ImageView)) {
                System.err.println("No ImageView found in button graphic");
                return appPane;
            }
            ImageView icon = (ImageView) graphic;

            // Set the new image
            try {
                Image image = new Image(getClass().getResourceAsStream(iconPath));
                icon.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading icon: " + e.getMessage());
            }

            return appPane;
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            return new AnchorPane(); // fallback
        }
    }
    public AnchorPane createRecommendedButton(String appName, String iconPath) {

        try {
            // Load the button template from FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/emoify_javafx/fxmls/recommendedApp.fxml"));
            AnchorPane appPane = loader.load();

            Button btn = (Button) appPane.lookup("#recommendedApp");
            ImageView icon = (ImageView) appPane.lookup("#appIcon");

            System.out.println("App icon: " + iconPath);

            if (icon != null && iconPath != null) {
                try {
                    Image iconImage = new Image(getClass().getResourceAsStream(iconPath));
                    icon.setImage(iconImage);
                    System.out.println("App icon set");
                } catch (Exception e) {
                    System.err.println("Couldn't load icon: " + iconPath);
                }
            }else{
                System.out.println("Icon is null");
            }

            if (btn != null) {
                //btn.setOnAction(e -> handleRecommendedAppClick(appName));
                System.out.println("Btn set");
            } else {
                // If no button exists, make the whole pane clickable
                //appPane.setOnMouseClicked(e -> handleRecommendedAppClick(appName));
                System.out.println("Btn failed");
            }

            return appPane;

        } catch (IOException e) {
            e.printStackTrace();
            return new AnchorPane();
        }
    }

    @FXML
    private void handleRecommendationClick(MouseEvent event, String name) {
        System.out.println("Recommendation selected: " + name);

    }

    private void setSelectedApp(String appName){
        ApiClient.setSelectedApp(appName).thenAccept(response -> {

            if(response == 200){
                System.out.println("Save selected App success!");
            }else{
                System.out.println("Save selected App failed!");
            }

        });
    }

    @FXML
    void handleCloseBtn(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}

package com.example.emoify_javafx;

import com.example.emoify_javafx.controllers.*;
import com.example.emoify_javafx.models.User;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class MainApplication extends Application {
    double x, y = 0;

    private boolean toolExecuted = false;

    private List<String> recommendations = new ArrayList<>();

    private Map<String, List<String>> recommendationsApps = new HashMap<>();

    private Map<String, List<String>> recommendationsAppIcons = new HashMap<>();

    HttpPollingService pollingServiceRecommendationShow;

    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("Tool started");

        Platform.runLater(this::setStateInit);
        //setStateInit();
        showWidgetWindow(stage);
        //showRecommendationWindowWithoutPool();
        //testWindows(stage);
        //fetchRecommendations();
        //getRecommendationState();
    }

    private void setStateInit(){
        ApiClient.setStateInit().thenAccept(response -> {

            if(response == 200){
                System.out.println("State init");
                getRecommendationState();
            }else{
                System.out.println("State init failed");
            }
        });
    }

    private void testWindows(Stage stage) throws IOException{

        String recommendation = "Relaxing music";
        List<String> iconsPath = new ArrayList<>();
        iconsPath.add("/com/example/emoify_javafx/icons/spotify.png");
        iconsPath.add("/com/example/emoify_javafx/icons/youtube.png");
        iconsPath.add("/com/example/emoify_javafx/icons/discord.png");

        List<String> aNames = new ArrayList<>();
        aNames.add("Spotify");
        aNames.add("Youtube");
        aNames.add("Discord");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxmls/expandingButton.fxml"));
        Parent root = loader.load();

        expandingButtonController expandingButtonController = loader.getController();
        expandingButtonController.buttonSetup(recommendation, iconsPath, aNames);

        stage.initStyle(StageStyle.UNDECORATED);
        Scene sc = new Scene(root, 248, 130);
        sc.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(sc);

        stage.show();
    }

    private void showWidgetWindow(Stage stage) throws IOException{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxmls/widgetWindow.fxml"));
        Parent root = loader.load();

        widgetController widgetController = loader.getController();

        widgetController.setWidgetOpenHandler(user -> {
            try {
                showInitialLoadingWindow(stage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Stage regStage = new Stage();
        regStage.initStyle(StageStyle.UNDECORATED);
        Scene sc = new Scene(root, 190, 106);
        sc.setFill(Color.TRANSPARENT);
        regStage.initStyle(StageStyle.TRANSPARENT);
        regStage.setScene(sc);

        sc.setOnMousePressed(evt -> {
            x = evt.getSceneX();
            y = evt.getSceneY();
        });

        sc.setOnMouseDragged(evt -> {
            regStage.setX(evt.getScreenX()- x);
            regStage.setY(evt.getScreenY()- y);
        });

        regStage.setX(Screen.getPrimary().getVisualBounds().getWidth() - 300); // pixels from left
        regStage.setY(Screen.getPrimary().getVisualBounds().getHeight() - 500);

        regStage.showAndWait();
    }

    private void showInitialLoadingWindow(Stage stage) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxmls/initialLoadingWindow.fxml"));
        Parent root = loader.load();

        initialLoadingController initialLoadingController = loader.getController();

        initialLoadingController.setLoginOpenHandler(data -> {

            if(Objects.equals(data, "Register")){
                try {
                    showRegistrationWindow(stage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }else{
                try {
                    showMainWindow(stage, data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Stage regStage = new Stage();
        regStage.initStyle(StageStyle.UNDECORATED);
        Scene sc = new Scene(root, 412, 320);
        regStage.setScene(sc);

        sc.setOnMousePressed(evt -> {
            x = evt.getSceneX();
            y = evt.getSceneY();
        });

        sc.setOnMouseDragged(evt -> {
            regStage.setX(evt.getScreenX()- x);
            regStage.setY(evt.getScreenY()- y);
        });

        regStage.show();

    }

    private void showRegistrationWindow(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxmls/registrationWindow.fxml"));
        Parent root = loader.load();

        registrationController regController = loader.getController();

        regController.setRegistrationSuccessHandler(user -> {
            try {
                String name = "user1";
                showMainWindow(primaryStage, name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Configure and show registration stage
        Stage regStage = new Stage();
        regStage.initStyle(StageStyle.UNDECORATED);
        Scene sc = new Scene(root, 600, 450);
        regStage.setScene(sc);

        sc.setOnMousePressed(evt -> {
            x = evt.getSceneX();
            y = evt.getSceneY();
        });

        sc.setOnMouseDragged(evt -> {
            regStage.setX(evt.getScreenX()- x);
            regStage.setY(evt.getScreenY()- y);
        });

        regStage.show();
    }

    private void showMainWindow(Stage stage, String user) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("mainWindow.fxml"));
        Scene sc = new Scene(fxmlLoader.load(), 600, 400);
        stage.initStyle(StageStyle.UNDECORATED);

        //move around
        sc.setOnMousePressed(evt -> {
            x = evt.getSceneX();
            y = evt.getSceneY();
        });

        sc.setOnMouseDragged(evt -> {
            stage.setX(evt.getScreenX()- x);
            stage.setY(evt.getScreenY()- y);
        });

        stage.setScene(sc);
        stage.show();
    }

    private void showRecommendationWindow() throws IOException {

        Platform.runLater(() -> {
            try {
                pollingServiceRecommendationShow.cancel();
                System.out.println("Polling canceled!");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("fxmls/recommendationWindow.fxml"));
                Parent root = loader.load();

                Stage regStage = new Stage();
                regStage.initStyle(StageStyle.UNDECORATED);

//                recommendationController controller = loader.getController();
//                controller.getRecommendations();

                Scene sc = new Scene(root, 157, 134);
                regStage.setScene(sc);

                root.layout();   // Force layout pass

                // Get the preferred size of the content
                double prefWidth = root.prefWidth(-1);
                double prefHeight = root.prefHeight(-1);

                // Set window size with some padding
                regStage.setWidth(prefWidth + 20);
                regStage.setHeight(prefHeight + 20);

                // Set minimum size to prevent window from being too small
                regStage.setMinWidth(prefWidth * 0.8);
                regStage.setMinHeight(prefHeight * 0.8);

                regStage.show();
            }catch (IOException e){
                e.printStackTrace();
                System.err.println("Failed to load recommendation window: " + e.getMessage());
            }
        });
    }

    private void fetchRecommendations(){
        ApiClient.getRecommendations().thenAccept(response -> {
            System.out.println("Recommendation getting..");
            recommendations.clear();
            JSONArray appsArray = new JSONArray(response);

            for (int i = 0; i < appsArray.length(); i++) {
                JSONObject recObject = appsArray.getJSONObject(i);

                String recommendation = recObject.getString("recommendation");
                recommendations.add(recommendation);

                System.out.println("Recommendation: " + recommendation);

                JSONArray appRec = recObject.getJSONArray("recommendation_options");

                List<String> appNames = new ArrayList<>();
                List<String> appIcons = new ArrayList<>();

                for(int j = 0; j < appRec.length(); j++){
                    JSONObject appObject = appRec.getJSONObject(j);

                    String appName = appObject.getString("app_name");
                    String path = appObject.getString("app_url");

                    appNames.add(appName);
                    appIcons.add(path);
                }

                recommendationsApps.put(recommendation, appNames);
                recommendationsAppIcons.put(recommendation, appIcons);

            }
            System.out.println("Recommendations obtained: " + recommendations);
            Platform.runLater(() -> {
                try {
                    showRecommendationWindowWithoutPool();
                } catch (IOException e) {
                    e.printStackTrace(); // avoid RuntimeException inside runLater
                }
            });
        });
    }

    private void showRecommendationWindowWithoutPool() throws IOException  {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxmls/recommendationWindow.fxml"));
        Parent root = loader.load();

        Stage regStage = new Stage();
        regStage.initStyle(StageStyle.UNDECORATED);

        recommendationController controller = loader.getController();
        controller.setRecommendationsFromMain(recommendations, recommendationsApps, recommendationsAppIcons);

        controller.setCallback(data -> {
            System.out.println("data received: " + data);

            toolExecuted = false;
            regStage.close();

        });

        root.layout();

        Scene sc = new Scene(root, 268, 270);

        sc.setFill(Color.TRANSPARENT);
        regStage.initStyle(StageStyle.TRANSPARENT);
        regStage.setScene(sc);

        //fade initial
        root.setOpacity(0);
        root.setTranslateY(50);

        ParallelTransition combinedTransition = new ParallelTransition();

        // Fade transition
        FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.millis(500), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Slide transition
        TranslateTransition slideUp = new TranslateTransition(javafx.util.Duration.millis(500), root);
        slideUp.setFromY(50);
        slideUp.setToY(0);
        slideUp.setInterpolator(Interpolator.EASE_OUT);

        // Combine both transitions
        combinedTransition.getChildren().addAll(fadeIn, slideUp);

        regStage.setX(Screen.getPrimary().getVisualBounds().getWidth() - 300); // pixels from left
        regStage.setY(Screen.getPrimary().getVisualBounds().getHeight() - 250);
        regStage.show();
        combinedTransition.play();
    }
    private void getRecommendationState(){
        pollingServiceRecommendationShow = new HttpPollingService(
                "http://localhost:5000/api/getExecutedState",
                Duration.ofSeconds(2)// Poll every 2 seconds
        );

        // Handle successful updates
        pollingServiceRecommendationShow.setOnSucceeded(event -> {

            boolean showRecApp = false;

            System.out.println("Polling running..");

            if(!toolExecuted){
                if(pollingServiceRecommendationShow.getValue() != null){

                    JSONObject value = new JSONObject(pollingServiceRecommendationShow.getValue());

                    showRecApp = value.getBoolean("show");

                    if(showRecApp){
                        toolExecuted = true;
                        System.out.println("App executed");
                        fetchRecommendations();
//                ApiClient.saveRecommendationState().thenAccept(response -> {
//
//                    if(response == 200){
//                        System.out.println("App show");
//
//                    }else{
//                        System.out.println("App execute send failed!");
//                    }
//                });
                    }
                }
            }

        });

        // Handle errors
        pollingServiceRecommendationShow.setOnFailed(event -> {
            System.err.println("Polling failed: " + event.getSource().getException());
        });

        // Start the service
        pollingServiceRecommendationShow.start();
    }
    public static void main(String[] args) {
        launch();
    }
}
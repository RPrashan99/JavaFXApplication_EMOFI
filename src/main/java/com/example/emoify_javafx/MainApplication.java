package com.example.emoify_javafx;

import com.example.emoify_javafx.controllers.*;
import com.example.emoify_javafx.models.BackendConnector;
import com.example.emoify_javafx.models.RecommendationApp;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class MainApplication extends Application {
    double x, y = 0;

    private int runTimes = 0;

    private volatile boolean toolExecuted = false;
    private volatile boolean recommendationWindowOpen = false;

    private CompletableFuture<Void> pendingFetch = null;

    private List<String> recommendations = new ArrayList<>();

    private Map<String, List<String>> recommendationsApps = new HashMap<>();

    private Map<String, List<String>> recommendationsAppIcons = new HashMap<>();

    private Map<String, List<RecommendationApp>> recommendationsAppClasses = new HashMap<>();

    HttpPollingService pollingServiceRecommendationShow;

    systemStartController systemStartControllerClass = new systemStartController();;

    private boolean isRegistered = false; //check user registered before
    private boolean isOpen = false; // check main window still open
    private Process pythonProcess;//python process
    private boolean isStarted = false;

    @Override
    public void start(Stage stage) throws IOException {
        //original
//        System.out.println("Tool started");
//        Platform.runLater(this::setStateInit);
//        showWidgetWindow(stage);

        //setStateInit();
        //showRecommendationWindowWithoutPool();
        //testWindows(stage);
        //fetchRecommendations();
        //getRecommendationState();

        //showSearchQueryWindow("Relaxing video");
        //messagePortalWindow();
        testWindows(stage);
        //startEMOIFY(stage);
    }

    private void showSystemStartWindow(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxmls/systemStartWindow.fxml"));
        Parent root = loader.load();

        Stage regStage = new Stage();

        systemStartControllerClass = loader.getController();

        systemStartControllerClass.setLoadingHandler(data -> {

            regStage.close();

            if(data.equals("Start")){
                Platform.runLater(this::setStateInit);
                try {
                    showWidgetWindow(stage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
//            else if (data.equals("Register")) {
//                Platform.runLater(this::setStateInit);
//                try {
//                    showWidgetWindow(stage);
//                    showInitialLoadingWindow(stage);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }

        });

        regStage.initStyle(StageStyle.UNDECORATED);
        Scene sc = new Scene(root, 311, 156);
        sc.setFill(Color.TRANSPARENT);
        regStage.initStyle(StageStyle.TRANSPARENT);
        regStage.setScene(sc);

        regStage.show();
    }

    private void startPythonBackend() {
        try {
            // Path to your python executable and main.py
            String pythonPath = "C:\\Users\\rpras\\anaconda3\\envs\\myenv\\python.exe"; // or "python3" or full path
            //String scriptPath = "C:\\Users\\rpras\\OneDrive\\Documents\\Rashmitha\\Semester_7\\project\\FER Integration\\Facial_Emotion_Recongnition\\DesktopApp\\app.py";

//            ProcessBuilder pb = new ProcessBuilder(pythonPath, "app.py");
//
//            pb.directory(new File("C:\\Users\\rpras\\OneDrive\\Documents\\Rashmitha\\Semester_7\\project\\FER Integration\\Facial_Emotion_Recongnition\\DesktopApp"));
//            pythonProcess = pb.start();
            System.out.println("Backend starting..");

            new Thread(() -> {

                try{
                    Thread.sleep(2000);
                    if(pythonProcess != null){
                        BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()));
                        String line;
                        while (!isStarted) {
                            if((line = reader.readLine()) != null) {
                                System.out.println("Python output: " + line);
                                isStarted  = true;
                            }
                            Thread.sleep(500);
                        }
                    }
                }catch (Exception e){
                    System.out.println("Buffer read error");
                }

            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Stop backend when JavaFX application closes
        BackendConnector.stopBackend();
        if (pythonProcess != null && pythonProcess.isAlive()) {
            pythonProcess.destroy();
            System.out.println("Python process ended");
        }
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
        showSystemStartWindow(stage);
        startPythonBackend();
    }

    private void showWidgetWindow(Stage stage) throws IOException{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxmls/widgetWindow.fxml"));
        Parent root = loader.load();

        widgetController widgetController = loader.getController();

        widgetController.setWidgetOpenHandler(user -> {
            try {
                if(isRegistered){
                    showMainWindow(user);
                }else{
                    showInitialLoadingWindow(stage);
                }
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
                    showMainWindow(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Stage regStage = new Stage();
        regStage.initStyle(StageStyle.UNDECORATED);
        Scene sc = new Scene(root, 411, 319);
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
                BackendConnector.startBackend();
                showMainWindow(name);
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

    private void showMainWindow(String user) throws IOException {
        isRegistered = true;
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("mainWindow.fxml"));
        Scene sc = new Scene(fxmlLoader.load(), 600, 400);

        Stage mainStage = new Stage();
        mainStage.initStyle(StageStyle.UNDECORATED);

        //move around
        sc.setOnMousePressed(evt -> {
            x = evt.getSceneX();
            y = evt.getSceneY();
        });

        sc.setOnMouseDragged(evt -> {
            mainStage.setX(evt.getScreenX()- x);
            mainStage.setY(evt.getScreenY()- y);
        });

        mainStage.setScene(sc);
        mainStage.show();
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

        if(pendingFetch != null && !pendingFetch.isDone()){
            System.out.println("Still fetching recommendations..");
            return;
        }

        pendingFetch = ApiClient.getRecommendations().thenAccept(response -> {
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
                List<RecommendationApp> appClasses= new ArrayList<>();

                for(int j = 0; j < appRec.length(); j++){
                    JSONObject appObject = appRec.getJSONObject(j);

                    String appName = appObject.getString("app_name");
                    String path = appObject.getString("app_url");
                    String searchQuery = appObject.getString("search_query");
                    Boolean isLocal = appObject.getBoolean("isLocal");
                    System.out.println("App url: " + path);

                    RecommendationApp appClass = new RecommendationApp(appName, path, searchQuery, isLocal);
                    appClasses.add(appClass);

                    appNames.add(appName);
                    appIcons.add(path);
                }

                recommendationsApps.put(recommendation, appNames);
                recommendationsAppIcons.put(recommendation, appIcons);
                recommendationsAppClasses.put(recommendation, appClasses);

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

            //handle app based additional window open
            boolean isSearched = selectedAppHandle(data);

            if(!isSearched){
                System.out.println("No search query");
                sendMessageInfo(data.get(0), data.get(1), "");
            }

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
                Duration.ofSeconds(3)// Poll every 3 seconds
        );

        // Handle successful updates
        pollingServiceRecommendationShow.setOnSucceeded(event -> {

            boolean showRecApp = false;

            //System.out.println("Polling running..");

            if(pollingServiceRecommendationShow.getValue() != null){
                JSONObject value = new JSONObject(pollingServiceRecommendationShow.getValue());
                if(!toolExecuted ){
                    showRecApp = value.getBoolean("show");

                    if(showRecApp){
                        runTimes = runTimes + 1;
                        toolExecuted = true;
                        System.out.println("App executed: " + runTimes);
                        fetchRecommendations();
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

    //unfinished
    private boolean selectedAppHandle(List<String> selectedApp) {

        if(selectedApp != null && !selectedApp.isEmpty()){

            String rec = selectedApp.get(0);
            String app = selectedApp.get(1);

            List<RecommendationApp> recApps = recommendationsAppClasses.get(rec);

            for(int i = 0; i < recApps.size(); i++){

                if(recApps.get(i).getApp_name().equals(app)){

                    System.out.println("app url: " + recApps.get(i).getApp_url());

                    if(recApps.get(i).getApp_url().contains("<search_query>")){
                        String searchQuery = recApps.get(i).getSearch_query();
                        System.out.println("Found search query");

                        Platform.runLater(()->{
                            try {
                                showSearchQueryWindow(rec, app, searchQuery);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        return true;

                    }else{
                        return false;
                    }
                }

            }
        }else{
            return false;
        }

        return false;
    }

    private void showSearchQueryWindow(String recommendation, String selectedApp, String searchQuery) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxmls/searchView.fxml"));
        Parent root = loader.load();

        Stage regStage = new Stage();

        SearchController searchController = loader.getController();
        searchController.existingSearchQuery(searchQuery);

        searchController.setSearchSuccessHandler(query -> {

            sendMessageInfo(recommendation, selectedApp, query);
            regStage.close();

        });
        regStage.initStyle(StageStyle.UNDECORATED);

        Scene sc = new Scene(root, 347, 207);

        sc.setFill(Color.TRANSPARENT);
        regStage.initStyle(StageStyle.TRANSPARENT);
        regStage.setScene(sc);

        regStage.setX(Screen.getPrimary().getVisualBounds().getWidth() - 350); // pixels from left
        regStage.setY(Screen.getPrimary().getVisualBounds().getHeight() - 250);
        regStage.show();

    }

    private void messagePortalWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxmls/whatsappWindow.fxml"));
        Parent root = loader.load();

        messageWindowController searchController = loader.getController();

        Stage regStage = new Stage();
        regStage.initStyle(StageStyle.UNDECORATED);

        Scene sc = new Scene(root, 430, 250);

        sc.setFill(Color.TRANSPARENT);
        regStage.initStyle(StageStyle.TRANSPARENT);
        regStage.setScene(sc);

        regStage.setX(Screen.getPrimary().getVisualBounds().getWidth() - 450); // pixels from left
        regStage.setY(Screen.getPrimary().getVisualBounds().getHeight() - 280);
        regStage.show();

    }

    private void sendMessageInfo(String recommendation, String selectedApp, String searchQuery){

        ApiClient.saveRecommendationState(recommendation, selectedApp, searchQuery).thenAccept(response -> {
            if(response == 200){
                System.out.println("Message Send");

                new Thread(()-> {
                    try {
                        Thread.sleep(2000);
                        toolExecuted = false;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }else{
                System.out.println("Message send failed!");
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
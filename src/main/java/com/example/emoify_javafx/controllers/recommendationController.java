package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.ApiClient;
import com.example.emoify_javafx.HttpPollingService;
import com.example.emoify_javafx.callbacks.CallbackListener;
import com.example.emoify_javafx.models.AnimationEvent;
import com.example.emoify_javafx.models.AnimationEventBus;
import com.example.emoify_javafx.models.ExApp;
import com.example.emoify_javafx.models.User;
import com.google.common.eventbus.Subscribe;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import javafx.scene.media.AudioClip;

import static com.example.emoify_javafx.models.AnimationEvent.EventType.FINISHED;
import static com.example.emoify_javafx.models.AnimationEvent.EventType.STARTED;

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

    //btn list expand and collapse
    private Node selectedButton;
    private List<Node> originalOrder = new ArrayList<>();
    private boolean isExpanded = false;

    //new
    private boolean isMainAnimationPaused = false;
    private final List<Animation> activeAnimations = new ArrayList<>();
    private CallbackListener callback;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        AnimationEventBus.getInstance().register(this);
        //fetchRecommendationsFromApi();
        try {
            setRecommendation();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Platform.runLater(this::playSound);
    }

    public void setCallback(CallbackListener callback) {
        this.callback = callback;
    }

    public void setRecommendationsFromMain(List<String> recommendationsMain, Map<String, List<String>> recommendationsAppsMain,
                                           Map<String, List<String>> recommendationsAppIconsMain){
        originalOrder.clear();
        selectedButton = null;
        isExpanded = false;
        recommendationPane.getChildren().clear();
        recommendations = recommendationsMain;
        recommendationsApps = recommendationsAppsMain;
        recommendationsAppIcons = recommendationsAppIconsMain;
    }

    public void setRecommendationBefore(List<String> recs, Map<String, List<String>> apps, Map<String, List<String>> icons){
        recommendations = recs;
        recommendationsApps = apps;
        recommendationsAppIcons = icons;
    }

    private void setRecommendation() throws IOException {

        Platform.runLater(() -> {
            recommendationPane.getChildren().clear();

            for (int i = 0; i < recommendations.size(); i++) {
                VBox appButton = null;
                try {
                    appButton = createRecommendationButtonOptions(recommendations.get(i));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                recommendationPane.getChildren().add(appButton);

                appButton.applyCss();
                appButton.layout();
            }
            originalOrder.addAll(recommendationPane.getChildren());
        });
    }

    public void fetchRecommendationsFromApi() {
        ApiClient.getRecommendations().thenAccept(this::getRecommendations);
    }

    public void getRecommendations(String response) {
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
            System.out.println("Recommendations apps: " + recommendationsApps);
            System.out.println("Recommendations icons: " + recommendationsAppIcons);

        }
        System.out.println("Recommendations obtained: " + recommendations);
        try {
            setRecommendation();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private VBox createRecommendationButtonOptions(String recommendation) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/emoify_javafx/fxmls/expandingButton.fxml"));
        VBox pane = loader.load();

        expandingButtonController expandingButtonController = loader.getController();
        expandingButtonController.buttonSetup(recommendation,
                recommendationsAppIcons.get(recommendation),
                recommendationsApps.get(recommendation));

        expandingButtonController.setCallback(data -> {
            System.out.println("RecommendedController received: " + data);

            // Forward to MainApplication
            if (callback != null) {
                callback.onDataPassed(data);
            }
        });

        expandingButtonController.setClickHandler(this::handleRecommendationClick);

        return pane;
    }

    private void handleRecommendationClick(ActionEvent event) {

        Button clickedButton = (Button) event.getSource();

        Node node = clickedButton.getParent();

        String text = clickedButton.getText();

        if (isExpanded) {
            // Deselect - return to original state
            collapseSelection();
            System.out.println("Expand: " + text);
        } else {
            // Select new button
            expandSelection(node);
            System.out.println("Collapse: " + text);
        }
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

    //New order
    public void expandSelection(Node button) {
        selectedButton = button;
        isExpanded = true;

        // Create a list of all buttons except the selected one
        List<Node> otherButtons = recommendationPane.getChildren()
                .stream()
                .filter(node -> node != button)
                .toList();

        // Animate other buttons out
        ParallelTransition hideAnimation = new ParallelTransition();
        for (Node node : otherButtons) {
            FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.millis(300), node);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> node.setVisible(false));
            hideAnimation.getChildren().add(fadeOut);
        }

        // Move selected button to top
        recommendationPane.getChildren().remove(button);
        recommendationPane.getChildren().add(0, button);

        // Animate selected button (optional scale effect)
        ScaleTransition scaleUp = new ScaleTransition(javafx.util.Duration.millis(300), button);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);

        SequentialTransition sequence = new SequentialTransition(
                new ParallelTransition(hideAnimation, scaleUp)
        );
        registerAnimation(sequence);
        sequence.play();
    }

    //Original order
    private void collapseSelection() {
        isExpanded = false;

        // Make all buttons visible again (but still transparent)
        for (Node node : recommendationPane.getChildren()) {
            node.setVisible(true);
        }

        // Animate other buttons in
        ParallelTransition showAnimation = new ParallelTransition();
        for (Node node : recommendationPane.getChildren()) {
            if (node != selectedButton) {
                FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.millis(300), node);
                fadeIn.setToValue(1);
                showAnimation.getChildren().add(fadeIn);
            }
        }

        // Animate selected button back to normal
        ScaleTransition scaleDown = new ScaleTransition(javafx.util.Duration.millis(300), selectedButton);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        // Restore original order
        SequentialTransition sequence = new SequentialTransition(
                new ParallelTransition(showAnimation, scaleDown),
                new PauseTransition(javafx.util.Duration.millis(100)), // Small delay
                new Transition() {
                    {
                        setCycleDuration(javafx.util.Duration.millis(1));
                    }
                    protected void interpolate(double frac) {
                        recommendationPane.getChildren().setAll(originalOrder);
                    }
                }
        );
        registerAnimation(sequence);
        sequence.play();
    }

    @Subscribe
    public void onButtonAnimationEvent(AnimationEvent event) {
        if (event.getType() == STARTED) {
            // Pause main animations while button animates
            System.out.println("Animation paused");
        } else if (event.getType() == FINISHED) {
            // Resume main animations
            System.out.println("Animation started");
            resumeMainAnimations();
        }
    }

    private void resumeMainAnimations(){
        if (!isMainAnimationPaused) return;

        activeAnimations.forEach(animation -> {
            if (animation.getStatus() == Animation.Status.PAUSED) {
                animation.play();
            }
        });

        isMainAnimationPaused = false;
        System.out.println("Main animations resumed");
    }

    private void pauseAnimations(){
        if (isMainAnimationPaused) return;

        activeAnimations.forEach(animation -> {
            if (animation.getStatus() == Animation.Status.RUNNING) {
                animation.pause();
            }
        });

        isMainAnimationPaused = true;
        System.out.println("Main animations paused");
    }

    private void registerAnimation(Animation animation) {
        activeAnimations.add(animation);

        // Clean up when animation finishes
        animation.statusProperty().addListener((obs, oldStatus, newStatus) -> {
            if (newStatus == Animation.Status.STOPPED) {
                activeAnimations.remove(animation);
            }
        });
    }

    @FXML
    void handleCloseBtn(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    public void playSound() {
        // Load the sound file (should be in your resources folder)
        AudioClip sound = new AudioClip(getClass().getResource("/com/example/emoify_javafx/sounds/happy_meme.wav").toString());

        // Play the sound
        sound.play();

        // You can also control volume (0.0 to 1.0)
        sound.setVolume(0.7);

        // For continuous play
        sound.setCycleCount(2);
    }
}

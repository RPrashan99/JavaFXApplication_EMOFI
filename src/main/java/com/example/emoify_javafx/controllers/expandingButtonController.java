package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.ApiClient;
import com.example.emoify_javafx.callbacks.CallbackListener;
import com.example.emoify_javafx.models.AnimationEvent;
import com.example.emoify_javafx.models.AnimationEventBus;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

import static com.example.emoify_javafx.models.AnimationEvent.EventType.FINISHED;
import static com.example.emoify_javafx.models.AnimationEvent.EventType.STARTED;

public class expandingButtonController {

    @FXML private VBox mainContainer;
    @FXML private Button mainButton;
    @FXML private HBox hiddenButtonsContainer;
    @FXML private Button option1, option2, option3;
    @FXML private ImageView option1Icon, option2Icon, option3Icon;

    private boolean isExpanded = false;
    private double expandedHeight;

    private List<String>apps = new ArrayList<>();
    private String recommendation;

    private Consumer<ActionEvent> clickHandler;

    private CallbackListener callback;

    @FXML
    public void initialize() {
        // Initially hide the options
        hiddenButtonsContainer.setVisible(false);
        hiddenButtonsContainer.setManaged(false);

        // Calculate expanded height
        hiddenButtonsContainer.applyCss();
        hiddenButtonsContainer.layout();
        expandedHeight = hiddenButtonsContainer.getHeight() + mainContainer.getSpacing();
    }

    public void setCallback(CallbackListener callback) {
        this.callback = callback;
    }

    public void setClickHandler(Consumer<ActionEvent> handler) {
        this.clickHandler = handler;
        mainButton.setOnAction(event -> {
            handleMainButtonClick();
            handler.accept(event);
        });
    }

    private void handleMainButtonClick() {
        AnimationEventBus.getInstance().post(new AnimationEvent(STARTED, mainButton));
        if (isExpanded) {
            System.out.println("Recom btn: " + mainButton.getText() + " collapsed");
            collapse();
        } else {
            System.out.println("Recom btn: " + mainButton.getText() + " expanded");
            expand();
        }
        isExpanded = !isExpanded;
    }

    private void expand() {
        // Make container visible before animation
        hiddenButtonsContainer.setVisible(true);
        hiddenButtonsContainer.setManaged(true);

        // Create parallel transition
        ParallelTransition expandTransition = new ParallelTransition();

        // Fade in options
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), hiddenButtonsContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Slide down effect
        TranslateTransition slideDown = new TranslateTransition(Duration.millis(300), hiddenButtonsContainer);
        slideDown.setFromY(-20);
        slideDown.setToY(0);

        // Scale main button
        ScaleTransition scaleButton = new ScaleTransition(Duration.millis(300), mainButton);
        scaleButton.setToX(1.05);
        scaleButton.setToY(1.05);

        expandTransition.getChildren().addAll(fadeIn, slideDown, scaleButton);

        expandTransition.setOnFinished(e -> {
            AnimationEventBus.getInstance().post(new AnimationEvent(FINISHED, mainButton));
        });

        expandTransition.play();

        // Update main button text
        mainButton.setText("Hide Options");
    }

    private void collapse() {
        ParallelTransition collapseTransition = new ParallelTransition();

        // Fade out options
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), hiddenButtonsContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        // Slide up effect
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(250), hiddenButtonsContainer);
        slideUp.setFromY(0);
        slideUp.setToY(-20);

        // Scale main button back
        ScaleTransition scaleButton = new ScaleTransition(Duration.millis(250), mainButton);
        scaleButton.setToX(1.0);
        scaleButton.setToY(1.0);

        collapseTransition.getChildren().addAll(fadeOut, slideUp, scaleButton);
        collapseTransition.setOnFinished(e -> {
            hiddenButtonsContainer.setVisible(false);
            hiddenButtonsContainer.setManaged(false);

            AnimationEventBus.getInstance().post(new AnimationEvent(FINISHED, mainButton));

        });

        collapseTransition.play();

        // Update main button text
        mainButton.setText(recommendation);
    }

    public void buttonSetup(String recommendationText, List<String> icons, List<String>appNames){
        recommendation = recommendationText;
        apps=appNames;
        setMainButtonText(recommendationText);
        setSubButtons(icons, appNames);
    }

    private void setSubButtons(List<String> icons, List<String> appNames){

        String defaultImage = "/com/example/emoify_javafx/icons/default_app.png";
        String defaultPath = "/com/example/emoify_javafx/icons/";

        option3.setVisible(false);
        option3.setManaged(false);

        if(!icons.isEmpty()){
            option1.setText(appNames.get(0));
            try{
                String app1 = appNames.get(0).toLowerCase(Locale.ROOT);
                Image image1 = new Image(getClass().getResourceAsStream(defaultPath + app1 + ".png"));
                option1Icon.setImage(image1);
            }catch (Exception e){
                Image image1 = new Image(getClass().getResourceAsStream(defaultImage));
                option1Icon.setImage(image1);
            }

            option2.setText(appNames.get(1));
            try{
                String app2 = appNames.get(1).toLowerCase(Locale.ROOT);
                Image image2 = new Image(getClass().getResourceAsStream(defaultPath + app2 + ".png"));
                option2Icon.setImage(image2);
            }catch (Exception e){
                Image image2 = new Image(getClass().getResourceAsStream(defaultImage));
                option2Icon.setImage(image2);
            }

            if(icons.size() > 2){

                option3.setVisible(true);
                option3.setManaged(true);
                option3.setText(appNames.get(2));
                try{
                    String app3 = appNames.get(2).toLowerCase(Locale.ROOT);
                    Image image3 = new Image(getClass().getResourceAsStream(defaultPath + app3 + ".png"));
                    option3Icon.setImage(image3);
                }catch (Exception e){
                    Image image3 = new Image(getClass().getResourceAsStream(defaultImage));
                    option3Icon.setImage(image3);
                }
            }

        }else{
            System.out.println("Icons set is null");
        }

    }

    public void setMainButtonText(String text) {
        mainButton.setText(text);
    }

    private void sendRecommendationOutput(String selectedApp){
        ApiClient.saveRecommendationState(recommendation, selectedApp).thenAccept(response -> {
            if(response == 200){
                System.out.println("App Send");
            }else{
                System.out.println("App send failed!");
            }
        });
    }

    @FXML
    void handleOption1Clicked(MouseEvent event) {
        String selectedApp = apps.get(0);
        System.out.println("Pressed app: " + selectedApp);
        sendRecommendationOutput(selectedApp);

        if (callback != null) {
            callback.onDataPassed(selectedApp);
        }

//        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        stage.close();
    }

    @FXML
    void handleOption2Clicked(MouseEvent event) {
        String selectedApp = apps.get(1);
        System.out.println("Pressed app: " + selectedApp);
        sendRecommendationOutput(selectedApp);

        if (callback != null) {
            callback.onDataPassed(selectedApp);
        }

//        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        stage.close();
    }

    @FXML
    void handleOption3Clicked(MouseEvent event) {
        String selectedApp = apps.get(2);
        System.out.println("Pressed app: " + selectedApp);
        sendRecommendationOutput(selectedApp);

        if (callback != null) {
            callback.onDataPassed(selectedApp);
        }

//        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        stage.close();
    }

    void handleCloseBtn(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}

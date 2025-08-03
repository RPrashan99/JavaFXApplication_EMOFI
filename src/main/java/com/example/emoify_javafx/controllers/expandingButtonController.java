package com.example.emoify_javafx.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

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

    @FXML
    private void handleMainButtonClick() {
        if (isExpanded) {
            collapse();
        } else {
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

        if(!icons.isEmpty()){

            Image image1 = new Image(getClass().getResourceAsStream(icons.get(0)));
            if(!image1.isError()){
                option1Icon.setImage(image1);
            }

            Image image2 = new Image(getClass().getResourceAsStream(icons.get(1)));
            if(!image2.isError()){
                option2Icon.setImage(image2);
            }

            Image image3 = new Image(getClass().getResourceAsStream(icons.get(2)));
            if(!image3.isError()){
                option3Icon.setImage(image3);
            }

        }else{
            System.out.println("Icons set is null");
        }

    }

    public void setMainButtonText(String text) {
        mainButton.setText(text);
    }

    @FXML
    void handleOption1Clicked(MouseEvent event) {
        System.out.println("Pressed app: " + apps.get(0));
    }

    @FXML
    void handleOption2Clicked(MouseEvent event) {
        System.out.println("Pressed app: " + apps.get(1));
    }

    @FXML
    void handleOption3Clicked(MouseEvent event) {
        System.out.println("Pressed app: " + apps.get(2));
    }
}

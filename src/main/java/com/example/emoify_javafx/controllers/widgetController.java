package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.models.User;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.function.Consumer;

public class widgetController {
    @FXML
    private Button mainBtn;
    @FXML private Button openBtn;
    @FXML private Button exitBtn;
    @FXML private VBox subBtnContainer;

    private boolean isExpanded = false;

    private Consumer<String> widgetOpenHandler;

    @FXML
    public void initialize() {
        // Initially hide the subBtnContainer
        subBtnContainer.setOpacity(0);
        subBtnContainer.setVisible(false);

        mainBtn.setOnAction(event -> toggleSubButtons());
        openBtn.setOnAction(event -> toggleSubButtons());
    }

    public void setWidgetOpenHandler(Consumer<String> handler) {
        this.widgetOpenHandler = handler;
    }

    private void toggleSubButtons() {
        if (isExpanded) {
            collapseSubButtons();
        } else {
            expandSubButtons();
        }
    }

    private void expandSubButtons() {
        subBtnContainer.setVisible(true);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), subBtnContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), subBtnContainer);
        slideIn.setFromX(100);  // start 100px to the right
        slideIn.setToX(0);

        ParallelTransition show = new ParallelTransition(fadeIn, slideIn);
        show.play();

        isExpanded = true;
    }

    private void collapseSubButtons() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), subBtnContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), subBtnContainer);
        slideOut.setFromX(0);  // start 100px to the right
        slideOut.setToX(100);

        ParallelTransition hide = new ParallelTransition(fadeOut, slideOut);
        hide.setOnFinished(event -> subBtnContainer.setVisible(false));
        hide.play();

        isExpanded = false;
    }
    @FXML
    void handleCloseBtn(MouseEvent event){
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleOpenBtn(MouseEvent event){

        if (widgetOpenHandler != null) {
            widgetOpenHandler.accept("Open");
        }
    }
}

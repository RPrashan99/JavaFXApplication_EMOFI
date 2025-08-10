package com.example.emoify_javafx.controllers;

import com.jfoenix.controls.JFXToggleButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.util.Locale;
import java.util.function.Consumer;

public class exAppCardController {

    @FXML
    private JFXToggleButton appBtn;

    @FXML
    private ImageView appIcon;

    @FXML
    private Label appNameLabel;

    @FXML
    private Label ytAvailableLabel;

    private Consumer<Boolean> appEnableHandler;

    public void setAppValues(String appName, String category, Boolean isAvailable, String icon){
        appNameLabel.setText(appName);
        appBtn.setSelected(isAvailable);
        ytAvailableLabel.setText(category);

        setImageIcon(appName);
    }

    private void setImageIcon(String appName){

        String defaultImage = "/com/example/emoify_javafx/icons/default_app.png";
        String defaultPath = "/com/example/emoify_javafx/icons/";

        try{
            String app = appName.toLowerCase(Locale.ROOT);
            Image image = new Image(getClass().getResourceAsStream(defaultPath + app + ".png"));
            appIcon.setImage(image);
        }catch (Exception e){
            Image image = new Image(getClass().getResourceAsStream(defaultImage));
            appIcon.setImage(image);
        }
    }

    public void setAppEnableHandler(Consumer<Boolean> handler) {
        this.appEnableHandler = handler;
    }

    @FXML
    void onAppBtnPressed(MouseEvent event) {
        boolean isSelected = appBtn.isSelected();
        if(appEnableHandler != null){
            appEnableHandler.accept(isSelected);
        }
    }

}

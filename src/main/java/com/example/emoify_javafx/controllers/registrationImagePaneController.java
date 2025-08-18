package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.ApiClient;
import com.example.emoify_javafx.models.RegistrationData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class registrationImagePaneController {


    @FXML
    private Button addAppBtn;

    private RegistrationData registrationData;

    public boolean validateInputs(){

        boolean isValidated = true;

        return isValidated;
    }

    public void setRegistrationData(RegistrationData data) {
        this.registrationData = data;
    }

    @FXML
    void handleAddAppBtn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/emoify_javafx/fxmls/addAppWindow.fxml"));
            Parent root = loader.load();

            addAppController controller = loader.getController();
            controller.setUserName("userName");

            controller.setAppSaveHandler(apps -> {
                registrationData.setApps(apps);
            });

            Stage stage = new Stage();
            stage.setTitle("Add Applications");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.models.RegistrationData;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class registrationPaneController {

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private TextField phoneNumberTextField;

    @FXML
    private AnchorPane registrationAnchorPane;

    @FXML
    private TextField userNameTextField;

    private RegistrationData registrationData;

    public void setRegistrationData(RegistrationData data) {
        this.registrationData = data;
        bindFields();
    }

    private void bindFields() {
        userNameTextField.textProperty().bindBidirectional(registrationData.usernameProperty());
        passwordTextField.textProperty().bindBidirectional(registrationData.passwordProperty());
        phoneNumberTextField.textProperty().bindBidirectional(registrationData.phoneNumberProperty());
    }

    public boolean validateInputs(){

        boolean isValidated;

        if(!userNameTextField.getText().isEmpty() && !passwordTextField.getText().isEmpty() && !phoneNumberTextField.getText().isEmpty()){
            isValidated = true;
        }else{
            isValidated = false;
        }

        return isValidated;
    }
}

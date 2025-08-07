package com.example.emoify_javafx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class messageWindowController implements Initializable {

    @FXML
    private Label appMessagePortal;

    @FXML
    private TextField messageField;

    @FXML
    private Button openBtn;

    @FXML
    private TextField phoneField;

    @FXML
    private BorderPane rootPane;

    @FXML
    private Button sendBtn;

    @FXML
    void handleCloseBtn(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleSendBtn(MouseEvent event) {
        
        if(phoneField.getText().isEmpty()){
            phoneField.setStyle("-fx-border-color: red; -fx-border-radius: 6; -fx-background-radius: 6;");
        } else if (messageField.getText().isEmpty()) {
            messageField.setStyle("-fx-border-color: red; -fx-border-radius: 6; -fx-background-radius: 6;");
        } else{
            System.out.println("Phone: " + phoneField.getText() + " Message: " + messageField.getText());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        }

    }

    @FXML
    void handleOpenBtn(MouseEvent event){
        System.out.println("Open Application");
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();

    }

    private void phoneNumberFormatter(){
        Pattern pattern = Pattern.compile("\\+94 \\d{0,7}");

        // TextFormatter filter to restrict input
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (pattern.matcher(newText).matches()) {
                return change;
            }
            return null;
        };

        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        phoneField.setTextFormatter(textFormatter);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        phoneNumberFormatter();
    }
}


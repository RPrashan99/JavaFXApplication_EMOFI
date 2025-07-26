package com.example.emoify_javafx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private Button appsButton;

    @FXML
    private Button closeBtn;

    @FXML
    private AnchorPane contentPane;

    @FXML
    private Button homeButton;

    @FXML
    private Button settingsButton;

    @FXML
    void handleCloseBtn(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    void setContent(String fxmlPath){
        try {
            AnchorPane newContent = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(newContent);
            AnchorPane.setTopAnchor(newContent, 0.0);
            AnchorPane.setBottomAnchor(newContent, 0.0);
            AnchorPane.setLeftAnchor(newContent, 0.0);
            AnchorPane.setRightAnchor(newContent, 0.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void loadHomeWindow() {
        setContent("fxmls/homeSubWindow.fxml");
    }

    @FXML
    private void loadAppsWindow() {
        setContent("fxmls/exAppWindow.fxml");
    }

//    @FXML
//    private void loadSettingsWindow() {
//        setContent("Settings.fxml");
//    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadHomeWindow();
    }
}
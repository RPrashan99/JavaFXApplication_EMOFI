package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.ApiClient;
import com.example.emoify_javafx.models.RegistrationData;
import com.example.emoify_javafx.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class registrationController implements Initializable {

    @FXML
    private AnchorPane registrationPane;
    @FXML
    private Button registrationBtn;
    @FXML
    private Button closeBtn;
    @FXML
    private ImageView registrationBtnIcon;
    @FXML
    private Label paneName;

    private Consumer<User> registrationSuccessHandler;
    private registrationPaneController registrationPaneControllerClass;
    private registrationImagePaneController registrationImagePaneControllerClass;

    private AnchorPane currentPane;
    private String path = "/com/example/emoify_javafx/fxmls/";
    private RegistrationData registrationData = new RegistrationData();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setPanes(path + "registrationPane.fxml");
    }

    public void setRegistrationSuccessHandler(Consumer<User> handler) {
        this.registrationSuccessHandler = handler;
    }

    @FXML
    void setPanes(String fxmlPath){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane newPane = loader.load();

            if(fxmlPath.equals(path + "registrationPane.fxml")){
                registrationPaneControllerClass = loader.getController();
                registrationPaneControllerClass.setRegistrationData(registrationData);
            }else if(fxmlPath.equals(path + "registrationImagePane.fxml")){
                registrationImagePaneControllerClass = loader.getController();
                //personalInfoController.setRegistrationData(registrationData);
            }

            registrationPane.getChildren().setAll(newPane);
            AnchorPane.setTopAnchor(newPane, 0.0);
            AnchorPane.setBottomAnchor(newPane, 0.0);
            AnchorPane.setLeftAnchor(newPane, 0.0);
            AnchorPane.setRightAnchor(newPane, 0.0);
            currentPane = newPane;
            updateRegistrationBtn();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void registrationBtnOnClicked(MouseEvent event){
        if(currentPane.getId().equals("registrationAnchorPane")){
            if(registrationPaneControllerClass.validateInputs()){
                setPanes(path + "registrationImagePane.fxml");
            }
        }else if(currentPane.getId().equals("registrationImageAnchorPane")){
            if(registrationImagePaneControllerClass.validateInputs()){
                completeRegistration();
            }
        }
    }

    private void completeRegistration(){

        saveUserData();

        Stage stage = (Stage) registrationBtnIcon.getScene().getWindow();
        stage.close();

        User newUser = new User(registrationData.getUsername(), registrationData.getPassword(), registrationData.getPhoneNumber());

        if (registrationSuccessHandler != null) {
            registrationSuccessHandler.accept(newUser);
        }
    }

    private void saveUserData(){

        String uName = registrationData.getUsername();
        String pass = registrationData.getPassword();
        String pNum = registrationData.getPhoneNumber();

        ApiClient.saveUserData(uName, pass, pNum).thenAccept(response -> {

            if(response == 200){
                System.out.println("Save new User Data success!");
            }else{
                System.out.println("Save new User Data failed!");
            }
        });
    }

    private void updateRegistrationBtn(){

        if(currentPane.getId().equals("registrationImageAnchorPane")){
            String imagePath = "/com/example/emoify_javafx/icons/checkmark.png";
            Image newImage = new Image(getClass().getResourceAsStream(imagePath));
            registrationBtnIcon.setImage(newImage);

        }else{
            String imagePath = "/com/example/emoify_javafx/icons/right-arrow.png";
            Image newImage = new Image(getClass().getResourceAsStream(imagePath));
            registrationBtnIcon.setImage(newImage);
        }
    }

    @FXML
    void handleCloseBtn(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}

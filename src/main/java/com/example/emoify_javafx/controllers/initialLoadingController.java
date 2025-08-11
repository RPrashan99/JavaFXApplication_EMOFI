package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.ApiClient;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class initialLoadingController implements Initializable {

    @FXML
    private ImageView appIcon;

    @FXML
    private Button ctnBtn;

    @FXML
    private ImageView loadingIcon;

    @FXML
    private Label loadingLabel;

    @FXML
    private AnchorPane registrationAnchorPane;

    private Consumer<String> loginOpenHandler;

    private String output;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ctnBtn.setVisible(false);
        playImageViewAnimation(appIcon);
        checkLogin();
    }

    public void setLoginOpenHandler(Consumer<String> handler) {
        this.loginOpenHandler = handler;
    }

    private void playImageViewAnimation(ImageView imageView) {
        // Fade in and out
        FadeTransition fade = new FadeTransition(Duration.seconds(2), imageView);
        fade.setFromValue(0.3);
        fade.setToValue(1.0);
        fade.setCycleCount(Animation.INDEFINITE);
        fade.setAutoReverse(true);

        // Slight horizontal movement (left-right)
        TranslateTransition translate = new TranslateTransition(Duration.seconds(2), imageView);
        translate.setFromX(-5);  // Move slightly to the left
        translate.setToX(5);     // Move slightly to the right
        translate.setCycleCount(Animation.INDEFINITE);
        translate.setAutoReverse(true);
        translate.setInterpolator(Interpolator.EASE_BOTH);

        // Play both together
        ParallelTransition animation = new ParallelTransition(fade, translate);
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();
    }

    private void checkLogin(){
        String imagePath = "/com/example/emoify_javafx/icons/Success.gif";
        Image image = new Image(getClass().getResourceAsStream(imagePath));

        ApiClient.getLogin().thenAccept(response -> {

            JSONObject jsonObject = new JSONObject(response);

            String message = jsonObject.getString("message");

            if(!message.equals("No users available")){
                JSONArray jsonArray = jsonObject.getJSONArray("user");

                List<Object> list = jsonArray.toList();

                String userName = list.get(1).toString();

                System.out.println("Username: " + userName);

                Platform.runLater(() -> {
                    loadingIcon.setImage(image);

                    loadingLabel.setText("Loading successful");
                    ctnBtn.setText("Login");
                    ctnBtn.setVisible(true);

                    output = userName;
                });

            }else{
                loadingIcon.setImage(image);

                loadingLabel.setText("Loading successful");
                ctnBtn.setVisible(true);
                ctnBtn.setText("Register");
                output = "Register";
            }
        });
    }

    @FXML
    void handleLoginBtn(MouseEvent event){

        Stage stage = (Stage) ctnBtn.getScene().getWindow();
        stage.close();

        if (loginOpenHandler != null) {
            loginOpenHandler.accept(output);
        }
    }

}

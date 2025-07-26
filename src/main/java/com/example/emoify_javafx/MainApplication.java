package com.example.emoify_javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MainApplication extends Application {
    double x, y = 0;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("mainWindow.fxml"));
        Scene sc = new Scene(fxmlLoader.load(), 600, 400);
        stage.initStyle(StageStyle.UNDECORATED);

        //move around
        sc.setOnMousePressed(evt -> {
            x = evt.getSceneX();
            y = evt.getSceneY();
        });

        sc.setOnMouseDragged(evt -> {
            stage.setX(evt.getScreenX()- x);
            stage.setY(evt.getScreenY()- y);
        });

        stage.setScene(sc);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
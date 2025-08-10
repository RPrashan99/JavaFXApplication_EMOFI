package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.ApiClient;
import com.example.emoify_javafx.models.ExApp;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import com.jfoenix.controls.JFXToggleButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class exAppController implements Initializable {

    @FXML
    private ScrollPane scrollAppPane;

    private ObservableList<ExApp> exApps;

    private List<ExApp> exAppsList = new ArrayList<>();

    private Map<String, Label>appLabels;
    private Map<String, JFXToggleButton>appButtons;

    private String userName = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        appLabels = new HashMap<>();
        appButtons = new HashMap<>();

        setAppBox();
//        createButtonList();
//        createLabelList();

//        updateAppDetails();
    }
    private void updateAppDetails(){
        ApiClient.getAppDetails().thenAccept(response -> {
            JSONArray appsArray = new JSONArray(response);

            for (int i = 0; i < appsArray.length(); i++) {
                JSONObject appObject = appsArray.getJSONObject(i);
                String appName = appObject.getString("name");
                String path = appObject.getString("path");
                String iconPath = appObject.getString("icon");
                Boolean access = appObject.getBoolean("isAccessGiven");
                Boolean available = appObject.getBoolean("isAvailable");

                ExApp newApp = new ExApp(appName, path, iconPath, access, available);
                exAppsList.add(newApp);
            }

            System.out.println("app list: " + exAppsList);

            setAppDetails();
        });
    }

    private void setAppBox(){
        GridPane appPane = new GridPane();
        appPane.setHgap(10); // Horizontal gap between columns
        appPane.setVgap(10); // Vertical gap between rows

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setPercentWidth(50); // Each column takes 50% of width
        col2.setPercentWidth(50);
        appPane.getColumnConstraints().addAll(col1, col2);

        appPane.setPadding(new Insets(10, 10, 10, 10));

        // Sample data - in a real app this would come from your data source
        String[] buttonData = {
                "Button 1",
                "Button 2",
                "Button 3",
                "Button 4",
                "Button 5",
                "Button 6",
                "Button 7",
                "Button 8",
                "Button 9"
        };

        // Dynamically add HBoxes with buttons
        for (int i = 0; i < buttonData.length; i++) {
            HBox hbox = new HBox(10); // 10 is the spacing between buttons
            hbox.setHgrow(hbox, Priority.ALWAYS); // Make HBox grow horizontally
            hbox.setStyle("""
                -fx-background-color: #f8f9fa;
                -fx-background-radius: 10;
                -fx-border-radius: 10;
                -fx-border-color: #e0e0e0;
                -fx-border-width: 1;
                -fx-padding: 15;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);
            """);

            // Set uniform height constraints
            hbox.setMinHeight(40); // Minimum height
            hbox.setPrefHeight(40); // Preferred height
            hbox.setMaxHeight(40);

            // Create buttons for this row
            JFXToggleButton btn1 = new JFXToggleButton();
            Label btnLabel = new Label(buttonData[i]);

            // Make buttons grow to fill available space
            HBox.setHgrow(btn1, Priority.ALWAYS);
            btn1.setPrefHeight(40);

            hbox.getChildren().addAll(btnLabel, btn1);
            int row = 0;
            if(i < 2){
                row = 0;
            }else{
                row = i / 2;
            }
            System.out.println("i: " + i + " row: " + row );

            if(i % 2 == 0){
                appPane.add(hbox, 0, row); // Add to grid in column 0, current row
            }else{
                appPane.add(hbox, 1, row); // Add to grid in column 0, current row
            }
        }

        scrollAppPane.setContent(appPane);
        scrollAppPane.setFitToWidth(true); // Makes content fill viewport width
        scrollAppPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // No horizontal scroll

        // IMPORTANT: Let GridPane grow vertically
        scrollAppPane.setContent(appPane);
        GridPane.setVgrow(appPane, Priority.ALWAYS);

        GridPane.setVgrow(appPane, Priority.ALWAYS);
        appPane.setMaxWidth(Double.MAX_VALUE);

        // Wrap in VBox to ensure proper sizing (optional but recommended)
//        VBox root = new VBox(scrollPane);
//        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private void setAppDetails(){
        Platform.runLater(()-> {
            if(!exAppsList.isEmpty()){
                for (int i = 0; i < exAppsList.size(); i++){
                    ExApp app = exAppsList.get(i);
                    String appName = app.getAppName();
                    boolean access = app.isIsAccessGiven();
                    boolean available = app.isIsAvailable();

                    JFXToggleButton btn = appButtons.get(appName);
                    if(access){
                        btn.setSelected(true);
                    }else{
                        btn.setSelected(false);
                    }

                    Label lb = appLabels.get(appName);
                    if(available){
                        lb.setText("Available");
                        lb.setTextFill(Color.web("#15d929"));
                    } else {
                        lb.setText("Not Available");
                        lb.setTextFill(Color.web("#842424"));
                        btn.setDisable(true);
                    }
                }
            }
        });
    }

    public void setUserName(String user){
        userName = user;
    }

    @FXML
    void handleAddApp(MouseEvent event){

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/emoify_javafx/fxmls/addAppWindow.fxml"));
                Parent root = loader.load();

                addAppController controller = loader.getController();
                controller.setUserName(userName);

                Stage stage = new Stage();
                stage.setTitle("Add Applications");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }


//        ApiClient.openAddApp(userName).thenAccept(reposnse -> {
//
//            System.out.println("Response: " + reposnse.toString());
//
//        });

    }
}

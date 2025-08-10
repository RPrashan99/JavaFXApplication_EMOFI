package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.ApiClient;
import com.example.emoify_javafx.models.ExApp;
import com.sun.jna.platform.win32.Mpr;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

    @FXML
    private Button applyBtn;

    private ObservableList<ExApp> exApps;

    private List<ExApp> exAppsList = new ArrayList<>();

    private Map<String, Boolean> appEnableList = new HashMap<>();
    private Map<String, Boolean> appChangedList = new HashMap<>();

    private String userName = "";



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        applyBtn.setVisible(false);
        updateAppDetails();
    }
    private void updateAppDetails(){
        exAppsList.clear();
        appEnableList.clear();

        ApiClient.getAppDetails().thenAccept(response -> {
            JSONObject appsObject = new JSONObject(response);

            JSONArray appsArray = appsObject.getJSONArray("apps");

            for (int i = 0; i < appsArray.length(); i++) {

                JSONObject appObject = appsArray.getJSONObject(i);
                String appName = appObject.getString("name");
                String path = appObject.getString("path");
                Boolean isLocal = appObject.getBoolean("isLocal");
                String category = appObject.getString("category");
                //String iconPath = appObject.getString("icon");
                Boolean available = appObject.getBoolean("isAvailable");

                //dummy value
                String iconPath = "NO ICON";
                ExApp newApp = new ExApp(appName, path, iconPath, category, available, isLocal);
                exAppsList.add(newApp);
            }

            System.out.println("app list: " + exAppsList);

            Platform.runLater(()->{
                try {
                    setAppBox();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    private void setAppBox() throws IOException {
        GridPane appPane = new GridPane();
        appPane.setHgap(10); // Horizontal gap between columns
        appPane.setVgap(10); // Vertical gap between rows

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setPercentWidth(50); // Each column takes 50% of width
        col2.setPercentWidth(50);
        appPane.getColumnConstraints().addAll(col1, col2);

        appPane.setPadding(new Insets(10, 10, 10, 10));

        // Dynamically add HBoxes with buttons
        for (int i = 0; i < exAppsList.size(); i++) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/emoify_javafx/fxmls/exAppCard.fxml"));
                HBox appCard = loader.load();

                ExApp app = exAppsList.get(i);

                exAppCardController exAppCardController =  loader.getController();
                exAppCardController.setAppValues(app.getAppName(), app.getCategory(), app.isIsAvailable(), app.getIconPath());

                exAppCardController.setAppEnableHandler(isSelected -> {
                    System.out.println("Changed");
                    if(appChangedList.containsKey(app.getAppName())){
                        appChangedList.remove(app.getAppName());
                    }else{
                        appChangedList.put(app.getAppName(), isSelected);
                    }
                    if(!appChangedList.isEmpty()){
                        applyBtn.setVisible(true);
                    }else{
                        applyBtn.setVisible(false);
                    }
                });

                int row = 0;
                if(i < 2){
                    row = 0;
                }else{
                    row = i / 2;
                }
                System.out.println("i: " + i + " row: " + row );

                if(i % 2 == 0){
                    appPane.add(appCard, 0, row); // Add to grid in column 0, current row
                }else{
                    appPane.add(appCard, 1, row); // Add to grid in column 0, current row
                }
            }catch (IOException e){
                System.out.println("Error: " + e);
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

                controller.setAppSubmitHandler(data -> {

                    updateAppDetails();

                });

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

    @FXML
    void handleApplyBtn(MouseEvent event){

        System.out.println("Changed list: " + appChangedList);
        List<String> changedApps = new ArrayList<>();
        Map<String, Boolean> changedValues = new HashMap<String, Boolean>();

        for(int i = 0; i < exAppsList.size(); i++){
            String appName = exAppsList.get(i).getAppName();

            if(appChangedList.containsKey(appName)){
                changedApps.add(appName);
                changedValues.put(appName, appChangedList.get(appName));
            }
        }

        ApiClient.setAppSettings(changedApps, changedValues).thenAccept(response -> {

            if(response == 200){
                System.out.println("Save new App Data success!");
                applyBtn.setVisible(false);
            }else{
                System.out.println("Save new App Data failed!");
            }
        });

    }
}

package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.ApiClient;
import com.example.emoify_javafx.models.ExApp;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import com.jfoenix.controls.JFXToggleButton;
import javafx.scene.paint.Color;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.*;

public class exAppController implements Initializable {

    @FXML
    private Label discordAvailableLabel;

    @FXML
    private JFXToggleButton discordBtn;

    @FXML
    private Label solitaireAvailableLabel;

    @FXML
    private JFXToggleButton solitaireBtn;

    @FXML
    private Label spotifyAvailableLabel;

    @FXML
    private JFXToggleButton spotifyBtn;

    @FXML
    private Label teamsAvailableLabel;

    @FXML
    private JFXToggleButton teamsBtn;

    @FXML
    private Label telegramAvailableLabel;

    @FXML
    private JFXToggleButton telegramBtn;

    @FXML
    private Label whatsappAvailableLabel;

    @FXML
    private JFXToggleButton whatsappBtn;

    @FXML
    private JFXToggleButton youtubeBtn;

    @FXML
    private Label ytAvailableLabel;

    @FXML
    private Label zoomAvailableLabel;

    @FXML
    private JFXToggleButton zoomBtn;

    private ObservableList<ExApp> exApps;

    private List<ExApp> exAppsList = new ArrayList<>();

    private Map<String, Label>appLabels;
    private Map<String, JFXToggleButton>appButtons;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        appLabels = new HashMap<>();
        appButtons = new HashMap<>();
        createButtonList();
        createLabelList();

        updateAppDetails();
    }

    private void createLabelList(){
        appLabels.put("Youtube", ytAvailableLabel);
        appLabels.put("Telegram", teamsAvailableLabel);
        appLabels.put("Whatsapp", whatsappAvailableLabel);
        appLabels.put("Zoom", zoomAvailableLabel);
        appLabels.put("Spotify", spotifyAvailableLabel);
        appLabels.put("Teams", teamsAvailableLabel);
        appLabels.put("Discord", discordAvailableLabel);
        appLabels.put("Solitaire", solitaireAvailableLabel);
    }

    private void createButtonList(){
        appButtons.put("Youtube", youtubeBtn);
        appButtons.put("Telegram", telegramBtn);
        appButtons.put("Whatsapp", whatsappBtn);
        appButtons.put("Zoom", zoomBtn);
        appButtons.put("Spotify", spotifyBtn);
        appButtons.put("Teams", teamsBtn);
        appButtons.put("Discord", discordBtn);
        appButtons.put("Solitaire", solitaireBtn);
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

    private void setAppDetails(){
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
    }
}

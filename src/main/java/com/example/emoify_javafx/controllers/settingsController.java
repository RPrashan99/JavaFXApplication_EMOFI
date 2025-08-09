package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.ApiClient;
import com.jfoenix.controls.JFXToggleButton;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class settingsController {

    @FXML
    private HBox systemSoundBtnGroup;

    @FXML
    private ToggleButton lowSoundBtn, midSoundBtn, highSoundBtn;

    @FXML
    private Button applyBtn;

    @FXML
    private JFXToggleButton systemDisableToggleBtn;

    @FXML
    private Spinner<Integer> recommendationTimeSpinner, restTimeSpinner, appExecuteTimeSpinner;

    private ToggleButton[] soundButtons;

    private Map<String, Object> settings = new HashMap<>();

    private Map<String, String> settings_set = new HashMap<>();

    @FXML
    public void initialize() {
        // Group sound buttons for exclusive selection
        soundButtons = new ToggleButton[]{lowSoundBtn, midSoundBtn, highSoundBtn};
        setupSoundButtonGroup();

        // Initialize spinners
        recommendationTimeSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 60, 0));
        restTimeSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 60, 0));
        appExecuteTimeSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 120, 0));

        // Optional: Setup apply button
        applyBtn.setOnAction(e -> applySettings());

        getCurrentValues();
    }

    private void setupSoundButtonGroup() {
        for (ToggleButton btn : soundButtons) {
            btn.setOnAction(event -> {
                for (ToggleButton otherBtn : soundButtons) {
                    otherBtn.setSelected(otherBtn == btn);
                }
                System.out.println("Selected sound level: " + btn.getText());
            });
        }
    }

    private void applySettings() {
        boolean isSystemDisabled = systemDisableToggleBtn.isSelected();
        String soundLevel = getSelectedSoundLevel();
        int recommendationTime = recommendationTimeSpinner.getValue();
        int restTime = restTimeSpinner.getValue();
        int executionTime = appExecuteTimeSpinner.getValue();

        System.out.println("=== Apply Settings ===");
        System.out.println("System Disabled: " + isSystemDisabled);
        System.out.println("Sound Level: " + soundLevel);
        System.out.println("Recommendation Timer: " + recommendationTime + " min");
        System.out.println("Rest Timer: " + restTime + " min");
        System.out.println("App Execution Timer: " + executionTime + " min");

        List<String> setting_names = new ArrayList<>();
        setting_names.add("systemDisable");
        setting_names.add("soundLevel");
        setting_names.add("recommendationTime");
        setting_names.add("restTime");
        setting_names.add("appExecutionTime");

        List<String> setting_value = new ArrayList<>();
        setting_value.add(Boolean.toString(isSystemDisabled));
        setting_value.add(soundLevel);
        setting_value.add(String.valueOf(recommendationTime));
        setting_value.add(String.valueOf(restTime));
        setting_value.add(String.valueOf(executionTime));

        ApiClient.setSettings(1, setting_names, setting_value).thenAccept(response -> {

            if(response == 200){
                System.out.println("Settings applied");
            }else{
                System.out.println("Settings apply failed");
            }
        });
    }

    private String getSelectedSoundLevel() {
        for (ToggleButton btn : soundButtons) {
            if (btn.isSelected()) {
                return btn.getText();
            }
        }
        return "None";
    }

    public void setInitialValues(boolean disableState, String soundState, Integer recT, Integer restT, Integer execT){
        //disable state

        if(!settings.isEmpty()){
            if((boolean) settings.get("sysDisable") != disableState){
                systemDisableToggleBtn.setSelected(disableState);
                settings.replace("sysDisable", disableState);
            }

            if(settings.get("soundBtn") != soundState){
                ToggleButton selectedSoundBtn;

                if(soundState.equals("Low")){
                    selectedSoundBtn = lowSoundBtn;
                    lowSoundBtn.setSelected(true);
                    settings.replace("soundBtn", "Low");
                } else if (soundState.equals("Mid")) {
                    selectedSoundBtn = midSoundBtn;
                    midSoundBtn.setSelected(true);
                    settings.replace("soundBtn", "Mid");
                }else{
                    selectedSoundBtn = highSoundBtn;
                    highSoundBtn.setSelected(true);
                    settings.replace("soundBtn", "High");
                }

                for (ToggleButton btn : soundButtons) {
                    if (btn != selectedSoundBtn) btn.setSelected(false);
                }
            }

            if((Integer) settings.get("recTime") != recT){
                recommendationTimeSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 60, recT));
                settings.replace("recTime", recT);
            }
            if((Integer) settings.get("restTime") != restT){
                restTimeSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 60, restT));
                settings.replace("restTime", restT);
            }
            if((Integer) settings.get("execTime") != execT){
                appExecuteTimeSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 120, execT));
                settings.replace("execTime", execT);
            }
        }else{
            systemDisableToggleBtn.setSelected(disableState);
            settings.put("sysDisable", disableState);

            //sound set
            ToggleButton selectedSoundBtn;

            if(soundState.equals("Low")){
                selectedSoundBtn = lowSoundBtn;
                lowSoundBtn.setSelected(true);
                settings.put("soundBtn", "Low");
            } else if (soundState.equals("Mid")) {
                selectedSoundBtn = midSoundBtn;
                midSoundBtn.setSelected(true);
                settings.put("soundBtn", "Mid");
            }else{
                selectedSoundBtn = highSoundBtn;
                highSoundBtn.setSelected(true);
                settings.put("soundBtn", "High");
            }

            for (ToggleButton btn : soundButtons) {
                if (btn != selectedSoundBtn) btn.setSelected(false);
            }

            //timers set
            recommendationTimeSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 60, recT));
            settings.put("recTime", recT);
            restTimeSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 60, restT));
            settings.put("restTime", restT);
            appExecuteTimeSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 120, execT));
            settings.put("execTime", execT);

        }

    }

    public void getCurrentValues(){

        ApiClient.getAppSettings().thenAccept(response -> {

            JSONObject responseJson = new JSONObject(response);

            JSONArray appSettings = responseJson.getJSONArray("settings");

            Map<String, String> settings_new = new HashMap<>();

            for(int i = 0; i < appSettings.length(); i++){

                String key = appSettings.getJSONArray(i).getString(2);
                String value = appSettings.getJSONArray(i).getString(3);
                settings_new.put(key, value);
                //System.out.println("Settings: " + key + " value: " + value);
            }
            System.out.println("Settings: " + settings_new);
            settings_set = settings_new;
            setCurrentValues();

        });
    }

    public void setCurrentValues(){
        String valueTheme = settings_set.get("theme");
        System.out.println("Theme: " + valueTheme);

        String valueSysDisable = settings_set.get("systemDisable");
        boolean disableBoolean;
        if(Objects.equals(valueSysDisable, "false")){
            disableBoolean = false;
        }else{
            disableBoolean = true;
        }
        System.out.println("System disable: " + disableBoolean);

        String valueRecTime = settings_set.get("recommendationTime");
        Integer recTimeInt = Integer.parseInt(valueRecTime);
        System.out.println("Recommendation Time: " + recTimeInt);

        String valueRestTime = settings_set.get("restTime");
        Integer restTimeInt = Integer.parseInt(valueRestTime);
        System.out.println("Rest time: " + restTimeInt);

        String valueExecTime = settings_set.get("appExecuteTime");
        Integer execTimeInt = Integer.parseInt(valueExecTime);
        System.out.println("App Execute Time: " + execTimeInt);

        String valueSoundLevel = settings_set.get("soundLevel");
        System.out.println("Sound Level: " + valueSoundLevel);

        setInitialValues(disableBoolean, valueSoundLevel, recTimeInt, restTimeInt, execTimeInt);
    }
}

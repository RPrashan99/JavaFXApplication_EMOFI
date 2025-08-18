package com.example.emoify_javafx.models;

import com.example.emoify_javafx.controllers.addAppController;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class RegistrationData {
    private StringProperty username = new SimpleStringProperty();
    private StringProperty password = new SimpleStringProperty();
    private StringProperty phoneNumber = new SimpleStringProperty();
    private Map<String, List<addAppController.AppData>> categoryApps;

    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty phoneNumberProperty() { return phoneNumber; }

    public String getUsername() {
        return username.get();
    }

    public String getPassword() {
        return password.get();
    }

    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    public void setApps(Map<String, List<addAppController.AppData>> apps) {
        categoryApps = apps;
    }

    public Map<String, List<addAppController.AppData>> getApps(){
        return categoryApps;
    }

}

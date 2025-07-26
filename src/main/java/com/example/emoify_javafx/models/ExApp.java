package com.example.emoify_javafx.models;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class ExApp {

    SimpleStringProperty appName;
    SimpleStringProperty path;
    SimpleBooleanProperty isAccessGiven;
    SimpleBooleanProperty isAvailable;

    public ExApp(String appName, String path, Boolean isAccessGiven, Boolean isAvailable) {
        this.appName = new SimpleStringProperty(appName);
        this.path = new SimpleStringProperty(path);
        this.isAccessGiven = new SimpleBooleanProperty(isAccessGiven);
        this.isAvailable = new SimpleBooleanProperty(isAvailable);
    }

    public String getAppName() {
        return appName.get();
    }

    public SimpleStringProperty appNameProperty() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName.set(appName);
    }

    public String getPath() {
        return path.get();
    }

    public SimpleStringProperty pathProperty() {
        return path;
    }

    public void setPath(String path) {
        this.path.set(path);
    }

    public boolean isIsAccessGiven() {
        return isAccessGiven.get();
    }

    public SimpleBooleanProperty isAccessGivenProperty() {
        return isAccessGiven;
    }

    public void setIsAccessGiven(boolean isAccessGiven) {
        this.isAccessGiven.set(isAccessGiven);
    }

    public boolean isIsAvailable() {
        return isAvailable.get();
    }

    public SimpleBooleanProperty isAvailableProperty() {
        return isAvailable;
    }

    public void setIsAvailable(boolean isAvailable) {
        this.isAvailable.set(isAvailable);
    }
}

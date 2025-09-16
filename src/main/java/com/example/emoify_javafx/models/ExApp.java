package com.example.emoify_javafx.models;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class ExApp {

    SimpleStringProperty appName;
    SimpleStringProperty path;
    SimpleStringProperty iconPath;
    SimpleStringProperty category;
    SimpleBooleanProperty isAvailable;
    SimpleBooleanProperty isLocal;

    public ExApp() {
        this.appName = new SimpleStringProperty("");
        this.path = new SimpleStringProperty("");
        this.iconPath = new SimpleStringProperty("");
        this.category = new SimpleStringProperty("");
        this.isAvailable = new SimpleBooleanProperty(false);
        this.isLocal = new SimpleBooleanProperty(false);
    }

    public ExApp(String appName, String path, String iconPath, String category, Boolean isAvailable, Boolean isLocal) {
        this.appName = new SimpleStringProperty(appName);
        this.path = new SimpleStringProperty(path);
        this.category = new SimpleStringProperty(category);
        this.iconPath = new SimpleStringProperty(iconPath);
        this.isAvailable = new SimpleBooleanProperty(isAvailable);
        this.isLocal = new SimpleBooleanProperty(isLocal);

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

    public String getIconPath() {
        return iconPath.get();
    }

    public SimpleStringProperty iconPathProperty() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath.set(iconPath);
    }

    public String getCategory() {
        return category.get();
    }

    public SimpleStringProperty categoryProperty() {
        return category;
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    public boolean getIsAvailable() {
        return isAvailable.get();
    }

    public SimpleBooleanProperty isAvailableProperty() {
        return isAvailable;
    }

    public void setIsAvailable(boolean isAvailable) {
        this.isAvailable.set(isAvailable);
    }

    public boolean getIsLocal() {
        return isLocal.get();
    }

    public SimpleBooleanProperty isLocalProperty() {
        return isLocal;
    }

    public void setIsLocal(boolean isLocal) {
        this.isLocal.set(isLocal);
    }
}

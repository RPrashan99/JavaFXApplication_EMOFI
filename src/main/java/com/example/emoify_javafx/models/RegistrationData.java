package com.example.emoify_javafx.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RegistrationData {
    private StringProperty username = new SimpleStringProperty();
    private StringProperty password = new SimpleStringProperty();
    private StringProperty phoneNumber = new SimpleStringProperty();

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
}

package com.example.emoify_javafx.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class RegistrationData {
    private StringProperty username = new SimpleStringProperty();
    private StringProperty password = new SimpleStringProperty();
    private StringProperty phoneNumber = new SimpleStringProperty();
    //private ObjectProperty<LocalDate> birthday = new SimpleObjectProperty<>();

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

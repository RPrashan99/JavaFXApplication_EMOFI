package com.example.emoify_javafx.models;

import java.security.Timestamp;

public class DataModel {
    private final int id;
    private final String content;
    private final Timestamp timestamp;

    public DataModel(int id, String content, Timestamp timestamp) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
    }
}

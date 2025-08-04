package com.example.emoify_javafx.models;

import javafx.scene.control.Button;

public class AnimationEvent {
    public enum EventType { STARTED, FINISHED }

    private final EventType type;
    private final Button button;

    public AnimationEvent(EventType type, Button button) {
        this.type = type;
        this.button = button;
    }

    public EventType getType() { return type; }
    public Button getButton() { return button; }
}

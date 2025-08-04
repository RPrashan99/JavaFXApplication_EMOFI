package com.example.emoify_javafx.models;

import com.google.common.eventbus.EventBus;

public class AnimationEventBus {
    private static final EventBus INSTANCE = new EventBus();

    public static EventBus getInstance() {
        return INSTANCE;
    }

    private AnimationEventBus() {} // Prevent instantiation
}

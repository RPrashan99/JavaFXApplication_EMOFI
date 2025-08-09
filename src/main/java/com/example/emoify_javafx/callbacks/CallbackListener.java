package com.example.emoify_javafx.callbacks;

import java.util.List;

@FunctionalInterface
public interface CallbackListener {
    public void onDataPassed(List<String> data);
}

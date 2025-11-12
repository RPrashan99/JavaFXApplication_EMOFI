package com.example.emoify_javafx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class logSubWindowController {

    @FXML
    private TextArea logArea;

    private Process pythonProcess;

    @FXML
    private void handleRunPython() {

        if(pythonProcess != null){
            Thread t2 = new Thread(() -> {
                try {
                    try (BufferedReader reader =
                                 new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String finalLine = line;
                            Platform.runLater(() -> logArea.appendText(finalLine + "\n"));
                        }
                    }

                    int exitCode = pythonProcess.waitFor();
                    Platform.runLater(() ->
                            logArea.appendText("\nProcess finished with exit code: " + exitCode + "\n")
                    );

                } catch (IOException | InterruptedException e) {
                    Platform.runLater(() -> logArea.appendText("Error: " + e.getMessage() + "\n"));
                }
            });
            t2.setDaemon(true);
            t2.start();
        } else {
            System.out.println("Python process is null");
        }
    }

    public void setPythonProcess(Process pyProcess){
        pythonProcess = pyProcess;
    }
}

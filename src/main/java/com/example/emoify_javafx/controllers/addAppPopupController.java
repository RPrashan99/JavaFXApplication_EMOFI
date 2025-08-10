package com.example.emoify_javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// JNA Imports
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class addAppPopupController {

    @FXML private TextField appNameField;
    @FXML private TextField appPathField;

    private String category;
    private addAppController parentController;
    private static final String UNINSTALL_REGISTRY_PATH = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall";

    public void setCategory(String category) {
        this.category = category;
    }

    public void setParentController(addAppController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void searchInstalledApps() {
        String name = appNameField.getText().toLowerCase();
        if (name.isEmpty()) {
            showAlert("Error", "Empty Name", "Please enter an app name to search.");
            return;
        }

        Map<String, String> installedApps = getInstalledPrograms();

        for (Map.Entry<String, String> entry : installedApps.entrySet()) {
            if (entry.getKey().toLowerCase().contains(name)) {
                String exePath = findExecutableInFolder(entry.getValue());
                if (exePath != null) {
                    appPathField.setText(exePath);
                    return;
                }
            }
        }

        showAlert("Not Found", "App Not Found", "No matching application found in installed programs.");
    }

    /**
     * Replicates the Python code's winreg functionality to get installed programs.
     * Searches both HKEY_LOCAL_MACHINE and HKEY_CURRENT_USER.
     * @return A map of application display names and their installation locations.
     */
    private Map<String, String> getInstalledPrograms() {
        Map<String, String> programPaths = new HashMap<>();

        // Search in HKEY_LOCAL_MACHINE
        getProgramsFromRegistry(WinReg.HKEY_LOCAL_MACHINE, programPaths);
        // Search in HKEY_CURRENT_USER
        getProgramsFromRegistry(WinReg.HKEY_CURRENT_USER, programPaths);

        return programPaths;
    }

    private void getProgramsFromRegistry(WinReg.HKEY hkey, Map<String, String> programPaths) {
        try {
            // Get all subkeys under the Uninstall key
            String[] subkeys = Advapi32Util.registryGetKeys(hkey, UNINSTALL_REGISTRY_PATH);
            for (String subkeyName : subkeys) {
                String subkeyPath = UNINSTALL_REGISTRY_PATH + "\\" + subkeyName;
                try {
                    if (Advapi32Util.registryValueExists(hkey, subkeyPath, "DisplayName") &&
                            Advapi32Util.registryValueExists(hkey, subkeyPath, "InstallLocation")) {

                        String displayName = Advapi32Util.registryGetStringValue(hkey, subkeyPath, "DisplayName");
                        String installLocation = Advapi32Util.registryGetStringValue(hkey, subkeyPath, "InstallLocation");

                        if (installLocation != null && !installLocation.trim().isEmpty()) {
                            programPaths.put(displayName, installLocation);
                        }
                    }
                } catch (Exception e) {
                    // Ignore keys that don't have DisplayName or InstallLocation
                }
            }
        } catch (Exception e) {
            // Ignore registry keys that cannot be accessed
        }
    }

    /**
     * Replicates the Python code's os.walk functionality.
     * Recursively searches a directory and its subdirectories for the first .exe file.
     * @param folderPath The path to the installation directory.
     * @return The absolute path to the first executable found, or null if none is found.
     */
    private String findExecutableInFolder(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            return null;
        }

        try {
            List<Path> exeFiles = Files.walk(folder.toPath())
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".exe"))
                    .limit(1) // We only need the first one we find
                    .toList();

            if (!exeFiles.isEmpty()) {
                return exeFiles.get(0).toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @FXML
    private void browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Application Executable");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Executable Files", "*.exe")
        );

        Stage stage = (Stage) appPathField.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            appPathField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void saveApp() {
        String name = appNameField.getText();
        String path = appPathField.getText();

        if (name == null || name.trim().isEmpty()) {
            showAlert("Missing", "App Name Required", "Please enter an application name.");
            return;
        }

        parentController.addAppToList(category, name, path);
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) appNameField.getScene().getWindow()).close();
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

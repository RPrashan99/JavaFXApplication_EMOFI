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
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.InputStreamReader;

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

        try {
            // First, check for UWP apps using the AUMID finder
            String aumid = getAppAumid(name);
            if (aumid != null) {
                appPathField.setText(aumid);
                return;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // Fall through to the next search method if this one fails
        }

        // If not a UWP app, search for standard desktop apps in the registry
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

        // If not found in the registry, search common user folders like Desktop and Downloads
        String portableAppPath = findExecutableInUserFolders(name);
        if (portableAppPath != null) {
            appPathField.setText(portableAppPath);
            return;
        }

        showAlert("Not Found", "App Not Found", "No matching application found in installed programs or common folders.");
    }

    /**
     * Finds the AUMID of a Windows application.
     *
     * @param appName The display name of the application (e.g., "Microsoft Photos", "Paint").
     * @return The AUMID string, or null if not found.
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the process is interrupted.
     */
    public static String getAppAumid(String appName) throws IOException, InterruptedException {
        // PowerShell command to find the AUMID. We use Get-AppxPackage to find the UWP app
        // and then get its PackageFamilyName and the ID from its manifest.
        // The command is piped to a Select-Object to get a clean output.
        String powershellCommand = "powershell.exe -NoProfile -Command \"Get-AppxPackage | Where-Object { $_.Name -like '*" + appName + "*' } | ForEach-Object { $_.PackageFamilyName + '!' + (Get-AppxPackageManifest $_).package.applications.application.id -join ',' } \"";

        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", powershellCommand);
        processBuilder.redirectErrorStream(true); // Merges standard error into standard output

        Process process = processBuilder.start();

        // Read the output from the command
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            String aumidResult = output.toString().trim();
            if (!aumidResult.isEmpty()) {
                // Return the AUMID. If multiple IDs are found, this will return a comma-separated list.
                return aumidResult;
            }
        }

        // Return null if no AUMID was found or the command failed.
        return null;
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

    /**
     * Searches common user directories like Desktop and Downloads for executable files
     * that match the given app name.
     * @param appName The name of the app to search for.
     * @return The absolute path to the first matching executable found, or null if none is found.
     */
    private String findExecutableInUserFolders(String appName) {
        String userHome = System.getProperty("user.home");
        Path desktopPath = Paths.get(userHome, "Desktop");
        Path downloadsPath = Paths.get(userHome, "Downloads");

        try {
            // Search Desktop folder
            List<Path> desktopFiles = Files.walk(desktopPath)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().contains(appName.toLowerCase()) && p.toString().toLowerCase().endsWith(".exe"))
                    .limit(1)
                    .toList();
            if (!desktopFiles.isEmpty()) {
                return desktopFiles.get(0).toString();
            }

            // Search Downloads folder
            List<Path> downloadsFiles = Files.walk(downloadsPath)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().contains(appName.toLowerCase()) && p.toString().toLowerCase().endsWith(".exe"))
                    .limit(1)
                    .toList();
            if (!downloadsFiles.isEmpty()) {
                return downloadsFiles.get(0).toString();
            }
        } catch (IOException e) {
            // Error during file walk, but we'll return null to indicate nothing was found.
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
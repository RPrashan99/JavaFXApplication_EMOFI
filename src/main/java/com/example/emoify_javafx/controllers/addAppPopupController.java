package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.models.ExApp;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

// JNA Imports
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class addAppPopupController {

    @FXML private ComboBox<String> appNameField;
    @FXML private TextField appPathField;

    private String category;
    private addAppController parentController;
    private ObservableList<String> allInstalledApps;
    private Map<String, String> combinedAppPaths;

    // Flag to prevent the ComboBox's listener from entering an infinite loop
    private boolean isUpdatingItems = false;

    // Constant for the registry path where installed apps are listed for 64-bit applications
    private static final String UNINSTALL_REGISTRY_PATH_64 = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall";
    // Constant for the registry path where installed apps are listed for 32-bit applications on a 64-bit OS
    private static final String UNINSTALL_REGISTRY_PATH_32 = "SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall";

    // Set of keywords to filter out system and unwanted apps
    private static final Set<String> IGNORED_KEYWORDS = new HashSet<>(Arrays.asList(
            "runtime", "redistributable", "sdk", "service", "security", "update", "hotfix",
            "visual c++", "visual studio", "system", "vulnerable"
    ));

    private List<ExApp> existingApps;

    public void setExistingApps(List<ExApp> apps) {
        this.existingApps = apps;
        existingApps.forEach(app ->
                System.out.println("Existing app in popup: " + app.getAppName() + ", Path: " + app.getPath())
        );
        loadAndFilterApps();

        // You can use this full info to check duplicates or autofill, etc.
    }

    @FXML
    public void initialize() {
        appNameField.getEditor().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                appNameField.show();
                if(appPathField.getText() != null){
                    appPathField.clear();
                }
            }else {
                // On focus lost, check for duplicate app name
                String enteredName = appNameField.getEditor().getText();
                if (enteredName != null && !enteredName.trim().isEmpty()) {
                    if (appNameExists(enteredName)) {
                        showAlert("Duplicate App Name", "App Name Exists",
                                "The app name '" + enteredName + "' already exists in the database. Please enter a different name.");
                    }
                }
            }
        });

        // Optional: also show popup on mouse click inside the editor
        appNameField.getEditor().setOnMouseClicked(event -> {
            if (!appNameField.isShowing()) {
                appNameField.show();
                if(appPathField.getText() != null){
                    appPathField.clear();
                }
            }
        });

        appNameField.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (!isUpdatingItems && appPathField.getText() != null) {
                appPathField.clear();
            }
        });
    }


    private void loadAndFilterApps() {
        combinedAppPaths = new HashMap<>();

        Map<String, String> installedProgramsMap = getInstalledPrograms();
        Map<String, String> uwpAppsMap = getUWPApps();
        Map<String, String> portableAppsMap = getPortableAppsFromCommonFolders();

        combinedAppPaths.putAll(installedProgramsMap);
        combinedAppPaths.putAll(uwpAppsMap);
        combinedAppPaths.putAll(portableAppsMap);

        Set<String> normalizedExistingApps = existingApps.stream()
                .map(app -> app.getAppName() == null ? "" : app.getAppName().trim().toLowerCase())
                .collect(Collectors.toSet());

        combinedAppPaths.keySet().removeIf(name -> {
            if (name == null) return true;

            String trimmedName = name.trim().toLowerCase();

            boolean exists = normalizedExistingApps.contains(trimmedName);
            boolean numericOnly = trimmedName.matches("^\\d+$");
            boolean containsGuid = trimmedName.matches(".*[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}.*");
            boolean hasStoreSuffix = trimmedName.contains("_cw5n1h2txyewy");
            boolean hasExclamation = trimmedName.contains("!");
            boolean isSystemApp = trimmedName.contains("microsoft.net.native")
                    || trimmedName.contains("microsoft.ui.xaml")
                    || trimmedName.contains("microsoft.windowsappruntime")
                    || trimmedName.contains("microsoft.win32webviewhost")
                    || trimmedName.contains("windowsfeedbackhub");

            return exists || numericOnly || containsGuid || hasStoreSuffix || hasExclamation || isSystemApp;
        });

        allInstalledApps = FXCollections.observableArrayList(combinedAppPaths.keySet());
        appNameField.setItems(allInstalledApps);

        appNameField.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (isUpdatingItems || newText == null) return;

            isUpdatingItems = true;
            Platform.runLater(() -> {
                if (newText.isEmpty() || allInstalledApps.contains(newText)) {
                    appNameField.setItems(allInstalledApps);
                } else {
                    ObservableList<String> filteredList = allInstalledApps.stream()
                            .filter(app -> app.toLowerCase().contains(newText.toLowerCase()))
                            .collect(Collectors.toCollection(FXCollections::observableArrayList));
                    appNameField.setItems(filteredList);
                }
                isUpdatingItems = false;
            });
        });
    }

    private String cleanAndFilterAppName(String originalName) {
        if (originalName == null || originalName.trim().isEmpty()) {
            return null;
        }

        String lowerCaseName = originalName.toLowerCase();

        // Check for ignored keywords
        for (String keyword : IGNORED_KEYWORDS) {
            if (lowerCaseName.contains(keyword)) {
                return null;
            }
        }

        // Clean up prefixes like "Microsoft." or "Microsoft.Windows."
        if (originalName.contains(".")) {
            String[] parts = originalName.split("\\.");
            return parts[parts.length - 1];
        }

        return originalName;
    }

    public void handleCloseButton(ActionEvent event) {
        // Get the stage from the event source (the button) and close it.
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setParentController(addAppController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void searchInstalledApps() {
        String name = appNameField.getEditor().getText();
        if (name.isEmpty()) {
            showAlert("Error", "Empty Name", "Please enter an app name to search.");
            return;
        }

        // Search logic now prioritizes UWP, then installed apps, then user folders.
        // It will return the first valid path it finds.

        // 1. Check for UWP apps using the AUMID finder
        try {
            String aumid = getAppAumid(name);
            if (aumid != null) {
                appPathField.setText(aumid);
                return;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // Fall through to the next search method if this one fails
        }

        // 2. Search for standard desktop apps in the registry based on a partial match
        Map<String, String> installedApps = getInstalledPrograms();
        for (Map.Entry<String, String> entry : installedApps.entrySet()) {
            if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
                String exePath = findExecutableInFolder(entry.getValue());
                if (exePath != null) {
                    appPathField.setText(exePath);
                    return;
                }
            }
        }

        // 3. If not found in the registry, search common user folders like Desktop and Downloads
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
     * Finds the AUMIDs and display names of all UWP (Universal Windows Platform) applications
     * installed on the system using a PowerShell command.
     * @return A map of display names and their corresponding AUMIDs.
     */
    private Map<String, String> getUWPApps() {
        Map<String, String> uwpApps = new HashMap<>();
        try {
            // The PowerShell command to get all AppxPackage objects, select their Name and PackageFamilyName,
            // and format them as a single string for parsing.
            String powershellCommand = "powershell.exe -NoProfile -Command \"Get-AppxPackage | Select-Object Name, PackageFamilyName | ForEach-Object { $_.Name + ';' + $_.PackageFamilyName + '!' + (Get-AppxPackageManifest -Package $_).package.applications.application.id } \"";

            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", powershellCommand);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";", 2);
                if (parts.length == 2) {
                    String displayName = parts[0];
                    String aumid = parts[1];
                    // Clean up the display name for better readability
                    displayName = displayName.replace("Name=", "").trim();
                    aumid = aumid.replace("PackageFamilyName=", "").trim();
                    String cleanedName = cleanAndFilterAppName(displayName);
                    if (cleanedName != null) {
                        uwpApps.put(cleanedName, aumid);
                    }
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return uwpApps;
    }

    /**
     * Replicates the Python code's winreg functionality to get installed programs.
     * Searches both HKEY_LOCAL_MACHINE and HKEY_CURRENT_USER.
     * @return A map of application display names and their installation locations.
     */
    private Map<String, String> getInstalledPrograms() {
        Map<String, String> programPaths = new HashMap<>();
        // Search in HKEY_LOCAL_MACHINE for 64-bit and 32-bit apps
        getProgramsFromRegistry(WinReg.HKEY_LOCAL_MACHINE, UNINSTALL_REGISTRY_PATH_64, programPaths);
        getProgramsFromRegistry(WinReg.HKEY_LOCAL_MACHINE, UNINSTALL_REGISTRY_PATH_32, programPaths);
        // Search in HKEY_CURRENT_USER for 64-bit and 32-bit apps
        getProgramsFromRegistry(WinReg.HKEY_CURRENT_USER, UNINSTALL_REGISTRY_PATH_64, programPaths);
        getProgramsFromRegistry(WinReg.HKEY_CURRENT_USER, UNINSTALL_REGISTRY_PATH_32, programPaths);
        return programPaths;
    }

    private void getProgramsFromRegistry(WinReg.HKEY hkey, String registryPath, Map<String, String> programPaths) {
        try {
            String[] subkeys = Advapi32Util.registryGetKeys(hkey, registryPath);
            for (String subkeyName : subkeys) {
                String subkeyPath = registryPath + "\\" + subkeyName;
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
            List<Path> exeFiles = new ArrayList<>(Files.walk(folder.toPath())
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".exe"))
                    .limit(3) // We only need the first one we find
                    .toList());

            if (!exeFiles.isEmpty()) {

                exeFiles.removeIf(exePath -> exePath.getFileName().toString().toLowerCase().matches(".*(uninstall|unins\\d{3,}|remove|cleanup).*"));

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

    private Map<String, String> getPortableAppsFromCommonFolders() {
        Map<String, String> portableApps = new HashMap<>();
        String userHome = System.getProperty("user.home");
        Path desktopPath = Paths.get(userHome, "Desktop");
        Path downloadsPath = Paths.get(userHome, "Downloads");

        try {
            // Search Desktop folder
            Files.walk(desktopPath)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".exe"))
                    .forEach(p -> portableApps.put(p.getFileName().toString().replace(".exe", ""), p.toString()));

            // Search Downloads folder
            Files.walk(downloadsPath)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".exe"))
                    .forEach(p -> portableApps.put(p.getFileName().toString().replace(".exe", ""), p.toString()));
        } catch (IOException e) {
            // Handle error, but don't stop the program.
            e.printStackTrace();
        }

        return portableApps;
    }

    // Duplicate check helper methods:
    private boolean appNameExists(String name) {
        if (existingApps == null || name == null) return false;

        String checkName = name.trim().toLowerCase(Locale.ROOT);
        return existingApps.stream()
                .anyMatch(app -> app.getAppName() != null && app.getAppName().trim().toLowerCase(Locale.ROOT).equals(checkName));
    }

    private boolean appPathExists(String path) {
        if (existingApps == null || path == null) return false;

        String checkPath = path.trim().toLowerCase(Locale.ROOT);
        return existingApps.stream()
                .anyMatch(app -> app.getPath() != null && app.getPath().trim().toLowerCase(Locale.ROOT).equals(checkPath));
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
            String selectedPath = selectedFile.getAbsolutePath();
            appPathField.setText(selectedPath);
        }
    }

    @FXML
    private void saveApp() {
        String name = appNameField.getEditor().getText();
        String path = appPathField.getText();

        if (name == null || name.trim().isEmpty()) {
            showAlert("Missing", "App Name Required", "Please enter an application name.");
            return;
        }

        if (path == null || path.trim().isEmpty()) {
            showAlert("Missing", "App Path Required", "Please enter or select an application path.");
            return;
        }

        if (appNameExists(name)) {
            showAlert("Duplicate App Name", "App Name Exists",
                    "The app name '" + name.trim() + "' already exists in the database.");
            return;
        }

        if (appPathExists(path)) {
            showAlert("Duplicate App Path", "App Path Exists",
                    "The app path '" + path.trim() + "' already exists in the database.");
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



//
//package com.example.emoify_javafx.controllers;
//
//import javafx.application.Platform;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.scene.Node;
//import javafx.scene.control.Alert;
//import javafx.scene.control.ComboBox;
//import javafx.scene.control.TextField;
//import javafx.stage.FileChooser;
//import javafx.stage.Stage;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.*;
//        import java.util.concurrent.TimeUnit;
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.util.stream.Collectors;
//
//// JNA Imports
//import com.sun.jna.platform.win32.Advapi32Util;
//import com.sun.jna.platform.win32.WinReg;
//
//public class addAppPopupController {
//
//    @FXML private ComboBox<String> appNameField;
//    @FXML private TextField appPathField;
//
//    private String category;
//    private addAppController parentController;
//    private ObservableList<String> allInstalledApps;
//    private Map<String, String> combinedAppPaths;
//
//    // Flag to prevent the ComboBox's listener from entering an infinite loop
//    private boolean isUpdatingItems = false;
//
//    // Constant for the registry path where installed apps are listed for 64-bit applications
//    private static final String UNINSTALL_REGISTRY_PATH_64 = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall";
//    // Constant for the registry path where installed apps are listed for 32-bit applications on a 64-bit OS
//    private static final String UNINSTALL_REGISTRY_PATH_32 = "SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall";
//
//    // Set of keywords to filter out system and unwanted apps
//    private static final Set<String> IGNORED_KEYWORDS = new HashSet<>(Arrays.asList(
//            "runtime", "redistributable", "sdk", "service", "security", "update", "hotfix",
//            "visual c++", "visual studio", "system", "vulnerable"
//    ));
//
//    private List<String> existingAppNames = new ArrayList<>();
//
//    public void setExistingAppNames(List<String> existingAppNames) {
//        this.existingAppNames = existingAppNames;
//    }
//
//    @FXML
//    public void initialize() {
//        // Initialize the class-level map
//        combinedAppPaths = new HashMap<>();
//        // Load the list of all installed apps (desktop and UWP) on startup
//        Map<String, String> installedProgramsMap = getInstalledPrograms();
//        Map<String, String> uwpAppsMap = getUWPApps();
//        // NEW: Load the list of portable apps from common user folders
//        Map<String, String> portableAppsMap = getPortableAppsFromCommonFolders();
//
//        // Combine all three maps into our class-level variable
//        combinedAppPaths.putAll(installedProgramsMap);
//        combinedAppPaths.putAll(uwpAppsMap);
//        combinedAppPaths.putAll(portableAppsMap);
//
//        // Remove all existing apps to prevent duplicates
//        for (String existingApp : existingAppNames) {
//            combinedAppPaths.keySet().removeIf(name ->
//                    existingAppNames.stream()
//                            .anyMatch(existing -> existing.equalsIgnoreCase(name))
//            );
//
//        }
//
//        allInstalledApps = FXCollections.observableArrayList(combinedAppPaths.keySet());
//        appNameField.setItems(allInstalledApps);
//
//        appNameField.getEditor().textProperty().addListener((obs, oldText, newText) -> {
//            if (isUpdatingItems || newText == null) {
//                return;
//            }
//
//            isUpdatingItems = true;
//            try {
//                Platform.runLater(() -> {
//                    if (newText.isEmpty() || allInstalledApps.contains(newText)) {
//                        appNameField.setItems(allInstalledApps);
//                    } else {
//                        ObservableList<String> filteredList = allInstalledApps.stream()
//                                .filter(app -> app.toLowerCase().contains(newText.toLowerCase()))
//                                .collect(Collectors.toCollection(FXCollections::observableArrayList));
//                        appNameField.setItems(filteredList);
//                    }
//                    isUpdatingItems = false;
//                });
//            } catch (Exception e) {
//                isUpdatingItems = false;
//            }
//        });
//    }
//
//    private String cleanAndFilterAppName(String originalName) {
//        if (originalName == null || originalName.trim().isEmpty()) {
//            return null;
//        }
//
//        String lowerCaseName = originalName.toLowerCase();
//
//        // Check for ignored keywords
//        for (String keyword : IGNORED_KEYWORDS) {
//            if (lowerCaseName.contains(keyword)) {
//                return null;
//            }
//        }
//
//        // Clean up prefixes like "Microsoft." or "Microsoft.Windows."
//        if (originalName.contains(".")) {
//            String[] parts = originalName.split("\\.");
//            return parts[parts.length - 1];
//        }
//
//        return originalName;
//    }
//
//    public void handleCloseButton(ActionEvent event) {
//        // Get the stage from the event source (the button) and close it.
//        Node source = (Node) event.getSource();
//        Stage stage = (Stage) source.getScene().getWindow();
//        stage.close();
//    }
//
//    public void setCategory(String category) {
//        this.category = category;
//    }
//
//    public void setParentController(addAppController parentController) {
//        this.parentController = parentController;
//    }
//
//    @FXML
//    private void searchInstalledApps() {
//        String name = appNameField.getEditor().getText();
//        if (name.isEmpty()) {
//            showAlert("Error", "Empty Name", "Please enter an app name to search.");
//            return;
//        }
//
//        // Search logic now prioritizes UWP, then installed apps, then user folders.
//        // It will return the first valid path it finds.
//
//        // 1. Check for UWP apps using the AUMID finder
//        try {
//            String aumid = getAppAumid(name);
//            if (aumid != null) {
//                appPathField.setText(aumid);
//                return;
//            }
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//            // Fall through to the next search method if this one fails
//        }
//
//        // 2. Search for standard desktop apps in the registry based on a partial match
//        Map<String, String> installedApps = getInstalledPrograms();
//        for (Map.Entry<String, String> entry : installedApps.entrySet()) {
//            if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
//                String exePath = findExecutableInFolder(entry.getValue());
//                if (exePath != null) {
//                    appPathField.setText(exePath);
//                    return;
//                }
//            }
//        }
//
//        // 3. If not found in the registry, search common user folders like Desktop and Downloads
//        String portableAppPath = findExecutableInUserFolders(name);
//        if (portableAppPath != null) {
//            appPathField.setText(portableAppPath);
//            return;
//        }
//
//        showAlert("Not Found", "App Not Found", "No matching application found in installed programs or common folders.");
//    }
//
//    /**
//     * Finds the AUMID of a Windows application.
//     *
//     * @param appName The display name of the application (e.g., "Microsoft Photos", "Paint").
//     * @return The AUMID string, or null if not found.
//     * @throws IOException if an I/O error occurs.
//     * @throws InterruptedException if the process is interrupted.
//     */
//    public static String getAppAumid(String appName) throws IOException, InterruptedException {
//        // PowerShell command to find the AUMID. We use Get-AppxPackage to find the UWP app
//        // and then get its PackageFamilyName and the ID from its manifest.
//        // The command is piped to a Select-Object to get a clean output.
//        String powershellCommand = "powershell.exe -NoProfile -Command \"Get-AppxPackage | Where-Object { $_.Name -like '*" + appName + "*' } | ForEach-Object { $_.PackageFamilyName + '!' + (Get-AppxPackageManifest $_).package.applications.application.id -join ',' } \"";
//
//        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", powershellCommand);
//        processBuilder.redirectErrorStream(true); // Merges standard error into standard output
//
//        Process process = processBuilder.start();
//
//        // Read the output from the command
//        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        StringBuilder output = new StringBuilder();
//        String line;
//        while ((line = reader.readLine()) != null) {
//            output.append(line);
//        }
//
//        int exitCode = process.waitFor();
//        if (exitCode == 0) {
//            String aumidResult = output.toString().trim();
//            if (!aumidResult.isEmpty()) {
//                // Return the AUMID. If multiple IDs are found, this will return a comma-separated list.
//                return aumidResult;
//            }
//        }
//
//        // Return null if no AUMID was found or the command failed.
//        return null;
//    }
//
//    /**
//     * Finds the AUMIDs and display names of all UWP (Universal Windows Platform) applications
//     * installed on the system using a PowerShell command.
//     * @return A map of display names and their corresponding AUMIDs.
//     */
//    private Map<String, String> getUWPApps() {
//        Map<String, String> uwpApps = new HashMap<>();
//        try {
//            // The PowerShell command to get all AppxPackage objects, select their Name and PackageFamilyName,
//            // and format them as a single string for parsing.
//            String powershellCommand = "powershell.exe -NoProfile -Command \"Get-AppxPackage | Select-Object Name, PackageFamilyName | ForEach-Object { $_.Name + ';' + $_.PackageFamilyName + '!' + (Get-AppxPackageManifest -Package $_).package.applications.application.id } \"";
//
//            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", powershellCommand);
//            processBuilder.redirectErrorStream(true);
//            Process process = processBuilder.start();
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] parts = line.split(";", 2);
//                if (parts.length == 2) {
//                    String displayName = parts[0];
//                    String aumid = parts[1];
//                    // Clean up the display name for better readability
//                    displayName = displayName.replace("Name=", "").trim();
//                    aumid = aumid.replace("PackageFamilyName=", "").trim();
//                    String cleanedName = cleanAndFilterAppName(displayName);
//                    if (cleanedName != null) {
//                        uwpApps.put(cleanedName, aumid);
//                    }
//                }
//            }
//            process.waitFor();
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//        return uwpApps;
//    }
//
//    /**
//     * Replicates the Python code's winreg functionality to get installed programs.
//     * Searches both HKEY_LOCAL_MACHINE and HKEY_CURRENT_USER.
//     * @return A map of application display names and their installation locations.
//     */
//    private Map<String, String> getInstalledPrograms() {
//        Map<String, String> programPaths = new HashMap<>();
//        // Search in HKEY_LOCAL_MACHINE for 64-bit and 32-bit apps
//        getProgramsFromRegistry(WinReg.HKEY_LOCAL_MACHINE, UNINSTALL_REGISTRY_PATH_64, programPaths);
//        getProgramsFromRegistry(WinReg.HKEY_LOCAL_MACHINE, UNINSTALL_REGISTRY_PATH_32, programPaths);
//        // Search in HKEY_CURRENT_USER for 64-bit and 32-bit apps
//        getProgramsFromRegistry(WinReg.HKEY_CURRENT_USER, UNINSTALL_REGISTRY_PATH_64, programPaths);
//        getProgramsFromRegistry(WinReg.HKEY_CURRENT_USER, UNINSTALL_REGISTRY_PATH_32, programPaths);
//        return programPaths;
//    }
//
//    private void getProgramsFromRegistry(WinReg.HKEY hkey, String registryPath, Map<String, String> programPaths) {
//        try {
//            String[] subkeys = Advapi32Util.registryGetKeys(hkey, registryPath);
//            for (String subkeyName : subkeys) {
//                String subkeyPath = registryPath + "\\" + subkeyName;
//                try {
//                    if (Advapi32Util.registryValueExists(hkey, subkeyPath, "DisplayName") &&
//                            Advapi32Util.registryValueExists(hkey, subkeyPath, "InstallLocation")) {
//
//                        String displayName = Advapi32Util.registryGetStringValue(hkey, subkeyPath, "DisplayName");
//                        String installLocation = Advapi32Util.registryGetStringValue(hkey, subkeyPath, "InstallLocation");
//
//                        if (installLocation != null && !installLocation.trim().isEmpty()) {
//                            programPaths.put(displayName, installLocation);
//                        }
//                    }
//                } catch (Exception e) {
//                    // Ignore keys that don't have DisplayName or InstallLocation
//                }
//            }
//        } catch (Exception e) {
//            // Ignore registry keys that cannot be accessed
//        }
//    }
//
//    /**
//     * Replicates the Python code's os.walk functionality.
//     * Recursively searches a directory and its subdirectories for the first .exe file.
//     * @param folderPath The path to the installation directory.
//     * @return The absolute path to the first executable found, or null if none is found.
//     */
//    private String findExecutableInFolder(String folderPath) {
//        File folder = new File(folderPath);
//        if (!folder.isDirectory()) {
//            return null;
//        }
//
//        try {
//            List<Path> exeFiles = Files.walk(folder.toPath())
//                    .filter(Files::isRegularFile)
//                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".exe"))
//                    .limit(1) // We only need the first one we find
//                    .toList();
//
//            if (!exeFiles.isEmpty()) {
//                return exeFiles.get(0).toString();
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    /**
//     * Searches common user directories like Desktop and Downloads for executable files
//     * that match the given app name.
//     * @param appName The name of the app to search for.
//     * @return The absolute path to the first matching executable found, or null if none is found.
//     */
//    private String findExecutableInUserFolders(String appName) {
//        String userHome = System.getProperty("user.home");
//        Path desktopPath = Paths.get(userHome, "Desktop");
//        Path downloadsPath = Paths.get(userHome, "Downloads");
//
//        try {
//            // Search Desktop folder
//            List<Path> desktopFiles = Files.walk(desktopPath)
//                    .filter(Files::isRegularFile)
//                    .filter(p -> p.getFileName().toString().toLowerCase().contains(appName.toLowerCase()) && p.toString().toLowerCase().endsWith(".exe"))
//                    .limit(1)
//                    .toList();
//            if (!desktopFiles.isEmpty()) {
//                return desktopFiles.get(0).toString();
//            }
//
//            // Search Downloads folder
//            List<Path> downloadsFiles = Files.walk(downloadsPath)
//                    .filter(Files::isRegularFile)
//                    .filter(p -> p.getFileName().toString().toLowerCase().contains(appName.toLowerCase()) && p.toString().toLowerCase().endsWith(".exe"))
//                    .limit(1)
//                    .toList();
//            if (!downloadsFiles.isEmpty()) {
//                return downloadsFiles.get(0).toString();
//            }
//        } catch (IOException e) {
//            // Error during file walk, but we'll return null to indicate nothing was found.
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    private Map<String, String> getPortableAppsFromCommonFolders() {
//        Map<String, String> portableApps = new HashMap<>();
//        String userHome = System.getProperty("user.home");
//        Path desktopPath = Paths.get(userHome, "Desktop");
//        Path downloadsPath = Paths.get(userHome, "Downloads");
//
//        try {
//            // Search Desktop folder
//            Files.walk(desktopPath)
//                    .filter(Files::isRegularFile)
//                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".exe"))
//                    .forEach(p -> portableApps.put(p.getFileName().toString().replace(".exe", ""), p.toString()));
//
//            // Search Downloads folder
//            Files.walk(downloadsPath)
//                    .filter(Files::isRegularFile)
//                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".exe"))
//                    .forEach(p -> portableApps.put(p.getFileName().toString().replace(".exe", ""), p.toString()));
//        } catch (IOException e) {
//            // Handle error, but don't stop the program.
//            e.printStackTrace();
//        }
//
//        return portableApps;
//    }
//
//    @FXML
//    private void browseFile() {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Select Application Executable");
//        fileChooser.getExtensionFilters().add(
//                new FileChooser.ExtensionFilter("Executable Files", "*.exe")
//        );
//
//        Stage stage = (Stage) appPathField.getScene().getWindow();
//        File selectedFile = fileChooser.showOpenDialog(stage);
//        if (selectedFile != null) {
//            appPathField.setText(selectedFile.getAbsolutePath());
//        }
//    }
//
//    @FXML
//    private void saveApp() {
//        String name = appNameField.getEditor().getText();
//        String path = appPathField.getText();
//
//        if (name == null || name.trim().isEmpty()) {
//            showAlert("Missing", "App Name Required", "Please enter an application name.");
//            return;
//        }
//
//        // Check for duplicates before adding
//        for (String existingApp : existingAppNames) {
//            if (existingApp.equalsIgnoreCase(name.trim())) {
//                showAlert("Duplicate App", "App Already Exists",
//                        "This application is already added. Please select another app.");
//                return;
//            }
//        }
//
//        parentController.addAppToList(category, name, path);
//        closeWindow();
//    }
//
//    private void closeWindow() {
//        ((Stage) appNameField.getScene().getWindow()).close();
//    }
//
//    private void showAlert(String title, String header, String content) {
//        Alert alert = new Alert(Alert.AlertType.ERROR);
//        alert.setTitle(title);
//        alert.setHeaderText(header);
//        alert.setContentText(content);
//        alert.showAndWait();
//    }
//}
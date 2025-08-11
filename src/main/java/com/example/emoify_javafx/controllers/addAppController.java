package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class addAppController {

    @FXML private VBox mainContainer;
    @FXML private Button submitButton;
    @FXML private ScrollPane scrollPane;

    private Consumer<Boolean> appSubmitHandler;

    private String userName;
    private final String[] categories = {"Songs", "Entertainment", "SocialMedia",
            "Games", "Communication", "Help", "Other"};
    private Map<String, VBox> categoryContainers = new HashMap<>();
    private Map<String, List<AppData>> categoryApps = new HashMap<>();

    public static class AppData {
        public String name;
        public String path;
        ImageView icon;

        public AppData(String name, String path, ImageView icon) {
            this.name = name;
            this.path = path;
            this.icon = icon;
        }
    }

    @FXML
    private void initialize() {
        initializeCategories();
    }

    public void setAppSubmitHandler(Consumer<Boolean> handler) {
        this.appSubmitHandler = handler;
    }

    private void initializeCategories() {
        for (String category : categories) {
            categoryApps.put(category, new ArrayList<>());

            VBox categoryBox = new VBox();
            categoryBox.getStyleClass().add("category-card");
            categoryBox.setSpacing(10);

            HBox headerBox = new HBox();
            headerBox.setSpacing(10);

            Label categoryLabel = new Label(category);
            categoryLabel.getStyleClass().add("category-title");

            Button addButton = new Button("+");
            addButton.getStyleClass().add("add-button");
            addButton.setOnAction(e -> showAddAppPopup(category));

            headerBox.getChildren().addAll(addButton, categoryLabel);

            ScrollPane appScrollPane = new ScrollPane();
            appScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            appScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            appScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

            VBox appsContainer = new VBox();
            appsContainer.setSpacing(10);
            appScrollPane.setContent(appsContainer);

            categoryBox.getChildren().addAll(headerBox, appScrollPane);
            mainContainer.getChildren().add(categoryBox);

            categoryContainers.put(category, appsContainer);
        }
    }

    public void showAddAppPopup(String category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/emoify_javafx/fxmls/addAppPopup.fxml"));
            Parent root = loader.load();

            addAppPopupController controller = loader.getController();
            controller.setCategory(category);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Add Application");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open add app window", e.getMessage());
        }
    }

    public void addAppToList(String category, String name, String path) {
        System.out.println("category1:" + category);
        try {
            // Create icon (simplified - in real app you'd load actual icon)
            // Create a placeholder icon. In a real app, you would load a proper icon.
            //Image placeholderImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/emoify_javafx/assets/Other.png")));
            ImageView iconView = new ImageView();
//            iconView.setFitHeight(50);
//            iconView.setFitWidth(50);
            // Here you would load the actual icon image

            AppData appData = new AppData(name, path, iconView);
            categoryApps.get(category).add(appData);
            updateAppList(category);
        } catch (Exception e) {
            showAlert("Error", "Failed to add application", e.getMessage());
        }
    }

    private void updateAppList(String category) {
        VBox container = categoryContainers.get(category);
        container.getChildren().clear();

        for (AppData app : categoryApps.get(category)) {
            VBox appBox = new VBox();
            appBox.getStyleClass().add("app-item");
            appBox.setSpacing(5);

            ImageView iconView = app.icon;

            Label nameLabel = new Label(app.name);
            nameLabel.getStyleClass().add("app-name");

            Button deleteButton = new Button("×");
            deleteButton.getStyleClass().add("delete-button");
            deleteButton.setOnAction(e -> confirmDeleteApp(category, app));

            appBox.getChildren().addAll(iconView, nameLabel, deleteButton);
            container.getChildren().add(appBox);
        }
    }

    private void confirmDeleteApp(String category, AppData app) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete App");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to remove " + app.name + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                categoryApps.get(category).remove(app);
                updateAppList(category);
                // Also remove from database
            }
        });
    }

    @FXML
    private void handleSubmit() {
        System.out.println("Submitting all app data...");

        ApiClient.addApp(categoryApps) // You need to pass categories array
                .thenAccept(status -> {
                    Platform.runLater(() -> {
                        if (status >= 200 && status < 300) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setHeaderText(null);
                            alert.setContentText("Your preferences have been saved!");
                            alert.showAndWait();

                            if (appSubmitHandler != null) {
                                appSubmitHandler.accept(true);
                            }

                            // Close the window
                            ((Stage) submitButton.getScene().getWindow()).close();
                        } else {
                            showAlert("Database Error", "Failed to save application data",
                                    "Server returned status: " + status);
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        showAlert("Database Error", "Failed to save application data",
                                e.getMessage());
                    });
                    return null;
                });
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

package com.example.emoify_javafx.controllers;

import com.example.emoify_javafx.models.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class SearchController {

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton, closeBtn;

    @FXML
    private Label appLabel, versionLabel, authorLabel;
    private Consumer<String> searchSuccessHandler;

    public void setSearchSuccessHandler(Consumer<String> handler) {
        this.searchSuccessHandler = handler;
    }

    public void existingSearchQuery(String search){
        searchField.setText(search);
    }

    @FXML
    void handleSearch() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            System.out.println("Search Query: " + query);

            if (searchSuccessHandler != null) {
                searchSuccessHandler.accept(query);
            }

        }
    }

    @FXML
    void handleCloseBtn(MouseEvent event){

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();

    }


}


module com.example.emoify_javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;
    requires org.json;
    requires com.jfoenix;

    opens com.example.emoify_javafx to javafx.fxml;
    opens com.example.emoify_javafx.controllers to javafx.fxml;
    // Export your controller package to javafx.fxml
    exports com.example.emoify_javafx.controllers to javafx.fxml;

    // If you have other packages that need to be accessible
    exports com.example.emoify_javafx;
}
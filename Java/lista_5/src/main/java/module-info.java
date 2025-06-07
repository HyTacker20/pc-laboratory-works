module com.shapeeditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    
    // Export the actual packages used in the application
    exports com.shapeeditor;
    exports com.shapeeditor.controller;
    exports com.shapeeditor.model;
    exports com.shapeeditor.persistence;
    exports com.shapeeditor.view;
    exports com.shapeeditor.util;
    
    // Open packages to JavaFX
    opens com.shapeeditor to javafx.fxml, javafx.graphics;
    opens com.shapeeditor.controller to javafx.fxml, javafx.graphics;
    opens com.shapeeditor.view to javafx.fxml, javafx.graphics;
    opens com.shapeeditor.persistence to javafx.fxml, javafx.graphics;
    opens com.shapeeditor.model to javafx.fxml, javafx.graphics;
    opens com.shapeeditor.util to javafx.fxml, javafx.graphics;
}
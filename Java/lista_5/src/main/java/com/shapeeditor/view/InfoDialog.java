package com.shapeeditor.view;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Dialog showing application information.
 */
public class InfoDialog extends Dialog<Void> {
    
    /**
     * Constructs a new InfoDialog with the specified owner.
     *
     * @param owner The owner stage
     */
    public InfoDialog(Stage owner) {
        // Set the dialog title and owner
        setTitle("Shape Editor - Information");
        initOwner(owner);
        
        // Create the content
        VBox content = createContent();
        getDialogPane().setContent(content);
        
        // Add an OK button
        getDialogPane().getButtonTypes().add(ButtonType.OK);
    }
    
    /**
     * Creates the content for the dialog.
     *
     * @return The content VBox
     */
    private VBox createContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        // Add the application name
        Label nameLabel = new Label("Shape Editor");
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        content.getChildren().add(nameLabel);
        
        // Add the version
        Label versionLabel = new Label("Version 1.0");
        content.getChildren().add(versionLabel);
        
        // Add a separator
        content.getChildren().add(new Label(""));
        
        // Add the description
        Label descriptionLabel = new Label(
            "Shape Editor is a simple application for creating and manipulating shapes. " +
            "It allows you to create circles, rectangles, and polygons, and to move, " +
            "rotate, and change their colors."
        );
        descriptionLabel.setWrapText(true);
        content.getChildren().add(descriptionLabel);
        
        // Add a separator
        content.getChildren().add(new Label(""));
        
        // Add the copyright
        Label copyrightLabel = new Label("© 2025 Shape Editor Team");
        content.getChildren().add(copyrightLabel);
        
        return content;
    }
}
package com.shapeeditor.view;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Dialog showing the user guide for the application.
 */
public class UserGuideDialog extends Dialog<Void> {
    
    /**
     * Constructs a new UserGuideDialog with the specified owner.
     *
     * @param owner The owner stage
     */
    public UserGuideDialog(Stage owner) {
        // Set the dialog title and owner
        setTitle("Shape Editor - User Guide");
        initOwner(owner);
        
        // Create the content
        VBox content = createContent();
        
        // Wrap the content in a scroll pane
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        
        getDialogPane().setContent(scrollPane);
        
        // Add an OK button
        getDialogPane().getButtonTypes().add(ButtonType.OK);
    }
    
    /**
     * Creates the content for the dialog.
     *
     * @return The content VBox
     */
    private VBox createContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // Add the title
        Text titleText = new Text("Shape Editor User Guide");
        titleText.setFont(Font.font("System", FontWeight.BOLD, 18));
        content.getChildren().add(titleText);
        
        // Add the introduction
        addSection(content, "Introduction", 
            "Shape Editor is a simple application for creating and manipulating shapes. " +
            "This guide will help you understand how to use the application effectively."
        );
        
        // Add the toolbar section
        addSection(content, "Toolbar", 
            "The toolbar on the left side of the application contains buttons for creating " +
            "and manipulating shapes. The toolbar is divided into three sections:\n\n" +
            "1. Shapes: Contains buttons for selecting the shape type to create.\n" +
            "2. Actions: Contains buttons for manipulating the selected shape.\n" +
            "3. Color: Contains a color picker for selecting the fill color of shapes."
        );
        
        // Add the shapes section
        addSection(content, "Creating Shapes", 
            "To create a shape, follow these steps:\n\n" +
            "1. Click on one of the shape buttons in the toolbar (Circle, Rectangle, or Polygon).\n" +
            "2. Click on the canvas where you want to create the shape.\n" +
            "3. The shape will be created at the clicked position with default size and the selected color.\n\n" +
            "After creating a shape, the application automatically switches back to selection mode."
        );
        
        // Add the selection section
        addSection(content, "Selecting and Moving Shapes", 
            "To select a shape, make sure the 'Select' button in the toolbar is active, " +
            "then click on the shape you want to select. The selected shape will be highlighted " +
            "with a blue border.\n\n" +
            "To move a shape, click and drag it to the desired position."
        );
        
        // Add the rotation section
        addSection(content, "Rotating Shapes", 
            "To rotate a shape, first select it, then click the 'Rotate' button in the toolbar. " +
            "Each click rotates the shape by 45 degrees clockwise."
        );
        
        // Add the deletion section
        addSection(content, "Deleting Shapes", 
            "To delete a shape, first select it, then click the 'Delete' button in the toolbar."
        );
        
        // Add the color section
        addSection(content, "Changing Colors", 
            "To change the color of a shape, first select the desired color from the color picker " +
            "in the toolbar, then create a new shape. The new shape will have the selected color.\n\n" +
            "Note: Currently, you cannot change the color of an existing shape."
        );
        
        // Add the file operations section
        addSection(content, "File Operations", 
            "The File menu contains the following options:\n\n" +
            "1. New: Clears the canvas and starts a new drawing.\n" +
            "2. Open: Opens a saved drawing (not implemented in this version).\n" +
            "3. Save: Saves the current drawing (not implemented in this version).\n" +
            "4. Exit: Closes the application."
        );
        
        return content;
    }
    
    /**
     * Adds a section to the content with a title and description.
     *
     * @param content The content VBox
     * @param title The section title
     * @param description The section description
     */
    private void addSection(VBox content, String title, String description) {
        // Add the section title
        Text titleText = new Text(title);
        titleText.setFont(Font.font("System", FontWeight.BOLD, 14));
        content.getChildren().add(titleText);
        
        // Add the section description
        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        content.getChildren().add(descriptionLabel);
        
        // Add some space
        content.getChildren().add(new Label(""));
    }
}
package com.shapeeditor.view;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Dialog for displaying performance test results.
 * Shows the results of the performance tests in a scrollable text area.
 */
public class PerformanceResultsDialog extends Dialog<Void> {
    /** The text area for displaying the results */
    private TextArea resultsTextArea;
    
    /**
     * Constructs a new PerformanceResultsDialog with the specified owner and results.
     *
     * @param owner The owner stage
     * @param results The performance test results to display
     */
    public PerformanceResultsDialog(Stage owner, String results) {
        // Set the title and header
        setTitle("Performance Test Results");
        setHeaderText("Shape Editor Performance Test Results");
        
        // Set the owner
        initOwner(owner);
        
        // Create the content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Add a label
        Label label = new Label("The following results show the performance of the optimized components:");
        content.getChildren().add(label);
        
        // Create a text area for the results
        resultsTextArea = new TextArea(results);
        resultsTextArea.setEditable(false);
        resultsTextArea.setWrapText(true);
        resultsTextArea.setPrefWidth(600);
        resultsTextArea.setPrefHeight(400);
        content.getChildren().add(resultsTextArea);
        
        // Set the content
        getDialogPane().setContent(content);
        
        // Add OK button
        getDialogPane().getButtonTypes().add(ButtonType.OK);
    }
}
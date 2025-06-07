package com.shapeeditor;

import com.shapeeditor.controller.ShapeEditorController;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main application class for the Shape Editor.
 * This class serves as the entry point for the JavaFX application.
 * It initializes all components and connects them together.
 */
public class ShapeEditorApplication extends Application {
    
    /** The main controller for the application */
    private ShapeEditorController controller;
    
    /**
     * The main entry point for the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Create the main controller
            controller = new ShapeEditorController(primaryStage);
            
            // Ensure the FileController has access to the primary stage
            if (controller.getFileController() != null) {
                controller.getFileController().setPrimaryStage(primaryStage);
            }
            
            // Request focus for the drawing canvas to enable keyboard events
            if (controller.getMainView() != null && 
                controller.getMainView().getDrawingCanvas() != null) {
                controller.getMainView().getDrawingCanvas().requestFocus();
            }
            
            System.out.println("Shape Editor application started successfully");
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * The stop method is called when the application should stop, and provides a
     * chance to clean up resources before the application exits.
     */
    @Override
    public void stop() {
        try {
            // Perform cleanup operations here
            System.out.println("Shape Editor application stopping...");
            
            // Additional cleanup if needed
            
            System.out.println("Shape Editor application stopped successfully");
        } catch (Exception e) {
            System.err.println("Error stopping application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
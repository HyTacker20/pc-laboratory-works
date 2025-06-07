package com.shapeeditor.controller;

import com.shapeeditor.view.MainView;
import com.shapeeditor.view.DrawingCanvas;
import com.shapeeditor.view.ToolBar;
import javafx.stage.Stage;

/**
 * Main controller for the Shape Editor application.
 * Coordinates the other controllers and connects the view components.
 */
public class ShapeEditorController {
    /** The main view of the application */
    private MainView mainView;
    
    /** Controller for drawing operations */
    private DrawingController drawingController;
    
    /** Controller for selection operations */
    private SelectionController selectionController;
    
    /** Controller for file operations */
    private FileController fileController;
    
    /**
     * Constructs a new ShapeEditorController with the specified primary stage.
     *
     * @param primaryStage The primary stage for the application
     */
    public ShapeEditorController(Stage primaryStage) {
        // Initialize the main view
        this.mainView = new MainView(primaryStage);
        
        // Initialize the sub-controllers
        initializeControllers();
        
        // Connect the view components with the controllers
        connectComponents();
        
        // Show the main window
        mainView.show();
    }
    
    /**
     * Initializes the sub-controllers.
     */
    private void initializeControllers() {
        DrawingCanvas canvas = mainView.getDrawingCanvas();
        ToolBar toolBar = mainView.getToolBar();
        
        // Initialize the drawing controller
        drawingController = new DrawingController(canvas, toolBar);
        
        // Initialize the selection controller
        selectionController = new SelectionController(canvas, toolBar);
        
        // Initialize the file controller
        fileController = new FileController(canvas);
    }
    
    /**
     * Connects the view components with the controllers.
     */
    private void connectComponents() {
        // Set up menu item handlers
        setupMenuHandlers();
        
        // Connect the toolbar with the canvas
        mainView.getToolBar().setCanvas(mainView.getDrawingCanvas());
    }
    
    /**
     * Sets up the menu item handlers.
     */
    private void setupMenuHandlers() {
        // Connect File menu items to the FileController
        mainView.setOnNewAction(() -> fileController.newFile());
        mainView.setOnOpenAction(() -> fileController.openFile());
        mainView.setOnSaveAction(() -> fileController.saveFile());
        
        // Connect drawing mode selection to the DrawingController
        mainView.setOnDrawingModeChange(mode -> drawingController.setDrawingMode(mode));
    }
    
    /**
     * Gets the main view of the application.
     *
     * @return The main view
     */
    public MainView getMainView() {
        return mainView;
    }
    
    /**
     * Gets the drawing controller.
     *
     * @return The drawing controller
     */
    public DrawingController getDrawingController() {
        return drawingController;
    }
    
    /**
     * Gets the selection controller.
     *
     * @return The selection controller
     */
    public SelectionController getSelectionController() {
        return selectionController;
    }
    
    /**
     * Gets the file controller.
     *
     * @return The file controller
     */
    public FileController getFileController() {
        return fileController;
    }
}
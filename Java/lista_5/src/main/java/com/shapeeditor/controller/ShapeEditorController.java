package com.shapeeditor.controller;

import com.shapeeditor.view.DrawingCanvas;
import com.shapeeditor.view.MainView;
import com.shapeeditor.view.PerformanceResultsDialog;
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
    
    /** Flag indicating if performance testing is enabled */
    private boolean performanceTestingEnabled = false;
    
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
        
        // Connect the selection controller with the toolbar
        toolBar.setSelectionController(selectionController);
        
        // Connect the drawing controller with the toolbar
        toolBar.setDrawingController(drawingController);
        
        // Set the drawing controller on the canvas
        canvas.setDrawingController(drawingController);
        
        // Set the default drawing mode to CIRCLE so users can start drawing right away
        drawingController.setDrawingMode(DrawingController.DrawingMode.CIRCLE);
        toolBar.selectButton(ToolBar.ToolbarButtonType.CIRCLE);
    }
    
    /**
     * Connects the view components with the controllers.
     */
    private void connectComponents() {
        // Set up menu item handlers
        setupMenuHandlers();
        
        // Connect the toolbar with the canvas
        mainView.getToolBar().setCanvas(mainView.getDrawingCanvas());
        
        // Set the primary stage for the file controller
        fileController.setPrimaryStage(mainView.getPrimaryStage());
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
        
        // Connect performance test menu item
        mainView.setOnPerformanceTestAction(this::handlePerformanceTest);
    }
    
    /**
     * Handles the performance test action.
     * Runs the performance test and displays the results in a dialog.
     */
    private void handlePerformanceTest() {
        // Run the performance test
        String results = runPerformanceTest();
        
        // Display the results in a custom dialog
        PerformanceResultsDialog dialog = new PerformanceResultsDialog(
            mainView.getPrimaryStage(), results);
        
        // Show the dialog
        dialog.showAndWait();
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
    
    /**
     * Runs a performance test on the optimized components.
     * This method measures the performance of the DrawingCanvas, SelectionController,
     * ShapePersistence, and JsonConverter components, as well as their integrated performance.
     *
     * @return A string containing the test results
     */
    public String runPerformanceTest() {
        return com.shapeeditor.util.PerformanceTest.runPerformanceTest();
    }
    
    /**
     * Enables or disables performance testing.
     *
     * @param enabled true to enable performance testing, false to disable
     */
    public void setPerformanceTestingEnabled(boolean enabled) {
        this.performanceTestingEnabled = enabled;
    }
    
    /**
     * Checks if performance testing is enabled.
     *
     * @return true if performance testing is enabled, false otherwise
     */
    public boolean isPerformanceTestingEnabled() {
        return performanceTestingEnabled;
    }
}
package com.shapeeditor.view;

import java.util.function.Consumer;

import com.shapeeditor.controller.DrawingController;

import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * The main application window for the Shape Editor.
 * Contains the menu bar, toolbar, and drawing canvas.
 */
public class MainView {
    /** The primary stage for the application */
    private Stage primaryStage;
    
    /** The root layout for the application */
    private BorderPane rootLayout;
    
    /** The toolbar component */
    private ToolBar toolBar;
    
    /** The drawing canvas component */
    private DrawingCanvas drawingCanvas;
    
    /** The scene for the application */
    private Scene scene;
    
    /** Callback for the New menu item action */
    private Runnable onNewAction;
    
    /** Callback for the Open menu item action */
    private Runnable onOpenAction;
    
    /** Callback for the Save menu item action */
    private Runnable onSaveAction;
    
    /** Callback for drawing mode changes */
    private Consumer<DrawingController.DrawingMode> onDrawingModeChange;
    
    /** Callback for the Performance Test menu item action */
    private Runnable onPerformanceTestAction;
    
    /** Menu items */
    private MenuItem newItem;
    private MenuItem openItem;
    private MenuItem saveItem;
    private MenuItem performanceTestItem;
    
    /**
     * Constructs a new MainView with the specified primary stage.
     *
     * @param primaryStage The primary stage for the application
     */
    public MainView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Shape Editor");
        
        // Initialize the components
        initializeComponents();
        
        // Set up the scene
        scene = new Scene(rootLayout, 1000, 700);
        primaryStage.setScene(scene);
    }
    
    /**
     * Initializes the UI components.
     */
    private void initializeComponents() {
        // Create the root layout
        rootLayout = new BorderPane();
        
        // Create the menu bar
        MenuBar menuBar = createMenuBar();
        rootLayout.setTop(menuBar);
        
        // Create the toolbar
        toolBar = new ToolBar();
        rootLayout.setLeft(toolBar);
        
        // Create the drawing canvas
        drawingCanvas = new DrawingCanvas();
        rootLayout.setCenter(drawingCanvas);
    }
    
    /**
     * Creates the menu bar with File and Help menus.
     *
     * @return The created menu bar
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // Create File menu
        Menu fileMenu = new Menu("File");
        newItem = new MenuItem("New");
        openItem = new MenuItem("Open");
        saveItem = new MenuItem("Save");
        MenuItem exitItem = new MenuItem("Exit");
        
        // Add action handlers for File menu items
        newItem.setOnAction(event -> handleNewAction());
        openItem.setOnAction(event -> handleOpenAction());
        saveItem.setOnAction(event -> handleSaveAction());
        exitItem.setOnAction(event -> handleExitAction());
        
        fileMenu.getItems().addAll(newItem, openItem, saveItem, exitItem);
        
        // Create Tools menu
        Menu toolsMenu = new Menu("Tools");
        performanceTestItem = new MenuItem("Run Performance Test");
        
        // Add action handler for Performance Test menu item
        performanceTestItem.setOnAction(event -> handlePerformanceTestAction());
        
        toolsMenu.getItems().add(performanceTestItem);
        
        // Create Help menu
        Menu helpMenu = new Menu("Help");
        MenuItem userGuideItem = new MenuItem("User Guide");
        MenuItem infoItem = new MenuItem("Info");
        
        // Add action handlers for Help menu items
        userGuideItem.setOnAction(event -> handleUserGuideAction());
        infoItem.setOnAction(event -> handleInfoAction());
        
        helpMenu.getItems().addAll(userGuideItem, infoItem);
        
        // Add menus to menu bar
        menuBar.getMenus().addAll(fileMenu, toolsMenu, helpMenu);
        
        return menuBar;
    }
    
    /**
     * Shows the main window.
     */
    public void show() {
        primaryStage.show();
    }
    
    /**
     * Handles the New menu item action.
     */
    private void handleNewAction() {
        if (onNewAction != null) {
            onNewAction.run();
        } else {
            // Default implementation
            drawingCanvas.clearShapes();
        }
    }
    
    /**
     * Handles the Open menu item action.
     */
    private void handleOpenAction() {
        if (onOpenAction != null) {
            onOpenAction.run();
        } else {
            // Default implementation
            System.out.println("Open action triggered");
        }
    }
    
    /**
     * Handles the Save menu item action.
     */
    private void handleSaveAction() {
        if (onSaveAction != null) {
            onSaveAction.run();
        } else {
            // Default implementation
            System.out.println("Save action triggered");
        }
    }
    
    /**
     * Handles the Exit menu item action.
     */
    private void handleExitAction() {
        primaryStage.close();
    }
    
    /**
     * Handles the User Guide menu item action.
     */
    private void handleUserGuideAction() {
        UserGuideDialog userGuideDialog = new UserGuideDialog(primaryStage);
        userGuideDialog.showAndWait();
    }
    
    /**
     * Handles the Info menu item action.
     */
    private void handleInfoAction() {
        InfoDialog infoDialog = new InfoDialog(primaryStage);
        infoDialog.showAndWait();
    }
    
    /**
     * Handles the Performance Test menu item action.
     */
    private void handlePerformanceTestAction() {
        if (onPerformanceTestAction != null) {
            onPerformanceTestAction.run();
        } else {
            // Default implementation
            System.out.println("Performance Test action triggered");
        }
    }
    
    /**
     * Gets the drawing canvas.
     *
     * @return The drawing canvas
     */
    public DrawingCanvas getDrawingCanvas() {
        return drawingCanvas;
    }
    
    /**
     * Gets the toolbar.
     *
     * @return The toolbar
     */
    public ToolBar getToolBar() {
        return toolBar;
    }
    
    /**
     * Gets the primary stage.
     *
     * @return The primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Sets the callback for the New menu item action.
     *
     * @param onNewAction The callback to run when the New menu item is selected
     */
    public void setOnNewAction(Runnable onNewAction) {
        this.onNewAction = onNewAction;
    }
    
    /**
     * Sets the callback for the Open menu item action.
     *
     * @param onOpenAction The callback to run when the Open menu item is selected
     */
    public void setOnOpenAction(Runnable onOpenAction) {
        this.onOpenAction = onOpenAction;
    }
    
    /**
     * Sets the callback for the Save menu item action.
     *
     * @param onSaveAction The callback to run when the Save menu item is selected
     */
    public void setOnSaveAction(Runnable onSaveAction) {
        this.onSaveAction = onSaveAction;
    }
    
    /**
     * Sets the callback for drawing mode changes.
     *
     * @param onDrawingModeChange The callback to run when the drawing mode changes
     */
    public void setOnDrawingModeChange(Consumer<DrawingController.DrawingMode> onDrawingModeChange) {
        this.onDrawingModeChange = onDrawingModeChange;
    }
    
    /**
     * Notifies the drawing mode change callback.
     *
     * @param mode The new drawing mode
     */
    public void notifyDrawingModeChange(DrawingController.DrawingMode mode) {
        if (onDrawingModeChange != null) {
            onDrawingModeChange.accept(mode);
        }
    }
    
    /**
     * Sets the callback for the Performance Test menu item action.
     *
     * @param onPerformanceTestAction The callback to run when the Performance Test menu item is selected
     */
    public void setOnPerformanceTestAction(Runnable onPerformanceTestAction) {
        this.onPerformanceTestAction = onPerformanceTestAction;
    }
}
package com.shapeeditor.view;

import com.shapeeditor.controller.DrawingController;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Toolbar component containing buttons for shape creation and manipulation.
 */
public class ToolBar extends VBox {
    /** Toggle group for shape selection buttons */
    private ToggleGroup shapeToggleGroup;
    
    /** Toggle button for selection mode */
    private ToggleButton selectButton;
    
    /** Toggle button for circle creation */
    private ToggleButton circleButton;
    
    /** Toggle button for rectangle creation */
    private ToggleButton rectangleButton;
    
    /** Toggle button for polygon creation */
    private ToggleButton polygonButton;
    
    /** Toggle button for regular polygon creation */
    private ToggleButton regularPolygonButton;
    
    /** Button for deleting the selected shape */
    private Button deleteButton;
    
    /** Button for rotating the selected shape */
    private Button rotateButton;
    
    /** Color picker for selecting shape colors */
    private ColorPicker colorPicker;
    
    /** Currently selected color */
    private Color currentColor;
    
    /** Drawing canvas reference */
    private DrawingCanvas canvas;
    
    /** Selection controller reference */
    private com.shapeeditor.controller.SelectionController selectionController;
    
    /** Spinner for selecting the number of sides for regular polygons */
    private Spinner<Integer> sidesSpinner;
    
    /** Drawing controller reference */
    private DrawingController drawingController;
    
    /**
     * Enum representing the available toolbar buttons.
     */
    public enum ToolbarButtonType {
        /** Selection mode button */
        SELECT,
        
        /** Circle creation button */
        CIRCLE,
        
        /** Rectangle creation button */
        RECTANGLE,
        
        /** Polygon creation button */
        POLYGON,
        
        /** Regular polygon creation button */
        REGULAR_POLYGON
    }
    
    /**
     * Constructs a new ToolBar.
     */
    public ToolBar() {
        // Set up the layout
        setPadding(new Insets(10));
        setSpacing(10);
        setPrefWidth(150);
        
        // Initialize the color
        currentColor = Color.BLUE;
        
        // Create the UI components
        createShapeButtons();
        createActionButtons();
        createColorPicker();
    }
    
    /**
     * Creates the shape selection buttons.
     */
    private void createShapeButtons() {
        // Create a label for the shape section
        Label shapesLabel = new Label("Shapes");
        getChildren().add(shapesLabel);
        
        // Create a toggle group for the shape buttons
        shapeToggleGroup = new ToggleGroup();
        
        // Create the selection button
        selectButton = new ToggleButton("Select");
        selectButton.setTooltip(new Tooltip("Select and move shapes"));
        selectButton.setToggleGroup(shapeToggleGroup);
        selectButton.setPrefWidth(130);
        selectButton.setSelected(true);
        getChildren().add(selectButton);
        
        // Create the circle button
        circleButton = new ToggleButton("Circle");
        circleButton.setTooltip(new Tooltip("Create a circle"));
        circleButton.setToggleGroup(shapeToggleGroup);
        circleButton.setPrefWidth(130);
        getChildren().add(circleButton);
        
        // Create the rectangle button
        rectangleButton = new ToggleButton("Rectangle");
        rectangleButton.setTooltip(new Tooltip("Create a rectangle"));
        rectangleButton.setToggleGroup(shapeToggleGroup);
        rectangleButton.setPrefWidth(130);
        getChildren().add(rectangleButton);
        
        // Create the polygon button
        polygonButton = new ToggleButton("Polygon");
        polygonButton.setTooltip(new Tooltip("Create a polygon by clicking points"));
        polygonButton.setToggleGroup(shapeToggleGroup);
        polygonButton.setPrefWidth(130);
        getChildren().add(polygonButton);
        
        // Create the regular polygon button
        regularPolygonButton = new ToggleButton("Regular Polygon");
        regularPolygonButton.setTooltip(new Tooltip("Create a regular polygon with specified sides"));
        regularPolygonButton.setToggleGroup(shapeToggleGroup);
        regularPolygonButton.setPrefWidth(130);
        getChildren().add(regularPolygonButton);
        
        // Create a spinner for selecting the number of sides
        Label sidesLabel = new Label("Number of sides:");
        getChildren().add(sidesLabel);
        
        sidesSpinner = new Spinner<>(3, 20, 3, 1);
        sidesSpinner.setEditable(true);
        sidesSpinner.setPrefWidth(130);
        sidesSpinner.setTooltip(new Tooltip("Set the number of sides for regular polygons"));
        getChildren().add(sidesSpinner);
        
        // Add a separator
        getChildren().add(new Separator(Orientation.HORIZONTAL));
    }
    
    /**
     * Creates the action buttons.
     */
    private void createActionButtons() {
        // Create a label for the actions section
        Label actionsLabel = new Label("Actions");
        getChildren().add(actionsLabel);
        
        // Create the delete button
        deleteButton = new Button("Delete");
        deleteButton.setTooltip(new Tooltip("Delete the selected shape"));
        deleteButton.setPrefWidth(130);
        deleteButton.setDisable(true);
        getChildren().add(deleteButton);
        
        // Create the rotate button
        rotateButton = new Button("Rotate");
        rotateButton.setTooltip(new Tooltip("Rotate the selected shape"));
        rotateButton.setPrefWidth(130);
        rotateButton.setDisable(true);
        getChildren().add(rotateButton);
        
        // Add a separator
        getChildren().add(new Separator(Orientation.HORIZONTAL));
    }
    
    /**
     * Creates the color picker.
     */
    private void createColorPicker() {
        // Create a label for the color section
        Label colorLabel = new Label("Color");
        getChildren().add(colorLabel);
        
        // Create the color picker
        colorPicker = new ColorPicker(currentColor);
        colorPicker.setOnColorChange(this::handleColorChange);
        getChildren().add(colorPicker);
    }
    
    /**
     * Sets the drawing canvas reference.
     *
     * @param canvas The drawing canvas
     */
    public void setCanvas(DrawingCanvas canvas) {
        this.canvas = canvas;
        
        // Set up the button actions
        setupButtonActions();
    }
    
    /**
     * Sets the selection controller reference.
     *
     * @param selectionController The selection controller
     */
    public void setSelectionController(com.shapeeditor.controller.SelectionController selectionController) {
        this.selectionController = selectionController;
    }
    
    /**
     * Sets the drawing controller reference.
     *
     * @param drawingController The drawing controller
     */
    public void setDrawingController(DrawingController drawingController) {
        this.drawingController = drawingController;
    }
    
    /**
     * Sets up the button actions.
     */
    private void setupButtonActions() {
        // Set up the delete button action
        deleteButton.setOnAction(event -> {
            if (canvas != null && canvas.getSelectedShape() != null) {
                canvas.removeShape(canvas.getSelectedShape());
                updateButtonStates();
            }
        });
        
        // Set up the rotate button action
        rotateButton.setOnAction(event -> {
            if (canvas != null && canvas.getSelectedShape() != null) {
                canvas.getSelectedShape().rotate(45);
                canvas.redraw();
            }
        });
        
        // Set up the sides spinner value change listener
        sidesSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (drawingController != null) {
                drawingController.setPolygonSides(newValue);
            }
        });
        
        // Set up button selection listeners
        selectButton.setOnAction(event -> {
            if (selectButton.isSelected() && drawingController != null) {
                drawingController.setDrawingMode(DrawingController.DrawingMode.SELECT);
            }
        });
        
        circleButton.setOnAction(event -> {
            if (circleButton.isSelected() && drawingController != null) {
                drawingController.setDrawingMode(DrawingController.DrawingMode.CIRCLE);
            }
        });
        
        rectangleButton.setOnAction(event -> {
            if (rectangleButton.isSelected() && drawingController != null) {
                drawingController.setDrawingMode(DrawingController.DrawingMode.RECTANGLE);
            }
        });
        
        polygonButton.setOnAction(event -> {
            if (polygonButton.isSelected() && drawingController != null) {
                drawingController.setDrawingMode(DrawingController.DrawingMode.POLYGON);
            }
        });
        
        regularPolygonButton.setOnAction(event -> {
            if (regularPolygonButton.isSelected() && drawingController != null) {
                drawingController.setDrawingMode(DrawingController.DrawingMode.REGULAR_POLYGON);
                // Set the number of sides from the spinner
                drawingController.setPolygonSides(sidesSpinner.getValue());
            }
        });
    }
    
    /**
     * Updates the state of the action buttons based on the selected shape.
     */
    public void updateButtonStates() {
        boolean hasSelection = (canvas != null && canvas.getSelectedShape() != null);
        deleteButton.setDisable(!hasSelection);
        rotateButton.setDisable(!hasSelection);
    }
    
    /**
     * Gets the currently selected color.
     *
     * @return The current color
     */
    public Color getCurrentColor() {
        return currentColor;
    }
    
    /**
     * Selects the specified button.
     *
     * @param buttonType The button type to select
     */
    public void selectButton(ToolbarButtonType buttonType) {
        switch (buttonType) {
            case SELECT:
                selectButton.setSelected(true);
                break;
            case CIRCLE:
                circleButton.setSelected(true);
                break;
            case RECTANGLE:
                rectangleButton.setSelected(true);
                break;
            case POLYGON:
                polygonButton.setSelected(true);
                break;
            case REGULAR_POLYGON:
                regularPolygonButton.setSelected(true);
                break;
        }
    }
    
    /**
     * Checks if the application is in selection mode.
     *
     * @return true if in selection mode, false otherwise
     */
    public boolean isSelectionMode() {
        return selectButton.isSelected();
    }
    
    /**
     * Sets the color of the color picker.
     *
     * @param color The new color
     */
    public void setColor(Color color) {
        // Update both the color picker and the current color
        colorPicker.setColor(color);
        currentColor = color;
    }
    
    /**
     * Handles color change events from the color picker.
     *
     * @param color The new color
     */
    private void handleColorChange(Color color) {
        // Always update the current color
        currentColor = color;
        
        // If we have a selection controller and we're in selection mode,
        // apply the color change to the selected shape
        if (selectionController != null && isSelectionMode()) {
            selectionController.changeShapeColor(color);
        }
    }
    
    /**
     * Gets the color picker component.
     *
     * @return The color picker
     */
    public ColorPicker getColorPicker() {
        return colorPicker;
    }
    
    /**
     * Gets the number of sides selected for regular polygons.
     *
     * @return The number of sides
     */
    public int getPolygonSides() {
        return sidesSpinner.getValue();
    }
}
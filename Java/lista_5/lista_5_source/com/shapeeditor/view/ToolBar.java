package com.shapeeditor.view;

import com.shapeeditor.model.Circle;
import com.shapeeditor.model.Rectangle;
import com.shapeeditor.model.Polygon;
import com.shapeeditor.model.ShapeFactory;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
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
    
    /**
     * Enum representing the available toolbar buttons.
     */
    public enum Button {
        /** Selection mode button */
        SELECT,
        
        /** Circle creation button */
        CIRCLE,
        
        /** Rectangle creation button */
        RECTANGLE,
        
        /** Polygon creation button */
        POLYGON
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
        polygonButton.setTooltip(new Tooltip("Create a polygon"));
        polygonButton.setToggleGroup(shapeToggleGroup);
        polygonButton.setPrefWidth(130);
        getChildren().add(polygonButton);
        
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
        colorPicker.setOnColorChange(color -> currentColor = color);
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
        
        // Set up the canvas click handler for shape creation
        if (canvas != null) {
            canvas.setOnMouseClicked(event -> {
                if (selectButton.isSelected()) {
                    // Selection mode - do nothing here, the canvas handles selection
                    updateButtonStates();
                } else {
                    // Shape creation mode
                    double x = event.getX();
                    double y = event.getY();
                    
                    if (circleButton.isSelected()) {
                        // Create a circle
                        Circle circle = ShapeFactory.createCircle(x, y, 50, currentColor);
                        canvas.addShape(circle);
                    } else if (rectangleButton.isSelected()) {
                        // Create a rectangle
                        Rectangle rectangle = ShapeFactory.createRectangle(x - 50, y - 30, 100, 60, currentColor);
                        canvas.addShape(rectangle);
                    } else if (polygonButton.isSelected()) {
                        // Create a polygon (triangle)
                        Polygon polygon = ShapeFactory.createDefaultTriangle(x, y);
                        polygon.setFillColor(currentColor);
                        canvas.addShape(polygon);
                    }
                    
                    // Switch back to selection mode
                    selectButton.setSelected(true);
                }
            });
        }
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
     * @param button The button to select
     */
    public void selectButton(Button button) {
        switch (button) {
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
        colorPicker.setColor(color);
        currentColor = color;
    }
}
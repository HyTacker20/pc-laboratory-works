package com.shapeeditor.view;

import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Custom color picker component for selecting colors.
 * Displays a grid of predefined colors for easy selection.
 */
public class ColorPicker extends GridPane {
    /** The currently selected color */
    private Color selectedColor;
    
    /** Callback for color change events */
    private Consumer<Color> onColorChangeCallback;
    
    /** Flag to prevent recursive color change calls */
    private boolean isUpdating = false;
    
    /** Array of predefined colors */
    private static final Color[] PREDEFINED_COLORS = {
        Color.BLACK, Color.DARKGRAY, Color.GRAY, Color.LIGHTGRAY, Color.WHITE,
        Color.RED, Color.DARKRED, Color.ORANGE, Color.YELLOW, Color.GOLD,
        Color.LIME, Color.GREEN, Color.DARKGREEN, Color.CYAN, Color.TEAL,
        Color.BLUE, Color.NAVY, Color.PURPLE, Color.MAGENTA, Color.PINK
    };
    
    /** Label showing the current color name */
    private Label currentColorLabel;
    
    /**
     * Constructs a new ColorPicker with the specified initial color.
     *
     * @param initialColor The initial selected color
     */
    public ColorPicker(Color initialColor) {
        this.selectedColor = initialColor;
        
        // Set up the layout
        setPadding(new Insets(5));
        setHgap(5);
        setVgap(5);
        
        // Create the color buttons
        createColorButtons();
        
        // Create the current color display
        createCurrentColorDisplay();
    }
    
    /**
     * Creates the color buttons for the predefined colors.
     */
    private void createColorButtons() {
        int row = 0;
        int col = 0;
        
        for (Color color : PREDEFINED_COLORS) {
            Button colorButton = createColorButton(color);
            add(colorButton, col, row);
            
            // Move to the next column or row
            col++;
            if (col > 4) {
                col = 0;
                row++;
            }
        }
    }
    
    /**
     * Creates a button for the specified color.
     *
     * @param color The color for the button
     * @return The created button
     */
    private Button createColorButton(Color color) {
        // Create a rectangle with the color
        Rectangle rect = new Rectangle(25, 25, color);
        
        // Create a button with the rectangle
        Button button = new Button();
        button.setGraphic(rect);
        button.setPadding(new Insets(2));
        
        // Add a border to the button if it's the selected color
        if (color.equals(selectedColor)) {
            button.setStyle("-fx-border-color: black; -fx-border-width: 2;");
        }
        
        // Add a tooltip with the color name
        button.setTooltip(new Tooltip(getColorName(color)));
        
        // Add an action handler to the button
        button.setOnAction(event -> {
            setSelectedColor(color);
        });
        
        return button;
    }
    
    /**
     * Sets the selected color and notifies the callback.
     *
     * @param color The new selected color
     */
    private void setSelectedColor(Color color) {
        // Guard against recursive calls
        if (isUpdating) {
            return;
        }
        
        try {
            isUpdating = true;
            
            this.selectedColor = color;
            
            // Update the button styles
            updateButtonStyles();
            
            // Update the current color display
            updateCurrentColorDisplay();
            
            // Notify the callback
            if (onColorChangeCallback != null) {
                onColorChangeCallback.accept(color);
            }
        } finally {
            isUpdating = false;
        }
    }
    
    /**
     * Updates the styles of all buttons to reflect the selected color.
     */
    private void updateButtonStyles() {
        // Clear the border from all buttons
        for (javafx.scene.Node node : getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                Rectangle rect = (Rectangle) button.getGraphic();
                
                if (rect.getFill().equals(selectedColor)) {
                    button.setStyle("-fx-border-color: black; -fx-border-width: 2;");
                } else {
                    button.setStyle("");
                }
            }
        }
    }
    
    /**
     * Sets the callback for color change events.
     *
     * @param callback The callback to be notified when the color changes
     */
    public void setOnColorChange(Consumer<Color> callback) {
        this.onColorChangeCallback = callback;
    }
    
    /**
     * Gets the currently selected color.
     *
     * @return The selected color
     */
    public Color getSelectedColor() {
        return selectedColor;
    }
    
    /**
     * Sets the color of the color picker.
     *
     * @param color The new color
     */
    public void setColor(Color color) {
        setSelectedColor(color);
    }
    
    /**
     * Creates a display for the current color.
     */
    private void createCurrentColorDisplay() {
        // Create a label for the current color
        currentColorLabel = new Label("Current: " + getColorName(selectedColor));
        currentColorLabel.setPadding(new Insets(5, 0, 0, 0));
        
        // Create a rectangle showing the current color
        Rectangle currentColorRect = new Rectangle(100, 25, selectedColor);
        currentColorRect.setStroke(Color.BLACK);
        
        // Add the components to the grid
        add(currentColorLabel, 0, 5, 5, 1);
        add(currentColorRect, 0, 6, 5, 1);
    }
    
    /**
     * Updates the current color display.
     */
    private void updateCurrentColorDisplay() {
        if (currentColorLabel != null) {
            currentColorLabel.setText("Current: " + getColorName(selectedColor));
            
            // Update the rectangle color
            for (javafx.scene.Node node : getChildren()) {
                if (node instanceof Rectangle && GridPane.getRowIndex(node) == 6) {
                    ((Rectangle) node).setFill(selectedColor);
                    break;
                }
            }
        }
    }
    
    /**
     * Gets a human-readable name for a color.
     *
     * @param color The color
     * @return The color name
     */
    private String getColorName(Color color) {
        if (color.equals(Color.BLACK)) return "Black";
        if (color.equals(Color.DARKGRAY)) return "Dark Gray";
        if (color.equals(Color.GRAY)) return "Gray";
        if (color.equals(Color.LIGHTGRAY)) return "Light Gray";
        if (color.equals(Color.WHITE)) return "White";
        if (color.equals(Color.RED)) return "Red";
        if (color.equals(Color.DARKRED)) return "Dark Red";
        if (color.equals(Color.ORANGE)) return "Orange";
        if (color.equals(Color.YELLOW)) return "Yellow";
        if (color.equals(Color.GOLD)) return "Gold";
        if (color.equals(Color.LIME)) return "Lime";
        if (color.equals(Color.GREEN)) return "Green";
        if (color.equals(Color.DARKGREEN)) return "Dark Green";
        if (color.equals(Color.CYAN)) return "Cyan";
        if (color.equals(Color.TEAL)) return "Teal";
        if (color.equals(Color.BLUE)) return "Blue";
        if (color.equals(Color.NAVY)) return "Navy";
        if (color.equals(Color.PURPLE)) return "Purple";
        if (color.equals(Color.MAGENTA)) return "Magenta";
        if (color.equals(Color.PINK)) return "Pink";
        
        // For custom colors, return the RGB values
        return String.format("RGB(%.0f,%.0f,%.0f)",
            color.getRed() * 255,
            color.getGreen() * 255,
            color.getBlue() * 255);
    }
}
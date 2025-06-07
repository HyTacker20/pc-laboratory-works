package com.shapeeditor.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;

/**
 * Custom color picker component for selecting colors.
 * Displays a grid of predefined colors for easy selection.
 */
public class ColorPicker extends GridPane {
    /** The currently selected color */
    private Color selectedColor;
    
    /** Callback for color change events */
    private Consumer<Color> onColorChangeCallback;
    
    /** Array of predefined colors */
    private static final Color[] PREDEFINED_COLORS = {
        Color.BLACK, Color.GRAY, Color.SILVER, Color.WHITE,
        Color.RED, Color.MAROON, Color.YELLOW, Color.OLIVE,
        Color.LIME, Color.GREEN, Color.AQUA, Color.TEAL,
        Color.BLUE, Color.NAVY, Color.FUCHSIA, Color.PURPLE
    };
    
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
            if (col > 3) {
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
        Rectangle rect = new Rectangle(20, 20, color);
        
        // Create a button with the rectangle
        Button button = new Button();
        button.setGraphic(rect);
        button.setPadding(new Insets(2));
        
        // Add a border to the button if it's the selected color
        if (color.equals(selectedColor)) {
            button.setStyle("-fx-border-color: black; -fx-border-width: 2;");
        }
        
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
        this.selectedColor = color;
        
        // Update the button styles
        updateButtonStyles();
        
        // Notify the callback
        if (onColorChangeCallback != null) {
            onColorChangeCallback.accept(color);
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
}
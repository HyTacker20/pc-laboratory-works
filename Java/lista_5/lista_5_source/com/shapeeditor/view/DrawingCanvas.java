package com.shapeeditor.view;

import com.shapeeditor.model.Shape;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Canvas component for rendering and interacting with shapes.
 * Handles drawing shapes and mouse interactions.
 */
public class DrawingCanvas extends Canvas {
    /** List of shapes to be rendered on the canvas */
    private List<Shape> shapes;
    
    /** Currently selected shape */
    private Shape selectedShape;
    
    /** Graphics context for drawing on the canvas */
    private GraphicsContext graphicsContext;
    
    /** Flag indicating if the user is dragging a shape */
    private boolean isDragging;
    
    /** Last mouse X position during drag operation */
    private double lastX;
    
    /** Last mouse Y position during drag operation */
    private double lastY;
    
    /**
     * Constructs a new DrawingCanvas with default size.
     */
    public DrawingCanvas() {
        super(800, 600);
        
        shapes = new ArrayList<>();
        graphicsContext = getGraphicsContext2D();
        
        // Set up the canvas background
        setBackground(Color.WHITE);
        
        // Set up mouse event handlers
        setupMouseHandlers();
    }
    
    /**
     * Sets the background color of the canvas.
     *
     * @param color The background color
     */
    private void setBackground(Color color) {
        graphicsContext.setFill(color);
        graphicsContext.fillRect(0, 0, getWidth(), getHeight());
    }
    
    /**
     * Sets up mouse event handlers for the canvas.
     */
    private void setupMouseHandlers() {
        // Mouse pressed event handler
        setOnMousePressed(this::handleMousePressed);
        
        // Mouse dragged event handler
        setOnMouseDragged(this::handleMouseDragged);
        
        // Mouse released event handler
        setOnMouseReleased(this::handleMouseReleased);
    }
    
    /**
     * Handles mouse pressed events.
     *
     * @param event The mouse event
     */
    private void handleMousePressed(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        
        // Try to select a shape under the mouse cursor
        selectedShape = null;
        for (int i = shapes.size() - 1; i >= 0; i--) {
            Shape shape = shapes.get(i);
            if (shape.contains(x, y)) {
                selectedShape = shape;
                break;
            }
        }
        
        // If a shape is selected, prepare for dragging
        if (selectedShape != null) {
            isDragging = true;
            lastX = x;
            lastY = y;
            
            // Bring the selected shape to the front
            if (shapes.remove(selectedShape)) {
                shapes.add(selectedShape);
            }
            
            // Redraw the canvas
            redraw();
        }
    }
    
    /**
     * Handles mouse dragged events.
     *
     * @param event The mouse event
     */
    private void handleMouseDragged(MouseEvent event) {
        if (isDragging && selectedShape != null) {
            double x = event.getX();
            double y = event.getY();
            
            // Calculate the distance moved
            double deltaX = x - lastX;
            double deltaY = y - lastY;
            
            // Move the selected shape
            selectedShape.move(
                selectedShape.getX() + deltaX,
                selectedShape.getY() + deltaY
            );
            
            // Update the last position
            lastX = x;
            lastY = y;
            
            // Redraw the canvas
            redraw();
        }
    }
    
    /**
     * Handles mouse released events.
     *
     * @param event The mouse event
     */
    private void handleMouseReleased(MouseEvent event) {
        isDragging = false;
    }
    
    /**
     * Adds a shape to the canvas.
     *
     * @param shape The shape to add
     */
    public void addShape(Shape shape) {
        shapes.add(shape);
        redraw();
    }
    
    /**
     * Removes a shape from the canvas.
     *
     * @param shape The shape to remove
     * @return true if the shape was removed, false otherwise
     */
    public boolean removeShape(Shape shape) {
        boolean removed = shapes.remove(shape);
        if (removed) {
            if (selectedShape == shape) {
                selectedShape = null;
            }
            redraw();
        }
        return removed;
    }
    
    /**
     * Clears all shapes from the canvas.
     */
    public void clearShapes() {
        shapes.clear();
        selectedShape = null;
        redraw();
    }
    
    /**
     * Gets the list of shapes on the canvas.
     *
     * @return The list of shapes
     */
    public List<Shape> getShapes() {
        return new ArrayList<>(shapes);
    }
    
    /**
     * Gets the currently selected shape.
     *
     * @return The selected shape, or null if no shape is selected
     */
    public Shape getSelectedShape() {
        return selectedShape;
    }
    
    /**
     * Sets the currently selected shape.
     *
     * @param shape The shape to select, or null to deselect
     */
    public void setSelectedShape(Shape shape) {
        selectedShape = shape;
        redraw();
    }
    
    /**
     * Redraws the canvas with all shapes.
     */
    public void redraw() {
        // Clear the canvas
        setBackground(Color.WHITE);
        
        // Draw all shapes
        for (Shape shape : shapes) {
            shape.draw(graphicsContext);
        }
        
        // Highlight the selected shape
        if (selectedShape != null) {
            highlightSelectedShape();
        }
    }
    
    /**
     * Highlights the selected shape with a border.
     */
    private void highlightSelectedShape() {
        // Save the current state of the graphics context
        graphicsContext.save();
        
        // Draw a selection border around the shape
        // This is a simplified approach; a more sophisticated approach would
        // draw a border that follows the shape's outline
        graphicsContext.setStroke(Color.BLUE);
        graphicsContext.setLineWidth(2);
        
        double x = selectedShape.getX();
        double y = selectedShape.getY();
        
        // Draw a selection rectangle (this is a simplified approach)
        graphicsContext.strokeRect(x - 50, y - 50, 100, 100);
        
        // Restore the graphics context
        graphicsContext.restore();
    }
    
    /**
     * Resizes the canvas to the specified width and height.
     *
     * @param width The new width
     * @param height The new height
     */
    public void resize(double width, double height) {
        setWidth(width);
        setHeight(height);
        redraw();
    }
}
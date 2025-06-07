package com.shapeeditor.controller;

import com.shapeeditor.model.Shape;
import com.shapeeditor.view.DrawingCanvas;
import com.shapeeditor.view.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * Controller for selection operations.
 * Handles shape selection, movement, resizing, and rotation.
 */
public class SelectionController {
    /** The drawing canvas */
    private DrawingCanvas canvas;
    
    /** The toolbar */
    private ToolBar toolBar;
    
    /** Flag indicating if the user is dragging a shape */
    private boolean isDragging;
    
    /** Last mouse X position during drag operation */
    private double lastX;
    
    /** Last mouse Y position during drag operation */
    private double lastY;
    
    /** Resize factor for keyboard resizing */
    private static final double RESIZE_FACTOR = 1.1;
    
    /** Rotation angle for keyboard rotation (in degrees) */
    private static final double ROTATION_ANGLE = 15.0;
    
    /**
     * Constructs a new SelectionController with the specified canvas and toolbar.
     *
     * @param canvas The drawing canvas
     * @param toolBar The toolbar
     */
    public SelectionController(DrawingCanvas canvas, ToolBar toolBar) {
        this.canvas = canvas;
        this.toolBar = toolBar;
        this.isDragging = false;
        
        // Set up the mouse and keyboard event handlers
        setupEventHandlers();
    }
    
    /**
     * Sets up the mouse and keyboard event handlers.
     */
    private void setupEventHandlers() {
        // Set up mouse event handlers
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        
        // Set up keyboard event handlers for the canvas
        canvas.setOnKeyPressed(this::handleKeyPressed);
        
        // Make the canvas focusable to receive key events
        canvas.setFocusTraversable(true);
    }
    
    /**
     * Handles mouse pressed events.
     *
     * @param event The mouse event
     */
    private void handleMousePressed(MouseEvent event) {
        // Only handle events in selection mode
        if (!isSelectionMode()) {
            return;
        }
        
        double x = event.getX();
        double y = event.getY();
        
        // Try to select a shape under the mouse cursor
        Shape selectedShape = null;
        for (Shape shape : canvas.getShapes()) {
            if (shape.contains(x, y)) {
                selectedShape = shape;
                break;
            }
        }
        
        // Update the selected shape
        canvas.setSelectedShape(selectedShape);
        
        // If a shape is selected, prepare for dragging
        if (selectedShape != null) {
            isDragging = true;
            lastX = x;
            lastY = y;
            
            // Request focus for keyboard events
            canvas.requestFocus();
            
            // Update the toolbar button states
            toolBar.updateButtonStates();
        }
    }
    
    /**
     * Handles mouse dragged events.
     *
     * @param event The mouse event
     */
    private void handleMouseDragged(MouseEvent event) {
        // Only handle events in selection mode
        if (!isSelectionMode() || !isDragging) {
            return;
        }
        
        Shape selectedShape = canvas.getSelectedShape();
        if (selectedShape != null) {
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
            canvas.redraw();
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
     * Handles key pressed events.
     *
     * @param event The key event
     */
    private void handleKeyPressed(KeyEvent event) {
        Shape selectedShape = canvas.getSelectedShape();
        if (selectedShape == null) {
            return;
        }
        
        KeyCode keyCode = event.getCode();
        
        // Handle different key presses
        switch (keyCode) {
            case DELETE:
                // Delete the selected shape
                canvas.removeShape(selectedShape);
                toolBar.updateButtonStates();
                break;
                
            case R:
                // Rotate the selected shape
                rotateShape(selectedShape, event.isShiftDown() ? -ROTATION_ANGLE : ROTATION_ANGLE);
                break;
                
            case PLUS:
            case EQUALS:
                // Increase the size of the selected shape
                resizeShape(selectedShape, RESIZE_FACTOR);
                break;
                
            case MINUS:
                // Decrease the size of the selected shape
                resizeShape(selectedShape, 1.0 / RESIZE_FACTOR);
                break;
                
            case UP:
                // Move the selected shape up
                moveShape(selectedShape, 0, -5);
                break;
                
            case DOWN:
                // Move the selected shape down
                moveShape(selectedShape, 0, 5);
                break;
                
            case LEFT:
                // Move the selected shape left
                moveShape(selectedShape, -5, 0);
                break;
                
            case RIGHT:
                // Move the selected shape right
                moveShape(selectedShape, 5, 0);
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Rotates the specified shape by the specified angle.
     *
     * @param shape The shape to rotate
     * @param angleDegrees The angle to rotate by, in degrees
     */
    private void rotateShape(Shape shape, double angleDegrees) {
        shape.rotate(angleDegrees);
        canvas.redraw();
    }
    
    /**
     * Resizes the specified shape by the specified factor.
     *
     * @param shape The shape to resize
     * @param factor The factor to resize by
     */
    private void resizeShape(Shape shape, double factor) {
        shape.resize(factor);
        canvas.redraw();
    }
    
    /**
     * Moves the specified shape by the specified delta.
     *
     * @param shape The shape to move
     * @param deltaX The x-coordinate delta
     * @param deltaY The y-coordinate delta
     */
    private void moveShape(Shape shape, double deltaX, double deltaY) {
        shape.move(shape.getX() + deltaX, shape.getY() + deltaY);
        canvas.redraw();
    }
    
    /**
     * Changes the color of the selected shape.
     *
     * @param color The new color
     */
    public void changeShapeColor(Color color) {
        Shape selectedShape = canvas.getSelectedShape();
        if (selectedShape != null) {
            selectedShape.setFillColor(color);
            canvas.redraw();
        }
    }
    
    /**
     * Checks if the application is in selection mode.
     *
     * @return true if in selection mode, false otherwise
     */
    private boolean isSelectionMode() {
        return toolBar.isSelectionMode();
    }
}
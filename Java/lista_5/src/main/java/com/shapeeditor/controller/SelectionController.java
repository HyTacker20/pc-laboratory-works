package com.shapeeditor.controller;

import java.util.List;

import com.shapeeditor.model.Shape;
import com.shapeeditor.util.EventThrottler;
import com.shapeeditor.util.QuadTree;
import com.shapeeditor.view.ColorPicker;
import com.shapeeditor.view.DrawingCanvas;
import com.shapeeditor.view.DrawingCanvas.ResizeHandle;
import com.shapeeditor.view.ToolBar;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * Controller for selection operations.
 * Handles shape selection, movement, resizing, and rotation.
 *
 * Optimized with:
 * - Spatial partitioning (QuadTree): Efficiently locates shapes at a given point
 * - Event throttling: Limits the frequency of expensive operations during rapid events
 * - Optimized selection logic: Uses spatial index for faster shape selection
 */
public class SelectionController {
    /** The drawing canvas */
    private DrawingCanvas canvas;
    
    /** The toolbar */
    private ToolBar toolBar;
    
    /** The color picker for shape colors */
    private ColorPicker colorPicker;
    
    /** Flag indicating if the user is dragging a shape */
    private boolean isDragging;
    
    /** Flag indicating if the user is resizing a shape */
    private boolean isResizing;
    
    /** The current resize handle being dragged */
    private ResizeHandle currentResizeHandle;
    
    /** Last mouse X position during drag operation */
    private double lastX;
    
    /** Last mouse Y position during drag operation */
    private double lastY;
    
    /** Resize factor for keyboard resizing */
    private static final double RESIZE_FACTOR = 1.1;
    
    /** Rotation angle for keyboard rotation (in degrees) */
    private static final double ROTATION_ANGLE = 15.0;
    
    /**
     * Spatial partitioning structure for efficient shape selection.
     * This QuadTree allows for O(log n) point queries instead of O(n) linear search.
     */
    private QuadTree spatialIndex;
    
    /**
     * Event throttler for drag operations.
     * Limits the frequency of expensive operations during rapid events like dragging.
     */
    private EventThrottler dragThrottler;
    
    /**
     * Throttle interval for drag operations in milliseconds.
     * Set to 16ms to achieve approximately 60 frames per second.
     */
    private static final long DRAG_THROTTLE_INTERVAL = 16; // ~60 FPS
    
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
        this.isResizing = false;
        this.currentResizeHandle = ResizeHandle.NONE;
        
        // Initialize the spatial index with the canvas dimensions
        this.spatialIndex = new QuadTree(
            new QuadTree.Boundary(0, 0, canvas.getWidth(), canvas.getHeight()), 0);
        
        // Initialize the drag throttler
        this.dragThrottler = new EventThrottler(DRAG_THROTTLE_INTERVAL);
        
        // Set up the mouse and keyboard event handlers
        setupEventHandlers();
        
        // Get the color picker from the toolbar
        this.colorPicker = toolBar.getColorPicker();
        
        // Set up color change handler
        if (this.colorPicker != null) {
            this.colorPicker.setOnColorChange(this::changeShapeColor);
        }
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
        // Check if the event is already consumed or not in selection mode
        if (event.isConsumed() || !isSelectionMode()) {
            return;
        }
        
        double x = event.getX();
        double y = event.getY();
        
        // Check if we're clicking on a resize handle of the selected shape
        Shape currentSelectedShape = canvas.getSelectedShape();
        if (currentSelectedShape != null) {
            ResizeHandle handle = canvas.getResizeHandleAt(x, y);
            if (handle != ResizeHandle.NONE) {
                isResizing = true;
                isDragging = false;
                currentResizeHandle = handle;
                lastX = x;
                lastY = y;
                event.consume();
                return;
            }
        }
        
        // Try to select a shape under the mouse cursor using the spatial index
        // First, update the spatial index with the current shapes
        updateSpatialIndex();
        
        // Query the spatial index for shapes at the mouse position
        // This is an O(log n) operation instead of O(n) linear search
        List<Shape> shapesAtPoint = spatialIndex.queryPoint(x, y);
        
        // Find the topmost shape (last in the list of shapes)
        Shape selectedShape = null;
        if (!shapesAtPoint.isEmpty()) {
            // Get all shapes from the canvas
            List<Shape> allShapes = canvas.getShapes();
            
            // Find the topmost shape (the one with the highest index in allShapes)
            int highestIndex = -1;
            for (Shape shape : shapesAtPoint) {
                int index = allShapes.indexOf(shape);
                if (index > highestIndex) {
                    highestIndex = index;
                    selectedShape = shape;
                }
            }
        }
        
        // Update the selected shape
        canvas.setSelectedShape(selectedShape);
        
        // If a shape is selected, prepare for dragging
        if (selectedShape != null) {
            isDragging = true;
            isResizing = false;
            lastX = x;
            lastY = y;
            
            // Request focus for keyboard events
            canvas.requestFocus();
            
            // Update the toolbar button states
            toolBar.updateButtonStates();
            
            // Update the color picker to show the selected shape's color
            updateColorPickerWithSelectedShapeColor();
            
            // Reset the drag throttler
            dragThrottler.reset();
            
            event.consume();
        }
    }
    
    /**
     * Handles mouse dragged events.
     *
     * @param event The mouse event
     */
    private void handleMouseDragged(MouseEvent event) {
        // Check if the event is already consumed or not in selection mode
        if (event.isConsumed() || !isSelectionMode()) {
            return;
        }
        
        double x = event.getX();
        double y = event.getY();
        
        // Handle resizing
        if (isResizing) {
            Shape selectedShape = canvas.getSelectedShape();
            if (selectedShape != null) {
                // Check if we should process this event based on throttling
                if (dragThrottler.shouldProcessEvent()) {
                    resizeShapeWithHandle(selectedShape, currentResizeHandle, x, y);
                    
                    // Update the last position
                    lastX = x;
                    lastY = y;
                    
                    // Redraw the canvas
                    canvas.redraw();
                } else {
                    // Just update the last position without redrawing
                    lastX = x;
                    lastY = y;
                }
                
                event.consume();
            }
            return;
        }
        
        // Handle dragging
        if (isDragging) {
            Shape selectedShape = canvas.getSelectedShape();
            if (selectedShape != null) {
                // Calculate the distance moved
                double deltaX = x - lastX;
                double deltaY = y - lastY;
                
                // Check if we should process this event based on throttling
                if (dragThrottler.shouldProcessEvent()) {
                    // Move the selected shape
                    selectedShape.move(
                        selectedShape.getX() + deltaX,
                        selectedShape.getY() + deltaY
                    );
                    
                    // Update the spatial index
                    updateSpatialIndex();
                    
                    // Redraw the canvas
                    canvas.redraw();
                }
                
                // Always update the last position
                lastX = x;
                lastY = y;
                
                event.consume();
            }
        }
    }
    
    /**
     * Handles mouse released events.
     *
     * @param event The mouse event
     */
    private void handleMouseReleased(MouseEvent event) {
        // Check if the event is already consumed or not in selection mode
        if (event.isConsumed() || !isSelectionMode()) {
            return;
        }
        
        // Only consume the event if we were dragging or resizing
        if (isDragging || isResizing) {
            // Force a final redraw to ensure the shape is in its final position
            if (canvas.getSelectedShape() != null) {
                canvas.redraw();
            }
            
            // Update the spatial index
            updateSpatialIndex();
            
            event.consume();
        }
        
        isDragging = false;
        isResizing = false;
        currentResizeHandle = ResizeHandle.NONE;
        
        // Reset the drag throttler
        dragThrottler.reset();
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
            case BACK_SPACE:
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
        
        // Update the spatial index after rotation
        updateSpatialIndex();
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
        
        // Update the spatial index after resizing
        updateSpatialIndex();
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
        
        // Update the spatial index after moving
        updateSpatialIndex();
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
            
            // Update the toolbar's current color
            toolBar.setColor(color);
        }
    }
    
    /**
     * Updates the color picker to display the current color of the selected shape.
     */
    private void updateColorPickerWithSelectedShapeColor() {
        Shape selectedShape = canvas.getSelectedShape();
        if (selectedShape != null && colorPicker != null) {
            Color shapeColor = selectedShape.getFillColor();
            colorPicker.setColor(shapeColor);
            toolBar.setColor(shapeColor);
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
    
    /**
     * Resizes a shape based on the current resize handle and mouse position.
     * Delegates to the DrawingCanvas's implementation.
     *
     * @param shape The shape to resize
     * @param handle The resize handle being dragged
     * @param mouseX The current mouse x-coordinate
     * @param mouseY The current mouse y-coordinate
     */
    private void resizeShapeWithHandle(Shape shape, ResizeHandle handle, double mouseX, double mouseY) {
        canvas.resizeShapeWithHandle(shape, handle, mouseX, mouseY);
    }
    
    /**
     * Updates the spatial index with the current shapes.
     * This rebuilds the QuadTree with the current shapes for efficient spatial queries.
     */
    private void updateSpatialIndex() {
        // Get all shapes from the canvas
        List<Shape> shapes = canvas.getShapes();
        
        // Update the spatial index
        spatialIndex.update(shapes);
    }
}
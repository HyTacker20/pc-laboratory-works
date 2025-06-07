package com.shapeeditor.controller;

import com.shapeeditor.model.Circle;
import com.shapeeditor.model.Rectangle;
import com.shapeeditor.model.Polygon;
import com.shapeeditor.model.ShapeFactory;
import com.shapeeditor.view.DrawingCanvas;
import com.shapeeditor.view.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * Controller for drawing operations.
 * Manages shape creation based on mouse input.
 */
public class DrawingController {
    /** The drawing canvas */
    private DrawingCanvas canvas;
    
    /** The toolbar */
    private ToolBar toolBar;
    
    /** Current drawing mode */
    private DrawingMode currentMode;
    
    /**
     * Enum representing the available drawing modes.
     */
    public enum DrawingMode {
        /** Selection mode */
        SELECT,
        
        /** Circle drawing mode */
        CIRCLE,
        
        /** Rectangle drawing mode */
        RECTANGLE,
        
        /** Polygon drawing mode */
        POLYGON
    }
    
    /**
     * Constructs a new DrawingController with the specified canvas and toolbar.
     *
     * @param canvas The drawing canvas
     * @param toolBar The toolbar
     */
    public DrawingController(DrawingCanvas canvas, ToolBar toolBar) {
        this.canvas = canvas;
        this.toolBar = toolBar;
        this.currentMode = DrawingMode.SELECT;
        
        // Set up the mouse event handlers
        setupMouseHandlers();
    }
    
    /**
     * Sets up the mouse event handlers for the canvas.
     */
    private void setupMouseHandlers() {
        canvas.setOnMouseClicked(this::handleMouseClicked);
    }
    
    /**
     * Handles mouse clicked events.
     *
     * @param event The mouse event
     */
    private void handleMouseClicked(MouseEvent event) {
        if (currentMode == DrawingMode.SELECT) {
            // Selection is handled by the SelectionController
            return;
        }
        
        // Get the mouse coordinates
        double x = event.getX();
        double y = event.getY();
        
        // Get the current color from the toolbar
        Color color = toolBar.getCurrentColor();
        
        // Create a shape based on the current mode
        switch (currentMode) {
            case CIRCLE:
                createCircle(x, y, color);
                break;
            case RECTANGLE:
                createRectangle(x, y, color);
                break;
            case POLYGON:
                createPolygon(x, y, color);
                break;
            default:
                break;
        }
        
        // Switch back to selection mode
        setDrawingMode(DrawingMode.SELECT);
    }
    
    /**
     * Creates a circle at the specified position.
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param color The fill color
     */
    private void createCircle(double x, double y, Color color) {
        Circle circle = ShapeFactory.createCircle(x, y, 50, color);
        canvas.addShape(circle);
    }
    
    /**
     * Creates a rectangle at the specified position.
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param color The fill color
     */
    private void createRectangle(double x, double y, Color color) {
        Rectangle rectangle = ShapeFactory.createRectangle(x - 50, y - 30, 100, 60, color);
        canvas.addShape(rectangle);
    }
    
    /**
     * Creates a polygon at the specified position.
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param color The fill color
     */
    private void createPolygon(double x, double y, Color color) {
        Polygon polygon = ShapeFactory.createDefaultTriangle(x, y);
        polygon.setFillColor(color);
        canvas.addShape(polygon);
    }
    
    /**
     * Sets the current drawing mode.
     *
     * @param mode The drawing mode
     */
    public void setDrawingMode(DrawingMode mode) {
        this.currentMode = mode;
        
        // Update the toolbar button selection
        switch (mode) {
            case SELECT:
                toolBar.selectButton(ToolBar.Button.SELECT);
                break;
            case CIRCLE:
                toolBar.selectButton(ToolBar.Button.CIRCLE);
                break;
            case RECTANGLE:
                toolBar.selectButton(ToolBar.Button.RECTANGLE);
                break;
            case POLYGON:
                toolBar.selectButton(ToolBar.Button.POLYGON);
                break;
        }
    }
    
    /**
     * Gets the current drawing mode.
     *
     * @return The current drawing mode
     */
    public DrawingMode getCurrentMode() {
        return currentMode;
    }
}
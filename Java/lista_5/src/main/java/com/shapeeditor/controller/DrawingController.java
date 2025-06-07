package com.shapeeditor.controller;

import com.shapeeditor.model.Circle;
import com.shapeeditor.model.Polygon;
import com.shapeeditor.model.Rectangle;
import com.shapeeditor.model.Shape;
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
    
    /** Flag indicating if a drawing operation is in progress */
    private boolean isDrawing;
    
    /** Starting X position for drawing */
    private double startX;
    
    /** Starting Y position for drawing */
    private double startY;
    
    /** Temporary shape being drawn */
    private Shape tempShape;
    
    /** List of points for polygon creation */
    private java.util.List<Double> polygonPointsX;
    
    /** List of points for polygon creation */
    private java.util.List<Double> polygonPointsY;
    
    /** Flag indicating if we're creating a polygon */
    private boolean creatingPolygon;
    
    /** Number of sides for regular polygon creation */
    private int polygonSides = 3;
    
    /** Tolerance for considering a point "close" to another point */
    private static final double POINT_PROXIMITY_THRESHOLD = 10.0;
    
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
        POLYGON,
        
        /** Regular polygon drawing mode */
        REGULAR_POLYGON
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
        
        // Initialize polygon points lists
        this.polygonPointsX = new java.util.ArrayList<>();
        this.polygonPointsY = new java.util.ArrayList<>();
        this.creatingPolygon = false;
        
        // Set up the mouse event handlers
        setupMouseHandlers();
        
        // Set up color change listener
        if (toolBar.getColorPicker() != null) {
            toolBar.getColorPicker().setOnColorChange(color -> {
                // When color changes in the color picker, update the toolbar's current color
                toolBar.setColor(color);
            });
        }
    }
    
    /**
     * Sets up the mouse event handlers for the canvas.
     */
    private void setupMouseHandlers() {
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
    }
    
    /**
     * Handles mouse pressed events to start drawing.
     *
     * @param event The mouse event
     */
    public void handleMousePressed(MouseEvent event) {
        if (currentMode == DrawingMode.SELECT) {
            // Selection is handled by the SelectionController
            return;
        }
        
        // Get the mouse coordinates
        double x = event.getX();
        double y = event.getY();
        
        // Get the current color from the toolbar
        Color color = toolBar.getCurrentColor();
        
        // Handle polygon creation differently
        if (currentMode == DrawingMode.POLYGON) {
            handlePolygonClick(x, y, color);
            event.consume();
            return;
        }
        
        // For regular polygon, we'll create it on mouse press and finalize on mouse release
        if (currentMode == DrawingMode.REGULAR_POLYGON) {
            startX = x;
            startY = y;
            isDrawing = true;
            // Create a temporary regular polygon with initial radius of 0
            tempShape = ShapeFactory.createRegularPolygon(startX, startY, polygonSides, 0, color);
            canvas.addTempShape(tempShape);
            event.consume();
            return;
        }
        
        // For other shapes, use the original behavior
        startX = x;
        startY = y;
        
        // Start drawing operation
        isDrawing = true;
        
        // Create a temporary shape based on the current mode
        switch (currentMode) {
            case CIRCLE:
                tempShape = ShapeFactory.createCircle(startX, startY, 0, color);
                break;
            case RECTANGLE:
                tempShape = ShapeFactory.createRectangle(startX, startY, 0, 0, color);
                break;
            default:
                break;
        }
        
        // Ensure the shape has the current color
        if (tempShape != null) {
            tempShape.setFillColor(color);
        }
        
        // Add the temporary shape to the canvas
        if (tempShape != null) {
            canvas.addTempShape(tempShape);
        }
        
        // Consume the event to prevent it from being handled by the canvas
        event.consume();
    }
    
    /**
     * Handles mouse clicks for polygon creation.
     *
     * @param x The x-coordinate of the click
     * @param y The y-coordinate of the click
     * @param color The current color for the polygon
     */
    private void handlePolygonClick(double x, double y, Color color) {
        // If we're not already creating a polygon, start a new one
        if (!creatingPolygon) {
            // Start a new polygon
            creatingPolygon = true;
            polygonPointsX.clear();
            polygonPointsY.clear();
            
            // Add the first point
            polygonPointsX.add(x);
            polygonPointsY.add(y);
            
            // Create a temporary polygon with just the first point
            tempShape = createPolygonFromPoints(color);
            canvas.addTempShape(tempShape);
            return;
        }
        
        // Check if the user clicked near the first point to complete the polygon
        double firstPointX = polygonPointsX.get(0);
        double firstPointY = polygonPointsY.get(0);
        
        if (polygonPointsX.size() > 2 && isPointNear(x, y, firstPointX, firstPointY)) {
            // Complete the polygon by closing it
            // We don't add the first point again to avoid duplicate points
            
            // Finalize the polygon
            finalizePolygon(color);
            return;
        }
        
        // Check if the new line would intersect with any existing edges
        if (polygonPointsX.size() >= 2) {
            // Convert to arrays for the intersection check
            double[] pointsXArray = polygonPointsX.stream().mapToDouble(Double::doubleValue).toArray();
            double[] pointsYArray = polygonPointsY.stream().mapToDouble(Double::doubleValue).toArray();
            
            // Create a temporary polygon to check for intersections
            // Use the first point as the reference point
            double referenceX = polygonPointsX.get(0);
            double referenceY = polygonPointsY.get(0);
            
            // Calculate points relative to the reference point
            double[] relativePointsX = new double[pointsXArray.length];
            double[] relativePointsY = new double[pointsYArray.length];
            
            for (int i = 0; i < pointsXArray.length; i++) {
                relativePointsX[i] = pointsXArray[i] - referenceX;
                relativePointsY[i] = pointsYArray[i] - referenceY;
            }
            
            Polygon tempPoly = new Polygon(referenceX, referenceY, relativePointsX, relativePointsY, color);
            
            // Get the last point added
            double lastX = polygonPointsX.get(polygonPointsX.size() - 1);
            double lastY = polygonPointsY.get(polygonPointsY.size() - 1);
            
            // Check if the new line would intersect with any existing edges
            if (tempPoly.lineIntersectsWithEdges(lastX, lastY, x, y)) {
                // Show an error message or visual indication
                System.out.println("Error: Line segments cannot intersect!");
                return;
            }
        }
        
        // Add the new point
        polygonPointsX.add(x);
        polygonPointsY.add(y);
        
        // Update the temporary polygon
        if (tempShape != null) {
            canvas.clearTempShape();
        }
        
        tempShape = createPolygonFromPoints(color);
        canvas.addTempShape(tempShape);
    }
    
    /**
     * Creates a polygon from the current list of points.
     *
     * @param color The fill color for the polygon
     * @return A new Polygon instance
     */
    private Polygon createPolygonFromPoints(Color color) {
        int numPoints = polygonPointsX.size();
        
        // Handle special cases for visualization during creation
        if (numPoints == 1) {
            // For a single point, create a small visible marker
            double[] pointsXArray = {-5, 5, 0};
            double[] pointsYArray = {-5, -5, 5};
            return new Polygon(polygonPointsX.get(0), polygonPointsY.get(0), pointsXArray, pointsYArray, color);
        } else if (numPoints == 2) {
            // For two points, create a thin rectangle to represent the line
            double x1 = polygonPointsX.get(0);
            double y1 = polygonPointsY.get(0);
            double x2 = polygonPointsX.get(1);
            double y2 = polygonPointsY.get(1);
            
            // Calculate center point of the line
            double centerX = (x1 + x2) / 2;
            double centerY = (y1 + y2) / 2;
            
            // Calculate line direction and length
            double dx = x2 - x1;
            double dy = y2 - y1;
            double length = Math.sqrt(dx * dx + dy * dy);
            
            // Create a thin rectangle (line representation)
            double[] pointsXArray = {x1 - centerX, x2 - centerX};
            double[] pointsYArray = {y1 - centerY, y2 - centerY};
            
            return new Polygon(centerX, centerY, pointsXArray, pointsYArray, color);
        }
        
        // For 3 or more points, create a proper polygon using the exact points clicked by the user
        // Convert lists to arrays
        double[] pointsXArray = new double[numPoints];
        double[] pointsYArray = new double[numPoints];
        
        // Use the first point as the reference point (instead of calculating a center)
        double referenceX = polygonPointsX.get(0);
        double referenceY = polygonPointsY.get(0);
        
        // Calculate points relative to the reference point
        for (int i = 0; i < numPoints; i++) {
            pointsXArray[i] = polygonPointsX.get(i) - referenceX;
            pointsYArray[i] = polygonPointsY.get(i) - referenceY;
        }
        
        // Create and return the polygon with the reference point as its position
        return new Polygon(referenceX, referenceY, pointsXArray, pointsYArray, color);
    }
    
    /**
     * Finalizes the polygon creation by adding it to the canvas.
     *
     * @param color The fill color for the polygon
     */
    private void finalizePolygon(Color color) {
        if (polygonPointsX.size() < 3) {
            // Need at least 3 points to create a valid polygon
            System.out.println("Error: A polygon must have at least 3 points!");
            return;
        }
        
        // Create the final polygon using the exact points clicked by the user
        // Convert lists to arrays
        int numPoints = polygonPointsX.size();
        double[] pointsXArray = new double[numPoints];
        double[] pointsYArray = new double[numPoints];
        
        // Use the first point as the reference point
        double referenceX = polygonPointsX.get(0);
        double referenceY = polygonPointsY.get(0);
        
        // Calculate points relative to the reference point
        for (int i = 0; i < numPoints; i++) {
            pointsXArray[i] = polygonPointsX.get(i) - referenceX;
            pointsYArray[i] = polygonPointsY.get(i) - referenceY;
        }
        
        // Create the final polygon
        Polygon finalPolygon = new Polygon(referenceX, referenceY, pointsXArray, pointsYArray, color);
        
        // Add it to the canvas
        canvas.clearTempShape();
        canvas.addShape(finalPolygon);
        
        // Reset polygon creation state
        creatingPolygon = false;
        polygonPointsX.clear();
        polygonPointsY.clear();
        tempShape = null;
    }
    
    /**
     * Checks if a point is near another point within the proximity threshold.
     *
     * @param x1 The x-coordinate of the first point
     * @param y1 The y-coordinate of the first point
     * @param x2 The x-coordinate of the second point
     * @param y2 The y-coordinate of the second point
     * @return true if the points are within the proximity threshold
     */
    private boolean isPointNear(double x1, double y1, double x2, double y2) {
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        return distance <= POINT_PROXIMITY_THRESHOLD;
    }
    
    /**
     * Handles mouse dragged events to update the shape being drawn.
     *
     * @param event The mouse event
     */
    public void handleMouseDragged(MouseEvent event) {
        // Get the current mouse coordinates
        double currentX = event.getX();
        double currentY = event.getY();
        
        if (currentMode == DrawingMode.POLYGON && creatingPolygon) {
            // For polygon creation, show a preview line from the last point to the current mouse position
            if (polygonPointsX.size() > 0) {
                // Get the last point
                double lastX = polygonPointsX.get(polygonPointsX.size() - 1);
                double lastY = polygonPointsY.get(polygonPointsY.size() - 1);
                
                // Create a temporary polygon that includes all current points plus a preview point
                java.util.List<Double> previewX = new java.util.ArrayList<>(polygonPointsX);
                java.util.List<Double> previewY = new java.util.ArrayList<>(polygonPointsY);
                previewX.add(currentX);
                previewY.add(currentY);
                
                // Convert to arrays
                double[] pointsXArray = new double[previewX.size()];
                double[] pointsYArray = new double[previewY.size()];
                
                // Use the first point as the reference point
                double referenceX = previewX.get(0);
                double referenceY = previewY.get(0);
                
                // Calculate points relative to the reference point
                for (int i = 0; i < previewX.size(); i++) {
                    pointsXArray[i] = previewX.get(i) - referenceX;
                    pointsYArray[i] = previewY.get(i) - referenceY;
                }
                
                // Create and show the preview polygon
                Color color = toolBar.getCurrentColor();
                Polygon previewPolygon = new Polygon(referenceX, referenceY, pointsXArray, pointsYArray, color);
                canvas.clearTempShape();
                canvas.addTempShape(previewPolygon);
            }
            
            event.consume();
            return;
        }
        
        if (!isDrawing || tempShape == null) {
            return;
        }
        
        // Calculate width and height based on drag distance
        double width = Math.abs(currentX - startX);
        double height = Math.abs(currentY - startY);
        
        // Update the temporary shape based on the current mode
        switch (currentMode) {
            case CIRCLE:
                // For circle, use the distance from start point to current point as the radius
                double radius = Math.sqrt(Math.pow(currentX - startX, 2) + Math.pow(currentY - startY, 2));
                ((Circle) tempShape).setRadius(radius);
                break;
            case RECTANGLE:
                // For rectangle, update the position and dimensions
                double rectX = Math.min(startX, currentX);
                double rectY = Math.min(startY, currentY);
                ((Rectangle) tempShape).setPosition(rectX, rectY);
                ((Rectangle) tempShape).setDimensions(width, height);
                break;
            case REGULAR_POLYGON:
                // For regular polygon, use the distance as the radius
                double polygonRadius = Math.sqrt(Math.pow(currentX - startX, 2) + Math.pow(currentY - startY, 2));
                // Replace the temporary shape with a new one with the updated radius
                Color color = toolBar.getCurrentColor();
                canvas.clearTempShape();
                tempShape = ShapeFactory.createRegularPolygon(startX, startY, polygonSides, polygonRadius, color);
                canvas.addTempShape(tempShape);
                break;
            default:
                break;
        }
        
        // Redraw the canvas to show the updated shape
        canvas.redraw();
        
        // Consume the event to prevent it from being handled by the canvas
        event.consume();
    }
    
    /**
     * Handles mouse released events to finalize the shape.
     *
     * @param event The mouse event
     */
    public void handleMouseReleased(MouseEvent event) {
        if (currentMode == DrawingMode.POLYGON && creatingPolygon) {
            // For polygon creation, we don't use mouse release
            return;
        }
        
        if (!isDrawing || tempShape == null) {
            return;
        }
        
        // Get the current mouse coordinates
        double currentX = event.getX();
        double currentY = event.getY();
        
        // Check if the shape has a minimum size
        boolean validShape = false;
        
        switch (currentMode) {
            case CIRCLE:
                double radius = ((Circle) tempShape).getRadius();
                validShape = radius > 2; // Minimum radius of 2 pixels
                break;
            case RECTANGLE:
                double width = ((Rectangle) tempShape).getWidth();
                double height = ((Rectangle) tempShape).getHeight();
                validShape = width > 2 && height > 2; // Minimum size of 2x2 pixels
                break;
            case REGULAR_POLYGON:
                // For regular polygon, we consider it valid if it has a minimum distance from center
                double distance = Math.sqrt(Math.pow(currentX - startX, 2) + Math.pow(currentY - startY, 2));
                validShape = distance > 2; // Minimum radius of 2 pixels
                break;
            default:
                break;
        }
        
        // Only add the shape if it's valid
        if (validShape) {
            // Finalize the shape
            canvas.addShape(tempShape);
        }
        
        canvas.clearTempShape();
        
        // Reset drawing state
        isDrawing = false;
        tempShape = null;
        
        // Note: We no longer switch back to selection mode here
        // to allow users to create multiple shapes of the same type
        
        // Consume the event to prevent it from being handled by the canvas
        event.consume();
    }
    
    /**
     * Creates a circle at the specified position.
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param color The fill color
     */
    /**
     * Creates a circle with the specified parameters.
     *
     * @param centerX The x-coordinate of the circle's center
     * @param centerY The y-coordinate of the circle's center
     * @param radius The radius of the circle
     * @param color The fill color
     * @return The created circle
     */
    private Circle createCircle(double centerX, double centerY, double radius, Color color) {
        Circle circle = ShapeFactory.createCircle(centerX, centerY, radius, color);
        return circle;
    }
    
    /**
     * Creates a rectangle with the specified parameters.
     *
     * @param x The x-coordinate of the rectangle's top-left corner
     * @param y The y-coordinate of the rectangle's top-left corner
     * @param width The width of the rectangle
     * @param height The height of the rectangle
     * @param color The fill color
     * @return The created rectangle
     */
    private Rectangle createRectangle(double x, double y, double width, double height, Color color) {
        Rectangle rectangle = ShapeFactory.createRectangle(x, y, width, height, color);
        return rectangle;
    }
    
    /**
     * Creates a polygon at the specified position.
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param color The fill color
     * @return The created polygon
     */
    private Polygon createPolygon(double x, double y, Color color) {
        // Instead of creating a default triangle, we'll create a polygon from the points
        // that have been collected during the polygon creation process
        if (polygonPointsX.size() > 0) {
            return createPolygonFromPoints(color);
        } else {
            // Start a new polygon creation process
            polygonPointsX.add(x);
            polygonPointsY.add(y);
            creatingPolygon = true;
            
            // Create a temporary marker for the first point
            double[] pointsXArray = {-5, 5, 0};
            double[] pointsYArray = {-5, -5, 5};
            return new Polygon(x, y, pointsXArray, pointsYArray, color);
        }
    }
    
    /**
     * Sets the number of sides for regular polygon creation.
     *
     * @param sides The number of sides
     * @throws IllegalArgumentException if sides is less than 3
     */
    public void setPolygonSides(int sides) {
        if (sides < 3) {
            throw new IllegalArgumentException("A polygon must have at least 3 sides");
        }
        this.polygonSides = sides;
    }
    
    /**
     * Gets the current number of sides for regular polygon creation.
     *
     * @return The number of sides
     */
    public int getPolygonSides() {
        return polygonSides;
    }
    
    /**
     * Sets the current drawing mode.
     *
     * @param mode The drawing mode
     */
    public void setDrawingMode(DrawingMode mode) {
        // If we're switching away from polygon mode while creating a polygon,
        // cancel the polygon creation
        if (currentMode == DrawingMode.POLYGON && creatingPolygon && mode != DrawingMode.POLYGON) {
            creatingPolygon = false;
            polygonPointsX.clear();
            polygonPointsY.clear();
            if (tempShape != null) {
                canvas.clearTempShape();
                tempShape = null;
            }
        }
        
        this.currentMode = mode;
        
        // Update the toolbar button selection
        switch (mode) {
            case SELECT:
                toolBar.selectButton(ToolBar.ToolbarButtonType.SELECT);
                break;
            case CIRCLE:
                toolBar.selectButton(ToolBar.ToolbarButtonType.CIRCLE);
                break;
            case RECTANGLE:
                toolBar.selectButton(ToolBar.ToolbarButtonType.RECTANGLE);
                break;
            case POLYGON:
                toolBar.selectButton(ToolBar.ToolbarButtonType.POLYGON);
                break;
            case REGULAR_POLYGON:
                toolBar.selectButton(ToolBar.ToolbarButtonType.POLYGON); // Use the same button for now
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
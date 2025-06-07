package com.shapeeditor.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.shapeeditor.model.Circle;
import com.shapeeditor.model.Polygon;
import com.shapeeditor.model.Rectangle;
import com.shapeeditor.model.Shape;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * Canvas component for rendering and interacting with shapes.
 * Handles drawing shapes and mouse interactions.
 *
 * Optimized with:
 * - Dirty region tracking: Only redraws parts of the canvas that have changed
 * - Bounding box caching: Avoids recalculating shape boundaries
 * - Trigonometric calculation caching: Caches sin/cos values for rotation operations
 */
public class DrawingCanvas extends Canvas {
    /** List of shapes to be rendered on the canvas */
    private List<Shape> shapes;
    
    /** Currently selected shape */
    private Shape selectedShape;
    
    /** Temporary shape being drawn */
    private Shape tempShape;
    
    /** Graphics context for drawing on the canvas */
    private GraphicsContext graphicsContext;
    
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
    
    /**
     * Dirty region tracking for redraw optimization.
     * These variables define the rectangular region that needs to be redrawn.
     */
    private double dirtyLeft, dirtyTop, dirtyRight, dirtyBottom;
    
    /** Flag indicating if there's a dirty region to redraw */
    private boolean hasDirtyRegion = false;
    
    /**
     * Cache for shape bounding boxes.
     * Stores the bounding box of each shape to avoid recalculating it.
     * The bounding box is stored as [left, top, right, bottom].
     */
    private final Map<Shape, double[]> boundingBoxCache = new HashMap<>();
    
    /**
     * Cache for trigonometric calculations.
     * Stores the sine and cosine values for the last rotation angle.
     * This avoids expensive trigonometric calculations when the rotation angle doesn't change.
     */
    private double cachedRotation = -1;
    private double cachedSin = 0;
    private double cachedCos = 1;
    
    /** Reference to the drawing controller */
    private com.shapeeditor.controller.DrawingController drawingController;
    
    /** Enum representing the different resize handles */
    public enum ResizeHandle {
        TOP_LEFT, TOP_MIDDLE, TOP_RIGHT,
        MIDDLE_LEFT, MIDDLE_RIGHT,
        BOTTOM_LEFT, BOTTOM_MIDDLE, BOTTOM_RIGHT,
        ROTATION,
        NONE
    }
    
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
        setOnMousePressed(event -> {
            // First let the DrawingController handle the event if it exists
            if (drawingController != null) {
                drawingController.handleMousePressed(event);
            }
            
            // If the event wasn't consumed, handle it ourselves
            if (!event.isConsumed()) {
                handleMousePressed(event);
            }
        });
        
        // Mouse dragged event handler
        setOnMouseDragged(event -> {
            // First let the DrawingController handle the event if it exists
            if (drawingController != null) {
                drawingController.handleMouseDragged(event);
            }
            
            // If the event wasn't consumed, handle it ourselves
            if (!event.isConsumed()) {
                handleMouseDragged(event);
            }
        });
        
        // Mouse released event handler
        setOnMouseReleased(event -> {
            // First let the DrawingController handle the event if it exists
            if (drawingController != null) {
                drawingController.handleMouseReleased(event);
            }
            
            // If the event wasn't consumed, handle it ourselves
            if (!event.isConsumed()) {
                handleMouseReleased(event);
            }
        });
    }
    
    /**
     * Handles mouse pressed events.
     *
     * @param event The mouse event
     */
    private void handleMousePressed(MouseEvent event) {
        // No need to check if the event is consumed here anymore
        // since we already check in the event handler
        
        double x = event.getX();
        double y = event.getY();
        
        // Check if we're clicking on a resize handle of the selected shape
        if (selectedShape != null) {
            ResizeHandle handle = getResizeHandleAt(x, y);
            if (handle != ResizeHandle.NONE) {
                isResizing = true;
                currentResizeHandle = handle;
                lastX = x;
                lastY = y;
                event.consume();
                return;
            }
        }
        
        // If not on a resize handle, try to select a shape under the mouse cursor
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
            isResizing = false;
            lastX = x;
            lastY = y;
            
            // Bring the selected shape to the front
            if (shapes.remove(selectedShape)) {
                shapes.add(selectedShape);
            }
            
            // Redraw the canvas
            redraw();
            event.consume();
        }
    }
    
    /**
     * Handles mouse dragged events.
     *
     * @param event The mouse event
     */
    private void handleMouseDragged(MouseEvent event) {
        // No need to check if the event is consumed here anymore
        // since we already check in the event handler
        
        double x = event.getX();
        double y = event.getY();
        
        // Handle resizing
        if (isResizing && selectedShape != null) {
            // Mark the current shape area as dirty before resizing
            markShapeDirty(selectedShape);
            
            // Add extra margin for selection handles
            double[] bbox = getShapeBoundingBox(selectedShape);
            double margin = 70; // Large enough for rotation handle
            markDirty(bbox[0] - margin, bbox[1] - margin, bbox[2] + margin, bbox[3] + margin);
            
            resizeShapeWithHandle(selectedShape, currentResizeHandle, x, y);
            
            // Mark the new shape area as dirty after resizing
            markShapeDirty(selectedShape);
            
            // Add extra margin for selection handles
            bbox = getShapeBoundingBox(selectedShape);
            markDirty(bbox[0] - margin, bbox[1] - margin, bbox[2] + margin, bbox[3] + margin);
            
            // Update the last position
            lastX = x;
            lastY = y;
            
            // Redraw the canvas
            redraw();
            event.consume();
            return;
        }
        
        // Handle dragging
        if (isDragging && selectedShape != null) {
            // Mark the current shape area as dirty before moving
            markShapeDirty(selectedShape);
            
            // Add extra margin for selection handles
            double[] bbox = getShapeBoundingBox(selectedShape);
            double margin = 70; // Large enough for rotation handle
            markDirty(bbox[0] - margin, bbox[1] - margin, bbox[2] + margin, bbox[3] + margin);
            
            // Calculate the distance moved
            double deltaX = x - lastX;
            double deltaY = y - lastY;
            
            // Move the selected shape
            selectedShape.move(
                selectedShape.getX() + deltaX,
                selectedShape.getY() + deltaY
            );
            
            // Mark the new shape area as dirty after moving
            markShapeDirty(selectedShape);
            
            // Add extra margin for selection handles
            bbox = getShapeBoundingBox(selectedShape);
            markDirty(bbox[0] - margin, bbox[1] - margin, bbox[2] + margin, bbox[3] + margin);
            
            // Update the last position
            lastX = x;
            lastY = y;
            
            // Redraw the canvas
            redraw();
            event.consume();
        }
    }
    
    /**
     * Handles mouse released events.
     *
     * @param event The mouse event
     */
    private void handleMouseReleased(MouseEvent event) {
        // No need to check if the event is consumed here anymore
        // since we already check in the event handler
        
        // Only consume the event if we were dragging or resizing
        if (isDragging || isResizing) {
            event.consume();
        }
        
        isDragging = false;
        isResizing = false;
        currentResizeHandle = ResizeHandle.NONE;
    }
    
    /**
     * Adds a shape to the canvas.
     *
     * @param shape The shape to add
     */
    public void addShape(Shape shape) {
        shapes.add(shape);
        markShapeDirty(shape);
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
            markShapeDirty(shape);
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
        // Mark the entire canvas as dirty
        markDirty(0, 0, getWidth(), getHeight());
        shapes.clear();
        selectedShape = null;
        boundingBoxCache.clear();
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
        // Mark the old selected shape's region as dirty
        if (selectedShape != null) {
            // Add extra margin for selection handles
            double[] bbox = getShapeBoundingBox(selectedShape);
            double margin = 70; // Large enough for rotation handle
            markDirty(bbox[0] - margin, bbox[1] - margin, bbox[2] + margin, bbox[3] + margin);
        }
        
        selectedShape = shape;
        
        // Mark the new selected shape's region as dirty
        if (selectedShape != null) {
            // Add extra margin for selection handles
            double[] bbox = getShapeBoundingBox(selectedShape);
            double margin = 70; // Large enough for rotation handle
            markDirty(bbox[0] - margin, bbox[1] - margin, bbox[2] + margin, bbox[3] + margin);
        }
        
        redraw();
    }
    
    /**
     * Adds a temporary shape to the canvas for preview during drawing.
     *
     * @param shape The temporary shape to add
     */
    public void addTempShape(Shape shape) {
        if (this.tempShape != null) {
            markShapeDirty(this.tempShape);
        }
        this.tempShape = shape;
        markShapeDirty(shape);
        redraw();
    }
    
    /**
     * Clears the temporary shape from the canvas.
     */
    public void clearTempShape() {
        if (this.tempShape != null) {
            markShapeDirty(this.tempShape);
        }
        this.tempShape = null;
        redraw();
    }
    
    /**
     * Marks a region as dirty, requiring redraw.
     * This is a key optimization that allows the canvas to only redraw
     * the parts that have changed, rather than the entire canvas.
     *
     * @param left The left coordinate of the dirty region
     * @param top The top coordinate of the dirty region
     * @param right The right coordinate of the dirty region
     * @param bottom The bottom coordinate of the dirty region
     */
    private void markDirty(double left, double top, double right, double bottom) {
        // Add a small margin to ensure complete coverage
        double margin = 5.0;
        left -= margin;
        top -= margin;
        right += margin;
        bottom += margin;
        
        // Ensure the dirty region is within the canvas bounds
        left = Math.max(0, left);
        top = Math.max(0, top);
        right = Math.min(getWidth(), right);
        bottom = Math.min(getHeight(), bottom);
        
        if (right <= left || bottom <= top) {
            return; // Invalid region
        }
        
        if (!hasDirtyRegion) {
            // First dirty region
            dirtyLeft = left;
            dirtyTop = top;
            dirtyRight = right;
            dirtyBottom = bottom;
            hasDirtyRegion = true;
        } else {
            // Expand existing dirty region
            dirtyLeft = Math.min(dirtyLeft, left);
            dirtyTop = Math.min(dirtyTop, top);
            dirtyRight = Math.max(dirtyRight, right);
            dirtyBottom = Math.max(dirtyBottom, bottom);
        }
    }
    
    /**
     * Marks a shape's region as dirty, requiring redraw.
     * This method also invalidates the bounding box cache for the shape.
     *
     * @param shape The shape whose region is dirty
     */
    private void markShapeDirty(Shape shape) {
        if (shape == null) return;
        
        double[] bbox = getShapeBoundingBox(shape);
        markDirty(bbox[0], bbox[1], bbox[2], bbox[3]);
        
        // Invalidate the bounding box cache for this shape
        boundingBoxCache.remove(shape);
    }
    
    /**
     * Redraws the canvas with all shapes.
     * Optimized to only redraw dirty regions, which significantly improves performance
     * when only a small part of the canvas has changed.
     */
    public void redraw() {
        if (!hasDirtyRegion) {
            // If no dirty region, mark the entire canvas as dirty
            markDirty(0, 0, getWidth(), getHeight());
        }
        
        // Clear only the dirty region
        graphicsContext.clearRect(dirtyLeft, dirtyTop, dirtyRight - dirtyLeft, dirtyBottom - dirtyTop);
        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillRect(dirtyLeft, dirtyTop, dirtyRight - dirtyLeft, dirtyBottom - dirtyTop);
        
        // Set clipping to only draw within the dirty region
        graphicsContext.save();
        graphicsContext.beginPath();
        graphicsContext.rect(dirtyLeft, dirtyTop, dirtyRight - dirtyLeft, dirtyBottom - dirtyTop);
        graphicsContext.clip();
        
        // Draw all shapes that intersect with the dirty region
        for (Shape shape : shapes) {
            double[] bbox = getShapeBoundingBox(shape);
            if (bbox[0] <= dirtyRight && bbox[2] >= dirtyLeft &&
                bbox[1] <= dirtyBottom && bbox[3] >= dirtyTop) {
                shape.draw(graphicsContext);
            }
        }
        
        // Draw the temporary shape if it exists and intersects with the dirty region
        if (tempShape != null) {
            double[] bbox = getShapeBoundingBox(tempShape);
            if (bbox[0] <= dirtyRight && bbox[2] >= dirtyLeft &&
                bbox[1] <= dirtyBottom && bbox[3] >= dirtyTop) {
                tempShape.draw(graphicsContext);
            }
        }
        
        // Highlight the selected shape if it intersects with the dirty region
        if (selectedShape != null) {
            double[] bbox = getShapeBoundingBox(selectedShape);
            if (bbox[0] <= dirtyRight && bbox[2] >= dirtyLeft &&
                bbox[1] <= dirtyBottom && bbox[3] >= dirtyTop) {
                highlightSelectedShape();
            }
        }
        
        // Restore the graphics context
        graphicsContext.restore();
        
        // Reset the dirty region
        hasDirtyRegion = false;
    }
    
    /**
     * Highlights the selected shape with a border and resize handles,
     * accounting for rotation.
     */
    private void highlightSelectedShape() {
        // Save the current state of the graphics context
        graphicsContext.save();
        
        double rotation = selectedShape.getRotation();
        double width, height, centerX, centerY;
        
        // Get the actual dimensions of the shape based on its type
        if (selectedShape instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) selectedShape;
            width = rectangle.getWidth();
            height = rectangle.getHeight();
            centerX = rectangle.getX() + width / 2;
            centerY = rectangle.getY() + height / 2;
        } else if (selectedShape instanceof Circle) {
            Circle circle = (Circle) selectedShape;
            double radius = circle.getRadius();
            width = radius * 2;
            height = radius * 2;
            centerX = circle.getX();
            centerY = circle.getY();
        } else if (selectedShape instanceof Polygon) {
            Polygon polygon = (Polygon) selectedShape;
            
            // For polygons, we need to calculate the dimensions from the points
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
            
            double[] pointsX = polygon.getPointsX();
            double[] pointsY = polygon.getPointsY();
            int numPoints = polygon.getNumPoints();
            
            // Find the bounding box
            for (int i = 0; i < numPoints; i++) {
                minX = Math.min(minX, pointsX[i]);
                minY = Math.min(minY, pointsY[i]);
                maxX = Math.max(maxX, pointsX[i]);
                maxY = Math.max(maxY, pointsY[i]);
            }
            
            width = maxX - minX;
            height = maxY - minY;
            
            // Use the polygon's centroid method for more accurate center calculation
            double[] centroid = polygon.getCentroid();
            centerX = polygon.getX() + centroid[0];
            centerY = polygon.getY() + centroid[1];
        } else {
            // Fallback to bounding box for unknown shape types
            double[] boundingBox = getShapeBoundingBox(selectedShape);
            double left = boundingBox[0];
            double top = boundingBox[1];
            double right = boundingBox[2];
            double bottom = boundingBox[3];
            width = right - left;
            height = bottom - top;
            centerX = left + width / 2;
            centerY = top + height / 2;
        }
        
        // Draw a selection border that follows the shape's rotation
        graphicsContext.setStroke(Color.BLUE);
        graphicsContext.setLineWidth(2);
        graphicsContext.setLineDashes(5, 5); // Dashed line for selection box
        
        if (rotation == 0) {
            // For non-rotated shapes, draw a simple rectangle
            double left = centerX - width / 2;
            double top = centerY - height / 2;
            graphicsContext.strokeRect(left, top, width, height);
        } else {
            // For rotated shapes, draw a rotated rectangle
            graphicsContext.save();
            
            // Translate to the center of the shape
            graphicsContext.translate(centerX, centerY);
            
            // Rotate the context
            graphicsContext.rotate(rotation);
            
            // Draw the rectangle centered at the origin
            graphicsContext.strokeRect(-width/2, -height/2, width, height);
            
            // Restore the context
            graphicsContext.restore();
        }
        
        graphicsContext.setLineDashes(null); // Reset to solid line
        
        // Draw resize handles using the same dimensions
        double left = centerX - width / 2;
        double top = centerY - height / 2;
        double right = centerX + width / 2;
        double bottom = centerY + height / 2;
        drawResizeHandles(left, top, right, bottom);
        
        // Restore the graphics context
        graphicsContext.restore();
    }
    
    /**
     * Draws resize handles at the corners and midpoints of the selected shape,
     * accounting for rotation.
     *
     * @param left The left coordinate of the selection rectangle
     * @param top The top coordinate of the selection rectangle
     * @param right The right coordinate of the selection rectangle
     * @param bottom The bottom coordinate of the selection rectangle
     */
    private void drawResizeHandles(double left, double top, double right, double bottom) {
        double handleSize = 8;
        graphicsContext.setFill(Color.WHITE);
        graphicsContext.setStroke(Color.BLUE);
        graphicsContext.setLineWidth(1);
        
        // Calculate center of the shape
        double centerX = left + (right - left) / 2;
        double centerY = top + (bottom - top) / 2;
        
        // Get the rotation of the selected shape
        double rotation = selectedShape != null ? selectedShape.getRotation() : 0;
        
        // Calculate the four corners and midpoints of the rectangle
        double width = right - left;
        double height = bottom - top;
        
        // Use cached trigonometric values if available, or update the cache
        updateTrigCache(rotation);
        double cos = cachedCos;
        double sin = cachedSin;
        
        // Calculate the four corners of the rotated rectangle
        double[] corners = new double[8];
        
        // Top-left corner (relative to center)
        corners[0] = -width/2;
        corners[1] = -height/2;
        
        // Top-right corner (relative to center)
        corners[2] = width/2;
        corners[3] = -height/2;
        
        // Bottom-right corner (relative to center)
        corners[4] = width/2;
        corners[5] = height/2;
        
        // Bottom-left corner (relative to center)
        corners[6] = -width/2;
        corners[7] = height/2;
        
        // Rotate each corner and calculate its absolute position
        double[] rotatedCorners = new double[8];
        for (int i = 0; i < 8; i += 2) {
            double x = corners[i];
            double y = corners[i + 1];
            
            // Apply rotation using cached sin/cos values
            rotatedCorners[i] = centerX + (x * cos - y * sin);
            rotatedCorners[i + 1] = centerY + (x * sin + y * cos);
        }
        
        // Draw corner handles at the rotated positions
        drawHandle(rotatedCorners[0], rotatedCorners[1], handleSize);  // Top-left
        drawHandle(rotatedCorners[2], rotatedCorners[3], handleSize);  // Top-right
        drawHandle(rotatedCorners[4], rotatedCorners[5], handleSize);  // Bottom-right
        drawHandle(rotatedCorners[6], rotatedCorners[7], handleSize);  // Bottom-left
        
        // Calculate and draw middle handles using cached sin/cos values
        // Top middle
        double topMidX = centerX + (0 * cos - (-height/2) * sin);
        double topMidY = centerY + (0 * sin + (-height/2) * cos);
        drawHandle(topMidX, topMidY, handleSize);
        
        // Right middle
        double rightMidX = centerX + ((width/2) * cos - 0 * sin);
        double rightMidY = centerY + ((width/2) * sin + 0 * cos);
        drawHandle(rightMidX, rightMidY, handleSize);
        
        // Bottom middle
        double bottomMidX = centerX + (0 * cos - (height/2) * sin);
        double bottomMidY = centerY + (0 * sin + (height/2) * cos);
        drawHandle(bottomMidX, bottomMidY, handleSize);
        
        // Left middle
        double leftMidX = centerX + ((-width/2) * cos - 0 * sin);
        double leftMidY = centerY + ((-width/2) * sin + 0 * cos);
        drawHandle(leftMidX, leftMidY, handleSize);
        
        // Calculate rotation handle position
        // Position the rotation handle at the top edge of the shape based on the shape's current rotation
        double rotationDistance = height / 2 + 20; // Distance from center to rotation handle (top edge + margin)
        
        // Calculate the position of the rotation handle based on the shape's rotation
        // This ensures it follows the top edge of the shape as it rotates
        double handleRadians = Math.toRadians(rotation);
        double rotationHandleX = centerX + rotationDistance * Math.sin(handleRadians);
        double rotationHandleY = centerY - rotationDistance * Math.cos(handleRadians);
        
        // Draw rotation handle as a circle with a line pointing upward
        double rotationHandleSize = handleSize * 1.5; // Make it larger than regular handles
        
        // Draw the connecting line first
        graphicsContext.setStroke(Color.BLUE);
        graphicsContext.setLineDashes(3, 3); // Dashed line
        graphicsContext.beginPath();
        graphicsContext.moveTo(rotationHandleX, rotationHandleY);
        graphicsContext.lineTo(centerX, centerY);
        graphicsContext.stroke();
        graphicsContext.setLineDashes(null); // Reset to solid line
        
        // Draw the circular handle
        graphicsContext.setFill(Color.CYAN);
        graphicsContext.fillOval(rotationHandleX - rotationHandleSize/2, rotationHandleY - rotationHandleSize/2,
                               rotationHandleSize, rotationHandleSize);
        graphicsContext.setStroke(Color.BLUE);
        graphicsContext.strokeOval(rotationHandleX - rotationHandleSize/2, rotationHandleY - rotationHandleSize/2,
                                 rotationHandleSize, rotationHandleSize);
        
        // Draw a line inside the circle pointing upward (rotation indicator)
        // The indicator maintains a consistent upward orientation regardless of shape rotation
        double indicatorLength = rotationHandleSize * 0.7;
        
        // Always point upward/outward from the center of the shape
        double indicatorEndX = rotationHandleX;
        double indicatorEndY = rotationHandleY - indicatorLength/2;
        
        graphicsContext.setStroke(Color.DARKBLUE);
        graphicsContext.setLineWidth(2);
        graphicsContext.beginPath();
        graphicsContext.moveTo(rotationHandleX, rotationHandleY);
        graphicsContext.lineTo(indicatorEndX, indicatorEndY);
        graphicsContext.stroke();
        graphicsContext.setLineWidth(1);
    }
    
    /**
     * Draws a single resize handle.
     *
     * @param x The x-coordinate of the handle center
     * @param y The y-coordinate of the handle center
     * @param size The size of the handle
     */
    private void drawHandle(double x, double y, double size) {
        double halfSize = size / 2;
        graphicsContext.fillRect(x - halfSize, y - halfSize, size, size);
        graphicsContext.strokeRect(x - halfSize, y - halfSize, size, size);
    }
    
    /**
     * Gets the bounding box of a shape.
     *
     * @param shape The shape
     * @return An array containing [left, top, right, bottom] coordinates
     */
    public double[] getShapeBoundingBox(Shape shape) {
        // Check if the bounding box is already cached
        double[] cachedBox = boundingBoxCache.get(shape);
        if (cachedBox != null) {
            return cachedBox;
        }
        
        double x = shape.getX();
        double y = shape.getY();
        double rotation = shape.getRotation();
        double left = x;
        double top = y;
        double right = x;
        double bottom = y;
        
        // For non-rotated shapes, use the simple bounding box
        if (rotation == 0) {
            if (shape instanceof Circle) {
                Circle circle = (Circle) shape;
                double radius = circle.getRadius();
                left = x - radius;
                top = y - radius;
                right = x + radius;
                bottom = y + radius;
            } else if (shape instanceof Rectangle) {
                Rectangle rectangle = (Rectangle) shape;
                left = x;
                top = y;
                right = x + rectangle.getWidth();
                bottom = y + rectangle.getHeight();
            } else if (shape instanceof Polygon) {
                Polygon polygon = (Polygon) shape;
                double[] pointsX = polygon.getPointsX();
                double[] pointsY = polygon.getPointsY();
                
                for (int i = 0; i < polygon.getNumPoints(); i++) {
                    double pointX = x + pointsX[i];
                    double pointY = y + pointsY[i];
                    
                    left = Math.min(left, pointX);
                    top = Math.min(top, pointY);
                    right = Math.max(right, pointX);
                    bottom = Math.max(bottom, pointY);
                }
            }
        } else {
            // For rotated shapes, calculate the rotated bounding box
            double[] corners = null;
            
            if (shape instanceof Circle) {
                // For circles, rotation doesn't affect the bounding box
                Circle circle = (Circle) shape;
                double radius = circle.getRadius();
                left = x - radius;
                top = y - radius;
                right = x + radius;
                bottom = y + radius;
            } else if (shape instanceof Rectangle) {
                Rectangle rectangle = (Rectangle) shape;
                double width = rectangle.getWidth();
                double height = rectangle.getHeight();
                
                // Calculate the four corners of the rectangle
                corners = new double[] {
                    0, 0,           // Top-left
                    width, 0,       // Top-right
                    width, height,  // Bottom-right
                    0, height       // Bottom-left
                };
            } else if (shape instanceof Polygon) {
                Polygon polygon = (Polygon) shape;
                double[] pointsX = polygon.getPointsX();
                double[] pointsY = polygon.getPointsY();
                
                // Use all points of the polygon
                corners = new double[polygon.getNumPoints() * 2];
                for (int i = 0; i < polygon.getNumPoints(); i++) {
                    corners[i * 2] = pointsX[i];
                    corners[i * 2 + 1] = pointsY[i];
                }
            }
            
            // If we have corners to rotate, calculate the rotated bounding box
            if (corners != null) {
                // Cache trigonometric calculations
                updateTrigCache(rotation);
                
                // Initialize with extreme values
                left = Double.MAX_VALUE;
                top = Double.MAX_VALUE;
                right = Double.MIN_VALUE;
                bottom = Double.MIN_VALUE;
                
                // Rotate each corner and find the extremes
                for (int i = 0; i < corners.length; i += 2) {
                    double cornerX = corners[i];
                    double cornerY = corners[i + 1];
                    
                    // Rotate the point using cached sin/cos values
                    double rotatedX = cornerX * cachedCos - cornerY * cachedSin;
                    double rotatedY = cornerX * cachedSin + cornerY * cachedCos;
                    
                    // Update the bounding box
                    left = Math.min(left, x + rotatedX);
                    top = Math.min(top, y + rotatedY);
                    right = Math.max(right, x + rotatedX);
                    bottom = Math.max(bottom, y + rotatedY);
                }
            }
        }
        
        double[] result = new double[] {left, top, right, bottom};
        
        // Cache the result
        boundingBoxCache.put(shape, result);
        
        return result;
    }
    
    /**
     * Updates the cached trigonometric values for a rotation angle.
     * This is an optimization that avoids recalculating sine and cosine values
     * when the rotation angle doesn't change, which is common during dragging operations.
     *
     * @param rotation The rotation angle in degrees
     */
    private void updateTrigCache(double rotation) {
        if (cachedRotation != rotation) {
            double radians = Math.toRadians(rotation);
            cachedSin = Math.sin(radians);
            cachedCos = Math.cos(radians);
            cachedRotation = rotation;
        }
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
    
    /**
     * Determines which resize handle is at the specified coordinates,
     * accounting for shape rotation.
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @return The resize handle at the coordinates, or NONE if no handle is at the coordinates
     */
    public ResizeHandle getResizeHandleAt(double x, double y) {
        if (selectedShape == null) {
            return ResizeHandle.NONE;
        }
        
        double width, height, centerX, centerY;
        
        // Get the actual dimensions of the shape based on its type
        if (selectedShape instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) selectedShape;
            width = rectangle.getWidth();
            height = rectangle.getHeight();
            centerX = rectangle.getX() + width / 2;
            centerY = rectangle.getY() + height / 2;
        } else if (selectedShape instanceof Circle) {
            Circle circle = (Circle) selectedShape;
            double radius = circle.getRadius();
            width = radius * 2;
            height = radius * 2;
            centerX = circle.getX();
            centerY = circle.getY();
        } else if (selectedShape instanceof Polygon) {
            Polygon polygon = (Polygon) selectedShape;
            
            // For polygons, we need to calculate the dimensions from the points
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
            
            double[] pointsX = polygon.getPointsX();
            double[] pointsY = polygon.getPointsY();
            
            for (int i = 0; i < polygon.getNumPoints(); i++) {
                minX = Math.min(minX, pointsX[i]);
                minY = Math.min(minY, pointsY[i]);
                maxX = Math.max(maxX, pointsX[i]);
                maxY = Math.max(maxY, pointsY[i]);
            }
            
            width = maxX - minX;
            height = maxY - minY;
            centerX = polygon.getX() + (minX + maxX) / 2;
            centerY = polygon.getY() + (minY + maxY) / 2;
        } else {
            // Fallback to bounding box for unknown shape types
            double[] boundingBox = getShapeBoundingBox(selectedShape);
            double left = boundingBox[0];
            double top = boundingBox[1];
            double right = boundingBox[2];
            double bottom = boundingBox[3];
            width = right - left;
            height = bottom - top;
            centerX = left + width / 2;
            centerY = top + height / 2;
        }
        
        // Calculate left, top, right, bottom for handle positions
        double left = centerX - width / 2;
        double top = centerY - height / 2;
        double right = centerX + width / 2;
        double bottom = centerY + height / 2;
        
        double handleSize = 8;
        double halfHandleSize = handleSize / 2;
        double rotation = selectedShape.getRotation();
        
        // Calculate rotation handle position based on the shape's rotation
        double rotationDistance = height / 2 + 20; // Distance from center to rotation handle (top edge + margin)
        double handleRadians = Math.toRadians(rotation);
        double rotationHandleX = centerX + rotationDistance * Math.sin(handleRadians);
        double rotationHandleY = centerY - rotationDistance * Math.cos(handleRadians);
        
        // Check rotation handle first
        if (isPointNearHandle(x, y, rotationHandleX, rotationHandleY, halfHandleSize * 1.5)) {
            return ResizeHandle.ROTATION;
        }
        
        // If the shape is not rotated, check handles at the regular positions
        if (rotation == 0) {
            // Check each resize handle
            if (isPointNearHandle(x, y, left, top, halfHandleSize)) {
                return ResizeHandle.TOP_LEFT;
            } else if (isPointNearHandle(x, y, centerX, top, halfHandleSize)) {
                return ResizeHandle.TOP_MIDDLE;
            } else if (isPointNearHandle(x, y, right, top, halfHandleSize)) {
                return ResizeHandle.TOP_RIGHT;
            } else if (isPointNearHandle(x, y, left, centerY, halfHandleSize)) {
                return ResizeHandle.MIDDLE_LEFT;
            } else if (isPointNearHandle(x, y, right, centerY, halfHandleSize)) {
                return ResizeHandle.MIDDLE_RIGHT;
            } else if (isPointNearHandle(x, y, left, bottom, halfHandleSize)) {
                return ResizeHandle.BOTTOM_LEFT;
            } else if (isPointNearHandle(x, y, centerX, bottom, halfHandleSize)) {
                return ResizeHandle.BOTTOM_MIDDLE;
            } else if (isPointNearHandle(x, y, right, bottom, halfHandleSize)) {
                return ResizeHandle.BOTTOM_RIGHT;
            }
        } else {
            // For rotated shapes, calculate the actual corner and side positions
            
            // Calculate the four corners of the rotated rectangle
            double[] corners = new double[8];
            
            // Top-left corner (relative to center)
            corners[0] = -width/2;
            corners[1] = -height/2;
            
            // Top-right corner (relative to center)
            corners[2] = width/2;
            corners[3] = -height/2;
            
            // Bottom-right corner (relative to center)
            corners[4] = width/2;
            corners[5] = height/2;
            
            // Bottom-left corner (relative to center)
            corners[6] = -width/2;
            corners[7] = height/2;
            
            // Calculate sin and cos for the rotation
            double rotationRadians = Math.toRadians(rotation);
            double cos = Math.cos(rotationRadians);
            double sin = Math.sin(rotationRadians);
            
            // Rotate each corner and calculate its absolute position
            double[] rotatedCorners = new double[8];
            for (int i = 0; i < 8; i += 2) {
                double cornerX = corners[i];
                double cornerY = corners[i + 1];
                
                // Apply rotation
                rotatedCorners[i] = centerX + (cornerX * cos - cornerY * sin);
                rotatedCorners[i + 1] = centerY + (cornerX * sin + cornerY * cos);
            }
            
            // Check corner handles
            if (isPointNearHandle(x, y, rotatedCorners[0], rotatedCorners[1], halfHandleSize)) {
                return ResizeHandle.TOP_LEFT;
            } else if (isPointNearHandle(x, y, rotatedCorners[2], rotatedCorners[3], halfHandleSize)) {
                return ResizeHandle.TOP_RIGHT;
            } else if (isPointNearHandle(x, y, rotatedCorners[4], rotatedCorners[5], halfHandleSize)) {
                return ResizeHandle.BOTTOM_RIGHT;
            } else if (isPointNearHandle(x, y, rotatedCorners[6], rotatedCorners[7], halfHandleSize)) {
                return ResizeHandle.BOTTOM_LEFT;
            }
            
            // Calculate and check middle handles
            // Top middle
            double topMidX = centerX + (0 * cos - (-height/2) * sin);
            double topMidY = centerY + (0 * sin + (-height/2) * cos);
            if (isPointNearHandle(x, y, topMidX, topMidY, halfHandleSize)) {
                return ResizeHandle.TOP_MIDDLE;
            }
            
            // Right middle
            double rightMidX = centerX + ((width/2) * cos - 0 * sin);
            double rightMidY = centerY + ((width/2) * sin + 0 * cos);
            if (isPointNearHandle(x, y, rightMidX, rightMidY, halfHandleSize)) {
                return ResizeHandle.MIDDLE_RIGHT;
            }
            
            // Bottom middle
            double bottomMidX = centerX + (0 * cos - (height/2) * sin);
            double bottomMidY = centerY + (0 * sin + (height/2) * cos);
            if (isPointNearHandle(x, y, bottomMidX, bottomMidY, halfHandleSize)) {
                return ResizeHandle.BOTTOM_MIDDLE;
            }
            
            // Left middle
            double leftMidX = centerX + ((-width/2) * cos - 0 * sin);
            double leftMidY = centerY + ((-width/2) * sin + 0 * cos);
            if (isPointNearHandle(x, y, leftMidX, leftMidY, halfHandleSize)) {
                return ResizeHandle.MIDDLE_LEFT;
            }
        }
        
        return ResizeHandle.NONE;
    }
    
    /**
     * Checks if a point is near a handle.
     *
     * @param pointX The x-coordinate of the point
     * @param pointY The y-coordinate of the point
     * @param handleX The x-coordinate of the handle
     * @param handleY The y-coordinate of the handle
     * @param tolerance The distance tolerance
     * @return true if the point is near the handle, false otherwise
     */
    private boolean isPointNearHandle(double pointX, double pointY, double handleX, double handleY, double tolerance) {
        double dx = pointX - handleX;
        double dy = pointY - handleY;
        return Math.sqrt(dx * dx + dy * dy) <= tolerance;
    }
    
    /**
     * Resizes a shape based on the current resize handle and mouse position.
     *
     * @param shape The shape to resize
     * @param handle The resize handle being dragged
     * @param mouseX The current mouse x-coordinate
     * @param mouseY The current mouse y-coordinate
     */
    /**
     * Resizes a shape based on the current resize handle and mouse position.
     * Handles resizing for both rotated and non-rotated shapes.
     *
     * @param shape The shape to resize
     * @param handle The resize handle being dragged
     * @param mouseX The current mouse x-coordinate
     * @param mouseY The current mouse y-coordinate
     */
    public void resizeShapeWithHandle(Shape shape, ResizeHandle handle, double mouseX, double mouseY) {
        if (handle == ResizeHandle.ROTATION) {
            rotateShape(shape, mouseX, mouseY);
            return;
        }
        
        // For rotated shapes, we need to transform the mouse coordinates
        // to account for the rotation before resizing
        double rotation = shape.getRotation();
        double transformedMouseX = mouseX;
        double transformedMouseY = mouseY;
        
        if (rotation != 0) {
            // Get the shape's center
            double centerX, centerY;
            
            if (shape instanceof Rectangle) {
                Rectangle rectangle = (Rectangle) shape;
                double width = rectangle.getWidth();
                double height = rectangle.getHeight();
                centerX = rectangle.getX() + width / 2;
                centerY = rectangle.getY() + height / 2;
            } else if (shape instanceof Circle) {
                Circle circle = (Circle) shape;
                centerX = circle.getX();
                centerY = circle.getY();
            } else if (shape instanceof Polygon) {
                Polygon polygon = (Polygon) shape;
                
                // For polygons, calculate center from points
                double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
                double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
                
                double[] pointsX = polygon.getPointsX();
                double[] pointsY = polygon.getPointsY();
                
                for (int i = 0; i < polygon.getNumPoints(); i++) {
                    minX = Math.min(minX, pointsX[i]);
                    minY = Math.min(minY, pointsY[i]);
                    maxX = Math.max(maxX, pointsX[i]);
                    maxY = Math.max(maxY, pointsY[i]);
                }
                
                centerX = polygon.getX() + (minX + maxX) / 2;
                centerY = polygon.getY() + (minY + maxY) / 2;
            } else {
                // Fallback to bounding box for unknown shape types
                double[] boundingBox = getShapeBoundingBox(shape);
                centerX = boundingBox[0] + (boundingBox[2] - boundingBox[0]) / 2;
                centerY = boundingBox[1] + (boundingBox[3] - boundingBox[1]) / 2;
            }
            
            // Translate mouse coordinates to be relative to the shape's center
            double relativeX = mouseX - centerX;
            double relativeY = mouseY - centerY;
            
            // Rotate the coordinates in the opposite direction of the shape's rotation
            double radians = Math.toRadians(-rotation);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            
            double rotatedX = relativeX * cos - relativeY * sin;
            double rotatedY = relativeX * sin + relativeY * cos;
            
            // Translate back to absolute coordinates
            transformedMouseX = centerX + rotatedX;
            transformedMouseY = centerY + rotatedY;
        }
        
        if (shape instanceof Circle) {
            resizeCircle((Circle) shape, handle, mouseX, mouseY);
        } else if (shape instanceof Rectangle) {
            resizeRectangle((Rectangle) shape, handle, transformedMouseX, transformedMouseY);
        } else if (shape instanceof Polygon) {
            resizePolygon((Polygon) shape, handle, transformedMouseX, transformedMouseY);
        }
    }
    
    /**
     * Rotates a shape based on the mouse position relative to the shape's center.
     *
     * @param shape The shape to rotate
     * @param mouseX The current mouse x-coordinate
     * @param mouseY The current mouse y-coordinate
     */
    private void rotateShape(Shape shape, double mouseX, double mouseY) {
        // Get the shape's center
        double centerX, centerY;
        
        if (shape instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) shape;
            double width = rectangle.getWidth();
            double height = rectangle.getHeight();
            centerX = rectangle.getX() + width / 2;
            centerY = rectangle.getY() + height / 2;
        } else if (shape instanceof Circle) {
            Circle circle = (Circle) shape;
            centerX = circle.getX();
            centerY = circle.getY();
        } else if (shape instanceof Polygon) {
            Polygon polygon = (Polygon) shape;
            
            // Use the polygon's centroid method for more accurate center calculation
            double[] centroid = polygon.getCentroid();
            centerX = polygon.getX() + centroid[0];
            centerY = polygon.getY() + centroid[1];
        } else {
            // Fallback to bounding box for unknown shape types
            double[] boundingBox = getShapeBoundingBox(shape);
            double left = boundingBox[0];
            double top = boundingBox[1];
            double right = boundingBox[2];
            double bottom = boundingBox[3];
            
            centerX = left + (right - left) / 2;
            centerY = top + (bottom - top) / 2;
        }
        
        // Calculate the angle from center to mouse position
        double newAngle = Math.toDegrees(Math.atan2(mouseY - centerY, mouseX - centerX));
        
        // Adjust the angle to make it more intuitive
        // We add 90 degrees to make the rotation follow the mouse movement directly
        newAngle = ((newAngle + 90) % 360);
        if (newAngle < 0) {
            newAngle += 360;
        }
        
        // Get the current rotation
        double currentRotation = shape.getRotation();
        
        // Apply smoothing by interpolating between the current and new rotation
        // The smaller the interpolation factor, the smoother the rotation
        double interpolationFactor = 0.2; // Adjust this value for desired smoothness (0.1 to 0.5 is a good range)
        
        // Calculate the shortest path between the two angles
        double angleDiff = newAngle - currentRotation;
        
        // Normalize the angle difference to be between -180 and 180 degrees
        if (angleDiff > 180) {
            angleDiff -= 360;
        } else if (angleDiff < -180) {
            angleDiff += 360;
        }
        
        // Calculate the interpolated rotation
        double interpolatedRotation = currentRotation + angleDiff * interpolationFactor;
        
        // Ensure the rotation stays within 0-360 range
        interpolatedRotation = interpolatedRotation % 360;
        if (interpolatedRotation < 0) {
            interpolatedRotation += 360;
        }
        
        // Set the rotation using the interpolated value
        shape.setRotation(interpolatedRotation);
    }
    
    /**
     * Resizes a circle based on the current resize handle and mouse position.
     *
     * @param circle The circle to resize
     * @param handle The resize handle being dragged
     * @param mouseX The current mouse x-coordinate
     * @param mouseY The current mouse y-coordinate
     */
    /**
     * Resizes a circle based on the current resize handle and mouse position.
     * For circles, the resize handles are positioned on the circle's perimeter
     * at the appropriate angles, accounting for rotation.
     *
     * @param circle The circle to resize
     * @param handle The resize handle being dragged
     * @param mouseX The current mouse x-coordinate
     * @param mouseY The current mouse y-coordinate
     */
    private void resizeCircle(Circle circle, ResizeHandle handle, double mouseX, double mouseY) {
        double centerX = circle.getX();
        double centerY = circle.getY();
        
        // Calculate the distance from the center to the mouse position
        // This will be used to determine the new radius
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        
        // For circles, we always use the direct distance (Euclidean distance)
        // This ensures the handles stay on the circle's perimeter
        double newRadius = Math.sqrt(dx * dx + dy * dy);
        
        // Set the new radius, ensuring it's positive
        if (newRadius > 0) {
            circle.setRadius(newRadius);
        }
    }
    
    /**
     * Resizes a rectangle based on the current resize handle and mouse position.
     *
     * @param rectangle The rectangle to resize
     * @param handle The resize handle being dragged
     * @param mouseX The current mouse x-coordinate
     * @param mouseY The current mouse y-coordinate
     */
    private void resizeRectangle(Rectangle rectangle, ResizeHandle handle, double mouseX, double mouseY) {
        double x = rectangle.getX();
        double y = rectangle.getY();
        double width = rectangle.getWidth();
        double height = rectangle.getHeight();
        double rotation = rectangle.getRotation();
        
        // If the rectangle is not rotated, use the simple resizing logic
        if (rotation == 0) {
            double newX = x;
            double newY = y;
            double newWidth = width;
            double newHeight = height;
            
            switch (handle) {
                case TOP_LEFT:
                    newX = mouseX;
                    newY = mouseY;
                    newWidth = x + width - mouseX;
                    newHeight = y + height - mouseY;
                    break;
                case TOP_MIDDLE:
                    newY = mouseY;
                    newHeight = y + height - mouseY;
                    break;
                case TOP_RIGHT:
                    newY = mouseY;
                    newWidth = mouseX - x;
                    newHeight = y + height - mouseY;
                    break;
                case MIDDLE_LEFT:
                    newX = mouseX;
                    newWidth = x + width - mouseX;
                    break;
                case MIDDLE_RIGHT:
                    newWidth = mouseX - x;
                    break;
                case BOTTOM_LEFT:
                    newX = mouseX;
                    newWidth = x + width - mouseX;
                    newHeight = mouseY - y;
                    break;
                case BOTTOM_MIDDLE:
                    newHeight = mouseY - y;
                    break;
                case BOTTOM_RIGHT:
                    newWidth = mouseX - x;
                    newHeight = mouseY - y;
                    break;
                default:
                    break;
            }
            
            // Ensure width and height are positive
            if (newWidth > 0 && newHeight > 0) {
                rectangle.setPosition(newX, newY);
                rectangle.setDimensions(newWidth, newHeight);
            }
        } else {
            // For rotated rectangles, we need to handle resizing differently
            // Calculate the center of the rectangle
            double centerX = x + width / 2;
            double centerY = y + height / 2;
            
            // Rotate the mouse point in the opposite direction of the rectangle's rotation
            double radians = Math.toRadians(-rotation);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            
            // Translate to origin
            double translatedX = mouseX - centerX;
            double translatedY = mouseY - centerY;
            
            // Rotate
            double rotatedMouseX = translatedX * cos - translatedY * sin + centerX;
            double rotatedMouseY = translatedX * sin + translatedY * cos + centerY;
            
            // Calculate the new dimensions based on the rotated mouse position
            double newX = x;
            double newY = y;
            double newWidth = width;
            double newHeight = height;
            double oldRight = x + width;
            double oldBottom = y + height;
            
            switch (handle) {
                case TOP_LEFT:
                    newX = rotatedMouseX;
                    newY = rotatedMouseY;
                    newWidth = oldRight - rotatedMouseX;
                    newHeight = oldBottom - rotatedMouseY;
                    break;
                case TOP_MIDDLE:
                    newY = rotatedMouseY;
                    newHeight = oldBottom - rotatedMouseY;
                    break;
                case TOP_RIGHT:
                    newWidth = rotatedMouseX - x;
                    newY = rotatedMouseY;
                    newHeight = oldBottom - rotatedMouseY;
                    break;
                case MIDDLE_LEFT:
                    newX = rotatedMouseX;
                    newWidth = oldRight - rotatedMouseX;
                    break;
                case MIDDLE_RIGHT:
                    newWidth = rotatedMouseX - x;
                    break;
                case BOTTOM_LEFT:
                    newX = rotatedMouseX;
                    newWidth = oldRight - rotatedMouseX;
                    newHeight = rotatedMouseY - y;
                    break;
                case BOTTOM_MIDDLE:
                    newHeight = rotatedMouseY - y;
                    break;
                case BOTTOM_RIGHT:
                    newWidth = rotatedMouseX - x;
                    newHeight = rotatedMouseY - y;
                    break;
                default:
                    break;
            }
            
            // Ensure width and height are positive
            if (newWidth > 0 && newHeight > 0) {
                // For side handles, we need to maintain the opposite side's position
                double adjustedX = newX;
                double adjustedY = newY;
                
                // Calculate the position adjustment based on the handle being dragged
                if (handle == ResizeHandle.MIDDLE_RIGHT || handle == ResizeHandle.TOP_RIGHT ||
                    handle == ResizeHandle.BOTTOM_RIGHT) {
                    // Right side handles - keep left side fixed
                    adjustedX = x;
                } else if (handle == ResizeHandle.BOTTOM_MIDDLE || handle == ResizeHandle.BOTTOM_LEFT ||
                           handle == ResizeHandle.BOTTOM_RIGHT) {
                    // Bottom side handles - keep top side fixed
                    adjustedY = y;
                } else if (handle == ResizeHandle.TOP_MIDDLE) {
                    // Top middle - adjust Y but keep X
                    adjustedX = x;
                } else if (handle == ResizeHandle.MIDDLE_LEFT) {
                    // Left middle - adjust X but keep Y
                    adjustedY = y;
                }
                
                // For corner handles, we need to adjust both coordinates
                // For rotated shapes, we need to apply rotation transformation to the adjustment
                if (handle == ResizeHandle.TOP_LEFT || handle == ResizeHandle.TOP_RIGHT ||
                    handle == ResizeHandle.BOTTOM_LEFT || handle == ResizeHandle.BOTTOM_RIGHT) {
                    // Calculate the new center after resizing
                    double newCenterX = adjustedX + newWidth / 2;
                    double newCenterY = adjustedY + newHeight / 2;
                    
                    // Calculate the offset from the old center
                    double offsetX = newCenterX - centerX;
                    double offsetY = newCenterY - centerY;
                    
                    // Rotate the offset back to match the rectangle's rotation
                    radians = Math.toRadians(rotation);
                    cos = Math.cos(radians);
                    sin = Math.sin(radians);
                    
                    double rotatedOffsetX = offsetX * cos - offsetY * sin;
                    double rotatedOffsetY = offsetX * sin + offsetY * cos;
                    
                    // Apply the rotated offset to the position
                    adjustedX = x - rotatedOffsetX;
                    adjustedY = y - rotatedOffsetY;
                }
                
                // Set the new position and dimensions
                rectangle.setPosition(adjustedX, adjustedY);
                rectangle.setDimensions(newWidth, newHeight);
            }
        }
    }
    
    /**
     * Resizes a polygon based on the current resize handle and mouse position.
     *
     * @param polygon The polygon to resize
     * @param handle The resize handle being dragged
     * @param mouseX The current mouse x-coordinate
     * @param mouseY The current mouse y-coordinate
     */
    private void resizePolygon(Polygon polygon, ResizeHandle handle, double mouseX, double mouseY) {
        // Mark the current polygon area as dirty before resizing
        markShapeDirty(polygon);
        
        // Get the bounding box from cache
        double[] boundingBox = getShapeBoundingBox(polygon);
        double left = boundingBox[0];
        double top = boundingBox[1];
        double right = boundingBox[2];
        double bottom = boundingBox[3];
        
        double centerX = left + (right - left) / 2;
        double centerY = top + (bottom - top) / 2;
        
        double oldWidth = right - left;
        double oldHeight = bottom - top;
        double polygonX = polygon.getX();
        double polygonY = polygon.getY();
        double rotation = polygon.getRotation();
        
        // If the polygon is rotated, rotate the mouse point in the opposite direction
        double adjustedMouseX = mouseX;
        double adjustedMouseY = mouseY;
        
        if (rotation != 0) {
            // Use cached trigonometric values
            updateTrigCache(-rotation);
            double cos = cachedCos;
            double sin = cachedSin;
            
            // Translate to origin (center of polygon)
            double translatedX = mouseX - centerX;
            double translatedY = mouseY - centerY;
            
            // Rotate using cached values
            adjustedMouseX = translatedX * cos - translatedY * sin + centerX;
            adjustedMouseY = translatedX * sin + translatedY * cos + centerY;
        }
        
        double newLeft = left;
        double newTop = top;
        double newRight = right;
        double newBottom = bottom;
        
        // Determine new bounds based on which handle is being dragged
        switch (handle) {
            case TOP_LEFT:
                newLeft = adjustedMouseX;
                newTop = adjustedMouseY;
                break;
            case TOP_MIDDLE:
                newTop = adjustedMouseY;
                break;
            case TOP_RIGHT:
                newRight = adjustedMouseX;
                newTop = adjustedMouseY;
                break;
            case MIDDLE_LEFT:
                newLeft = adjustedMouseX;
                break;
            case MIDDLE_RIGHT:
                newRight = adjustedMouseX;
                break;
            case BOTTOM_LEFT:
                newLeft = adjustedMouseX;
                newBottom = adjustedMouseY;
                break;
            case BOTTOM_MIDDLE:
                newBottom = adjustedMouseY;
                break;
            case BOTTOM_RIGHT:
                newRight = adjustedMouseX;
                newBottom = adjustedMouseY;
                break;
            default:
                break;
        }
        
        double newWidth = newRight - newLeft;
        double newHeight = newBottom - newTop;
        
        // Only resize if the new dimensions are positive
        if (newWidth > 0 && newHeight > 0) {
            double scaleX = newWidth / oldWidth;
            double scaleY = newHeight / oldHeight;
            
            // Get the existing points from the polygon
            double[] pointsX = polygon.getPointsX();
            double[] pointsY = polygon.getPointsY();
            int numPoints = polygon.getNumPoints();
            
            // Calculate the center of the points
            double pointsCenterX = 0;
            double pointsCenterY = 0;
            for (int i = 0; i < numPoints; i++) {
                pointsCenterX += pointsX[i];
                pointsCenterY += pointsY[i];
            }
            pointsCenterX /= numPoints;
            pointsCenterY /= numPoints;
            
            // Scale each point relative to the center (modify in place)
            for (int i = 0; i < numPoints; i++) {
                // Calculate point position relative to the center
                double relX = pointsX[i] - pointsCenterX;
                double relY = pointsY[i] - pointsCenterY;
                
                // Scale the point and update directly in the array
                pointsX[i] = pointsCenterX + relX * scaleX;
                pointsY[i] = pointsCenterY + relY * scaleY;
            }
            
            // Calculate the position adjustment needed based on the handle being dragged
            double deltaX = 0;
            double deltaY = 0;
            
            // Calculate the change in position of the bounding box
            double boxDeltaX = 0;
            double boxDeltaY = 0;
            
            // Determine position adjustment based on which handle is being dragged
            if (handle == ResizeHandle.TOP_LEFT || handle == ResizeHandle.MIDDLE_LEFT ||
                handle == ResizeHandle.BOTTOM_LEFT) {
                boxDeltaX = newLeft - left;
            } else if (handle == ResizeHandle.TOP_RIGHT || handle == ResizeHandle.MIDDLE_RIGHT ||
                       handle == ResizeHandle.BOTTOM_RIGHT) {
                boxDeltaX = 0; // Right handles don't move the left edge
            } else {
                // Middle handles - keep the shape centered horizontally
                boxDeltaX = (newLeft - left) / 2;
            }
            
            if (handle == ResizeHandle.TOP_LEFT || handle == ResizeHandle.TOP_MIDDLE ||
                handle == ResizeHandle.TOP_RIGHT) {
                boxDeltaY = newTop - top;
            } else if (handle == ResizeHandle.BOTTOM_LEFT || handle == ResizeHandle.BOTTOM_MIDDLE ||
                       handle == ResizeHandle.BOTTOM_RIGHT) {
                boxDeltaY = 0; // Bottom handles don't move the top edge
            } else {
                // Middle handles - keep the shape centered vertically
                boxDeltaY = (newTop - top) / 2;
            }
            
            // If the polygon is rotated, rotate the delta in the same direction as the polygon
            if (rotation != 0) {
                // Use cached trigonometric values
                updateTrigCache(rotation);
                double cos = cachedCos;
                double sin = cachedSin;
                
                deltaX = boxDeltaX * cos - boxDeltaY * sin;
                deltaY = boxDeltaX * sin + boxDeltaY * cos;
            } else {
                deltaX = boxDeltaX;
                deltaY = boxDeltaY;
            }
            
            // Create a new polygon with the scaled points
            Polygon newPolygon = new Polygon(
                polygonX + deltaX,
                polygonY + deltaY,
                pointsX,
                pointsY,
                polygon.getFillColor()
            );
            
            // Set the rotation to match the original polygon
            newPolygon.setRotation(rotation);
            
            // Replace the old polygon with the new one in the shapes list
            int index = shapes.indexOf(polygon);
            if (index >= 0) {
                // Mark the old polygon area as dirty
                markShapeDirty(polygon);
                
                shapes.set(index, newPolygon);
                selectedShape = newPolygon;
                
                // Invalidate the bounding box cache for the old polygon
                boundingBoxCache.remove(polygon);
                
                // Mark the new polygon area as dirty
                markShapeDirty(newPolygon);
            }
            
            // Invalidate the bounding box cache for this shape
            boundingBoxCache.remove(polygon);
            
            // Mark the new polygon area as dirty
            markShapeDirty(polygon);
        }
    }
    
    /**
     * Sets the drawing controller for this canvas.
     *
     * @param controller The drawing controller
     */
    public void setDrawingController(com.shapeeditor.controller.DrawingController controller) {
        this.drawingController = controller;
        
        // Refresh the mouse handlers to ensure they use the new controller
        setupMouseHandlers();
    }
    
    /**
     * Gets the drawing controller associated with this canvas.
     *
     * @return The drawing controller
     */
    public com.shapeeditor.controller.DrawingController getDrawingController() {
        return drawingController;
    }
    
    
    /**
     * Utility method to find a drawing controller by traversing parent components.
     *
     * @return The found drawing controller or null if none is found
     */
    private com.shapeeditor.controller.DrawingController findDrawingControllerInParent() {
        javafx.scene.Parent parent = this.getParent();
        while (parent != null) {
            // Check if the parent is a MainView or another class that might have a drawing controller
            try {
                // Use reflection to check if the parent has a getDrawingController method
                java.lang.reflect.Method method = parent.getClass().getMethod("getDrawingController");
                if (method != null) {
                    Object result = method.invoke(parent);
                    if (result instanceof com.shapeeditor.controller.DrawingController) {
                        return (com.shapeeditor.controller.DrawingController) result;
                    }
                }
            } catch (Exception e) {
                // Method doesn't exist or couldn't be invoked, continue to the next parent
            }
            
            parent = parent.getParent();
        }
        return null;
    }
}
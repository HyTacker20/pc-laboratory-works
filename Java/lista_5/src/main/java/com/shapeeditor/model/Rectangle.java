package com.shapeeditor.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents a rectangle shape in the shape editor.
 */
public class Rectangle extends Shape {
    /** Width of the rectangle */
    private double width;
    
    /** Height of the rectangle */
    private double height;
    
    /**
     * Constructs a rectangle with the specified position, dimensions, and color.
     *
     * @param x The x-coordinate of the rectangle's top-left corner
     * @param y The y-coordinate of the rectangle's top-left corner
     * @param width The width of the rectangle
     * @param height The height of the rectangle
     * @param fillColor The fill color of the rectangle
     */
    public Rectangle(double x, double y, double width, double height, Color fillColor) {
        super(x, y, fillColor);
        this.width = width;
        this.height = height;
    }
    
    /**
     * Draws the rectangle on the specified graphics context.
     *
     * @param gc The graphics context to draw on
     */
    @Override
    public void draw(GraphicsContext gc) {
        // Save the current state of the graphics context
        gc.save();
        
        // Apply transformations
        // Translate to the center of the rectangle instead of the top-left corner
        gc.translate(x + width / 2, y + height / 2);
        gc.rotate(rotation);
        
        // Set the fill color and draw the rectangle centered at the origin
        gc.setFill(fillColor);
        gc.fillRect(-width / 2, -height / 2, width, height);
        
        // Restore the graphics context to its original state
        gc.restore();
    }
    
    /**
     * Checks if the specified point is contained within the rectangle.
     *
     * @param pointX The x-coordinate of the point
     * @param pointY The y-coordinate of the point
     * @return true if the point is contained within the rectangle, false otherwise
     */
    @Override
    public boolean contains(double pointX, double pointY) {
        // If there's no rotation, we can use a simple bounding box check
        if (rotation == 0) {
            return pointX >= x && pointX <= x + width &&
                   pointY >= y && pointY <= y + height;
        }
        
        // For rotated rectangles, we need to transform the point to the rectangle's coordinate system
        double radians = Math.toRadians(rotation);
        double cosTheta = Math.cos(radians);
        double sinTheta = Math.sin(radians);
        
        // Translate the point relative to the rectangle's center
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        double translatedX = pointX - centerX;
        double translatedY = pointY - centerY;
        
        // Rotate the point in the opposite direction of the rectangle's rotation
        double rotatedX = translatedX * cosTheta + translatedY * sinTheta;
        double rotatedY = -translatedX * sinTheta + translatedY * cosTheta;
        
        // Check if the rotated point is within the rectangle's bounds
        return rotatedX >= -width / 2 && rotatedX <= width / 2 &&
               rotatedY >= -height / 2 && rotatedY <= height / 2;
    }
    
    /**
     * Resizes the rectangle by the specified factor.
     *
     * @param factor The factor to resize by (e.g., 2.0 doubles the size)
     */
    @Override
    public void resize(double factor) {
        if (factor > 0) {
            this.width *= factor;
            this.height *= factor;
        }
    }
    
    /**
     * Gets the width of the rectangle.
     *
     * @return The width
     */
    public double getWidth() {
        return width;
    }
    
    /**
     * Sets the width of the rectangle.
     *
     * @param width The new width
     */
    public void setWidth(double width) {
        if (width > 0) {
            this.width = width;
        }
    }
    
    /**
     * Gets the height of the rectangle.
     *
     * @return The height
     */
    public double getHeight() {
        return height;
    }
    
    /**
     * Sets the height of the rectangle.
     *
     * @param height The new height
     */
    public void setHeight(double height) {
        if (height > 0) {
            this.height = height;
        }
    }
    
    /**
     * Sets the position of the rectangle.
     *
     * @param newX The new x-coordinate
     * @param newY The new y-coordinate
     */
    public void setPosition(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }
    
    /**
     * Sets the dimensions of the rectangle.
     *
     * @param width The new width
     * @param height The new height
     */
    public void setDimensions(double width, double height) {
        if (width >= 0) {
            this.width = width;
        }
        if (height >= 0) {
            this.height = height;
        }
    }
}
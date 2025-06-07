package com.shapeeditor.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents a circle shape in the shape editor.
 */
public class Circle extends Shape {
    /** Radius of the circle */
    private double radius;
    
    /**
     * Constructs a circle with the specified position, radius, and color.
     *
     * @param x The x-coordinate of the circle's center
     * @param y The y-coordinate of the circle's center
     * @param radius The radius of the circle
     * @param fillColor The fill color of the circle
     */
    public Circle(double x, double y, double radius, Color fillColor) {
        super(x, y, fillColor);
        this.radius = radius;
    }
    
    /**
     * Draws the circle on the specified graphics context.
     *
     * @param gc The graphics context to draw on
     */
    @Override
    public void draw(GraphicsContext gc) {
        // Save the current state of the graphics context
        gc.save();
        
        // Apply transformations
        gc.translate(x, y);
        gc.rotate(rotation);
        
        // Set the fill color and draw the circle
        gc.setFill(fillColor);
        gc.fillOval(-radius, -radius, radius * 2, radius * 2);
        
        // Restore the graphics context to its original state
        gc.restore();
    }
    
    /**
     * Checks if the specified point is contained within the circle.
     *
     * @param pointX The x-coordinate of the point
     * @param pointY The y-coordinate of the point
     * @return true if the point is contained within the circle, false otherwise
     */
    @Override
    public boolean contains(double pointX, double pointY) {
        // For circles, rotation doesn't affect the shape itself, but it can affect
        // visual elements like patterns or textures within the circle.
        // If we need to account for rotation, we would rotate the test point
        // in the opposite direction of the circle's rotation.
        
        double testX = pointX;
        double testY = pointY;
        
        // If the circle is rotated, rotate the test point in the opposite direction
        if (rotation != 0) {
            double radians = Math.toRadians(-rotation);
            double cosTheta = Math.cos(radians);
            double sinTheta = Math.sin(radians);
            
            // Translate to origin (center of circle)
            double translatedX = pointX - x;
            double translatedY = pointY - y;
            
            // Rotate
            double rotatedX = translatedX * cosTheta - translatedY * sinTheta;
            double rotatedY = translatedX * sinTheta + translatedY * cosTheta;
            
            // Translate back
            testX = rotatedX + x;
            testY = rotatedY + y;
        }
        
        // Calculate the distance from the point to the center of the circle
        double distanceX = testX - x;
        double distanceY = testY - y;
        double distanceSquared = distanceX * distanceX + distanceY * distanceY;
        
        // Check if the distance is less than or equal to the radius
        return distanceSquared <= radius * radius;
    }
    
    /**
     * Resizes the circle by the specified factor.
     *
     * @param factor The factor to resize by (e.g., 2.0 doubles the size)
     */
    @Override
    public void resize(double factor) {
        if (factor > 0) {
            this.radius *= factor;
        }
    }
    
    /**
     * Gets the radius of the circle.
     *
     * @return The radius
     */
    public double getRadius() {
        return radius;
    }
    
    /**
     * Sets the radius of the circle.
     *
     * @param radius The new radius
     */
    public void setRadius(double radius) {
        if (radius > 0) {
            this.radius = radius;
        }
    }
}
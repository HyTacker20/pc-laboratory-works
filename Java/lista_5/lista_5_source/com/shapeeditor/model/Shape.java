package com.shapeeditor.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Abstract base class for all shapes in the shape editor.
 * Defines common properties and behaviors for all shapes.
 */
public abstract class Shape {
    /** X-coordinate of the shape's position */
    protected double x;
    
    /** Y-coordinate of the shape's position */
    protected double y;
    
    /** Rotation angle in degrees */
    protected double rotation;
    
    /** Fill color of the shape */
    protected Color fillColor;
    
    /**
     * Constructs a shape with the specified position and color.
     *
     * @param x The x-coordinate of the shape's position
     * @param y The y-coordinate of the shape's position
     * @param fillColor The fill color of the shape
     */
    public Shape(double x, double y, Color fillColor) {
        this.x = x;
        this.y = y;
        this.rotation = 0.0;
        this.fillColor = fillColor;
    }
    
    /**
     * Draws the shape on the specified graphics context.
     *
     * @param gc The graphics context to draw on
     */
    public abstract void draw(GraphicsContext gc);
    
    /**
     * Checks if the specified point is contained within the shape.
     *
     * @param pointX The x-coordinate of the point
     * @param pointY The y-coordinate of the point
     * @return true if the point is contained within the shape, false otherwise
     */
    public abstract boolean contains(double pointX, double pointY);
    
    /**
     * Moves the shape to a new position.
     *
     * @param newX The new x-coordinate
     * @param newY The new y-coordinate
     */
    public void move(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }
    
    /**
     * Rotates the shape by the specified angle in degrees.
     *
     * @param angleDegrees The angle to rotate by, in degrees
     */
    public void rotate(double angleDegrees) {
        this.rotation = (this.rotation + angleDegrees) % 360;
        if (this.rotation < 0) {
            this.rotation += 360;
        }
    }
    
    /**
     * Resizes the shape by the specified factor.
     *
     * @param factor The factor to resize by (e.g., 2.0 doubles the size)
     */
    public abstract void resize(double factor);
    
    /**
     * Sets the fill color of the shape.
     *
     * @param color The new fill color
     */
    public void setFillColor(Color color) {
        this.fillColor = color;
    }
    
    /**
     * Gets the x-coordinate of the shape's position.
     *
     * @return The x-coordinate
     */
    public double getX() {
        return x;
    }
    
    /**
     * Gets the y-coordinate of the shape's position.
     *
     * @return The y-coordinate
     */
    public double getY() {
        return y;
    }
    
    /**
     * Gets the rotation angle of the shape in degrees.
     *
     * @return The rotation angle
     */
    public double getRotation() {
        return rotation;
    }
    
    /**
     * Gets the fill color of the shape.
     *
     * @return The fill color
     */
    public Color getFillColor() {
        return fillColor;
    }
}
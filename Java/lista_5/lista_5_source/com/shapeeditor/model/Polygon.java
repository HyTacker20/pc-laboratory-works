package com.shapeeditor.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Arrays;

/**
 * Represents a polygon shape in the shape editor.
 * A polygon is defined by a series of points.
 */
public class Polygon extends Shape {
    /** Array of x-coordinates of the polygon's points */
    private double[] pointsX;
    
    /** Array of y-coordinates of the polygon's points */
    private double[] pointsY;
    
    /** Number of points in the polygon */
    private int numPoints;
    
    /**
     * Constructs a polygon with the specified position, points, and color.
     *
     * @param x The x-coordinate of the polygon's reference point
     * @param y The y-coordinate of the polygon's reference point
     * @param pointsX Array of x-coordinates relative to the reference point
     * @param pointsY Array of y-coordinates relative to the reference point
     * @param fillColor The fill color of the polygon
     * @throws IllegalArgumentException if pointsX and pointsY have different lengths or are empty
     */
    public Polygon(double x, double y, double[] pointsX, double[] pointsY, Color fillColor) {
        super(x, y, fillColor);
        
        if (pointsX.length != pointsY.length) {
            throw new IllegalArgumentException("The number of x and y coordinates must be the same");
        }
        
        if (pointsX.length == 0) {
            throw new IllegalArgumentException("A polygon must have at least one point");
        }
        
        this.numPoints = pointsX.length;
        this.pointsX = Arrays.copyOf(pointsX, numPoints);
        this.pointsY = Arrays.copyOf(pointsY, numPoints);
    }
    
    /**
     * Draws the polygon on the specified graphics context.
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
        
        // Set the fill color
        gc.setFill(fillColor);
        
        // Create arrays for the transformed points
        double[] xPoints = new double[numPoints];
        double[] yPoints = new double[numPoints];
        
        // Copy the points
        for (int i = 0; i < numPoints; i++) {
            xPoints[i] = pointsX[i];
            yPoints[i] = pointsY[i];
        }
        
        // Fill the polygon
        gc.fillPolygon(xPoints, yPoints, numPoints);
        
        // Restore the graphics context to its original state
        gc.restore();
    }
    
    /**
     * Checks if the specified point is contained within the polygon.
     * Uses the ray casting algorithm to determine if a point is inside a polygon.
     *
     * @param pointX The x-coordinate of the point
     * @param pointY The y-coordinate of the point
     * @return true if the point is contained within the polygon, false otherwise
     */
    @Override
    public boolean contains(double pointX, double pointY) {
        // Translate the point relative to the polygon's position
        double testX = pointX - x;
        double testY = pointY - y;
        
        // If the polygon is rotated, we need to rotate the test point in the opposite direction
        if (rotation != 0) {
            double radians = Math.toRadians(-rotation);
            double cosTheta = Math.cos(radians);
            double sinTheta = Math.sin(radians);
            
            double rotatedX = testX * cosTheta - testY * sinTheta;
            double rotatedY = testX * sinTheta + testY * cosTheta;
            
            testX = rotatedX;
            testY = rotatedY;
        }
        
        // Ray casting algorithm
        boolean inside = false;
        for (int i = 0, j = numPoints - 1; i < numPoints; j = i++) {
            if (((pointsY[i] > testY) != (pointsY[j] > testY)) &&
                (testX < (pointsX[j] - pointsX[i]) * (testY - pointsY[i]) / (pointsY[j] - pointsY[i]) + pointsX[i])) {
                inside = !inside;
            }
        }
        
        return inside;
    }
    
    /**
     * Resizes the polygon by the specified factor.
     *
     * @param factor The factor to resize by (e.g., 2.0 doubles the size)
     */
    @Override
    public void resize(double factor) {
        if (factor > 0) {
            for (int i = 0; i < numPoints; i++) {
                pointsX[i] *= factor;
                pointsY[i] *= factor;
            }
        }
    }
    
    /**
     * Gets the x-coordinates of the polygon's points.
     *
     * @return A copy of the array of x-coordinates
     */
    public double[] getPointsX() {
        return Arrays.copyOf(pointsX, numPoints);
    }
    
    /**
     * Gets the y-coordinates of the polygon's points.
     *
     * @return A copy of the array of y-coordinates
     */
    public double[] getPointsY() {
        return Arrays.copyOf(pointsY, numPoints);
    }
    
    /**
     * Gets the number of points in the polygon.
     *
     * @return The number of points
     */
    public int getNumPoints() {
        return numPoints;
    }
    
    /**
     * Adds a point to the polygon.
     *
     * @param pointX The x-coordinate of the point relative to the reference point
     * @param pointY The y-coordinate of the point relative to the reference point
     */
    public void addPoint(double pointX, double pointY) {
        // Create new arrays with increased size
        double[] newPointsX = new double[numPoints + 1];
        double[] newPointsY = new double[numPoints + 1];
        
        // Copy existing points
        System.arraycopy(pointsX, 0, newPointsX, 0, numPoints);
        System.arraycopy(pointsY, 0, newPointsY, 0, numPoints);
        
        // Add the new point
        newPointsX[numPoints] = pointX;
        newPointsY[numPoints] = pointY;
        
        // Update the arrays and count
        pointsX = newPointsX;
        pointsY = newPointsY;
        numPoints++;
    }
}
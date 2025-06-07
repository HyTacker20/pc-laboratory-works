package com.shapeeditor.model;

import java.util.Arrays;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

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
            // Calculate the center of the polygon points
            double centerX = 0;
            double centerY = 0;
            for (int i = 0; i < numPoints; i++) {
                centerX += pointsX[i];
                centerY += pointsY[i];
            }
            centerX /= numPoints;
            centerY /= numPoints;
            
            // Scale each point relative to the center
            for (int i = 0; i < numPoints; i++) {
                // Calculate point position relative to the center
                double relX = pointsX[i] - centerX;
                double relY = pointsY[i] - centerY;
                
                // Scale the point and update its position
                pointsX[i] = centerX + relX * factor;
                pointsY[i] = centerY + relY * factor;
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
    
    /**
     * Scales the polygon by the specified factor.
     * This is similar to resize but keeps the original method for compatibility.
     *
     * @param factor The factor to scale by (e.g., 2.0 doubles the size)
     */
    public void scale(double factor) {
        if (factor <= 0) {
            return;
        }
        resize(factor);
    }
    
    /**
     * Calculates the centroid (geometric center) of the polygon.
     * The centroid is calculated as the average of all vertex coordinates.
     *
     * @return An array containing the x and y coordinates of the centroid relative to the polygon's position
     */
    public double[] getCentroid() {
        double centerX = 0;
        double centerY = 0;
        
        for (int i = 0; i < numPoints; i++) {
            centerX += pointsX[i];
            centerY += pointsY[i];
        }
        
        centerX /= numPoints;
        centerY /= numPoints;
        
        return new double[] {centerX, centerY};
    }
    /**
     * Checks if a line segment from (x1,y1) to (x2,y2) intersects with any of the polygon's edges.
     * This is used to prevent creating self-intersecting polygons.
     *
     * @param x1 The x-coordinate of the first point
     * @param y1 The y-coordinate of the first point
     * @param x2 The x-coordinate of the second point
     * @param y2 The y-coordinate of the second point
     * @return true if the line segment intersects with any edge, false otherwise
     */
    public boolean lineIntersectsWithEdges(double x1, double y1, double x2, double y2) {
        // If the polygon has fewer than 2 points, there can't be any intersections
        if (numPoints < 2) {
            return false;
        }
        
        // Check each edge of the polygon
        for (int i = 0; i < numPoints; i++) {
            int nextIndex = (i + 1) % numPoints;
            
            // Skip if one of the endpoints of the new line is the same as the start of the current edge
            // This prevents false positives when adding consecutive points
            if ((x1 == x + pointsX[i] && y1 == y + pointsY[i]) ||
                (x2 == x + pointsX[i] && y2 == y + pointsY[i])) {
                continue;
            }
            
            // Skip if one of the endpoints of the new line is the same as the end of the current edge
            if ((x1 == x + pointsX[nextIndex] && y1 == y + pointsY[nextIndex]) ||
                (x2 == x + pointsX[nextIndex] && y2 == y + pointsY[nextIndex])) {
                continue;
            }
            
            // Check if the line segments intersect
            if (doLineSegmentsIntersect(
                    x1, y1, x2, y2,
                    x + pointsX[i], y + pointsY[i],
                    x + pointsX[nextIndex], y + pointsY[nextIndex])) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Determines if two line segments intersect.
     * Uses the algorithm described in "Computational Geometry in C" by Joseph O'Rourke.
     *
     * @param x1 The x-coordinate of the first point of the first line segment
     * @param y1 The y-coordinate of the first point of the first line segment
     * @param x2 The x-coordinate of the second point of the first line segment
     * @param y2 The y-coordinate of the second point of the first line segment
     * @param x3 The x-coordinate of the first point of the second line segment
     * @param y3 The y-coordinate of the first point of the second line segment
     * @param x4 The x-coordinate of the second point of the second line segment
     * @param y4 The y-coordinate of the second point of the second line segment
     * @return true if the line segments intersect, false otherwise
     */
    private boolean doLineSegmentsIntersect(
            double x1, double y1, double x2, double y2,
            double x3, double y3, double x4, double y4) {
        
        // Calculate the direction of the vectors
        double d1x = x2 - x1;
        double d1y = y2 - y1;
        double d2x = x4 - x3;
        double d2y = y4 - y3;
        
        // Calculate the determinant
        double det = d1x * d2y - d1y * d2x;
        
        // If det is zero, the lines are parallel
        if (Math.abs(det) < 1e-10) {
            return false;
        }
        
        // Calculate the parameters of the intersection point
        double dx = x3 - x1;
        double dy = y3 - y1;
        double t = (dx * d2y - dy * d2x) / det;
        double u = (dx * d1y - dy * d1x) / det;
        
        // Check if the intersection point is within both line segments
        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }
}
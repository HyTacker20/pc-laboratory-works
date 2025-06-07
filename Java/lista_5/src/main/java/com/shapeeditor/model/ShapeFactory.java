package com.shapeeditor.model;

import javafx.scene.paint.Color;

/**
 * Factory class for creating different types of shapes.
 * This class provides static methods to create various shapes with default or specified parameters.
 */
public class ShapeFactory {
    
    /**
     * Creates a circle with the specified parameters.
     *
     * @param centerX The x-coordinate of the circle's center
     * @param centerY The y-coordinate of the circle's center
     * @param radius The radius of the circle
     * @param fillColor The fill color of the circle
     * @return A new Circle instance
     */
    public static Circle createCircle(double centerX, double centerY, double radius, Color fillColor) {
        return new Circle(centerX, centerY, radius, fillColor);
    }
    
    /**
     * Creates a circle with default parameters.
     *
     * @param centerX The x-coordinate of the circle's center
     * @param centerY The y-coordinate of the circle's center
     * @return A new Circle instance with default radius and color
     */
    public static Circle createDefaultCircle(double centerX, double centerY) {
        return new Circle(centerX, centerY, 50, Color.BLUE);
    }
    
    /**
     * Creates a rectangle with the specified parameters.
     *
     * @param x The x-coordinate of the rectangle's top-left corner
     * @param y The y-coordinate of the rectangle's top-left corner
     * @param width The width of the rectangle
     * @param height The height of the rectangle
     * @param fillColor The fill color of the rectangle
     * @return A new Rectangle instance
     */
    public static Rectangle createRectangle(double x, double y, double width, double height, Color fillColor) {
        return new Rectangle(x, y, width, height, fillColor);
    }
    
    /**
     * Creates a rectangle with default parameters.
     *
     * @param x The x-coordinate of the rectangle's top-left corner
     * @param y The y-coordinate of the rectangle's top-left corner
     * @return A new Rectangle instance with default width, height, and color
     */
    public static Rectangle createDefaultRectangle(double x, double y) {
        return new Rectangle(x, y, 100, 60, Color.GREEN);
    }
    
    /**
     * Creates a regular polygon with the specified parameters.
     *
     * @param centerX The x-coordinate of the polygon's center
     * @param centerY The y-coordinate of the polygon's center
     * @param numSides The number of sides of the regular polygon
     * @param radius The distance from the center to each vertex
     * @param fillColor The fill color of the polygon
     * @return A new Polygon instance
     * @throws IllegalArgumentException if numSides is less than 3
     */
    public static Polygon createRegularPolygon(double centerX, double centerY, int numSides, double radius, Color fillColor) {
        if (numSides < 3) {
            throw new IllegalArgumentException("A polygon must have at least 3 sides");
        }
        
        double[] pointsX = new double[numSides];
        double[] pointsY = new double[numSides];
        
        // Calculate the points of the regular polygon
        for (int i = 0; i < numSides; i++) {
            double angle = 2 * Math.PI * i / numSides;
            pointsX[i] = radius * Math.cos(angle);
            pointsY[i] = radius * Math.sin(angle);
        }
        
        return new Polygon(centerX, centerY, pointsX, pointsY, fillColor);
    }
    
    /**
     * Creates a triangle with the specified parameters.
     *
     * @param x1 The x-coordinate of the first point
     * @param y1 The y-coordinate of the first point
     * @param x2 The x-coordinate of the second point
     * @param y2 The y-coordinate of the second point
     * @param x3 The x-coordinate of the third point
     * @param y3 The y-coordinate of the third point
     * @param fillColor The fill color of the triangle
     * @return A new Polygon instance representing a triangle
     */
    public static Polygon createTriangle(double x1, double y1, double x2, double y2, double x3, double y3, Color fillColor) {
        double centerX = (x1 + x2 + x3) / 3;
        double centerY = (y1 + y2 + y3) / 3;
        
        double[] pointsX = {x1 - centerX, x2 - centerX, x3 - centerX};
        double[] pointsY = {y1 - centerY, y2 - centerY, y3 - centerY};
        
        return new Polygon(centerX, centerY, pointsX, pointsY, fillColor);
    }
    
    /**
     * Creates a default equilateral triangle.
     *
     * @param centerX The x-coordinate of the triangle's center
     * @param centerY The y-coordinate of the triangle's center
     * @return A new Polygon instance representing an equilateral triangle with default parameters
     */
    public static Polygon createDefaultTriangle(double centerX, double centerY) {
        return createRegularPolygon(centerX, centerY, 3, 50, Color.RED);
    }
    
    /**
     * Creates a star shape with the specified parameters.
     *
     * @param centerX The x-coordinate of the star's center
     * @param centerY The y-coordinate of the star's center
     * @param numPoints The number of points of the star
     * @param outerRadius The distance from the center to the outer points
     * @param innerRadius The distance from the center to the inner points
     * @param fillColor The fill color of the star
     * @return A new Polygon instance representing a star
     * @throws IllegalArgumentException if numPoints is less than 3
     */
    public static Polygon createStar(double centerX, double centerY, int numPoints, double outerRadius, 
                                    double innerRadius, Color fillColor) {
        if (numPoints < 3) {
            throw new IllegalArgumentException("A star must have at least 3 points");
        }
        
        int totalPoints = numPoints * 2;
        double[] pointsX = new double[totalPoints];
        double[] pointsY = new double[totalPoints];
        
        // Calculate the points of the star
        for (int i = 0; i < totalPoints; i++) {
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double angle = Math.PI * i / numPoints;
            pointsX[i] = radius * Math.cos(angle);
            pointsY[i] = radius * Math.sin(angle);
        }
        
        return new Polygon(centerX, centerY, pointsX, pointsY, fillColor);
    }
}
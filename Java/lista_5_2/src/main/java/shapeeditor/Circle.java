package shapeeditor;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * This is my circle shape class.
 * It extends the Shape class and adds all the circle-specific stuff.
 *
 * @author Andrii Hermak
 * @version 1.0
 */
public class Circle extends Shape {

    /** Serial version ID for serialization */
    private static final long serialVersionUID = 1L;
    /** Radius of the circle */
    private double radius;
    
    /** How close you need to click to grab a control point */
    private static final double CONTROL_POINT_TOLERANCE = 10.0;
    
    /** Number of control points around the circle */
    private static final int CONTROL_POINTS_COUNT = 8;

    /**
     * Constructor for making a new circle
     *
     * @param centerX X position of circle center
     * @param centerY Y position of circle center
     * @param radius how big the circle is
     */
    public Circle(double centerX, double centerY, double radius) {
        super(centerX, centerY);
        this.radius = radius;
    }

    /**
     * Draws the circle on the screen
     *
     * @param gc graphics context to draw on
     */
    @Override
    public void draw(GraphicsContext gc) {
        applyTransformation(gc);

        // Draw fill
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Draw border
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        restoreTransformation(gc);

        // Draw selection handles
        drawSelectionHandles(gc);
    }

    /**
     * Checks if you clicked inside the circle
     *
     * @param x X coordinate where you clicked
     * @param y Y coordinate where you clicked
     * @return true if you clicked inside, false if you missed
     */
    @Override
    public boolean contains(double x, double y) {
        // Calculate distance from point to circle center
        double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));

        // Point is inside the circle if distance is less than radius
        return distance <= radius;
    }

    /**
     * Makes the circle bigger or smaller
     *
     * @param scaleFactor how much to resize
     */
    @Override
    public void resize(double scaleFactor) {
        radius *= scaleFactor;

        // Prevent radius from becoming too small
        if (radius < 5) {
            radius = 5;
        }
    }

    /**
     * Gets the radius of the circle.
     *
     * @return how big the circle is
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Sets the radius of the circle.
     *
     * @param radius new size for the circle
     */
    public void setRadius(double radius) {
        if (radius > 0) {
            this.radius = radius;
        }
    }
    
    /**
     * Figures out where each control point is located.
     * I put 8 control points around the circle every 45 degrees.
     *
     * @param index which control point (0-7)
     * @return the x,y position as an array [x, y]
     */
    private double[] getControlPointCoordinates(int index) {
        double angle = Math.toRadians(index * 45); // 8 points every 45 degrees
        
        double[] coordinates = new double[2];
        coordinates[0] = centerX + radius * Math.cos(angle);
        coordinates[1] = centerY + radius * Math.sin(angle);
        
        return coordinates;
    }
    
    /**
     * Checks if you clicked on one of the control points.
     * The circle has 8 control points around its edge.
     *
     * @param x X coordinate where you clicked
     * @param y Y coordinate where you clicked
     * @return which control point you clicked, or -1 if none
     */
    @Override
    public int getControlPointAt(double x, double y) {
        if (!selected || !pointEditMode) {
            return -1;
        }
        
        // Check each control point
        for (int i = 0; i < CONTROL_POINTS_COUNT; i++) {
            double[] coordinates = getControlPointCoordinates(i);
            double distance = Math.sqrt(Math.pow(x - coordinates[0], 2) + Math.pow(y - coordinates[1], 2));
            
            if (distance <= CONTROL_POINT_TOLERANCE) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Handles dragging a control point to resize the circle.
     * When you drag a control point, it changes the circle's size.
     *
     * @param pointIndex which control point you're moving
     * @param deltaX how far to move horizontally
     * @param deltaY how far to move vertically
     */
    @Override
    public void moveControlPoint(int pointIndex, double deltaX, double deltaY) {
        if (pointIndex < 0 || pointIndex >= CONTROL_POINTS_COUNT) {
            return;
        }
        
        // Get control point coordinates
        double[] coordinates = getControlPointCoordinates(pointIndex);
        
        // Calculate new distance from center (new radius)
        double newX = coordinates[0] + deltaX;
        double newY = coordinates[1] + deltaY;
        double newRadius = Math.sqrt(Math.pow(newX - centerX, 2) + Math.pow(newY - centerY, 2));
        
        // Update radius
        if (newRadius >= 5) {
            radius = newRadius;
        }
    }
    
    /**
     * Draws the little square handles around the circle.
     * These are the points you can grab to resize the circle.
     * The selected one turns red so you know which one you're moving.
     *
     * @param gc graphics context to draw on
     */
    @Override
    public void drawControlPoints(GraphicsContext gc) {
        double handleSize = 8;
        
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
        
        // Draw control points
        for (int i = 0; i < CONTROL_POINTS_COUNT; i++) {
            double[] coordinates = getControlPointCoordinates(i);
            
            // Highlight selected point
            if (i == selectedPointIndex) {
                gc.setFill(Color.RED);
            } else {
                gc.setFill(Color.WHITE);
            }
            
            gc.fillRect(coordinates[0] - handleSize/2, coordinates[1] - handleSize/2, handleSize, handleSize);
            gc.strokeRect(coordinates[0] - handleSize/2, coordinates[1] - handleSize/2, handleSize, handleSize);
        }
    }
}
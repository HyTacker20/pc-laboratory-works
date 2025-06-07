package shapeeditor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * This is my polygon shape class.
 * It can make regular polygons with any number of sides.
 * You can also drag the vertices to make custom shapes.
 *
 * @author Andrii Hermak
 * @version 1.0
 */
public class Polygon extends Shape {

    /** Serial version ID for serialization */
    private static final long serialVersionUID = 1L;
    /** Number of sides in the polygon */
    private int sides;
    /** Distance from center to vertices in a regular polygon */
    private double radius;
    /** X coordinates of polygon vertices */
    private transient List<Double> xPoints;
    /** Y coordinates of polygon vertices */
    private transient List<Double> yPoints;
    /** Whether vertices have been manually modified */
    private boolean pointsModified = false;
    /** Custom distances from center to each vertex */
    private double[] modifiedRadii;
    /** Custom angles for each vertex */
    private double[] modifiedAngles;
    
    /** How close you need to click to grab a control point */
    private static final double CONTROL_POINT_TOLERANCE = 10.0;

    /**
     * Constructor for making a new polygon
     *
     * @param centerX X position of polygon center
     * @param centerY Y position of polygon center
     * @param sides how many sides the polygon has
     * @param radius how big the polygon is
     */
    public Polygon(double centerX, double centerY, int sides, double radius) {
        super(centerX, centerY);
        this.sides = sides < 3 ? 3 : sides; // Minimum 3 sides
        this.radius = radius;
        initializeModifiedArrays();
        calculatePoints();
    }
    
    /**
     * Sets up arrays to remember custom vertex positions.
     * This is for when you drag vertices to make custom shapes.
     */
    private void initializeModifiedArrays() {
        modifiedRadii = new double[sides];
        modifiedAngles = new double[sides];
        
        // Initialize with default values
        for (int i = 0; i < sides; i++) {
            modifiedRadii[i] = radius;
            modifiedAngles[i] = Math.toRadians(i * 360.0 / sides);
        }
    }

    /**
     * Figures out where all the corners of the polygon should be.
     * Takes into account the center, size, number of sides, and
     * any custom changes you made by dragging vertices.
     */
    private void calculatePoints() {
        xPoints = new ArrayList<>(sides);
        yPoints = new ArrayList<>(sides);

        // Check if arrays are initialized and have correct size
        if (modifiedRadii == null || modifiedRadii.length != sides ||
            modifiedAngles == null || modifiedAngles.length != sides) {
            initializeModifiedArrays();
        }

        for (int i = 0; i < sides; i++) {
            double currentRadius, currentAngle;
            
            if (pointsModified) {
                // Use modified values
                currentRadius = modifiedRadii[i];
                currentAngle = modifiedAngles[i];
            } else {
                // Use default values for regular polygon
                currentRadius = radius;
                currentAngle = Math.toRadians(i * 360.0 / sides);
            }

            // Calculate vertex coordinates
            double x = centerX + currentRadius * Math.cos(currentAngle);
            double y = centerY + currentRadius * Math.sin(currentAngle);

            xPoints.add(x);
            yPoints.add(y);
        }
    }

    /**
     * Gets an array of X coordinates for all polygon vertices.
     *
     * @return array of all the X positions of the corners
     */
    private double[] getXPointsArray() {
        double[] result = new double[xPoints.size()];
        for (int i = 0; i < xPoints.size(); i++) {
            result[i] = xPoints.get(i);
        }
        return result;
    }

    /**
     * Gets an array of Y coordinates for all polygon vertices.
     *
     * @return array of all the Y positions of the corners
     */
    private double[] getYPointsArray() {
        double[] result = new double[yPoints.size()];
        for (int i = 0; i < yPoints.size(); i++) {
            result[i] = yPoints.get(i);
        }
        return result;
    }

    /**
     * Draws the polygon on the screen
     *
     * @param gc graphics context to draw on
     */
    @Override
    public void draw(GraphicsContext gc) {
        // Recalculate points if needed
        if (xPoints == null || yPoints == null) {
            calculatePoints();
        }

        applyTransformation(gc);

        // Draw fill
        gc.fillPolygon(getXPointsArray(), getYPointsArray(), sides);

        // Draw border
        gc.strokePolygon(getXPointsArray(), getYPointsArray(), sides);

        restoreTransformation(gc);

        // Draw selection handles
        drawSelectionHandles(gc);
    }

    /**
     * Checks if you clicked inside the polygon
     *
     * @param x X coordinate where you clicked
     * @param y Y coordinate where you clicked
     * @return true if you clicked inside, false if you missed
     */
    @Override
    public boolean contains(double x, double y) {
        // Recalculate points if needed
        if (xPoints == null || yPoints == null) {
            calculatePoints();
        }

        // Handle rotation
        if (rotation != 0) {
            // Translate coordinate system to polygon center
            double translatedX = x - centerX;
            double translatedY = y - centerY;

            // Rotate point in opposite direction
            double angle = Math.toRadians(-rotation);
            double rotatedX = translatedX * Math.cos(angle) - translatedY * Math.sin(angle);
            double rotatedY = translatedX * Math.sin(angle) + translatedY * Math.cos(angle);

            // Translate back
            x = rotatedX + centerX;
            y = rotatedY + centerY;
        }

        // Ray casting algorithm (point in polygon)
        boolean inside = false;
        for (int i = 0, j = sides - 1; i < sides; j = i++) {
            double xi = xPoints.get(i);
            double yi = yPoints.get(i);
            double xj = xPoints.get(j);
            double yj = yPoints.get(j);

            boolean intersect = ((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) {
                inside = !inside;
            }
        }

        return inside;
    }

    /**
     * Makes the polygon bigger or smaller
     *
     * @param scaleFactor how much to resize
     */
    @Override
    public void resize(double scaleFactor) {
        radius *= scaleFactor;

        // Prevent radius from becoming too small
        if (radius < 10) {
            radius = 10;
        }

        // Recalculate points
        calculatePoints();
    }

    /**
     * Moves the whole polygon
     *
     * @param deltaX how far to move horizontally
     * @param deltaY how far to move vertically
     */
    @Override
    public void move(double deltaX, double deltaY) {
        super.move(deltaX, deltaY);

        // Recalculate points
        calculatePoints();
    }

    /**
     * Rotates the polygon
     *
     * @param degrees how many degrees to rotate
     */
    @Override
    public void rotate(double degrees) {
        super.rotate(degrees);

        // Vertex coordinates don't need to be updated,
        // as rotation is handled by graphics context transformation
    }

    /**
     * Gets the number of sides in the polygon.
     *
     * @return how many sides the polygon has
     */
    public int getSides() {
        return sides;
    }

    /**
     * Sets the number of sides in the polygon.
     *
     * @param sides changes how many sides the polygon has
     */
    public void setSides(int sides) {
        if (sides >= 3) {
            // Preserve modified points when changing sides
            boolean wasModified = pointsModified;
            double[] oldRadii = modifiedRadii;
            double[] oldAngles = modifiedAngles;
            
            this.sides = sides;
            initializeModifiedArrays();
            
            if (wasModified && oldRadii != null && oldAngles != null) {
                // Copy modified values for common points
                int minSides = Math.min(sides, oldRadii.length);
                for (int i = 0; i < minSides; i++) {
                    modifiedRadii[i] = oldRadii[i];
                    modifiedAngles[i] = oldAngles[i];
                }
                pointsModified = true;
            }
            
            calculatePoints();
        }
    }

    /**
     * Gets the radius of the polygon.
     *
     * @return how big the polygon is
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Sets the radius of the polygon.
     *
     * @param radius changes how big the polygon is
     */
    public void setRadius(double radius) {
        if (radius > 0) {
            this.radius = radius;
            
            if (!pointsModified) {
                // If points weren't modified, simply recalculate
                calculatePoints();
            } else {
                // If points were modified, preserve proportions
                double scaleFactor = radius / this.radius;
                for (int i = 0; i < sides; i++) {
                    modifiedRadii[i] *= scaleFactor;
                }
                this.radius = radius;
                calculatePoints();
            }
        }
    }
    
    /**
     * Checks if you clicked on one of the polygon's corners.
     * Tests each corner to see if you clicked close enough to grab it.
     *
     * @param x X coordinate where you clicked
     * @param y Y coordinate where you clicked
     * @return which corner you clicked, or -1 if none
     */
    @Override
    public int getControlPointAt(double x, double y) {
        if (!selected || !pointEditMode || xPoints == null || yPoints == null) {
            return -1;
        }
        
        // Check each vertex
        for (int i = 0; i < sides; i++) {
            double distance = Math.sqrt(Math.pow(x - xPoints.get(i), 2) + Math.pow(y - yPoints.get(i), 2));
            
            if (distance <= CONTROL_POINT_TOLERANCE) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Handles dragging a corner of the polygon.
     * This lets you make custom shapes by moving the corners around.
     * Once you move corners, it's not a regular polygon anymore.
     *
     * @param pointIndex which corner you're moving
     * @param deltaX how far to move horizontally
     * @param deltaY how far to move vertically
     */
    @Override
    public void moveControlPoint(int pointIndex, double deltaX, double deltaY) {
        if (pointIndex < 0 || pointIndex >= sides || xPoints == null || yPoints == null) {
            return;
        }
        
        // Calculate new vertex position
        double newX = xPoints.get(pointIndex) + deltaX;
        double newY = yPoints.get(pointIndex) + deltaY;
        
        // Calculate new radius and angle for this vertex
        double dx = newX - centerX;
        double dy = newY - centerY;
        double newRadius = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.atan2(dy, dx);
        
        // Update vertex coordinates
        xPoints.set(pointIndex, centerX + newRadius * Math.cos(angle));
        yPoints.set(pointIndex, centerY + newRadius * Math.sin(angle));
        
        // Save modified values
        pointsModified = true;
        modifiedRadii[pointIndex] = newRadius;
        modifiedAngles[pointIndex] = angle;
    }
    
    /**
     * Draws the little square handles at each corner of the polygon.
     * These are the points you can grab to reshape the polygon.
     * The selected one turns red so you know which one you're moving.
     *
     * @param gc graphics context to draw on
     */
    @Override
    public void drawControlPoints(GraphicsContext gc) {
        if (xPoints == null || yPoints == null) {
            calculatePoints();
        }
        
        double handleSize = 8;
        
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
        
        // Draw control points (vertices)
        for (int i = 0; i < sides; i++) {
            // Highlight selected point
            if (i == selectedPointIndex) {
                gc.setFill(Color.RED);
            } else {
                gc.setFill(Color.WHITE);
            }
            
            gc.fillRect(xPoints.get(i) - handleSize/2, yPoints.get(i) - handleSize/2, handleSize, handleSize);
            gc.strokeRect(xPoints.get(i) - handleSize/2, yPoints.get(i) - handleSize/2, handleSize, handleSize);
        }
    }
    
    /**
     * Special method for saving polygons to a file.
     * Needs to remember if you customized the shape by moving corners.
     *
     * @param out output stream to write to
     * @throws IOException if something goes wrong with saving
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        
        // Save information about modified points
        out.writeBoolean(pointsModified);
        
        if (pointsModified) {
            // Save modified radii and angles
            for (int i = 0; i < sides; i++) {
                out.writeDouble(modifiedRadii[i]);
                out.writeDouble(modifiedAngles[i]);
            }
        }
    }
    
    /**
     * Special method for loading polygons from a file.
     * Restores any custom shape you made by moving corners.
     *
     * @param in input stream to read from
     * @throws IOException if something goes wrong with loading
     * @throws ClassNotFoundException if it can't find the right class
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        // Read information about modified points
        pointsModified = in.readBoolean();
        
        // Initialize arrays if not created yet
        if (modifiedRadii == null || modifiedRadii.length != sides) {
            modifiedRadii = new double[sides];
        }
        
        if (modifiedAngles == null || modifiedAngles.length != sides) {
            modifiedAngles = new double[sides];
        }
        
        if (pointsModified) {
            // Read modified radii and angles
            for (int i = 0; i < sides; i++) {
                modifiedRadii[i] = in.readDouble();
                modifiedAngles[i] = in.readDouble();
            }
        } else {
            // Initialize with default values
            for (int i = 0; i < sides; i++) {
                modifiedRadii[i] = radius;
                modifiedAngles[i] = Math.toRadians(i * 360.0 / sides);
            }
        }
        
        // Calculate points based on read data
        calculatePoints();
    }
}
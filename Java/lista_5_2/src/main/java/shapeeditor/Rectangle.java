package shapeeditor;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * This is my rectangle shape class.
 * It extends the Shape class and adds all the rectangle-specific stuff.
 *
 * @author Andrii Hermak
 * @version 1.0
 */
public class Rectangle extends Shape {

    /** Serial version ID for serialization */
    private static final long serialVersionUID = 1L;
    /** Width of the rectangle */
    private double width;
    /** Height of the rectangle */
    private double height;
    
    /** How close you need to click to grab a control point */
    private static final double CONTROL_POINT_TOLERANCE = 10.0;

    /**
     * Constructor for making a new rectangle
     *
     * @param x X position of the top-left corner
     * @param y Y position of the top-left corner
     * @param width how wide the rectangle is
     * @param height how tall the rectangle is
     */
    public Rectangle(double x, double y, double width, double height) {
        // Pass the center of the rectangle to the base class constructor
        super(x + width / 2, y + height / 2);
        this.width = width;
        this.height = height;
    }

    /**
     * Draws the rectangle on the screen
     *
     * @param gc graphics context to draw on
     */
    @Override
    public void draw(GraphicsContext gc) {
        applyTransformation(gc);

        // Calculate position of the rectangle's top-left corner
        double x = centerX - width / 2;
        double y = centerY - height / 2;

        // Draw fill
        gc.fillRect(x, y, width, height);

        // Draw border
        gc.strokeRect(x, y, width, height);

        restoreTransformation(gc);

        // Draw selection handles
        drawSelectionHandles(gc);
    }

    /**
     * Checks if you clicked inside the rectangle
     *
     * @param x X coordinate where you clicked
     * @param y Y coordinate where you clicked
     * @return true if you clicked inside, false if you missed
     */
    @Override
    public boolean contains(double x, double y) {
        // If the rectangle is rotated, we need to transform the point coordinates
        if (rotation != 0) {
            // Translate coordinate system origin to rectangle center
            double translatedX = x - centerX;
            double translatedY = y - centerY;

            // Rotate point in the opposite direction
            double angle = Math.toRadians(-rotation);
            double rotatedX = translatedX * Math.cos(angle) - translatedY * Math.sin(angle);
            double rotatedY = translatedX * Math.sin(angle) + translatedY * Math.cos(angle);

            // Translate back
            x = rotatedX + centerX;
            y = rotatedY + centerY;
        }

        // Calculate rectangle boundaries
        double left = centerX - width / 2;
        double right = centerX + width / 2;
        double top = centerY - height / 2;
        double bottom = centerY + height / 2;

        // Point is inside the rectangle if its coordinates are within boundaries
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    /**
     * Makes the rectangle bigger or smaller
     *
     * @param scaleFactor how much to resize
     */
    @Override
    public void resize(double scaleFactor) {
        width *= scaleFactor;
        height *= scaleFactor;

        // Prevent dimensions from becoming too small
        if (width < 10) {
            width = 10;
        }
        if (height < 10) {
            height = 10;
        }
    }

    /**
     * Gets the width of the rectangle.
     *
     * @return how wide the rectangle is
     */
    public double getWidth() {
        return width;
    }

    /**
     * Sets the width of the rectangle.
     *
     * @param width new width for the rectangle
     */
    public void setWidth(double width) {
        if (width > 0) {
            this.width = width;
        }
    }

    /**
     * Gets the height of the rectangle.
     *
     * @return how tall the rectangle is
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the height of the rectangle.
     *
     * @param height new height for the rectangle
     */
    public void setHeight(double height) {
        if (height > 0) {
            this.height = height;
        }
    }
    
    /**
     * Figures out where each control point is located.
     * I put 8 control points - one at each corner and one in the middle of each edge.
     * These let you resize and reshape the rectangle.
     *
     * @param index which control point (0-7)
     * @return the x,y position as an array [x, y]
     */
    private double[] getControlPointCoordinates(int index) {
        double x = centerX - width / 2;
        double y = centerY - height / 2;
        
        double[] coordinates = new double[2];
        
        switch (index) {
            case 0: // Top-left corner
                coordinates[0] = x;
                coordinates[1] = y;
                break;
            case 1: // Middle of top edge
                coordinates[0] = x + width / 2;
                coordinates[1] = y;
                break;
            case 2: // Top-right corner
                coordinates[0] = x + width;
                coordinates[1] = y;
                break;
            case 3: // Middle of right edge
                coordinates[0] = x + width;
                coordinates[1] = y + height / 2;
                break;
            case 4: // Bottom-right corner
                coordinates[0] = x + width;
                coordinates[1] = y + height;
                break;
            case 5: // Middle of bottom edge
                coordinates[0] = x + width / 2;
                coordinates[1] = y + height;
                break;
            case 6: // Bottom-left corner
                coordinates[0] = x;
                coordinates[1] = y + height;
                break;
            case 7: // Middle of left edge
                coordinates[0] = x;
                coordinates[1] = y + height / 2;
                break;
        }
        
        // Account for rotation
        if (rotation != 0) {
            double dx = coordinates[0] - centerX;
            double dy = coordinates[1] - centerY;
            double angle = Math.toRadians(rotation);
            
            coordinates[0] = centerX + dx * Math.cos(angle) - dy * Math.sin(angle);
            coordinates[1] = centerY + dx * Math.sin(angle) + dy * Math.cos(angle);
        }
        
        return coordinates;
    }
    
    /**
     * Checks if you clicked on one of the control points.
     * Tests all 8 control points to see if you clicked close enough to grab one.
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
        for (int i = 0; i < 8; i++) {
            double[] coordinates = getControlPointCoordinates(i);
            double distance = Math.sqrt(Math.pow(x - coordinates[0], 2) + Math.pow(y - coordinates[1], 2));
            
            if (distance <= CONTROL_POINT_TOLERANCE) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Handles dragging a control point to resize the rectangle.
     * Different control points do different things:
     * - Corner points change both width and height
     * - Edge points only change one dimension
     *
     * @param pointIndex which control point you're moving (0-7)
     * @param deltaX how far to move horizontally
     * @param deltaY how far to move vertically
     */
    @Override
    public void moveControlPoint(int pointIndex, double deltaX, double deltaY) {
        if (pointIndex < 0 || pointIndex > 7) {
            return;
        }
        
        // If shape is rotated, transform the movement
        if (rotation != 0) {
            double angle = Math.toRadians(-rotation);
            double rotatedDeltaX = deltaX * Math.cos(angle) - deltaY * Math.sin(angle);
            double rotatedDeltaY = deltaX * Math.sin(angle) + deltaY * Math.cos(angle);
            deltaX = rotatedDeltaX;
            deltaY = rotatedDeltaY;
        }
        
        switch (pointIndex) {
            case 0: // Top-left corner
                width -= deltaX;
                height -= deltaY;
                centerX += deltaX / 2;
                centerY += deltaY / 2;
                break;
            case 1: // Middle of top edge
                height -= deltaY;
                centerY += deltaY / 2;
                break;
            case 2: // Top-right corner
                width += deltaX;
                height -= deltaY;
                centerX += deltaX / 2;
                centerY += deltaY / 2;
                break;
            case 3: // Middle of right edge
                width += deltaX;
                centerX += deltaX / 2;
                break;
            case 4: // Bottom-right corner
                width += deltaX;
                height += deltaY;
                centerX += deltaX / 2;
                centerY += deltaY / 2;
                break;
            case 5: // Middle of bottom edge
                height += deltaY;
                centerY += deltaY / 2;
                break;
            case 6: // Bottom-left corner
                width -= deltaX;
                height += deltaY;
                centerX += deltaX / 2;
                centerY += deltaY / 2;
                break;
            case 7: // Middle of left edge
                width -= deltaX;
                centerX += deltaX / 2;
                break;
        }
        
        // Prevent dimensions from becoming too small
        if (width < 10) {
            double diff = 10 - width;
            width = 10;
            if (pointIndex == 0 || pointIndex == 6 || pointIndex == 7) {
                centerX -= diff / 2;
            } else if (pointIndex == 2 || pointIndex == 3 || pointIndex == 4) {
                centerX += diff / 2;
            }
        }
        
        if (height < 10) {
            double diff = 10 - height;
            height = 10;
            if (pointIndex == 0 || pointIndex == 1 || pointIndex == 2) {
                centerY -= diff / 2;
            } else if (pointIndex == 4 || pointIndex == 5 || pointIndex == 6) {
                centerY += diff / 2;
            }
        }
    }
    
    /**
     * Draws the little square handles on the rectangle.
     * These are the points you can grab to resize the rectangle.
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
        for (int i = 0; i < 8; i++) {
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
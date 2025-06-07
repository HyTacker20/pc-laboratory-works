package shapeeditor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * This is the parent class for all shapes in the editor.
 *
 * @author Andrii Hermak
 * @version 1.0
 */
public abstract class Shape implements Serializable {

    /** Serial version ID for serialization */
    private static final long serialVersionUID = 1L;

    /** X coordinate of the shape's center */
    protected double centerX;
    /** Y coordinate of the shape's center */
    protected double centerY;
    /** Rotation angle in degrees */
    protected double rotation;
    /** Fill color of the shape */
    protected transient javafx.scene.paint.Color fillColor;
    /** Border color of the shape */
    protected transient javafx.scene.paint.Color strokeColor;
    /** Border width of the shape */
    protected double strokeWidth;
    /** Whether the shape is currently selected */
    protected boolean selected;
    /** Whether point edit mode is active */
    protected boolean pointEditMode;
    /** Index of the selected control point, -1 if none */
    protected int selectedPointIndex = -1;

    /**
     * Constructor that takes the center position
     *
     * @param centerX X position of shape center
     * @param centerY Y position of shape center
     */
    public Shape(double centerX, double centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.rotation = 0.0;
        this.fillColor = Color.LIGHTBLUE;
        this.strokeColor = Color.BLUE;
        this.strokeWidth = 2.0;
        this.selected = false;
        this.pointEditMode = false;
    }

    /**
     * Every shape needs to know how to draw itself
     *
     * @param gc graphics context to draw on
     */
    public abstract void draw(GraphicsContext gc);

    /**
     * Checks if you clicked inside the shape
     *
     * @param x X coordinate where you clicked
     * @param y Y coordinate where you clicked
     * @return true if you clicked inside, false if you missed
     */
    public abstract boolean contains(double x, double y);
    
    /**
     * Checks if you clicked near one of the control points.
     * Control points are the little squares that let you
     * reshape the object by dragging them.
     *
     * @param x X coordinate where you clicked
     * @param y Y coordinate where you clicked
     * @return which control point you clicked, or -1 if none
     */
    public abstract int getControlPointAt(double x, double y);
    
    /**
     * Moves a control point when you drag it.
     * Each shape type handles this differently.
     *
     * @param pointIndex which control point to move
     * @param deltaX how far to move horizontally
     * @param deltaY how far to move vertically
     */
    public abstract void moveControlPoint(int pointIndex, double deltaX, double deltaY);
    
    /**
     * Draws the little square handles for editing the shape.
     * These are the points you can grab and drag to change the shape.
     *
     * @param gc graphics context to draw on
     */
    public abstract void drawControlPoints(GraphicsContext gc);

    /**
     * Moves the shape by the specified amount.
     *
     * @param deltaX movement along X axis
     * @param deltaY movement along Y axis
     */
    public void move(double deltaX, double deltaY) {
        centerX += deltaX;
        centerY += deltaY;
    }

    /**
     * Makes the shape bigger or smaller.
     * Use values &gt; 1.0 to make it bigger, &lt; 1.0 to make it smaller.
     *
     * @param scaleFactor how much to resize (e.g., 1.1 = 10% bigger)
     */
    public abstract void resize(double scaleFactor);

    /**
     * Rotates the shape
     *
     * @param degrees how many degrees to rotate
     */
    public void rotate(double degrees) {
        rotation = (rotation + degrees) % 360;
        if (rotation < 0) {
            rotation += 360;
        }
    }


    /**
     * Sets the selection state of the shape.
     *
     * @param selected new shape selection state
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        if (!selected) {
            pointEditMode = false;
            selectedPointIndex = -1;
        }
    }
    
    /**
     * Sets the point edit mode state.
     *
     * @param pointEditMode new point edit mode state
     */
    public void setPointEditMode(boolean pointEditMode) {
        this.pointEditMode = pointEditMode;
        if (!pointEditMode) {
            selectedPointIndex = -1;
        }
    }
    
    /**
     * Sets the index of the selected control point.
     *
     * @param selectedPointIndex index of selected point
     */
    public void setSelectedPointIndex(int selectedPointIndex) {
        this.selectedPointIndex = selectedPointIndex;
    }
    
    /**
     * Gets the fill color of the shape.
     *
     * @return the fill color
     */
    public Color getFillColor() {
        return fillColor;
    }
    
    /**
     * Sets the fill color of the shape.
     *
     * @param fillColor the new fill color
     */
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    /**
     * Draws the selection handles when you select a shape.
     * Shows a white square with black border at the center,
     * and control points if you're in edit mode.
     *
     * @param gc graphics context to draw on
     */
    protected void drawSelectionHandles(GraphicsContext gc) {
        if (selected) {
            double handleSize = 8;

            gc.setFill(Color.WHITE);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1.0);

            // Draw handles
            gc.fillRect(centerX - handleSize/2, centerY - handleSize/2, handleSize, handleSize);
            gc.strokeRect(centerX - handleSize/2, centerY - handleSize/2, handleSize, handleSize);
            
            // Draw control points in point edit mode
            if (pointEditMode) {
                drawControlPoints(gc);
            }
        }
    }

    /**
     * Sets up the drawing settings before drawing a shape.
     * Gets the colors and line width ready, and makes
     * selected shapes have thicker borders.
     *
     * @param gc graphics context to set up
     */
    protected void setupGraphicsContext(GraphicsContext gc) {
        gc.setFill(fillColor);
        gc.setStroke(strokeColor);
        gc.setLineWidth(strokeWidth);

        // Increase line width for selected shape
        if (selected) {
            gc.setLineWidth(strokeWidth + 1);
        }
    }

    /**
     * This is the part that handles rotation.
     * It saves the current state, moves to the shape center,
     * rotates, moves back, and sets up the drawing settings.
     *
     * @param gc graphics context to transform
     */
    protected void applyTransformation(GraphicsContext gc) {
        gc.save();

        // Translate to shape center
        gc.translate(centerX, centerY);

        // Rotate by specified angle
        gc.rotate(rotation);

        // Return to shape coordinate system origin
        gc.translate(-centerX, -centerY);

        setupGraphicsContext(gc);
    }

    /**
     * Puts the graphics context back to normal after drawing.
     * Undoes what applyTransformation did.
     *
     * @param gc graphics context to restore
     */
    protected void restoreTransformation(GraphicsContext gc) {
        gc.restore();
    }
    
    /**
     * Special method for saving shapes to a file.
     * Had to handle colors specially because they're marked transient.
     *
     * @param out output stream to write to
     * @throws IOException if something goes wrong with saving
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        
        // Save color components
        if (fillColor != null) {
            out.writeDouble(fillColor.getRed());
            out.writeDouble(fillColor.getGreen());
            out.writeDouble(fillColor.getBlue());
            out.writeDouble(fillColor.getOpacity());
        } else {
            out.writeDouble(-1.0); // Marker for null
        }
        
        if (strokeColor != null) {
            out.writeDouble(strokeColor.getRed());
            out.writeDouble(strokeColor.getGreen());
            out.writeDouble(strokeColor.getBlue());
            out.writeDouble(strokeColor.getOpacity());
        } else {
            out.writeDouble(-1.0); // Marker for null
        }
    }
    
    /**
     * Special method for loading shapes from a file.
     * Rebuilds the colors from their saved components.
     *
     * @param in input stream to read from
     * @throws IOException if something goes wrong with loading
     * @throws ClassNotFoundException if it can't find the right class
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        // Read fill color components
        double red = in.readDouble();
        if (red >= 0) {
            double green = in.readDouble();
            double blue = in.readDouble();
            double opacity = in.readDouble();
            fillColor = new Color(red, green, blue, opacity);
        } else {
            fillColor = null;
        }
        
        // Read border color components
        red = in.readDouble();
        if (red >= 0) {
            double green = in.readDouble();
            double blue = in.readDouble();
            double opacity = in.readDouble();
            strokeColor = new Color(red, green, blue, opacity);
        } else {
            strokeColor = null;
        }
    }
}
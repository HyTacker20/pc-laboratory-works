package shapeeditor;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * This is the canvas where all the shapes are drawn.
 * It's like a drawing board that keeps track of all shapes
 * and lets you select and modify them.
 *
 * @author Andrii Hermak
 * @version 1.0
 */
public class DrawingPane extends Pane {

    /** List of all shapes in the drawing */
    private List<Shape> shapes;
    /** Currently selected shape, or null if none */
    private Shape selectedShape;
    /** Whether point edit mode is active */
    private boolean pointEditMode = false;
    /** Index of the selected control point, -1 if none */
    private int selectedPointIndex = -1;
    /** Canvas where shapes are drawn */
    private Canvas canvas;
    /** Graphics context for drawing on the canvas */
    private GraphicsContext gc;

    /**
     * Sets up the drawing area
     */
    public DrawingPane() {
        shapes = new ArrayList<>();
        selectedShape = null;

        // Create and configure canvas
        canvas = new Canvas(800, 600);
        getChildren().add(canvas);
        gc = canvas.getGraphicsContext2D();

        // Adjust canvas size to panel size
        widthProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setWidth(newVal.doubleValue());
            redraw();
        });

        heightProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setHeight(newVal.doubleValue());
            redraw();
        });

        // Initial size
        setPrefSize(800, 600);

        // Initial drawing
        redraw();
    }

    /**
     * Adds a new shape to the drawing
     *
     * @param shape the shape to add
     */
    public void addShape(Shape shape) {
        // Add shape to list
        shapes.add(shape);

        // Select newly added shape
        selectShape(shape);

        // Refresh view
        redraw();
    }

    /**
     * Gets a copy of the list of all shapes in the drawing.
     *
     * @return list of all shapes in the drawing
     */
    public List<Shape> getShapes() {
        return new ArrayList<>(shapes);
    }

    /**
     * Replaces all shapes with a new list
     * (used when loading from a file)
     *
     * @param shapes new list of shapes
     */
    public void setShapes(List<Shape> shapes) {
        this.shapes = new ArrayList<>(shapes);
        this.selectedShape = null;
        redraw();
    }

    /**
     * Removes all shapes from the drawing
     */
    public void clearShapes() {
        shapes.clear();
        selectedShape = null;
        redraw();
    }

    /**
     * Tries to select a shape at the given position
     *
     * @param x X coordinate where you clicked
     * @param y Y coordinate where you clicked
     * @return true if it found and selected a shape, false if not
     */
    public boolean selectShapeAt(double x, double y) {
        // Search for shape from end of list (newest first)
        for (int i = shapes.size() - 1; i >= 0; i--) {
            Shape shape = shapes.get(i);
            if (shape.contains(x, y)) {
                selectShape(shape);
                return true;
            }
        }

        // If no shape found, deselect current one
        selectShape(null);
        return false;
    }

    /**
     * Selects a specific shape or deselects everything
     *
     * @param shape shape to select, or null to deselect
     */
    public void selectShape(Shape shape) {
        // If selecting the same shape, do nothing
        if (selectedShape == shape) {
            return;
        }

        // Deselect previous shape
        if (selectedShape != null) {
            selectedShape.setSelected(false);
            selectedShape.setPointEditMode(false);
        }

        // Select new shape
        selectedShape = shape;
        if (selectedShape != null) {
            selectedShape.setSelected(true);
            selectedShape.setPointEditMode(pointEditMode);
        }
        
        // Reset selected control point
        selectedPointIndex = -1;

        // Refresh view
        redraw();
    }

    /**
     * Gets the currently selected shape.
     *
     * @return the shape that's currently selected, or null if none
     */
    public Shape getSelectedShape() {
        return selectedShape;
    }
    
    /**
     * Checks if point edit mode is currently active.
     *
     * @return whether point edit mode is turned on
     */
    public boolean isPointEditMode() {
        return pointEditMode;
    }
    
    /**
     * Turns point edit mode on or off
     *
     * @param pointEditMode true to enable, false to disable
     */
    public void setPointEditMode(boolean pointEditMode) {
        this.pointEditMode = pointEditMode;
        
        if (selectedShape != null) {
            selectedShape.setPointEditMode(pointEditMode);
            selectedShape.setSelectedPointIndex(-1);
        }
        
        redraw();
    }
    
    /**
     * Tries to select a control point where you clicked.
     * If it finds one, it selects it so you can drag it.
     *
     * @param x X coordinate where you clicked
     * @param y Y coordinate where you clicked
     * @return true if it found and selected a control point, false if not
     */
    public boolean selectControlPointAt(double x, double y) {
        if (selectedShape != null && pointEditMode) {
            int pointIndex = selectedShape.getControlPointAt(x, y);
            
            if (pointIndex >= 0) {
                selectedShape.setSelectedPointIndex(pointIndex);
                selectedPointIndex = pointIndex;
                redraw();
                return true;
            } else {
                selectedShape.setSelectedPointIndex(-1);
                selectedPointIndex = -1;
                redraw();
            }
        }
        
        return false;
    }
    
    /**
     * Moves the selected control point when you drag it.
     * This changes the shape by moving one of its control points.
     * Only works if you have a shape selected, point edit mode on,
     * and a control point selected.
     *
     * @param deltaX how far to move horizontally
     * @param deltaY how far to move vertically
     * @return true if it moved a control point, false if not
     */
    public boolean moveSelectedControlPoint(double deltaX, double deltaY) {
        if (selectedShape != null && pointEditMode && selectedPointIndex >= 0) {
            selectedShape.moveControlPoint(selectedPointIndex, deltaX, deltaY);
            redraw();
            return true;
        }
        
        return false;
    }

    /**
     * Deletes the currently selected shape
     *
     * @return true if it deleted something, false if nothing was selected
     */
    public boolean deleteSelectedShape() {
        if (selectedShape != null) {
            shapes.remove(selectedShape);
            selectedShape = null;
            redraw();
            return true;
        }
        return false;
    }

    /**
     * Moves the selected shape when you drag it
     *
     * @param deltaX how far to move horizontally
     * @param deltaY how far to move vertically
     * @return true if it moved a shape, false if nothing was selected
     */
    public boolean moveSelectedShape(double deltaX, double deltaY) {
        if (selectedShape != null) {
            selectedShape.move(deltaX, deltaY);
            redraw();
            return true;
        }
        return false;
    }

    /**
     * Makes the selected shape bigger or smaller
     *
     * @param scaleFactor how much to resize
     * @return true if it resized a shape, false if nothing was selected
     */
    public boolean resizeSelectedShape(double scaleFactor) {
        if (selectedShape != null) {
            selectedShape.resize(scaleFactor);
            redraw();
            return true;
        }
        return false;
    }

    /**
     * Redraws everything on the canvas
     */
    public void redraw() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // Clear canvas
        gc.clearRect(0, 0, width, height);

        // Draw background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);

        // Draw grid
        drawGrid();

        // Draw all shapes
        for (Shape shape : shapes) {
            shape.draw(gc);
        }
    }

    /**
     * Draws the grid pattern in the background.
     * The grid helps you position shapes more precisely.
     */
    private void drawGrid() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double gridSize = 20;

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);

        // Draw vertical lines
        for (double x = 0; x <= width; x += gridSize) {
            gc.strokeLine(x, 0, x, height);
        }

        // Draw horizontal lines
        for (double y = 0; y <= height; y += gridSize) {
            gc.strokeLine(0, y, width, y);
        }
    }
}
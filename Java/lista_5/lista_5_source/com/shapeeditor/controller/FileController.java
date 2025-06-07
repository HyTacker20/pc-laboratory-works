package com.shapeeditor.controller;

import com.shapeeditor.model.Shape;
import com.shapeeditor.persistence.ShapePersistence;
import com.shapeeditor.view.DrawingCanvas;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for file operations.
 * Manages saving and loading shapes.
 */
public class FileController {
    /** The drawing canvas */
    private DrawingCanvas canvas;
    
    /** The file chooser for open/save dialogs */
    private FileChooser fileChooser;
    
    /** The primary stage for showing dialogs */
    private Stage primaryStage;
    
    /** The shape persistence handler */
    private ShapePersistence shapePersistence;
    
    /**
     * Constructs a new FileController with the specified canvas.
     *
     * @param canvas The drawing canvas
     */
    public FileController(DrawingCanvas canvas) {
        this.canvas = canvas;
        
        // Initialize the file chooser and persistence handler
        initializeFileChooser();
        this.shapePersistence = new ShapePersistence();
    }
    
    /**
     * Sets the primary stage for showing dialogs.
     *
     * @param primaryStage The primary stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Initializes the file chooser.
     */
    private void initializeFileChooser() {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Shape Editor File");
        
        // Set the initial directory to the user's home directory
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        
        // Add extension filters for shape files
        FileChooser.ExtensionFilter jsonFilter =
            new FileChooser.ExtensionFilter("JSON Shape files (*.json)", "*.json");
        FileChooser.ExtensionFilter legacyFilter =
            new FileChooser.ExtensionFilter("Legacy Shape files (*.shapes)", "*.shapes");
        fileChooser.getExtensionFilters().addAll(jsonFilter, legacyFilter);
    }
    
    /**
     * Creates a new empty file.
     */
    public void newFile() {
        // Clear the canvas
        canvas.clearShapes();
    }
    
    /**
     * Opens a file and loads the shapes.
     */
    public void openFile() {
        if (primaryStage == null) {
            System.err.println("Primary stage not set");
            return;
        }
        
        // Show the open file dialog
        File file = fileChooser.showOpenDialog(primaryStage);
        
        if (file != null) {
            loadShapesFromFile(file);
        }
    }
    
    /**
     * Saves the current shapes to a file.
     */
    public void saveFile() {
        if (primaryStage == null) {
            System.err.println("Primary stage not set");
            return;
        }
        
        // Show the save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);
        
        if (file != null) {
            // Add the extension if it's not there
            String path = file.getPath();
            if (!path.endsWith(".json") && !path.endsWith(".shapes")) {
                file = new File(path + ".json");
            }
            
            saveShapesToFile(file);
        }
    }
    
    /**
     * Loads shapes from the specified file.
     *
     * @param file The file to load from
     */
    private void loadShapesFromFile(File file) {
        try {
            List<Shape> shapes;
            
            // Check file extension to determine which loading method to use
            if (file.getName().endsWith(".json")) {
                // Use JSON persistence
                shapes = shapePersistence.loadShapesSafely(file);
            } else {
                // Use legacy loading for .shapes files
                shapes = loadLegacyShapes(file);
            }
            
            // Clear the canvas and add the loaded shapes
            canvas.clearShapes();
            for (Shape shape : shapes) {
                canvas.addShape(shape);
            }
        } catch (Exception e) {
            System.err.println("Error loading shapes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Saves the current shapes to the specified file.
     *
     * @param file The file to save to
     */
    private void saveShapesToFile(File file) {
        try {
            List<Shape> shapes = canvas.getShapes();
            
            // Check file extension to determine which saving method to use
            if (file.getName().endsWith(".json")) {
                // Use JSON persistence
                shapePersistence.saveShapesSafely(shapes, file);
            } else {
                // Use legacy saving for .shapes files
                saveLegacyShapes(shapes, file);
            }
        } catch (Exception e) {
            System.err.println("Error saving shapes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads shapes from a legacy .shapes file format.
     * This method is kept for backward compatibility.
     *
     * @param file The file to load from
     * @return The list of loaded shapes
     */
    private List<Shape> loadLegacyShapes(File file) {
        // Implementation of the original loadShapesFromFile method
        // This is kept for backward compatibility with the .shapes format
        List<Shape> shapes = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Shape shape = parseLegacyShapeLine(line);
                if (shape != null) {
                    shapes.add(shape);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading legacy shapes: " + e.getMessage());
        }
        
        return shapes;
    }
    
    /**
     * Saves shapes to a legacy .shapes file format.
     * This method is kept for backward compatibility.
     *
     * @param shapes The shapes to save
     * @param file The file to save to
     */
    private void saveLegacyShapes(List<Shape> shapes, File file) {
        // Implementation of the original saveShapesToFile method
        // This is kept for backward compatibility with the .shapes format
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Shape shape : shapes) {
                String line = formatLegacyShapeLine(shape);
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving legacy shapes: " + e.getMessage());
        }
    }
    // The following methods are kept for backward compatibility with the .shapes format
    
    /**
     * Parses a line from a legacy file into a Shape object.
     *
     * @param line The line to parse
     * @return The parsed Shape, or null if parsing failed
     */
    private Shape parseLegacyShapeLine(String line) {
        // Implementation of the original parseShapeLine method
        try {
            String[] parts = line.split(",");
            String type = parts[0];
            
            // Parse common properties
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double rotation = Double.parseDouble(parts[3]);
            javafx.scene.paint.Color color = javafx.scene.paint.Color.web(parts[4]);
            
            // Create the shape based on the type
            Shape shape = null;
            
            switch (type) {
                case "Circle":
                    double radius = Double.parseDouble(parts[5]);
                    shape = com.shapeeditor.model.ShapeFactory.createCircle(x, y, radius, color);
                    break;
                    
                case "Rectangle":
                    double width = Double.parseDouble(parts[5]);
                    double height = Double.parseDouble(parts[6]);
                    shape = com.shapeeditor.model.ShapeFactory.createRectangle(x - width/2, y - height/2, width, height, color);
                    break;
                    
                case "Polygon":
                    int numPoints = Integer.parseInt(parts[5]);
                    double radius2 = Double.parseDouble(parts[6]);
                    shape = com.shapeeditor.model.ShapeFactory.createRegularPolygon(x, y, numPoints, radius2, color);
                    break;
                    
                default:
                    System.err.println("Unknown shape type: " + type);
                    return null;
            }
            
            // Set the rotation
            shape.rotate(rotation);
            
            return shape;
        } catch (Exception e) {
            System.err.println("Error parsing legacy shape: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Formats a Shape object into a line for a legacy file.
     *
     * @param shape The shape to format
     * @return The formatted line
     */
    private String formatLegacyShapeLine(Shape shape) {
        // Implementation of the original formatShapeLine method
        StringBuilder sb = new StringBuilder();
        
        // Add common properties
        sb.append(shape.getClass().getSimpleName()).append(",");
        sb.append(shape.getX()).append(",");
        sb.append(shape.getY()).append(",");
        sb.append(shape.getRotation()).append(",");
        sb.append(formatLegacyColor(shape.getFillColor()));
        
        // Add type-specific properties
        if (shape instanceof com.shapeeditor.model.Circle) {
            com.shapeeditor.model.Circle circle = (com.shapeeditor.model.Circle) shape;
            sb.append(",").append(circle.getRadius());
        } else if (shape instanceof com.shapeeditor.model.Rectangle) {
            com.shapeeditor.model.Rectangle rectangle = (com.shapeeditor.model.Rectangle) shape;
            sb.append(",").append(rectangle.getWidth());
            sb.append(",").append(rectangle.getHeight());
        } else if (shape instanceof com.shapeeditor.model.Polygon) {
            com.shapeeditor.model.Polygon polygon = (com.shapeeditor.model.Polygon) shape;
            sb.append(",").append(polygon.getNumPoints());
            // Note: This assumes there's a getRadius method in Polygon, which might not be accurate
            // In a real implementation, you might need to calculate this or use a different property
            double avgDistance = calculateAverageDistance(polygon);
            sb.append(",").append(avgDistance);
        }
        
        return sb.toString();
    }
    
    /**
     * Calculates the average distance from the center to the points of a polygon.
     * This is used as an approximation for the "radius" of the polygon.
     *
     * @param polygon The polygon
     * @return The average distance
     */
    private double calculateAverageDistance(com.shapeeditor.model.Polygon polygon) {
        double[] pointsX = polygon.getPointsX();
        double[] pointsY = polygon.getPointsY();
        int numPoints = polygon.getNumPoints();
        
        double sumDistances = 0;
        for (int i = 0; i < numPoints; i++) {
            double distance = Math.sqrt(pointsX[i] * pointsX[i] + pointsY[i] * pointsY[i]);
            sumDistances += distance;
        }
        
        return sumDistances / numPoints;
    }
    
    /**
     * Formats a Color object into a string representation for legacy files.
     *
     * @param color The color to format
     * @return The formatted color string
     */
    private String formatLegacyColor(javafx.scene.paint.Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }
}
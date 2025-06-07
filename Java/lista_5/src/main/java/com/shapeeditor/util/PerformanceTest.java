package com.shapeeditor.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.shapeeditor.model.Shape;
import com.shapeeditor.model.ShapeFactory;
import com.shapeeditor.persistence.ShapePersistence;
import com.shapeeditor.view.DrawingCanvas;

import javafx.scene.paint.Color;

/**
 * Utility class for performance testing of the Shape Editor application.
 * This class provides methods to test the performance of various optimized components
 * and measure the impact of the optimizations.
 */
public class PerformanceTest {
    /** Random number generator for creating random shapes */
    private static final Random random = new Random();
    
    /** Number of shapes to create for the stress test */
    private static final int NUM_SHAPES = 1000;
    
    /** Canvas width for testing */
    private static final double CANVAS_WIDTH = 800;
    
    /** Canvas height for testing */
    private static final double CANVAS_HEIGHT = 600;
    
    /** Temporary file for persistence testing */
    private static final String TEMP_FILE_PATH = "temp_performance_test.json";
    
    /**
     * Runs a comprehensive performance test on all optimized components.
     * Measures and reports the performance of each component.
     * 
     * @return A string containing the test results
     */
    public static String runPerformanceTest() {
        StringBuilder results = new StringBuilder();
        results.append("Shape Editor Performance Test Results\n");
        results.append("====================================\n\n");
        
        // Test DrawingCanvas performance
        results.append(testDrawingCanvasPerformance());
        results.append("\n");
        
        // Test SelectionController performance
        results.append(testSelectionPerformance());
        results.append("\n");
        
        // Test ShapePersistence performance
        results.append(testPersistencePerformance());
        results.append("\n");
        
        // Test JsonConverter performance
        results.append(testJsonConverterPerformance());
        results.append("\n");
        
        // Test integrated performance
        results.append(testIntegratedPerformance());
        
        return results.toString();
    }
    
    /**
     * Tests the performance of the DrawingCanvas with dirty region tracking and caching.
     * 
     * @return A string containing the test results
     */
    private static String testDrawingCanvasPerformance() {
        StringBuilder results = new StringBuilder();
        results.append("DrawingCanvas Performance Test\n");
        results.append("-----------------------------\n");
        
        // Create a canvas
        DrawingCanvas canvas = new DrawingCanvas();
        
        // Create a list of random shapes
        List<Shape> shapes = createRandomShapes(NUM_SHAPES);
        
        // Measure time to add all shapes
        long startTime = System.nanoTime();
        for (Shape shape : shapes) {
            canvas.addShape(shape);
        }
        long endTime = System.nanoTime();
        long addTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        
        results.append("Time to add ").append(NUM_SHAPES).append(" shapes: ").append(addTime).append(" ms\n");
        
        // Measure time to redraw the canvas
        startTime = System.nanoTime();
        canvas.redraw();
        endTime = System.nanoTime();
        long redrawTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        
        results.append("Time to redraw canvas: ").append(redrawTime).append(" ms\n");
        
        // Measure time to select and move a shape (which uses dirty region tracking)
        if (!shapes.isEmpty()) {
            Shape shape = shapes.get(shapes.size() / 2); // Select a shape in the middle
            canvas.setSelectedShape(shape);
            
            startTime = System.nanoTime();
            shape.move(shape.getX() + 10, shape.getY() + 10);
            canvas.redraw();
            endTime = System.nanoTime();
            long moveTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
            
            results.append("Time to move a shape and redraw: ").append(moveTime).append(" ms\n");
        }
        
        return results.toString();
    }
    
    /**
     * Tests the performance of the SelectionController with spatial partitioning and event throttling.
     * 
     * @return A string containing the test results
     */
    private static String testSelectionPerformance() {
        StringBuilder results = new StringBuilder();
        results.append("Selection Performance Test\n");
        results.append("-------------------------\n");
        
        // Create a list of random shapes
        List<Shape> shapes = createRandomShapes(NUM_SHAPES);
        
        // Create a QuadTree
        QuadTree quadTree = new QuadTree(new QuadTree.Boundary(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT), 0);
        
        // Measure time to build the QuadTree
        long startTime = System.nanoTime();
        quadTree.update(shapes);
        long endTime = System.nanoTime();
        long buildTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        
        results.append("Time to build QuadTree with ").append(NUM_SHAPES).append(" shapes: ").append(buildTime).append(" ms\n");
        
        // Measure time to query the QuadTree
        double queryX = CANVAS_WIDTH / 2;
        double queryY = CANVAS_HEIGHT / 2;
        
        startTime = System.nanoTime();
        List<Shape> foundShapes = quadTree.queryPoint(queryX, queryY);
        endTime = System.nanoTime();
        long queryTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        
        results.append("Time to query QuadTree: ").append(queryTime).append(" ms\n");
        results.append("Number of shapes found at query point: ").append(foundShapes.size()).append("\n");
        
        // Test event throttling
        EventThrottler throttler = new EventThrottler(16); // 16ms throttle interval (60 FPS)
        
        int throttledEvents = 0;
        int totalEvents = 100;
        
        startTime = System.nanoTime();
        for (int i = 0; i < totalEvents; i++) {
            if (throttler.shouldProcessEvent()) {
                throttledEvents++;
            }
            
            try {
                Thread.sleep(1); // Simulate 1ms between events
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        endTime = System.nanoTime();
        long throttleTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        
        results.append("Event throttling: ").append(throttledEvents).append(" of ").append(totalEvents);
        results.append(" events processed in ").append(throttleTime).append(" ms\n");
        
        return results.toString();
    }
    
    /**
     * Tests the performance of the ShapePersistence with buffered I/O and optimized string processing.
     * 
     * @return A string containing the test results
     */
    private static String testPersistencePerformance() {
        StringBuilder results = new StringBuilder();
        results.append("Persistence Performance Test\n");
        results.append("---------------------------\n");
        
        // Create a list of random shapes
        List<Shape> shapes = createRandomShapes(NUM_SHAPES);
        
        // Create a ShapePersistence instance
        ShapePersistence persistence = new ShapePersistence();
        
        // Create a temporary file
        File tempFile = new File(TEMP_FILE_PATH);
        
        // Measure time to save shapes
        long startTime = System.nanoTime();
        boolean saveSuccess = persistence.saveShapesSafely(shapes, tempFile);
        long endTime = System.nanoTime();
        long saveTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        
        results.append("Time to save ").append(NUM_SHAPES).append(" shapes: ").append(saveTime).append(" ms\n");
        results.append("Save successful: ").append(saveSuccess).append("\n");
        
        // Measure time to load shapes
        startTime = System.nanoTime();
        List<Shape> loadedShapes = persistence.loadShapesSafely(tempFile);
        endTime = System.nanoTime();
        long loadTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        
        results.append("Time to load ").append(loadedShapes.size()).append(" shapes: ").append(loadTime).append(" ms\n");
        
        // Clean up the temporary file
        tempFile.delete();
        
        return results.toString();
    }
    
    /**
     * Tests the performance of the JsonConverter with reduced type casting and object pooling.
     * 
     * @return A string containing the test results
     */
    private static String testJsonConverterPerformance() {
        StringBuilder results = new StringBuilder();
        results.append("JsonConverter Performance Test\n");
        results.append("----------------------------\n");
        
        // This test is indirectly covered by the persistence test
        results.append("JsonConverter performance is measured as part of the persistence test.\n");
        results.append("The optimizations include reduced type casting, eliminated redundant collection copying,\n");
        results.append("and object pooling for StringBuilder and Map instances.\n");
        
        return results.toString();
    }
    
    /**
     * Tests the integrated performance of all optimized components working together.
     * 
     * @return A string containing the test results
     */
    private static String testIntegratedPerformance() {
        StringBuilder results = new StringBuilder();
        results.append("Integrated Performance Test\n");
        results.append("-------------------------\n");
        
        // Create a canvas
        DrawingCanvas canvas = new DrawingCanvas();
        
        // Create a list of random shapes
        List<Shape> shapes = createRandomShapes(NUM_SHAPES);
        
        // Add shapes to the canvas
        for (Shape shape : shapes) {
            canvas.addShape(shape);
        }
        
        // Create a ShapePersistence instance
        ShapePersistence persistence = new ShapePersistence();
        
        // Create a temporary file
        File tempFile = new File(TEMP_FILE_PATH);
        
        // Measure time for a complete save-load cycle
        long startTime = System.nanoTime();
        
        // Save the shapes
        persistence.saveShapesSafely(canvas.getShapes(), tempFile);
        
        // Clear the canvas
        canvas.clearShapes();
        
        // Load the shapes
        List<Shape> loadedShapes = persistence.loadShapesSafely(tempFile);
        
        // Add the loaded shapes to the canvas
        for (Shape shape : loadedShapes) {
            canvas.addShape(shape);
        }
        
        // Redraw the canvas
        canvas.redraw();
        
        long endTime = System.nanoTime();
        long totalTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        
        results.append("Time for complete save-load cycle with ").append(NUM_SHAPES).append(" shapes: ");
        results.append(totalTime).append(" ms\n");
        
        // Clean up the temporary file
        tempFile.delete();
        
        return results.toString();
    }
    
    /**
     * Creates a list of random shapes for testing.
     * 
     * @param count The number of shapes to create
     * @return A list of random shapes
     */
    private static List<Shape> createRandomShapes(int count) {
        List<Shape> shapes = new ArrayList<>(count);
        
        for (int i = 0; i < count; i++) {
            // Randomly choose a shape type
            int shapeType = random.nextInt(3);
            
            // Random position
            double x = random.nextDouble() * CANVAS_WIDTH;
            double y = random.nextDouble() * CANVAS_HEIGHT;
            
            // Random color
            Color color = Color.rgb(
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
            );
            
            Shape shape;
            
            switch (shapeType) {
                case 0: // Circle
                    double radius = 10 + random.nextDouble() * 40;
                    shape = ShapeFactory.createCircle(x, y, radius, color);
                    break;
                    
                case 1: // Rectangle
                    double width = 20 + random.nextDouble() * 80;
                    double height = 20 + random.nextDouble() * 80;
                    shape = ShapeFactory.createRectangle(x, y, width, height, color);
                    break;
                    
                case 2: // Polygon
                    int numPoints = 3 + random.nextInt(8); // 3 to 10 points
                    double polygonRadius = 15 + random.nextDouble() * 35;
                    shape = ShapeFactory.createRegularPolygon(x, y, numPoints, polygonRadius, color);
                    break;
                    
                default:
                    shape = ShapeFactory.createCircle(x, y, 20, color);
            }
            
            // Random rotation
            shape.rotate(random.nextDouble() * 360);
            
            shapes.add(shape);
        }
        
        return shapes;
    }
}
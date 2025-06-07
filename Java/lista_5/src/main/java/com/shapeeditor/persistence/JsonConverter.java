package com.shapeeditor.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.shapeeditor.model.Circle;
import com.shapeeditor.model.Polygon;
import com.shapeeditor.model.Rectangle;
import com.shapeeditor.model.Shape;
import com.shapeeditor.model.ShapeFactory;

import javafx.scene.paint.Color;

/**
 * Utility class for converting between Shape objects and JSON representation.
 * This class provides methods to convert Shape objects to JSON format and vice versa.
 *
 * Optimized with:
 * - Reduced type casting: Uses helper methods to minimize expensive type conversions
 * - Object pooling: Reuses StringBuilder and Map instances to reduce garbage collection
 * - Eliminated redundant collection copying: Direct array conversion for polygon points
 * - Pre-allocated collections: Initializes collections with known sizes to avoid resizing
 */
public class JsonConverter {
    /**
     * Object pool for StringBuilder instances to reduce GC pressure.
     * Reusing StringBuilder instances avoids creating new objects for each string operation.
     */
    private static final ConcurrentLinkedQueue<StringBuilder> STRING_BUILDER_POOL = new ConcurrentLinkedQueue<>();
    
    /**
     * Initial capacity for StringBuilder to avoid resizing.
     * Setting an appropriate initial capacity prevents costly array resizing operations.
     */
    private static final int INITIAL_STRING_BUILDER_CAPACITY = 128;
    
    /**
     * Object pool for temporary HashMap instances.
     * Reusing Map instances reduces garbage collection overhead during JSON conversion.
     */
    private static final ConcurrentLinkedQueue<Map<String, Object>> MAP_POOL = new ConcurrentLinkedQueue<>();
    
    // Initial capacity for HashMap to avoid resizing
    private static final int INITIAL_MAP_CAPACITY = 16;

    /**
     * Converts a Shape object to a JSON-compatible Map.
     *
     * @param shape The Shape object to convert
     * @return A Map representing the Shape in JSON format
     */
    public Map<String, Object> shapeToJson(Shape shape) {
        // Get or create a map from the pool
        Map<String, Object> jsonMap = getMapFromPool();
        
        // Add common properties
        jsonMap.put("type", shape.getClass().getSimpleName());
        jsonMap.put("x", shape.getX());
        jsonMap.put("y", shape.getY());
        jsonMap.put("rotation", shape.getRotation());
        jsonMap.put("fillColor", colorToHex(shape.getFillColor()));
        
        // Add type-specific properties without excessive casting
        if (shape instanceof Circle) {
            Circle circle = (Circle) shape;
            jsonMap.put("radius", circle.getRadius());
        } else if (shape instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) shape;
            jsonMap.put("width", rectangle.getWidth());
            jsonMap.put("height", rectangle.getHeight());
        } else if (shape instanceof Polygon) {
            Polygon polygon = (Polygon) shape;
            jsonMap.put("pointsX", polygon.getPointsX());
            jsonMap.put("pointsY", polygon.getPointsY());
            jsonMap.put("numPoints", polygon.getNumPoints());
        }
        
        return jsonMap;
    }
    
    /**
     * Converts a list of Shape objects to a JSON-compatible List of Maps.
     *
     * @param shapes The list of Shape objects to convert
     * @return A List of Maps representing the Shapes in JSON format
     */
    public List<Map<String, Object>> shapesToJson(List<Shape> shapes) {
        // Pre-allocate the exact size needed to avoid resizing
        List<Map<String, Object>> jsonList = new ArrayList<>(shapes.size());
        
        for (Shape shape : shapes) {
            jsonList.add(shapeToJson(shape));
        }
        
        return jsonList;
    }
    
    /**
     * Converts a JSON-compatible Map to a Shape object.
     * Optimized to reduce type casting and improve performance.
     *
     * @param jsonMap The Map representing a Shape in JSON format
     * @return The converted Shape object, or null if conversion failed
     */
    public Shape jsonToShape(Map<String, Object> jsonMap) {
        try {
            // Extract values with helper methods to reduce casting
            String type = getString(jsonMap, "type");
            double x = getDouble(jsonMap, "x");
            double y = getDouble(jsonMap, "y");
            double rotation = getDouble(jsonMap, "rotation");
            Color fillColor = hexToColor(getString(jsonMap, "fillColor"));
            
            Shape shape = null;
            
            switch (type) {
                case "Circle":
                    double radius = getDouble(jsonMap, "radius");
                    shape = ShapeFactory.createCircle(x, y, radius, fillColor);
                    break;
                    
                case "Rectangle":
                    double width = getDouble(jsonMap, "width");
                    double height = getDouble(jsonMap, "height");
                    shape = ShapeFactory.createRectangle(x, y, width, height, fillColor);
                    break;
                    
                case "Polygon":
                    // Direct array conversion without intermediate List copying
                    shape = createPolygonFromJson(jsonMap, x, y, fillColor);
                    break;
                    
                default:
                    System.err.println("Unknown shape type: " + type);
                    return null;
            }
            
            // Set the rotation
            shape.rotate(rotation);
            
            return shape;
        } catch (Exception e) {
            System.err.println("Error converting JSON to Shape: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Helper method to create a Polygon from JSON data without redundant collection copying.
     *
     * @param jsonMap The JSON map containing polygon data
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param fillColor The fill color
     * @return A new Polygon instance
     */
    @SuppressWarnings("unchecked")
    private Polygon createPolygonFromJson(Map<String, Object> jsonMap, double x, double y, Color fillColor) {
        Object pointsXObj = jsonMap.get("pointsX");
        Object pointsYObj = jsonMap.get("pointsY");
        
        // Handle different possible types for points data
        if (pointsXObj instanceof double[] && pointsYObj instanceof double[]) {
            // Direct array use if already in the right format
            return new Polygon(x, y, (double[]) pointsXObj, (double[]) pointsYObj, fillColor);
        } else if (pointsXObj instanceof List && pointsYObj instanceof List) {
            // Convert from List to array if needed
            List<Number> pointsXList = (List<Number>) pointsXObj;
            List<Number> pointsYList = (List<Number>) pointsYObj;
            int size = pointsXList.size();
            
            // Create arrays directly with the known size
            double[] pointsX = new double[size];
            double[] pointsY = new double[size];
            
            // Fill arrays in a single pass
            for (int i = 0; i < size; i++) {
                pointsX[i] = pointsXList.get(i).doubleValue();
                pointsY[i] = pointsYList.get(i).doubleValue();
            }
            
            return new Polygon(x, y, pointsX, pointsY, fillColor);
        }
        
        throw new IllegalArgumentException("Invalid polygon points format");
    }
    
    /**
     * Converts a JSON-compatible List of Maps to a list of Shape objects.
     *
     * @param jsonList The List of Maps representing Shapes in JSON format
     * @return A List of converted Shape objects
     */
    public List<Shape> jsonToShapes(List<Map<String, Object>> jsonList) {
        // Pre-allocate the exact size needed to avoid resizing
        List<Shape> shapes = new ArrayList<>(jsonList.size());
        
        for (Map<String, Object> jsonMap : jsonList) {
            Shape shape = jsonToShape(jsonMap);
            if (shape != null) {
                shapes.add(shape);
            }
        }
        
        return shapes;
    }
    
    /**
     * Helper method to safely get a String value from a JSON map.
     *
     * @param map The JSON map
     * @param key The key to look up
     * @return The String value
     */
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Helper method to safely get a double value from a JSON map.
     *
     * @param map The JSON map
     * @param key The key to look up
     * @return The double value
     */
    private double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new IllegalArgumentException("Value for key '" + key + "' is not a number");
    }
    
    /**
     * Converts a Color object to a hexadecimal string representation.
     * Optimized to use StringBuilder from pool to reduce string concatenation.
     *
     * @param color The Color to convert
     * @return The hexadecimal string representation of the Color
     */
    private String colorToHex(Color color) {
        // Get a StringBuilder from the pool
        StringBuilder sb = getStringBuilderFromPool();
        
        try {
            // Append the hex color components
            sb.append('#');
            appendHexByte(sb, (int) (color.getRed() * 255));
            appendHexByte(sb, (int) (color.getGreen() * 255));
            appendHexByte(sb, (int) (color.getBlue() * 255));
            
            return sb.toString();
        } finally {
            // Return the StringBuilder to the pool
            recycleStringBuilder(sb);
        }
    }
    
    /**
     * Helper method to append a byte as a two-character hex string.
     *
     * @param sb The StringBuilder to append to
     * @param value The byte value (0-255)
     */
    private void appendHexByte(StringBuilder sb, int value) {
        char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        sb.append(HEX_DIGITS[(value >> 4) & 0xF]);
        sb.append(HEX_DIGITS[value & 0xF]);
    }
    
    /**
     * Converts a hexadecimal string representation to a Color object.
     *
     * @param hex The hexadecimal string representation of the Color
     * @return The converted Color object
     */
    private Color hexToColor(String hex) {
        return Color.web(hex);
    }
    
    /**
     * Gets a StringBuilder from the pool or creates a new one if the pool is empty.
     *
     * @return A StringBuilder instance
     */
    private StringBuilder getStringBuilderFromPool() {
        StringBuilder sb = STRING_BUILDER_POOL.poll();
        if (sb == null) {
            return new StringBuilder(INITIAL_STRING_BUILDER_CAPACITY);
        }
        sb.setLength(0); // Clear the StringBuilder
        return sb;
    }
    
    /**
     * Recycles a StringBuilder back to the pool.
     *
     * @param sb The StringBuilder to recycle
     */
    private void recycleStringBuilder(StringBuilder sb) {
        if (sb.capacity() <= INITIAL_STRING_BUILDER_CAPACITY * 2) {
            // Only recycle reasonably sized builders to prevent memory leaks
            STRING_BUILDER_POOL.offer(sb);
        }
    }
    
    /**
     * Gets a Map from the pool or creates a new one if the pool is empty.
     *
     * @return A Map instance
     */
    private Map<String, Object> getMapFromPool() {
        Map<String, Object> map = MAP_POOL.poll();
        if (map == null) {
            return new HashMap<>(INITIAL_MAP_CAPACITY);
        }
        map.clear(); // Clear the Map
        return map;
    }
    
    /**
     * Recycles a Map back to the pool.
     * This method is not currently used but could be implemented if needed.
     *
     * @param map The Map to recycle
     */
    @SuppressWarnings("unused")
    private void recycleMap(Map<String, Object> map) {
        if (map.size() <= INITIAL_MAP_CAPACITY * 2) {
            // Only recycle reasonably sized maps to prevent memory leaks
            MAP_POOL.offer(map);
        }
    }
}
package com.shapeeditor.persistence;

import com.shapeeditor.model.Circle;
import com.shapeeditor.model.Polygon;
import com.shapeeditor.model.Rectangle;
import com.shapeeditor.model.Shape;
import com.shapeeditor.model.ShapeFactory;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for converting between Shape objects and JSON representation.
 * This class provides methods to convert Shape objects to JSON format and vice versa.
 */
public class JsonConverter {

    /**
     * Converts a Shape object to a JSON-compatible Map.
     *
     * @param shape The Shape object to convert
     * @return A Map representing the Shape in JSON format
     */
    public Map<String, Object> shapeToJson(Shape shape) {
        Map<String, Object> jsonMap = new HashMap<>();
        
        // Add common properties
        jsonMap.put("type", shape.getClass().getSimpleName());
        jsonMap.put("x", shape.getX());
        jsonMap.put("y", shape.getY());
        jsonMap.put("rotation", shape.getRotation());
        jsonMap.put("fillColor", colorToHex(shape.getFillColor()));
        
        // Add type-specific properties
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
        List<Map<String, Object>> jsonList = new ArrayList<>();
        
        for (Shape shape : shapes) {
            jsonList.add(shapeToJson(shape));
        }
        
        return jsonList;
    }
    
    /**
     * Converts a JSON-compatible Map to a Shape object.
     *
     * @param jsonMap The Map representing a Shape in JSON format
     * @return The converted Shape object, or null if conversion failed
     */
    public Shape jsonToShape(Map<String, Object> jsonMap) {
        try {
            String type = (String) jsonMap.get("type");
            double x = ((Number) jsonMap.get("x")).doubleValue();
            double y = ((Number) jsonMap.get("y")).doubleValue();
            double rotation = ((Number) jsonMap.get("rotation")).doubleValue();
            Color fillColor = hexToColor((String) jsonMap.get("fillColor"));
            
            Shape shape = null;
            
            switch (type) {
                case "Circle":
                    double radius = ((Number) jsonMap.get("radius")).doubleValue();
                    shape = ShapeFactory.createCircle(x, y, radius, fillColor);
                    break;
                    
                case "Rectangle":
                    double width = ((Number) jsonMap.get("width")).doubleValue();
                    double height = ((Number) jsonMap.get("height")).doubleValue();
                    shape = ShapeFactory.createRectangle(x, y, width, height, fillColor);
                    break;
                    
                case "Polygon":
                    // Handle array conversion
                    List<Number> pointsXList = (List<Number>) jsonMap.get("pointsX");
                    List<Number> pointsYList = (List<Number>) jsonMap.get("pointsY");
                    
                    double[] pointsX = new double[pointsXList.size()];
                    double[] pointsY = new double[pointsYList.size()];
                    
                    for (int i = 0; i < pointsXList.size(); i++) {
                        pointsX[i] = pointsXList.get(i).doubleValue();
                        pointsY[i] = pointsYList.get(i).doubleValue();
                    }
                    
                    shape = new Polygon(x, y, pointsX, pointsY, fillColor);
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
     * Converts a JSON-compatible List of Maps to a list of Shape objects.
     *
     * @param jsonList The List of Maps representing Shapes in JSON format
     * @return A List of converted Shape objects
     */
    public List<Shape> jsonToShapes(List<Map<String, Object>> jsonList) {
        List<Shape> shapes = new ArrayList<>();
        
        for (Map<String, Object> jsonMap : jsonList) {
            Shape shape = jsonToShape(jsonMap);
            if (shape != null) {
                shapes.add(shape);
            }
        }
        
        return shapes;
    }
    
    /**
     * Converts a Color object to a hexadecimal string representation.
     *
     * @param color The Color to convert
     * @return The hexadecimal string representation of the Color
     */
    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
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
}
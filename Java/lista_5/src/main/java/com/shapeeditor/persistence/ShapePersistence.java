package com.shapeeditor.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.shapeeditor.model.Shape;

/**
 * Class responsible for saving and loading shapes to/from files.
 * This class uses JSON format for persistence and handles all file I/O operations.
 *
 * Optimized with:
 * - Buffered I/O operations: Uses BufferedReader/BufferedWriter for efficient file access
 * - Chunk-based reading: Reads files in chunks instead of line by line for better performance
 * - Optimized string processing: Efficient JSON parsing with minimal object creation
 */
public class ShapePersistence {
    /** The JSON converter for converting between Shape objects and JSON */
    private JsonConverter jsonConverter;
    
    /**
     * Constructs a new ShapePersistence instance.
     * Initializes the JSON converter.
     */
    public ShapePersistence() {
        this.jsonConverter = new JsonConverter();
    }
    
    /**
     * Saves a list of shapes to a file in JSON format.
     *
     * @param shapes The list of shapes to save
     * @param file The file to save to
     * @throws IOException If an I/O error occurs
     */
    public void saveShapesToFile(List<Shape> shapes, File file) throws IOException {
        // Convert shapes to JSON-compatible format
        List<Map<String, Object>> jsonShapes = jsonConverter.shapesToJson(shapes);
        
        // Convert to JSON string manually
        StringBuilder jsonString = new StringBuilder("[\n");
        for (int i = 0; i < jsonShapes.size(); i++) {
            jsonString.append("  ").append(mapToJsonString(jsonShapes.get(i)));
            if (i < jsonShapes.size() - 1) {
                jsonString.append(",");
            }
            jsonString.append("\n");
        }
        jsonString.append("]");
        
        // Write to file with try-with-resources for proper resource management
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(jsonString.toString());
        }
    }
    
    /**
     * Converts a Map to a JSON string.
     *
     * @param map The map to convert
     * @return The JSON string representation
     */
    private String mapToJsonString(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            
            sb.append("\"").append(entry.getKey()).append("\": ");
            Object value = entry.getValue();
            
            if (value == null) {
                sb.append("null");
            } else if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                sb.append(value);
            } else if (value instanceof List) {
                sb.append(listToJsonString((List<?>) value));
            } else {
                sb.append("\"").append(value).append("\"");
            }
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Converts a List to a JSON string.
     *
     * @param list The list to convert
     * @return The JSON string representation
     */
    private String listToJsonString(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        
        for (Object item : list) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            
            if (item == null) {
                sb.append("null");
            } else if (item instanceof String) {
                sb.append("\"").append(item).append("\"");
            } else if (item instanceof Number || item instanceof Boolean) {
                sb.append(item);
            } else if (item instanceof Map) {
                sb.append(mapToJsonString((Map<String, Object>) item));
            } else {
                sb.append("\"").append(item).append("\"");
            }
        }
        
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Loads a list of shapes from a file in JSON format.
     *
     * @param file The file to load from
     * @return The list of loaded shapes
     * @throws IOException If an I/O error occurs
     */
    public List<Shape> loadShapesFromFile(File file) throws IOException {
        // Use try-with-resources with buffered streaming for efficient reading
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            StringBuilder jsonString = new StringBuilder();
            char[] buffer = new char[8192]; // 8KB buffer for efficient reading
            int charsRead;
            
            // Read file in chunks instead of line by line for better performance
            while ((charsRead = reader.read(buffer)) != -1) {
                jsonString.append(buffer, 0, charsRead);
            }
            
            // Parse JSON string to list of maps
            List<Map<String, Object>> jsonShapes = parseJsonToList(jsonString.toString());
            
            // Convert JSON maps to Shape objects
            return jsonConverter.jsonToShapes(jsonShapes);
        }
    }
    
    /**
     * Parses a JSON string to a list of maps.
     * This is a simplified JSON parser that handles the specific format used by this application.
     *
     * @param json The JSON string to parse
     * @return The list of maps
     */
    private List<Map<String, Object>> parseJsonToList(String json) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // Trim whitespace and check for array format
        json = json.trim();
        if (!json.startsWith("[") || !json.endsWith("]")) {
            return result; // Not a valid JSON array
        }
        
        // Remove the outer brackets
        json = json.substring(1, json.length() - 1).trim();
        
        // Split by commas, but respect nested objects and arrays
        List<String> objectStrings = splitJsonObjects(json);
        
        // Parse each object string into a map
        for (String objStr : objectStrings) {
            Map<String, Object> map = parseJsonObject(objStr.trim());
            if (!map.isEmpty()) {
                result.add(map);
            }
        }
        
        return result;
    }
    
    /**
     * Splits a JSON string into individual object strings.
     * Optimized version with better string handling.
     *
     * @param json The JSON string to split
     * @return A list of individual object strings
     */
    private List<String> splitJsonObjects(String json) {
        List<String> result = new ArrayList<>();
        if (json.isEmpty()) {
            return result;
        }
        
        int startPos = 0;
        int braceCount = 0;
        int bracketCount = 0;
        boolean inQuotes = false;
        boolean escaped = false;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (escaped) {
                escaped = false;
                continue;
            }
            
            switch (c) {
                case '\\':
                    escaped = true;
                    break;
                case '"':
                    inQuotes = !inQuotes;
                    break;
                case '{':
                    if (!inQuotes) braceCount++;
                    break;
                case '}':
                    if (!inQuotes) braceCount--;
                    break;
                case '[':
                    if (!inQuotes) bracketCount++;
                    break;
                case ']':
                    if (!inQuotes) bracketCount--;
                    break;
                case ',':
                    if (!inQuotes && braceCount == 0 && bracketCount == 0) {
                        // This comma separates top-level objects
                        if (i > startPos) {
                            result.add(json.substring(startPos, i));
                            startPos = i + 1;
                        }
                    }
                    break;
            }
        }
        
        // Add the last object if there is one
        if (startPos < json.length()) {
            result.add(json.substring(startPos));
        }
        
        return result;
    }
    
    /**
     * Parses a JSON object string into a map.
     *
     * @param json The JSON object string to parse
     * @return A map representing the JSON object
     */
    private Map<String, Object> parseJsonObject(String json) {
        Map<String, Object> result = new HashMap<>();
        
        // Trim whitespace and check for object format
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            return result; // Not a valid JSON object
        }
        
        // Remove the outer braces
        json = json.substring(1, json.length() - 1).trim();
        
        // Split by commas, but respect nested objects and arrays
        List<String> keyValuePairs = splitJsonKeyValuePairs(json);
        
        // Parse each key-value pair
        for (String pair : keyValuePairs) {
            int colonIndex = findUnquotedColon(pair);
            if (colonIndex == -1) continue;
            
            String key = pair.substring(0, colonIndex).trim();
            String value = pair.substring(colonIndex + 1).trim();
            
            // Remove quotes from key
            if (key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length() - 1);
            }
            
            // Parse the value
            Object parsedValue = parseJsonValue(value);
            result.put(key, parsedValue);
        }
        
        return result;
    }
    
    /**
     * Finds the index of the first unquoted colon in a string.
     *
     * @param str The string to search
     * @return The index of the first unquoted colon, or -1 if not found
     */
    private int findUnquotedColon(String str) {
        boolean inQuotes = false;
        boolean escaped = false;
        
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            
            if (escaped) {
                escaped = false;
                continue;
            }
            
            if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ':' && !inQuotes) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Splits a JSON string into individual key-value pairs.
     *
     * @param json The JSON string to split
     * @return A list of individual key-value pair strings
     */
    private List<String> splitJsonKeyValuePairs(String json) {
        // This is similar to splitJsonObjects but for key-value pairs
        return splitJsonObjects(json);
    }
    
    /**
     * Parses a JSON value string into an appropriate Java object.
     *
     * @param json The JSON value string to parse
     * @return The parsed Java object
     */
    private Object parseJsonValue(String json) {
        json = json.trim();
        
        // Check for null
        if (json.equals("null")) {
            return null;
        }
        
        // Check for boolean
        if (json.equals("true")) {
            return Boolean.TRUE;
        }
        if (json.equals("false")) {
            return Boolean.FALSE;
        }
        
        // Check for string
        if (json.startsWith("\"") && json.endsWith("\"")) {
            return json.substring(1, json.length() - 1);
        }
        
        // Check for array
        if (json.startsWith("[") && json.endsWith("]")) {
            List<Object> list = new ArrayList<>();
            String content = json.substring(1, json.length() - 1).trim();
            if (!content.isEmpty()) {
                List<String> items = splitJsonObjects(content);
                for (String item : items) {
                    list.add(parseJsonValue(item.trim()));
                }
            }
            return list;
        }
        
        // Check for object
        if (json.startsWith("{") && json.endsWith("}")) {
            return parseJsonObject(json);
        }
        
        // Check for number
        try {
            if (json.contains(".")) {
                return Double.parseDouble(json);
            } else {
                return Integer.parseInt(json);
            }
        } catch (NumberFormatException e) {
            // Not a number, return as string
            return json;
        }
    }
    
    /**
     * Saves a list of shapes to a file with the specified path in JSON format.
     *
     * @param shapes The list of shapes to save
     * @param filePath The path of the file to save to
     * @throws IOException If an I/O error occurs
     */
    public void saveShapesToFile(List<Shape> shapes, String filePath) throws IOException {
        saveShapesToFile(shapes, new File(filePath));
    }
    
    /**
     * Loads a list of shapes from a file with the specified path in JSON format.
     *
     * @param filePath The path of the file to load from
     * @return The list of loaded shapes
     * @throws IOException If an I/O error occurs
     */
    public List<Shape> loadShapesFromFile(String filePath) throws IOException {
        return loadShapesFromFile(new File(filePath));
    }
    
    /**
     * Handles exceptions that may occur during file operations.
     * Logs the error and optionally rethrows it.
     *
     * @param e The exception that occurred
     * @param operation The operation being performed (e.g., "save" or "load")
     * @param rethrow Whether to rethrow the exception
     * @throws IOException If rethrow is true
     */
    /**
     * Logs an exception that occurred during file operations.
     *
     * @param e The exception that occurred
     * @param operation The operation being performed (e.g., "save" or "load")
     */
    private void logException(Exception e, String operation) {
        System.err.println("Error " + operation + "ing shapes: " + e.getMessage());
        e.printStackTrace();
    }
    
    /**
     * Safely saves shapes to a file, handling any exceptions.
     * Uses improved error handling with specific exception types.
     *
     * @param shapes The list of shapes to save
     * @param file The file to save to
     * @return true if the operation was successful, false otherwise
     */
    public boolean saveShapesSafely(List<Shape> shapes, File file) {
        if (shapes == null) {
            System.err.println("Cannot save null shapes list");
            return false;
        }
        
        if (file == null) {
            System.err.println("Cannot save to null file");
            return false;
        }
        
        try {
            // Create parent directories if they don't exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    System.err.println("Failed to create directory: " + parentDir.getPath());
                    return false;
                }
            }
            
            saveShapesToFile(shapes, file);
            return true;
        } catch (IOException e) {
            logException(e, "save");
            return false;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logException(e, "save");
            return false;
        }
    }
    
    /**
     * Safely loads shapes from a file, handling any exceptions.
     * Uses improved error handling with specific exception types.
     *
     * @param file The file to load from
     * @return The list of loaded shapes, or an empty list if an error occurred
     */
    public List<Shape> loadShapesSafely(File file) {
        if (file == null) {
            System.err.println("Cannot load from null file");
            return new ArrayList<>();
        }
        
        if (!file.exists()) {
            System.err.println("File does not exist: " + file.getPath());
            return new ArrayList<>();
        }
        
        if (!file.isFile()) {
            System.err.println("Not a file: " + file.getPath());
            return new ArrayList<>();
        }
        
        if (!file.canRead()) {
            System.err.println("Cannot read file: " + file.getPath());
            return new ArrayList<>();
        }
        
        try {
            return loadShapesFromFile(file);
        } catch (IOException e) {
            logException(e, "load");
            return new ArrayList<>();
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logException(e, "load");
            return new ArrayList<>();
        }
    }
}
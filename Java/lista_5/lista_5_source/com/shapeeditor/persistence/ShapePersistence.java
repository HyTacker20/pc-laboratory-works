package com.shapeeditor.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.shapeeditor.model.Shape;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for saving and loading shapes to/from files.
 * This class uses JSON format for persistence and handles all file I/O operations.
 */
public class ShapePersistence {
    /** The JSON converter for converting between Shape objects and JSON */
    private JsonConverter jsonConverter;
    
    /** The Gson instance for JSON serialization/deserialization */
    private Gson gson;
    
    /**
     * Constructs a new ShapePersistence instance.
     * Initializes the JSON converter and Gson instance.
     */
    public ShapePersistence() {
        this.jsonConverter = new JsonConverter();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
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
        
        // Convert to JSON string
        String jsonString = gson.toJson(jsonShapes);
        
        // Write to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(jsonString);
        }
    }
    
    /**
     * Loads a list of shapes from a file in JSON format.
     *
     * @param file The file to load from
     * @return The list of loaded shapes
     * @throws IOException If an I/O error occurs
     */
    public List<Shape> loadShapesFromFile(File file) throws IOException {
        // Read JSON string from file
        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
        }
        
        // Parse JSON string to list of maps
        Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
        List<Map<String, Object>> jsonShapes = gson.fromJson(jsonString.toString(), listType);
        
        // Convert JSON maps to Shape objects
        return jsonConverter.jsonToShapes(jsonShapes);
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
    private void handleException(IOException e, String operation, boolean rethrow) throws IOException {
        System.err.println("Error " + operation + "ing shapes: " + e.getMessage());
        e.printStackTrace();
        
        if (rethrow) {
            throw e;
        }
    }
    
    /**
     * Safely saves shapes to a file, handling any exceptions.
     *
     * @param shapes The list of shapes to save
     * @param file The file to save to
     * @return true if the operation was successful, false otherwise
     */
    public boolean saveShapesSafely(List<Shape> shapes, File file) {
        try {
            saveShapesToFile(shapes, file);
            return true;
        } catch (IOException e) {
            try {
                handleException(e, "save", false);
            } catch (IOException ignored) {
                // This should never happen since rethrow is false
            }
            return false;
        }
    }
    
    /**
     * Safely loads shapes from a file, handling any exceptions.
     *
     * @param file The file to load from
     * @return The list of loaded shapes, or an empty list if an error occurred
     */
    public List<Shape> loadShapesSafely(File file) {
        try {
            return loadShapesFromFile(file);
        } catch (IOException e) {
            try {
                handleException(e, "load", false);
            } catch (IOException ignored) {
                // This should never happen since rethrow is false
            }
            return new ArrayList<>();
        }
    }
}
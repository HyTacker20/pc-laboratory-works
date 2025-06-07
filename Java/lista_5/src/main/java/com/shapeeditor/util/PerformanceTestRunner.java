package com.shapeeditor.util;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * A simple application to run the performance tests and display the results.
 */
public class PerformanceTestRunner extends Application {

    /**
     * The main entry point for the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Run the performance tests
            String results = PerformanceTest.runPerformanceTest();
            
            // Print the results to the console
            System.out.println("\n=== PERFORMANCE TEST RESULTS ===\n");
            System.out.println(results);
            System.out.println("\n=== END OF PERFORMANCE TEST RESULTS ===\n");
            
            // Exit the application
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Error running performance tests: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
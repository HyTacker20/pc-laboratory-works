package shapeeditor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Main application class for the Shape Editor.
 * This class initializes the JavaFX application and sets up the UI.
 *
 * @author Andrii Hermak
 * @version 1.0
 */
public class Paint extends Application {
    
    /**
     * Default constructor for the Paint application.
     */
    public Paint() {
        // Default constructor
    }

    /**
     * The main method that starts everything
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * JavaFX calls this method to start the application.
     * It creates the window and all the UI components.
     *
     * @param primaryStage the main window of the app
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Create main application container
            BorderPane root = new BorderPane();

            // Create UI controller
            EditorUI editorUI = new EditorUI(primaryStage);

            // Set main UI element as root
            root.setCenter(editorUI.getMainPanel());

            // Create and configure scene
            Scene scene = new Scene(root, 800, 600);
            // Safely add stylesheet if it exists
            try {
                if (getClass().getResource("application.css") != null) {
                    scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
                }
            } catch (Exception e) {
                System.out.println("Cannot load CSS file: " + e.getMessage());
            }

            // Configure and display main window
            primaryStage.setScene(scene);
            primaryStage.setTitle("Paint");
            primaryStage.show();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
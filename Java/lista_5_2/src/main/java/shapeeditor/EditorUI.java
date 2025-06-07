package shapeeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * This class creates all the buttons, menus, and controls for the app.
 * It handles all the user interactions like clicking buttons and
 * dragging shapes. It also takes care of saving and loading files.
 *
 * @author Andrii Hermak
 * @version 1.0
 */
public class EditorUI {

    /** Main panel that contains all UI components */
    private BorderPane mainPanel;
    /** Drawing area where shapes are displayed */
    private DrawingPane drawingPane;
    /** Main application window */
    private Stage primaryStage;
    /** Currently selected shape creation tool */
    private ShapeType currentShapeType = ShapeType.NONE;
    /** Whether point edit mode is active */
    private boolean pointEditMode = false;
    /** Last mouse X position for drag operations */
    private double lastX;
    /** Last mouse Y position for drag operations */
    private double lastY;
    /** Whether a shape is currently being dragged */
    private boolean isDragging = false;
    /** Whether a control point is currently being dragged */
    private boolean isDraggingControlPoint = false;
    /** Currently displayed context menu, if any */
    private ContextMenu currentContextMenu = null;

    /**
     * Constructor that sets up the user interface
     *
     * @param stage the main window of the application
     */
    public EditorUI(Stage stage) {
        this.primaryStage = stage;
        initUI();
    }

    /**
     * Creates all the UI components and puts them together
     */
    private void initUI() {
        mainPanel = new BorderPane();

        // Create drawing panel
        drawingPane = new DrawingPane();

        // Create main menu
        MenuBar menuBar = createMainMenu();

        // Create toolbar
        ToolBar toolBar = createToolBar();

        // Configure main panel
        mainPanel.setTop(new VBox(menuBar, toolBar));
        mainPanel.setCenter(drawingPane);

        // Add mouse and scroll event handlers
        setupMouseHandlers();
    }

    /**
     * Makes the menu bar with File and Edit menus
     *
     * @return the menu bar component
     */
    private MenuBar createMainMenu() {
        MenuBar menuBar = new MenuBar();

        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New");
        MenuItem openItem = new MenuItem("Open...");
        MenuItem saveItem = new MenuItem("Save...");
        MenuItem exitItem = new MenuItem("Exit");

        fileMenu.getItems().addAll(newItem, openItem, saveItem, new SeparatorMenuItem(), exitItem);

        // Edit menu
        Menu editMenu = new Menu("Edit");
        MenuItem deleteItem = new MenuItem("Delete selected shape");

        editMenu.getItems().add(deleteItem);

        // Add menus to menu bar
        menuBar.getMenus().addAll(fileMenu, editMenu);

        // Menu event handlers

        // New
        newItem.setOnAction(e -> clearCanvas());

        // Open
        openItem.setOnAction(e -> openFile());

        // Save
        saveItem.setOnAction(e -> saveFile());

        // Delete selected shape
        deleteItem.setOnAction(e -> deleteSelectedShape());

        // Exit
        exitItem.setOnAction(e -> primaryStage.close());

        return menuBar;
    }

    /**
     * Makes the toolbar with buttons for shapes and tools
     *
     * @return the toolbar component
     */
    private ToolBar createToolBar() {
        ToolBar toolBar = new ToolBar();

        // Circle button
        Button circleButton = new Button("Circle");
        circleButton.setOnAction(e -> setCurrentShapeType(ShapeType.CIRCLE));

        // Rectangle button
        Button rectangleButton = new Button("Rectangle");
        rectangleButton.setOnAction(e -> setCurrentShapeType(ShapeType.RECTANGLE));

        // Polygon button
        Button polygonButton = new Button("Polygon");
        polygonButton.setOnAction(e -> setCurrentShapeType(ShapeType.POLYGON));

        // Selection mode button
        Button selectButton = new Button("Select");
        selectButton.setOnAction(e -> setCurrentShapeType(ShapeType.NONE));
        
        // Point edit mode button
        Button editPointsButton = new Button("Edit Points");
        editPointsButton.setOnAction(e -> togglePointEditMode());

        // Add buttons to toolbar
        toolBar.getItems().addAll(selectButton, new Separator(),
                circleButton, rectangleButton, polygonButton, new Separator(),
                editPointsButton);

        return toolBar;
    }

    /**
     * Switches point edit mode on/off.
     * When this mode is on, you can drag the control points
     * to change the shape's form.
     */
    private void togglePointEditMode() {
        pointEditMode = !pointEditMode;
        drawingPane.setPointEditMode(pointEditMode);
    }

    /**
     * Sets up all the mouse and keyboard handlers
     * This is for interacting with shapes
     */
    private void setupMouseHandlers() {
        // Mouse press handler
        drawingPane.setOnMousePressed(e -> {
            lastX = e.getX();
            lastY = e.getY();

            // Right mouse button - show context menu
            if (e.isSecondaryButtonDown()) {
                Shape selectedShape = drawingPane.getSelectedShape();
                if (selectedShape != null) {
                    showContextMenu(selectedShape, e);
                }
                return;
            }

            // Shape creation mode
            if (currentShapeType != ShapeType.NONE) {
                createShape(e.getX(), e.getY());
            } else {
                // Point edit mode
                if (pointEditMode && drawingPane.getSelectedShape() != null) {
                    // Try to select a control point
                    boolean pointSelected = drawingPane.selectControlPointAt(e.getX(), e.getY());
                    
                    if (pointSelected) {
                        isDragging = true;
                        isDraggingControlPoint = true;
                        return;
                    }
                }
                
                // Selection mode - select shape
                drawingPane.selectShapeAt(e.getX(), e.getY());

                // If we clicked on a shape, start dragging
                if (drawingPane.getSelectedShape() != null) {
                    isDragging = true;
                    isDraggingControlPoint = false;
                }
            }
        });

        // Mouse drag handler
        drawingPane.setOnMouseDragged(e -> {
            if (isDragging) {
                // Calculate movement
                double deltaX = e.getX() - lastX;
                double deltaY = e.getY() - lastY;

                if (isDraggingControlPoint) {
                    // Move control point
                    drawingPane.moveSelectedControlPoint(deltaX, deltaY);
                } else {
                    // Move entire shape
                    drawingPane.moveSelectedShape(deltaX, deltaY);
                }

                // Update last position
                lastX = e.getX();
                lastY = e.getY();
            }
        });

        // Mouse release handler
        drawingPane.setOnMouseReleased(e -> {
            isDragging = false;
            isDraggingControlPoint = false;
        });

        // Mouse wheel handler (resize)
        drawingPane.setOnScroll(e -> {
            if (drawingPane.getSelectedShape() != null && !pointEditMode) {
                // Scale factor
                double scaleFactor = e.getDeltaY() > 0 ? 1.05 : 0.95;

                // Resize shape
                drawingPane.resizeSelectedShape(scaleFactor);
            }
        });
        
        // Keyboard handler
        drawingPane.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case P:
                    // Toggle point edit mode
                    togglePointEditMode();
                    break;
                case ESCAPE:
                    // Exit point edit mode
                    if (pointEditMode) {
                        togglePointEditMode();
                    }
                    break;
                default:
                    break;
            }
        });
        
        // Set focusTraversable to receive keyboard events
        drawingPane.setFocusTraversable(true);
    }

    /**
     * Shows the right-click menu for a shape
     *
     * @param shape the shape you right-clicked on
     * @param event the mouse event with position info
     */
    private void showContextMenu(Shape shape, MouseEvent event) {
        // If a context menu is already open, hide it and don't show a new one
        if (currentContextMenu != null) {
            currentContextMenu.hide();
            return;
        }
        
        // Create new context menu
        ContextMenu contextMenu = new ContextMenu();
        currentContextMenu = contextMenu;

        // Fill color option
        MenuItem colorItem = new MenuItem("Change fill color");
        colorItem.setOnAction(e -> changeShapeColor(shape));

        // Rotation option
        MenuItem rotateItem = new MenuItem("Rotate 45°");
        rotateItem.setOnAction(e -> rotateShape(shape, 45));
        
        // Toggle point edit mode option
        MenuItem editPointsItem = new MenuItem(pointEditMode ? "Disable point editing" : "Enable point editing");
        editPointsItem.setOnAction(e -> togglePointEditMode());

        // Delete shape option
        MenuItem deleteItem = new MenuItem("Delete shape");
        deleteItem.setOnAction(e -> deleteSelectedShape());

        // Add options to menu
        contextMenu.getItems().addAll(colorItem, rotateItem, new SeparatorMenuItem(), editPointsItem, new SeparatorMenuItem(), deleteItem);

        // Add handler to reset currentContextMenu when menu is hidden
        contextMenu.setOnHidden(e -> currentContextMenu = null);

        // Show context menu
        contextMenu.show(drawingPane, event.getScreenX(), event.getScreenY());
    }

    /**
     * Opens a color picker to change a shape's color
     *
     * @param shape the shape to change color
     */
    private void changeShapeColor(Shape shape) {
        // Create color picker
        ColorPicker colorPicker = new ColorPicker(shape.getFillColor());

        // Create dialog
        Dialog<Color> dialog = new Dialog<>();
        dialog.setTitle("Color Selection");
        dialog.setHeaderText("Choose shape fill color");

        // Dialog buttons
        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        // Dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Color:"), 0, 0);
        grid.add(colorPicker, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // Result conversion
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return colorPicker.getValue();
            }
            return null;
        });

        // Show dialog and handle result
        Optional<Color> result = dialog.showAndWait();
        result.ifPresent(color -> {
            shape.setFillColor(color);
            drawingPane.redraw();
        });
    }

    /**
     * Rotates a shape by a specific angle
     *
     * @param shape the shape to rotate
     * @param degrees how many degrees to rotate
     */
    private void rotateShape(Shape shape, double degrees) {
        shape.rotate(degrees);
        drawingPane.redraw();
    }

    /**
     * Creates a new shape where you clicked
     *
     * @param x X coordinate where you clicked
     * @param y Y coordinate where you clicked
     */
    private void createShape(double x, double y) {
        Shape shape = null;

        switch (currentShapeType) {
            case CIRCLE:
                shape = new Circle(x, y, 50);
                break;
            case RECTANGLE:
                shape = new Rectangle(x - 50, y - 30, 100, 60);
                break;
            case POLYGON:
                // Show dialog to choose number of sides
                int sides = showPolygonDialog();
                if (sides > 0) {
                    shape = new Polygon(x, y, sides, 50);
                } else {
                    return; // Polygon creation canceled
                }
                break;
            default:
                return;
        }

        drawingPane.addShape(shape);

        // Switch to selection mode after creating shape
        setCurrentShapeType(ShapeType.NONE);
    }
    
    /**
     * Shows a dialog to choose how many sides for a polygon
     *
     * @return number of sides, or -1 if you canceled
     */
    private int showPolygonDialog() {
        // Create dialog
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Polygon");
        dialog.setHeaderText("Choose number of polygon sides");
        
        // Dialog buttons
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        // Dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Sides input field
        javafx.scene.control.Spinner<Integer> sidesSpinner = new javafx.scene.control.Spinner<>(3, 20, 6);
        sidesSpinner.setEditable(true);
        
        grid.add(new Label("Number of sides:"), 0, 0);
        grid.add(sidesSpinner, 1, 0);
        
        dialog.getDialogPane().setContent(grid);
        
        // Set focus to input field
        javafx.application.Platform.runLater(() -> sidesSpinner.requestFocus());
        
        // Result conversion
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return sidesSpinner.getValue();
            }
            return -1;
        });
        
        // Show dialog and return result
        Optional<Integer> result = dialog.showAndWait();
        return result.orElse(-1);
    }

    /**
     * Deletes the shape you have selected.
     * Nothing happens if no shape is selected.
     */
    private void deleteSelectedShape() {
        drawingPane.deleteSelectedShape();
    }

    /**
     * Sets which kind of shape will be created next
     *
     * @param type the type of shape to create
     */
    private void setCurrentShapeType(ShapeType type) {
        this.currentShapeType = type;
        
        // Disable point edit mode when changing mode
        if (pointEditMode) {
            togglePointEditMode();
        }
    }

    /**
     * Removes all shapes from the drawing
     * (asks for confirmation first)
     */
    private void clearCanvas() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Are you sure you want to delete all shapes?");
        alert.setContentText("This operation cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            drawingPane.clearShapes();
        }
    }


    /**
     * Saves your drawing to a file
     * so you can open it later
     */
    private void saveFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Shapes");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Shape editor files", "*.shp")
        );

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
                out.writeObject(drawingPane.getShapes());

                // Save confirmation
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Saved");
                alert.setHeaderText(null);
                alert.setContentText("Shapes saved to file " + file.getName());
                alert.showAndWait();

            } catch (IOException e) {
                // Save error
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error saving file");
                alert.setContentText("Cannot save file: " + e.getMessage());
                alert.showAndWait();

                e.printStackTrace();
            }
        }
    }

    /**
     * Opens a saved drawing from a file
     */
    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Shapes");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Shape editor files", "*.shp")
        );

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                // Load shapes
                @SuppressWarnings("unchecked")
                java.util.List<Shape> shapes = (java.util.List<Shape>) in.readObject();

                // Set loaded shapes
                drawingPane.setShapes(shapes);

                // Load confirmation
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Loaded");
                alert.setHeaderText(null);
                alert.setContentText("Shapes loaded from file " + file.getName());
                alert.showAndWait();

            } catch (IOException | ClassNotFoundException e) {
                // Load error
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error loading file");
                alert.setContentText("Cannot load file: " + e.getMessage());
                alert.showAndWait();

                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the main UI panel containing all components.
     *
     * @return the main UI panel
     */
    public BorderPane getMainPanel() {
        return mainPanel;
    }

    /**
     * List of the different shape types you can create.
     * Helps keep track of which shape button is selected.
     */
    /**
     * Enum representing the different types of shapes that can be created.
     */
    public enum ShapeType {
        /** No shape selected */
        NONE,
        /** Circle shape */
        CIRCLE,
        /** Rectangle shape */
        RECTANGLE,
        /** Polygon shape */
        POLYGON
    }
}
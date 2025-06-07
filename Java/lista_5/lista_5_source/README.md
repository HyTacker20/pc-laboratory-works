# JavaFX Shape Editor

A graphical editor application for creating, manipulating, and saving geometric shapes using JavaFX.

## Project Overview

The JavaFX Shape Editor is a desktop application that allows users to create, edit, and manipulate various geometric shapes on a canvas. The application follows the Model-View-Controller (MVC) architectural pattern and provides a user-friendly interface for working with shapes.

### Key Features

- Create different types of shapes (Circle, Rectangle, Polygon)
- Select, move, resize, and rotate shapes
- Change shape colors
- Save and load shapes to/from files in JSON format
- Undo/redo functionality
- User guide and information dialogs

### Project Structure

The project is organized into the following packages:

- `com.shapeeditor`: Main application class
- `com.shapeeditor.model`: Shape classes and factory
- `com.shapeeditor.view`: UI components
- `com.shapeeditor.controller`: Application controllers
- `com.shapeeditor.persistence`: File I/O and JSON conversion

## Setup Instructions

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- JavaFX SDK 11 or higher
- Gson library for JSON processing

### Building the Project

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/javafx-shape-editor.git
   cd javafx-shape-editor
   ```

2. Ensure JavaFX is properly set up in your development environment.

3. Compile the project:
   ```
   javac -d bin --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml com/shapeeditor/*.java com/shapeeditor/*/*.java
   ```

4. Run the application:
   ```
   java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -cp bin:lib/* com.shapeeditor.ShapeEditorApplication
   ```

## Usage Guide

### Creating Shapes

1. Select the shape type from the toolbar (Circle, Rectangle, or Polygon).
2. Click on the canvas to place the shape.
3. For polygons, click multiple times to add vertices, and double-click to finish.

### Selecting and Manipulating Shapes

1. Click on a shape to select it. A blue border will appear around the selected shape.
2. Drag the shape to move it.
3. Use the toolbar buttons or keyboard shortcuts to:
   - Rotate the shape (R key)
   - Resize the shape (S key)
   - Change the shape's color (C key)
   - Delete the shape (Delete key)

### Saving and Loading

1. Use File > Save to save your shapes to a JSON file.
2. Use File > Open to load shapes from a previously saved file.
3. Use File > New to clear the canvas and start a new drawing.

## Documentation Generation

The project uses both Javadoc and Doxygen for documentation generation.

### Generating Javadoc

To generate Javadoc documentation:

```bash
mkdir -p docs/javadoc
javadoc -d docs/javadoc -sourcepath . -subpackages com.shapeeditor
```

This will create HTML documentation in the `docs/javadoc` directory.

### Generating Doxygen Documentation

To generate Doxygen documentation:

1. Ensure you have Doxygen installed on your system.
2. Run the following command from the project root:

```bash
doxygen Doxyfile
```

This will create HTML documentation in the `docs/doxygen/html` directory.

## Contributing

Contributions to the JavaFX Shape Editor are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
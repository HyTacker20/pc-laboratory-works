# Documentation Guide for JavaFX Shape Editor

This guide explains how to maintain and generate documentation for the JavaFX Shape Editor project using both Javadoc and Doxygen.

## Javadoc Documentation

### Writing Javadoc Comments

Javadoc comments are used to document Java classes, interfaces, methods, and fields. They begin with `/**` and end with `*/`.

#### Class Documentation

```java
/**
 * Class description goes here.
 * You can include multiple lines of text.
 * 
 * @author Your Name
 * @version 1.0
 */
public class MyClass {
    // Class implementation
}
```

#### Method Documentation

```java
/**
 * Method description goes here.
 * 
 * @param paramName Description of the parameter
 * @return Description of the return value
 * @throws ExceptionType Description of when this exception is thrown
 */
public ReturnType methodName(ParamType paramName) throws ExceptionType {
    // Method implementation
}
```

#### Field Documentation

```java
/** Description of the field */
private FieldType fieldName;
```

### Javadoc Tags

Common Javadoc tags include:

- `@param paramName description` - Documents a method parameter
- `@return description` - Documents the return value
- `@throws exceptionType description` - Documents an exception that may be thrown
- `@author name` - Specifies the author
- `@version number` - Specifies the version
- `@see reference` - Adds a "See Also" entry
- `@since version` - Specifies when this feature was introduced
- `@deprecated reason` - Marks the feature as deprecated

## Doxygen Documentation

Doxygen can process Javadoc comments, but it also supports additional features and commands.

### Doxygen-Specific Features

#### Grouping

You can group related elements together:

```java
/**
 * @defgroup drawing Drawing Components
 * @brief Components related to drawing on the canvas
 */

/**
 * @ingroup drawing
 */
public class DrawingCanvas {
    // Class implementation
}
```

#### Cross-Referencing

You can create links to other elements:

```java
/**
 * See the DrawingCanvas class for more information.
 * 
 * @see DrawingCanvas
 */
```

#### Including Images

```java
/**
 * Class that handles the main view.
 * 
 * @image html screenshot.png "Screenshot of the main view"
 */
```

## Generating Documentation

### Using the Provided Scripts

We've provided scripts to generate both Javadoc and Doxygen documentation:

- For Unix/Linux/macOS: Run `./generate_docs.sh`
- For Windows: Run `generate_docs.bat`

These scripts will create documentation in the following locations:
- Javadoc: `docs/javadoc/index.html`
- Doxygen: `docs/doxygen/html/index.html`

### Manual Generation

#### Javadoc

To manually generate Javadoc documentation:

```bash
mkdir -p docs/javadoc
javadoc -d docs/javadoc -sourcepath . -subpackages com.shapeeditor
```

#### Doxygen

To manually generate Doxygen documentation:

```bash
doxygen Doxyfile
```

## Best Practices

1. **Be Consistent**: Use a consistent style for all documentation.
2. **Document Public API**: Always document public classes, methods, and fields.
3. **Keep It Updated**: Update documentation when you change code.
4. **Be Concise**: Write clear, concise descriptions.
5. **Use Examples**: Include examples where appropriate.
6. **Document Exceptions**: Document all exceptions that may be thrown.
7. **Document Parameters**: Document all parameters and return values.

## Viewing Documentation

After generating the documentation, you can view it by opening the following files in your web browser:

- Javadoc: `docs/javadoc/index.html`
- Doxygen: `docs/doxygen/html/index.html`
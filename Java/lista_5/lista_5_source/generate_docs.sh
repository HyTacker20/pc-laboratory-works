#!/bin/bash

# Script to generate both Javadoc and Doxygen documentation for the JavaFX Shape Editor

echo "Generating documentation for JavaFX Shape Editor..."

# Create directories if they don't exist
mkdir -p docs/javadoc
mkdir -p docs/doxygen

# Generate Javadoc documentation
echo "Generating Javadoc documentation..."
javadoc -d docs/javadoc -sourcepath . -subpackages com.shapeeditor

# Check if Javadoc generation was successful
if [ $? -eq 0 ]; then
    echo "Javadoc documentation generated successfully in docs/javadoc/"
else
    echo "Error generating Javadoc documentation"
    exit 1
fi

# Check if Doxygen is installed
if command -v doxygen &> /dev/null; then
    echo "Generating Doxygen documentation..."
    doxygen Doxyfile
    
    # Check if Doxygen generation was successful
    if [ $? -eq 0 ]; then
        echo "Doxygen documentation generated successfully in docs/doxygen/html/"
    else
        echo "Error generating Doxygen documentation"
        exit 1
    fi
else
    echo "Doxygen is not installed. Please install Doxygen to generate Doxygen documentation."
    echo "Javadoc documentation is still available in docs/javadoc/"
fi

echo "Documentation generation complete."
echo "Javadoc: docs/javadoc/index.html"
echo "Doxygen: docs/doxygen/html/index.html"
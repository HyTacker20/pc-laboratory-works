@echo off
REM Batch script to generate both Javadoc and Doxygen documentation for the JavaFX Shape Editor

echo Generating documentation for JavaFX Shape Editor...

REM Create directories if they don't exist
if not exist docs\javadoc mkdir docs\javadoc
if not exist docs\doxygen mkdir docs\doxygen

REM Generate Javadoc documentation
echo Generating Javadoc documentation...
javadoc -d docs\javadoc -sourcepath src\main\java -subpackages com.shapeeditor

REM Check if Javadoc generation was successful
if %ERRORLEVEL% EQU 0 (
    echo Javadoc documentation generated successfully in docs\javadoc\
) else (
    echo Error generating Javadoc documentation
    exit /b 1
)

REM Check if Doxygen is installed
where doxygen >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Generating Doxygen documentation...
    doxygen Doxyfile
    
    REM Check if Doxygen generation was successful
    if %ERRORLEVEL% EQU 0 (
        echo Doxygen documentation generated successfully in docs\doxygen\html\
    ) else (
        echo Error generating Doxygen documentation
        exit /b 1
    )
) else (
    echo Doxygen is not installed. Please install Doxygen to generate Doxygen documentation.
    echo Javadoc documentation is still available in docs\javadoc\
)

echo Documentation generation complete.
echo Javadoc: docs\javadoc\index.html
echo Doxygen: docs\doxygen\html\index.html

pause
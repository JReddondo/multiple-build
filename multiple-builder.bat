@echo off
REM Script to execute Multiple Builder in Windows

REM jar path (same directory as .bat)
set JAR_PATH=%~dp0multiple-builder-1.0.jar

if not exist "%JAR_PATH%" (
    echo Error: JAR not found:
    echo %JAR_PATH%
    exit /b 1
)

REM Pass all arguments directly to the JAR
java -jar "%JAR_PATH%" %*

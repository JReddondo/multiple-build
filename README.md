# Multiple Builder Application

A Java application that executes `clean build` for multiple Maven and Gradle projects in a root directory.

## Features

- ✅ Automatically detects Maven projects (pom.xml) and Gradle projects (build.gradle/build.gradle.kts)
- ✅ Executes `clean install` for Maven and `clean build` for Gradle
- ✅ Supports wrappers (mvnw, gradlew) and global commands
- ✅ Compatible with Windows and Linux/Mac
- ✅ Generates a summary of successful and failed builds
- ✅ Creates a detailed log file with all execution information
- ✅ Uses SLF4J with Logback for professional logging

## Requirements

- Java 21 or higher
- Maven (to compile this project)
- Maven and/or Gradle installed (to build the target projects)

## Build

```bash
mvn clean package
```

This will generate an executable JAR at `target/multiple-builder-1.0-SNAPSHOT.jar`.

## Installation as Terminal Command

### Option 1: Using the provided wrapper scripts (Recommended)

The project includes `multiple-builder.bat` (Windows) and `multiple-builder.sh` (Linux/Mac) scripts that simplify execution.

#### Windows

1. After building the project with `mvn clean package`, copy both files to a directory in your PATH:
   - `multiple-builder.bat`
   - `target\multiple-builder-1.0.jar`
   
   For example, create `C:\tools\multiple-builder\` and add it to your PATH, or use an existing directory like `C:\Program Files\multiple-builder\`

2. Verify installation:
   ```batch
   multiple-builder -V
   multiple-builder --help
   ```

#### Linux/Mac

1. After building the project with `mvn clean package`, copy both files to a directory:
   ```bash
   sudo mkdir -p /usr/local/lib/multiple-builder
   sudo cp target/multiple-builder-1.0.jar /usr/local/lib/multiple-builder/
   sudo cp multiple-builder.sh /usr/local/bin/multiple-builder
   sudo chmod +x /usr/local/bin/multiple-builder
   ```

2. Edit `/usr/local/bin/multiple-builder` to point to the correct JAR location:
   ```bash
   JAR_PATH="/usr/local/lib/multiple-builder/multiple-builder-1.0.jar"
   ```

3. Verify installation:
   ```bash
   multiple-builder -V
   multiple-builder --help
   ```

### Option 2: Create your own wrapper script

#### Windows

Create a file `multiple-builder.bat` in a directory that's in your PATH:

```batch
@echo off
java -jar "C:\path\to\multiple-builder-1.0.jar" %*
```

Update the path to match your JAR file location.

#### Linux/Mac

Create a file `multiple-builder` in `/usr/local/bin/`:

```bash
#!/bin/bash
java -jar /path/to/multiple-builder-1.0.jar "$@"
```

Update the path to match your JAR file location, then make it executable:
```bash
chmod +x /usr/local/bin/multiple-builder
```

## Usage

### Basic Syntax

```bash
multiple-builder [OPTIONS]
```

### Options

- `-p, --path <path>` (required): Root directory containing the projects to build
- `-a, --app <prefix>` (optional): Prefix for the log file name
- `-l, --log-path <path>` (optional): Directory where the log file will be saved (default: root directory)
- `-h, --help`: Shows help information
- `-V, --version`: Shows version information

### Examples

**Example 1: Show version**
```bash
multiple-builder -V
```
Output: `1.0`

**Example 2: Show help**
```bash
multiple-builder --help
```

**Example 3: Basic build**
```bash
multiple-builder -p D:\projects
```
Generates in `D:\projects`:
- `multiple_build_2026_01_13_10_21.log` (report)
- `multiple_build_execution_2026_01_13_10_21.log` (execution log)
**Example 4: With application prefix**
```bash
multiple-builder -p D:\projects -a myproject
```
Generates in `D:\projects`:
- `myproject_multiple_build_2026_01_13_10_21.log` (report)
- `myproject_multiple_build_execution_2026_01_13_10_21.log` (execution log)

**Example 5: With custom log path**
```bash
multiple-builder -p D:\projects -l C:\logs
```
Generates in `C:\logs`:
- `multiple_build_2026_01_13_10_21.log` (report)
- `multiple_build_execution_2026_01_13_10_21.log` (execution log)

**Example 6: On Linux/Mac**
```bash
multiple-builder --path /home/user/projects --app backend
```
Generates in `/home/user/projects`:
- `backend_multiple_build_2026_01_13_10_21.log` (report)
- `backend_multiple_build_execution_2026_01_13_10_21.log` (execution log)

## Expected Directory Structure

```
root/
├── project1/           (Maven)
│   └── pom.xml
├── project2/           (Gradle)
│   └── build.gradle
├── project3/           (Gradle with Kotlin)
│   └── build.gradle.kts
└── project4/           (Maven with wrapper)
    ├── pom.xml
    ├── mvnw
    └── mvnw.cmd
```

## Output

### Console
The application displays on the console:
- Detected projects
- Progress of each build
- Final summary with:
  - Total projects
  - Successful builds (with duration)
  - Failed builds (with error message)

### Log File
A log file is generated with the format `[app_]multiple_build_yyyy_MM_dd_HH_mm.log` containing:
- Execution date and time
- Total projects processed
- Summary of successful and failed builds
- List of successful projects with duration
- List of failed projects with errors and paths
- Detailed results of each project

### Execution Log File
An execution log file is also generated with the format `[app_]multiple_build_execution_yyyy_MM_dd_HH_mm.log` containing:
- All application logs (INFO, WARN, ERROR levels)
- Detailed execution trace
- Build progress information
- Both files share the same timestamp and prefix to be easily paired

**Example**: If you run with `-a myapp`, you will get:
- `myapp_multiple_build_2026_01_13_14_30.log` (summary report)
- `myapp_multiple_build_execution_2026_01_13_14_30.log` (detailed execution log)

## Technologies Used

- **Picocli**: Command-line argument handling
- **SLF4J**: Logging API
- **Logback**: Logging implementation
- **Maven Shade Plugin**: Creation of executable JAR with all dependencies

## Exit Code

- `0`: All builds were successful
- `1`: At least one build failed or no projects were found

## Notes

- The application automatically detects the operating system and uses the appropriate commands
- Prioritizes the use of wrappers (mvnw/gradlew) over global commands
- Detailed build logs are shown in DEBUG mode
- Both log files (report and execution) share the same timestamp and prefix for easy identification and pairing

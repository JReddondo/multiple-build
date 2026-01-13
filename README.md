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

## Installation

### Download from GitHub Releases (Recommended)

Download the latest release from the [GitHub Releases page](../../releases/latest). Each release includes:
- `multiple-builder-{x}.jar` - The executable JAR file
- `multiple-builder.bat` - Windows wrapper script
- `multiple-builder.sh` - Linux/Mac wrapper script

#### Windows Installation

1. **Download the files:**
   - Download `multiple-builder-{x}.jar`
   - Download `multiple-builder.bat`

2. **Choose a location:**
   - Create a directory for the application (e.g., `C:\tools\multiple-builder`)
   - Place both files in that directory

3. **Add to PATH:**
   - Right-click on "This PC" → Properties → Advanced system settings
   - Click "Environment Variables"
   - Under "User variables" or "System variables", select "Path" and click "Edit"
   - Click "New" and add the path to your directory (e.g., `C:\tools\multiple-builder`)
   - Click "OK" to save all dialogs
   - **Restart your terminal/command prompt**

4. **Verify installation:**
   ```batch
   multiple-builder -V
   multiple-builder --help
   ```

#### Linux/Mac Installation

1. **Download the files:**
   - Download `multiple-builder-{x}.jar`
   - Download `multiple-builder.sh`

2. **Choose a location:**
   ```bash
   # Option A: User directory (no sudo required)
   mkdir -p ~/tools/multiple-builder
   cd ~/tools/multiple-builder
   # Move downloaded files here
   
   # Option B: System-wide (requires sudo)
   sudo mkdir -p /usr/local/bin
   # Move downloaded files here
   ```

3. **Make the script executable:**
   ```bash
   chmod +x multiple-builder.sh
   ```

4. **Add to PATH (if using Option A):**
   
   Add this line to your shell configuration file:
   - **For bash**: Add to `~/.bashrc` or `~/.bash_profile`
   - **For zsh**: Add to `~/.zshrc`
   
   ```bash
   export PATH="$HOME/tools/multiple-builder:$PATH"
   ```
   
   Reload your shell configuration:
   ```bash
   source ~/.bashrc  # or source ~/.zshrc for zsh
   ```
   
   > **Note:** If you chose Option B (`/usr/local/bin`), it's typically already in your PATH, so this step is not needed.

5. **Verify installation:**
   ```bash
   multiple-builder.sh -V
   multiple-builder.sh --help
   ```

### Build from Source

If you prefer to build from source:

```bash
mvn clean package
```

This will generate an executable JAR at `target/multiple-builder-{x}.jar`.

After building, follow the installation steps above but use the JAR file from the `target` directory and the wrapper scripts from the project root.

### Advanced: Create a custom wrapper script

If you want to customize the wrapper script or run the JAR directly:

#### Windows

Create a file `multiple-builder.bat` in a directory that's in your PATH:

```batch
@echo off
java -jar "C:\path\to\multiple-builder-{x}.jar" %*
```

Update the path to match your JAR file location.

#### Linux/Mac

Create a file `multiple-builder` in a directory in your PATH (e.g., `/usr/local/bin/`):

```bash
#!/bin/bash
java -jar /path/to/multiple-builder-{x}.jar "$@"
```

Update the path to match your JAR file location, then make it executable:
```bash
chmod +x /usr/local/bin/multiple-builder
```

#### Running directly with Java

You can always run the JAR directly without a wrapper:
```bash
java -jar multiple-builder-{x}.jar -p /path/to/projects
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

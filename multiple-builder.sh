#!/usr/bin/env bash
# Script to execute Multiple Builder in Linux/Mac

set -e

# Directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_PATH="$SCRIPT_DIR/multiple-builder-1.0.jar"

# Check that the jar exists
if [ ! -f "$JAR_PATH" ]; then
    echo "Error: JAR not found:"
    echo "  $JAR_PATH"
    exit 1
fi

# Pass all arguments directly to the JAR
java -jar "$JAR_PATH" "$@"

#!/bin/bash

# Build script for WCCOAJava Driver using CMake
# Requires: cmake, API_ROOT and JAVA_HOME environment variables

set -e

# Check prerequisites
if [ "$API_ROOT" = "" ]; then
  echo "ERROR: env var \$API_ROOT is not set"
  echo "Example: export API_ROOT=/opt/WinCC_OA/3.20/api"
  exit 1
fi

if [ "$JAVA_HOME" = "" ]; then
  echo "ERROR: env var \$JAVA_HOME is not set"
  echo "Example: export JAVA_HOME=/usr/lib/jvm/java-11-openjdk"
  exit 1
fi

# Check if cmake is available
if ! command -v cmake &> /dev/null; then
  echo "ERROR: cmake not found in PATH"
  echo "Please install cmake version 3.1 or higher"
  echo "  Ubuntu/Debian: sudo apt-get install cmake"
  echo "  RHEL/CentOS: sudo yum install cmake"
  exit 1
fi

echo "=== Build Configuration ==="
echo "API_ROOT: $API_ROOT"
echo "JAVA_HOME: $JAVA_HOME"
echo "CMake version: $(cmake --version | head -1)"
echo ""

# Source the environment setup
if [ -f "./make.env" ]; then
  echo "Sourcing make.env for additional environment setup..."
  source ./make.env
fi

# Create and enter build directory
BUILD_DIR="build"
if [ -d "$BUILD_DIR" ]; then
  echo "Cleaning existing build directory..."
  rm -rf "$BUILD_DIR"
fi

mkdir -p "$BUILD_DIR"
cd "$BUILD_DIR"

# Run CMake
echo ""
echo "=== Running CMake Configuration ==="
cmake ..

# Build
echo ""
echo "=== Building ==="
cmake --build . --config RelWithDebInfo

# Create output directory
if [ ! -d "../../Builds" ]; then
  mkdir -p ../../Builds
fi

# Copy outputs
echo ""
echo "=== Installing to ../Builds ==="
if [ -f "WCCOAjavadrv" ]; then
  cp WCCOAjavadrv ../../Builds/
  echo "  Copied WCCOAjavadrv"
fi

if [ -f "libWCCOAjavadrv.so" ]; then
  cp libWCCOAjavadrv.so ../../Builds/
  echo "  Copied libWCCOAjavadrv.so"
fi

# Copy to API_ROOT/../bin
INSTALL_DIR="$API_ROOT/../bin"
echo ""
echo "=== Installing to $INSTALL_DIR ==="
if [ ! -d "$INSTALL_DIR" ]; then
  echo "Creating installation directory: $INSTALL_DIR"
  mkdir -p "$INSTALL_DIR"
fi

if [ -f "../../Builds/WCCOAjavadrv" ]; then
  sudo cp ../../Builds/WCCOAjavadrv "$INSTALL_DIR/"
  echo "  Copied WCCOAjavadrv to $INSTALL_DIR"
fi

if [ -f "../../Builds/libWCCOAjavadrv.so" ]; then
  sudo cp ../../Builds/libWCCOAjavadrv.so "$INSTALL_DIR/"
  echo "  Copied libWCCOAjavadrv.so to $INSTALL_DIR"
fi

echo ""
echo "=== Build Complete ==="
echo "Output files in: ../Builds/"
ls -lh ../../Builds/
echo ""
echo "Installed files in: $INSTALL_DIR"
ls -lh "$INSTALL_DIR"/WCCOAjavadrv "$INSTALL_DIR"/libWCCOAjavadrv.so 2>/dev/null || echo "  (files not found)"
echo ""

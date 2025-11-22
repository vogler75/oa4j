# Build Instructions for WCCOAJava Manager

## Migration to CMake Build System

This project has been upgraded to use CMake instead of the legacy Makefile system to be compatible with **WinCC OA 3.20 API**.

## Prerequisites

### Required Software
- CMake 3.1 or higher
- C++ compiler (gcc 11+ recommended)
- Java JDK (with JAVA_HOME set)
- WinCC OA 3.20 API

### Required Environment Variables

```bash
# Path to WinCC OA API installation
export API_ROOT=/opt/WinCC_OA/3.20/api

# Path to Java JDK
export JAVA_HOME=/path/to/java/jdk
```

## Building

### Quick Build
Simply run the build script:

```bash
./build.sh
```

The script will:
1. Verify environment variables are set
2. Configure the project with CMake
3. Build both the executable and shared library
4. Copy output files to `../Builds/`

### Manual CMake Build
If you prefer to use CMake directly:

```bash
# Create build directory
mkdir -p build
cd build

# Configure
cmake ..

# Build
cmake --build . --config RelWithDebInfo

# Install outputs
cp WCCOAJavaManager ../../Builds/
cp libWCCOAjava.so ../../Builds/
```

## Output Files

The build produces two files in `../Builds/`:
- **WCCOAJavaManager** - The standalone manager executable
- **libWCCOAjava.so** - The shared library for JNI integration

## Changes from Previous Version

### What Changed
1. **Build System**: Migrated from Makefile to CMake
   - Old: `make clean && make`
   - New: `cmake .. && cmake --build .`

2. **CMakeLists.txt**: Updated to use WinCC OA 3.20 API structure
   - Removed dependency on Manager.mk
   - Uses `add_manager()` macro from CMakeDefines.txt
   - Properly links to MANAGER_LIBS defined in API
   - Adds Java include paths and libjvm linkage

3. **Build Script**: Replaced make.sh with build.sh
   - make.sh now redirects to build.sh for compatibility
   - build.sh performs environment checks
   - Automatic output file installation

### What Stayed the Same
- Source code files (no changes needed)
- Output file names and locations
- Runtime behavior

## Troubleshooting

### "cmake: command not found"
Install cmake:
- **Ubuntu/Debian**: `sudo apt-get install cmake`
- **RHEL/CentOS**: `sudo yum install cmake`

### "API_ROOT is not set"
Set the environment variable:
```bash
export API_ROOT=/opt/WinCC_OA/3.20/api
```

### "JAVA_HOME is not set"
Set the environment variable:
```bash
export JAVA_HOME=$(readlink -f $(which javac) | sed 's:/bin/javac::')
```

### "Could not find libjvm.so"
Ensure JAVA_HOME points to a valid JDK installation with libjvm.so in:
- `$JAVA_HOME/lib/server/libjvm.so` or
- `$JAVA_HOME/jre/lib/server/libjvm.so`

## Legacy Build System

The old Makefile-based build system (using `Manager.mk`) is no longer supported in WinCC OA 3.20. All projects must migrate to CMake.

For reference, the old build used:
```makefile
include $(API_ROOT)/Manager.mk
```

This has been replaced with:
```cmake
include(${API_ROOT}/CMakeDefines.txt)
```

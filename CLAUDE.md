# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

WinCC OA for Java (oa4j) is a Java API for connecting to Siemens WinCC Open Architecture SCADA system. The project bridges Java applications with the WinCC OA native API using JNI (Java Native Interface). It consists of three main components:

1. **Native C++ Components** (`Native/`): JNI bridge and manager implementations
2. **Java Library** (`Java/`): Java API and runtime (`winccoa-java.jar`)
3. **Examples** (`Examples/`): Sample implementations in Java, Scala, and Clojure

**Note:** A valid WinCC OA API license is required to use this library.

## Architecture

### Three Execution Modes

1. **Manager Mode** (`Native/Manager`): Standalone Java programs that connect to WinCC OA
   - Uses `JManager` class to initialize and manage the connection
   - Programs run as WinCC OA managers (WCCOAJavaManager executable)
   - Common pattern: `JManager.init(args).start()` → do work → `JManager.stop()`

2. **Driver Mode** (`Native/Driver`): Framework for implementing WinCC OA drivers in Java
   - Uses `JDriver` or `JDriverSimple` classes
   - Connects WinCC OA to peripheral devices for data exchange

3. **Control Extension Mode** (`Native/CtrlExt`): Call Java functions from WinCC OA Control scripts
   - Uses `ExternHdlFunction` interface
   - Called via `javaCall()` or `javaCallAsync()` from Control scripts

### Key Java API Patterns

The `JClient` class provides a fluent API for WinCC OA operations:

```java
// Read datapoint values
JClient.dpGet().add("ExampleDP_Trend1.").await()

// Write datapoint values
JClient.dpSet().add("ExampleDP_Trend1.", 123.45).send()

// Subscribe to datapoint changes (hotlink)
JClient.dpConnect()
  .add("ExampleDP_Trend1.")
  .action((JDpHLGroup hotlink) -> { /* handle changes */ })
  .connect()

// Query with SQL-like syntax
JClient.dpQuery().query("SELECT '_online.._value' FROM 'ExampleDP_*'").await()

// Historical data
JClient.dpGetPeriod().add("ExampleDP_Trend1.").await()
```

All operations support both synchronous (`.await()`) and asynchronous (`.send()`) execution.

### Native/Java Bridge

- **LibJava/**: Core JVM integration code shared across all native components
- JNI layer connects C++ WinCC OA API to Java objects
- Native code in `at_rocworks_oa4j_jni_*.cpp` files implements JNI methods
- Java classes in `at.rocworks.oa4j.jni` package mirror native API structures

## Build Commands

### Java Components

```bash
# Build Java library (from Java/ directory)
cd Java
./make.sh      # Linux/Mac
make.bat       # Windows

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=ApiTestDpGet

# Compile without running tests
mvn package -Dmaven.test.skip=true
```

**Output:** `Java/lib/winccoa-java-1.0-SNAPSHOT.jar`

### Native C++ Components (Linux)

**Prerequisites:**
- CMake 3.1+, GCC 11+
- Java JDK with `JAVA_HOME` set
- WinCC OA 3.20+ API with `API_ROOT` set

```bash
# Build Manager
cd Native/Manager
export API_ROOT=/opt/WinCC_OA/3.20/api
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
./build.sh
```

**Output:** `Native/Builds/WCCOAJavaManager` and `libWCCOAjava.so`

### Windows Build

1. Set `JAVA_HOME` environment variable
2. Run `vc-start_3.XX.bat` from `Native/Tools/`
3. Open solution from appropriate `Native/Manager/build*` directory
4. Build to produce `WCCOAjava.dll` and `WCCOAJavaManager.exe`

## Version Compatibility

| WinCC OA Version | Build Directory |
|------------------|-----------------|
| 3.20+            | `Native/Manager/build/` (CMake) |
| 3.19             | `Native/Manager/build/` |
| 3.18             | `Native/Manager/build-3.18/` |
| < 3.18           | `Native/Manager/build-pre-318/` |

## Running Java Managers

### Command Line Arguments

```bash
WCCOAJavaManager -proj <project-id> [-num <number>] [-path <dir>] [-debug]
                 [-cp <classpath>] [-c <classname>] [-lp <libpath>] [-ud <userdir>]
```

- `-proj`: WinCC OA project name/ID (required)
- `-class|-c`: Initial class to load (defaults to "Main")
- `-classpath|-cp`: Java classpath
- `-libpath|-lp`: Library path (must include `WCCOAjava.dll`/`.so`)

### Example

```bash
WCCOAJavaManager -proj MyProject -cp bin:bin/winccoa-java.jar -c ApiTestDpConnect
```

### WinCC OA Configuration

Add to project config file (`config` or `config.level`):

```ini
[java]
classPath = "bin;bin/winccoa-java-1.0-SNAPSHOT.jar"
# Optional JVM options (space-separated)
jvmOption = "-Xmx512m --enable-native-access=ALL-UNNAMED"
# For complex setups, use a separate file
configFile = "config.java"
```

### Logs

Logs are written to `WCCILjava1.0.log` (and `.err`) in:
- Project `log/` directory if it exists
- Current directory otherwise

## Development Tips

### Variable Types

Java variable types in `at.rocworks.oa4j.var` package map to WinCC OA types:
- `FloatVar`, `IntegerVar`, `LongVar`, `UIntegerVar`
- `TextVar`, `TimeVar`, `CharVar`
- `BitVar`, `Bit32Var`, `Bit64Var`
- `DynVar` - Dynamic arrays
- `DpIdentifierVar` - Datapoint identifiers

Use `Variable.valueOf()` for type conversion.

### Thread Safety

`JClient` is thread-safe. Callbacks are processed in a separate thread to avoid blocking the main WinCC OA event loop.

### Manager Initialization Pattern

```java
public static void main(String[] args) throws Exception {
    JManager m = new JManager();
    m.init(args).start();
    // Your code here
    m.stop();
}
```

### Debugging

- Use `JDebug.out.info()` for logging (not `System.out.println()`)
- Set `-debug` flag for verbose JNI layer output
- Check manager logs in project `log/` directory
- Common issue: Missing JVM DLL in PATH (Windows) or LD_LIBRARY_PATH (Linux)

## License

GNU Affero General Public License v3.0

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

WinCC OA for Java (oa4j) is a Java API for connecting to Siemens WinCC Open Architecture SCADA system. The project bridges Java applications with the WinCC OA native API using JNI (Java Native Interface). It consists of three main components:

1. **Native C++ Components** (`Native/`): JNI bridge and manager implementations
2. **Java Library** (`Java/`): Java API and runtime (`winccoa-java.jar`)
3. **Examples** (`Examples/`): Sample implementations in Java, Scala, and Clojure

## Architecture

### Three Execution Modes

The library supports three different integration modes with WinCC OA:

1. **Manager Mode** (`Native/Manager`): Standalone Java programs that connect to WinCC OA
   - Uses `JManager` class to initialize and manage the connection
   - Programs run as WinCC OA managers (WCCOAJavaManager executable)
   - Common pattern: `JManager.init(args).start()` → do work → `JManager.stop()`

2. **Driver Mode** (`Native/Driver`): Framework for implementing WinCC OA drivers in Java
   - Uses `JDriver` or `JDriverSimple` classes
   - Connects WinCC OA to peripheral devices for data exchange
   - Driver implementations exist in separate repositories (MQTT, Kafka, EPICS)

3. **Control Extension Mode** (`Native/CtrlExt`): Call Java functions from WinCC OA Control scripts
   - Extends WinCC OA Control language with Java functions
   - Uses `ExternHdlFunction` interface
   - Called via `javaCall()` or `javaCallAsync()` from Control scripts

### Key Java API Patterns

The `JClient` class provides a fluent API for WinCC OA operations:

- **dpGet**: Read datapoint values
  ```java
  JClient.dpGet().add("ExampleDP_Trend1.").await()
  ```

- **dpSet**: Write datapoint values
  ```java
  JClient.dpSet().add("ExampleDP_Trend1.", 123.45).send()
  ```

- **dpConnect**: Subscribe to datapoint changes (hotlink)
  ```java
  JClient.dpConnect()
    .add("ExampleDP_Trend1.")
    .action((JDpHLGroup hotlink) -> { /* handle changes */ })
    .connect()
  ```

- **dpQuery**: Query datapoints with SQL-like syntax
- **dpGetPeriod**: Historical data queries

All operations support both synchronous (`.await()`) and asynchronous (`.send()`) execution.

### Native/Java Bridge

- **LibJava/**: Core JVM integration code shared across all native components
- JNI layer connects C++ WinCC OA API to Java objects
- Native code in `at_rocworks_oa4j_jni_*.cpp` files implements JNI methods
- Java classes in `at.rocworks.oa4j.jni` package mirror native API structures

## Build System

### Java Components

**Prerequisites:**
- Java JDK 8 (required for Java 8 compatibility)
- Maven 3.x
- Internet connection for Maven dependencies

**Build:**
```bash
cd Java
./make.sh
```

This runs: `mvn package dependency:copy-dependencies`

**Output:** `Java/lib/winccoa-java-1.0-SNAPSHOT.jar` (rename to `winccoa-java.jar` for deployment)

**Run Tests:**
```bash
cd Java
mvn test
```

Test classes are in `Java/src/test/java/` and include examples for all API operations.

### Native C++ Components (Linux)

**Build System:** CMake (migrated from Makefile for WinCC OA 3.20+ compatibility)

**Prerequisites:**
- CMake 3.1+
- GCC 11+ (C++ compiler)
- Java JDK (with JAVA_HOME set)
- WinCC OA 3.20 API (with API_ROOT set)

**Environment Setup:**
```bash
export API_ROOT=/opt/WinCC_OA/3.20/api
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
```

**Build Manager (Native/Manager):**
```bash
cd Native/Manager
./build.sh
```

This creates:
- `WCCOAJavaManager` - Standalone manager executable
- `libWCCOAjava.so` - Shared library for JNI integration

Outputs are placed in `Native/Builds/`

**Build Driver (Native/Driver):**
```bash
cd Native/Driver
./make.sh
```

Uses legacy Makefile system (pre-3.19 versions).

### Windows Build

Native components on Windows use Visual Studio:
1. Set `JAVA_HOME` environment variable
2. Run `vc-start_3.XX.bat` from `Native/Tools/` to start Visual Studio
3. Open solution from appropriate build directory:
   - `Native/Manager/build/` for WinCC OA 3.19+
   - `Native/Manager/build-3.18/` for WinCC OA 3.18
   - `Native/Manager/build-pre-318/` for earlier versions
4. Build solution to produce `WCCOAjava.dll` and `WCCOAjava.exe` (or `WCCOAJavaManager.exe` for 3.19+)

## Version Compatibility

- **WinCC OA 3.20+**: Use CMake build system (`Native/Manager/build/`)
- **WinCC OA 3.19**: Use Visual Studio solution in `build/` directory
- **WinCC OA 3.18**: Use Visual Studio solution in `build-3.18/` directory
- **WinCC OA < 3.18**: Use Visual Studio solution in `build-pre-318/` directory

The CMake migration was necessary because WinCC OA 3.20 API removed `Manager.mk` support.

## Running Java Managers

### Command Line Arguments

- `-proj <project-id>`: WinCC OA project name/ID (required)
- `-num <number>`: Manager number
- `-path <dir>`: Project directory (optional)
- `-debug`: Enable detailed debug output
- `-userdir|-ud <dir>`: Set `-Duser.dir` (defaults to project directory)
- `-libpath|-lp <path>`: Set `-Djava.library.path` (must include `WCCOAjava.dll`/`.so`)
- `-classpath|-cp <path>`: Set `-Djava.class.path`
- `-class|-c <classname>`: Initial class to load (must have `main()` method, defaults to "Main")

### Example Execution

```bash
# Linux
WCCOAJavaManager -proj MyProject -cp bin:bin/winccoa-java.jar -c ApiTestDpConnect

# Windows
WCCOAJavaManager -proj 08ae7439-cd23-4780-96fd-8ba337c0685b-SIM -cp bin\winccoa-java.jar -c ApiTestDpGet
```

### Logs

Logs are written to `WCCILjava1.0.log` (and `.err` for errors) in:
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

Use `Variable.valueOf()` for type conversion (not `Integer.valueOf()` etc.).

### Thread Safety

`JClient` is designed to be thread-safe. Callback functions are processed in a separate thread to avoid blocking the main WinCC OA event loop.

### Manager Initialization Pattern

Standard pattern for all manager-based programs:

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

## Git Branch Strategy

- **master**: Main branch for releases and stable code
- **WinCC OA version branches**: May exist for version-specific compatibility

Current branch: `WinCCOA-3.20`

## License

GNU Affero General Public License v3.0

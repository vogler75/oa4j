# WinCC OA for Java (oa4j)

Java API for connecting to Siemens WinCC Open Architecture SCADA system.

**Note:** Requires a valid WinCC OA API license.

## Prerequisites

- Java JDK 11+ with `JAVA_HOME` set
- Apache Maven 3.6+
- WinCC OA 3.18+ with `API_ROOT` set (for native components)
- CMake 3.1+ and GCC 11+ (Linux)

## Build

### Java Library

```bash
cd Java
./make.sh
```

Output: `Java/lib/winccoa-java-1.0-SNAPSHOT.jar`

### Native Manager

```bash
cd Native/Manager
export API_ROOT=/opt/WinCC_OA/3.20/api
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
./build.sh
```

Output: `Native/Builds/WCCOAjava` and `libWCCOAjava.so`

### Native Driver

```bash
cd Native/Driver
export API_ROOT=/opt/WinCC_OA/3.20/api
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
./build.sh
```

Output: `Native/Builds/WCCOAjavadrv` and `libWCCOAjavadrv.so`

## Install

Install the Java library to your local Maven repository:

```bash
cd Java
./install.sh
```

This installs `winccoa-java-1.0-SNAPSHOT.jar` as:
- Group ID: `at.rocworks`
- Artifact ID: `winccoa-java`
- Version: `1.0-SNAPSHOT`

You can then add it as a dependency to your Maven projects:

```xml
<dependency>
    <groupId>at.rocworks</groupId>
    <artifactId>winccoa-java</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

```java
import at.rocworks.oa4j.WinCCOA;

public class Example {
    public static void main(String[] args) throws Exception {
        WinCCOA oa = WinCCOA.connect(args);

        oa.dpSet("ExampleDP_Trend1.", 123.45);

        oa.disconnect();
    }
}
```

See `Examples/` directory for more examples in Java, Scala, and Clojure.

For detailed documentation, see `CLAUDE.md` and the `docs/` directory.

## License

GNU Affero General Public License v3.0

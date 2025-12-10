# WinCC OA Java API Reference

This document describes the complete Java API for interacting with Siemens WinCC Open Architecture SCADA system. The API is accessed through the `WinCCOA` class, which provides a single, unified interface for all operations.

## Table of Contents

1. [Overview](#overview)
2. [Getting Started](#getting-started)
3. [Connection Management](#connection-management)
4. [Reading Datapoints](#reading-datapoints)
5. [Writing Datapoints](#writing-datapoints)
6. [Subscribing to Changes](#subscribing-to-changes)
7. [Querying Datapoints](#querying-datapoints)
8. [Historical Data](#historical-data)
9. [Datapoint Metadata](#datapoint-metadata)
10. [Variable Types](#variable-types)
11. [Error Handling](#error-handling)
12. [Complete Examples](#complete-examples)

---

## Overview

The WinCC OA Java API consists of:

- **WinCCOA** - Main class providing all datapoint operations and connection management
- **Variable Types** - Type-safe wrappers for WinCC OA data types (FloatVar, IntegerVar, TextVar, etc.)
- **Builder Classes** - Fluent API for complex operations (JDpGet, JDpSet, JDpConnect, etc.)
- **Response Classes** - Containers for operation results (JDpMsgAnswer, JDpVCItem, etc.)

**Key Characteristics:**

- Thread-safe: All operations can be called from any thread
- Singleton pattern: Only one WinCCOA connection per JVM
- Fluent API: Builder pattern for complex operations
- Synchronous and asynchronous modes: Choose `.await()` or `.send()`

---

## Getting Started

### Minimal Example

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.var.Variable;

public class Main {
    public static void main(String[] args) throws Exception {
        // Connect to WinCC OA
        WinCCOA oa = WinCCOA.connect(args);

        // Read a datapoint
        Variable value = oa.dpGet("ExampleDP_Arg1.");
        System.out.println("Value: " + value);

        // Write a datapoint
        oa.dpSet("ExampleDP_Arg1.", 42);

        // Disconnect when done
        oa.disconnect();
    }
}
```

### Complete Program Template

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.var.*;
import at.rocworks.oa4j.base.*;

public class MyApplication {
    public static void main(String[] args) throws Exception {
        // 1. Connect to WinCC OA
        WinCCOA oa = WinCCOA.connect(args);

        try {
            // 2. Your application logic here
            runApplication(oa);

        } catch (Exception e) {
            WinCCOA.logStackTrace(e);
        } finally {
            // 3. Cleanup
            oa.disconnect();
        }
    }

    private static void runApplication(WinCCOA oa) throws Exception {
        // Your code here
    }
}
```

---

## Connection Management

### WinCCOA Class

`at.rocworks.oa4j.WinCCOA`

The WinCCOA class is the main entry point for all operations. It manages the connection lifecycle and provides all API methods.

### Connecting to WinCC OA

#### connect(String[] args)

Connects using command-line arguments.

```java
public static WinCCOA connect(String[] args) throws Exception
```

**Parameters:**
- `args` - Command-line arguments (e.g., from `main()`)

**Supported Arguments:**

| Argument | Description | Example |
|----------|-------------|---------|
| `-proj <name>` | Project name (required) | `-proj MyProject` |
| `-path <dir>` | Project directory path | `-path /opt/projects/myproj` |
| `-num <n>` | Manager number | `-num 2` |
| `-db` | Use DB_MAN instead of API_MAN | `-db` |
| `-noinit` | Skip resource initialization | `-noinit` |
| `-debug` | Enable debug output | `-debug` |

**Returns:** WinCCOA instance

**Throws:** Exception if connection fails

**Example:**

```java
// From command line: java Main -proj MyProject -num 1
WinCCOA oa = WinCCOA.connect(args);
```

---

#### connect(String project)

Connects with just a project name (manager number defaults to 1).

```java
public static WinCCOA connect(String project) throws Exception
```

**Parameters:**
- `project` - WinCC OA project name

**Example:**

```java
WinCCOA oa = WinCCOA.connect("MyProject");
```

---

#### connect(String project, int managerNumber)

Connects with a project name and manager number.

```java
public static WinCCOA connect(String project, int managerNumber) throws Exception
```

**Parameters:**
- `project` - WinCC OA project name
- `managerNumber` - Manager number (used to run multiple instances)

**Example:**

```java
WinCCOA oa = WinCCOA.connect("MyProject", 2);
```

---

#### disconnect()

Disconnects from WinCC OA and releases all resources.

```java
public void disconnect()
```

**Example:**

```java
oa.disconnect();
```

**Important:** Always call `disconnect()` before application exit, preferably in a `finally` block.

---

#### getInstance()

Gets the current WinCCOA instance (if connected).

```java
public static WinCCOA getInstance()
```

**Returns:** The WinCCOA instance, or `null` if not connected

**Example:**

```java
WinCCOA oa = WinCCOA.getInstance();
if (oa != null && oa.isConnected()) {
    // Use the instance
}
```

---

### Status & Information Methods

#### isConnected()

Checks if currently connected to WinCC OA.

```java
public boolean isConnected()
```

**Returns:** `true` if connected

**Example:**

```java
if (oa.isConnected()) {
    System.out.println("Connected!");
}
```

---

#### isActive()

Checks if connected to the active host in a redundant system. In non-redundant systems, always returns `true`.

```java
public Boolean isActive()
```

**Returns:** `true` if connected to active host

---

#### getProjectPath()

Gets the absolute path to the WinCC OA project directory.

```java
public String getProjectPath()
```

**Returns:** Absolute path to project directory

---

#### getConfigDir()

Gets the absolute path to the config directory.

```java
public String getConfigDir()
```

**Returns:** Absolute path to config directory

---

#### getLogDir()

Gets the absolute path to the log directory.

```java
public String getLogDir()
```

**Returns:** Absolute path to log directory

---

#### getManagerName()

Gets the manager name.

```java
public String getManagerName()
```

**Returns:** Manager name (e.g., "WCCOAjava1")

---

#### getManagerNumber()

Gets the manager number.

```java
public int getManagerNumber()
```

**Returns:** Manager number

---

#### getConfigValue(String key)

Retrieves a value from the WinCC OA configuration file.

```java
public String getConfigValue(String key)
```

**Parameters:**
- `key` - Configuration key (e.g., `"java:classPath"`)

**Returns:** Configuration value, or `null` if not found

**Example:**

```java
String classpath = oa.getConfigValue("java:classPath");
```

---

#### getConfigValue(String key, String defaultValue)

Retrieves a configuration value with a default fallback.

```java
public String getConfigValue(String key, String defaultValue)
```

**Parameters:**
- `key` - Configuration key
- `defaultValue` - Default value if not found

**Returns:** Configuration value, or default

---

## Reading Datapoints

### Single Datapoint - Simple

#### dpGet(String dp)

Reads a single datapoint value synchronously.

```java
public Variable dpGet(String dp)
```

**Parameters:**
- `dp` - Datapoint name (e.g., `"ExampleDP_Arg1."` or `"System1:ExampleDP_Arg1."`)

**Returns:** Variable containing the value, or `null` if not found

**Example:**

```java
// Read and cast to specific type
Variable value = oa.dpGet("ExampleDP_Arg1.");
if (value instanceof FloatVar) {
    double d = ((FloatVar) value).getValue();
    System.out.println("Value: " + d);
}

// Read integer
IntegerVar intVal = (IntegerVar) oa.dpGet("ExampleDP_Int.");
int i = intVal.getValue();

// Read text
TextVar textVal = (TextVar) oa.dpGet("ExampleDP_Text.");
String s = textVal.getValue();

// Read boolean
BitVar boolVal = (BitVar) oa.dpGet("ExampleDP_Bool.");
boolean b = boolVal.getValue();
```

---

### Multiple Datapoints - Simple

#### dpGet(List&lt;String&gt; dps)

Reads multiple datapoint values synchronously.

```java
public List<Variable> dpGet(List<String> dps)
```

**Parameters:**
- `dps` - List of datapoint names

**Returns:** List of Variable values in the same order as the input

**Example:**

```java
List<String> dpNames = Arrays.asList(
    "ExampleDP_Arg1.",
    "ExampleDP_Arg2.",
    "ExampleDP_Arg3."
);

List<Variable> values = oa.dpGet(dpNames);
for (int i = 0; i < values.size(); i++) {
    System.out.println(dpNames.get(i) + " = " + values.get(i));
}
```

---

### Multiple Datapoints - Builder API

#### dpGet()

Creates a fluent builder for reading multiple datapoints with detailed control.

```java
public JDpGet dpGet()
```

**Returns:** JDpGet builder instance

**Builder Methods:**

| Method | Description |
|--------|-------------|
| `add(String dp)` | Add a datapoint to read |
| `add(String dp, String attr)` | Add datapoint with specific attribute |
| `await()` | Execute synchronously and wait for result |
| `send()` | Execute asynchronously (fire and forget) |

**Example:**

```java
// Read multiple datapoints
JDpMsgAnswer answer = oa.dpGet()
    .add("ExampleDP_Arg1.")
    .add("ExampleDP_Arg2.")
    .add("ExampleDP_Trend1.:_offline.._value")  // specific attribute
    .await();

// Process results
for (JDpVCItem item : answer) {
    System.out.println(item.getDpName() + " = " + item.getVariable());
}
```

**Attribute Examples:**

```java
// Online value (default)
oa.dpGet().add("ExampleDP_Arg1.").await();

// Specific attribute
oa.dpGet().add("ExampleDP_Arg1.:_online.._value").await();
oa.dpGet().add("ExampleDP_Arg1.:_online.._stime").await();  // timestamp

// Offline/historical attribute
oa.dpGet().add("ExampleDP_Trend1.:_offline.._value").await();
```

---

## Writing Datapoints

### Single Datapoint - Fire and Forget

#### dpSet(String dp, Object value)

Writes a single datapoint value asynchronously (no confirmation).

```java
public JDpSet dpSet(String dp, Object value)
```

**Parameters:**
- `dp` - Datapoint name
- `value` - Value to write (automatically converted to appropriate type)

**Returns:** JDpSet instance (already sent)

**Supported Value Types:**
- Java primitives: `int`, `long`, `float`, `double`, `boolean`, `char`
- Java objects: `Integer`, `Long`, `Float`, `Double`, `Boolean`, `String`
- WinCC OA types: `FloatVar`, `IntegerVar`, `TextVar`, etc.
- Arrays and Lists (for dynamic types)

**Example:**

```java
// Write numbers
oa.dpSet("ExampleDP_Float.", 3.14159);
oa.dpSet("ExampleDP_Int.", 42);
oa.dpSet("ExampleDP_Long.", 1234567890L);

// Write text
oa.dpSet("ExampleDP_Text.", "Hello WinCC OA");

// Write boolean
oa.dpSet("ExampleDP_Bool.", true);

// Write time
oa.dpSet("ExampleDP_Time.", new TimeVar(System.currentTimeMillis()));

// Write to nested element
oa.dpSet("ExampleDP_Struct.temperature", 25.5);
oa.dpSet("ExampleDP_Struct.status", "OK");
```

---

### Single Datapoint - With Confirmation

#### dpSetWait(String dp, Object value)

Writes a single datapoint value synchronously and waits for confirmation.

```java
public int dpSetWait(String dp, Object value)
```

**Parameters:**
- `dp` - Datapoint name
- `value` - Value to write

**Returns:**
- `0` - Success
- Non-zero - Error code

**Example:**

```java
int result = oa.dpSetWait("ExampleDP_Arg1.", 42);
if (result == 0) {
    System.out.println("Write successful");
} else {
    System.err.println("Write failed with error code: " + result);
}
```

---

### Multiple Datapoints - Arrays

#### dpSet(String[] dps, Object[] values)

Writes multiple datapoints asynchronously (fire and forget).

```java
public JDpSet dpSet(String[] dps, Object[] values)
```

**Parameters:**
- `dps` - Array of datapoint names
- `values` - Array of values (must match length of `dps`)

**Returns:** JDpSet instance (already sent)

**Example:**

```java
String[] dps = {"ExampleDP_Arg1.", "ExampleDP_Arg2.", "ExampleDP_Arg3."};
Object[] values = {100, 200, 300};
oa.dpSet(dps, values);
```

---

#### dpSetWait(String[] dps, Object[] values)

Writes multiple datapoints synchronously with confirmation.

```java
public int dpSetWait(String[] dps, Object[] values)
```

**Parameters:**
- `dps` - Array of datapoint names
- `values` - Array of values

**Returns:** 0 on success, non-zero on failure

---

### Multiple Datapoints - Builder API

#### dpSet()

Creates a fluent builder for writing multiple datapoints.

```java
public JDpSet dpSet()
```

**Returns:** JDpSet builder instance

**Builder Methods:**

| Method | Description |
|--------|-------------|
| `add(String dp, Object value)` | Add a datapoint/value pair |
| `send()` | Execute asynchronously (fire and forget) |
| `await()` | Execute synchronously and wait for confirmation |

**Example:**

```java
// Fire and forget
oa.dpSet()
    .add("ExampleDP_Arg1.", 100)
    .add("ExampleDP_Arg2.", "Hello")
    .add("ExampleDP_Arg3.", true)
    .add("Device1.temperature", 25.5)
    .add("Device1.humidity", 60.0)
    .send();

// Wait for confirmation
JDpMsgAnswer answer = oa.dpSet()
    .add("ExampleDP_Arg1.", 42)
    .add("ExampleDP_Arg2.", "Test")
    .await();

if (answer.getErrorCode() == 0) {
    System.out.println("All writes successful");
}
```

---

## Subscribing to Changes

### Basic Subscription (Hotlink)

#### dpConnect()

Creates a subscription that triggers a callback when datapoint values change.

```java
public JDpConnect dpConnect()
```

**Returns:** JDpConnect builder instance

**Builder Methods:**

| Method | Description |
|--------|-------------|
| `add(String dp)` | Add datapoint to subscribe to |
| `action(Consumer<JDpHLGroup> callback)` | Set the callback function |
| `connect()` | Activate the subscription |
| `disconnect()` | Deactivate the subscription |

**Callback Parameter:**
- `JDpHLGroup` - Group of changed datapoints (can contain 1 or more items)

**Example - Simple Subscription:**

```java
// Subscribe to a single datapoint
JDpConnect connection = oa.dpConnect()
    .add("ExampleDP_Arg1.")
    .action(hlg -> {
        for (JDpVCItem item : hlg) {
            System.out.println(item.getDpName() + " changed to " + item.getVariable());
        }
    })
    .connect();

// Later, to stop receiving updates:
connection.disconnect();
```

**Example - Multiple Datapoints:**

```java
JDpConnect connection = oa.dpConnect()
    .add("ExampleDP_Arg1.")
    .add("ExampleDP_Arg2.")
    .add("ExampleDP_Arg3.")
    .action(hlg -> {
        hlg.forEach(item -> {
            String name = item.getDpName();
            Variable value = item.getVariable();
            TimeVar timestamp = item.getTimeStamp();
            System.out.println(name + " = " + value + " at " + timestamp);
        });
    })
    .connect();
```

**Example - Extracting Values:**

```java
oa.dpConnect()
    .add("Device1.temperature")
    .add("Device1.pressure")
    .action(hlg -> {
        for (JDpVCItem item : hlg) {
            if (item.getDpName().contains("temperature")) {
                FloatVar temp = (FloatVar) item.getVariable();
                System.out.println("Temperature: " + temp.getValue() + " °C");
            } else if (item.getDpName().contains("pressure")) {
                FloatVar press = (FloatVar) item.getVariable();
                System.out.println("Pressure: " + press.getValue() + " bar");
            }
        }
    })
    .connect();
```

**Important Notes:**

- The callback runs in a separate thread
- Callbacks should execute quickly to avoid blocking the event loop
- Store the `JDpConnect` instance to disconnect later
- Always disconnect subscriptions when no longer needed

---

### Alert Subscription

#### alertConnect()

Creates a subscription for WinCC OA alerts (alarms).

```java
public JAlertConnect alertConnect()
```

**Returns:** JAlertConnect builder instance

**Example:**

```java
JAlertConnect alertConn = oa.alertConnect()
    .action(alert -> {
        System.out.println("Alert: " + alert);
    })
    .connect();

// Later:
alertConn.disconnect();
```

---

## Querying Datapoints

### Basic Query

#### dpQuery(String query)

Executes a WinCC OA SQL-like query.

```java
public JDpQuery dpQuery(String query)
```

**Parameters:**
- `query` - Query string in WinCC OA query syntax

**Returns:** JDpQuery instance (use `.await()` to get results)

**Query Syntax:**

```
SELECT '<attribute>' [, '<attribute>', ...] FROM '<pattern>' [WHERE <condition>]
```

**Example - Simple Query:**

```java
// Get all datapoints matching pattern
JDpMsgAnswer answer = oa.dpQuery(
    "SELECT '_online.._value' FROM 'ExampleDP_*'"
).await();

for (JDpVCItem item : answer) {
    System.out.println(item.getDpName() + " = " + item.getVariable());
}
```

**Example - Multiple Attributes:**

```java
// Get value and timestamp
JDpMsgAnswer answer = oa.dpQuery(
    "SELECT '_online.._value', '_online.._stime' FROM 'ExampleDP_Arg*'"
).await();

for (JDpVCItem item : answer) {
    System.out.println(item.getDpName() + ":");
    System.out.println("  Value: " + item.getVariable());
    System.out.println("  Time: " + item.getTimeStamp());
}
```

**Example - With Filter:**

```java
// Filter by datapoint type
JDpMsgAnswer answer = oa.dpQuery(
    "SELECT '_online.._value' FROM 'ExampleDP_*' WHERE _DPT = \"ExampleDP_Float\""
).await();
```

**Common Attributes:**

| Attribute | Description |
|-----------|-------------|
| `_online.._value` | Current value |
| `_online.._stime` | Last update timestamp |
| `_online.._status` | Status flags |
| `_offline.._value` | Historical/archive value |
| `_original.._value` | Original value (before conversions) |

---

### Query-Based Subscriptions

#### dpQueryConnectSingle(String query)

Creates a query-based subscription that triggers only on the first matching change.

```java
public JDpQueryConnect dpQueryConnectSingle(String query)
```

**Parameters:**
- `query` - Query string

**Returns:** JDpQueryConnectSingle instance

**Example:**

```java
JDpQueryConnect conn = oa.dpQueryConnectSingle(
    "SELECT '_online.._value' FROM 'ExampleDP_*'"
)
.action(hlg -> {
    // Triggers only once, even if multiple datapoints match
    System.out.println("First change detected!");
})
.connect();
```

---

#### dpQueryConnectAll(String query)

Creates a query-based subscription that triggers on all matching changes.

```java
public JDpQueryConnect dpQueryConnectAll(String query)
```

**Parameters:**
- `query` - Query string

**Returns:** JDpQueryConnectAll instance

**Example:**

```java
JDpQueryConnect conn = oa.dpQueryConnectAll(
    "SELECT '_online.._value' FROM 'Sensor_*.temperature'"
)
.action(hlg -> {
    // Triggers whenever any matching datapoint changes
    hlg.forEach(item -> {
        System.out.println(item.getDpName() + " changed to " + item.getVariable());
    });
})
.connect();
```

---

## Historical Data

### Time Period Query

#### dpGetPeriod(TimeVar start, TimeVar stop, int maxCount)

Queries historical data for a time period.

```java
public JDpGetPeriod dpGetPeriod(TimeVar start, TimeVar stop, int maxCount)
```

**Parameters:**
- `start` - Start time
- `stop` - End time
- `maxCount` - Maximum number of values to return (0 = unlimited)

**Returns:** JDpGetPeriod builder instance

**Example:**

```java
import at.rocworks.oa4j.var.TimeVar;

// Get last hour of data
TimeVar start = new TimeVar(System.currentTimeMillis() - 3600000);  // 1 hour ago
TimeVar stop = new TimeVar(System.currentTimeMillis());

JDpMsgAnswer answer = oa.dpGetPeriod(start, stop, 0)
    .add("ExampleDP_Trend1.:_offline.._value")
    .await();

for (JDpVCItem item : answer) {
    System.out.println(item.getTimeStamp() + " : " + item.getVariable());
}
```

---

#### dpGetPeriod(Date start, Date stop, int maxCount)

Queries historical data using Java Date objects.

```java
public JDpGetPeriod dpGetPeriod(Date start, Date stop, int maxCount)
```

**Example:**

```java
import java.util.Date;
import java.util.Calendar;

Calendar cal = Calendar.getInstance();
cal.add(Calendar.HOUR, -24);  // 24 hours ago
Date start = cal.getTime();
Date stop = new Date();

JDpMsgAnswer answer = oa.dpGetPeriod(start, stop, 100)
    .add("ExampleDP_Trend1.:_offline.._value")
    .add("ExampleDP_Trend2.:_offline.._value")
    .await();
```

---

#### dpGetPeriod(long start, long stop, int maxCount)

Queries historical data using millisecond timestamps.

```java
public JDpGetPeriod dpGetPeriod(long start, long stop, int maxCount)
```

**Example:**

```java
long now = System.currentTimeMillis();
long oneHourAgo = now - 3600000;

JDpMsgAnswer answer = oa.dpGetPeriod(oneHourAgo, now, 0)
    .add("ExampleDP_Trend1.:_offline.._value")
    .await();
```

---

## Datapoint Metadata

### Datapoint Names

#### dpNames(String pattern)

Gets all datapoint names matching a wildcard pattern.

```java
public String[] dpNames(String pattern)
```

**Parameters:**
- `pattern` - Wildcard pattern (`*` matches any characters)

**Returns:** Array of matching datapoint names

**Example:**

```java
// Get all datapoints starting with "Sensor"
String[] sensors = oa.dpNames("Sensor*");
for (String dp : sensors) {
    System.out.println(dp);
}

// Get all datapoints
String[] allDps = oa.dpNames("*");

// Get datapoints matching a pattern
String[] temps = oa.dpNames("*temperature*");
```

---

#### dpNames(String pattern, String type)

Gets datapoint names matching both a pattern and a type.

```java
public String[] dpNames(String pattern, String type)
```

**Parameters:**
- `pattern` - Wildcard pattern
- `type` - Datapoint type name to filter by

**Returns:** Array of matching datapoint names

**Example:**

```java
// Get all "Sensor" type datapoints starting with "Device"
String[] devices = oa.dpNames("Device*", "Sensor");
```

---

### Datapoint Existence

#### dpExists(String dpName)

Checks if a datapoint exists.

```java
public boolean dpExists(String dpName)
```

**Parameters:**
- `dpName` - Datapoint name to check

**Returns:** `true` if datapoint exists, `false` otherwise

**Example:**

```java
if (oa.dpExists("Device_1")) {
    System.out.println("Device_1 exists");
    Variable value = oa.dpGet("Device_1.value");
} else {
    System.out.println("Device_1 does not exist");
}
```

---

### Datapoint Comments

#### dpGetComment(DpIdentifierVar dpid)

Gets the multi-language comment/description for a datapoint.

```java
public LangTextVar dpGetComment(DpIdentifierVar dpid)
```

**Parameters:**
- `dpid` - Datapoint identifier

**Returns:** LangTextVar containing language-specific comments

---

## Variable Types

WinCC OA uses strongly-typed variables. All values are represented as `Variable` objects that must be cast to their specific type.

### Type Hierarchy

```
Variable (abstract base class)
├── FloatVar
├── IntegerVar
├── LongVar
├── UIntegerVar
├── TextVar
├── TimeVar
├── CharVar
├── BitVar
├── Bit32Var
├── Bit64Var
├── DpIdentifierVar
├── LangTextVar
└── DynVar (for arrays)
    ├── DynFloatVar
    ├── DynIntegerVar
    ├── DynTextVar
    └── ...
```

### Primitive Types

| Java Class | WinCC OA Type | Java Equivalent | Example |
|------------|---------------|-----------------|---------|
| `FloatVar` | float | double | `new FloatVar(3.14)` |
| `IntegerVar` | int | int | `new IntegerVar(42)` |
| `LongVar` | long | long | `new LongVar(1234567890L)` |
| `UIntegerVar` | uint | int (unsigned) | `new UIntegerVar(100)` |
| `TextVar` | string | String | `new TextVar("Hello")` |
| `TimeVar` | time | Date/timestamp | `new TimeVar(System.currentTimeMillis())` |
| `CharVar` | char | char | `new CharVar('A')` |
| `BitVar` | bit | boolean | `new BitVar(true)` |
| `Bit32Var` | bit32 | int (bitfield) | `new Bit32Var(0xFF)` |
| `Bit64Var` | bit64 | long (bitfield) | `new Bit64Var(0xFFL)` |

### Working with Variables

**Creating Variables:**

```java
FloatVar temp = new FloatVar(25.5);
IntegerVar count = new IntegerVar(100);
TextVar name = new TextVar("Device1");
BitVar active = new BitVar(true);
TimeVar now = new TimeVar(System.currentTimeMillis());
```

**Reading Values:**

```java
Variable value = oa.dpGet("ExampleDP_Float.");

// Type checking
if (value instanceof FloatVar) {
    FloatVar floatVal = (FloatVar) value;
    double d = floatVal.getValue();
    System.out.println("Float value: " + d);
}

// Or direct cast (if you know the type)
FloatVar floatVal = (FloatVar) oa.dpGet("ExampleDP_Float.");
double d = floatVal.getValue();
```

**Type Conversion:**

```java
// Using Variable.valueOf() for automatic conversion
Variable var = Variable.valueOf(42);        // Creates IntegerVar
Variable var2 = Variable.valueOf(3.14);     // Creates FloatVar
Variable var3 = Variable.valueOf("text");   // Creates TextVar
Variable var4 = Variable.valueOf(true);     // Creates BitVar
```

### Dynamic Arrays

**Creating Dynamic Arrays:**

```java
import at.rocworks.oa4j.var.*;

// Float array
DynFloatVar floatArray = new DynFloatVar();
floatArray.add(new FloatVar(1.1));
floatArray.add(new FloatVar(2.2));
floatArray.add(new FloatVar(3.3));

// Integer array
DynIntegerVar intArray = new DynIntegerVar();
intArray.add(new IntegerVar(10));
intArray.add(new IntegerVar(20));

// Text array
DynTextVar textArray = new DynTextVar();
textArray.add(new TextVar("First"));
textArray.add(new TextVar("Second"));
```

**Reading Dynamic Arrays:**

```java
DynFloatVar arr = (DynFloatVar) oa.dpGet("ExampleDP_DynFloat.");
for (int i = 0; i < arr.getSize(); i++) {
    FloatVar element = (FloatVar) arr.get(i);
    System.out.println("Element " + i + ": " + element.getValue());
}
```

**Writing Dynamic Arrays:**

```java
DynFloatVar temperatures = new DynFloatVar();
temperatures.add(new FloatVar(20.5));
temperatures.add(new FloatVar(21.3));
temperatures.add(new FloatVar(19.8));

oa.dpSet("Device1.temperatures", temperatures);
```

### Time Values

**Creating Time Variables:**

```java
// Current time
TimeVar now = new TimeVar(System.currentTimeMillis());

// Specific time
Calendar cal = Calendar.getInstance();
cal.set(2024, Calendar.JANUARY, 15, 10, 30, 0);
TimeVar specific = new TimeVar(cal.getTimeInMillis());

// From Date
Date date = new Date();
TimeVar fromDate = new TimeVar(date.getTime());
```

**Reading Time Values:**

```java
TimeVar timestamp = (TimeVar) oa.dpGet("ExampleDP_Time.");
long millis = timestamp.getTime();
Date date = new Date(millis);
System.out.println("Timestamp: " + date);
```

---

## Error Handling

### Return Codes

Most write and management operations return integer codes:

| Return Value | Meaning |
|--------------|---------|
| `0` | Success |
| Non-zero | Error code (see WinCC OA documentation) |

**Example:**

```java
int result = oa.dpSetWait("ExampleDP_Arg1.", 42);
if (result != 0) {
    WinCCOA.logError("dpSet failed with error code: " + result);
}
```

---

### Null Checks

Read operations return `null` if a datapoint doesn't exist:

```java
Variable value = oa.dpGet("NonExistent.");
if (value == null) {
    System.err.println("Datapoint not found!");
} else {
    System.out.println("Value: " + value);
}
```

---

### Exception Handling

Connection and operation errors throw exceptions:

```java
try {
    WinCCOA oa = WinCCOA.connect("MyProject");

    Variable value = oa.dpGet("ExampleDP_Arg1.");
    if (value != null) {
        System.out.println("Value: " + value);
    }

} catch (Exception e) {
    WinCCOA.logStackTrace(e);
} finally {
    if (oa != null) {
        oa.disconnect();
    }
}
```

---

### Logging

#### log(String message)

Logs an info message to the WinCC OA log system.

```java
public static void log(String message)
```

**Example:**

```java
WinCCOA.log("Application started");
```

---

#### logError(String message)

Logs an error message.

```java
public static void logError(String message)
```

**Example:**

```java
WinCCOA.logError("Failed to process data");
```

---

#### logStackTrace(Throwable exception)

Logs an exception's stack trace.

```java
public static void logStackTrace(Throwable exception)
```

**Example:**

```java
try {
    // ...
} catch (Exception e) {
    WinCCOA.logStackTrace(e);
}
```

---

#### log(ErrPrio prio, ErrCode code, String text)

Logs with specific priority and error code.

```java
public static void log(ErrPrio prio, ErrCode code, String text)
```

**Parameters:**
- `prio` - Priority level (PRIO_INFO, PRIO_WARNING, PRIO_SEVERE)
- `code` - Error code (NOERR, UNEXPECTEDSTATE, etc.)
- `text` - Message text

**Example:**

```java
import at.rocworks.oa4j.jni.ErrPrio;
import at.rocworks.oa4j.jni.ErrCode;

WinCCOA.log(ErrPrio.PRIO_WARNING, ErrCode.NOERR, "Temperature exceeded threshold");
```

---

## Complete Examples

### Example 1: Read and Write

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.var.*;

public class ReadWriteExample {
    public static void main(String[] args) throws Exception {
        WinCCOA oa = WinCCOA.connect(args);

        try {
            // Read a value
            FloatVar currentTemp = (FloatVar) oa.dpGet("Device1.temperature");
            System.out.println("Current temperature: " + currentTemp.getValue());

            // Write a value
            oa.dpSet("Device1.setpoint", 25.0);

            // Write multiple values
            oa.dpSet()
                .add("Device1.temperature", 22.5)
                .add("Device1.pressure", 1.013)
                .add("Device1.active", true)
                .send();

            WinCCOA.log("Read/Write operations completed");

        } finally {
            oa.disconnect();
        }
    }
}
```

---

### Example 2: Monitoring Changes

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.base.JDpConnect;
import at.rocworks.oa4j.var.FloatVar;

public class MonitoringExample {
    public static void main(String[] args) throws Exception {
        WinCCOA oa = WinCCOA.connect(args);

        try {
            // Monitor temperature changes
            JDpConnect tempMonitor = oa.dpConnect()
                .add("Device1.temperature")
                .action(hlg -> {
                    hlg.forEach(item -> {
                        FloatVar temp = (FloatVar) item.getVariable();
                        double value = temp.getValue();

                        if (value > 30.0) {
                            WinCCOA.logError("Temperature too high: " + value);
                        } else {
                            WinCCOA.log("Temperature: " + value);
                        }
                    });
                })
                .connect();

            // Keep running
            WinCCOA.log("Monitoring started. Press Ctrl+C to stop.");
            Thread.sleep(Long.MAX_VALUE);

        } finally {
            oa.disconnect();
        }
    }
}
```

---

### Example 3: Querying and Processing

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.base.JDpMsgAnswer;
import at.rocworks.oa4j.base.JDpVCItem;
import at.rocworks.oa4j.var.FloatVar;

public class QueryExample {
    public static void main(String[] args) throws Exception {
        WinCCOA oa = WinCCOA.connect(args);

        try {
            // Find all temperature sensors
            JDpMsgAnswer answer = oa.dpQuery(
                "SELECT '_online.._value' FROM 'Sensor_*.temperature'"
            ).await();

            double sum = 0;
            int count = 0;

            for (JDpVCItem item : answer) {
                FloatVar temp = (FloatVar) item.getVariable();
                sum += temp.getValue();
                count++;
                System.out.println(item.getDpName() + ": " + temp.getValue());
            }

            if (count > 0) {
                double average = sum / count;
                System.out.println("Average temperature: " + average);
            }

        } finally {
            oa.disconnect();
        }
    }
}
```

---

### Example 4: Historical Data Analysis

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.base.JDpMsgAnswer;
import at.rocworks.oa4j.base.JDpVCItem;
import at.rocworks.oa4j.var.FloatVar;

public class HistoricalDataExample {
    public static void main(String[] args) throws Exception {
        WinCCOA oa = WinCCOA.connect(args);

        try {
            // Get last 24 hours of data
            long now = System.currentTimeMillis();
            long dayAgo = now - (24 * 3600 * 1000);

            JDpMsgAnswer answer = oa.dpGetPeriod(dayAgo, now, 0)
                .add("Sensor1.temperature:_offline.._value")
                .await();

            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            double sum = 0;
            int count = 0;

            for (JDpVCItem item : answer) {
                FloatVar temp = (FloatVar) item.getVariable();
                double value = temp.getValue();

                min = Math.min(min, value);
                max = Math.max(max, value);
                sum += value;
                count++;
            }

            System.out.println("Statistics for last 24 hours:");
            System.out.println("  Samples: " + count);
            System.out.println("  Minimum: " + min);
            System.out.println("  Maximum: " + max);
            System.out.println("  Average: " + (sum / count));

        } finally {
            oa.disconnect();
        }
    }
}
```

---

### Example 5: Multi-Device Control

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.base.JDpConnect;
import at.rocworks.oa4j.var.BitVar;

public class MultiDeviceControl {

    private static WinCCOA oa;

    public static void main(String[] args) throws Exception {
        oa = WinCCOA.connect(args);

        try {
            // Get all devices
            String[] devices = oa.dpNames("Device_*");
            System.out.println("Found " + devices.length + " devices");

            // Initialize all devices
            for (String device : devices) {
                oa.dpSet()
                    .add(device + ".active", true)
                    .add(device + ".mode", "AUTO")
                    .add(device + ".setpoint", 25.0)
                    .send();
            }

            // Monitor all device states
            JDpConnect stateMonitor = oa.dpConnect();
            for (String device : devices) {
                stateMonitor.add(device + ".alarm");
            }
            stateMonitor
                .action(hlg -> {
                    hlg.forEach(item -> {
                        BitVar alarm = (BitVar) item.getVariable();
                        if (alarm.getValue()) {
                            handleAlarm(item.getDpName());
                        }
                    });
                })
                .connect();

            WinCCOA.log("Multi-device control active");
            Thread.sleep(Long.MAX_VALUE);

        } finally {
            oa.disconnect();
        }
    }

    private static void handleAlarm(String dpName) {
        WinCCOA.logError("ALARM: " + dpName);

        // Extract device name
        String device = dpName.substring(0, dpName.indexOf('.'));

        // Take corrective action
        oa.dpSet(device + ".mode", "SAFE");
    }
}
```

---

## Best Practices

1. **Always Disconnect:** Use try-finally blocks to ensure `disconnect()` is called

```java
WinCCOA oa = WinCCOA.connect(args);
try {
    // Your code
} finally {
    oa.disconnect();
}
```

2. **Check for Null:** Always check if `dpGet()` returns null

```java
Variable value = oa.dpGet("SomeDP.");
if (value != null) {
    // Process value
}
```

3. **Use Type Checking:** Verify variable types before casting

```java
Variable value = oa.dpGet("SomeDP.");
if (value instanceof FloatVar) {
    FloatVar fv = (FloatVar) value;
    // ...
}
```

4. **Keep Callbacks Fast:** Hotlink callbacks should execute quickly

```java
// Good - fast processing
oa.dpConnect()
    .add("DP.")
    .action(hlg -> System.out.println("Changed!"))
    .connect();

// Bad - slow processing blocks event loop
oa.dpConnect()
    .add("DP.")
    .action(hlg -> {
        Thread.sleep(10000);  // DON'T DO THIS
    })
    .connect();
```

5. **Use Batch Operations:** Write multiple values in one call

```java
// Good - single operation
oa.dpSet()
    .add("DP1.", 1)
    .add("DP2.", 2)
    .add("DP3.", 3)
    .send();

// Less efficient - three separate operations
oa.dpSet("DP1.", 1);
oa.dpSet("DP2.", 2);
oa.dpSet("DP3.", 3);
```

6. **Use Logging:** Use WinCC OA logging instead of System.out

```java
// Good
WinCCOA.log("Application started");

// Less good
System.out.println("Application started");
```

---

## Related Classes Reference

| Class | Package | Purpose |
|-------|---------|---------|
| `WinCCOA` | at.rocworks.oa4j | Main API class |
| `JDpGet` | at.rocworks.oa4j.base | Builder for reading datapoints |
| `JDpSet` | at.rocworks.oa4j.base | Builder for writing datapoints |
| `JDpConnect` | at.rocworks.oa4j.base | Builder for subscribing to changes |
| `JDpQuery` | at.rocworks.oa4j.base | Builder for queries |
| `JDpGetPeriod` | at.rocworks.oa4j.base | Builder for historical data |
| `JAlertConnect` | at.rocworks.oa4j.base | Builder for alert subscriptions |
| `JDpMsgAnswer` | at.rocworks.oa4j.base | Response container |
| `JDpVCItem` | at.rocworks.oa4j.base | Individual value/change item |
| `JDpHLGroup` | at.rocworks.oa4j.base | Group of hotlink items |
| `Variable` | at.rocworks.oa4j.var | Base class for all variables |
| `FloatVar` | at.rocworks.oa4j.var | Float variable |
| `IntegerVar` | at.rocworks.oa4j.var | Integer variable |
| `TextVar` | at.rocworks.oa4j.var | Text variable |
| `BitVar` | at.rocworks.oa4j.var | Boolean variable |
| `TimeVar` | at.rocworks.oa4j.var | Time variable |
| `DynVar` | at.rocworks.oa4j.var | Dynamic array base |
| `ErrPrio` | at.rocworks.oa4j.jni | Error priority enumeration |
| `ErrCode` | at.rocworks.oa4j.jni | Error code enumeration |

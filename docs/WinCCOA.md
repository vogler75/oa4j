# WinCCOA Unified API

The `WinCCOA` class provides a simplified, unified API for interacting with WinCC OA. It hides the internal complexity of `JManager` and `JClient`, providing a single entry point for all operations.

## Quick Start

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.var.Variable;

public class MyApp {
    public static void main(String[] args) throws Exception {
        // Connect to WinCC OA
        WinCCOA oa = WinCCOA.connect(args);

        // Read a datapoint
        Variable value = oa.dpGet("ExampleDP_Arg1.");
        System.out.println("Value: " + value);

        // Write a datapoint
        oa.dpSet("ExampleDP_Arg1.", 42);

        // Subscribe to changes
        oa.dpConnect()
            .add("ExampleDP_Arg1.")
            .action(hlg -> hlg.forEach(item ->
                System.out.println(item.getDpName() + " = " + item.getVariable())
            ))
            .connect();

        // Disconnect when done
        oa.disconnect();
    }
}
```

## Connection & Lifecycle

### Connecting

```java
// Using command-line arguments
WinCCOA oa = WinCCOA.connect(args);

// Using project name only
WinCCOA oa = WinCCOA.connect("MyProject");

// Using project name and manager number
WinCCOA oa = WinCCOA.connect("MyProject", 2);
```

### Getting the Instance

```java
// Get existing instance (returns null if not connected)
WinCCOA oa = WinCCOA.getInstance();
```

### Disconnecting

```java
oa.disconnect();
```

**Note:** Only one connection per JVM is supported (singleton pattern). Call `disconnect()` before creating a new connection.

---

## Status & Information

| Method | Description |
|--------|-------------|
| `isConnected()` | Check if connected to WinCC OA |
| `isActive()` | Check if connected to active host (redundancy) |
| `getProjectPath()` | Get project directory path |
| `getConfigDir()` | Get config directory path |
| `getLogDir()` | Get log directory path |
| `getManagerName()` | Get manager name (e.g., "WCCOAjava1") |
| `getManagerNumber()` | Get manager number |
| `getConfigValue(key)` | Get configuration value |
| `getConfigValue(key, default)` | Get configuration value with default |

```java
if (oa.isConnected()) {
    System.out.println("Project: " + oa.getProjectPath());
    System.out.println("Manager: " + oa.getManagerName());
}
```

---

## Datapoint Read Operations

### Simple Read

```java
Variable value = oa.dpGet("ExampleDP_Arg1.");
```

### Multiple Reads

```java
List<Variable> values = oa.dpGet(Arrays.asList(
    "ExampleDP_Arg1.",
    "ExampleDP_Arg2."
));
```

### Fluent Builder

```java
oa.dpGet()
    .add("ExampleDP_Arg1.")
    .add("ExampleDP_Arg2.")
    .action(answer -> {
        for (int i = 0; i < answer.size(); i++) {
            System.out.println(answer.getItem(i).getVariable());
        }
    })
    .await();
```

---

## Datapoint Write Operations

### Fire-and-Forget Write

```java
oa.dpSet("ExampleDP_Arg1.", 42);
```

### Write with Confirmation

```java
int result = oa.dpSetWait("ExampleDP_Arg1.", 42);
if (result != 0) {
    System.err.println("Write failed: " + result);
}
```

### Fluent Builder (Multiple Writes)

```java
oa.dpSet()
    .add("ExampleDP_Arg1.", 100)
    .add("ExampleDP_Arg2.", "text")
    .add("ExampleDP_Arg3.", true)
    .send();
```

---

## Datapoint Subscriptions (Hotlinks)

### Subscribe to Changes

```java
var connection = oa.dpConnect()
    .add("ExampleDP_Arg1.")
    .add("ExampleDP_Arg2.")
    .action(hlg -> {
        hlg.forEach(item -> {
            System.out.println(item.getDpName() + " changed to " + item.getVariable());
        });
    })
    .connect();

// Later, disconnect
connection.disconnect();
```

### Alert Subscription

```java
oa.alertConnect()
    .add("ExampleDP_Arg1.:_alert_hdl.._act_state")
    .action(hlg -> {
        // Handle alerts
    })
    .connect();
```

---

## Datapoint Query

### Simple Query

```java
var answer = oa.dpQuery("SELECT '_online.._value' FROM 'ExampleDP_*'").await();
```

### Query with Subscription

```java
// Triggers on all changes
oa.dpQueryConnectAll("SELECT '_online.._value' FROM 'ExampleDP_*'")
    .action(answer -> {
        // Handle changes
    })
    .connect();
```

---

## Historical Data

**Note:** To query historical data, the datapoint must have archiving enabled. Configure archiving attributes before using `dpGetPeriod`:

```java
// Configure archiving for a datapoint element (all settings in one call)
oa.dpSet()
    .add("MyDP.value:_archive.._type", 45)           // DPCONFIG_DB_ARCHIVEINFO
    .add("MyDP.value:_archive.._archive", true)      // Enable archiving
    .add("MyDP.value:_archive.1._class", "_NGA_G_EVENT")  // Archive on change
    .await();
```

### Querying Historical Data

```java
// Using timestamps (milliseconds)
long now = System.currentTimeMillis();
long oneHourAgo = now - 3600000;

oa.dpGetPeriod(oneHourAgo, now, 100)
    .add("ExampleDP_Trend1.:_offline.._value")
    .action(answer -> {
        // Process historical values
        for (int i = 0; i < answer.size(); i++) {
            var item = answer.getItem(i);
            System.out.println(item.getTime() + ": " + item.getVariable());
        }
    })
    .await();

// Using Date objects
Date start = new Date(System.currentTimeMillis() - 3600000);
Date end = new Date();
oa.dpGetPeriod(start, end, 100)
    .add("ExampleDP_Trend1.:_offline.._value")
    .await();
```

---

## Datapoint Metadata

### Find Datapoints

```java
// By pattern
String[] dps = oa.dpNames("ExampleDP_*");

// By pattern and type
String[] dps = oa.dpNames("*", "ExampleDP_Float");
```

### Check Existence

```java
if (oa.dpExists("MyDatapoint")) {
    System.out.println("Datapoint exists");
}
```

---

## Datapoint Type Management

### Create a Type

```java
import at.rocworks.oa4j.var.DpElementType;
import at.rocworks.oa4j.var.DpTypeElement;

// Build type structure
DpTypeElement root = new DpTypeElement("MyType", 1, DpElementType.RECORD);
root.addChild(new DpTypeElement("value", 2, DpElementType.FLOAT));
root.addChild(new DpTypeElement("status", 3, DpElementType.INT));

// Nested structure
DpTypeElement config = new DpTypeElement("config", 4, DpElementType.RECORD);
config.addChild(new DpTypeElement("enabled", 5, DpElementType.BIT));
root.addChild(config);

// Create the type
int result = oa.dpTypeCreate(root);
```

### Read a Type

```java
DpTypeElement typeDef = oa.dpTypeGet("MyType");
if (typeDef != null) {
    System.out.println(typeDef.toString());
}
```

### Modify a Type

```java
int typeId = oa.dpTypeNameToId("MyType");
DpTypeElement newElement = new DpTypeElement("description", 0, DpElementType.TEXT);
oa.dpTypeChange(typeId, newElement, true);  // append=true
```

### Delete a Type

```java
oa.dpTypeDelete("MyType");
// or by ID
oa.dpTypeDelete(typeId);
```

---

## Datapoint Management

### Create a Datapoint

```java
int result = oa.dpCreate("MyDatapoint", "MyType");
if (result == 0) {
    System.out.println("Created successfully");
}
```

### Delete a Datapoint

```java
oa.dpDelete("MyDatapoint");
```

---

## CNS (Common Name Service)

### Create View and Tree

```java
import at.rocworks.oa4j.var.LangTextVar;
import at.rocworks.oa4j.var.CnsDataIdentifier;

// Create display names
LangTextVar names = new LangTextVar();
names.setText(0, "My View");

// Create view
oa.cnsCreateView(null, "myView", "/", names);

// Add tree
LangTextVar treeNames = new LangTextVar();
treeNames.setText(0, "Plant A");
oa.cnsAddTree(null, "myView", "plantA", CnsDataIdentifier.Types.NO_TYPE, null, treeNames);

// Add node
LangTextVar nodeNames = new LangTextVar();
nodeNames.setText(0, "Temperature");
oa.cnsAddNode("System1:myView/plantA", "temp1",
    CnsDataIdentifier.Types.DATAPOINT, dpId, nodeNames);
```

### Query CNS

```java
// Get all views
String[] views = oa.cnsGetViews(null);

// Get trees
String[] trees = oa.cnsGetTrees(null, "myView");

// Get node
CnsNode node = oa.cnsGetNode("System1:myView/plantA/temp1");

// Search by pattern
String[] matches = oa.cnsGetNodesByName(null, "myView", "*temp*", 0, 0);
```

### CNS Observer

```java
import at.rocworks.oa4j.var.CnsObserver;

CnsObserver observer = new CnsObserver() {
    @Override
    public void onCnsChange(String path, int changeType) {
        System.out.println("CNS changed: " + path + " type=" + changeType);
    }
};

int observerId = oa.cnsAddObserver(observer);
// Later...
oa.cnsRemoveObserver(observerId);
```

---

## User & Security

```java
// Check password
int result = oa.checkPassword("admin", "password");
// 0 = valid, -1 = invalid user, -2 = wrong password

// Set user
boolean success = oa.setUserId("admin", "password");
```

---

## Logging

Logging methods are static and work before/after connection:

```java
// Full logging
WinCCOA.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "Info message");

// Convenience methods
WinCCOA.log("Info message");
WinCCOA.logError("Error message");

// Log exceptions with stack trace
try {
    // ... code that may throw
} catch (Exception e) {
    WinCCOA.logStackTrace(e);  // Logs with SEVERE priority
    // or with custom priority
    WinCCOA.logStackTrace(ErrPrio.PRIO_WARNING, ErrCode.UNEXPECTEDSTATE, e);
}
```

---

## Redundancy State Listeners

In redundant systems, you can register a callback to be notified when the manager becomes active or passive:

```java
oa.onRedundancyStateChanged(isActive -> {
    if (isActive) {
        System.out.println("Manager is now ACTIVE - taking over primary role");
        // Start processing, enable features, etc.
    } else {
        System.out.println("Manager is now PASSIVE - standby mode");
        // Stop processing, disable features, etc.
    }
});
```

The callback receives `true` when becoming active, `false` when becoming passive.

To remove a listener, keep a reference to the callback:

```java
Consumer<Boolean> callback = isActive -> {
    WinCCOA.log(isActive ? "ACTIVE" : "PASSIVE");
};

oa.onRedundancyStateChanged(callback);

// Later, remove it
oa.removeRedundancyStateListener(callback);
```

---

## Complete Example

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.var.*;

public class CompleteExample {
    public static void main(String[] args) throws Exception {
        // Connect
        WinCCOA oa = WinCCOA.connect(args);
        WinCCOA.log("Connected to: " + oa.getProjectPath());

        // Create a type
        DpTypeElement type = new DpTypeElement("SensorType", 1, DpElementType.RECORD);
        type.addChild(new DpTypeElement("temperature", 2, DpElementType.FLOAT));
        type.addChild(new DpTypeElement("humidity", 3, DpElementType.FLOAT));
        oa.dpTypeCreate(type);

        Thread.sleep(2000);

        // Create datapoints
        for (int i = 1; i <= 3; i++) {
            oa.dpCreate("Sensor_" + i, "SensorType");
        }

        Thread.sleep(1000);

        // Write values
        for (int i = 1; i <= 3; i++) {
            oa.dpSet()
                .add("Sensor_" + i + ".temperature", 20.0 + i)
                .add("Sensor_" + i + ".humidity", 50.0 + i * 5)
                .send();
        }

        // Subscribe and monitor
        var conn = oa.dpConnect()
            .add("Sensor_1.temperature")
            .add("Sensor_2.temperature")
            .action(hlg -> hlg.forEach(item ->
                WinCCOA.log("Changed: " + item.getDpName() + " = " + item.getVariable())
            ))
            .connect();

        // Simulate changes
        for (int i = 0; i < 5; i++) {
            Thread.sleep(1000);
            oa.dpSet("Sensor_1.temperature", 25.0 + i);
        }

        conn.disconnect();

        // Cleanup
        for (int i = 1; i <= 3; i++) {
            oa.dpDelete("Sensor_" + i);
        }
        Thread.sleep(1000);
        oa.dpTypeDelete("SensorType");

        oa.disconnect();
    }
}
```

---

## Complete API Reference

This section provides comprehensive descriptions of all public functions in the WinCCOA API. Each function is documented with its full signature, parameters, return values, and usage notes to facilitate LLM understanding and code generation.

---

## Connection & Lifecycle Management

### `static WinCCOA connect(String[] args) throws Exception`

**Purpose:** Establishes connection to WinCC OA using command-line arguments.

**Parameters:**
- `args` (String[]): Command-line arguments array. Supported arguments include:
  - `-proj <name>`: Project name (required)
  - `-num <number>`: Manager number (default: 1)
  - `-path <directory>`: Project path
  - `-db <number>`: Database number
  - `-debug`: Enable debug output
  - `-noinit`: Skip initialization

**Returns:** WinCCOA instance (singleton)

**Throws:**
- `Exception`: If connection fails
- `IllegalStateException`: If already connected (only one connection per JVM allowed)

**Usage Pattern:**
```java
WinCCOA oa = WinCCOA.connect(args);
```

**Notes:**
- This is a singleton - only one active connection per JVM is supported
- Call `disconnect()` before attempting to reconnect
- The connection is automatically started and ready to use after this call returns

---

### `static WinCCOA connect(String project) throws Exception`

**Purpose:** Connects to WinCC OA with a project name using default manager number (1).

**Parameters:**
- `project` (String): The WinCC OA project name or identifier

**Returns:** WinCCOA instance (singleton)

**Throws:**
- `Exception`: If connection fails
- `IllegalStateException`: If already connected

**Usage Pattern:**
```java
WinCCOA oa = WinCCOA.connect("MyProject");
```

**Notes:**
- Equivalent to `connect(project, 1)`
- Manager type defaults to API_MAN (API manager)

---

### `static WinCCOA connect(String project, int managerNumber) throws Exception`

**Purpose:** Connects to WinCC OA with specific project and manager number.

**Parameters:**
- `project` (String): The WinCC OA project name
- `managerNumber` (int): Manager instance number (typically 1-99)

**Returns:** WinCCOA instance (singleton)

**Throws:**
- `Exception`: If connection fails
- `IllegalStateException`: If already connected

**Usage Pattern:**
```java
WinCCOA oa = WinCCOA.connect("MyProject", 2); // Manager number 2
```

**Notes:**
- Use different manager numbers to run multiple Java managers in the same project
- Each manager appears as `WCCOAjava<number>` in the project

---

### `static WinCCOA getInstance()`

**Purpose:** Retrieves the current WinCCOA instance without creating a new connection.

**Parameters:** None

**Returns:**
- Current WinCCOA instance if connected
- `null` if not connected

**Usage Pattern:**
```java
WinCCOA oa = WinCCOA.getInstance();
if (oa != null && oa.isConnected()) {
    // Use existing connection
}
```

**Notes:**
- Does not throw exception if not connected (returns null instead)
- Useful for accessing connection from different parts of application

---

### `void disconnect()`

**Purpose:** Disconnects from WinCC OA and releases all resources.

**Parameters:** None

**Returns:** void

**Side Effects:**
- Closes connection to WinCC OA
- Stops internal manager
- Clears singleton instance (allows reconnection)
- All active subscriptions (hotlinks) are terminated

**Usage Pattern:**
```java
oa.disconnect();
```

**Notes:**
- Always call this before application exit to ensure clean shutdown
- After disconnect, you can call `connect()` again to establish a new connection

---

## Status & Information Functions

### `boolean isConnected()`

**Purpose:** Checks if the manager is currently connected to WinCC OA.

**Parameters:** None

**Returns:**
- `true`: Connected and operational
- `false`: Not connected or connection lost

**Usage Pattern:**
```java
if (oa.isConnected()) {
    // Safe to perform operations
}
```

---

### `Boolean isActive()`

**Purpose:** In redundant systems, checks if connected to the currently active host.

**Parameters:** None

**Returns:**
- `true`: Connected to active host
- `false`: Connected to passive host
- `null`: Not in redundant configuration

**Usage Pattern:**
```java
Boolean active = oa.isActive();
if (active != null && active) {
    // Connected to active system
}
```

**Notes:**
- Only relevant in redundant WinCC OA setups
- Use redundancy state listeners for notifications when this changes

---

### `String getProjectPath()`

**Purpose:** Returns the absolute path to the WinCC OA project directory.

**Parameters:** None

**Returns:** String containing absolute directory path (e.g., "/opt/WinCC_OA/projects/MyProject")

**Usage Pattern:**
```java
String path = oa.getProjectPath();
System.out.println("Project: " + path);
```

---

### `String getConfigDir()`

**Purpose:** Returns the absolute path to the project's config directory.

**Parameters:** None

**Returns:** String containing config directory path (e.g., "/opt/WinCC_OA/projects/MyProject/config")

**Notes:** This directory contains config files like `config`, `config.level`, etc.

---

### `String getLogDir()`

**Purpose:** Returns the absolute path to the project's log directory.

**Parameters:** None

**Returns:** String containing log directory path (e.g., "/opt/WinCC_OA/projects/MyProject/log")

**Notes:** Manager log files are written to this directory

---

### `String getManagerName()`

**Purpose:** Returns the full manager name as it appears in WinCC OA.

**Parameters:** None

**Returns:** String manager name (e.g., "WCCOAjava1", "WCCOAjava2")

**Usage Pattern:**
```java
String name = oa.getManagerName();
// Returns "WCCOAjava1" for manager number 1
```

---

### `int getManagerNumber()`

**Purpose:** Returns the manager instance number.

**Parameters:** None

**Returns:** int manager number (1-99)

**Usage Pattern:**
```java
int num = oa.getManagerNumber(); // Returns 1, 2, 3, etc.
```

---

### `String getConfigValue(String key)`

**Purpose:** Retrieves a configuration value from the project config files.

**Parameters:**
- `key` (String): Configuration key name (e.g., "general/langs")

**Returns:**
- Configuration value as String if found
- `null` if key doesn't exist

**Usage Pattern:**
```java
String langs = oa.getConfigValue("general/langs");
```

**Notes:**
- Searches config files in standard WinCC OA hierarchy
- Key format follows WinCC OA config section/key convention

---

### `String getConfigValue(String key, String defaultValue)`

**Purpose:** Retrieves a configuration value with a fallback default.

**Parameters:**
- `key` (String): Configuration key name
- `defaultValue` (String): Default value to return if key not found

**Returns:** Configuration value or defaultValue if key doesn't exist

**Usage Pattern:**
```java
String port = oa.getConfigValue("myapp/port", "8080");
```

---

## Datapoint Read Operations

### `JDpGet dpGet()`

**Purpose:** Creates a fluent builder for reading one or more datapoint values.

**Parameters:** None

**Returns:** JDpGet builder instance

**Builder Methods:**
- `add(String dp)`: Add datapoint to read (uses `_original.._value` config)
- `add(DpIdentifierVar dpid)`: Add datapoint with specific config
- `add(String dp, VariablePtr var)`: Add datapoint and bind result to VariablePtr
- `async()`: Execute asynchronously
- `action(IAnswer callback)`: Set callback for handling results
- `send()`: Send request asynchronously (fire-and-forget)
- `await()`: Send request and wait for response (blocking)

**Usage Pattern:**
```java
// Single read with await
JDpMsgAnswer answer = oa.dpGet()
    .add("ExampleDP_Arg1.")
    .await();
Variable value = answer.getItem(0).getVariable();

// Multiple reads with callback
oa.dpGet()
    .add("ExampleDP_Arg1.")
    .add("ExampleDP_Arg2.")
    .action(answer -> {
        for (int i = 0; i < answer.size(); i++) {
            System.out.println(answer.getItem(i).getVariable());
        }
    })
    .send();
```

**Notes:**
- Default config is `_original.._value` (current value without timestamp)
- Use `await()` for synchronous operation (blocks until response)
- Use `send()` + `action()` for asynchronous operation
- Thread-safe

---

### `Variable dpGet(String dp)`

**Purpose:** Reads a single datapoint value synchronously (convenience method).

**Parameters:**
- `dp` (String): Datapoint name (e.g., "ExampleDP_Arg1." or "System1:MyDP.value")

**Returns:**
- Variable containing the datapoint value
- `null` if datapoint doesn't exist or read fails

**Usage Pattern:**
```java
Variable value = oa.dpGet("ExampleDP_Arg1.");
if (value != null) {
    System.out.println("Value: " + value.toString());
}
```

**Notes:**
- This is a blocking call
- Uses `_original.._value` config by default
- Returns null on error (check logs for details)

---

### `List<Variable> dpGet(List<String> dps)`

**Purpose:** Reads multiple datapoint values synchronously.

**Parameters:**
- `dps` (List<String>): List of datapoint names to read

**Returns:** List<Variable> containing values in the same order as input list

**Usage Pattern:**
```java
List<String> dpNames = Arrays.asList("DP1.", "DP2.", "DP3.");
List<Variable> values = oa.dpGet(dpNames);
for (int i = 0; i < values.size(); i++) {
    System.out.println(dpNames.get(i) + " = " + values.get(i));
}
```

**Notes:**
- Blocking call that waits for all values
- More efficient than multiple individual dpGet() calls
- Null entries in result list indicate read failures for those specific datapoints

---

## Datapoint Write Operations

### `JDpSet dpSet()`

**Purpose:** Creates a fluent builder for writing one or more datapoint values.

**Parameters:** None

**Returns:** JDpSet builder instance

**Builder Methods:**
- `add(String dp, Object value)`: Add datapoint and value to write
- `add(String dp, Variable var)`: Add datapoint with Variable object
- `add(DpIdentifierVar dpid, Variable var)`: Add with specific config
- `timed(TimeVar time)`: Set timestamp for timed write (dpSetTimed)
- `timed(Date time)`: Set timestamp using Date object
- `async()`: Execute asynchronously
- `action(IAnswer callback)`: Set callback for confirmation
- `send()`: Send write request (fire-and-forget)
- `await()`: Send write request and wait for confirmation

**Usage Pattern:**
```java
// Fire-and-forget write
oa.dpSet()
    .add("ExampleDP_Arg1.", 42)
    .add("ExampleDP_Arg2.", "text")
    .add("ExampleDP_Arg3.", true)
    .send();

// Write with confirmation
JDpMsgAnswer answer = oa.dpSet()
    .add("ExampleDP_Arg1.", 100)
    .await();

// Timed write (historical backdating)
oa.dpSet()
    .add("ExampleDP_Trend1.", 25.5)
    .timed(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
    .send();
```

**Notes:**
- `send()` returns immediately without confirmation
- `await()` blocks until WinCC OA confirms the write
- Timed writes allow backdating values into history
- Thread-safe

---

### `JDpSet dpSet(String dp, Object value)`

**Purpose:** Writes a single datapoint value asynchronously (fire-and-forget).

**Parameters:**
- `dp` (String): Datapoint name
- `value` (Object): Value to write (auto-converted to appropriate Variable type)

**Returns:** JDpSet instance (already sent)

**Supported Value Types:**
- Integer, Long, Float, Double → numeric variables
- String → TextVar
- Boolean → BitVar
- Date → TimeVar
- Variable objects → used directly
- Arrays → DynVar

**Usage Pattern:**
```java
oa.dpSet("ExampleDP_Arg1.", 42);
oa.dpSet("ExampleDP_Text.", "Hello");
oa.dpSet("ExampleDP_Bool.", true);
```

**Notes:**
- Fire-and-forget: does not wait for confirmation
- Value is automatically converted to correct Variable type
- Returns immediately

---

### `int dpSetWait(String dp, Object value)`

**Purpose:** Writes a single datapoint value and waits for confirmation.

**Parameters:**
- `dp` (String): Datapoint name
- `value` (Object): Value to write

**Returns:**
- `0`: Success
- Non-zero: Error code

**Usage Pattern:**
```java
int result = oa.dpSetWait("ExampleDP_Arg1.", 42);
if (result != 0) {
    System.err.println("Write failed with code: " + result);
}
```

**Notes:**
- Blocking call
- Returns error code for diagnostics
- Preferred when you need to verify write succeeded

---

### `JDpSet dpSet(String[] dps, Object[] values)`

**Purpose:** Writes multiple datapoint values asynchronously (fire-and-forget).

**Parameters:**
- `dps` (String[]): Array of datapoint names
- `values` (Object[]): Array of values (must match length of dps)

**Returns:** JDpSet instance (already sent)

**Throws:** IllegalArgumentException if array lengths don't match

**Usage Pattern:**
```java
String[] dps = {"DP1.", "DP2.", "DP3."};
Object[] values = {100, "text", true};
oa.dpSet(dps, values);
```

---

### `int dpSetWait(String[] dps, Object[] values)`

**Purpose:** Writes multiple datapoint values and waits for confirmation.

**Parameters:**
- `dps` (String[]): Array of datapoint names
- `values` (Object[]): Array of values (must match length of dps)

**Returns:**
- `0`: All writes succeeded
- Non-zero: At least one write failed

**Throws:** IllegalArgumentException if array lengths don't match

**Usage Pattern:**
```java
String[] dps = {"DP1.", "DP2."};
Object[] values = {100, 200};
int result = oa.dpSetWait(dps, values);
```

---

### `JDpSet dpSet(List<Map.Entry<String, Object>> pairs)`

**Purpose:** Writes multiple datapoint/value pairs asynchronously.

**Parameters:**
- `pairs` (List<Map.Entry<String, Object>>): List of datapoint-value pairs

**Returns:** JDpSet instance (already sent)

**Usage Pattern:**
```java
List<Map.Entry<String, Object>> pairs = new ArrayList<>();
pairs.add(Map.entry("DP1.", 100));
pairs.add(Map.entry("DP2.", "text"));
oa.dpSet(pairs);
```

---

### `int dpSetWait(List<Map.Entry<String, Object>> pairs)`

**Purpose:** Writes multiple datapoint/value pairs and waits for confirmation.

**Parameters:**
- `pairs` (List<Map.Entry<String, Object>>): List of datapoint-value pairs

**Returns:**
- `0`: Success
- Non-zero: Error code

---

## Datapoint Subscriptions (Hotlinks)

### `JDpConnect dpConnect()`

**Purpose:** Creates a fluent builder for subscribing to datapoint value changes.

**Parameters:** None

**Returns:** JDpConnect builder instance

**Builder Methods:**
- `add(String dp)`: Add datapoint to subscription (uses `_online.._value` config)
- `addGroup()`: Start a new subscription group
- `addGroup(String[] dps)`: Add multiple datapoints as a group
- `action(IHotLink callback)`: Set callback for value changes
- `action(IAnswer callback)`: Set callback for initial response
- `async()`: Execute asynchronously
- `connect()`: Activate subscription
- `disconnect()`: Deactivate subscription

**Callback Signature:**
- `IHotLink.hotlink(JDpHLGroup hlg)`: Called when values change
  - `hlg` contains list of changed items
  - Each item has: dpName, dpIdentifier, variable (new value), time

**Usage Pattern:**
```java
JDpConnect connection = oa.dpConnect()
    .add("ExampleDP_Arg1.")
    .add("ExampleDP_Arg2.")
    .action(hlg -> {
        // Called whenever ANY of the subscribed datapoints change
        hlg.forEach(item -> {
            System.out.println(item.getDpName() + " changed to " +
                             item.getVariable() + " at " + item.getTime());
        });
    })
    .connect();

// Later: stop receiving updates
connection.disconnect();
```

**Notes:**
- Callback runs in separate thread
- Default config is `_online.._value` (timestamped current value)
- Subscription remains active until `disconnect()` called
- Multiple groups can have different callbacks
- Thread-safe

---

### `JAlertConnect alertConnect()`

**Purpose:** Creates a fluent builder for subscribing to alert (alarm) changes.

**Parameters:** None

**Returns:** JAlertConnect builder instance

**Builder Methods:** Same as JDpConnect

**Usage Pattern:**
```java
oa.alertConnect()
    .add("ExampleDP_Arg1.:_alert_hdl.._act_state")
    .add("ExampleDP_Arg1.:_alert_hdl.._ack_state")
    .action(hlg -> {
        hlg.forEach(item -> {
            System.out.println("Alert state changed: " + item.getDpName());
        });
    })
    .connect();
```

**Notes:**
- Specialized for alert/alarm attributes
- Common configs: `_act_state`, `_ack_state`, `_came_range`
- Alert attributes are accessed via `:_alert_hdl..` notation

---

## Datapoint Query Operations

### `JDpQuery dpQuery(String query)`

**Purpose:** Executes a one-time SQL-like query on datapoints.

**Parameters:**
- `query` (String): WinCC OA query string in SQL-like syntax

**Returns:** JDpQuery instance

**Query Methods:**
- `send()`: Execute asynchronously
- `await()`: Execute and wait for results
- `action(IAnswer callback)`: Set result handler

**Query Syntax:**
```sql
SELECT 'config1', 'config2', ... FROM 'pattern' [WHERE condition]
```

**Usage Pattern:**
```java
// Get all values matching pattern
JDpMsgAnswer answer = oa.dpQuery(
    "SELECT '_online.._value' FROM 'ExampleDP_*'"
).await();

for (int i = 0; i < answer.size(); i++) {
    JDpVCItem item = answer.getItem(i);
    System.out.println(item.getDpName() + " = " + item.getVariable());
}

// Query with condition
oa.dpQuery(
    "SELECT '_online.._value' FROM 'Sensor_*.temperature' " +
    "WHERE '_online.._value' > 25.0"
).await();
```

**Notes:**
- One-time query (not a subscription)
- Powerful for bulk operations on pattern-matched datapoints
- Supports WHERE clauses for filtering
- Results can be large - consider patterns carefully

---

### `JDpQueryConnect dpQueryConnectSingle(String query)`

**Purpose:** Creates a subscription that triggers callback once when first matching datapoint changes.

**Parameters:**
- `query` (String): WinCC OA query string

**Returns:** JDpQueryConnect instance

**Query Methods:**
- `action(IAnswer callback)`: Set change handler
- `connect()`: Activate subscription
- `disconnect()`: Deactivate subscription

**Usage Pattern:**
```java
// Trigger on first change
oa.dpQueryConnectSingle(
    "SELECT '_online.._value' FROM 'ExampleDP_*'"
).action(answer -> {
    System.out.println("First datapoint changed!");
    // Only called for the first matching DP that changes
}).connect();
```

**Notes:**
- Callback triggered only for first DP matching query that changes
- Other matching DPs that change do not trigger callback
- Use case: detect when ANY of a set of DPs changes

---

### `JDpQueryConnect dpQueryConnectAll(String query)`

**Purpose:** Creates a subscription that triggers callback for ALL matching datapoint changes.

**Parameters:**
- `query` (String): WinCC OA query string

**Returns:** JDpQueryConnect instance

**Usage Pattern:**
```java
// Trigger on all changes
var connection = oa.dpQueryConnectAll(
    "SELECT '_online.._value' FROM 'Sensor_*.temperature'"
).action(answer -> {
    // Called whenever ANY Sensor_*.temperature changes
    for (int i = 0; i < answer.size(); i++) {
        System.out.println("Changed: " + answer.getItem(i).getDpName());
    }
}).connect();
```

**Notes:**
- Callback triggered for every matching DP that changes
- More comprehensive than dpQueryConnectSingle
- Pattern-based subscription - automatically includes new DPs matching pattern

---

## Historical Data Operations

### `JDpGetPeriod dpGetPeriod(TimeVar start, TimeVar stop, int maxCount)`

**Purpose:** Queries historical (archived) datapoint values for a time period.

**Parameters:**
- `start` (TimeVar): Start timestamp for query range
- `stop` (TimeVar): End timestamp for query range
- `maxCount` (int): Maximum number of values to return (0 = unlimited)

**Returns:** JDpGetPeriod builder instance

**Builder Methods:**
- `add(String dp)`: Add datapoint to query (must use `_offline` config)
- `add(DpIdentifierVar dpid)`: Add with specific identifier
- `action(IAnswer callback)`: Set result handler
- `send()`: Execute asynchronously
- `await()`: Execute and wait for results

**Usage Pattern:**
```java
TimeVar start = new TimeVar(new Date(System.currentTimeMillis() - 3600000)); // 1h ago
TimeVar stop = new TimeVar(new Date());

JDpMsgAnswer answer = oa.dpGetPeriod(start, stop, 100)
    .add("ExampleDP_Trend1.:_offline.._value")
    .await();

for (int i = 0; i < answer.size(); i++) {
    JDpVCItem item = answer.getItem(i);
    System.out.println(item.getTime() + ": " + item.getVariable());
}
```

**Prerequisites:**
- Datapoint must have archiving enabled
- Use `:_offline.._value` config (not `_online`)

**Notes:**
- Returns values in chronological order
- maxCount limits result size (0 for all values)
- Query can span any time range (past, present, or future)

---

### `JDpGetPeriod dpGetPeriod(Date start, Date stop, int maxCount)`

**Purpose:** Queries historical data using Java Date objects.

**Parameters:**
- `start` (Date): Start time
- `stop` (Date): End time
- `maxCount` (int): Maximum values (0 = unlimited)

**Returns:** JDpGetPeriod builder instance

**Usage Pattern:**
```java
Date start = new Date(System.currentTimeMillis() - 86400000); // 24h ago
Date stop = new Date();

oa.dpGetPeriod(start, stop, 1000)
    .add("Sensor_1.temperature:_offline.._value")
    .await();
```

---

### `JDpGetPeriod dpGetPeriod(long start, long stop, int maxCount)`

**Purpose:** Queries historical data using millisecond timestamps.

**Parameters:**
- `start` (long): Start time in milliseconds since epoch
- `stop` (long): End time in milliseconds since epoch
- `maxCount` (int): Maximum values (0 = unlimited)

**Returns:** JDpGetPeriod builder instance

**Usage Pattern:**
```java
long now = System.currentTimeMillis();
long yesterday = now - 86400000;

oa.dpGetPeriod(yesterday, now, 500)
    .add("Sensor_1.temperature:_offline.._value")
    .await();
```

---

## Datapoint Metadata Operations

### `String[] dpNames(String pattern)`

**Purpose:** Finds all datapoint names matching a wildcard pattern.

**Parameters:**
- `pattern` (String): Wildcard pattern (* = any characters, ? = single character)

**Returns:** String[] array of matching datapoint names (empty array if none found)

**Usage Pattern:**
```java
String[] dps = oa.dpNames("ExampleDP_*");
for (String dp : dps) {
    System.out.println("Found: " + dp);
}
```

**Pattern Examples:**
- `"ExampleDP_*"`: All DPs starting with ExampleDP_
- `"Sensor_?.temperature"`: Sensor_1.temperature, Sensor_A.temperature, etc.
- `"*"`: All datapoints in system

**Notes:**
- Case-sensitive matching
- Returns root DP names without element suffixes
- Efficient for discovery and validation

---

### `String[] dpNames(String pattern, String type)`

**Purpose:** Finds datapoint names matching pattern AND datapoint type.

**Parameters:**
- `pattern` (String): Wildcard pattern for DP names
- `type` (String): Datapoint type name (e.g., "ExampleDP_Float")

**Returns:** String[] of matching DPs with specified type

**Usage Pattern:**
```java
String[] floatDPs = oa.dpNames("*", "ExampleDP_Float");
String[] sensors = oa.dpNames("Sensor_*", "SensorType");
```

**Notes:**
- Both conditions must match
- Use "*" pattern to find all DPs of a specific type
- Type name must match exactly (case-sensitive)

---

### `boolean dpExists(String dpName)`

**Purpose:** Checks if a datapoint exists in the system.

**Parameters:**
- `dpName` (String): Datapoint name to check

**Returns:**
- `true`: Datapoint exists
- `false`: Datapoint does not exist

**Usage Pattern:**
```java
if (oa.dpExists("MyDatapoint")) {
    System.out.println("Datapoint exists");
} else {
    System.out.println("Datapoint not found");
}
```

**Notes:**
- Fast existence check
- Useful before create/delete operations
- Case-sensitive

---

### `LangTextVar dpGetComment(DpIdentifierVar dpid)`

**Purpose:** Retrieves multi-language comments for a datapoint.

**Parameters:**
- `dpid` (DpIdentifierVar): Datapoint identifier

**Returns:** LangTextVar containing comments in multiple languages (or null if not found)

**Usage Pattern:**
```java
DpIdentifierVar dpid = new DpIdentifierVar("MyDP");
LangTextVar comments = oa.dpGetComment(dpid);
if (comments != null) {
    String germanComment = comments.getText(1); // Language index 1
    String englishComment = comments.getText(0); // Language index 0
}
```

**Notes:**
- Comments are defined in datapoint type
- Language indices depend on project configuration

---

## Datapoint Type Management

### `int dpTypeCreate(DpTypeElement definition)`

**Purpose:** Creates a new datapoint type in the system.

**Parameters:**
- `definition` (DpTypeElement): Root element of type definition tree structure

**Returns:**
- `0`: Success
- Non-zero: Error code

**Type Structure:**
```java
DpTypeElement root = new DpTypeElement("MyType", 1, DpElementType.RECORD);
root.addChild(new DpTypeElement("value", 2, DpElementType.FLOAT));
root.addChild(new DpTypeElement("status", 3, DpElementType.INT));

// Nested structure
DpTypeElement config = new DpTypeElement("config", 4, DpElementType.RECORD);
config.addChild(new DpTypeElement("enabled", 5, DpElementType.BIT));
root.addChild(config);

int result = oa.dpTypeCreate(root);
```

**Element Types (DpElementType):**
- `RECORD`: Structure/container
- `FLOAT`, `INT`, `UINT`, `LONG`: Numeric types
- `TEXT`, `CHAR`: String types
- `BIT`, `BIT32`, `BIT64`: Boolean/bitfield types
- `TIME`: Timestamp type

**Notes:**
- Type name must be unique
- Element IDs must be unique within type (typically sequential)
- Wait 1-2 seconds after creation before using type
- Types can have nested structures (RECORD elements)

---

### `int dpTypeChange(int typeId, DpTypeElement definition, boolean append)`

**Purpose:** Modifies an existing datapoint type.

**Parameters:**
- `typeId` (int): Type identifier (get via dpTypeNameToId)
- `definition` (DpTypeElement): Element to add or modify
- `append` (boolean):
  - `true`: Append element to root
  - `false`: Replace type definition

**Returns:**
- `0`: Success
- Non-zero: Error code

**Usage Pattern:**
```java
int typeId = oa.dpTypeNameToId("MyType");
DpTypeElement newElement = new DpTypeElement("description", 10, DpElementType.TEXT);
oa.dpTypeChange(typeId, newElement, true); // Append
```

**Notes:**
- Changing types affects all datapoints of that type
- Cannot remove elements that are in use
- Use with caution in production systems

---

### `int dpTypeDelete(String typeName)`

**Purpose:** Deletes a datapoint type by name.

**Parameters:**
- `typeName` (String): Name of type to delete

**Returns:**
- `0`: Success
- Non-zero: Error (e.g., type in use, doesn't exist)

**Usage Pattern:**
```java
int result = oa.dpTypeDelete("MyType");
if (result != 0) {
    System.err.println("Cannot delete type (may be in use)");
}
```

**Prerequisites:**
- No datapoints of this type must exist
- Type must not be referenced by other types

---

### `int dpTypeDelete(int typeId)`

**Purpose:** Deletes a datapoint type by ID.

**Parameters:**
- `typeId` (int): Type identifier

**Returns:**
- `0`: Success
- Non-zero: Error code

---

### `int dpTypeNameToId(String typeName)`

**Purpose:** Converts a type name to its internal type ID.

**Parameters:**
- `typeName` (String): Type name

**Returns:**
- Positive integer: Type ID
- Negative value: Type not found

**Usage Pattern:**
```java
int typeId = oa.dpTypeNameToId("ExampleDP_Float");
if (typeId > 0) {
    System.out.println("Type ID: " + typeId);
}
```

---

### `DpTypeElement dpTypeGet(String typeName)`

**Purpose:** Retrieves the complete type definition as a tree structure.

**Parameters:**
- `typeName` (String): Type name

**Returns:**
- DpTypeElement: Root element of type tree
- `null`: Type not found

**Usage Pattern:**
```java
DpTypeElement typeDef = oa.dpTypeGet("MyType");
if (typeDef != null) {
    System.out.println("Type structure:");
    System.out.println(typeDef.toString()); // Prints tree structure

    // Navigate structure
    for (DpTypeElement child : typeDef.getChildren()) {
        System.out.println("  " + child.getName() + ": " + child.getType());
    }
}
```

**Notes:**
- Returns full hierarchical structure
- Use for introspection and validation
- toString() provides formatted tree view

---

## Datapoint Management

### `int dpCreate(String dpName, String typeName)`

**Purpose:** Creates a new datapoint instance of specified type.

**Parameters:**
- `dpName` (String): Name for the new datapoint (must be unique)
- `typeName` (String): Datapoint type (must exist)

**Returns:**
- `0`: Success
- Non-zero: Error (e.g., name exists, type not found)

**Usage Pattern:**
```java
int result = oa.dpCreate("Sensor_5", "SensorType");
if (result == 0) {
    System.out.println("Datapoint created successfully");
    Thread.sleep(1000); // Wait for system to propagate
}
```

**Notes:**
- Wait 500-1000ms after creation before accessing
- Datapoint name must not exist
- Type must exist before creating datapoints
- Initial values are defaults for the type

---

### `int dpDelete(String dpName)`

**Purpose:** Deletes a datapoint from the system.

**Parameters:**
- `dpName` (String): Datapoint name to delete

**Returns:**
- `0`: Success
- Non-zero: Error (e.g., doesn't exist, in use)

**Usage Pattern:**
```java
int result = oa.dpDelete("Sensor_5");
if (result == 0) {
    System.out.println("Datapoint deleted");
}
```

**Side Effects:**
- All configs, archives, and references are removed
- Cannot be undone
- Active subscriptions to this DP will receive disconnect notifications

**Notes:**
- Use with caution - data loss is permanent
- Check dpExists() before deleting if unsure

---

## CNS (Common Name Service) Operations

CNS provides hierarchical organization of datapoints in tree structures with multi-language display names. It's used for creating navigation hierarchies in WinCC OA user interfaces.

### View Management

#### `int cnsCreateView(String system, String viewId, String separator, LangTextVar displayNames)`

**Purpose:** Creates a new CNS view (top-level container for trees).

**Parameters:**
- `system` (String): System name (use `null` for default/local system)
- `viewId` (String): Unique identifier for the view
- `separator` (String): Path separator character (typically "/")
- `displayNames` (LangTextVar): Multi-language display names

**Returns:**
- `0`: Success
- Non-zero: Error (e.g., view already exists)

**Usage Pattern:**
```java
LangTextVar names = new LangTextVar();
names.setText(0, "My Plant View"); // English
names.setText(1, "Meine Anlagenansicht"); // German

oa.cnsCreateView(null, "plantView", "/", names);
```

**Notes:**
- Views are top-level containers
- Separator defines path notation (usually "/")
- Display names support multiple languages

---

#### `int cnsDeleteView(String system, String viewId)`

**Purpose:** Deletes a CNS view and all its trees.

**Parameters:**
- `system` (String): System name (null for default)
- `viewId` (String): View identifier

**Returns:**
- `0`: Success
- Non-zero: Error

**Warning:** Deletes all trees and nodes within the view

---

#### `String[] cnsGetViews(String system)`

**Purpose:** Lists all views in a system.

**Parameters:**
- `system` (String): System name (null for default)

**Returns:** String[] of view identifiers

**Usage Pattern:**
```java
String[] views = oa.cnsGetViews(null);
for (String view : views) {
    System.out.println("View: " + view);
}
```

---

#### `LangTextVar cnsGetViewDisplayNames(String system, String viewId)`

**Purpose:** Retrieves multi-language display names for a view.

**Parameters:**
- `system` (String): System name (null for default)
- `viewId` (String): View identifier

**Returns:** LangTextVar with display names

---

#### `int cnsChangeViewDisplayNames(String system, String viewId, LangTextVar displayNames)`

**Purpose:** Updates view display names.

**Parameters:**
- `system` (String): System name (null for default)
- `viewId` (String): View identifier
- `displayNames` (LangTextVar): New display names

**Returns:**
- `0`: Success
- Non-zero: Error

---

#### `String cnsGetViewSeparators(String system, String viewId)`

**Purpose:** Gets the path separator for a view.

**Parameters:**
- `system` (String): System name (null for default)
- `viewId` (String): View identifier

**Returns:** Separator string (e.g., "/")

---

#### `int cnsChangeViewSeparators(String system, String viewId, String separator)`

**Purpose:** Changes the path separator for a view.

**Parameters:**
- `system` (String): System name (null for default)
- `viewId` (String): View identifier
- `separator` (String): New separator

**Returns:**
- `0`: Success
- Non-zero: Error

---

### Tree Management

#### `int cnsAddTree(String system, String viewId, String nodeId, int nodeType, DpIdentifierVar dpId, LangTextVar displayNames)`

**Purpose:** Adds a new tree (root node) to a view.

**Parameters:**
- `system` (String): System name (null for default)
- `viewId` (String): View identifier
- `nodeId` (String): Unique ID for tree root
- `nodeType` (int): Node type from `CnsDataIdentifier.Types`
  - `NO_TYPE` (0): Structure node (no datapoint)
  - `DATAPOINT` (1): Links to datapoint
  - Other types for specific purposes
- `dpId` (DpIdentifierVar): Datapoint identifier (null for structure nodes)
- `displayNames` (LangTextVar): Multi-language display names

**Returns:**
- `0`: Success
- Non-zero: Error

**Usage Pattern:**
```java
LangTextVar treeNames = new LangTextVar();
treeNames.setText(0, "Building A");

oa.cnsAddTree(null, "plantView", "buildingA",
              CnsDataIdentifier.Types.NO_TYPE, null, treeNames);
```

---

#### `int cnsDeleteTree(String cnsPath)`

**Purpose:** Deletes a tree or subtree.

**Parameters:**
- `cnsPath` (String): Full CNS path (e.g., "System1:viewId/treeId")

**Returns:**
- `0`: Success
- Non-zero: Error

**Warning:** Recursively deletes all child nodes

---

#### `String[] cnsGetTrees(String system, String viewId)`

**Purpose:** Lists all tree roots in a view.

**Parameters:**
- `system` (String): System name (null for default)
- `viewId` (String): View identifier

**Returns:** String[] of tree root paths

---

#### `String cnsGetRoot(String cnsPath)`

**Purpose:** Gets the root path of the tree containing the given path.

**Parameters:**
- `cnsPath` (String): Any CNS path within a tree

**Returns:** Root path of the tree

**Usage Pattern:**
```java
String root = oa.cnsGetRoot("System1:plantView/buildingA/floor1/room5");
// Returns "System1:plantView/buildingA"
```

---

### Node Management

#### `int cnsAddNode(String parentPath, String nodeId, int nodeType, DpIdentifierVar dpId, LangTextVar displayNames)`

**Purpose:** Adds a child node to a parent node.

**Parameters:**
- `parentPath` (String): Parent CNS path
- `nodeId` (String): Unique ID for new node (within parent)
- `nodeType` (int): Node type (see CnsDataIdentifier.Types)
- `dpId` (DpIdentifierVar): Datapoint link (null for structure nodes)
- `displayNames` (LangTextVar): Multi-language names

**Returns:**
- `0`: Success
- Non-zero: Error

**Usage Pattern:**
```java
// Add structure node
LangTextVar names = new LangTextVar();
names.setText(0, "Floor 1");
oa.cnsAddNode("System1:plantView/buildingA", "floor1",
              CnsDataIdentifier.Types.NO_TYPE, null, names);

// Add datapoint node
DpIdentifierVar dpId = new DpIdentifierVar("Sensor_1.temperature");
LangTextVar dpNames = new LangTextVar();
dpNames.setText(0, "Temperature");
oa.cnsAddNode("System1:plantView/buildingA/floor1", "temp1",
              CnsDataIdentifier.Types.DATAPOINT, dpId, dpNames);
```

---

#### `CnsNode cnsGetNode(String cnsPath)`

**Purpose:** Retrieves complete information about a CNS node.

**Parameters:**
- `cnsPath` (String): Full CNS path

**Returns:**
- CnsNode object with all node properties
- `null`: Node not found

**CnsNode Properties:**
- `getPath()`: Full path
- `getNodeId()`: Node identifier
- `getDisplayNames()`: Multi-language names
- `getDataIdentifier()`: Linked datapoint info
- `getType()`: Node type

**Usage Pattern:**
```java
CnsNode node = oa.cnsGetNode("System1:plantView/buildingA/floor1");
if (node != null) {
    System.out.println("Path: " + node.getPath());
    System.out.println("Display: " + node.getDisplayNames().getText(0));
}
```

---

#### `CnsDataIdentifier cnsGetId(String cnsPath)`

**Purpose:** Gets the datapoint identifier linked to a node.

**Parameters:**
- `cnsPath` (String): CNS path

**Returns:**
- CnsDataIdentifier: Datapoint information
- `null`: No datapoint linked or node not found

**Usage Pattern:**
```java
CnsDataIdentifier id = oa.cnsGetId("System1:plantView/buildingA/floor1/temp1");
if (id != null) {
    System.out.println("Type: " + id.getType());
    // Can get linked datapoint name
}
```

---

#### `String[] cnsGetChildren(String cnsPath)`

**Purpose:** Lists all direct child nodes of a parent.

**Parameters:**
- `cnsPath` (String): Parent CNS path

**Returns:** String[] of child paths (empty if no children)

**Usage Pattern:**
```java
String[] children = oa.cnsGetChildren("System1:plantView/buildingA");
for (String child : children) {
    System.out.println("Child: " + child);
}
```

---

#### `String cnsGetParent(String cnsPath)`

**Purpose:** Gets the parent path of a node.

**Parameters:**
- `cnsPath` (String): CNS path

**Returns:**
- Parent path string
- `null`: Node is root or not found

**Usage Pattern:**
```java
String parent = oa.cnsGetParent("System1:plantView/buildingA/floor1");
// Returns "System1:plantView/buildingA"
```

---

#### `int cnsChangeNodeData(String cnsPath, DpIdentifierVar dpId, int nodeType)`

**Purpose:** Changes the datapoint link and type of a node.

**Parameters:**
- `cnsPath` (String): CNS path
- `dpId` (DpIdentifierVar): New datapoint identifier
- `nodeType` (int): New node type

**Returns:**
- `0`: Success
- Non-zero: Error

---

#### `int cnsChangeNodeDisplayNames(String cnsPath, LangTextVar displayNames)`

**Purpose:** Updates node display names.

**Parameters:**
- `cnsPath` (String): CNS path
- `displayNames` (LangTextVar): New display names

**Returns:**
- `0`: Success
- Non-zero: Error

---

### Search Operations

#### `String[] cnsGetNodesByName(String system, String viewId, String pattern, int searchMode, int langIdx)`

**Purpose:** Searches for nodes by name pattern.

**Parameters:**
- `system` (String): System name (null for default)
- `viewId` (String): View identifier
- `pattern` (String): Search pattern with wildcards
- `searchMode` (int): Search mode
  - `0`: Search node IDs only
  - `1`: Search display names only
  - `2`: Search both IDs and display names
- `langIdx` (int): Language index for display name search (0=first language)

**Returns:** String[] of matching CNS paths

**Usage Pattern:**
```java
// Find all nodes with "temp" in ID
String[] matches = oa.cnsGetNodesByName(null, "plantView", "*temp*", 0, 0);

// Find all nodes with "Temperature" in English display name
String[] matches2 = oa.cnsGetNodesByName(null, "plantView", "*Temperature*", 1, 0);
```

---

#### `String[] cnsGetNodesByData(String system, String viewId, DpIdentifierVar dpId)`

**Purpose:** Finds all nodes linked to a specific datapoint.

**Parameters:**
- `system` (String): System name (null for default)
- `viewId` (String): View identifier
- `dpId` (DpIdentifierVar): Datapoint identifier to search for

**Returns:** String[] of CNS paths linked to this datapoint

**Usage Pattern:**
```java
DpIdentifierVar dpId = new DpIdentifierVar("Sensor_1.temperature");
String[] nodes = oa.cnsGetNodesByData(null, "plantView", dpId);
for (String node : nodes) {
    System.out.println("Found at: " + node);
}
```

**Notes:**
- One datapoint can appear in multiple CNS locations
- Useful for reverse lookups

---

#### `DpIdentifierVar[] cnsGetIdSet(String system, String viewId, String pattern, int searchMode, int langIdx)`

**Purpose:** Gets all unique datapoint identifiers from nodes matching search criteria.

**Parameters:**
- `system` (String): System name (null for default)
- `viewId` (String): View identifier
- `pattern` (String): Search pattern
- `searchMode` (int): 0=ID, 1=display name, 2=both
- `langIdx` (int): Language index

**Returns:** DpIdentifierVar[] array of unique datapoint identifiers

**Usage Pattern:**
```java
// Get all datapoints under "sensors" nodes
DpIdentifierVar[] dpIds = oa.cnsGetIdSet(null, "plantView", "*sensor*", 0, 0);
for (DpIdentifierVar dpId : dpIds) {
    System.out.println("Datapoint: " + dpId.getName());
}
```

---

### Utility Functions

#### `String cnsSubStr(String cnsPath, int mask, boolean resolve)`

**Purpose:** Extracts specific parts of a CNS path.

**Parameters:**
- `cnsPath` (String): Full CNS path
- `mask` (int): Bitmask specifying which parts to extract
- `resolve` (boolean): If true, resolve node IDs to display names

**Returns:** Extracted path components as string

**Notes:**
- Mask values are defined in WinCC OA CNS constants
- Used for parsing and formatting CNS paths

---

#### `LangTextVar cnsGetSystemNames(String system)`

**Purpose:** Gets multi-language display names for a system.

**Parameters:**
- `system` (String): System name (null for default)

**Returns:** LangTextVar with system display names

---

#### `int cnsSetSystemNames(String system, LangTextVar displayNames)`

**Purpose:** Sets multi-language display names for a system.

**Parameters:**
- `system` (String): System name (null for default)
- `displayNames` (LangTextVar): New display names

**Returns:**
- `0`: Success
- Non-zero: Error

---

#### `boolean cnsCheckId(String id)`

**Purpose:** Validates a node ID.

**Parameters:**
- `id` (String): Node ID to validate

**Returns:**
- `true`: Valid node ID
- `false`: Invalid (contains illegal characters)

**Notes:**
- Check before cnsAddNode to ensure ID is valid
- IDs cannot contain certain special characters (e.g., path separators)

---

#### `boolean cnsCheckName(String displayName)`

**Purpose:** Validates a display name.

**Parameters:**
- `displayName` (String): Display name to validate

**Returns:**
- `true`: Valid
- `false`: Invalid

---

#### `boolean cnsCheckSeparator(char separator)`

**Purpose:** Validates a path separator character.

**Parameters:**
- `separator` (char): Separator to validate

**Returns:**
- `true`: Valid separator
- `false`: Invalid (e.g., alphanumeric characters not allowed)

**Usage Pattern:**
```java
if (oa.cnsCheckSeparator('/')) {
    // Safe to use as separator
}
```

---

### Observer Pattern

#### `int cnsAddObserver(CnsObserver observer)`

**Purpose:** Registers a callback for CNS structure changes.

**Parameters:**
- `observer` (CnsObserver): Observer implementation

**Returns:** Observer ID (use for removal)

**Observer Interface:**
```java
public interface CnsObserver {
    void onCnsChange(String path, int changeType);
}
```

**Change Types:**
- Node added
- Node deleted
- Node modified
- Tree added/deleted

**Usage Pattern:**
```java
CnsObserver observer = new CnsObserver() {
    @Override
    public void onCnsChange(String path, int changeType) {
        System.out.println("CNS changed: " + path + " type=" + changeType);
    }
};

int observerId = oa.cnsAddObserver(observer);
```

**Notes:**
- Callback runs asynchronously
- Useful for maintaining cached CNS structures
- Don't perform heavy operations in callback

---

#### `int cnsRemoveObserver(int observerId)`

**Purpose:** Unregisters a CNS observer.

**Parameters:**
- `observerId` (int): ID returned from cnsAddObserver

**Returns:**
- `0`: Success
- Non-zero: Error (e.g., invalid ID)

**Usage Pattern:**
```java
oa.cnsRemoveObserver(observerId);
```

---

## User & Security Operations

### `int checkPassword(String username, String password)`

**Purpose:** Verifies user credentials against WinCC OA user database.

**Parameters:**
- `username` (String): Username to check
- `password` (String): Password to verify

**Returns:**
- `0`: Valid credentials
- `-1`: Invalid username (user doesn't exist)
- `-2`: Wrong password (user exists but password incorrect)

**Usage Pattern:**
```java
int result = oa.checkPassword("admin", "secretpassword");
if (result == 0) {
    System.out.println("Valid credentials");
} else if (result == -1) {
    System.out.println("User not found");
} else if (result == -2) {
    System.out.println("Wrong password");
}
```

**Notes:**
- Uses WinCC OA user management system
- Does not change current user context (just verifies)
- For changing context, use setUserId

---

### `boolean setUserId(String username, String password)`

**Purpose:** Sets the current user context for subsequent operations.

**Parameters:**
- `username` (String): Username
- `password` (String): Password

**Returns:**
- `true`: Successfully changed user context
- `false`: Authentication failed

**Usage Pattern:**
```java
if (oa.setUserId("admin", "password")) {
    System.out.println("Running as admin");
    // Subsequent operations run with admin privileges
} else {
    System.err.println("Authentication failed");
}
```

**Side Effects:**
- Changes permission context for all subsequent operations
- Affects access to restricted datapoints and configurations
- Logged in WinCC OA audit trail

**Notes:**
- Required for operations needing elevated privileges
- User context persists until disconnect or another setUserId call
- Always validate credentials before setting user ID

---

## Logging Operations (Static Methods)

All logging methods are static and can be called before or after connection.

### `static void log(ErrPrio prio, ErrCode code, String text)`

**Purpose:** Logs a message to WinCC OA logging system with specific priority and error code.

**Parameters:**
- `prio` (ErrPrio): Priority level
  - `PRIO_SEVERE`: Critical errors
  - `PRIO_WARNING`: Warnings
  - `PRIO_INFO`: Informational messages
  - `PRIO_DEBUG`: Debug messages
- `code` (ErrCode): Error code classification
  - `NOERR`: No error (informational)
  - `UNEXPECTEDSTATE`: Unexpected state
  - Many others defined in ErrCode enum
- `text` (String): Message text

**Usage Pattern:**
```java
WinCCOA.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "Application started");
WinCCOA.log(ErrPrio.PRIO_WARNING, ErrCode.UNEXPECTEDSTATE, "Unusual condition");
WinCCOA.log(ErrPrio.PRIO_SEVERE, ErrCode.SYS, "Critical failure");
```

**Notes:**
- Messages appear in manager log file (e.g., `WCCILjava1.0.log`)
- Visible in WinCC OA log viewer
- Use appropriate priority for filtering and alerting

---

### `static void log(String message)`

**Purpose:** Logs an informational message (convenience method).

**Parameters:**
- `message` (String): Message text

**Usage Pattern:**
```java
WinCCOA.log("Processing started");
WinCCOA.log("Received 100 datapoints");
```

**Notes:**
- Equivalent to `log(ErrPrio.PRIO_INFO, ErrCode.NOERR, message)`
- Use for general informational logging

---

### `static void logError(String message)`

**Purpose:** Logs an error message (convenience method).

**Parameters:**
- `message` (String): Error message text

**Usage Pattern:**
```java
WinCCOA.logError("Failed to connect to database");
WinCCOA.logError("Invalid configuration");
```

**Notes:**
- Equivalent to `log(ErrPrio.PRIO_SEVERE, ErrCode.UNEXPECTEDSTATE, message)`
- Use for error conditions

---

### `static void logStackTrace(ErrPrio prio, ErrCode code, Throwable exception)`

**Purpose:** Logs an exception with full stack trace.

**Parameters:**
- `prio` (ErrPrio): Priority level
- `code` (ErrCode): Error code
- `exception` (Throwable): Exception to log

**Usage Pattern:**
```java
try {
    // ... code that may throw
} catch (Exception e) {
    WinCCOA.logStackTrace(ErrPrio.PRIO_SEVERE, ErrCode.UNEXPECTEDSTATE, e);
}
```

**Notes:**
- Stack trace is formatted and written to log
- Includes exception message and full call stack
- Essential for debugging production issues

---

### `static void logStackTrace(Throwable exception)`

**Purpose:** Logs an exception with SEVERE priority (convenience method).

**Parameters:**
- `exception` (Throwable): Exception to log

**Usage Pattern:**
```java
try {
    oa.dpSet("NonExistent.", 42).await();
} catch (Exception e) {
    WinCCOA.logStackTrace(e);
}
```

**Notes:**
- Equivalent to `logStackTrace(ErrPrio.PRIO_SEVERE, ErrCode.UNEXPECTEDSTATE, exception)`

---

## Redundancy State Management

### `void onRedundancyStateChanged(Consumer<Boolean> callback)`

**Purpose:** Registers a callback to be notified when redundancy state changes.

**Parameters:**
- `callback` (Consumer<Boolean>): Lambda or method reference receiving state
  - Receives `true` when manager becomes **active**
  - Receives `false` when manager becomes **passive**

**Usage Pattern:**
```java
oa.onRedundancyStateChanged(isActive -> {
    if (isActive) {
        System.out.println("Now ACTIVE - taking over primary role");
        // Start processing, enable outputs, etc.
    } else {
        System.out.println("Now PASSIVE - entering standby mode");
        // Stop processing, disable outputs, etc.
    }
});
```

**Use Cases:**
- Start/stop data collection based on active state
- Enable/disable external communication
- Implement hot-standby patterns
- Control hardware outputs (only active system controls devices)

**Notes:**
- Only relevant in redundant WinCC OA configurations
- Callback invoked on state transitions
- May be called during connection if already in redundant mode
- Thread-safe callback execution

---

### `void removeRedundancyStateListener(Consumer<Boolean> callback)`

**Purpose:** Unregisters a redundancy state callback.

**Parameters:**
- `callback` (Consumer<Boolean>): The exact callback reference to remove

**Usage Pattern:**
```java
Consumer<Boolean> callback = isActive -> {
    WinCCOA.log(isActive ? "ACTIVE" : "PASSIVE");
};

oa.onRedundancyStateChanged(callback);

// Later...
oa.removeRedundancyStateListener(callback);
```

**Notes:**
- Must pass same callback instance used in registration
- No effect if callback not registered

# WinCCOA Unified API

The `WinCCOA` class provides a simplified, unified API for interacting with WinCC OA. It hides the internal complexity of `JManager` and `JClient`, providing a single entry point for all operations.

## Quick Start

```java
import at.rocworks.oa4j.base.WinCCOA;
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

```java
// Using timestamps (milliseconds)
long now = System.currentTimeMillis();
long oneHourAgo = now - 3600000;

oa.dpGetPeriod(oneHourAgo, now, 100)
    .add("ExampleDP_Trend1.:_offline.._value")
    .action(answer -> {
        // Process historical values
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
import at.rocworks.oa4j.base.WinCCOA;
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

## Method Reference

### Connection & Lifecycle
- `static connect(String[] args)` - Connect using command-line args
- `static connect(String project)` - Connect with project name
- `static connect(String project, int manNum)` - Connect with project and manager number
- `static getInstance()` - Get existing instance
- `disconnect()` - Disconnect

### Status & Info
- `isConnected()`, `isActive()`
- `getProjectPath()`, `getConfigDir()`, `getLogDir()`
- `getManagerName()`, `getManagerNumber()`
- `getConfigValue(key)`, `getConfigValue(key, default)`

### Datapoint Operations
- `dpGet()`, `dpGet(dp)`, `dpGet(List<String>)`
- `dpSet()`, `dpSet(dp, value)`, `dpSetWait(dp, value)`
- `dpConnect()`, `alertConnect()`
- `dpQuery(query)`, `dpQueryConnectSingle(query)`, `dpQueryConnectAll(query)`
- `dpGetPeriod(start, stop, maxCount)`

### Metadata
- `dpNames(pattern)`, `dpNames(pattern, type)`
- `dpExists(dpName)`, `dpGetComment(dpid)`

### Type Management
- `dpTypeCreate(definition)`, `dpTypeChange(typeId, definition, append)`
- `dpTypeDelete(typeName)`, `dpTypeDelete(typeId)`
- `dpTypeNameToId(typeName)`, `dpTypeGet(typeName)`

### Datapoint Management
- `dpCreate(dpName, typeName)`, `dpDelete(dpName)`

### CNS (29 methods)
- View: `cnsCreateView`, `cnsDeleteView`, `cnsGetViews`, `cnsGetViewDisplayNames`, `cnsChangeViewDisplayNames`, `cnsGetViewSeparators`, `cnsChangeViewSeparators`
- Tree: `cnsAddTree`, `cnsDeleteTree`, `cnsGetTrees`, `cnsGetRoot`
- Node: `cnsAddNode`, `cnsGetNode`, `cnsGetId`, `cnsGetChildren`, `cnsGetParent`, `cnsChangeNodeData`, `cnsChangeNodeDisplayNames`
- Search: `cnsGetNodesByName`, `cnsGetNodesByData`, `cnsGetIdSet`
- Utils: `cnsSubStr`, `cnsGetSystemNames`, `cnsSetSystemNames`, `cnsCheckId`, `cnsCheckName`, `cnsCheckSeparator`
- Observer: `cnsAddObserver`, `cnsRemoveObserver`

### User & Security
- `checkPassword(user, password)`, `setUserId(user, password)`

### Logging (static)
- `log(ErrPrio, ErrCode, String)`, `log(String)`, `logError(String)`
- `logStackTrace(ErrPrio, ErrCode, Throwable)`, `logStackTrace(Throwable)`

### Redundancy State
- `onRedundancyStateChanged(Consumer<Boolean>)`, `removeRedundancyStateListener(Consumer<Boolean>)`

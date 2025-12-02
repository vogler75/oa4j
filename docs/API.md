# OA4J Java API Reference

This document describes the two main classes for interacting with WinCC OA from Java: `JManager` and `JClient`.

## Overview

- **JManager** - Manages the connection lifecycle to WinCC OA. Handles initialization, startup, shutdown, and the main event dispatch loop.
- **JClient** - Provides a fluent API for datapoint operations: reading, writing, subscribing to changes, and querying.

## Quick Start

```java
public static void main(String[] args) throws Exception {
    // Initialize and start the manager
    JManager m = new JManager();
    m.init(args).start();

    // Read a datapoint
    Variable value = JClient.dpGet("ExampleDP_Arg1.");
    System.out.println("Value: " + value);

    // Write a datapoint
    JClient.dpSet("ExampleDP_Arg1.", 42);

    // Subscribe to changes
    JClient.dpConnect()
        .add("ExampleDP_Arg1.")
        .action(hlg -> hlg.forEach(item ->
            System.out.println(item.getDpName() + " = " + item.getVariable())
        ))
        .connect();

    // Keep running or do other work...
    Thread.sleep(60000);

    // Cleanup
    m.stop();
}
```

---

## JManager Class

`at.rocworks.oa4j.base.JManager`

The JManager class is a singleton that manages the connection to WinCC OA. It handles the native API bridge, event dispatching, and task queue management.

### Constants

| Constant | Value | Description |
|----------|-------|-------------|
| `DB_MAN` | 3 | Database manager type |
| `API_MAN` | 7 | API manager type (default) |
| `MAX_ENQUEUE_SIZE_HIGH` | 100000 | Default upper threshold for task queue |
| `MAX_ENQUEUE_SIZE_LOW` | 50000 | Default lower threshold for task queue |

### Initialization Methods

#### `getInstance()`

```java
public static JManager getInstance()
```

Returns the singleton instance of the JManager.

**Returns:** The singleton JManager instance, or `null` if not yet initialized.

---

#### `init(String[] args)`

```java
public JManager init(String[] args) throws Exception
```

Initializes the manager by parsing command-line arguments.

**Parameters:**
- `args` - Command-line arguments array

**Supported arguments:**
| Argument | Description |
|----------|-------------|
| `-proj <name>` | Set the project name (required) |
| `-path <dir>` | Set the project directory path |
| `-num <n>` | Set the manager number |
| `-db` | Use DB_MAN manager type instead of API_MAN |
| `-noinit` | Skip resource initialization |
| `-debug` | Enable debug output |

**Returns:** This JManager instance for method chaining.

**Throws:** `Exception` if the native library cannot be loaded or version is incompatible.

**Example:**
```java
JManager m = new JManager();
m.init(new String[]{"-proj", "MyProject", "-num", "2"});
```

---

#### `init(String projName, int manType, int manNum)`

```java
public JManager init(String projName, int manType, int manNum) throws Exception
```

Initializes the manager with explicit configuration parameters.

**Parameters:**
- `projName` - The WinCC OA project name
- `manType` - The manager type (`API_MAN` or `DB_MAN`)
- `manNum` - The manager number

**Returns:** This JManager instance for method chaining.

**Example:**
```java
JManager m = new JManager();
m.init("MyProject", JManager.API_MAN, 1);
```

---

### Lifecycle Methods

#### `start()`

```java
public void start()
```

Starts the manager with default connection settings. Connects to both the data manager and event manager. This method blocks until the manager is fully started and connected.

---

#### `start(boolean connectToData, boolean connectToEvent)`

```java
public void start(boolean connectToData, boolean connectToEvent)
```

Starts the manager with specified connection options.

**Parameters:**
- `connectToData` - If true, connect to the data manager for datapoint operations
- `connectToEvent` - If true, connect to the event manager for alerts and events

---

#### `stop()`

```java
public void stop()
```

Stops the manager and disconnects from WinCC OA. This method should be called before application exit to ensure clean shutdown.

---

#### `pause()`

```java
public void pause()
```

Pauses the manager's dispatch loop. While paused, no new messages are processed from WinCC OA. This method blocks until the loop is fully paused.

---

#### `resume()`

```java
public void resume()
```

Resumes the manager's dispatch loop after a pause.

---

### Configuration Methods

#### `setProjName(String projName)`

```java
public JManager setProjName(String projName)
```

Sets the WinCC OA project name.

**Returns:** This JManager instance for method chaining.

---

#### `setManNum(int manNum)`

```java
public JManager setManNum(int manNum)
```

Sets the manager number. Used to distinguish multiple instances of the same manager type.

**Returns:** This JManager instance for method chaining.

---

#### `setLoopWaitUSec(int usec)`

```java
public JManager setLoopWaitUSec(int usec)
```

Sets the wait time for the main dispatch loop in microseconds. Lower values increase responsiveness but use more CPU.

**Parameters:**
- `usec` - Wait time in microseconds (default is 10000)

**Returns:** This JManager instance for method chaining.

---

#### `setMaxEnqueueSize(int high, int low)`

```java
public JManager setMaxEnqueueSize(int high, int low)
```

Sets the maximum enqueue size thresholds for the task queue. When the queue reaches the high threshold, new hotlinks are discarded. When the queue drops below the low threshold, normal processing resumes.

**Parameters:**
- `high` - Upper threshold at which hotlinks start being discarded
- `low` - Lower threshold at which normal processing resumes

**Returns:** This JManager instance for method chaining.

---

#### `setMaxDequeueSize(int high, int low)`

```java
public JManager setMaxDequeueSize(int high, int low)
```

Sets the maximum dequeue size thresholds for message queue processing.

**Returns:** This JManager instance for method chaining.

---

### State & Information Methods

#### `isEnabled()`

```java
public boolean isEnabled()
```

Checks if the WinCC OA native API is enabled (native library loaded).

**Returns:** `true` if the native API library is loaded.

---

#### `isConnected()`

```java
public boolean isConnected()
```

Checks if the manager is connected to WinCC OA.

**Returns:** `true` if the manager is currently connected.

---

#### `isActive()`

```java
public Boolean isActive()
```

Checks if the manager is connected to the active host in a redundant system. In non-redundant systems, this always returns `true`.

**Returns:** `true` if connected to the active host.

---

#### `getManName()`

```java
public String getManName()
```

Returns the manager name used for identification and logging.

**Returns:** The manager name in format `WCCOAjava{manNum}`.

---

#### `getManType()`

```java
public int getManType()
```

**Returns:** The manager type constant (`API_MAN` or `DB_MAN`).

---

#### `getManNum()`

```java
public int getManNum()
```

**Returns:** The manager number (default is 1).

---

#### `getProjPath()`

```java
public String getProjPath()
```

**Returns:** The absolute path to the WinCC OA project directory.

---

#### `getConfigDir()`

```java
public String getConfigDir()
```

**Returns:** The absolute path to the project's config directory.

---

#### `getLogDir()`

```java
public String getLogDir()
```

**Returns:** The absolute path to the project's log directory.

---

#### `getLogFile()`

```java
public String getLogFile()
```

**Returns:** The absolute path to the manager's log file (without extension).

---

#### `getConfigValue(String key)`

```java
public String getConfigValue(String key)
```

Retrieves a configuration value from the WinCC OA config file.

**Parameters:**
- `key` - The configuration key (e.g., `"java:classPath"`)

**Returns:** The configuration value, or `null` if not found.

---

#### `getConfigValueOrDefault(String key, String def)`

```java
public String getConfigValueOrDefault(String key, String def)
```

Retrieves a configuration value with a default fallback.

**Parameters:**
- `key` - The configuration key
- `def` - Default value if key is not found or empty

**Returns:** The configuration value, or the default.

---

#### `getLoopWaitUSec()`

```java
public int getLoopWaitUSec()
```

**Returns:** The wait time in microseconds for the dispatch loop.

---

#### `getEnqueueSize()`

```java
public int getEnqueueSize()
```

**Returns:** The number of tasks currently in the queue.

---

#### `getInitSysMsgData()`

```java
public Map<String, String> getInitSysMsgData()
```

Returns the initialization system message data containing system state, redundancy information, and configuration details.

**Returns:** A map of key-value pairs, or an empty map if not yet received.

---

### Task Execution Methods

#### `enqueueTask(Callable task)`

```java
public boolean enqueueTask(Callable task)
```

Adds a task to the manager's task queue for asynchronous execution. Tasks are executed in the main dispatch loop thread.

**Parameters:**
- `task` - The Callable to execute

**Returns:** `true` if the task was successfully added.

---

#### `executeTask(Callable task)`

```java
public Object executeTask(Callable task)
```

Executes a task synchronously in the manager's dispatch loop thread. This method blocks until the task completes.

**Parameters:**
- `task` - The Callable to execute

**Returns:** The result of the task execution, or `null` if no result.

---

### Logging Methods

#### `log(ErrPrio prio, ErrCode code, String text)`

```java
public static void log(ErrPrio prio, ErrCode code, String text)
```

Logs a message to the WinCC OA log system.

**Parameters:**
- `prio` - Priority level (e.g., `PRIO_INFO`, `PRIO_WARNING`, `PRIO_SEVERE`)
- `code` - Error code describing the state or error type
- `text` - The message text to log

**Example:**
```java
JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "Processing started");
```

---

#### `stackTrace(ErrPrio prio, ErrCode code, Throwable exception)`

```java
public static void stackTrace(ErrPrio prio, ErrCode code, Throwable exception)
```

Logs an exception's stack trace to the WinCC OA log system.

**Parameters:**
- `prio` - Priority level
- `code` - Error code
- `exception` - The exception to log

---

#### `stackTrace(Throwable exception)`

```java
public static void stackTrace(Throwable exception)
```

Logs an exception with `PRIO_SEVERE` priority and `UNEXPECTEDSTATE` error code.

---

---

## JClient Class

`at.rocworks.oa4j.base.JClient`

Static utility class providing the main API for WinCC OA datapoint operations. All methods are static and thread-safe.

### Connection Status

#### `isConnected()`

```java
public static boolean isConnected()
```

Checks if the manager is connected to WinCC OA.

**Returns:** `true` if the manager is initialized and connected.

---

### Reading Datapoints (dpGet)

#### `dpGet()`

```java
public static JDpGet dpGet()
```

Creates a new datapoint get request builder for reading multiple datapoints.

**Returns:** A new `JDpGet` builder instance.

**Example:**
```java
JDpMsgAnswer answer = JClient.dpGet()
    .add("ExampleDP_Arg1.")
    .add("ExampleDP_Arg2.")
    .await();

for (JDpVCItem item : answer) {
    System.out.println(item.getDpName() + " = " + item.getVariable());
}
```

---

#### `dpGet(String dp)`

```java
public static Variable dpGet(String dp)
```

Reads a single datapoint value synchronously.

**Parameters:**
- `dp` - The datapoint name (e.g., `"System1:ExampleDP_Arg1."`)

**Returns:** The datapoint value, or `null` if not found.

**Example:**
```java
Variable value = JClient.dpGet("ExampleDP_Arg1.");
double d = ((FloatVar)value).getValue();
```

---

#### `dpGet(String dp, VariablePtr var)`

```java
public static int dpGet(String dp, VariablePtr var)
```

Reads a single datapoint value into a variable pointer.

**Parameters:**
- `dp` - The datapoint name
- `var` - Output parameter that receives the value

**Returns:** Return code (0 = success).

---

#### `dpGet(List<String> dps)`

```java
public static List<Variable> dpGet(List<String> dps)
```

Reads multiple datapoint values synchronously.

**Parameters:**
- `dps` - List of datapoint names to read

**Returns:** List of values in the same order as the input.

---

### Historical Data (dpGetPeriod)

#### `dpGetPeriod(TimeVar start, TimeVar stop, int num)`

```java
public static JDpGetPeriod dpGetPeriod(TimeVar start, TimeVar stop, int num)
```

Creates a historical data query for a time period.

**Parameters:**
- `start` - Start time of the period
- `stop` - End time of the period
- `num` - Maximum number of values (0 = unlimited)

**Returns:** A new `JDpGetPeriod` builder instance.

**Example:**
```java
TimeVar start = new TimeVar(System.currentTimeMillis() - 3600000); // 1 hour ago
TimeVar stop = new TimeVar(System.currentTimeMillis());

JDpMsgAnswer answer = JClient.dpGetPeriod(start, stop, 100)
    .add("ExampleDP_Trend1.:_offline.._value")
    .await();
```

---

#### `dpGetPeriod(Date start, Date stop, int num)`

```java
public static JDpGetPeriod dpGetPeriod(Date start, Date stop, int num)
```

Creates a historical data query using Java Date objects.

---

#### `dpGetPeriod(Long start, long stop, int num)`

```java
public static JDpGetPeriod dpGetPeriod(Long start, long stop, int num)
```

Creates a historical data query using millisecond timestamps.

---

### Writing Datapoints (dpSet)

#### `dpSet()`

```java
public static JDpSet dpSet()
```

Creates a new datapoint set request builder for writing multiple datapoints.

**Returns:** A new `JDpSet` builder instance.

**Example:**
```java
JClient.dpSet()
    .add("ExampleDP_Arg1.", 100)
    .add("ExampleDP_Arg2.", "Hello")
    .add("ExampleDP_Arg3.", true)
    .send();
```

---

#### `dpSet(String dp, Object var)`

```java
public static JDpSet dpSet(String dp, Object var)
```

Writes a single datapoint value asynchronously (fire and forget).

**Parameters:**
- `dp` - The datapoint name
- `var` - The value to write (automatically converted)

**Returns:** The `JDpSet` instance (already sent).

**Example:**
```java
JClient.dpSet("ExampleDP_Arg1.", 42);
JClient.dpSet("ExampleDP_Arg2.", "text value");
JClient.dpSet("ExampleDP_Arg3.", 3.14159);
```

---

#### `dpSetWait(String dp, Object var)`

```java
public static int dpSetWait(String dp, Object var)
```

Writes a single datapoint value synchronously and waits for confirmation.

**Parameters:**
- `dp` - The datapoint name
- `var` - The value to write

**Returns:** Return code (0 = success).

**Example:**
```java
int result = JClient.dpSetWait("ExampleDP_Arg1.", 42);
if (result != 0) {
    System.err.println("Write failed with code: " + result);
}
```

---

### Subscribing to Changes (dpConnect)

#### `dpConnect()`

```java
public static JDpConnect dpConnect()
```

Creates a new datapoint connection (hotlink) builder. Hotlinks provide real-time notifications when datapoint values change.

**Returns:** A new `JDpConnect` builder instance.

**Example:**
```java
JDpConnect connection = JClient.dpConnect()
    .add("ExampleDP_Arg1.")
    .add("ExampleDP_Arg2.")
    .action((JDpHLGroup hlg) -> {
        hlg.forEach(item -> {
            System.out.println(item.getDpName() + " changed to " + item.getVariable());
        });
    })
    .connect();

// Later, to disconnect:
connection.disconnect();
```

---

### Alert Subscriptions

#### `alertConnect()`

```java
public static JAlertConnect alertConnect()
```

Creates a new alert connection builder for subscribing to alarm/alert notifications.

**Returns:** A new `JAlertConnect` builder instance.

---

### Querying Datapoints (dpQuery)

#### `dpQuery(String query)`

```java
public static JDpQuery dpQuery(String query)
```

Executes a datapoint query using WinCC OA SQL-like syntax.

**Parameters:**
- `query` - The query string in WinCC OA query syntax

**Returns:** A `JDpQuery` instance (already sent).

**Example:**
```java
// Query all online values matching a pattern
JDpMsgAnswer answer = JClient.dpQuery(
    "SELECT '_online.._value' FROM 'ExampleDP_*'"
).await();

// Query with multiple attributes
JDpMsgAnswer answer2 = JClient.dpQuery(
    "SELECT '_online.._value', '_online.._stime' FROM 'ExampleDP_Arg*' WHERE _DPT = \"ExampleDP_Float\""
).await();
```

---

#### `dpQueryConnectSingle(String query)`

```java
public static JDpQueryConnect dpQueryConnectSingle(String query)
```

Creates a query-based hotlink that triggers only on the first matching change.

**Parameters:**
- `query` - The query string

**Returns:** A new `JDpQueryConnectSingle` builder instance.

---

#### `dpQueryConnectAll(String query)`

```java
public static JDpQueryConnect dpQueryConnectAll(String query)
```

Creates a query-based hotlink that triggers on all matching changes.

**Parameters:**
- `query` - The query string

**Returns:** A new `JDpQueryConnectAll` builder instance.

---

### Datapoint Names

#### `dpNames(String pattern)`

```java
public static String[] dpNames(String pattern)
```

Returns datapoint names matching a pattern.

**Parameters:**
- `pattern` - Wildcard pattern (e.g., `"ExampleDP_*"`)

**Returns:** Array of matching datapoint names.

**Example:**
```java
String[] dps = JClient.dpNames("ExampleDP_*");
for (String dp : dps) {
    System.out.println(dp);
}
```

---

#### `dpNames(String pattern, String type)`

```java
public static String[] dpNames(String pattern, String type)
```

Returns datapoint names matching a pattern and type.

**Parameters:**
- `pattern` - Wildcard pattern
- `type` - Datapoint type name to filter by

**Returns:** Array of matching datapoint names.

---

### Datapoint Comments

#### `dpGetComment(DpIdentifierVar dpid)`

```java
public static LangTextVar dpGetComment(DpIdentifierVar dpid)
```

Retrieves the comment/description for a datapoint.

**Parameters:**
- `dpid` - The datapoint identifier

**Returns:** The language-dependent comment text.

---

### User Authentication

#### `checkPassword(String username, String password)`

```java
public static int checkPassword(String username, String password)
```

Verifies if the given password is valid for the requested user.

**Parameters:**
- `username` - The username to check
- `password` - The password to verify

**Returns:**
- `0` - OK (valid)
- `-1` - Invalid user
- `-2` - Wrong password

---

#### `setUserId(String username, String password)`

```java
public static boolean setUserId(String username, String password)
```

Sets a new user ID for the current session. The user is set when:
- The ID matches the password, or
- The current ID is ROOT_USER and the new user exists, or
- The new user ID is DEFAULT_USER

**Parameters:**
- `username` - The username to set
- `password` - The user's password

**Returns:** `true` if the user was successfully set.

---

## Related Classes

| Class | Description |
|-------|-------------|
| `JDpGet` | Builder for reading datapoint values |
| `JDpSet` | Builder for writing datapoint values |
| `JDpConnect` | Builder for subscribing to datapoint changes |
| `JDpQuery` | Builder for querying datapoints |
| `JDpGetPeriod` | Builder for historical data queries |
| `JAlertConnect` | Builder for alert subscriptions |
| `JDpMsgAnswer` | Response container for datapoint operations |
| `JDpVCItem` | Individual datapoint value/change item |
| `JDpHLGroup` | Group of hotlink items received in a callback |
| `Variable` | Base class for all WinCC OA variable types |

## Variable Types

| Java Class | WinCC OA Type |
|------------|---------------|
| `FloatVar` | float |
| `IntegerVar` | int |
| `LongVar` | long |
| `UIntegerVar` | uint |
| `TextVar` | string |
| `TimeVar` | time |
| `CharVar` | char |
| `BitVar` | bit |
| `Bit32Var` | bit32 |
| `Bit64Var` | bit64 |
| `DynVar` | dyn_* (dynamic arrays) |
| `DpIdentifierVar` | dpidentifier |

Use `Variable.valueOf()` for automatic type conversion.

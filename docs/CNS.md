# Common Name Service (CNS) API Reference

This document describes the complete Java API for working with WinCC OA's Common Name Service (CNS). The CNS provides a hierarchical naming structure that allows alternative, user-friendly names for datapoints.

## Table of Contents

1. [Overview](#overview)
2. [Data Classes](#data-classes)
3. [View Management](#view-management)
4. [Tree Management](#tree-management)
5. [Node Management](#node-management)
6. [Search and Query](#search-and-query)
7. [Change Notifications](#change-notifications)
8. [Validation](#validation)
9. [Complete Examples](#complete-examples)

---

## Overview

The Common Name Service (CNS) allows you to:

- Create hierarchical tree structures for organizing datapoints
- Assign multi-language display names to datapoints
- Use alternative navigation paths to access datapoints
- Organize datapoints by functional relationships instead of technical structure

**Key Concepts:**

- **View**: A separate naming hierarchy (like a virtual file system)
- **Tree**: A root node within a view
- **Node**: An element in the hierarchy that can have children
- **Display Name**: Multi-language text shown to users
- **Node ID**: Technical identifier for the node
- **Data Link**: Connection from a node to a datapoint

**CNS Path Format:**

```
System:view/tree/node/subnode/...
Example: System1:Plant/Area1/Device1/Temperature
```

All CNS operations are available through the `WinCCOA` class using methods prefixed with `cns`.

---

## Data Classes

### CnsNode

`at.rocworks.oa4j.var.CnsNode`

Represents a node in the CNS hierarchy.

**Properties:**

| Property | Type | Description |
|----------|------|-------------|
| `path` | String | Full CNS path (e.g., "System1:view/tree/node") |
| `name` | String | Node identifier name (technical ID) |
| `system` | String | System name (e.g., "System1") |
| `view` | String | View name |
| `displayNames` | LangTextVar | Multi-language display names |
| `displayPaths` | LangTextVar | Multi-language display paths |
| `dpId` | DpIdentifierVar | Linked datapoint identifier (if any) |
| `nodeType` | int | Node type (see CnsDataIdentifier.Types) |
| `userData` | byte[] | Custom user data |

**Methods:**

```java
String getPath()               // Get full CNS path
String getName()               // Get node ID
String getSystem()             // Get system name
String getView()               // Get view name
LangTextVar getDisplayNames()  // Get display names
DpIdentifierVar getDpId()      // Get linked datapoint
int getNodeType()              // Get node type
```

---

### CnsDataIdentifier

`at.rocworks.oa4j.var.CnsDataIdentifier`

Links a CNS node to a datapoint.

**Properties:**

| Property | Type | Description |
|----------|------|-------------|
| `dpId` | DpIdentifierVar | Datapoint identifier |
| `type` | int | Node type |
| `userData` | byte[] | Custom user data |

**Node Types:**

```java
// Constants in CnsDataIdentifier.Types
public static final int NO_TYPE = 0;      // Structure node without data link
public static final int DATAPOINT = 1;    // Node linked to a datapoint
public static final int DP_TYPE = 2;      // Node linked to a datapoint type
public static final int OPC_ITEM = 3;     // Node linked to an OPC item
public static final int ALL_TYPES = 4;    // All types (for search operations)
```

---

### LangTextVar

`at.rocworks.oa4j.var.LangTextVar`

Multi-language text container.

**Methods:**

```java
void setText(int langIndex, String text)  // Set text for a language
String getText(int langIndex)             // Get text for a language
```

**Language Indices:**

| Index | Language |
|-------|----------|
| 0 | English |
| 1 | German |
| 2 | French |
| 3 | Italian |
| ... | Other languages as configured |

**Example:**

```java
LangTextVar names = new LangTextVar();
names.setText(0, "Temperature");      // English
names.setText(1, "Temperatur");       // German
names.setText(2, "Température");      // French
```

---

### CnsObserver

`at.rocworks.oa4j.var.CnsObserver`

Interface for receiving CNS change notifications.

**Method to Implement:**

```java
void onCnsChange(String path, int changeType)
```

**Change Types:**

```java
// Constants in CnsObserver.ChangeType
public static final int STRUCTURE_CHANGED = 0;       // Nodes added/removed
public static final int NAMES_CHANGED = 1;           // Node names changed
public static final int DATA_CHANGED = 2;            // Node data (DP link) changed
public static final int VIEW_SEPARATOR_CHANGED = 3;  // View separator changed
public static final int SYSTEM_NAMES_CHANGED = 4;    // System names changed
```

---

## View Management

Views provide separate naming hierarchies within the CNS.

### cnsCreateView

Creates a new CNS view.

```java
public int cnsCreateView(String system, String viewId, String separator, LangTextVar displayNames)
```

**Parameters:**
- `system` - System name (use `null` for default system)
- `viewId` - Technical identifier for the view
- `separator` - Path separator character (typically `"/"`)
- `displayNames` - Multi-language display names for the view

**Returns:** `0` on success, non-zero error code on failure

**Example:**

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.var.LangTextVar;

WinCCOA oa = WinCCOA.connect(args);

// Create display names
LangTextVar names = new LangTextVar();
names.setText(0, "Plant Overview");     // English
names.setText(1, "Anlagenübersicht");  // German

// Create view
int result = oa.cnsCreateView(null, "plant", "/", names);
if (result == 0) {
    WinCCOA.log("View created successfully");
}
```

---

### cnsDeleteView

Deletes a CNS view and all its contents.

```java
public int cnsDeleteView(String system, String viewId)
```

**Parameters:**
- `system` - System name (use `null` for default)
- `viewId` - View identifier to delete

**Returns:** `0` on success, non-zero on failure

**Example:**

```java
int result = oa.cnsDeleteView(null, "plant");
```

---

### cnsGetViews

Gets all views in a system.

```java
public String[] cnsGetViews(String system)
```

**Parameters:**
- `system` - System name (use `null` for default)

**Returns:** Array of view identifiers

**Example:**

```java
String[] views = oa.cnsGetViews(null);
for (String view : views) {
    System.out.println("View: " + view);
}
```

---

### cnsGetViewDisplayNames

Gets the display names for a view.

```java
public LangTextVar cnsGetViewDisplayNames(String system, String viewId)
```

**Parameters:**
- `system` - System name
- `viewId` - View identifier

**Returns:** LangTextVar with display names

**Example:**

```java
LangTextVar names = oa.cnsGetViewDisplayNames(null, "plant");
System.out.println("English: " + names.getText(0));
System.out.println("German: " + names.getText(1));
```

---

### cnsChangeViewDisplayNames

Changes the display names for a view.

```java
public int cnsChangeViewDisplayNames(String system, String viewId, LangTextVar displayNames)
```

**Parameters:**
- `system` - System name
- `viewId` - View identifier
- `displayNames` - New display names

**Returns:** `0` on success, non-zero on failure

---

### cnsGetViewSeparators

Gets the path separator for a view.

```java
public String cnsGetViewSeparators(String system, String viewId)
```

**Parameters:**
- `system` - System name
- `viewId` - View identifier

**Returns:** Separator string

---

### cnsChangeViewSeparators

Changes the path separator for a view.

```java
public int cnsChangeViewSeparators(String system, String viewId, String separator)
```

**Parameters:**
- `system` - System name
- `viewId` - View identifier
- `separator` - New separator (typically `"/"` or `"."` or `"\\"`)

**Returns:** `0` on success, non-zero on failure

---

## Tree Management

Trees are root nodes within a view.

### cnsAddTree

Adds a new tree (root node) to a view.

```java
public int cnsAddTree(String system, String viewId, String nodeId, int nodeType,
                      DpIdentifierVar dpId, LangTextVar displayNames)
```

**Parameters:**
- `system` - System name (use `null` for default)
- `viewId` - View identifier
- `nodeId` - Technical identifier for the tree root
- `nodeType` - Node type (usually `CnsDataIdentifier.Types.NO_TYPE` for structure nodes)
- `dpId` - Datapoint identifier (use `null` for structure nodes)
- `displayNames` - Multi-language display names

**Returns:** `0` on success, non-zero on failure

**Example:**

```java
import at.rocworks.oa4j.var.CnsDataIdentifier;

// Create a structure node (no datapoint link)
LangTextVar treeNames = new LangTextVar();
treeNames.setText(0, "Production Area 1");
treeNames.setText(1, "Produktionsbereich 1");

int result = oa.cnsAddTree(
    null,                              // Default system
    "plant",                           // View
    "area1",                           // Node ID
    CnsDataIdentifier.Types.NO_TYPE,   // Structure node
    null,                              // No datapoint
    treeNames                          // Display names
);
```

---

### cnsDeleteTree

Deletes a tree or subtree.

```java
public int cnsDeleteTree(String cnsPath)
```

**Parameters:**
- `cnsPath` - Full CNS path to the tree or subtree

**Returns:** `0` on success, non-zero on failure

**Example:**

```java
int result = oa.cnsDeleteTree("System1:plant/area1");
```

---

### cnsGetTrees

Gets all trees in a view.

```java
public String[] cnsGetTrees(String system, String viewId)
```

**Parameters:**
- `system` - System name
- `viewId` - View identifier

**Returns:** Array of tree root paths

**Example:**

```java
String[] trees = oa.cnsGetTrees(null, "plant");
for (String tree : trees) {
    System.out.println("Tree: " + tree);
}
```

---

### cnsGetRoot

Gets the root node of a tree containing a given path.

```java
public String cnsGetRoot(String cnsPath)
```

**Parameters:**
- `cnsPath` - Any CNS path within the tree

**Returns:** Path to the root node

**Example:**

```java
String root = oa.cnsGetRoot("System1:plant/area1/device1/temp");
// Returns: "System1:plant/area1"
```

---

## Node Management

### cnsAddNode

Adds a child node to an existing parent node.

```java
public int cnsAddNode(String parentPath, String nodeId, int nodeType,
                      DpIdentifierVar dpId, LangTextVar displayNames)
```

**Parameters:**
- `parentPath` - Full CNS path to the parent node
- `nodeId` - Technical identifier for the new node
- `nodeType` - Node type (from CnsDataIdentifier.Types)
- `dpId` - Datapoint identifier (required if nodeType is DATAPOINT)
- `displayNames` - Multi-language display names

**Returns:** `0` on success, non-zero on failure

**Example - Structure Node:**

```java
LangTextVar names = new LangTextVar();
names.setText(0, "Devices");

int result = oa.cnsAddNode(
    "System1:plant/area1",           // Parent path
    "devices",                       // Node ID
    CnsDataIdentifier.Types.NO_TYPE, // Structure node
    null,                            // No datapoint
    names                            // Display names
);
```

**Example - Datapoint Node:**

```java
import at.rocworks.oa4j.var.DpIdentifierVar;

// Get the datapoint identifier (you need to get this from elsewhere)
Variable dpIdVar = oa.dpGet("Device1.temperature:_dp_fct_param");
DpIdentifierVar dpId = (DpIdentifierVar) dpIdVar;

LangTextVar names = new LangTextVar();
names.setText(0, "Temperature Sensor");
names.setText(1, "Temperatursensor");

int result = oa.cnsAddNode(
    "System1:plant/area1/devices",     // Parent path
    "temp1",                           // Node ID
    CnsDataIdentifier.Types.DATAPOINT, // Datapoint node
    dpId,                              // Datapoint ID
    names                              // Display names
);
```

---

### cnsGetNode

Gets full information about a node.

```java
public CnsNode cnsGetNode(String cnsPath)
```

**Parameters:**
- `cnsPath` - Full CNS path to the node

**Returns:** CnsNode object, or `null` if not found

**Example:**

```java
CnsNode node = oa.cnsGetNode("System1:plant/area1/devices/temp1");
if (node != null) {
    System.out.println("Node ID: " + node.getName());
    System.out.println("Display Name: " + node.getDisplayNames().getText(0));
    System.out.println("Type: " + node.getNodeType());
    if (node.getDpId() != null) {
        System.out.println("Linked DP: " + node.getDpId());
    }
}
```

---

### cnsGetId

Gets just the data identifier for a node.

```java
public CnsDataIdentifier cnsGetId(String cnsPath)
```

**Parameters:**
- `cnsPath` - Full CNS path

**Returns:** CnsDataIdentifier, or `null` if not found

**Example:**

```java
CnsDataIdentifier dataId = oa.cnsGetId("System1:plant/area1/devices/temp1");
if (dataId != null) {
    System.out.println("Type: " + dataId.getType());
    System.out.println("DP ID: " + dataId.getDpId());
}
```

---

### cnsGetChildren

Gets all child nodes of a parent.

```java
public String[] cnsGetChildren(String cnsPath)
```

**Parameters:**
- `cnsPath` - Full CNS path to the parent node

**Returns:** Array of child node paths

**Example:**

```java
String[] children = oa.cnsGetChildren("System1:plant/area1");
for (String child : children) {
    System.out.println("Child: " + child);
}
```

---

### cnsGetParent

Gets the parent of a node.

```java
public String cnsGetParent(String cnsPath)
```

**Parameters:**
- `cnsPath` - Full CNS path to a node

**Returns:** Parent node path, or `null` if at root

**Example:**

```java
String parent = oa.cnsGetParent("System1:plant/area1/devices/temp1");
// Returns: "System1:plant/area1/devices"
```

---

### cnsChangeNodeData

Changes the datapoint link and type of a node.

```java
public int cnsChangeNodeData(String cnsPath, DpIdentifierVar dpId, int nodeType)
```

**Parameters:**
- `cnsPath` - Full CNS path to the node
- `dpId` - New datapoint identifier
- `nodeType` - New node type

**Returns:** `0` on success, non-zero on failure

---

### cnsChangeNodeDisplayNames

Changes the display names of a node.

```java
public int cnsChangeNodeDisplayNames(String cnsPath, LangTextVar displayNames)
```

**Parameters:**
- `cnsPath` - Full CNS path to the node
- `displayNames` - New display names

**Returns:** `0` on success, non-zero on failure

**Example:**

```java
LangTextVar newNames = new LangTextVar();
newNames.setText(0, "Temperature Sensor 1");
newNames.setText(1, "Temperatursensor 1");

int result = oa.cnsChangeNodeDisplayNames(
    "System1:plant/area1/devices/temp1",
    newNames
);
```

---

## Search and Query

### cnsGetNodesByName

Searches for nodes by name pattern.

```java
public String[] cnsGetNodesByName(String system, String viewId, String pattern,
                                   int searchMode, int langIdx)
```

**Parameters:**
- `system` - System name (use `null` for default)
- `viewId` - View identifier
- `pattern` - Search pattern (supports wildcards: `*` and `?`)
- `searchMode` - Search mode (see below)
- `langIdx` - Language index (0 for English, 1 for German, etc.)

**Search Modes:**

| Value | Constant | Description |
|-------|----------|-------------|
| 0 | NAME | Search by node ID (technical name) |
| 1 | DISPLAY_NAME | Search by display name |
| 2 | ALL_NAMES | Search both ID and display names |

**Returns:** Array of matching CNS paths

**Example:**

```java
// Search by technical name
String[] matches = oa.cnsGetNodesByName(
    null,      // Default system
    "plant",   // View
    "*temp*",  // Pattern
    0,         // Search by ID
    0          // Language (not used for ID search)
);

// Search by display name
String[] displayMatches = oa.cnsGetNodesByName(
    null,
    "plant",
    "*Temperature*",  // Pattern
    1,                // Search by display name
    0                 // English
);

for (String path : matches) {
    System.out.println("Found: " + path);
}
```

---

### cnsGetNodesByData

Finds nodes linked to a specific datapoint.

```java
public String[] cnsGetNodesByData(String system, String viewId, DpIdentifierVar dpId)
```

**Parameters:**
- `system` - System name
- `viewId` - View identifier
- `dpId` - Datapoint identifier to search for

**Returns:** Array of CNS paths linked to this datapoint

**Example:**

```java
// Get datapoint identifier
Variable dpIdVar = oa.dpGet("Device1.temperature:_dp_fct_param");
DpIdentifierVar dpId = (DpIdentifierVar) dpIdVar;

// Find all nodes linked to this datapoint
String[] nodes = oa.cnsGetNodesByData(null, "plant", dpId);
for (String node : nodes) {
    System.out.println("Node: " + node);
}
```

---

### cnsGetIdSet

Gets datapoint identifiers for nodes matching a pattern (more efficient than cnsGetNodesByName).

```java
public DpIdentifierVar[] cnsGetIdSet(String system, String viewId, String pattern,
                                      int searchMode, int langIdx)
```

**Parameters:**
- `system` - System name
- `viewId` - View identifier
- `pattern` - Search pattern
- `searchMode` - Search mode (0=NAME, 1=DISPLAY_NAME, 2=ALL_NAMES)
- `langIdx` - Language index

**Returns:** Array of DpIdentifierVar for matching nodes

**Example:**

```java
// Get all datapoint IDs for temperature nodes
DpIdentifierVar[] dpIds = oa.cnsGetIdSet(
    null,
    "plant",
    "*temp*",
    2,  // Search both ID and display names
    0
);

// Read all values
for (DpIdentifierVar dpId : dpIds) {
    if (dpId != null) {
        // Use dpId to read the value
        System.out.println("DP ID: " + dpId);
    }
}
```

---

### cnsSubStr

Extracts parts of a CNS path.

```java
public String cnsSubStr(String cnsPath, int mask, boolean resolve)
```

**Parameters:**
- `cnsPath` - Full CNS path
- `mask` - Bitmask specifying which parts to extract
- `resolve` - If `true`, use display names; if `false`, use technical IDs

**Returns:** Extracted path parts

---

## Change Notifications

### cnsAddObserver

Registers for CNS change notifications.

```java
public int cnsAddObserver(CnsObserver observer)
```

**Parameters:**
- `observer` - Observer implementation

**Returns:** Observer ID (use this to remove the observer later)

**Example:**

```java
import at.rocworks.oa4j.var.CnsObserver;

// Create observer
CnsObserver observer = new CnsObserver() {
    @Override
    public void onCnsChange(String path, int changeType) {
        switch (changeType) {
            case CnsObserver.ChangeType.STRUCTURE_CHANGED:
                WinCCOA.log("Structure changed: " + path);
                break;
            case CnsObserver.ChangeType.NAMES_CHANGED:
                WinCCOA.log("Names changed: " + path);
                break;
            case CnsObserver.ChangeType.DATA_CHANGED:
                WinCCOA.log("Data changed: " + path);
                break;
            case CnsObserver.ChangeType.VIEW_SEPARATOR_CHANGED:
                WinCCOA.log("Separator changed: " + path);
                break;
            case CnsObserver.ChangeType.SYSTEM_NAMES_CHANGED:
                WinCCOA.log("System names changed: " + path);
                break;
        }
    }
};

// Register observer
int observerId = oa.cnsAddObserver(observer);
WinCCOA.log("Observer registered with ID: " + observerId);
```

---

### cnsRemoveObserver

Unregisters a CNS change observer.

```java
public int cnsRemoveObserver(int observerId)
```

**Parameters:**
- `observerId` - Observer ID returned by cnsAddObserver

**Returns:** `0` on success, non-zero on failure

**Example:**

```java
int result = oa.cnsRemoveObserver(observerId);
```

---

## Validation

### cnsCheckId

Validates a node ID.

```java
public boolean cnsCheckId(String id)
```

**Parameters:**
- `id` - Node ID to validate

**Returns:** `true` if valid

**Example:**

```java
if (oa.cnsCheckId("temp_sensor_1")) {
    System.out.println("Valid node ID");
} else {
    System.err.println("Invalid node ID");
}
```

---

### cnsCheckName

Validates a display name.

```java
public boolean cnsCheckName(String displayName)
```

**Parameters:**
- `displayName` - Display name to validate

**Returns:** `true` if valid

**Example:**

```java
if (oa.cnsCheckName("Temperature Sensor 1")) {
    System.out.println("Valid display name");
}
```

---

### cnsCheckSeparator

Validates a path separator character.

```java
public boolean cnsCheckSeparator(char separator)
```

**Parameters:**
- `separator` - Separator character to validate

**Returns:** `true` if valid

**Example:**

```java
if (oa.cnsCheckSeparator('/')) {
    System.out.println("Valid separator");
}
```

---

## Complete Examples

### Example 1: Create Complete Hierarchy

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.var.*;

public class CreateCnsHierarchy {
    public static void main(String[] args) throws Exception {
        WinCCOA oa = WinCCOA.connect(args);

        try {
            // Step 1: Create a view
            LangTextVar viewNames = new LangTextVar();
            viewNames.setText(0, "Plant Overview");
            viewNames.setText(1, "Anlagenübersicht");

            int result = oa.cnsCreateView(null, "plant", "/", viewNames);
            WinCCOA.log("View created: " + result);

            // Step 2: Create tree (root node)
            LangTextVar area1Names = new LangTextVar();
            area1Names.setText(0, "Production Area 1");
            area1Names.setText(1, "Produktionsbereich 1");

            result = oa.cnsAddTree(
                null,
                "plant",
                "area1",
                CnsDataIdentifier.Types.NO_TYPE,
                null,
                area1Names
            );
            WinCCOA.log("Tree created: " + result);

            // Step 3: Add device container
            LangTextVar devicesNames = new LangTextVar();
            devicesNames.setText(0, "Devices");
            devicesNames.setText(1, "Geräte");

            result = oa.cnsAddNode(
                "System1:plant/area1",
                "devices",
                CnsDataIdentifier.Types.NO_TYPE,
                null,
                devicesNames
            );
            WinCCOA.log("Container created: " + result);

            // Step 4: Link datapoints
            // Assuming we have datapoints Device1, Device2, etc.
            String[] devices = oa.dpNames("Device*");
            for (String device : devices) {
                // Get datapoint identifier
                Variable dpIdVar = oa.dpGet(device + ":_dp_fct_param");
                if (dpIdVar instanceof DpIdentifierVar) {
                    DpIdentifierVar dpId = (DpIdentifierVar) dpIdVar;

                    LangTextVar dpNames = new LangTextVar();
                    dpNames.setText(0, device);

                    result = oa.cnsAddNode(
                        "System1:plant/area1/devices",
                        device.toLowerCase(),
                        CnsDataIdentifier.Types.DATAPOINT,
                        dpId,
                        dpNames
                    );
                    WinCCOA.log("Device node created: " + device + " -> " + result);
                }
            }

            WinCCOA.log("Hierarchy creation complete!");

        } finally {
            oa.disconnect();
        }
    }
}
```

---

### Example 2: Navigate and Query

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.var.*;

public class NavigateCns {
    public static void main(String[] args) throws Exception {
        WinCCOA oa = WinCCOA.connect(args);

        try {
            // List all views
            System.out.println("=== All Views ===");
            String[] views = oa.cnsGetViews(null);
            for (String view : views) {
                System.out.println("View: " + view);

                LangTextVar names = oa.cnsGetViewDisplayNames(null, view);
                System.out.println("  Display: " + names.getText(0));
            }

            // List trees in a view
            System.out.println("\n=== Trees in 'plant' ===");
            String[] trees = oa.cnsGetTrees(null, "plant");
            for (String tree : trees) {
                System.out.println("Tree: " + tree);
            }

            // Navigate a tree
            System.out.println("\n=== Tree Structure ===");
            printTree(oa, "System1:plant/area1", 0);

            // Search for nodes
            System.out.println("\n=== Search Results ===");
            String[] results = oa.cnsGetNodesByName(
                null,
                "plant",
                "*temp*",
                2,  // Search all names
                0
            );
            for (String result : results) {
                System.out.println("Found: " + result);
            }

        } finally {
            oa.disconnect();
        }
    }

    // Recursive function to print tree structure
    private static void printTree(WinCCOA oa, String path, int depth) {
        String indent = "  ".repeat(depth);

        CnsNode node = oa.cnsGetNode(path);
        if (node != null) {
            System.out.println(indent + "+ " + node.getName() +
                             " (" + node.getDisplayNames().getText(0) + ")");

            if (node.getDpId() != null) {
                System.out.println(indent + "  -> " + node.getDpId());
            }

            String[] children = oa.cnsGetChildren(path);
            if (children != null) {
                for (String child : children) {
                    printTree(oa, child, depth + 1);
                }
            }
        }
    }
}
```

---

### Example 3: Monitor Changes

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.var.CnsObserver;

public class MonitorCnsChanges {
    public static void main(String[] args) throws Exception {
        WinCCOA oa = WinCCOA.connect(args);

        try {
            // Create observer
            CnsObserver observer = new CnsObserver() {
                @Override
                public void onCnsChange(String path, int changeType) {
                    String typeStr = switch (changeType) {
                        case CnsObserver.ChangeType.STRUCTURE_CHANGED ->
                            "STRUCTURE_CHANGED";
                        case CnsObserver.ChangeType.NAMES_CHANGED ->
                            "NAMES_CHANGED";
                        case CnsObserver.ChangeType.DATA_CHANGED ->
                            "DATA_CHANGED";
                        case CnsObserver.ChangeType.VIEW_SEPARATOR_CHANGED ->
                            "SEPARATOR_CHANGED";
                        case CnsObserver.ChangeType.SYSTEM_NAMES_CHANGED ->
                            "SYSTEM_NAMES_CHANGED";
                        default -> "UNKNOWN";
                    };

                    WinCCOA.log("CNS Change: " + typeStr + " at " + path);
                }
            };

            // Register observer
            int observerId = oa.cnsAddObserver(observer);
            WinCCOA.log("Observer registered with ID: " + observerId);

            // Keep running
            WinCCOA.log("Monitoring CNS changes. Press Ctrl+C to stop.");
            Thread.sleep(Long.MAX_VALUE);

        } finally {
            oa.disconnect();
        }
    }
}
```

---

### Example 4: Read Values via CNS

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.var.*;

public class ReadViaCns {
    public static void main(String[] args) throws Exception {
        WinCCOA oa = WinCCOA.connect(args);

        try {
            // Get all temperature sensors via CNS
            DpIdentifierVar[] sensors = oa.cnsGetIdSet(
                null,
                "plant",
                "*temperature*",
                1,  // Search by display name
                0   // English
            );

            System.out.println("Found " + sensors.length + " temperature sensors");

            // Read values for each sensor
            for (DpIdentifierVar dpId : sensors) {
                if (dpId != null) {
                    // Find the CNS path for this datapoint
                    String[] paths = oa.cnsGetNodesByData(null, "plant", dpId);
                    if (paths.length > 0) {
                        CnsNode node = oa.cnsGetNode(paths[0]);
                        String displayName = node.getDisplayNames().getText(0);

                        // Read the value
                        // Note: You would need to construct the full DP element path
                        System.out.println(displayName + " -> " + dpId);
                    }
                }
            }

        } finally {
            oa.disconnect();
        }
    }
}
```

---

### Example 5: Update Display Names

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.var.LangTextVar;

public class UpdateDisplayNames {
    public static void main(String[] args) throws Exception {
        WinCCOA oa = WinCCOA.connect(args);

        try {
            // Search for all temperature nodes
            String[] tempNodes = oa.cnsGetNodesByName(
                null,
                "plant",
                "*temp*",
                0,  // Search by ID
                0
            );

            // Update display names for all temperature nodes
            for (String path : tempNodes) {
                // Create new display names
                LangTextVar newNames = new LangTextVar();
                newNames.setText(0, "Temperature Sensor (Updated)");
                newNames.setText(1, "Temperatursensor (Aktualisiert)");

                int result = oa.cnsChangeNodeDisplayNames(path, newNames);
                if (result == 0) {
                    WinCCOA.log("Updated: " + path);
                } else {
                    WinCCOA.logError("Failed to update: " + path);
                }
            }

        } finally {
            oa.disconnect();
        }
    }
}
```

---

## Best Practices

1. **Use Structure Nodes:** Create logical groupings with structure nodes (NO_TYPE)

```java
// Good - organized hierarchy
oa.cnsAddTree(null, "plant", "area1", CnsDataIdentifier.Types.NO_TYPE, null, names);
oa.cnsAddNode("System1:plant/area1", "devices", CnsDataIdentifier.Types.NO_TYPE, null, names);
oa.cnsAddNode("System1:plant/area1/devices", "temp1", CnsDataIdentifier.Types.DATAPOINT, dpId, names);
```

2. **Always Use Multi-Language Names:** Even if you only use one language

```java
LangTextVar names = new LangTextVar();
names.setText(0, "English Name");
names.setText(1, "German Name");  // Add multiple languages
```

3. **Check Return Codes:** Always check if operations succeeded

```java
int result = oa.cnsAddNode(...);
if (result != 0) {
    WinCCOA.logError("Failed to add node, error code: " + result);
}
```

4. **Use Descriptive IDs:** Technical IDs should be clear and consistent

```java
// Good
oa.cnsAddNode("...", "temp_sensor_1", ...);

// Less clear
oa.cnsAddNode("...", "ts1", ...);
```

5. **Search Efficiently:** Use `cnsGetIdSet` instead of `cnsGetNodesByName` when you only need datapoint IDs

```java
// More efficient
DpIdentifierVar[] dpIds = oa.cnsGetIdSet(null, "plant", "*temp*", 0, 0);

// Less efficient
String[] paths = oa.cnsGetNodesByName(null, "plant", "*temp*", 0, 0);
// Then get dpId for each path...
```

---

## Return Values

Most CNS methods follow these conventions:

| Return Type | Success | Failure |
|-------------|---------|---------|
| `int` | `0` | Non-zero error code |
| `String` | Valid string | `null` |
| `String[]` | Array (may be empty) | `null` |
| `Object` | Valid object | `null` |
| `boolean` | `true` | `false` |

---

## Summary

The CNS API allows you to:

- **Create** views, trees, and nodes with `cnsCreate*` and `cnsAdd*` methods
- **Query** the hierarchy with `cnsGet*` methods
- **Search** for nodes with `cnsGetNodesByName` and `cnsGetIdSet`
- **Modify** existing nodes with `cnsChange*` methods
- **Monitor** changes with `cnsAddObserver`
- **Validate** input with `cnsCheck*` methods

All operations use the `WinCCOA` class instance obtained from `WinCCOA.connect()`.

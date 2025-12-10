# Datapoint Type and Datapoint Management API

This document describes the complete Java API for managing datapoint types and datapoints in WinCC OA. These operations allow you to programmatically create and manage the data structure of your WinCC OA system.

## Table of Contents

1. [Overview](#overview)
2. [Datapoint Type Management](#datapoint-type-management)
3. [Datapoint Management](#datapoint-management)
4. [Data Classes](#data-classes)
5. [Complete Examples](#complete-examples)

---

## Overview

The Datapoint Type and Datapoint Management API provides functions for:

- **Datapoint Type Management**: Create, modify, query, and delete datapoint type definitions
- **Datapoint Management**: Create, delete, and check existence of datapoint instances

**Key Concepts:**

- **Datapoint Type**: A template that defines the structure of datapoints (like a class or struct)
- **Datapoint**: An instance of a datapoint type (like an object)
- **Element**: A field within a datapoint type (can be primitive or nested structure)

All operations are thread-safe and accessible via the `WinCCOA` class.

---

## Datapoint Type Management

Datapoint types define the structure and data types that datapoints can contain.

### dpTypeCreate

Creates a new datapoint type.

```java
public int dpTypeCreate(DpTypeElement definition)
```

**Parameters:**
- `definition` - The root element of the type definition tree (must be type RECORD)

**Returns:** `0` on success, non-zero error code on failure

**Important Notes:**
- The root element must always be of type `RECORD`
- Element IDs must be unique within the type
- Element IDs must be non-zero (use sequential numbering starting from 1)
- Type creation is asynchronous; allow time for the type to be registered before using it

**Example - Simple Type:**

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.var.DpTypeElement;
import at.rocworks.oa4j.var.DpElementType;

WinCCOA oa = WinCCOA.connect(args);

// Create a simple type with float, int, and text fields
DpTypeElement root = new DpTypeElement("SensorType", 1, DpElementType.RECORD);

root.addChild(new DpTypeElement("temperature", 2, DpElementType.FLOAT));
root.addChild(new DpTypeElement("humidity", 3, DpElementType.FLOAT));
root.addChild(new DpTypeElement("name", 4, DpElementType.TEXT));
root.addChild(new DpTypeElement("active", 5, DpElementType.BIT));

int result = oa.dpTypeCreate(root);
if (result == 0) {
    WinCCOA.log("Type created successfully");
    // Wait for type to be registered
    Thread.sleep(2000);
} else {
    WinCCOA.logError("Type creation failed: " + result);
}
```

**Example - Nested Type:**

```java
// Create a complex nested type:
// DeviceType (RECORD)
//   ├── id (INT)
//   ├── name (TEXT)
//   ├── sensors (RECORD)
//   │     ├── temperature (FLOAT)
//   │     ├── pressure (FLOAT)
//   │     └── humidity (FLOAT)
//   └── config (RECORD)
//         ├── enabled (BIT)
//         ├── interval (INT)
//         └── threshold (FLOAT)

DpTypeElement root = new DpTypeElement("DeviceType", 1, DpElementType.RECORD);

root.addChild(new DpTypeElement("id", 2, DpElementType.INT));
root.addChild(new DpTypeElement("name", 3, DpElementType.TEXT));

// Add nested sensors structure
DpTypeElement sensors = new DpTypeElement("sensors", 4, DpElementType.RECORD);
sensors.addChild(new DpTypeElement("temperature", 5, DpElementType.FLOAT));
sensors.addChild(new DpTypeElement("pressure", 6, DpElementType.FLOAT));
sensors.addChild(new DpTypeElement("humidity", 7, DpElementType.FLOAT));
root.addChild(sensors);

// Add nested config structure
DpTypeElement config = new DpTypeElement("config", 8, DpElementType.RECORD);
config.addChild(new DpTypeElement("enabled", 9, DpElementType.BIT));
config.addChild(new DpTypeElement("interval", 10, DpElementType.INT));
config.addChild(new DpTypeElement("threshold", 11, DpElementType.FLOAT));
root.addChild(config);

int result = oa.dpTypeCreate(root);
```

---

### dpTypeChange

Modifies an existing datapoint type by adding new elements.

```java
public int dpTypeChange(int typeId, DpTypeElement definition, boolean append)
```

**Parameters:**
- `typeId` - The type ID (get from `dpTypeNameToId`)
- `definition` - The element to add or modify
- `append` - If `true`, append element to root; if `false`, replace existing

**Returns:** `0` on success, non-zero error code on failure

**Important Notes:**
- Existing datapoints of this type will be updated to include new elements
- Cannot remove elements from an existing type
- Cannot change element types of existing elements

**Example:**

```java
// Get the type ID
int typeId = oa.dpTypeNameToId("SensorType");

if (typeId > 0) {
    // Add a new element to the root
    DpTypeElement newElement = new DpTypeElement("location", 0, DpElementType.TEXT);

    int result = oa.dpTypeChange(typeId, newElement, true);
    if (result == 0) {
        WinCCOA.log("Type modified successfully");
    }
}
```

---

### dpTypeDelete

Deletes a datapoint type.

```java
public int dpTypeDelete(String typeName)
public int dpTypeDelete(int typeId)
```

**Parameters:**
- `typeName` - The type name to delete
- `typeId` - The type ID to delete

**Returns:** `0` on success, non-zero error code on failure

**Important Notes:**
- Type cannot be deleted if datapoints of this type exist
- Delete all datapoints using the type before deleting the type

**Example:**

```java
// Delete by name
int result = oa.dpTypeDelete("SensorType");
if (result == 0) {
    WinCCOA.log("Type deleted");
} else {
    WinCCOA.logError("Cannot delete type (datapoints may still exist)");
}

// Or delete by ID
int typeId = oa.dpTypeNameToId("SensorType");
if (typeId > 0) {
    result = oa.dpTypeDelete(typeId);
}
```

---

### dpTypeNameToId

Gets the type ID for a given type name.

```java
public int dpTypeNameToId(String typeName)
```

**Parameters:**
- `typeName` - The type name

**Returns:** The type ID (positive integer), or negative value if type not found

**Example:**

```java
int typeId = oa.dpTypeNameToId("SensorType");
if (typeId > 0) {
    WinCCOA.log("Type ID: " + typeId);
} else {
    WinCCOA.logError("Type not found");
}
```

---

### dpTypeGet

Retrieves the complete type definition as a tree structure.

```java
public DpTypeElement dpTypeGet(String typeName)
```

**Parameters:**
- `typeName` - The type name

**Returns:** The root `DpTypeElement` of the type, or `null` if not found

**Example:**

```java
DpTypeElement typeDef = oa.dpTypeGet("SensorType");
if (typeDef != null) {
    WinCCOA.log("Type structure for " + typeDef.getName() + ":");

    // Print type structure
    WinCCOA.log(typeDef.toString());

    // Get all element paths
    for (String path : typeDef.getElementPaths()) {
        WinCCOA.log("  " + path);
    }

    // Access specific elements
    DpTypeElement tempElement = typeDef.findElement("temperature");
    if (tempElement != null) {
        WinCCOA.log("Temperature element type: " + tempElement.getElementType());
    }
}
```

---

## Datapoint Management

Datapoint management operations allow you to create and manage instances of datapoint types.

### dpCreate

Creates a new datapoint of a given type.

```java
public int dpCreate(String dpName, String dpTypeName)
```

**Parameters:**
- `dpName` - The datapoint name (without system prefix)
- `dpTypeName` - The datapoint type name

**Returns:** `0` on success, non-zero error code on failure

**Important Notes:**
- The type must exist before creating datapoints
- Datapoint names must be unique within the system
- Creation is asynchronous; allow time before accessing the new datapoint
- Do not include system prefix in dpName (e.g., use "Device1" not "System1:Device1")

**Example:**

```java
// Create multiple datapoints of type "SensorType"
for (int i = 1; i <= 5; i++) {
    String dpName = "Sensor_" + i;
    int result = oa.dpCreate(dpName, "SensorType");

    if (result == 0) {
        WinCCOA.log("Created: " + dpName);
    } else {
        WinCCOA.logError("Failed to create " + dpName + ": " + result);
    }
}

// Wait for datapoints to be registered
Thread.sleep(2000);

// Now use the datapoints
oa.dpSet("Sensor_1.temperature", 25.5);
oa.dpSet("Sensor_1.humidity", 60.0);
oa.dpSet("Sensor_1.name", "Room Temperature");
oa.dpSet("Sensor_1.active", true);
```

---

### dpDelete

Deletes a datapoint.

```java
public int dpDelete(String dpName)
```

**Parameters:**
- `dpName` - The datapoint name to delete

**Returns:** `0` on success, non-zero error code on failure

**Important Notes:**
- The datapoint must exist
- Active connections (hotlinks) to the datapoint should be disconnected first
- Deletion is asynchronous

**Example:**

```java
// Check if datapoint exists
if (oa.dpExists("Sensor_1")) {
    int result = oa.dpDelete("Sensor_1");
    if (result == 0) {
        WinCCOA.log("Datapoint deleted");
    } else {
        WinCCOA.logError("Failed to delete datapoint: " + result);
    }
}
```

---

### dpExists

Checks if a datapoint exists.

```java
public boolean dpExists(String dpName)
```

**Parameters:**
- `dpName` - The datapoint name to check

**Returns:** `true` if the datapoint exists, `false` otherwise

**Example:**

```java
if (oa.dpExists("Sensor_1")) {
    WinCCOA.log("Datapoint exists");

    // Safe to read/write
    Variable temp = oa.dpGet("Sensor_1.temperature");
    WinCCOA.log("Temperature: " + temp);
} else {
    WinCCOA.log("Datapoint does not exist");

    // Create it
    oa.dpCreate("Sensor_1", "SensorType");
}
```

---

## Data Classes

### DpTypeElement

`at.rocworks.oa4j.var.DpTypeElement`

Represents an element in a datapoint type definition tree. Each element can have child elements, forming a hierarchical structure.

**Properties:**

| Property | Type | Description |
|----------|------|-------------|
| `name` | String | Element name |
| `elementId` | int | Element ID within the type (must be unique) |
| `elementType` | DpElementType | Type of the element (RECORD, FLOAT, INT, etc.) |
| `referencedTypeId` | int | Referenced type ID for type references (0 if not a reference) |
| `children` | List<DpTypeElement> | Child elements (for RECORD types) |

**Constructors:**

```java
// Standard constructor
DpTypeElement(String name, int elementId, DpElementType elementType)

// Constructor for type references
DpTypeElement(String name, int elementId, DpElementType elementType, int referencedTypeId)
```

**Methods:**

| Method | Description |
|--------|-------------|
| `addChild(DpTypeElement child)` | Add a child element |
| `getChildren()` | Get list of child elements |
| `getChildCount()` | Get number of children |
| `hasChildren()` | Check if element has children |
| `isLeaf()` | Check if element is a leaf (no children, primitive type) |
| `isTypeReference()` | Check if element is a type reference |
| `findElement(String path)` | Find element by path (e.g., "sensors.temperature") |
| `getElementPaths()` | Get list of all element paths in the tree |
| `getName()` | Get element name |
| `getElementId()` | Get element ID |
| `getElementType()` | Get element type |

**Example:**

```java
// Create a type element
DpTypeElement sensors = new DpTypeElement("sensors", 4, DpElementType.RECORD);
sensors.addChild(new DpTypeElement("temp", 5, DpElementType.FLOAT));
sensors.addChild(new DpTypeElement("press", 6, DpElementType.FLOAT));

// Query the structure
System.out.println("Has children: " + sensors.hasChildren());
System.out.println("Child count: " + sensors.getChildCount());

// Find nested element
DpTypeElement temp = sensors.findElement("temp");
if (temp != null) {
    System.out.println("Found element: " + temp.getName());
}

// Get all paths
for (String path : sensors.getElementPaths()) {
    System.out.println("Path: " + path);
}
```

---

### DpElementType

`at.rocworks.oa4j.var.DpElementType`

Enum representing WinCC OA datapoint element types.

**Container Types:**

```java
DpElementType.NOELEMENT  // No element / invalid
DpElementType.RECORD     // Container for children of different types
DpElementType.ARRAY      // Container for children of same type
```

**Primitive Types:**

```java
DpElementType.CHAR       // Character
DpElementType.UINT       // Unsigned integer
DpElementType.INT        // Integer
DpElementType.FLOAT      // Float
DpElementType.BIT        // Boolean
DpElementType.BIT32      // 32-bit value
DpElementType.TEXT       // Text string
DpElementType.TIME       // Time value
DpElementType.DPID       // Datapoint identifier
DpElementType.BIT64      // 64-bit value
DpElementType.LONG       // Long integer
DpElementType.ULONG      // Unsigned long
DpElementType.BLOB       // Binary data
DpElementType.LANGTEXT   // Multi-language text
```

**Dynamic Array Types:**

```java
DpElementType.DYNCHAR    // Dynamic char array
DpElementType.DYNUINT    // Dynamic uint array
DpElementType.DYNINT     // Dynamic int array
DpElementType.DYNFLOAT   // Dynamic float array
DpElementType.DYNBIT     // Dynamic bit array
DpElementType.DYNTEXT    // Dynamic text array
DpElementType.DYNTIME    // Dynamic time array
// ... and more
```

**Helper Methods:**

```java
DpElementType.fromValue(int value)  // Get type from numeric value
elementType.isLeafType()            // Check if primitive/leaf type
elementType.isDynType()             // Check if dynamic array type
elementType.isArrayType()           // Check if static array type
elementType.isRecordType()          // Check if record/container
elementType.isReferenceType()       // Check if type reference
```

**Example:**

```java
DpElementType type = DpElementType.FLOAT;

System.out.println("Is leaf: " + type.isLeafType());        // true
System.out.println("Is record: " + type.isRecordType());    // false
System.out.println("Is dynamic: " + type.isDynType());      // false

DpElementType dynType = DpElementType.DYNFLOAT;
System.out.println("Is dynamic: " + dynType.isDynType());   // true
```

---

## Complete Examples

### Example 1: Create Type and Datapoints

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.var.*;

public class CreateTypeAndDatapoints {
    public static void main(String[] args) throws Exception {
        WinCCOA oa = WinCCOA.connect(args);

        try {
            // Step 1: Create a datapoint type
            WinCCOA.log("Creating datapoint type...");

            DpTypeElement root = new DpTypeElement("SensorType", 1, DpElementType.RECORD);
            root.addChild(new DpTypeElement("temperature", 2, DpElementType.FLOAT));
            root.addChild(new DpTypeElement("humidity", 3, DpElementType.FLOAT));
            root.addChild(new DpTypeElement("pressure", 4, DpElementType.FLOAT));
            root.addChild(new DpTypeElement("name", 5, DpElementType.TEXT));
            root.addChild(new DpTypeElement("active", 6, DpElementType.BIT));
            root.addChild(new DpTypeElement("lastUpdate", 7, DpElementType.TIME));

            int result = oa.dpTypeCreate(root);
            if (result == 0) {
                WinCCOA.log("Type created successfully");
            } else {
                WinCCOA.logError("Type creation failed: " + result);
                return;
            }

            // Step 2: Wait for type to be registered
            WinCCOA.log("Waiting for type registration...");
            Thread.sleep(5000);

            // Step 3: Verify type was created
            int typeId = oa.dpTypeNameToId("SensorType");
            if (typeId > 0) {
                WinCCOA.log("Type registered with ID: " + typeId);
            } else {
                WinCCOA.logError("Type not found!");
                return;
            }

            // Step 4: Create datapoints
            WinCCOA.log("Creating datapoints...");
            for (int i = 1; i <= 3; i++) {
                String dpName = "Sensor_" + i;
                result = oa.dpCreate(dpName, "SensorType");
                if (result == 0) {
                    WinCCOA.log("Created: " + dpName);
                } else {
                    WinCCOA.logError("Failed to create " + dpName + ": " + result);
                }
            }

            // Step 5: Wait for datapoints to be created
            Thread.sleep(2000);

            // Step 6: Verify and initialize datapoints
            for (int i = 1; i <= 3; i++) {
                String dpName = "Sensor_" + i;
                if (oa.dpExists(dpName)) {
                    WinCCOA.log("Initializing " + dpName);

                    oa.dpSet()
                        .add(dpName + ".temperature", 20.0 + i)
                        .add(dpName + ".humidity", 50.0 + i * 5)
                        .add(dpName + ".pressure", 1013.25)
                        .add(dpName + ".name", "Sensor " + i)
                        .add(dpName + ".active", true)
                        .add(dpName + ".lastUpdate", new TimeVar(System.currentTimeMillis()))
                        .send();
                }
            }

            WinCCOA.log("Setup complete!");

        } finally {
            oa.disconnect();
        }
    }
}
```

---

### Example 2: Query Type Structure

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.var.*;

public class QueryTypeStructure {
    public static void main(String[] args) throws Exception {
        WinCCOA oa = WinCCOA.connect(args);

        try {
            // Get the type definition
            DpTypeElement typeDef = oa.dpTypeGet("SensorType");

            if (typeDef == null) {
                WinCCOA.logError("Type not found");
                return;
            }

            // Print type information
            System.out.println("=== Type: " + typeDef.getName() + " ===");
            System.out.println("Element ID: " + typeDef.getElementId());
            System.out.println("Element Type: " + typeDef.getElementType());
            System.out.println("Has Children: " + typeDef.hasChildren());
            System.out.println("Child Count: " + typeDef.getChildCount());

            // Print all element paths
            System.out.println("\n=== Element Paths ===");
            for (String path : typeDef.getElementPaths()) {
                System.out.println("  " + path);
            }

            // Print child details
            System.out.println("\n=== Child Elements ===");
            for (DpTypeElement child : typeDef.getChildren()) {
                System.out.println("Name: " + child.getName());
                System.out.println("  ID: " + child.getElementId());
                System.out.println("  Type: " + child.getElementType());
                System.out.println("  Is Leaf: " + child.isLeaf());
                System.out.println();
            }

            // Find specific element
            DpTypeElement tempElement = typeDef.findElement("temperature");
            if (tempElement != null) {
                System.out.println("=== Temperature Element ===");
                System.out.println("Name: " + tempElement.getName());
                System.out.println("Type: " + tempElement.getElementType());
            }

        } finally {
            oa.disconnect();
        }
    }
}
```

---

### Example 3: Modify Existing Type

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.var.*;

public class ModifyType {
    public static void main(String[] args) throws Exception {
        WinCCOA oa = WinCCOA.connect(args);

        try {
            // Get the type ID
            int typeId = oa.dpTypeNameToId("SensorType");

            if (typeId <= 0) {
                WinCCOA.logError("Type not found");
                return;
            }

            WinCCOA.log("Modifying type with ID: " + typeId);

            // Add new elements to the type
            DpTypeElement location = new DpTypeElement("location", 0, DpElementType.TEXT);
            int result = oa.dpTypeChange(typeId, location, true);

            if (result == 0) {
                WinCCOA.log("Added 'location' element");
            }

            DpTypeElement altitude = new DpTypeElement("altitude", 0, DpElementType.FLOAT);
            result = oa.dpTypeChange(typeId, altitude, true);

            if (result == 0) {
                WinCCOA.log("Added 'altitude' element");
            }

            // Wait for changes to propagate
            Thread.sleep(2000);

            // Verify changes
            DpTypeElement typeDef = oa.dpTypeGet("SensorType");
            if (typeDef != null) {
                System.out.println("\n=== Updated Type Structure ===");
                for (String path : typeDef.getElementPaths()) {
                    System.out.println("  " + path);
                }
            }

            // Existing datapoints now have these new elements
            if (oa.dpExists("Sensor_1")) {
                oa.dpSet()
                    .add("Sensor_1.location", "Building A, Floor 2")
                    .add("Sensor_1.altitude", 123.5)
                    .send();

                WinCCOA.log("Updated Sensor_1 with new fields");
            }

        } finally {
            oa.disconnect();
        }
    }
}
```

---

### Example 4: Complex Nested Type

```java
import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.var.*;

public class CreateComplexType {
    public static void main(String[] args) throws Exception {
        WinCCOA oa = WinCCOA.connect(args);

        try {
            // Create a complex nested type
            // DeviceType
            //   ├── info (RECORD)
            //   │     ├── id (INT)
            //   │     ├── name (TEXT)
            //   │     └── location (TEXT)
            //   ├── sensors (RECORD)
            //   │     ├── temperature (FLOAT)
            //   │     ├── pressure (FLOAT)
            //   │     └── humidity (FLOAT)
            //   ├── alarms (RECORD)
            //   │     ├── highTemp (FLOAT)
            //   │     ├── lowTemp (FLOAT)
            //   │     └── alertEmail (TEXT)
            //   └── status (RECORD)
            //         ├── active (BIT)
            //         ├── lastUpdate (TIME)
            //         └── errorCount (INT)

            DpTypeElement root = new DpTypeElement("DeviceType", 1, DpElementType.RECORD);

            // Info section
            DpTypeElement info = new DpTypeElement("info", 2, DpElementType.RECORD);
            info.addChild(new DpTypeElement("id", 3, DpElementType.INT));
            info.addChild(new DpTypeElement("name", 4, DpElementType.TEXT));
            info.addChild(new DpTypeElement("location", 5, DpElementType.TEXT));
            root.addChild(info);

            // Sensors section
            DpTypeElement sensors = new DpTypeElement("sensors", 6, DpElementType.RECORD);
            sensors.addChild(new DpTypeElement("temperature", 7, DpElementType.FLOAT));
            sensors.addChild(new DpTypeElement("pressure", 8, DpElementType.FLOAT));
            sensors.addChild(new DpTypeElement("humidity", 9, DpElementType.FLOAT));
            root.addChild(sensors);

            // Alarms section
            DpTypeElement alarms = new DpTypeElement("alarms", 10, DpElementType.RECORD);
            alarms.addChild(new DpTypeElement("highTemp", 11, DpElementType.FLOAT));
            alarms.addChild(new DpTypeElement("lowTemp", 12, DpElementType.FLOAT));
            alarms.addChild(new DpTypeElement("alertEmail", 13, DpElementType.TEXT));
            root.addChild(alarms);

            // Status section
            DpTypeElement status = new DpTypeElement("status", 14, DpElementType.RECORD);
            status.addChild(new DpTypeElement("active", 15, DpElementType.BIT));
            status.addChild(new DpTypeElement("lastUpdate", 16, DpElementType.TIME));
            status.addChild(new DpTypeElement("errorCount", 17, DpElementType.INT));
            root.addChild(status);

            // Create the type
            int result = oa.dpTypeCreate(root);
            if (result == 0) {
                WinCCOA.log("Complex type created");
                Thread.sleep(5000);

                // Create a device
                result = oa.dpCreate("Device_1", "DeviceType");
                if (result == 0) {
                    WinCCOA.log("Device created");
                    Thread.sleep(2000);

                    // Initialize all fields
                    oa.dpSet()
                        .add("Device_1.info.id", 1)
                        .add("Device_1.info.name", "Temperature Controller")
                        .add("Device_1.info.location", "Building A")
                        .add("Device_1.sensors.temperature", 22.5)
                        .add("Device_1.sensors.pressure", 1013.25)
                        .add("Device_1.sensors.humidity", 45.0)
                        .add("Device_1.alarms.highTemp", 30.0)
                        .add("Device_1.alarms.lowTemp", 15.0)
                        .add("Device_1.alarms.alertEmail", "admin@example.com")
                        .add("Device_1.status.active", true)
                        .add("Device_1.status.lastUpdate", new TimeVar(System.currentTimeMillis()))
                        .add("Device_1.status.errorCount", 0)
                        .send();

                    WinCCOA.log("Device initialized");
                }
            }

        } finally {
            oa.disconnect();
        }
    }
}
```

---

### Example 5: Cleanup - Delete Datapoints and Type

```java
import at.rocworks.oa4j.WinCCOA;

public class CleanupDatapoints {
    public static void main(String[] args) throws Exception {
        WinCCOA oa = WinCCOA.connect(args);

        try {
            String typeName = "SensorType";

            // Step 1: Find all datapoints of this type
            String[] allDps = oa.dpNames("*");
            int deletedCount = 0;

            WinCCOA.log("Searching for datapoints of type: " + typeName);

            for (String dp : allDps) {
                // Check if this is a datapoint of the target type
                // You can use dpQuery or check manually
                if (dp.startsWith("Sensor_")) {  // Simple check
                    int result = oa.dpDelete(dp);
                    if (result == 0) {
                        WinCCOA.log("Deleted: " + dp);
                        deletedCount++;
                    } else {
                        WinCCOA.logError("Failed to delete " + dp + ": " + result);
                    }
                }
            }

            WinCCOA.log("Deleted " + deletedCount + " datapoints");

            // Step 2: Wait for deletions to complete
            Thread.sleep(2000);

            // Step 3: Delete the type
            int result = oa.dpTypeDelete(typeName);
            if (result == 0) {
                WinCCOA.log("Type deleted: " + typeName);
            } else {
                WinCCOA.logError("Failed to delete type: " + result);
                WinCCOA.logError("There may still be datapoints using this type");
            }

        } finally {
            oa.disconnect();
        }
    }
}
```

---

## Best Practices

### 1. Type Design

**Plan Your Structure:**

```java
// Good - well-organized structure
DpTypeElement root = new DpTypeElement("DeviceType", 1, DpElementType.RECORD);

DpTypeElement config = new DpTypeElement("config", 2, DpElementType.RECORD);
config.addChild(new DpTypeElement("enabled", 3, DpElementType.BIT));
config.addChild(new DpTypeElement("interval", 4, DpElementType.INT));
root.addChild(config);

DpTypeElement data = new DpTypeElement("data", 5, DpElementType.RECORD);
data.addChild(new DpTypeElement("value", 6, DpElementType.FLOAT));
data.addChild(new DpTypeElement("timestamp", 7, DpElementType.TIME));
root.addChild(data);
```

**Use Descriptive Names:**

```java
// Good
new DpTypeElement("temperature", 2, DpElementType.FLOAT)
new DpTypeElement("pressureValue", 3, DpElementType.FLOAT)

// Less clear
new DpTypeElement("t", 2, DpElementType.FLOAT)
new DpTypeElement("pv", 3, DpElementType.FLOAT)
```

---

### 2. Element IDs

**Use Sequential Numbering:**

```java
// Good - sequential, easy to maintain
DpTypeElement root = new DpTypeElement("Type", 1, DpElementType.RECORD);
root.addChild(new DpTypeElement("field1", 2, DpElementType.FLOAT));
root.addChild(new DpTypeElement("field2", 3, DpElementType.INT));
root.addChild(new DpTypeElement("field3", 4, DpElementType.TEXT));

// Avoid - non-sequential, harder to maintain
DpTypeElement root = new DpTypeElement("Type", 1, DpElementType.RECORD);
root.addChild(new DpTypeElement("field1", 10, DpElementType.FLOAT));
root.addChild(new DpTypeElement("field2", 25, DpElementType.INT));
root.addChild(new DpTypeElement("field3", 7, DpElementType.TEXT));
```

---

### 3. Error Handling

**Always Check Return Codes:**

```java
int result = oa.dpTypeCreate(root);
if (result != 0) {
    WinCCOA.logError("Type creation failed with error: " + result);
    return;  // Don't continue if type creation failed
}
```

**Check Existence Before Operations:**

```java
// Before creating
int typeId = oa.dpTypeNameToId("SensorType");
if (typeId > 0) {
    WinCCOA.log("Type already exists");
    return;
}

// Before deleting
if (!oa.dpExists("Sensor_1")) {
    WinCCOA.log("Datapoint doesn't exist, nothing to delete");
    return;
}
```

---

### 4. Wait for Asynchronous Operations

**Allow Time for Registration:**

```java
// After creating type
oa.dpTypeCreate(root);
Thread.sleep(5000);  // Wait for type registration

// After creating datapoints
oa.dpCreate("Device1", "DeviceType");
Thread.sleep(2000);  // Wait for datapoint creation

// Before using
if (oa.dpExists("Device1")) {
    // Now safe to use
    oa.dpSet("Device1.value", 100);
}
```

---

### 5. Cleanup Order

**Delete Datapoints Before Types:**

```java
// Correct order
// 1. Delete all datapoints
for (String dp : datapoints) {
    oa.dpDelete(dp);
}
Thread.sleep(2000);

// 2. Delete the type
oa.dpTypeDelete("MyType");

// Wrong order - will fail
oa.dpTypeDelete("MyType");  // Fails if datapoints exist
```

---

## Return Values

| Return Value | Meaning |
|--------------|---------|
| `0` | Success |
| Positive | Success (for ID queries) |
| Negative | Error code or not found |

---

## Summary

The Datapoint Type and Datapoint Management API allows you to:

- **Create Types** with `dpTypeCreate()` - Define structure for datapoints
- **Modify Types** with `dpTypeChange()` - Add elements to existing types
- **Query Types** with `dpTypeGet()` and `dpTypeNameToId()` - Inspect type definitions
- **Delete Types** with `dpTypeDelete()` - Remove type definitions
- **Create Datapoints** with `dpCreate()` - Instantiate datapoints from types
- **Delete Datapoints** with `dpDelete()` - Remove datapoint instances
- **Check Existence** with `dpExists()` - Verify datapoints exist

All operations use the `WinCCOA` class instance obtained from `WinCCOA.connect()`.

**Key Points:**
- Type operations are asynchronous - wait for registration
- Element IDs must be unique within a type
- Root element must be RECORD type
- Datapoints must be deleted before types can be deleted
- Always check return codes and existence before operations

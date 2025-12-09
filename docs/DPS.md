# Datapoint Type and Datapoint Management API

This document describes the Java API for managing datapoint types and datapoints in WinCC OA.

## Overview

The API provides functions for:

- **Datapoint Type Management**: Create, modify, and delete datapoint type definitions
- **Datapoint Management**: Create, delete, and check existence of datapoints

All functions are thread-safe and accessible via the static `JClient` class.

## Java Classes

### DpTypeElement

Represents an element in a datapoint type definition tree structure. Each element can have child elements, forming a hierarchical structure that describes the complete datapoint type.

| Property | Type | Description |
|----------|------|-------------|
| `name` | `String` | Element name |
| `elementId` | `int` | Element ID within the type (must be unique) |
| `elementType` | `DpElementType` | Type of the element (RECORD, FLOAT, INT, etc.) |
| `referencedTypeId` | `int` | Referenced type ID for type references (0 if not a reference) |
| `children` | `List<DpTypeElement>` | Child elements |

**Constructors:**

```java
// Full constructor
DpTypeElement(String name, int elementId, DpElementType elementType, int referencedTypeId)

// Without type reference
DpTypeElement(String name, int elementId, DpElementType elementType)
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
| `findElement(String path)` | Find element by path (e.g., "config.enabled") |
| `getElementPaths()` | Get list of all element paths |

### DpElementType

Enum representing the datapoint element types. Values match the C++ `DpElementType` enum.

**Container Types:**

| Constant | Value | Description |
|----------|-------|-------------|
| `NOELEMENT` | 0 | No element / invalid |
| `RECORD` | 1 | Container for children of different types |
| `ARRAY` | 2 | Container for children of same type |

**Primitive Types:**

| Constant | Value | Description |
|----------|-------|-------------|
| `CHAR` | 19 | Character |
| `UINT` | 20 | Unsigned integer |
| `INT` | 21 | Integer |
| `FLOAT` | 22 | Float |
| `BIT` | 23 | Boolean |
| `BIT32` | 24 | 32-bit value |
| `TEXT` | 25 | Text string |
| `TIME` | 26 | Time value |
| `DPID` | 27 | Datapoint identifier |
| `BIT64` | 50 | 64-bit value |
| `LONG` | 54 | Long integer |
| `ULONG` | 58 | Unsigned long |
| `BLOB` | 46 | Binary data |
| `LANGTEXT` | 42 | Multi-language text |

**Dynamic Array Types:**

| Constant | Value | Description |
|----------|-------|-------------|
| `DYNCHAR` | 3 | Dynamic char array |
| `DYNUINT` | 4 | Dynamic uint array |
| `DYNINT` | 5 | Dynamic int array |
| `DYNFLOAT` | 6 | Dynamic float array |
| `DYNBIT` | 7 | Dynamic bit array |
| `DYNTEXT` | 9 | Dynamic text array |
| `DYNTIME` | 10 | Dynamic time array |

**Helper Methods:**

| Method | Description |
|--------|-------------|
| `fromValue(int value)` | Get DpElementType from numeric value |
| `isLeafType()` | Check if this is a primitive/leaf type |
| `isDynType()` | Check if this is a dynamic array type |
| `isArrayType()` | Check if this is a static array type |
| `isRecordType()` | Check if this is a record/container |
| `isReferenceType()` | Check if this is a type reference |

## Datapoint Type API

### dpTypeCreate

Creates a new datapoint type.

```java
public static int dpTypeCreate(DpTypeElement definition)
public static int dpTypeCreate(DpTypeElement definition, String system)
```

**Parameters:**
- `definition` - The root element of the type definition tree
- `system` - Optional system name (null for default system)

**Returns:** `0` on success, non-zero error code on failure.

**Notes:**
- The root element must be of type `RECORD`
- Element IDs must be unique within the type
- Type creation is asynchronous; wait for the type to be registered before using it

**Example:**

```java
// Create type structure:
// MyType (RECORD)
//   ├── value (FLOAT)
//   ├── status (INT)
//   └── config (RECORD)
//         ├── enabled (BIT)
//         └── threshold (FLOAT)

DpTypeElement root = new DpTypeElement("MyType", 1, DpElementType.RECORD);

root.addChild(new DpTypeElement("value", 2, DpElementType.FLOAT));
root.addChild(new DpTypeElement("status", 3, DpElementType.INT));

DpTypeElement config = new DpTypeElement("config", 4, DpElementType.RECORD);
config.addChild(new DpTypeElement("enabled", 5, DpElementType.BIT));
config.addChild(new DpTypeElement("threshold", 6, DpElementType.FLOAT));
root.addChild(config);

int result = JClient.dpTypeCreate(root);
if (result == 0) {
    System.out.println("Type created successfully");
}
```

---

### dpTypeChange

Modifies an existing datapoint type by adding or modifying elements.

```java
public static int dpTypeChange(int typeId, DpTypeElement definition)
public static int dpTypeChange(int typeId, DpTypeElement definition, boolean append)
```

**Parameters:**
- `typeId` - The type ID (from `dpTypeNameToId`)
- `definition` - The element to add/modify
- `append` - If `true`, append element to root; if `false`, replace existing

**Returns:** `0` on success, non-zero error code on failure.

**Notes:**
- Existing datapoints of this type will be updated to include new elements
- Cannot remove elements from an existing type
- Cannot change element types

**Example:**

```java
// Get the type ID
int typeId = JClient.dpTypeNameToId("MyType");

// Add a new element to the root
DpTypeElement newElement = new DpTypeElement("description", 0, DpElementType.TEXT);

int result = JClient.dpTypeChange(typeId, newElement, true);
if (result == 0) {
    System.out.println("Type modified successfully");
}
```

---

### dpTypeDelete

Deletes a datapoint type.

```java
public static int dpTypeDelete(int typeId)
public static int dpTypeDelete(String typeName)
```

**Parameters:**
- `typeId` - The type ID to delete
- `typeName` - The type name to delete

**Returns:** `0` on success, non-zero error code on failure.

**Notes:**
- Type cannot be deleted if datapoints of this type exist
- Delete all datapoints using the type before deleting the type

**Example:**

```java
// Delete by name
int result = JClient.dpTypeDelete("MyType");

// Or delete by ID
int typeId = JClient.dpTypeNameToId("MyType");
result = JClient.dpTypeDelete(typeId);
```

---

### dpTypeNameToId

Gets the type ID for a given type name.

```java
public static int dpTypeNameToId(String typeName)
```

**Parameters:**
- `typeName` - The type name

**Returns:** The type ID, or negative value if type not found.

**Example:**

```java
int typeId = JClient.dpTypeNameToId("ExampleDP_Float");
if (typeId > 0) {
    System.out.println("Type ID: " + typeId);
}
```

---

### dpTypeGetTree (JManager)

Retrieves the complete type definition as a tree structure.

```java
// Via JManager instance
DpTypeElement typeDef = JManager.getInstance().dpTypeGetTree("MyType");
```

**Parameters:**
- `typeName` - The type name

**Returns:** The root `DpTypeElement` of the type, or `null` if not found.

**Example:**

```java
DpTypeElement typeDef = JManager.getInstance().dpTypeGetTree("MyType");
if (typeDef != null) {
    System.out.println("Type structure:");
    System.out.println(typeDef.toString());

    // Get all element paths
    for (String path : typeDef.getElementPaths()) {
        System.out.println("  " + path);
    }
}
```

---

## Datapoint API

### dpCreate

Creates a new datapoint of a given type.

```java
public static int dpCreate(String dpName, String dpTypeName)
public static int dpCreate(String dpName, String dpTypeName, String system)
```

**Parameters:**
- `dpName` - The datapoint name (without system prefix)
- `dpTypeName` - The datapoint type name
- `system` - Optional system name (null for default system)

**Returns:** `0` on success, non-zero error code on failure.

**Notes:**
- The type must exist before creating datapoints
- Datapoint names must be unique within the system
- Creation is asynchronous; wait before accessing the new datapoint

**Example:**

```java
// Create datapoints of type "MyType"
for (int i = 1; i <= 5; i++) {
    int result = JClient.dpCreate("Device_" + i, "MyType");
    if (result == 0) {
        System.out.println("Created: Device_" + i);
    }
}

// Wait for datapoints to be registered
Thread.sleep(2000);

// Now use the datapoints
JClient.dpSet("Device_1.value", 123.45);
```

---

### dpDelete

Deletes a datapoint.

```java
public static int dpDelete(String dpName)
```

**Parameters:**
- `dpName` - The datapoint name to delete

**Returns:** `0` on success, non-zero error code on failure.

**Notes:**
- The datapoint must exist
- Active connections (hotlinks) to the datapoint should be disconnected first

**Example:**

```java
int result = JClient.dpDelete("Device_1");
if (result == 0) {
    System.out.println("Datapoint deleted");
}
```

---

### dpExists

Checks if a datapoint exists.

```java
public static boolean dpExists(String dpName)
```

**Parameters:**
- `dpName` - The datapoint name to check

**Returns:** `true` if the datapoint exists, `false` otherwise.

**Example:**

```java
if (JClient.dpExists("Device_1")) {
    System.out.println("Datapoint exists");
} else {
    System.out.println("Datapoint does not exist");
}
```

---

## Complete Example

```java
import at.rocworks.oa4j.base.JClient;
import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.var.DpElementType;
import at.rocworks.oa4j.var.DpTypeElement;

public class DpTypeExample {
    public static void main(String[] args) throws Exception {
        JManager m = new JManager();
        m.init(args).start();

        // Create a new datapoint type
        DpTypeElement root = new DpTypeElement("SensorType", 1, DpElementType.RECORD);
        root.addChild(new DpTypeElement("temperature", 2, DpElementType.FLOAT));
        root.addChild(new DpTypeElement("humidity", 3, DpElementType.FLOAT));
        root.addChild(new DpTypeElement("name", 4, DpElementType.TEXT));
        root.addChild(new DpTypeElement("active", 5, DpElementType.BIT));

        int result = JClient.dpTypeCreate(root);
        System.out.println("Type creation result: " + result);

        // Wait for type to be registered
        Thread.sleep(5000);

        // Create datapoints
        for (int i = 1; i <= 3; i++) {
            result = JClient.dpCreate("Sensor_" + i, "SensorType");
            System.out.println("Created Sensor_" + i + ": " + result);
        }

        // Wait for datapoints to be created
        Thread.sleep(2000);

        // Check and use datapoints
        for (int i = 1; i <= 3; i++) {
            String dpName = "Sensor_" + i;
            if (JClient.dpExists(dpName)) {
                JClient.dpSet()
                    .add(dpName + ".temperature", 20.0 + i)
                    .add(dpName + ".humidity", 50.0 + i * 5)
                    .add(dpName + ".name", "Sensor " + i)
                    .add(dpName + ".active", true)
                    .send();
            }
        }

        // Read type definition
        DpTypeElement typeDef = JManager.getInstance().dpTypeGetTree("SensorType");
        if (typeDef != null) {
            System.out.println("\nType structure:");
            System.out.println(typeDef.toString());
        }

        // Cleanup (optional)
        // for (int i = 1; i <= 3; i++) {
        //     JClient.dpDelete("Sensor_" + i);
        // }
        // Thread.sleep(2000);
        // JClient.dpTypeDelete("SensorType");

        m.stop();
    }
}
```

## Return Values

| Return | Meaning |
|--------|---------|
| `0` | Success |
| Non-zero | Error code |
| Negative type ID | Type not found |

## Notes

- All operations are executed via the WinCC OA manager dispatch loop using `executeTask()`
- Type and datapoint creation is asynchronous; wait before using newly created items
- Element IDs in type definitions must be unique and non-zero (except when using 0 for auto-assignment)
- The root element of a type must always be `RECORD`
- Nested structures (sub-records) also use `RECORD` type

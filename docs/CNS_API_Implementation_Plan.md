# CNS API Implementation Plan for OA4J

## Overview

This document outlines the implementation plan for the Common Name Service (CNS) API in the OA4J Java library. The CNS provides a hierarchical namespace for organizing datapoints into views, trees, and nodes.

This plan is based on the **WinCC OA 3.20 C++ API** available in the `CommonNameService`, `CNSNode`, `CNSUserData`, `CNSObserver`, and related classes.

**Total Functions to Implement:** ~35 core functions

## C++ API Classes Used

| C++ Class | Purpose |
|-----------|---------|
| `CommonNameService` | Main CNS operations (CRUD for views/trees/nodes, queries) |
| `CNSNode` | Represents a node in the CNS hierarchy |
| `CNSNodeNames` | Node name and display names container |
| `CNSDataIdentifier` | Links nodes to datapoints with type info |
| `CNSUserData` | Property management for nodes |
| `CNSObserver` | Observer pattern for CNS changes |
| `CNSNodeTree` | Tree structure for batch operations |
| `CNSViewBrowser` | High-level browsing interface |

---

## Implementation Strategy

### Phase 1: Core API - View, Tree, Node Operations (~25 Functions)

**Goal:** Enable complete CRUD operations for CNS views, trees, and nodes

### Phase 2: Properties, Observers & Utilities (~10 Functions)

**Goal:** Add property management, change notifications, and validation

---

## Phase 1: Core API Implementation

### 1.1 View Management (7 functions)

| C++ Method | Java Method | Description | Priority |
|------------|-------------|-------------|----------|
| `CommonNameService::createView()` | `cnsCreateView` | Create a new view | P1 |
| `CommonNameService::deleteView()` | `cnsDeleteView` | Delete a view | P1 |
| `CommonNameService::getViews()` | `cnsGetViews` | Get all views in a system | P1 |
| `CommonNameService::getViewNames()` | `cnsGetViewDisplayNames` | Get display names of a view | P2 |
| `CommonNameService::changeView(viewNames)` | `cnsChangeViewDisplayNames` | Change display names of a view | P2 |
| `CommonNameService::getSeparators()` | `cnsGetViewSeparators` | Get separators of a view | P3 |
| `CommonNameService::changeView(separator)` | `cnsChangeViewSeparators` | Change separators of a view | P3 |

**C++ Signatures:**
```cpp
// CommonNameService
virtual PVSSboolean createView(const CNSNodeNames &viewNames, const LangText &separators, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
virtual PVSSboolean createView(const SystemNumType &sys, const CNSNodeNames &viewNames, const LangText &separators, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
virtual PVSSboolean deleteView(const ViewId &view, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
virtual PVSSboolean deleteView(const SystemNumType &sys, const ViewId &view, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
virtual PVSSboolean getViews(const SystemNumType &sys, ViewIdVector &views) const;
virtual PVSSboolean getViewNames(const SystemNumType &sys, const ViewId &view, CNSNodeNames &names);
virtual PVSSboolean changeView(const SystemNumType &sys, const ViewId &view, const CNSNodeNames &viewNames, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
virtual PVSSboolean getSeparators(const SystemNumType &sys, const ViewId &view, LangText &separators) const;
virtual PVSSboolean changeView(const SystemNumType &sys, const ViewId &view, const LangText &separator, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
```

**Java API Design:**
```java
// JClient static methods
public static void cnsCreateView(String system, String viewId, String separator) throws JException
public static void cnsCreateView(String system, String viewId, String separator, LangTextVar displayNames) throws JException
public static void cnsDeleteView(String system, String viewId) throws JException
public static String[] cnsGetViews(String system) throws JException
public static LangTextVar cnsGetViewDisplayNames(String system, String viewId) throws JException
public static void cnsChangeViewDisplayNames(String system, String viewId, LangTextVar displayNames) throws JException
public static String cnsGetViewSeparators(String system, String viewId) throws JException
public static void cnsChangeViewSeparators(String system, String viewId, String separator) throws JException
```

### 1.2 Tree Management (5 functions)

| C++ Method | Java Method | Description | Priority |
|------------|-------------|-------------|----------|
| `CommonNameService::addTree()` | `cnsAddTree` | Add a tree to a view | P1 |
| `CommonNameService::deleteTree()` | `cnsDeleteTree` | Delete a tree or node | P1 |
| `CommonNameService::getTrees()` | `cnsGetTrees` | Get all trees in a view | P1 |
| `CommonNameService::getRoot()` | `cnsGetRoot` | Get root node of a tree | P1 |
| `CommonNameService::changeTree()` | `cnsChangeTree` | Replace an existing tree | P3 |

**C++ Signatures:**
```cpp
// CommonNameService - Multiple overloads for addTree
virtual PVSSboolean addTree(const ViewId &view, const CNSNodeTree &tree, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
virtual PVSSboolean addTree(const SystemNumType &sys, const ViewId &view, const CNSNodeTree &tree, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
virtual PVSSboolean addTree(const CNSNode &parent, const CNSNodeTree &tree, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
virtual PVSSboolean deleteTree(const CNSNode &node, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
virtual PVSSboolean getTrees(const SystemNumType &sys, const ViewId &view, CNSNodeVector &nodes) const;
virtual PVSSboolean getRoot(const CNSNode &node, CNSNode &root) const;
virtual PVSSboolean changeTree(const CNSNode &node, const CNSNodeTree &tree, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
```

**Java API Design:**
```java
public static void cnsAddTree(String viewPath, String nodeId, String nodeTypeId, DpIdentifierVar dpId, LangTextVar displayNames) throws JException
public static void cnsDeleteTree(String cnsPath) throws JException
public static String[] cnsGetTrees(String system, String viewId) throws JException
public static String cnsGetRoot(String cnsPath) throws JException
public static void cnsChangeTree(String cnsPath, String nodeTypeId, DpIdentifierVar dpId, LangTextVar displayNames) throws JException
```

### 1.3 Node Management (5 functions)

| C++ Method | Java Method | Description | Priority |
|------------|-------------|-------------|----------|
| `CommonNameService::addNode()` | `cnsAddNode` | Add a node to a tree | P1 |
| `CommonNameService::getChildren()` | `cnsGetChildren` | Get child nodes | P1 |
| `CommonNameService::getParent()` | `cnsGetParent` | Get parent node | P1 |
| `CommonNameService::changeNode(id)` | `cnsChangeNodeData` | Change node's datapoint and type | P1 |
| `CommonNameService::changeNode(names)` | `cnsChangeNodeDisplayNames` | Change display names of a node | P2 |

**C++ Signatures:**
```cpp
// CommonNameService
virtual PVSSboolean addNode(const CNSNode &parent, const CNSNodeNames &names, const CNSDataIdentifier &id, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
virtual PVSSboolean getChildren(const CNSNode &node, CNSNodeVector &children) const;
virtual PVSSboolean getParent(const CNSNode &node, CNSNode &parent) const;
virtual PVSSboolean changeNode(const CNSNode &node, const CNSDataIdentifier &id, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
virtual PVSSboolean changeNode(const CNSNode &node, const CNSNodeNames &newNames, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
```

**Java API Design:**
```java
public static void cnsAddNode(String parentPath, String nodeId, String nodeTypeId, DpIdentifierVar dpId, LangTextVar displayNames) throws JException
public static String[] cnsGetChildren(String cnsPath) throws JException
public static String cnsGetParent(String cnsPath) throws JException
public static void cnsChangeNodeData(String cnsPath, DpIdentifierVar dpId, String nodeTypeId) throws JException
public static void cnsChangeNodeDisplayNames(String cnsPath, LangTextVar displayNames) throws JException
```

### 1.4 Query & Navigation (6 functions)

| C++ Method | Java Method | Description | Priority |
|------------|-------------|-------------|----------|
| `CommonNameService::getId()` | `cnsGetId` | Get linked datapoint and type | P1 |
| `CommonNameService::getIdSet()` | `cnsGetIdSet` | Get datapoints for matching nodes | P1 |
| `CommonNameService::getNodes(pattern)` | `cnsGetNodesByName` | Search nodes by pattern | P1 |
| `CommonNameService::getNodes(id)` | `cnsGetNodesByData` | Find nodes by datapoint | P1 |
| `CommonNameService::getNode()` | `cnsGetNode` | Get a node by path | P1 |
| `CommonNameService::subStr()` | `cnsSubStr` | Extract parts of a CNS path | P2 |

**C++ Signatures:**
```cpp
// CommonNameService
virtual PVSSboolean getId(const char *name, DpIdentifier &id) const;
virtual PVSSboolean getId(const char *name, CNSDataIdentifier &id) const;
virtual PVSSboolean getIdSet(const char *pattern, const SystemNumType &sys, const ViewId &view, SearchMode mode, LanguageIdType langIdx, CNSDataIdentifierType type, CNSDataIdentifierSet &idSet) const;
virtual PVSSboolean getNodes(const char *pattern, const SystemNumType &sys, const ViewId &view, SearchMode mode, LanguageIdType langIdx, CNSDataIdentifierType type, CNSNodeVector &nodes) const;
virtual PVSSboolean getNodes(const CNSDataIdentifier &id, const SystemNumType &sys, const ViewId &view, CNSNodeVector &nodes) const;
virtual PVSSboolean getNode(const char *name, CNSNode &node) const;
CharString subStr(const CharString &path, int mask, bool resolve) const;
```

**Java API Design:**
```java
public static CnsNodeData cnsGetId(String cnsPath) throws JException  // returns dpId + nodeTypeId
public static DpIdentifierVar[] cnsGetIdSet(String cnsPattern) throws JException
public static String[] cnsGetNodesByName(String system, String viewId, String namePattern, int searchMode) throws JException
public static String[] cnsGetNodesByData(String system, String viewId, DpIdentifierVar dpId) throws JException
public static CnsNode cnsGetNode(String cnsPath) throws JException
public static String cnsSubStr(String cnsPath, int mask, boolean resolve) throws JException
```

### 1.5 CNSNode Getters (6 functions)

These are accessed through the `CNSNode` object returned by `cnsGetNode()`:

| C++ Method | Java Method | Description | Priority |
|------------|-------------|-------------|----------|
| `CNSNode::getPath()` | `CnsNode.getPath()` | Get CNS path | P1 |
| `CNSNode::getName()` | `CnsNode.getName()` | Get node name | P1 |
| `CNSNode::getDisplayNames()` | `CnsNode.getDisplayNames()` | Get display names | P1 |
| `CNSNode::getDisplayPaths()` | `CnsNode.getDisplayPaths()` | Get display paths | P2 |
| `CNSNode::getDataIdentifier()` | `CnsNode.getDataIdentifier()` | Get linked data | P1 |
| `CNSNode::getUserData()` | `CnsNode.getUserData()` | Get user data blob | P3 |

**Java API Design:**
```java
public class CnsNode {
    public String getPath();
    public String getName();
    public LangTextVar getDisplayNames();
    public LangTextVar getDisplayPaths();
    public CnsDataIdentifier getDataIdentifier();
    public byte[] getUserData();
    public String getSystem();
    public String getView();
}
```

### 1.6 System Names (2 functions)

| C++ Method | Java Method | Description | Priority |
|------------|-------------|-------------|----------|
| `CommonNameService::getSystemNames()` | `cnsGetSystemNames` | Get system display names | P3 |
| `CommonNameService::setSystemNames()` | `cnsSetSystemNames` | Set system display names | P3 |

**C++ Signatures:**
```cpp
virtual PVSSboolean getSystemNames(const SystemNumType &sys, CNSNodeNames &names);
virtual PVSSboolean setSystemNames(const SystemNumType &sys, const LangText &displayNames, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
```

**Java API Design:**
```java
public static LangTextVar cnsGetSystemNames(String system) throws JException
public static void cnsSetSystemNames(String system, LangTextVar displayNames) throws JException
```

---

## Phase 2: Properties, Observers & Utilities

### 2.1 Properties Management (4 functions)

| C++ Method | Java Method | Description | Priority |
|------------|-------------|-------------|----------|
| `CNSUserData::setNodeProperty()` | `cnsSetProperty` | Set node property | P2 |
| `CNSUserData::getNodeProperty()` | `cnsGetProperty` | Get node property value | P2 |
| `CNSUserData::getNodeProperties()` | `cnsGetPropertyKeys` | Get all property keys | P2 |
| `CNSUserData::removeNodeProperty()` | `cnsRemoveProperty` | Remove a property | P2 |

**C++ Signatures:**
```cpp
// CNSUserData
virtual PVSSboolean setNodeProperty(const CNSNode &node, const char *key, const Variable &value, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
virtual CharString getNodeProperty(const CNSNode &node, const char *key) const;
virtual PVSSboolean getNodeProperties(const CNSNode &node, PropertyMap &props) const;
virtual PVSSboolean removeNodeProperty(const CNSNode &node, const char *key, WaitForAnswer *wait=0, PVSSboolean del=PVSS_TRUE);
```

**Java API Design:**
```java
public static void cnsSetProperty(String cnsPath, String key, Variable value) throws JException
public static Variable cnsGetProperty(String cnsPath, String key) throws JException
public static Map<String, Variable> cnsGetProperties(String cnsPath) throws JException
public static void cnsRemoveProperty(String cnsPath, String key) throws JException
```

### 2.2 Observers (2 functions)

| C++ Method | Java Method | Description | Priority |
|------------|-------------|-------------|----------|
| `CommonNameService::addObserver()` | `cnsAddObserver` | Register CNS observer | P3 |
| `CommonNameService::removeObserver()` | `cnsRemoveObserver` | De-register CNS observer | P3 |

**C++ Signatures:**
```cpp
// CommonNameService
virtual PVSSboolean addObserver(CNSObserver &observer);
virtual PVSSboolean removeObserver(CNSObserver &observer);

// CNSObserver - Change types
enum CNSChanges {
    STRUCTURE_CHANGED,      // Node added/deleted
    NAMES_CHANGED,          // Display names changed
    DATA_CHANGED,           // DataIdentifier changed
    VIEW_SEPARATOR_CHANGED, // View separator changed
    SYSTEM_NAMES_CHANGED    // System names changed
};
virtual void update(const CharString &path, CNSChanges what, const DpMsgManipCNS &msg) = 0;
```

**Java API Design:**
```java
public interface CnsObserver {
    void onCnsChange(String cnsPath, CnsChangeType changeType);
}

public enum CnsChangeType {
    STRUCTURE_CHANGED,
    NAMES_CHANGED,
    DATA_CHANGED,
    VIEW_SEPARATOR_CHANGED,
    SYSTEM_NAMES_CHANGED
}

public static int cnsAddObserver(String cnsPattern, CnsObserver observer) throws JException
public static void cnsRemoveObserver(int observerId) throws JException
```

### 2.3 Validation (3 functions)

| C++ Method | Java Method | Description | Priority |
|------------|-------------|-------------|----------|
| `CNSNodeNames::isLegalName()` | `cnsCheckId` | Validate node ID | P2 |
| `CNSNodeNames::isLegalDisplayName()` | `cnsCheckName` | Validate display name | P2 |
| `CNSNodeNames::isLegalDisplayNameSeparator()` | `cnsCheckSeparator` | Validate separator | P2 |

**C++ Signatures:**
```cpp
// CNSNodeNames (static methods)
static PVSSboolean isLegalName(const char *name);
static PVSSboolean isLegalDisplayName(const char *displayName);
static PVSSboolean isLegalDisplayName(const char *displayName, const char separator);
static PVSSboolean isLegalDisplayNameSeparator(char separator);
```

**Java API Design:**
```java
public static boolean cnsCheckId(String id)
public static boolean cnsCheckName(String displayName)
public static boolean cnsCheckName(String displayName, char separator)
public static boolean cnsCheckSeparator(char separator)
```

### 2.4 OPC Access Rights (1 function)

| C++ Method | Java Method | Description | Priority |
|------------|-------------|-------------|----------|
| `CNSViewBrowser::getOpcAccessRight()` | `cnsGetOPCAccessRight` | Get OPC access rights | P3 |

**Java API Design:**
```java
public static int cnsGetOPCAccessRight(String cnsPath) throws JException
```

---

## New Java Classes Required

### 1. CnsNode.java
```java
package at.rocworks.oa4j.var;

public class CnsNode {
    private String path;
    private String name;
    private String system;
    private String view;
    private LangTextVar displayNames;
    private LangTextVar displayPaths;
    private CnsDataIdentifier dataIdentifier;
    private byte[] userData;

    // Getters
    public String getPath() { return path; }
    public String getName() { return name; }
    public String getSystem() { return system; }
    public String getView() { return view; }
    public LangTextVar getDisplayNames() { return displayNames; }
    public LangTextVar getDisplayPaths() { return displayPaths; }
    public CnsDataIdentifier getDataIdentifier() { return dataIdentifier; }
    public byte[] getUserData() { return userData; }
}
```

### 2. CnsDataIdentifier.java
```java
package at.rocworks.oa4j.var;

public class CnsDataIdentifier {
    public enum Type {
        EMPTY(0),
        DATAPOINT(1);
        // Add more types as needed

        private final int value;
        Type(int value) { this.value = value; }
        public int getValue() { return value; }
    }

    private DpIdentifierVar dpId;
    private Type type;
    private byte[] userData;

    public DpIdentifierVar getDpId() { return dpId; }
    public Type getType() { return type; }
    public byte[] getUserData() { return userData; }
}
```

### 3. CnsObserver.java
```java
package at.rocworks.oa4j.base;

public interface CnsObserver {
    void onCnsChange(String cnsPath, CnsChangeType changeType);
}
```

### 4. CnsChangeType.java
```java
package at.rocworks.oa4j.base;

public enum CnsChangeType {
    STRUCTURE_CHANGED,
    NAMES_CHANGED,
    DATA_CHANGED,
    VIEW_SEPARATOR_CHANGED,
    SYSTEM_NAMES_CHANGED
}
```

### 5. CnsSearchMode.java
```java
package at.rocworks.oa4j.base;

public enum CnsSearchMode {
    NAME(0),           // Search by node ID
    DISPLAY_NAME(1),   // Search by display name
    ALL_NAMES(2),      // Search both
    CASE_INSENSITIVE(4); // Flag for case-insensitive search

    private final int value;
    CnsSearchMode(int value) { this.value = value; }
    public int getValue() { return value; }
}
```

---

## Technical Implementation Details

### Native Layer (C++)

Each CNS function needs:
1. JNI method declaration in `Manager.java`
2. JNI implementation in `at_rocworks_oa4j_jni_Manager.cpp`
3. Proper type conversion between Java and WinCC OA types
4. Error handling and logging

**Example for `cnsGetNode`:**

```cpp
// In at_rocworks_oa4j_jni_Manager.cpp
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsGetNode
  (JNIEnv *env, jobject obj, jstring path)
{
    const char *pathStr = env->GetStringUTFChars(path, NULL);

    CNSNode node;
    CommonNameService *cns = Manager::getCNS();

    if (cns && cns->getNode(pathStr, node)) {
        // Create Java CnsNode object
        jclass cnsNodeClass = env->FindClass("at/rocworks/oa4j/var/CnsNode");
        jobject jnode = env->NewObject(cnsNodeClass, /* constructor */);

        // Set fields from CNSNode
        CharString nodePath;
        node.getPath(nodePath);
        // ... populate Java object

        env->ReleaseStringUTFChars(path, pathStr);
        return jnode;
    }

    env->ReleaseStringUTFChars(path, pathStr);
    return nullptr;
}
```

### Java Layer

All CNS functions will be added as static methods in the `JClient` class:

```java
public class JClient {
    // Existing methods...

    // ========== CNS - Views ==========
    public static void cnsCreateView(String system, String viewId, String separator) throws JException {
        JManager.getInstance().executeTask(() -> {
            int result = Manager.apiCnsCreateView(system, viewId, separator);
            if (result != 0) throw new JException("cnsCreateView failed: " + result);
            return null;
        });
    }

    public static CnsNode cnsGetNode(String cnsPath) throws JException {
        return JManager.getInstance().executeTask(() -> {
            return Manager.apiCnsGetNode(cnsPath);
        });
    }

    // ... more CNS methods
}
```

---

## Functions NOT Available in C++ API

The following functions from the original WinCC OA Control script API are **NOT available** in the C++ Manager API and cannot be implemented:

| Control Function | Reason |
|------------------|--------|
| `cnsChangeViewNames` | View ID renaming not in C++ API |
| `cnsChangeNodeName` | Node ID renaming not in C++ API |
| `cns_viewExists`, `cns_treeExists`, `cns_nodeExists` | Can be derived from `getNode()` |
| `cns_isView`, `cns_isTree`, `cns_isNode` | Can be derived from path parsing |
| `cns_createNodeType`, `cns_deleteNodeType` | Node type management not in C++ API |
| `cns_getNodeTypes`, `cns_getNodeTypeDisplayName` | Node type management not in C++ API |
| `cns_setNodeTypeDisplayName`, `cns_changeNodeTypeName` | Node type management not in C++ API |
| `cns_getNodeTypeIcon`, `cns_setNodeTypeIcon` | Node type icons not in C++ API |
| `cns_getNodeTypeValue`, `cns_setNodeTypeValue` | Node type values not in C++ API |
| `cns_getNodeIcon`, `cns_setNodeIcon` | Node icons not in C++ API |
| `cns_getViewPermission`, `cns_setViewPermission` | Permissions not in C++ API |
| `cns_getReadableViews` | Permission-filtered views not in C++ API |

**Note:** Some existence/type checks can be implemented in Java by wrapping `getNode()` and checking results.

---

## Summary

| Category | Count |
|----------|-------|
| **Phase 1: Core API** | ~31 functions |
| **Phase 2: Properties & Utilities** | ~10 functions |
| **Total Implementable** | ~41 functions |
| **Not Available (Control-only)** | ~15 functions |

### Priority Order

1. **P1 (Critical):** View/Tree/Node CRUD, basic queries - enables basic CNS operations
2. **P2 (Important):** Display names, properties, validation - enhances usability
3. **P3 (Nice to have):** Observers, system names, OPC rights - advanced features

---

## Testing Strategy

1. **Unit Tests** - Test each function individually
2. **Integration Tests** - Test complete workflows:
   - Create view → add tree → add nodes → query → delete
   - Property set/get/remove cycles
   - Observer registration and callbacks
3. **Error Tests** - Test invalid inputs, non-existent paths
4. **Performance Tests** - Test with large hierarchies

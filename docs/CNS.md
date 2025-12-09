# CNS (Common Name Service) API for Java

This document describes the CNS (Common Name Service) Java API wrapper for WinCC OA. The CNS provides a hierarchical naming structure that allows alternative names for datapoints.

## Overview

The CNS API is exposed through native methods in the `Manager` class and uses the following Java classes:

| Java Class | C++ Class | Description |
|------------|-----------|-------------|
| `CnsNode` | `CNSNode` | Represents a node in the CNS hierarchy |
| `CnsDataIdentifier` | `CNSDataIdentifier` | Links a node to a datapoint |
| `CnsObserver` | `CNSObserver` | Interface for change notifications |
| `LangTextVar` | `LangText` | Multi-language text values |
| `DpIdentifierVar` | `DpIdentifier` | Datapoint identifier |

## Java Classes

### CnsNode

Represents a CNS node with the following properties:

| Property | Type | Description |
|----------|------|-------------|
| `path` | `String` | Full CNS path (e.g., "System1:view/tree/node") |
| `name` | `String` | Node identifier name |
| `system` | `String` | System name (e.g., "System1") |
| `view` | `String` | View name |
| `displayNames` | `LangTextVar` | Multi-language display names |
| `displayPaths` | `LangTextVar` | Multi-language display paths |
| `dpId` | `DpIdentifierVar` | Linked datapoint identifier |
| `nodeType` | `int` | Node type (see CnsDataIdentifier.Types) |
| `userData` | `byte[]` | Custom user data |

### CnsDataIdentifier

Links a CNS node to a datapoint:

| Property | Type | Description |
|----------|------|-------------|
| `dpId` | `DpIdentifierVar` | Datapoint identifier |
| `type` | `int` | Node type |
| `userData` | `byte[]` | Custom user data |

**Node Types (`CnsDataIdentifier.Types`):**

| Constant | Value | Description |
|----------|-------|-------------|
| `NO_TYPE` | 0 | Structure node without data link |
| `DATAPOINT` | 1 | Node linked to a datapoint |
| `DP_TYPE` | 2 | Node linked to a datapoint type |
| `OPC_ITEM` | 3 | Node linked to an OPC item |
| `ALL_TYPES` | 4 | All types (for search operations) |

### CnsObserver

Interface for receiving CNS change notifications. Implement `onCnsChange(String path, int changeType)`.

**Change Types (`CnsObserver.ChangeType`):**

| Constant | Value | Description |
|----------|-------|-------------|
| `STRUCTURE_CHANGED` | 0 | Nodes added or removed |
| `NAMES_CHANGED` | 1 | Node names changed |
| `DATA_CHANGED` | 2 | Node data (DP link) changed |
| `VIEW_SEPARATOR_CHANGED` | 3 | View separator changed |
| `SYSTEM_NAMES_CHANGED` | 4 | System display names changed |

## API Function Reference

### View Management

| Java Method | C++ Method | Description |
|-------------|------------|-------------|
| `apiCnsCreateView(system, viewId, separator, displayNames)` | `CommonNameService::createView(sysNum, viewNames, separators)` | Create a new CNS view |
| `apiCnsDeleteView(system, viewId)` | `CommonNameService::deleteView(sysNum, viewId)` | Delete a view |
| `apiCnsGetViews(system)` | `CommonNameService::getViews(sysNum, views)` | Get all views in a system |
| `apiCnsGetViewDisplayNames(system, viewId)` | `CommonNameService::getViewNames(sysNum, viewId, names)` | Get view display names |
| `apiCnsChangeViewDisplayNames(system, viewId, displayNames)` | `CommonNameService::changeView(sysNum, viewId, names)` | Change view display names |
| `apiCnsGetViewSeparators(system, viewId)` | `CommonNameService::getSeparators(sysNum, viewId, separators)` | Get view path separators |
| `apiCnsChangeViewSeparators(system, viewId, separator)` | `CommonNameService::changeView(sysNum, viewId, separators)` | Change view separators |

### Tree Management

| Java Method | C++ Method | Description |
|-------------|------------|-------------|
| `apiCnsAddTree(system, viewId, nodeId, nodeType, dpId, displayNames)` | `CommonNameService::addTree(sysNum, viewId, tree)` | Add a tree to a view |
| `apiCnsDeleteTree(cnsPath)` | `CommonNameService::deleteTree(node)` | Delete a tree or subtree |
| `apiCnsGetTrees(system, viewId)` | `CommonNameService::getTrees(sysNum, viewId, trees)` | Get all trees in a view |
| `apiCnsGetRoot(cnsPath)` | `CommonNameService::getRoot(node, root)` | Get root node of a tree |

### Node Management

| Java Method | C++ Method | Description |
|-------------|------------|-------------|
| `apiCnsAddNode(parentPath, nodeId, nodeType, dpId, displayNames)` | `CommonNameService::addNode(parent, names, dataId)` | Add a child node |
| `apiCnsGetChildren(cnsPath)` | `CommonNameService::getChildren(node, children)` | Get child nodes |
| `apiCnsGetParent(cnsPath)` | `CommonNameService::getParent(node, parent)` | Get parent node |
| `apiCnsChangeNodeData(cnsPath, dpId, nodeType)` | `CommonNameService::changeNode(node, dataId)` | Change node data link |
| `apiCnsChangeNodeDisplayNames(cnsPath, displayNames)` | `CommonNameService::changeNode(node, names)` | Change node display names |

### Query & Navigation

| Java Method | C++ Method | Description |
|-------------|------------|-------------|
| `apiCnsGetNode(cnsPath)` | `CommonNameService::getNode(path, node)` | Get node by path |
| `apiCnsGetId(cnsPath)` | `CommonNameService::getId(path, dataId)` | Get data identifier by path |
| `apiCnsGetNodesByName(system, viewId, pattern, searchMode, langIdx)` | `CommonNameService::getNodes(pattern, sysNum, viewId, mode, langIdx, types, nodes)` | Search nodes by name pattern |
| `apiCnsGetNodesByData(system, viewId, dpId)` | `CommonNameService::getNodes(dataId, sysNum, viewId, nodes)` | Find nodes by datapoint |
| `apiCnsSubStr(cnsPath, mask, resolve)` | `CommonNameService::subStr(path, mask, resolve)` | Extract parts of CNS path |
| `apiCnsGetIdSet(system, viewId, pattern, searchMode, langIdx)` | `CommonNameService::getIdSet(pattern, sysNum, viewId, mode, langIdx, types, idSet)` | Get datapoint IDs matching pattern |

**Search Modes for `apiCnsGetNodesByName` and `apiCnsGetIdSet`:**

| Value | Mode | Description |
|-------|------|-------------|
| 0 | NAME | Search by node ID name |
| 1 | DISPLAY_NAME | Search by display name |
| 2 | ALL_NAMES | Search by both ID and display names |

### System Names

| Java Method | C++ Method | Description |
|-------------|------------|-------------|
| `apiCnsGetSystemNames(system)` | `CommonNameService::getSystemNames(sysNum, names)` | Get system display names |
| `apiCnsSetSystemNames(system, displayNames)` | `CommonNameService::setSystemNames(sysNum, displayNames)` | Set system display names |

### Validation

| Java Method | C++ Method | Description |
|-------------|------------|-------------|
| `apiCnsCheckId(id)` | `CNSNodeNames::isLegalName(name)` | Check if node ID is valid |
| `apiCnsCheckName(displayName)` | `CNSNodeNames::isLegalDisplayName(name)` | Check if display name is valid |
| `apiCnsCheckSeparator(separator)` | `CNSNodeNames::isLegalDisplayNameSeparator(sep)` | Check if separator is valid |

### Observer (Change Notifications)

| Java Method | C++ Method | Description |
|-------------|------------|-------------|
| `apiCnsAddObserver(observer)` | `CommonNameService::addObserver(observer)` | Register for change notifications |
| `apiCnsRemoveObserver(observerId)` | `CommonNameService::removeObserver(observer)` | Unregister observer |

## Usage Examples

### Creating a View and Tree

```java
import at.rocworks.oa4j.jni.Manager;
import at.rocworks.oa4j.var.LangTextVar;
import at.rocworks.oa4j.var.CnsDataIdentifier;

// Create display names
LangTextVar displayNames = new LangTextVar();
displayNames.setText(0, "My View");  // English
displayNames.setText(1, "Meine Ansicht");  // German

// Create a view
int result = manager.apiCnsCreateView(null, "myView", "/", displayNames);

// Add a tree root node
LangTextVar treeNames = new LangTextVar();
treeNames.setText(0, "Plant A");
result = manager.apiCnsAddTree(null, "myView", "plantA",
    CnsDataIdentifier.Types.NO_TYPE, null, treeNames);

// Add a child node linked to a datapoint
LangTextVar nodeNames = new LangTextVar();
nodeNames.setText(0, "Temperature");
DpIdentifierVar dpId = ...; // Get from dpGet or other API
result = manager.apiCnsAddNode("System1:myView/plantA", "temp1",
    CnsDataIdentifier.Types.DATAPOINT, dpId, nodeNames);
```

### Querying Nodes

```java
// Get all views
String[] views = manager.apiCnsGetViews(null);

// Get trees in a view
String[] trees = manager.apiCnsGetTrees(null, "myView");

// Get node details
CnsNode node = (CnsNode) manager.apiCnsGetNode("System1:myView/plantA/temp1");
System.out.println("Node: " + node.getName());
System.out.println("Display: " + node.getDisplayNames().getText(0));

// Search by pattern
String[] matches = manager.apiCnsGetNodesByName(null, "myView", "*temp*", 0, 0);

// Get datapoint IDs directly (more efficient)
DpIdentifierVar[] dpIds = manager.apiCnsGetIdSet(null, "myView", "*temp*", 0, 0);
```

### Observing Changes

```java
import at.rocworks.oa4j.var.CnsObserver;

// Create observer
CnsObserver observer = new CnsObserver() {
    @Override
    public void onCnsChange(String path, int changeType) {
        switch (changeType) {
            case CnsObserver.ChangeType.STRUCTURE_CHANGED:
                System.out.println("Structure changed: " + path);
                break;
            case CnsObserver.ChangeType.NAMES_CHANGED:
                System.out.println("Names changed: " + path);
                break;
            case CnsObserver.ChangeType.DATA_CHANGED:
                System.out.println("Data changed: " + path);
                break;
        }
    }
};

// Register observer
int observerId = manager.apiCnsAddObserver(observer);

// ... later, unregister
manager.apiCnsRemoveObserver(observerId);
```

## Return Values

Most methods follow these conventions:

| Return Type | Success | Failure |
|-------------|---------|---------|
| `int` | `0` | `-1` |
| `String` | Value | `null` |
| `String[]` | Array | `null` |
| `Object` | Object | `null` |
| `boolean` | `true` | `false` |

## Notes

- The `system` parameter can be `null` to use the default system
- CNS paths follow the format: `System:view/tree/node/...`
- Display names support multiple languages via `LangTextVar`
- The observer callback is invoked from the WinCC OA event thread

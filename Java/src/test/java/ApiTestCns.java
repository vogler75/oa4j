/*
    OA4J - WinCC Open Architecture for Java
    Copyright (C) 2017 Andreas Vogler

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

import at.rocworks.oa4j.base.JClient;
import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.jni.ErrCode;
import at.rocworks.oa4j.jni.ErrPrio;
import at.rocworks.oa4j.var.CnsDataIdentifier;
import at.rocworks.oa4j.var.CnsNode;
import at.rocworks.oa4j.var.DpIdentifierVar;
import at.rocworks.oa4j.var.LangTextVar;

/**
 * Test class for CNS (Common Name Service) API functions.
 * Creates a view with a 5-level tree, attaches datapoints, reads and walks the tree.
 *
 * @author vogler
 */
public class ApiTestCns {

    private static final String VIEW_ID = "testView";
    private static final String TREE_ROOT = "plant";
    private static final String SEPARATOR = "/";

    public static void main(String[] args) throws Exception {
        JManager m = new JManager();
        m.init(args).start();
        new ApiTestCns().run();
        m.stop();
    }

    public void run() throws InterruptedException {
        log("=== CNS API Test Start ===");

        // Get datapoints of type ExampleDP_Float
        String[] dps = JClient.dpNames("*", "ExampleDP_Float");
        if (dps == null || dps.length == 0) {
            log("ERROR: No datapoints of type ExampleDP_Float found!");
            return;
        }
        log("Found " + dps.length + " datapoints of type ExampleDP_Float");

        // Clean up any existing view from previous test runs
        cleanupView();

        // Create the view and tree structure
        if (!createViewAndTree(dps)) {
            log("ERROR: Failed to create view and tree!");
            return;
        }

        // Read and walk the tree
        readAndWalkTree();

        log("--- Waiting 5 seconds before cleanup ---");
        Thread.sleep(30000);

        // Delete the view
        cleanupView();

        log("=== CNS API Test End ===");
    }

    private void cleanupView() {
        log("Cleaning up view: " + VIEW_ID);
        int result = JClient.cnsDeleteView(null, VIEW_ID);
        if (result == 0) {
            log("View deleted successfully");
        } else {
            log("View did not exist or could not be deleted (this is OK for first run)");
        }
    }

    private boolean createViewAndTree(String[] dps) {
        log("--- Creating CNS View and Tree ---");

        // Create display names for the view
        LangTextVar viewDisplayNames = new LangTextVar();
        viewDisplayNames.setText(0, "Test View (English)");
        viewDisplayNames.setText(1, "Testansicht (Deutsch)");

        // Create the view
        log("Creating view: " + VIEW_ID);
        int result = JClient.cnsCreateView(null, VIEW_ID, SEPARATOR, viewDisplayNames);
        if (result != 0) {
            log("ERROR: Failed to create view!");
            return false;
        }
        log("View created successfully");

        // Verify view was created
        String[] views = JClient.cnsGetViews(null);
        if (views != null) {
            log("Available views: " + String.join(", ", views));
        }

        // Get view separator
        String sep = JClient.cnsGetViewSeparators(null, VIEW_ID);
        log("View separator: " + sep);

        // Create tree root (level 1) - structure node, no datapoint
        LangTextVar rootDisplayNames = new LangTextVar();
        rootDisplayNames.setText(0, "Plant");
        rootDisplayNames.setText(1, "Anlage");

        log("Creating tree root: " + TREE_ROOT);
        result = JClient.cnsAddTree(null, VIEW_ID, TREE_ROOT, CnsDataIdentifier.Types.NO_TYPE, null, rootDisplayNames);
        if (result != 0) {
            log("ERROR: Failed to create tree root!");
            return false;
        }
        log("Tree root created successfully");

        // Get the tree root path for adding children
        String[] trees = JClient.cnsGetTrees(null, VIEW_ID);
        if (trees == null || trees.length == 0) {
            log("ERROR: No trees found after creation!");
            return false;
        }
        String rootPath = trees[0];
        log("Tree root path: " + rootPath);

        // Create 5-level hierarchy with datapoints at level 5
        // Level 2: area1, area2
        // Level 3: unit1, unit2 under each area
        // Level 4: device1, device2 under each unit
        // Level 5: sensor nodes with datapoints

        int dpIndex = 0;
        String[] areas = {"area1", "area2"};
        String[] units = {"unit1", "unit2"};
        String[] devices = {"device1", "device2"};

        for (String area : areas) {
            // Level 2: Area
            LangTextVar areaNames = new LangTextVar();
            areaNames.setText(0, "Area " + area.substring(4));
            String areaPath = addNode(rootPath, area, CnsDataIdentifier.Types.NO_TYPE, null, areaNames);
            if (areaPath == null) continue;
            log("Area path: " + areaPath);

            for (String unit : units) {
                // Level 3: Unit
                LangTextVar unitNames = new LangTextVar();
                unitNames.setText(0, "Unit " + unit.substring(4));
                String unitPath = addNode(areaPath, unit, CnsDataIdentifier.Types.NO_TYPE, null, unitNames);
                if (unitPath == null) continue;

                for (String device : devices) {
                    // Level 4: Device
                    LangTextVar deviceNames = new LangTextVar();
                    deviceNames.setText(0, "Device " + device.substring(6));
                    String devicePath = addNode(unitPath, device, CnsDataIdentifier.Types.NO_TYPE, null, deviceNames);
                    if (devicePath == null) continue;

                    // Level 5: Sensor with datapoint
                    if (dpIndex < dps.length) {
                        String sensorId = "sensor" + (dpIndex + 1);
                        LangTextVar sensorNames = new LangTextVar();
                        sensorNames.setText(0, "Sensor " + (dpIndex + 1));

                        DpIdentifierVar dpId = new DpIdentifierVar(dps[dpIndex]);
                        log("  Attaching datapoint: " + dps[dpIndex] + " to " + sensorId);

                        addNode(devicePath, sensorId, CnsDataIdentifier.Types.DATAPOINT, dpId, sensorNames);
                        dpIndex++;
                    }
                }
            }
        }

        log("Created tree with " + dpIndex + " datapoints attached");
        return true;
    }

    private String addNode(String parentPath, String nodeId, int nodeType, DpIdentifierVar dpId, LangTextVar displayNames) {
        log("  Adding node: " + nodeId + " under " + parentPath);
        int result = JClient.cnsAddNode(parentPath, nodeId, nodeType, dpId, displayNames);
        if (result != 0) {
            log("ERROR: Failed to add node: " + nodeId + " under " + parentPath);
            return null;
        }
        // Return the new node's path by querying children
        // Note: CNS uses dot (.) as internal path separator, not the view's display separator
        String[] children = JClient.cnsGetChildren(parentPath);
        if (children != null) {
            for (String child : children) {
                // Check if this child ends with .nodeId (CNS internal format)
                if (child.endsWith("." + nodeId)) {
                    return child;
                }
            }
        }
        // Fallback: construct the path using dot separator (CNS internal format)
        String newPath = parentPath + "." + nodeId;
        log("    Constructed path (fallback): " + newPath);
        return newPath;
    }

    private void readAndWalkTree() {
        log("--- Reading and Walking the Tree ---");

        // Get all trees in the view
        String[] trees = JClient.cnsGetTrees(null, VIEW_ID);
        if (trees == null || trees.length == 0) {
            log("ERROR: No trees found!");
            return;
        }

        log("Found " + trees.length + " tree(s) in view " + VIEW_ID);

        for (String treePath : trees) {
            log("Walking tree: " + treePath);
            walkNode(treePath, 0);
        }

        // Test search by name
        log("--- Testing Search by Name ---");
        String[] sensorNodes = JClient.cnsGetNodesByName(null, VIEW_ID, "*sensor*", 0, 0);
        if (sensorNodes != null) {
            log("Found " + sensorNodes.length + " nodes matching '*sensor*':");
            for (String node : sensorNodes) {
                log("  " + node);
            }
        }

        // Test getIdSet
        log("--- Testing getIdSet ---");
        DpIdentifierVar[] dpIds = JClient.cnsGetIdSet(null, VIEW_ID, "*sensor*", 0, 0);
        if (dpIds != null) {
            log("Found " + dpIds.length + " datapoints via getIdSet:");
            for (DpIdentifierVar dpId : dpIds) {
                if (dpId != null) {
                    log("  " + dpId.getName());
                }
            }
        }

        // Test validation functions
        log("--- Testing Validation Functions ---");
        log("Valid node ID 'test123': " + JClient.cnsCheckId("test123"));
        log("Valid node ID 'test/123': " + JClient.cnsCheckId("test/123"));
        log("Valid display name 'Test Node': " + JClient.cnsCheckName("Test Node"));
        log("Valid separator '/': " + JClient.cnsCheckSeparator('/'));
        log("Valid separator '.': " + JClient.cnsCheckSeparator('.'));
    }

    private void walkNode(String nodePath, int depth) {
        String indent = "  ".repeat(depth);

        // Get node details
        CnsNode node = JClient.cnsGetNode(nodePath);
        if (node != null) {
            log(indent + "Node: " + node.getName());
            log(indent + "  Path: " + node.getPath());
            log(indent + "  Type: " + node.getNodeType());

            LangTextVar displayNames = node.getDisplayNames();
            if (displayNames != null) {
                String dn = displayNames.getText(0);
                if (dn != null) {
                    log(indent + "  Display Name: " + dn);
                }
            }

            DpIdentifierVar dpId = node.getDpId();
            if (dpId != null && dpId.getName() != null && !dpId.getName().isEmpty()) {
                log(indent + "  Datapoint: " + dpId.getName());
            }
        } else {
            log(indent + "Node: " + nodePath + " (could not get details)");
        }

        // Get parent (verify navigation)
        if (depth > 0) {
            String parent = JClient.cnsGetParent(nodePath);
            if (parent != null) {
                log(indent + "  Parent: " + parent);
            }
        }

        // Get root (verify navigation)
        String root = JClient.cnsGetRoot(nodePath);
        if (root != null && depth > 0) {
            log(indent + "  Root: " + root);
        }

        // Get and walk children
        String[] children = JClient.cnsGetChildren(nodePath);
        if (children != null && children.length > 0) {
            log(indent + "  Children: " + children.length);
            for (String child : children) {
                walkNode(child, depth + 1);
            }
        }
    }

    private void log(String message) {
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, message);
    }
}

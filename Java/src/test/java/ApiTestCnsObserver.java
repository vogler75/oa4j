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
import at.rocworks.oa4j.var.CnsObserver;
import at.rocworks.oa4j.var.LangTextVar;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test class for CNS Observer functionality.
 * Creates a CNS tree, registers an observer, modifies the tree,
 * and verifies that the observer receives change notifications.
 *
 * @author vogler
 */
public class ApiTestCnsObserver {

    private static final String VIEW_ID = "observerTestView";
    private static final String TREE_ROOT = "root";

    private final AtomicInteger changeCount = new AtomicInteger(0);
    private int observerId = -1;

    public static void main(String[] args) throws Exception {
        JManager m = new JManager();
        m.init(args).start();
        new ApiTestCnsObserver().run();
        m.stop();
    }

    public void run() throws InterruptedException {
        log("=== CNS Observer Test Start ===");

        // Clean up any existing view from previous test runs
        cleanupView();

        // Step 1: Create a simple CNS tree
        if (!createInitialTree()) {
            log("ERROR: Failed to create initial tree!");
            return;
        }

        // Step 2: Register the observer
        if (!registerObserver()) {
            log("ERROR: Failed to register observer!");
            cleanupView();
            return;
        }

        log("--- Waiting 2 seconds for observer to be ready ---");
        Thread.sleep(2000);

        // Step 3: Modify the tree and check for observer notifications
        performModifications();

        // Step 4: Wait and check results
        log("--- Waiting 5 seconds for observer notifications ---");
        Thread.sleep(5000);

        int totalChanges = changeCount.get();
        log("Total observer notifications received: " + totalChanges);

        if (totalChanges > 0) {
            log("SUCCESS: Observer received " + totalChanges + " notification(s)");
        } else {
            log("WARNING: No observer notifications received");
        }

        // Step 5: Cleanup
        log("--- Removing observer ---");
        if (observerId >= 0) {
            int result = JClient.cnsRemoveObserver(observerId);
            if (result == 0) {
                log("Observer removed successfully");
            } else {
                log("Failed to remove observer");
            }
        }

        log("--- Waiting 2 seconds before cleanup ---");
        Thread.sleep(2000);

        cleanupView();

        log("=== CNS Observer Test End ===");
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

    private boolean createInitialTree() {
        log("--- Creating Initial CNS Tree ---");

        // Create display names for the view
        LangTextVar viewDisplayNames = new LangTextVar();
        viewDisplayNames.setText(0, "Observer Test View");

        // Create the view
        log("Creating view: " + VIEW_ID);
        int result = JClient.cnsCreateView(null, VIEW_ID, "/", viewDisplayNames);
        if (result != 0) {
            log("ERROR: Failed to create view!");
            return false;
        }
        log("View created successfully");

        // Create tree root
        LangTextVar rootDisplayNames = new LangTextVar();
        rootDisplayNames.setText(0, "Root Node");

        log("Creating tree root: " + TREE_ROOT);
        result = JClient.cnsAddTree(null, VIEW_ID, TREE_ROOT, CnsDataIdentifier.Types.NO_TYPE, null, rootDisplayNames);
        if (result != 0) {
            log("ERROR: Failed to create tree root!");
            return false;
        }
        log("Tree root created successfully");

        // Get tree path
        String[] trees = JClient.cnsGetTrees(null, VIEW_ID);
        if (trees == null || trees.length == 0) {
            log("ERROR: No trees found after creation!");
            return false;
        }
        String rootPath = trees[0];
        log("Tree root path: " + rootPath);

        // Add initial child node
        LangTextVar childNames = new LangTextVar();
        childNames.setText(0, "Initial Child");

        log("Adding initial child node: child1");
        result = JClient.cnsAddNode(rootPath, "child1", CnsDataIdentifier.Types.NO_TYPE, null, childNames);
        if (result != 0) {
            log("ERROR: Failed to add initial child node!");
            return false;
        }
        log("Initial child node added successfully");

        return true;
    }

    private boolean registerObserver() {
        log("--- Registering CNS Observer ---");

        // Create observer implementation
        CnsObserver observer = new CnsObserver() {
            @Override
            public void onCnsChange(String path, int changeType) {
                String changeTypeName = getChangeTypeName(changeType);
                log("OBSERVER: Change detected - Path: " + path + ", Type: " + changeTypeName + " (" + changeType + ")");
                changeCount.incrementAndGet();
            }

            private String getChangeTypeName(int type) {
                switch (type) {
                    case CnsObserver.ChangeType.STRUCTURE_CHANGED: return "STRUCTURE_CHANGED";
                    case CnsObserver.ChangeType.NAMES_CHANGED: return "NAMES_CHANGED";
                    case CnsObserver.ChangeType.DATA_CHANGED: return "DATA_CHANGED";
                    case CnsObserver.ChangeType.VIEW_SEPARATOR_CHANGED: return "VIEW_SEPARATOR_CHANGED";
                    case CnsObserver.ChangeType.SYSTEM_NAMES_CHANGED: return "SYSTEM_NAMES_CHANGED";
                    default: return "UNKNOWN";
                }
            }
        };

        // Register the observer
        observerId = JClient.cnsAddObserver(observer);
        if (observerId < 0) {
            log("ERROR: Failed to register observer!");
            return false;
        }
        log("Observer registered successfully with ID: " + observerId);

        return true;
    }

    private void performModifications() {
        log("--- Performing Tree Modifications ---");

        // Get tree path
        String[] trees = JClient.cnsGetTrees(null, VIEW_ID);
        if (trees == null || trees.length == 0) {
            log("ERROR: No trees found!");
            return;
        }
        String rootPath = trees[0];

        // Modification 1: Add a new node
        log("Modification 1: Adding new node 'child2'");
        LangTextVar child2Names = new LangTextVar();
        child2Names.setText(0, "Second Child");
        int result = JClient.cnsAddNode(rootPath, "child2", CnsDataIdentifier.Types.NO_TYPE, null, child2Names);
        if (result == 0) {
            log("  Node 'child2' added successfully");
        } else {
            log("  ERROR: Failed to add node 'child2'");
        }

        // Wait a bit between modifications
        try { Thread.sleep(1000); } catch (InterruptedException e) { }

        // Modification 2: Add another node under root
        log("Modification 2: Adding new node 'child3'");
        LangTextVar child3Names = new LangTextVar();
        child3Names.setText(0, "Third Child");
        result = JClient.cnsAddNode(rootPath, "child3", CnsDataIdentifier.Types.NO_TYPE, null, child3Names);
        if (result == 0) {
            log("  Node 'child3' added successfully");
        } else {
            log("  ERROR: Failed to add node 'child3'");
        }

        // Wait a bit
        try { Thread.sleep(1000); } catch (InterruptedException e) { }

        // Modification 3: Change display names of a node
        log("Modification 3: Changing display name of 'child1'");
        String child1Path = rootPath + ".child1";
        LangTextVar newNames = new LangTextVar();
        newNames.setText(0, "Updated First Child");
        result = JClient.cnsChangeNodeDisplayNames(child1Path, newNames);
        if (result == 0) {
            log("  Display name changed successfully");
        } else {
            log("  ERROR: Failed to change display name (result=" + result + ")");
        }

        // Wait a bit
        try { Thread.sleep(1000); } catch (InterruptedException e) { }

        // Modification 4: Add a nested node
        log("Modification 4: Adding nested node 'subchild1' under 'child2'");
        String child2Path = rootPath + ".child2";
        LangTextVar subchildNames = new LangTextVar();
        subchildNames.setText(0, "Sub Child 1");
        result = JClient.cnsAddNode(child2Path, "subchild1", CnsDataIdentifier.Types.NO_TYPE, null, subchildNames);
        if (result == 0) {
            log("  Nested node 'subchild1' added successfully");
        } else {
            log("  ERROR: Failed to add nested node 'subchild1'");
        }

        log("--- Modifications Complete ---");
    }

    private void log(String message) {
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, message);
    }
}

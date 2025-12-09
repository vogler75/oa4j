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
import at.rocworks.oa4j.var.DpElementType;
import at.rocworks.oa4j.var.DpTypeElement;

/**
 * Test class for datapoint type management functions.
 * Tests dpTypeCreate, dpTypeChange, dpTypeGet, and dpTypeDelete.
 *
 * @author vogler
 */
public class ApiTestDpType {

    private static final String TEST_TYPE_NAME = "TestType_Java";

    public static void main(String[] args) throws Exception {
        JManager m = new JManager();
        m.init(args).start();
        new ApiTestDpType().run();
        m.stop();
    }

    public void run() throws InterruptedException {
        log("=== Datapoint Type Management Test Start ===");

        // Clean up any existing type from previous test runs
        cleanupType();

        // Test 1: Create a new datapoint type
        testCreateType();

        // Wait for type to be registered
        log("--- Waiting 30 seconds for type to be registered ---");
        Thread.sleep(30000);

        // Test 2: Verify the type exists
        testVerifyTypeExists();

        // Test 3: Modify the type (add new element)
        testChangeType();

        // Wait for change to be applied
        Thread.sleep(2000);

        // Test 4: Read type and show full details
        testGetType();

        // Test 5: Delete the type
        testDeleteType();

        log("=== Datapoint Type Management Test End ===");
    }

    private void cleanupType() {
        log("--- Cleaning up existing type: " + TEST_TYPE_NAME + " ---");
        int result = JClient.dpTypeDelete(TEST_TYPE_NAME);
        if (result == 0) {
            log("Type deleted successfully");
        } else {
            log("Type did not exist or could not be deleted (this is OK for first run)");
        }
    }

    private void testCreateType() {
        log("--- Test 1: Create Datapoint Type ---");

        // Create type structure:
        // TestType_Java (RECORD) - root must be RECORD type
        //   ├── value (FLOAT)
        //   ├── status (INT)
        //   └── config (RECORD) - nested structure also uses RECORD
        //         ├── enabled (BIT)
        //         └── threshold (FLOAT)

        DpTypeElement root = new DpTypeElement(TEST_TYPE_NAME, 1, DpElementType.RECORD);

        // Add value element
        DpTypeElement value = new DpTypeElement("value", 2, DpElementType.FLOAT);
        root.addChild(value);

        // Add status element
        DpTypeElement status = new DpTypeElement("status", 3, DpElementType.INT);
        root.addChild(status);

        // Add config structure (nested RECORD)
        DpTypeElement config = new DpTypeElement("config", 4, DpElementType.RECORD);
        config.addChild(new DpTypeElement("enabled", 5, DpElementType.BIT));
        config.addChild(new DpTypeElement("threshold", 6, DpElementType.FLOAT));
        root.addChild(config);

        log("Creating type with structure:");
        log(root.toString());

        int result = JClient.dpTypeCreate(root);
        if (result == 0) {
            log("SUCCESS: Type created successfully");
        } else {
            log("ERROR: Failed to create type (result=" + result + ")");
        }
    }

    private void testVerifyTypeExists() {
        log("--- Test 2: Verify Type Exists ---");

        int typeId = JClient.dpTypeNameToId(TEST_TYPE_NAME);
        if (typeId > 0) {
            log("SUCCESS: Type exists with ID: " + typeId);
        } else {
            log("ERROR: Type not found");
        }
    }

    private void testGetType() {
        log("--- Test 4: Get Datapoint Type ---");

        // Get the type ID
        int typeId = JClient.dpTypeNameToId(TEST_TYPE_NAME);
        log("Type ID for '" + TEST_TYPE_NAME + "': " + typeId);

        // Get the type structure as a tree
        DpTypeElement typeDef = JManager.getInstance().dpTypeGetTree(TEST_TYPE_NAME);
        if (typeDef != null) {
            log("SUCCESS: Retrieved type definition:");
            log(typeDef.toString());

            // List all elements with their types
            log("Element details:");
            printElementDetails(typeDef, 0);

            // List all element paths
            log("Element paths:");
            for (String path : typeDef.getElementPaths()) {
                log("  " + path);
            }
        } else {
            log("ERROR: Failed to retrieve type definition");
        }
    }

    private void printElementDetails(DpTypeElement element, int depth) {
        String indent = "  ".repeat(depth);
        log(indent + "- " + element.getName() +
            " [id=" + element.getElementId() +
            ", type=" + element.getElementType() +
            " (" + element.getElementType().getValue() + ")" +
            (element.getReferencedTypeId() != 0 ? ", refTypeId=" + element.getReferencedTypeId() : "") +
            "]");
        for (DpTypeElement child : element.getChildren()) {
            printElementDetails(child, depth + 1);
        }
    }

    private void testChangeType() {
        log("--- Test 3: Change Datapoint Type ---");

        // Get the type ID
        int typeId = JClient.dpTypeNameToId(TEST_TYPE_NAME);
        if (typeId < 0) {
            log("ERROR: Type not found, cannot modify");
            return;
        }

        // Add a new element "description" under root
        DpTypeElement newElement = new DpTypeElement("description", 0, DpElementType.TEXT);

        log("Adding new element 'description' to type (typeId=" + typeId + ")");

        int result = JClient.dpTypeChange(typeId, newElement, true);
        if (result == 0) {
            log("SUCCESS: Type modified successfully");
        } else {
            log("ERROR: Failed to modify type (result=" + result + ")");
        }
    }

    private void testDeleteType() {
        log("--- Test 5: Delete Datapoint Type ---");

        int result = JClient.dpTypeDelete(TEST_TYPE_NAME);
        if (result == 0) {
            log("SUCCESS: Type deleted successfully");

            // Verify deletion
            int typeId = JClient.dpTypeNameToId(TEST_TYPE_NAME);
            if (typeId < 0) {
                log("Verified: Type no longer exists");
            } else {
                log("WARNING: Type still exists after deletion");
            }
        } else {
            log("ERROR: Failed to delete type (result=" + result + ")");
        }
    }

    private void log(String message) {
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, message);
    }
}

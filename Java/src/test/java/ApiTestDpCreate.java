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
 * Test class for datapoint creation and deletion functions.
 * Tests dpCreate, dpExists, and dpDelete.
 *
 * @author vogler
 */
public class ApiTestDpCreate {

    private static final String TEST_TYPE_NAME = "TestType_DpCreate";
    private static final String TEST_DP_PREFIX = "TestDP_Java_";

    public static void main(String[] args) throws Exception {
        JManager m = new JManager();
        m.init(args).start();
        new ApiTestDpCreate().run();
        m.stop();
    }

    public void run() throws InterruptedException {
        log("=== Datapoint Create/Delete Test Start ===");

        // Setup: Create a test type
        if (!setupTestType()) {
            log("ERROR: Failed to setup test type, aborting");
            return;
        }

        // Test 1: Create datapoints
        testCreateDatapoints();

        // Test 2: Verify datapoints exist
        testDatapointsExist();

        // Test 3: Try to read/write to datapoints
        testReadWriteDatapoints();

        // Wait
        Thread.sleep(20000);

        // Test 4: Delete datapoints
        testDeleteDatapoints();

        // Test 5: Verify datapoints no longer exist
        testDatapointsNotExist();

        // Cleanup: Delete the test type
        cleanupTestType();

        log("=== Datapoint Create/Delete Test End ===");
    }

    private boolean setupTestType() {
        log("--- Setup: Creating test type ---");

        // First try to delete any existing type
        JClient.dpTypeDelete(TEST_TYPE_NAME);

        // Create a simple type structure:
        // TestType_DpCreate (RECORD) - root must be RECORD type
        //   ├── value (FLOAT)
        //   └── name (TEXT)

        DpTypeElement root = new DpTypeElement(TEST_TYPE_NAME, 1, DpElementType.RECORD);
        root.addChild(new DpTypeElement("value", 2, DpElementType.FLOAT));
        root.addChild(new DpTypeElement("name", 3, DpElementType.TEXT));

        int result = JClient.dpTypeCreate(root);
        if (result == 0) {
            log("Test type created successfully");
            return true;
        } else {
            log("Failed to create test type");
            return false;
        }
    }

    private void cleanupTestType() {
        log("--- Cleanup: Deleting test type ---");
        int result = JClient.dpTypeDelete(TEST_TYPE_NAME);
        if (result == 0) {
            log("Test type deleted successfully");
        } else {
            log("Failed to delete test type (may have datapoints using it)");
        }
    }

    private void testCreateDatapoints() {
        log("--- Test 1: Create Datapoints ---");

        for (int i = 1; i <= 3; i++) {
            String dpName = TEST_DP_PREFIX + i;
            log("Creating datapoint: " + dpName);

            int result = JClient.dpCreate(dpName, TEST_TYPE_NAME);
            if (result == 0) {
                log("  SUCCESS: Datapoint created");
            } else {
                log("  ERROR: Failed to create datapoint (result=" + result + ")");
            }
        }
    }

    private void testDatapointsExist() {
        log("--- Test 2: Verify Datapoints Exist ---");

        for (int i = 1; i <= 3; i++) {
            String dpName = TEST_DP_PREFIX + i;
            boolean exists = JClient.dpExists(dpName);
            log("Datapoint '" + dpName + "' exists: " + exists);
            if (!exists) {
                log("  ERROR: Datapoint should exist!");
            }
        }

        // Also verify using dpNames
        String[] dps = JClient.dpNames(TEST_DP_PREFIX + "*", TEST_TYPE_NAME);
        if (dps != null) {
            log("Found " + dps.length + " datapoints matching pattern:");
            for (String dp : dps) {
                log("  " + dp);
            }
        } else {
            log("ERROR: dpNames returned null");
        }
    }

    private void testReadWriteDatapoints() {
        log("--- Test 3: Read/Write Datapoints ---");

        for (int i = 1; i <= 3; i++) {
            String dpName = TEST_DP_PREFIX + i;

            // Write a value
            double valueToWrite = i * 10.5;
            log("Writing " + valueToWrite + " to " + dpName + ".value");

            JClient.dpSet()
                .add(dpName + ".value", valueToWrite)
                .add(dpName + ".name", "Test DP " + i)
                .send();
        }

        // Wait a bit for values to be written
        try { Thread.sleep(1000); } catch (InterruptedException e) { }

        // Read values back
        for (int i = 1; i <= 3; i++) {
            String dpName = TEST_DP_PREFIX + i;
            log("Reading from " + dpName);

            JClient.dpGet()
                .add(dpName + ".value")
                .add(dpName + ".name")
                .action((result) -> {
                    log("  Read result: items=" + result.size());
                    for (int j = 0; j < result.size(); j++) {
                        log("    " + result.getItem(j).getDpName() + " = " + result.getItem(j).getVariable());
                    }
                })
                .await();
        }
    }

    private void testDeleteDatapoints() {
        log("--- Test 4: Delete Datapoints ---");

        for (int i = 1; i <= 3; i++) {
            String dpName = TEST_DP_PREFIX + i;
            log("Deleting datapoint: " + dpName);

            int result = JClient.dpDelete(dpName);
            if (result == 0) {
                log("  SUCCESS: Datapoint deleted");
            } else {
                log("  ERROR: Failed to delete datapoint (result=" + result + ")");
            }
        }
    }

    private void testDatapointsNotExist() {
        log("--- Test 5: Verify Datapoints No Longer Exist ---");

        for (int i = 1; i <= 3; i++) {
            String dpName = TEST_DP_PREFIX + i;
            boolean exists = JClient.dpExists(dpName);
            log("Datapoint '" + dpName + "' exists: " + exists);
            if (exists) {
                log("  ERROR: Datapoint should not exist!");
            }
        }

        // Also verify using dpNames
        String[] dps = JClient.dpNames(TEST_DP_PREFIX + "*", TEST_TYPE_NAME);
        if (dps == null || dps.length == 0) {
            log("Verified: No datapoints found matching pattern");
        } else {
            log("ERROR: Found " + dps.length + " datapoints that should have been deleted");
        }
    }

    private void log(String message) {
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, message);
    }
}

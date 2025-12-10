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

import at.rocworks.oa4j.WinCCOA;
import at.rocworks.oa4j.base.IHotLink;
import at.rocworks.oa4j.base.JDpConnect;
import at.rocworks.oa4j.base.JDpMsgAnswer;
import at.rocworks.oa4j.base.JDpQueryConnect;
import at.rocworks.oa4j.var.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Comprehensive test class for the WinCCOA unified API.
 * Tests all major functionality including:
 * - Datapoint type creation with all supported data types
 * - Datapoint creation (100 datapoints)
 * - dpConnect subscriptions
 * - dpSet publishing
 * - dpGet reading
 * - dpNames querying
 * - dpQueryConnect subscriptions
 * - Cleanup
 *
 * @author vogler
 */
public class ApiTestWinCCOA {

    private static final String TEST_TYPE_NAME = "TestType_WinCCOA_Full";
    private static final String TEST_DP_PREFIX = "TestDP_WinCCOA_";
    private static final int NUM_DATAPOINTS = 100;

    private WinCCOA oa;
    private AtomicInteger dpConnectCount = new AtomicInteger(0);
    private AtomicInteger queryConnectCount = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        new ApiTestWinCCOA().run(args);
    }

    public void run(String[] args) throws Exception {
        WinCCOA.log("========================================");
        WinCCOA.log("=== WinCCOA Full API Test Start ===");
        WinCCOA.log("========================================");

        // Connect to WinCC OA
        oa = WinCCOA.connect(args);
        WinCCOA.log("Connected: " + oa.isConnected());
        WinCCOA.log("Project Path: " + oa.getProjectPath());
        WinCCOA.log("Config Dir: " + oa.getConfigDir());
        WinCCOA.log("Log Dir: " + oa.getLogDir());
        WinCCOA.log("Manager: " + oa.getManagerName() + " (#" + oa.getManagerNumber() + ")");
        WinCCOA.log("Active: " + oa.isActive());

        // Register redundancy state listener
        oa.onRedundancyStateChanged(isActive -> {
            WinCCOA.log(">>> Redundancy state changed: " + (isActive ? "ACTIVE" : "PASSIVE"));
        });

        try {
            // Step 1: Cleanup any previous test data
            cleanup();

            // Step 2: Create datapoint type with all supported data types
            testCreateType();

            // Wait for type to be registered
            WinCCOA.log("Waiting 5 seconds for type registration...");
            Thread.sleep(5000);

            // Step 2b: Verify type with dpTypeNameToId and dpTypeGet
            testVerifyType();

            // Step 3: Create 100 datapoints
            testCreateDatapoints();

            // Wait for datapoints to be created
            WinCCOA.log("Waiting 3 seconds for datapoint creation...");
            Thread.sleep(3000);

            // Step 4: Verify with dpNames
            testDpNames();

            // Step 5: Subscribe to some datapoints with dpConnect
            JDpConnect dpConnection = testDpConnect();

            // Step 6: Subscribe with dpQueryConnect
            JDpQueryConnect queryConnection = testDpQueryConnect();

            // Wait for subscriptions to be established
            Thread.sleep(1000);

            // Step 7: Publish values to datapoints
            testDpSet();

            // Wait for hotlinks to fire
            Thread.sleep(2000);

            // Step 8: Read values with dpGet
            testDpGet();

            // Step 9: Test dpGetPeriod (historical data)
            testDpGetPeriod();

            // Step 10: Disconnect subscriptions
            WinCCOA.log("--- Disconnecting subscriptions ---");
            dpConnection.disconnect();
            queryConnection.disconnect();
            WinCCOA.log("dpConnect received " + dpConnectCount.get() + " callbacks");
            WinCCOA.log("dpQueryConnect received " + queryConnectCount.get() + " callbacks");

            // Step 11: Cleanup
            WinCCOA.log("--- Final cleanup ---");
            cleanup();

        } catch (Exception e) {
            WinCCOA.logError("Test failed with exception:");
            WinCCOA.logStackTrace(e);
        }

        // Disconnect
        oa.disconnect();

        WinCCOA.log("========================================");
        WinCCOA.log("=== WinCCOA Full API Test End ===");
        WinCCOA.log("========================================");
    }

    /**
     * Cleanup any existing test datapoints and type
     */
    private void cleanup() {
        WinCCOA.log("--- Cleanup: Deleting existing test data ---");

        // Delete all test datapoints
        String[] existingDps = oa.dpNames(TEST_DP_PREFIX + "*", TEST_TYPE_NAME);
        if (existingDps != null && existingDps.length > 0) {
            WinCCOA.log("Deleting " + existingDps.length + " existing datapoints...");
            for (String dp : existingDps) {
                oa.dpDelete(dp);
            }
            try { Thread.sleep(2000); } catch (InterruptedException e) { }
        }

        // Delete test type
        oa.dpTypeDelete(TEST_TYPE_NAME);
        try { Thread.sleep(1000); } catch (InterruptedException e) { }
    }

    /**
     * Create a datapoint type with all supported primitive data types
     */
    private void testCreateType() {
        WinCCOA.log("--- Step 1: Create Datapoint Type ---");
        WinCCOA.log("Creating type '" + TEST_TYPE_NAME + "' with all supported data types...");

        // Create root structure
        DpTypeElement root = new DpTypeElement(TEST_TYPE_NAME, 1, DpElementType.RECORD);

        // Primitive types
        DpTypeElement primitives = new DpTypeElement("primitives", 2, DpElementType.RECORD);
        primitives.addChild(new DpTypeElement("boolVal", 3, DpElementType.BIT));
        primitives.addChild(new DpTypeElement("intVal", 4, DpElementType.INT));
        primitives.addChild(new DpTypeElement("uintVal", 5, DpElementType.UINT));
        primitives.addChild(new DpTypeElement("floatVal", 6, DpElementType.FLOAT));
        primitives.addChild(new DpTypeElement("textVal", 7, DpElementType.TEXT));
        primitives.addChild(new DpTypeElement("timeVal", 8, DpElementType.TIME));
        primitives.addChild(new DpTypeElement("charVal", 9, DpElementType.CHAR));
        primitives.addChild(new DpTypeElement("bit32Val", 10, DpElementType.BIT32));
        primitives.addChild(new DpTypeElement("bit64Val", 11, DpElementType.BIT64));
        primitives.addChild(new DpTypeElement("longVal", 12, DpElementType.LONG));
        primitives.addChild(new DpTypeElement("ulongVal", 13, DpElementType.ULONG));
        root.addChild(primitives);

        // Dynamic arrays
        DpTypeElement dynArrays = new DpTypeElement("dynArrays", 20, DpElementType.RECORD);
        dynArrays.addChild(new DpTypeElement("dynInt", 21, DpElementType.DYNINT));
        dynArrays.addChild(new DpTypeElement("dynFloat", 22, DpElementType.DYNFLOAT));
        dynArrays.addChild(new DpTypeElement("dynText", 23, DpElementType.DYNTEXT));
        dynArrays.addChild(new DpTypeElement("dynBit", 24, DpElementType.DYNBIT));
        dynArrays.addChild(new DpTypeElement("dynTime", 25, DpElementType.DYNTIME));
        root.addChild(dynArrays);

        // Status/control structure (common pattern)
        DpTypeElement status = new DpTypeElement("status", 30, DpElementType.RECORD);
        status.addChild(new DpTypeElement("online", 31, DpElementType.BIT));
        status.addChild(new DpTypeElement("errorCode", 32, DpElementType.INT));
        status.addChild(new DpTypeElement("lastUpdate", 33, DpElementType.TIME));
        status.addChild(new DpTypeElement("description", 34, DpElementType.TEXT));
        root.addChild(status);

        WinCCOA.log("Type structure:");
        WinCCOA.log(root.toString());

        int result = oa.dpTypeCreate(root);
        if (result == 0) {
            WinCCOA.log("SUCCESS: Type created");
        } else {
            WinCCOA.logError("FAILED: Type creation returned " + result);
        }
    }

    /**
     * Verify type was created and read it back
     */
    private void testVerifyType() {
        WinCCOA.log("--- Step 1b: Verify Type with dpTypeNameToId and dpTypeGet ---");

        // Get type ID
        int typeId = oa.dpTypeNameToId(TEST_TYPE_NAME);
        WinCCOA.log("dpTypeNameToId('" + TEST_TYPE_NAME + "'): " + typeId);

        if (typeId > 0) {
            // Read type definition back
            DpTypeElement typeDef = oa.dpTypeGet(TEST_TYPE_NAME);
            if (typeDef != null) {
                WinCCOA.log("dpTypeGet returned type with " + typeDef.getChildCount() + " top-level elements");
                WinCCOA.log("Element paths:");
                for (String path : typeDef.getElementPaths()) {
                    WinCCOA.log("  " + path);
                }
            } else {
                WinCCOA.logError("dpTypeGet returned null");
            }
        } else {
            WinCCOA.logError("Type not found!");
        }
    }

    /**
     * Create 100 datapoints of the test type
     */
    private void testCreateDatapoints() {
        WinCCOA.log("--- Step 2: Create " + NUM_DATAPOINTS + " Datapoints ---");

        int successCount = 0;
        for (int i = 1; i <= NUM_DATAPOINTS; i++) {
            String dpName = TEST_DP_PREFIX + String.format("%03d", i);
            int result = oa.dpCreate(dpName, TEST_TYPE_NAME);
            if (result == 0) {
                successCount++;
            } else {
                WinCCOA.logError("Failed to create " + dpName + " (result=" + result + ")");
            }

            // Log progress every 20 datapoints
            if (i % 20 == 0) {
                WinCCOA.log("Created " + i + "/" + NUM_DATAPOINTS + " datapoints...");
            }
        }

        WinCCOA.log("Successfully created " + successCount + "/" + NUM_DATAPOINTS + " datapoints");
    }

    /**
     * Test dpNames to query datapoints
     */
    private void testDpNames() {
        WinCCOA.log("--- Step 3: Query Datapoints with dpNames ---");

        // Query by pattern
        String[] allDps = oa.dpNames(TEST_DP_PREFIX + "*");
        WinCCOA.log("dpNames('" + TEST_DP_PREFIX + "*'): Found " + (allDps != null ? allDps.length : 0) + " datapoints");

        // Query by pattern and type
        String[] typedDps = oa.dpNames(TEST_DP_PREFIX + "*", TEST_TYPE_NAME);
        WinCCOA.log("dpNames('" + TEST_DP_PREFIX + "*', '" + TEST_TYPE_NAME + "'): Found " +
                   (typedDps != null ? typedDps.length : 0) + " datapoints");

        // Show first 5
        if (typedDps != null && typedDps.length > 0) {
            WinCCOA.log("First 5 datapoints:");
            for (int i = 0; i < Math.min(5, typedDps.length); i++) {
                WinCCOA.log("  " + typedDps[i]);
            }
        }

        // Test dpExists
        String testDp = TEST_DP_PREFIX + "001";
        boolean exists = oa.dpExists(testDp);
        WinCCOA.log("dpExists('" + testDp + "'): " + exists);

        boolean notExists = oa.dpExists("NonExistent_DP_12345");
        WinCCOA.log("dpExists('NonExistent_DP_12345'): " + notExists);
    }

    /**
     * Test dpConnect subscription
     */
    private JDpConnect testDpConnect() {
        WinCCOA.log("--- Step 4: Subscribe with dpConnect ---");

        // Subscribe to first 10 datapoints
        var builder = oa.dpConnect();
        for (int i = 1; i <= 10; i++) {
            String dp = TEST_DP_PREFIX + String.format("%03d", i);
            builder.add(dp + ".primitives.floatVal");
            builder.add(dp + ".primitives.textVal");
        }

        JDpConnect connection = builder
            .action((IHotLink) hlg -> {
                int count = dpConnectCount.incrementAndGet();
                if (count <= 5) {  // Only log first 5 to avoid spam
                    WinCCOA.log("[dpConnect #" + count + "] Received " + hlg.size() + " items:");
                    hlg.forEach(item -> {
                        WinCCOA.log("  " + item.getDpName() + " = " + item.getVariable());
                    });
                }
            })
            .connect();

        WinCCOA.log("Subscribed to 20 elements (floatVal + textVal for 10 datapoints)");
        return connection;
    }

    /**
     * Test dpQueryConnect subscription
     */
    private JDpQueryConnect testDpQueryConnect() {
        WinCCOA.log("--- Step 5: Subscribe with dpQueryConnect ---");

        String query = "SELECT '_online.._value' FROM '" + TEST_DP_PREFIX + "*.primitives.intVal'";
        WinCCOA.log("Query: " + query);

        JDpQueryConnect connection = oa.dpQueryConnectAll(query)
            .action((IHotLink) answer -> {
                int count = queryConnectCount.incrementAndGet();
                if (count <= 5) {  // Only log first 5 to avoid spam
                    WinCCOA.log("[dpQueryConnect #" + count + "] Received " + answer.size() + " rows");
                    if (answer.size() > 0 && answer.size() <= 5) {
                        for (int i = 0; i < answer.size(); i++) {
                            var item = answer.getItem(i);
                            WinCCOA.log("  " + item.getDpName() + " = " + item.getVariable());
                        }
                    }
                }
            })
            .connect();

        WinCCOA.log("dpQueryConnect established");
        return connection;
    }

    /**
     * Test dpSet to publish values
     */
    private void testDpSet() {
        WinCCOA.log("--- Step 6: Publish values with dpSet ---");

        // Set values on all 100 datapoints
        WinCCOA.log("Setting values on " + NUM_DATAPOINTS + " datapoints...");

        for (int i = 1; i <= NUM_DATAPOINTS; i++) {
            String dp = TEST_DP_PREFIX + String.format("%03d", i);

            // Use fluent builder to set multiple values at once
            oa.dpSet()
                // Primitive types
                .add(dp + ".primitives.boolVal", i % 2 == 0)
                .add(dp + ".primitives.intVal", i * 10)
                .add(dp + ".primitives.uintVal", i * 100)
                .add(dp + ".primitives.floatVal", i * 1.5)
                .add(dp + ".primitives.textVal", "Device " + i)
                .add(dp + ".primitives.charVal", (char)('A' + (i % 26)))
                .add(dp + ".primitives.bit32Val", i)
                .add(dp + ".primitives.longVal", (long)i * 1000000L)
                // Status
                .add(dp + ".status.online", true)
                .add(dp + ".status.errorCode", 0)
                .add(dp + ".status.description", "Initialized at " + new Date())
                .send();

            // Log progress every 25 datapoints
            if (i % 25 == 0) {
                WinCCOA.log("Set values on " + i + "/" + NUM_DATAPOINTS + " datapoints...");
            }
        }

        // Also test dpSetWait (synchronous write)
        String testDp = TEST_DP_PREFIX + "001";
        int result = oa.dpSetWait(testDp + ".primitives.floatVal", 999.99);
        WinCCOA.log("dpSetWait on " + testDp + ".primitives.floatVal: result=" + result);
    }

    /**
     * Test dpGet to read values
     */
    private void testDpGet() {
        WinCCOA.log("--- Step 7: Read values with dpGet ---");

        // Single value read
        String testDp = TEST_DP_PREFIX + "001";
        Variable floatVal = oa.dpGet(testDp + ".primitives.floatVal");
        WinCCOA.log("dpGet(" + testDp + ".primitives.floatVal): " + floatVal);

        Variable textVal = oa.dpGet(testDp + ".primitives.textVal");
        WinCCOA.log("dpGet(" + testDp + ".primitives.textVal): " + textVal);

        Variable intVal = oa.dpGet(testDp + ".primitives.intVal");
        WinCCOA.log("dpGet(" + testDp + ".primitives.intVal): " + intVal);

        Variable boolVal = oa.dpGet(testDp + ".primitives.boolVal");
        WinCCOA.log("dpGet(" + testDp + ".primitives.boolVal): " + boolVal);

        // Fluent builder for multiple reads
        WinCCOA.log("Reading multiple values with fluent dpGet()...");
        oa.dpGet()
            .add(TEST_DP_PREFIX + "050.primitives.floatVal")
            .add(TEST_DP_PREFIX + "050.primitives.textVal")
            .add(TEST_DP_PREFIX + "050.status.online")
            .add(TEST_DP_PREFIX + "050.status.description")
            .action(answer -> {
                WinCCOA.log("Fluent dpGet returned " + answer.size() + " items:");
                for (int i = 0; i < answer.size(); i++) {
                    var item = answer.getItem(i);
                    WinCCOA.log("  " + item.getDpName() + " = " + item.getVariable());
                }
            })
            .await();

        // Read from multiple datapoints
        WinCCOA.log("Reading intVal from datapoints 091-100...");
        oa.dpGet()
            .add(TEST_DP_PREFIX + "091.primitives.intVal")
            .add(TEST_DP_PREFIX + "092.primitives.intVal")
            .add(TEST_DP_PREFIX + "093.primitives.intVal")
            .add(TEST_DP_PREFIX + "094.primitives.intVal")
            .add(TEST_DP_PREFIX + "095.primitives.intVal")
            .add(TEST_DP_PREFIX + "096.primitives.intVal")
            .add(TEST_DP_PREFIX + "097.primitives.intVal")
            .add(TEST_DP_PREFIX + "098.primitives.intVal")
            .add(TEST_DP_PREFIX + "099.primitives.intVal")
            .add(TEST_DP_PREFIX + "100.primitives.intVal")
            .action(answer -> {
                WinCCOA.log("Read " + answer.size() + " values:");
                for (int i = 0; i < answer.size(); i++) {
                    var item = answer.getItem(i);
                    WinCCOA.log("  " + item.getDpName() + " = " + item.getVariable());
                }
            })
            .await();
    }

    /**
     * Test dpGetPeriod for historical data
     * First we need to configure archiving, then write values, then read history
     */
    private void testDpGetPeriod() throws InterruptedException {
        WinCCOA.log("--- Step 8: Query historical data with dpGetPeriod ---");

        String testDp = TEST_DP_PREFIX + "001.primitives.floatVal";

        // Step 8a: Configure archiving for the datapoint
        // _archive.._type = 45 (DPCONFIG_DB_ARCHIVEINFO)
        // _archive.._archive = 1 (enable archiving)
        // _archive.1._class = "_NGA_G_EVENT" (archive on value change)
        WinCCOA.log("Configuring archive for " + testDp + "...");

        // Set all archive config attributes in one dpSetWait call using Map.Entry
        int result = oa.dpSetWait(List.of(
            Map.entry(testDp + ":_archive.._type", 45),
            Map.entry(testDp + ":_archive.._archive", true),
            Map.entry(testDp + ":_archive.1._type", 15),
            Map.entry(testDp + ":_archive.1._class", Variable.newDpIdentifierVar("_NGA_G_EVENT"))
        ));
        WinCCOA.log("Archive config set: result=" + result);

        // Wait for archive config to be applied
        WinCCOA.log("Waiting 1 seconds for configuration...");
        Thread.sleep(1000);

        // Step 8b: Write 10 values to generate history
        WinCCOA.log("Writing 10 values to generate history...");
        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= 10; i++) {
            double value = i * 100.0; //+ Math.random() * 10;
            result = oa.dpSetWait(testDp, value);
            WinCCOA.log("  Write #" + i + ": " + value + " (result=" + result + ")");
            Thread.sleep(500);  // Small delay between writes
        }

        long endTime = System.currentTimeMillis();

        // Wait a bit for values to be archived
        WinCCOA.log("Waiting 1 seconds for values to be archived...");
        Thread.sleep(1000);

        // Step 8c: Query historical data
        WinCCOA.log("Querying historical data from " + testDp + "...");

        oa.dpGetPeriod(startTime, endTime, 0)
            .add(testDp)
            .action(answer -> {
                WinCCOA.log("dpGetPeriod returned " + answer.size() + " values:");
                for (int i = 0; i < answer.size(); i++) {
                    var item = answer.getItem(i);
                    WinCCOA.log("  " + item.getTime() + ": " + item.getVariable());
                }
            })
            .await();
    }
}

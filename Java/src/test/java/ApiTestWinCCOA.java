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
import at.rocworks.oa4j.base.JDpHLGroup;
import at.rocworks.oa4j.var.DpElementType;
import at.rocworks.oa4j.var.DpTypeElement;
import at.rocworks.oa4j.var.Variable;

/**
 * Test class demonstrating the unified WinCCOA API.
 *
 * @author vogler
 */
public class ApiTestWinCCOA {

    public static void main(String[] args) throws Exception {
        new ApiTestWinCCOA().run(args);
    }

    public void run(String[] args) throws Exception {
        WinCCOA.log("=== WinCCOA Unified API Test Start ===");

        // Connect to WinCC OA
        WinCCOA oa = WinCCOA.connect(args);
        WinCCOA.log("Connected to WinCC OA");

        // Test status methods
        testStatus(oa);

        // Test datapoint read/write
        testDatapointReadWrite(oa);

        // Test datapoint subscription
        testDatapointSubscription(oa);

        // Test datapoint names
        testDatapointNames(oa);

        // Test type and datapoint management
        testTypeManagement(oa);

        // Disconnect
        oa.disconnect();
        WinCCOA.log("Disconnected from WinCC OA");

        WinCCOA.log("=== WinCCOA Unified API Test End ===");
    }

    private void testStatus(WinCCOA oa) {
        WinCCOA.log("--- Test: Status & Info ---");

        WinCCOA.log("Connected: " + oa.isConnected());
        WinCCOA.log("Active: " + oa.isActive());
        WinCCOA.log("Project Path: " + oa.getProjectPath());
        WinCCOA.log("Config Dir: " + oa.getConfigDir());
        WinCCOA.log("Log Dir: " + oa.getLogDir());
        WinCCOA.log("Manager Name: " + oa.getManagerName());
        WinCCOA.log("Manager Number: " + oa.getManagerNumber());
    }

    private void testDatapointReadWrite(WinCCOA oa) {
        WinCCOA.log("--- Test: Datapoint Read/Write ---");

        // Write a value
        WinCCOA.log("Writing value 42.5 to ExampleDP_Trend1.");
        oa.dpSet("ExampleDP_Trend1.", 42.5);

        // Wait a bit
        try { Thread.sleep(500); } catch (InterruptedException e) { }

        // Read it back
        Variable value = oa.dpGet("ExampleDP_Trend1.");
        WinCCOA.log("Read value: " + value);

        // Test fluent API
        WinCCOA.log("Testing fluent dpSet...");
        oa.dpSet()
            .add("ExampleDP_Trend1.", 100.0)
            .send();

        try { Thread.sleep(500); } catch (InterruptedException e) { }

        // Fluent read
        oa.dpGet()
            .add("ExampleDP_Trend1.")
            .action((answer) -> {
                WinCCOA.log("Fluent read result: " + answer.getItem(0).getVariable());
            })
            .await();
    }

    private void testDatapointSubscription(WinCCOA oa) throws InterruptedException {
        WinCCOA.log("--- Test: Datapoint Subscription ---");

        // Subscribe to changes
        var connection = oa.dpConnect()
            .add("ExampleDP_Trend1.")
            .action((JDpHLGroup hlg) -> {
                hlg.forEach(item -> {
                    WinCCOA.log("Hotlink: " + item.getDpName() + " = " + item.getVariable());
                });
            })
            .connect();

        WinCCOA.log("Subscription active, writing test values...");

        // Write some values to trigger the hotlink
        for (int i = 1; i <= 3; i++) {
            oa.dpSet("ExampleDP_Trend1.", i * 10.0);
            Thread.sleep(500);
        }

        // Disconnect the hotlink
        connection.disconnect();
        WinCCOA.log("Subscription disconnected");
    }

    private void testDatapointNames(WinCCOA oa) {
        WinCCOA.log("--- Test: Datapoint Names ---");

        String[] dps = oa.dpNames("ExampleDP_*");
        if (dps != null) {
            WinCCOA.log("Found " + dps.length + " datapoints matching 'ExampleDP_*':");
            for (int i = 0; i < Math.min(5, dps.length); i++) {
                WinCCOA.log("  " + dps[i]);
            }
            if (dps.length > 5) {
                WinCCOA.log("  ... and " + (dps.length - 5) + " more");
            }
        }

        // Test dpExists
        WinCCOA.log("dpExists('ExampleDP_Trend1'): " + oa.dpExists("ExampleDP_Trend1"));
        WinCCOA.log("dpExists('NonExistentDP'): " + oa.dpExists("NonExistentDP"));
    }

    private void testTypeManagement(WinCCOA oa) throws InterruptedException {
        WinCCOA.log("--- Test: Type & Datapoint Management ---");

        String typeName = "TestType_WinCCOA";
        String dpName = "TestDP_WinCCOA";

        // Clean up from previous runs
        oa.dpDelete(dpName);
        oa.dpTypeDelete(typeName);
        Thread.sleep(1000);

        // Create a new type
        WinCCOA.log("Creating type: " + typeName);
        DpTypeElement root = new DpTypeElement(typeName, 1, DpElementType.RECORD);
        root.addChild(new DpTypeElement("value", 2, DpElementType.FLOAT));
        root.addChild(new DpTypeElement("name", 3, DpElementType.TEXT));

        int result = oa.dpTypeCreate(root);
        WinCCOA.log("Type creation result: " + result);

        // Wait for type to be registered
        Thread.sleep(2000);

        // Verify type exists
        int typeId = oa.dpTypeNameToId(typeName);
        WinCCOA.log("Type ID: " + typeId);

        if (typeId > 0) {
            // Read type definition
            DpTypeElement typeDef = oa.dpTypeGet(typeName);
            if (typeDef != null) {
                WinCCOA.log("Type definition: " + typeDef.toString());
            }

            // Create a datapoint
            WinCCOA.log("Creating datapoint: " + dpName);
            result = oa.dpCreate(dpName, typeName);
            WinCCOA.log("Datapoint creation result: " + result);

            Thread.sleep(1000);

            // Check if it exists
            WinCCOA.log("Datapoint exists: " + oa.dpExists(dpName));

            // Write/read values
            oa.dpSet(dpName + ".value", 123.45);
            oa.dpSet(dpName + ".name", "Test");
            Thread.sleep(500);

            Variable value = oa.dpGet(dpName + ".value");
            WinCCOA.log("Read value: " + value);

            // Clean up
            WinCCOA.log("Cleaning up...");
            oa.dpDelete(dpName);
            Thread.sleep(1000);
            oa.dpTypeDelete(typeName);
        }

        WinCCOA.log("Type management test complete");
    }
}

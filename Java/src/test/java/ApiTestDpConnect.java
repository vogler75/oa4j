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
import at.rocworks.oa4j.base.JDpConnect;
import at.rocworks.oa4j.base.JDpHLGroup;
import at.rocworks.oa4j.base.JDpMsgAnswer;
import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.jni.ErrCode;
import at.rocworks.oa4j.jni.ErrPrio;

/**
 * Connects to ExampleDP_Trend1, ExampleDP_Trend2, and ExampleDP_Text datapoints.
 * Reports received values periodically (every 1 second).
 *
 * Usage: ApiTestDpConnect [-verbose]
 *   -verbose : Print detailed info for each update (default: quiet mode)
 *
 * @author vogler
 */
public class ApiTestDpConnect {

    private Integer val = 0;
    private volatile long valueCount = 0;
    private volatile long totalValueCount = 0;
    private volatile boolean verbose = false;
    private volatile long lastPrintTime = System.currentTimeMillis();

    public static void main(String[] args) throws Exception {
        // add path to WCCOAjava.dll to your path environment!
        // logs are printed to WCCOAjava<num>.0.log and WCCOAjava10.err
        JManager m = new JManager();
        m.init(args).start();
        new ApiTestDpConnect(args).run();
        m.stop();
    }

    public ApiTestDpConnect(String[] args) {
        // Parse command line arguments
        for (String arg : args) {
            if (arg.equals("-verbose")) {
                verbose = true;
            }
        }
    }

    public void run() throws InterruptedException {
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "dpConnect...");
        JDpConnect conn = JClient.dpConnect()
                .add("ExampleDP_Trend1.")
                .add("ExampleDP_Trend2.")
                .add("ExampleDP_Text.")
                .action((JDpMsgAnswer answer)->{
                    if (verbose) {
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "--- ANSWER BEG ---");
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, answer.toString());
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "--- ANSWER END ---");
                    }
                })
                .action((JDpHLGroup hotlink)->{
                    if (verbose) {
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "--- HOTLINK BEG ---");
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, hotlink.toString());
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "getItemVar(0): "+hotlink.getItemVar(0));
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "getItemVar(1): "+hotlink.getItemVar(1));
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "getItemVar(2): "+hotlink.getItemVar(2).toDouble(0.0));
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "getItemVar(2).isNull: "+hotlink.getItemVar(2).isNull());
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "getItemVar(3): "+hotlink.getItemVar(3));
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "--- HOTLINK END ---");
                    }

                    val = hotlink.getItemVar(0).toInt();
                    Double v1 = hotlink.getItemVar(0).toDouble();
                    Double v2 = hotlink.getItemVar(1).toDouble();
                    JClient.dpSet("ExampleDP_Trend3.", v1+v2).send();

                    valueCount++;
                    totalValueCount++;

                    // Print summary every 1 second
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastPrintTime >= 1000) {
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR,
                            String.format("Received %d values", valueCount));
                        valueCount = 0;
                        lastPrintTime = currentTime;
                    }
                })
                .connect();

        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "Connected. Verbose mode: " + (verbose ? "ON" : "OFF"));
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "Listening for values (endless)...");

        while (true) {
            Thread.sleep(1000);
        }

        // Uncomment below to properly disconnect (unreachable in endless mode)
        // conn.disconnect();
        // JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "end");
    }
}


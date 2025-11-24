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
import at.rocworks.oa4j.var.FloatVar;
import at.rocworks.oa4j.var.TextVar;
import at.rocworks.oa4j.jni.ErrCode;
import at.rocworks.oa4j.jni.ErrPrio;

import java.util.Random;

/**
 * Publishes values endlessly to ExampleDP_Trend1 and ExampleDP_Trend2.
 * Delay between publishes is controlled by ExampleDP_Delay datapoint.
 * Also publishes random texts to ExampleDP_Text.
 *
 * Usage: ApiTestPublishEndless [-verbose]
 *   -verbose : Print detailed info for each publish (default: quiet mode with summary)
 *
 * @author vogler
 */
public class ApiTestPublishEndless {

    private long delayMs = 1000; // Default delay in milliseconds
    private volatile boolean running = true;
    private final Random random = new Random();
    private volatile boolean verbose = false;
    private volatile long lastPrintTime = System.currentTimeMillis();
    private volatile long totalPublishCount = 0;

    // Random text templates
    private static final String[] TEXT_TEMPLATES = {
        "Temperature: %.2f°C",
        "Pressure: %.2f bar",
        "Flow rate: %.2f L/min",
        "Speed: %.2f km/h",
        "Humidity: %.2f %%",
        "Voltage: %.2f V",
        "Current: %.2f A",
        "Power: %.2f kW",
        "Efficiency: %.2f %%",
        "Runtime: %d seconds"
    };

    public static void main(String[] args) throws Exception {
        JManager m = new JManager();
        m.init(args).start();
        new ApiTestPublishEndless(args).run();
        m.stop();
    }

    public ApiTestPublishEndless(String[] args) {
        // Parse command line arguments
        for (String arg : args) {
            if (arg.equals("-verbose")) {
                verbose = true;
            }
        }
    }

    private void run() throws InterruptedException {
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "Starting endless publisher...");
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "Verbose mode: " + (verbose ? "ON" : "OFF"));

        // Get initial delay value with dpGet (gets immediate answer)
        JDpMsgAnswer initialAnswer = JClient.dpGet()
                .add("ExampleDP_Delay.")
                .await();

        if (initialAnswer.size() > 0) {
            delayMs = initialAnswer.getItem(0).getVariable().toLong(1000L);
            JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR,
                String.format("Initial delay from dpGet: %d ms", delayMs));
        }

        // Connect to ExampleDP_Delay to get updates when delay changes
        JDpConnect delayConn = JClient.dpConnect()
                .add("ExampleDP_Delay.")
                .action((JDpHLGroup hotlink) -> {
                    long newDelay = hotlink.getItemVar(0).toLong(1000L);
                    if (newDelay >= 0) {
                        delayMs = newDelay;
                        if (verbose) {
                            JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR,
                                String.format("Delay updated to: %d ms", delayMs));
                        }
                    }
                })
                .connect();

        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "Connected to ExampleDP_Delay for updates");

        // Main publishing loop
        long publishCount = 0;
        long loopCount = 0;
        long batchSize = 1;  // Sleep every N loops
        while (running) {
            try {
                // Generate random values for trends
                double trend1Value = Math.random() * 100.0;
                double trend2Value = Math.random() * 100.0;

                // Generate random text
                String randomText = generateRandomText();

                // Publish to both trends and text datapoint
                if (verbose) {
                    JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR,
                        String.format("Publishing #%d: Trend1=%.2f, Trend2=%.2f, Text=%s",
                            publishCount, trend1Value, trend2Value, randomText));
                }

                JClient.dpSet()
                        .add("ExampleDP_Trend1.", new FloatVar(trend1Value))
                        .add("ExampleDP_Trend2.", new FloatVar(trend2Value))
                        .add("ExampleDP_Text.", new TextVar(randomText))
                        .send();

                publishCount++;
                totalPublishCount++;
                loopCount++;

                // Print summary every 1 second in quiet mode
                if (!verbose) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastPrintTime >= 1000) {
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR,
                            String.format("Published %d values", publishCount));
                        publishCount = 0;
                        lastPrintTime = currentTime;
                    }
                }

                // Wait for the configured delay
                if (delayMs > 0) {
                    // For small delays (< 1ms), batch sleep calls to reduce overhead
                    if (delayMs < 1) {
                        // Calculate batch size: how many loops before we sleep?
                        batchSize = Math.max(1, (long)(1.0 / delayMs));
                        if (loopCount % batchSize == 0) {
                            Thread.sleep(1);  // Sleep 1ms every N loops
                        }
                    } else {
                        // For delays >= 1ms, sleep on every loop
                        Thread.sleep(delayMs);
                    }
                }

            } catch (InterruptedException e) {
                JManager.log(ErrPrio.PRIO_WARNING, ErrCode.NOERR,
                    "Publisher thread interrupted: " + e.getMessage());
                running = false;
                break;
            } catch (Exception e) {
                JManager.log(ErrPrio.PRIO_WARNING, ErrCode.NOERR,
                    "Error during publish: " + e.getMessage());
                Thread.sleep(1000); // Wait before retrying
            }
        }

        // Cleanup
        delayConn.disconnect();
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR,
            String.format("Publisher stopped. Total publishes: %d", totalPublishCount));
    }

    /**
     * Generate a random text value using templates
     */
    private String generateRandomText() {
        int templateIndex = random.nextInt(TEXT_TEMPLATES.length);
        String template = TEXT_TEMPLATES[templateIndex];

        if (template.contains("%d")) {
            // Integer template
            return String.format(template, (long) random.nextInt(1000));
        } else {
            // Float template
            return String.format(template, random.nextDouble() * 100.0);
        }
    }
}

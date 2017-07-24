import at.rocworks.oa4j.base.*;
import at.rocworks.oa4j.var.TimeVar;

import java.util.Date;

/**
 * Created by vogler on 01.06.2017.
 */
public class ApiTestDpGetPeriod {

    public static void main(String[] args) throws Exception {
        // add path to WCCOAjava.dll to your path environment!
        // logs are printed to WCCOAjava<num>.0.log and WCCOAjava10.err
        JManager m = new JManager();
        m.init(args).start();
        new ApiTestDpGetPeriod().run();
        JDebug.out.info("done");
        m.stop();
    }

    public void run() throws InterruptedException {
        JDebug.out.info("dpGetPeriod...");
        JClient.dpGetPeriod(new Date().getTime() - 1000 * 60, new Date().getTime(), 0)
            .add("ExampleDP_Trend1.")
            .action((JDpMsgAnswer answer) -> {
                JDebug.out.info(answer.toString());
            })
            .await();
        JDebug.out.info("dpGetPeriod...done");
    }
}
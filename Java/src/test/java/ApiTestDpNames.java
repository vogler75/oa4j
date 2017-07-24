


import at.rocworks.oa4j.base.JClient;
import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.base.JDebug;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author vogler
 */
public class ApiTestDpNames {
    public static void main(String[] args) throws Exception {
        // add path to WCCOAjava.dll to your path environment!
        // logs are printed to WCCOAjava<num>.0.log and WCCOAjava10.err         
        JManager m = new JManager();
        m.init(args).start();
        new ApiTestDpNames().run();        
        m.stop();
    }    
    
    public void run() throws InterruptedException {               
        // variant 1
        JDebug.out.info("--- DPNAMES BEG ---");
        List<String> dps1 = Arrays.asList(JClient.dpNames("ExampleDP*"));
        dps1.forEach((dp)->JDebug.out.info(dp));
        JDebug.out.info("--- DPNAMES END ---");
        
        // variant 2
        JDebug.out.info("--- DPNAMES BEG ---");
        String[] dps2 = JClient.dpNames("*.**");
        JDebug.out.log(Level.INFO, "found {0} datapoints.", dps2.length);
        JDebug.out.info("--- DPNAMES END ---");

        // variant 3 with dpType
        JDebug.out.info("--- DPNAMES BEG ---");
        String[] dps3 = JClient.dpNames("*", "ExampleDP_Float");
        JDebug.out.log(Level.INFO, "found {0} datapoints.", (dps3==null ? "no" : dps3.length));
        List<String> lst3 = Arrays.asList(dps3);
        lst3.forEach((dp)->JDebug.out.info(dp));
        JDebug.out.info("--- DPNAMES END ---");
    }              
}

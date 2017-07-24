


import at.rocworks.oa4j.base.JClient;
import at.rocworks.oa4j.base.JDpConnect;
import at.rocworks.oa4j.base.JDpHLGroup;
import at.rocworks.oa4j.base.JDpMsgAnswer;
import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.base.JDebug;
import at.rocworks.oa4j.var.DpIdentifierVar;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author vogler
 */
public class ApiTestDpConnect {    

    private Integer val = 0;

    public static void main(String[] args) throws Exception {
        // add path to WCCOAjava.dll to your path environment!
        // logs are printed to WCCOAjava<num>.0.log and WCCOAjava10.err         
        JManager m = new JManager();
        m.init(args).start();
        new ApiTestDpConnect().run();        
        m.stop();
    }
    
    public void run() throws InterruptedException {
        JDebug.out.info("dpConnect...");
        JDpConnect conn = JClient.dpConnect()
                .add("ExampleDP_Trend1.")
                .add("ExampleDP_Trend2.")
                .action((JDpMsgAnswer answer)->{
                    JDebug.out.info("--- ANSWER BEG ---");
                    JDebug.out.info(answer.toString());
                    JDebug.out.info("--- ANSWER END ---");
                })                
                .action((JDpHLGroup hotlink)->{
                    //JDebug.out.info("--- HOTLINK BEG ---");
                    //JDebug.out.info(hotlink.toString());
                    //JDebug.out.info("getItemVar(0): "+hotlink.getItemVar(0));
                    //JDebug.out.info("getItemVar(1): "+hotlink.getItemVar(1));
                    //JDebug.out.info("getItemVar(2): "+hotlink.getItemVar(2).toDouble(0.0));
                    //JDebug.out.info("getItemVar(2).isNull: "+hotlink.getItemVar(2).isNull());
                    val=hotlink.getItemVar(0).toInt();
                    Double v1 = hotlink.getItemVar(0).toDouble();
                    Double v2 = hotlink.getItemVar(1).toDouble();
                    JClient.dpSet("ExampleDP_Trend3.", v1+v2).send();
                    //JDebug.out.info("--- HOTLINK END ---");
                })
                .connect();
        
        JDebug.out.info("sleep...");
        while (val>=0)
            Thread.sleep(1000);
        JDebug.out.info("done");
        conn.disconnect();
        JDebug.out.info("end");
    }          
}


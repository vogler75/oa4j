


import at.rocworks.oa4j.base.JClient;
import at.rocworks.oa4j.base.JDpMsgAnswer;
import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.base.JDebug;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author vogler
 */
public class ApiTestDpQuery {
    public static void main(String[] args) throws Exception {
        // add path to WCCOAjava.dll to your path environment!
        // logs are printed to WCCOAjava<num>.0.log and WCCOAjava10.err         
        JManager m = new JManager();
        m.init(args).start();
        new ApiTestDpQuery().run();        
        JDebug.out.info("done");
        m.stop();
    }    
    
    public void run() throws InterruptedException {        
        JDebug.out.info("dpQuery...");
        JClient.dpQuery("SELECT '_online.._value','_online.._stime' FROM 'Test*.**'")
                .action((JDpMsgAnswer answer)->{
                    JDebug.out.info(answer.toString());
                })
                .await();
        JDebug.out.info("dpQuery...done");
    }                  
}

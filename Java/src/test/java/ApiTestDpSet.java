


import at.rocworks.oa4j.base.JClient;
import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.var.*;
import at.rocworks.oa4j.base.JDebug;
//import javafx.util.Pair;

import java.util.Arrays;
import java.util.Date;
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
public class ApiTestDpSet {
    public static void main(String[] args) throws Exception {
        // add path to WCCOAjava.dll to your path environment!
        // logs are printed to WCCOAjava<num>.0.log and WCCOAjava10.err         
        JManager m = new JManager();
        m.init(args).start();
        new ApiTestDpSet().run();
        m.stop();        
    }      
    
    private void run() throws InterruptedException {
        int ret;

        //DynVar ds = new DynVar(new TextVar("hello"), new TextVar("dynamic"), new TextVar("string"));
        //JClient.dpSet("test.dyn_string", ds);

        DynVar di = new DynVar(1.0, 2.0, 3.0);
        JClient.dpSet("test.dyn_int", di);

        JDebug.out.info("--- DPSET BEG ---");
        ret = JClient.dpSet()
                .add("System1:ExampleDP_Trend1.", new FloatVar(Math.random()))
                .add("System1:ExampleDP_SumAlert.:_original.._value", "hello world")
                .await()
                .getRetCode();
        JDebug.out.log(Level.INFO, "retCode={0}", ret);
        JDebug.out.info("--- DPSET END ---");
        
        Thread.sleep(1000);
        
        JDebug.out.info("--- DPSETTIMED BEG ---");
        Date t = new Date(new Date().getTime()+10000);
        ret = JClient.dpSet()
                .timed(t)
                .add("System1:ExampleDP_Trend1.:_original.._value", new FloatVar(Math.random()))
                .add("System1:ExampleDP_SumAlert.:_original.._value", new TextVar("hello timed world "+t.toString()))
                .await()
                .getRetCode();
        JDebug.out.log(Level.INFO, "retCode={0}", ret);
        JDebug.out.info("--- DPSETTIMED END ---");

        Thread.sleep(1000);
    }    
}

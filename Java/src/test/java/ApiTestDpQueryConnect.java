


import at.rocworks.oa4j.base.JClient;
import at.rocworks.oa4j.base.JDpHLGroup;
import at.rocworks.oa4j.base.JDpMsgAnswer;
import at.rocworks.oa4j.base.JDpQueryConnect;
import at.rocworks.oa4j.base.JDpVCItem;
import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.TimeVar;
import at.rocworks.oa4j.var.Variable;
import at.rocworks.oa4j.base.JDebug;
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
public class ApiTestDpQueryConnect {

    private class Counter {
        public volatile int value = 0;
    }    
    
    public static void main(String[] args) throws Exception {
        // add path to WCCOAjava.dll to your path environment!
        // logs are printed to WCCOAjava<num>.0.log and WCCOAjava10.err         
        JManager m = new JManager();
        m.init(args).start();
        new ApiTestDpQueryConnect().run();
        m.stop();
        
    }
    
    final Counter c = new Counter();
    Date t1 = new Date();
    Date t2 = new Date();
    
    
    public void run() throws InterruptedException {        
        JDebug.out.info("dpQueryConnect...");
        JDpQueryConnect conn = JClient.dpQueryConnectSingle("SELECT '_online.._value','_online.._stime' FROM 'Epics_*.Input'")
                .action((JDpMsgAnswer answer)->{
//                    JDebug.out.info("--- ANSWER BEG ---");
//                    JDebug.out.info(answer.toString());
//                    JDebug.out.info("--- ANSWER END ---");
                    
                })
                .action((JDpHLGroup hotlink)->{                
                    //printHotlink(hotlink);
                    printStatistics(hotlink);
                })
                .connect();
        
        JDebug.out.info("sleep...");
        Thread.sleep(1000*10);
        JDebug.out.info("done");
        conn.disconnect();
        JDebug.out.info("end");
        Thread.sleep(1000*10);        
    }              
    
    private void printStatistics(JDpHLGroup hotlink) {
        if ( hotlink.getNumberOfItems() > 0 ) {
            JDpVCItem data = hotlink.getItem(1);            
            DynVar list = (DynVar)data.getVariable();                            
            c.value+=list.size();
                
            t2=new Date();
            long ms;
            if ( (ms=t2.getTime()-t1.getTime()) >= 1000 && c.value > 0) {
                JDebug.out.log(Level.INFO, "v/s: {0}", c.value/(ms/1000));
                t1=t2;
                c.value=0;
            }
        }
    }

    private void printHotlink(JDpHLGroup hotlink) {
        JDebug.out.log(Level.INFO, "--- HOTLINK BEG --- {0}", hotlink.getNumberOfItems());
        // first item is the header, it is a dyn of the selected attributes
        if ( hotlink.getNumberOfItems() > 0 ) {
            // second item contains the result data
            JDpVCItem data = hotlink.getItem(1);
            
            // the data item is a list of list
            // row 1: dpname | column-1 | column-2 | ...
            // row 2: dpname | column-1 | column-2 | ...
            // .....
            // row n: dpname | column-1 | column-2 | ...
            DynVar list = (DynVar)data.getVariable();
            for ( int i=1; i<list.size(); i++ ) {
                // one data item in the list is also a list
                DynVar row = (DynVar)list.get(i);
                if ( row.size() == 0 ) continue;
                
                // the row contains the selected columns/values in a list
                String dp = row.get(0).toString();    // column zero is always the dp
                Variable value = row.get(1);          // column one is the first colum '_online.._value'
                TimeVar stime = (TimeVar) row.get(2); // column two is the second colum '_online.._stime'
                
                JDebug.out.log(Level.INFO, "dp={0} value={1} stime={2}", new Object[]{dp, value.toString(), stime.toString()});
            }
        }
        JDebug.out.log(Level.INFO, "--- HOTLINK END --- ");
    }
}
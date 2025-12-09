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
import at.rocworks.oa4j.base.JDpHLGroup;
import at.rocworks.oa4j.base.JDpMsgAnswer;
import at.rocworks.oa4j.base.JDpQueryConnect;
import at.rocworks.oa4j.base.JDpVCItem;
import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.var.DpIdentifierVar;
import at.rocworks.oa4j.var.DynVar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import at.rocworks.oa4j.jni.ErrCode;
import at.rocworks.oa4j.jni.ErrPrio;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author vogler
 */
public class ApiTestThreads {
    public static void main(String[] args) throws Exception {
        // add path to WCCOAjava.dll to your path environment!
        // logs are printed to WCCOAjava<num>.0.log and WCCOAjava10.err         
        JManager m = new JManager();
        m.init(args).start();
        new ApiTestThreads().run();
        m.stop();   
    }
    
    private volatile int c_dpGet=0;
    private volatile int c_dpNames=0;
    private volatile int c_dpConnect=0;
    private volatile int c_dpGetComment=0;
    
    private void run() throws InterruptedException {
        doDpQueryConnect();
        
        int delay = 0;
        
        //new Thread(()->doDpNames(delay)).start();
        
        //new Thread(()->doDpGet(delay)).start();
        //new Thread(()->doDpGet(delay)).start();
        
        //new Thread(()->doDpGetComment(delay)).start();
        int k=5;
        while ( true ) {
            JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, String.format("dpGet %s dpNames %s dpConnect %s dpGetComment %s",
                    c_dpGet/k, c_dpNames/k, c_dpConnect/k, c_dpGetComment/k));
            c_dpGet=c_dpNames=c_dpConnect=c_dpGetComment=0;
            Thread.sleep(k*1000);
        }
    }

    private void doDpGet(int delay) {
        List<Integer> x = new ArrayList<>();
        for ( int i=1; i<=100; i++ ) x.add(i);
        while ( true ) {
            x.parallelStream().forEach((i)->{
                JClient.dpGet()
                        .add("System1:Test_1_"+String.valueOf(i)+".Value:_online.._value")
                        .action((JDpMsgAnswer answer)->{
                            c_dpGet++;
                            //JDebug.out.info(answer.toString());
                        })
                        .await();
            });
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                Logger.getLogger(ApiTestThreads.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void doDpGetComment(int delay) {
        while ( true ) {
            DpIdentifierVar dpid = new DpIdentifierVar("System1:ExampleDP_Result.");
            String comment = JClient.dpGetComment(dpid).toString();
            //JDebug.out.info("Comment: "+comment);
            c_dpGetComment++;
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                Logger.getLogger(ApiTestThreads.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void doDpNames(int delay) {
        while ( true ) {
            //JDebug.out.info("--- DPNAMES BEG ---");
            List<String> dps1 = Arrays.asList(JClient.dpNames("ExampleDP*"));
            c_dpNames++;
            try {
                //dps1.forEach((dp)->JDebug.out.info(dp));
                //JDebug.out.info("--- DPNAMES END ---");
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                Logger.getLogger(ApiTestThreads.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
    }

    
    public void doDpQueryConnect() {
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "dpQueryConnect...");
        JDpQueryConnect conn = JClient.dpQueryConnectSingle("SELECT '_online.._value','_online.._stime' FROM 'Test*.**'")
                .action((JDpMsgAnswer answer)->{
//                    JDebug.out.info("--- ANSWER BEG ---");
//                    JDebug.out.info(answer.toString());
//                    JDebug.out.info("--- ANSWER END ---");
                    
                })
                .action((JDpHLGroup hotlink)->{
                    {                                                
                        //JDebug.out.log(Level.INFO, "--- HOTLINK BEG --- {0}", hotlink.getNumberOfItems());
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
                            c_dpConnect+=list.size();

//                            for ( int i=1; i<list.size(); i++ ) {
//                                // one data item in the list is also a list 
//                                DynVar row = (DynVar)list.get(i);                                
//                                if ( row.size() == 0 ) continue;
//                                
//                                // the row contains the selected columns/values in a list
//                                String dp = row.get(0).toString();    // column zero is always the dp
//                                Variable value = row.get(1);          // column one is the first colum '_online.._value'
//                                TimeVar stime = (TimeVar) row.get(2); // column two is the second colum '_online.._stime'                                      
//                                
//                                
//                                //JDebug.out.log(Level.INFO, "dp={0} value={1} stime={2}", new Object[]{dp, value.toString(), stime.toString()});
//                            }
                        }
                        //JDebug.out.log(Level.INFO, "--- HOTLINK END --- ");
                    }
                })
                .connect();         
    }
}

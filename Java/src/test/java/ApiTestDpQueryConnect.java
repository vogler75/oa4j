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
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.TimeVar;
import at.rocworks.oa4j.var.Variable;
import at.rocworks.oa4j.base.JDebug;
import java.util.Date;
import java.util.logging.Level;
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
        JDpQueryConnect conn = doDpQueryConnect();
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "sleep...");
        Thread.sleep(1000*60*60*24);
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "done");
        conn.disconnect();
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "end");
        Thread.sleep(1000*10);        
    }

    private JDpQueryConnect doDpQueryConnect() {
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "dpQueryConnect...");
        JDpQueryConnect conn = JClient.dpQueryConnectSingle("SELECT '_online.._value','_online.._stime' FROM 'Test_*.**'");
        conn.action((JDpMsgAnswer answer)->{
                try {
                    JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "--- ANSWER BEG --- "+answer.getErrorText());
                    Date tt1 = conn.getInitTimestamp();
                    Date tt2 = conn.getDoneTimestamp();
                    Double t = (tt2.getTime() - tt1.getTime()) / 1000.0;
                    JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "t= " + t);
                    if (answer.size() > 1) {
                        int count = answer.getItem(1).getVariable().getDynVar().size();
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "v/s=" + count / t);
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "err=" + answer.getErrorCode() + ": " + answer.getErrorText());
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "items=" + count);
                        //JDebug.out.info(answer.toString());
                    }
                    JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "--- ANSWER END ---");
                } catch (Exception ex) {
                    JManager.stackTrace(ErrPrio.PRIO_SEVERE, ErrCode.UNEXPECTEDSTATE, ex);
                }
                })
                .action((JDpHLGroup hotlink)->{
                    //printHotlink(hotlink);
                    printStatistics(hotlink);
                })
                .connect();
        return conn;
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
                
                JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, String.format("dp=%s value=%s stime=%s", dp, value.toString(), stime.toString()));
            }
        }
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "--- HOTLINK END --- ");
    }
}
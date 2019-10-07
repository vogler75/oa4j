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
import at.rocworks.oa4j.base.JDpMsgAnswer;
import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.var.Variable;
import at.rocworks.oa4j.var.VariablePtr;
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
public class ApiTestDpGet {
    public static void main(String[] args) throws Exception {
        // add path to WCCOAjava.dll to your path environment!
        // logs are printed to WCCOAjava<num>.0.log and WCCOAjava10.err
        JManager m = new JManager();
        m.init(args).start();
        new ApiTestDpGet().run();
        m.stop();        
    }      
    
    private void run() throws InterruptedException {
        JDebug.out.info("--- DPGET XXX ---");
        JClient.dpGet()
                .add("HMI_Tag_1.:_online.._value")
                .add("HMI_Tag_Text.:_online.._value")
                .action((JDpMsgAnswer answer)->{
                    JDebug.out.info("--- ANSWER BEG ---");
                    JDebug.out.info(answer.toString());
                    JDebug.out.info("--- ANSWER END ---");
                })
                .await();
        JDebug.out.info("--- DPGET END ---");

        /*
        JDebug.out.info("--- DPGET BEG ---");
        JDpMsgAnswer answer = JClient.dpGet()
                .add("HMI_Tag_1.:_online.._value")
                .add("HMI_Tag_2.:_online.._value")
                .await();
        JDebug.out.log(Level.INFO, "ret={0}", answer.getRetCode());
        answer.forEach((vc)->JDebug.out.info(vc.toString()));
        JDebug.out.log(Level.INFO, "toDouble:");
        try {
            answer.forEach((vc) -> JDebug.out.info(vc.getVariable().toDouble().toString()));
        } catch ( Exception ex ) {
            JDebug.StackTrace(Level.INFO, ex);
        }
        JDebug.out.info("--- DPGET END ---");

        JDebug.out.info("--- DPGET BEG ---");
        VariablePtr res1 = new VariablePtr();
        VariablePtr res2 = new VariablePtr();
        JClient.dpGet()
                .add("HMI_Tag_1.:_online.._value", res1)
                .add("HMI_Tag_2.:_online.._value", res2)
                .await();
        JDebug.out.log(Level.INFO, "res1 is a {0} value={1}", new Object[] {res1.get().isA(), res1.get()});
        JDebug.out.log(Level.INFO, "res2 is a {0} value={1}", new Object[] {res2.get().isA(), res2.get()});
        JDebug.out.info("--- DPGET END ---");

        JDebug.out.info("--- DPGET BEG ARRAY ---");
        List<Variable> res3 = JClient.dpGet(Arrays.asList("HMI_Tag_1.", "HMI_Tag_1."));
        res3.forEach((v)->JDebug.out.log(Level.INFO, "res3 is a {0} value={1}", new Object[] {v.isA(), v.formatValue()}));
        JDebug.out.info("--- DPGET END ARRAY ---");
        */

//        int i=0;
//        String oldc="", newc="";
//        while ( true ) {
//            JDpId dpid = new JDpId("System1:ExampleDP_Result.");
//            newc=JClient.dpGetComment(dpid).toString();
//            if ( !newc.equals(oldc) && ++i % 1000 == 0 ) {
//                JDebug.out.log(Level.INFO, "comment: {0} {1}", new Object[] {i, newc});
//                oldc=newc;
//            }
//        }
        //Thread.sleep(10000);


    }


}

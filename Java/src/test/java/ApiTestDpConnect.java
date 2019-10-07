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
                .add("HMI_Tag_1.")
                .action((JDpMsgAnswer answer)->{
                    JDebug.out.info("--- ANSWER BEG ---");
                    JDebug.out.info(answer.toString());
                    JDebug.out.info("--- ANSWER END ---");
                })                
                .action((JDpHLGroup hotlink)->{
                    //JDebug.out.info("--- HOTLINK BEG ---");
                    val=hotlink.getItemVar(0).toInt();
                    /*if (val%10000==0)*/ JDebug.out.info(val.toString());
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


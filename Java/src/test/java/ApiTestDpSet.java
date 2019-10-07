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

        //DynVar di = new DynVar(1.0, 2.0, 3.0);
        //JClient.dpSet("test.dyn_int", di);


        for (int j=1; j<=10; j++) {
            for (int i = 1; i <= 200000; i++) {
                int val = (int) Math.round(Math.random() * 1000);
                //JDebug.out.info("value="+val);
                ret = JClient.dpSet().add("HMI_Tag_2.", new IntegerVar(val)).send().getRetCode();
                //Thread.sleep(1000);
            }

            JDebug.out.info("wait...");
            Thread.sleep(10000);
            JDebug.out.info("continue...");
        }

        JDebug.out.info("done.");

        /*
        JDebug.out.info("--- Tag_Text ... ---");
        ret = JClient.dpSet().add("HMI_Tag_Text.", new TextVar("Hallo du da")).await().getRetCode();
        JDebug.out.info("--- Tag_Text Done ---");
        */

        /*
        for (int i=1; i<=100000; i++) {
            //JDebug.out.info("--- DPSET BEG ---");
            ret = JClient.dpSet()
                    .add("HMI_Tag_1.", new IntegerVar(i))
                    //.add("HMI_Tag_Text.", "hello "+i)
                    .send()
                    .getRetCode();
            //JDebug.out.log(Level.INFO, "retCode={0}", ret);
            //JDebug.out.info("--- DPSET END ---");

            //Thread.sleep(10);
        }
        JDebug.out.info("--- LOOP END ---");
        Thread.sleep(1000);
        ret = JClient.dpSet()
                .add("HMI_Tag_1.", new IntegerVar(-1))
                .await()
                .getRetCode();
        JDebug.out.info("--- END END ---");
        */

        /*
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
        */
        //Thread.sleep(10000);
    }    
}

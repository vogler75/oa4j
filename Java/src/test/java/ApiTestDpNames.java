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
        // variant 2
        JDebug.out.info("--- DPNAMES BEG ---");
        String[] dps2 = JClient.dpNames("*.**");
        JDebug.out.log(Level.INFO, "found {0} datapoints.", dps2.length);
        JDebug.out.info("--- DPNAMES END ---");

        // variant 1
        JDebug.out.info("--- DPNAMES BEG ---");
        List<String> dps1 = Arrays.asList(JClient.dpNames("HMI_Tag*"));
        dps1.forEach((dp)->JDebug.out.info(dp));
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

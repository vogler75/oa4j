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
import at.rocworks.oa4j.base.*;
import at.rocworks.oa4j.var.TimeVar;

import java.util.Date;
import at.rocworks.oa4j.jni.ErrCode;
import at.rocworks.oa4j.jni.ErrPrio;

/**
 *
 * @author vogler
 */
public class ApiTestDpGetPeriod {

    public static void main(String[] args) throws Exception {
        // add path to WCCOAjava.dll to your path environment!
        // logs are printed to WCCOAjava<num>.0.log and WCCOAjava10.err
        JManager m = new JManager();
        m.init(args).start();
        new ApiTestDpGetPeriod().run();
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "done");
        m.stop();
    }

    public void run() throws InterruptedException {
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "dpGetPeriod...");
        JClient.dpGetPeriod(new Date().getTime() - 1000 * 60, new Date().getTime(), 0)
            .add("ExampleDP_Trend1.")
            .action((JDpMsgAnswer answer) -> {
                JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, answer.toString());
            })
            .await();
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "dpGetPeriod...done");
    }
}
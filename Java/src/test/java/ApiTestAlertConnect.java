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
import at.rocworks.oa4j.base.JAlertConnect;
import at.rocworks.oa4j.base.JClient;
import at.rocworks.oa4j.base.JDpHLGroup;
import at.rocworks.oa4j.base.JDpVCItem;
import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.var.Variable;
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
public class ApiTestAlertConnect {
    public static void main(String[] args) throws Exception {
        // add path to WCCOAjava.dll to your path environment!
        // logs are printed to WCCOAjava<num>.0.log and WCCOAjava10.err         
        JManager m = new JManager();
        m.init(args).start();
        new ApiTestAlertConnect().run();        
        m.stop();
    }
    
    public void run() throws InterruptedException {        
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "alertConnect...");
        
        JAlertConnect conn = JClient.alertConnect()
                .add(":_alert_hdl.._system_time")
                .add(":_alert_hdl.._abbr")
                .add(":_alert_hdl.._ack_state")
                .add(":_alert_hdl.._ack_time")
                .add(":_alert_hdl.._ack_type" )
                .add(":_alert_hdl.._ack_user")
                .add(":_alert_hdl.._ackable")
                .add(":_alert_hdl.._add_values")
                .add(":_alert_hdl.._alert_color")
                .add(":_alert_hdl.._alert_font_style" )
                .add(":_alert_hdl.._alert_fore_color" )
                .add(":_alert_hdl.._archive")
                .add(":_alert_hdl.._came_time")
                .add(":_alert_hdl.._came_time_idx")
                .add(":_alert_hdl.._class" )
                .add(":_alert_hdl.._comment")
                .add(":_alert_hdl.._dest" )
                .add(":_alert_hdl.._dest_text")
                .add(":_alert_hdl.._direction" )
                .add(":_alert_hdl.._gone_time")
                .add(":_alert_hdl.._gone_time_idx")
                .add(":_alert_hdl.._inact_ack" )
                .add(":_alert_hdl.._panel" )
                .add(":_alert_hdl.._partner" )                
                .add(":_alert_hdl.._partn_idx" )
                .add(":_alert_hdl.._prior")
                .add(":_alert_hdl.._single_ack" )
                .add(":_alert_hdl.._text")
                .add(":_alert_hdl.._value")
                .add(":_alert_hdl.._visible" )
                .action((JDpHLGroup hotlink) -> {
                    JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "--- HOTLINK BEG ---");
                    hotlink.getItems().forEach((JDpVCItem vc)->{
                        Variable var = vc.getVariable();
                        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, String.format("%s: %s [%s]", vc.getDpName(), var.formatValue(), var.isA()));
                    });
                    JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "--- HOTLINK END ---");
                })
                .connect();
        
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "sleep...");
        Thread.sleep(1000*60*60);
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "done");
        conn.disconnect();
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "end");
    }              
}

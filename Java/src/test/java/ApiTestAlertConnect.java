


import at.rocworks.oa4j.base.JAlertConnect;
import at.rocworks.oa4j.base.JClient;
import at.rocworks.oa4j.base.JDpHLGroup;
import at.rocworks.oa4j.base.JDpVCItem;
import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.var.Variable;
import at.rocworks.oa4j.base.JDebug;
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
        JDebug.out.info("alertConnect...");
        
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
                    JDebug.out.info("--- HOTLINK BEG ---");
                    hotlink.getItems().forEach((JDpVCItem vc)->{
                        Variable var = vc.getVariable();
                        JDebug.out.log(Level.INFO, "{0}: {1} [{2}]", new Object[]{vc.getDpName(), var.formatValue(), var.isA()});
                    });
                    //JDebug.out.info(hotlink.toString());
                    JDebug.out.info("--- HOTLINK END ---");
                })
                .connect();
        
        JDebug.out.info("sleep...");
        Thread.sleep(1000*60*60);
        JDebug.out.info("done");
        conn.disconnect();
        JDebug.out.info("end");
    }              
}

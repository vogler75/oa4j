/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.logger;

import at.rocworks.oa4j.base.JClient;
import at.rocworks.oa4j.base.JDpHLGroup;
import at.rocworks.oa4j.base.JDpVCItem;
import at.rocworks.oa4j.var.Bit64Var;
import at.rocworks.oa4j.var.DpIdentifierVar;
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.IntegerVar;
import at.rocworks.oa4j.var.TimeVar;
import at.rocworks.oa4j.var.Variable;

import at.rocworks.oa4j.logger.base.IDataCollector;
import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.logger.data.types.api.AlertItemAPI;
import at.rocworks.oa4j.logger.data.types.api.EventItemAPI;
import at.rocworks.oa4j.base.JDebug;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */
public class DataWellAPI {
    private final int bulksize;
    private DataList bulk;    

    public DataWellAPI(int bulksize) {
        this.bulksize=bulksize;
        this.bulk=(bulksize>0) ? new DataList(bulksize) : null;                
    }
    
    public void queryConnect(String query, IDataCollector collector, IDatapointFilter filter) {
        String sql = "SELECT '_online.._value','_online.._stime','_online.._status64','_online.._manager','_online.._user' " + query;
        JDebug.out.log(Level.INFO, "WinCC OA Query: {0}", new Object[]{sql});
        int ret = JClient.dpQueryConnectSingle(sql)
                .action((JDpHLGroup hotlink) -> {
//                    JDebug.out.info(hotlink.toString());
                    if (hotlink.getNumberOfItems() > 1) {
                        JDpVCItem item = hotlink.getItem(1);
                        DynVar res = (DynVar) item.getVariable();
                        for (int i = 1; i < res.size(); i++) {
                            DynVar val = (DynVar) res.get(i);
                            if (val.size() == 0) 
                                continue;                            
                            
                            String dp = val.get(0).toString();                            
                            if (!filter.isArchived(new DpIdentifierVar(dp))) {
                                continue;
                            }
                                                        
                            Variable value = val.get(1);
                            if ( value == null ) // 17.07.2016: a new created datapoint element is NULL (3.13)
                                continue;                                                                                   
                            
                            TimeVar stime = (TimeVar) val.get(2);

                            Bit64Var status = (Bit64Var) val.get(3);
                            IntegerVar manager = (IntegerVar) val.get(4);
                            IntegerVar user = (IntegerVar) val.get(5);
                            
                            EventItemAPI event = new EventItemAPI(
                                    dp, value, stime.getTime(),
                                    status.getValue(),
                                    manager.getValue(),
                                    user.getValue()); 

//                            EventItemAPI event = new EventItemAPI(
//                                    dp, value, stime.getTime());

//                            JDebug.out.info("event="+event.toJSONObject());

//                            if (event.getDp().getFQN().equals("System1:LoggerQuery."))
//                                JDebug.out.info(event.toJSONObject().toJSONString());

                            if ( bulk == null ) {
                                collector.collectData(event);
                            } else {
                                bulk.addItem(event);
                                if (bulk.isFull()) {
                                    collector.collectData(bulk);
                                    bulk = new DataList(bulksize);
                                }
                            }
                        }
                        if (bulk != null && !bulk.isEmpty()) {
                            collector.collectData(bulk);
                            bulk = new DataList(bulksize);                           
                        }
                    }
                }).async(true).connect().getRetCode();
        JDebug.out.log(Level.INFO, "WinCC OA Query: ret={0}", ret);
    }    

    public void alertConnect(IDataCollector collector) {
        JDebug.out.log(Level.INFO, "WinCC OA Alert Connect...");
        int ret = JClient.alertConnect()
                .add(":_alert_hdl.._system_time") // 0
                .add(":_alert_hdl.._abbr") // 1
                .add(":_alert_hdl.._ack_state") // 2
                .add(":_alert_hdl.._ack_time") // 3
                .add(":_alert_hdl.._ack_type") // 4
                .add(":_alert_hdl.._ack_user") // 5
                .add(":_alert_hdl.._ackable") // 6
                .add(":_alert_hdl.._add_values") // 7
                .add(":_alert_hdl.._alert_color") // 8
                .add(":_alert_hdl.._alert_font_style") // 9
                .add(":_alert_hdl.._alert_fore_color") // 10
                .add(":_alert_hdl.._archive") // 11
                .add(":_alert_hdl.._came_time") // 12
                .add(":_alert_hdl.._came_time_idx") // 13
                .add(":_alert_hdl.._class") // 14
                .add(":_alert_hdl.._comment") // 15
                .add(":_alert_hdl.._dest") // 16
                .add(":_alert_hdl.._dest_text") // 17
                .add(":_alert_hdl.._direction") // 18
                .add(":_alert_hdl.._gone_time") // 19
                .add(":_alert_hdl.._gone_time_idx") // 20
                .add(":_alert_hdl.._inact_ack") // 21
                .add(":_alert_hdl.._panel") // 22
                .add(":_alert_hdl.._partner") // 23
                .add(":_alert_hdl.._partn_idx") // 24
                .add(":_alert_hdl.._prior") // 25
                .add(":_alert_hdl.._single_ack") // 26
                .add(":_alert_hdl.._text") // 27
                .add(":_alert_hdl.._value") // 28
                .add(":_alert_hdl.._visible") // 29
                .action((JDpHLGroup hotlink) -> {
                    JDebug.out.fine("--- ALERT BEG ---");
                    ArrayList<JDpVCItem> items = hotlink.getItems();
                    
                    // JDebug Begin
                    items.forEach((JDpVCItem vc) -> {
                        Variable var = vc.getVariable();
                        JDebug.out.log(Level.FINE, "{0}: {1} [{2}]", new Object[]{vc.getDpName(), var.formatValue(), var.isA()});
                    });
                    // JDebug End

                    AlertItemAPI alert = new AlertItemAPI(
                            new Dp(items.get(0).getDpName()), 
                            items.get(0).getVariable().getTimeVar(), 
                            items.get(1).getVariable().getLangTextVar(), 
                            items.get(2).getVariable().getIntegerVar(), 
                            items.get(3).getVariable().getTimeVar(), 
                            items.get(4).getVariable().getIntegerVar(), 
                            items.get(5).getVariable().getUIntegerVar(),
                            items.get(6).getVariable().getBitVar(),
                            items.get(7).getVariable().getDynVar(),
                            items.get(8).getVariable().getTextVar(),
                            items.get(9).getVariable().getTextVar(),
                            items.get(10).getVariable().getTextVar(),
                            items.get(11).getVariable().getBitVar(),
                            items.get(12).getVariable().getTimeVar(),
                            items.get(13).getVariable().getIntegerVar(),
                            items.get(14).getVariable().getDpIdentifierVar(),
                            items.get(15).getVariable().getTextVar(),
                            items.get(16).getVariable().getIntegerVar(),
                            items.get(17).getVariable().getLangTextVar(),
                            items.get(18).getVariable().getBitVar(),
                            items.get(19).getVariable().getTimeVar(),
                            items.get(20).getVariable().getIntegerVar(),
                            items.get(21).getVariable().getBitVar(),
                            items.get(22).getVariable().getTextVar(),
                            items.get(23).getVariable().getTimeVar(),
                            items.get(24).getVariable().getIntegerVar(),
                            items.get(25).getVariable().getCharVar(),
                            items.get(26).getVariable().getBitVar(),
                            items.get(27).getVariable().getLangTextVar(),
                            items.get(28).getVariable(),
                            items.get(29).getVariable().getBitVar()                            
                    );
                    
                    DataList list = new DataList(1);
                    list.addItem(alert);
                    collector.collectData(list);
                    
                    //JDebug.out.info(hotlink.toString());
                    JDebug.out.fine("--- ALERT END ---");
                })
                .connect().getRetCode();
        JDebug.out.log(Level.INFO, "WinCC OA Alert Connect: ret={0}", ret);
    }    
}

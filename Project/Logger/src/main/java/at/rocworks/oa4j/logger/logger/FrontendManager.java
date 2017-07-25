/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.logger;

import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.jni.DpMsg;
import at.rocworks.oa4j.jni.SysMsg;
import at.rocworks.oa4j.logger.base.IDataCollector;
import at.rocworks.oa4j.base.JDebug;

import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */
public class FrontendManager extends JManager {
    private IDataCollector collector;
    private DataWellFilter archives;
    private DataWellAPI well;
    
    //private DataList bulk;
    
    public FrontendManager() {
        super();
    }        
    
    public void start(IDataCollector collector, DataWellFilter archives, int bulksize) {
        this.collector=collector;    
        this.archives=archives;
        this.well=new DataWellAPI(bulksize);
        start();
        well.setActive(isActive());
    }

    @Override
    protected void becameActive() {
        well.setActive(true);
        super.becameActive();
    }

    @Override
    protected void becamePassive() {
        well.setActive(false);
        super.becamePassive();
    }

    @Override
    public void start() {
        super.start();
    }
            
    @Override
    public boolean doReceiveSysMsg(long cPtrSysMsg) { 
        SysMsg msg = new SysMsg(cPtrSysMsg);
//        JDebug.out.log(Level.INFO, "isA => {0} getSysMsgType => {1}", new Object[] {msg.isA(), msg.getSysMsgType()});
//        JDebug.out.info(msg.toDebug(99));
        
        switch ( msg.getSysMsgType()) {
            case INIT_SYS_MSG: 
                //JDebug.out.info("--- INIT_SYS_MSG ---");
                break;
            case END_DP_INIT: 
                //JDebug.out.info("--- END_DP_INIT ---");
                break;
        }        
        return super.doReceiveSysMsg(cPtrSysMsg);
    }        
    
   @Override
    public boolean doReceiveDpMsg(long cPtrDpMsg) {
        DpMsg msg = new DpMsg(cPtrDpMsg);
//        JDebug.out.log(Level.INFO, "isA => {0}", msg.isA());
//        JDebug.out.info(msg.toDebug(99));
        try {
            switch ( msg.isA() ) {        
                case DP_MSG_ASYNCH_REQUEST:
                case DP_MSG_PERIOD_REQUEST: {
                    JDebug.out.info("--- DP_MSG_ASYNC/PERIOD_REQUEST ---");
                    JDebug.out.info(msg.toString());
                    return true;
                }                
                case DP_MSG_ALERT_TIME_REQUEST:
                case DP_MSG_ALERT_PERIOD_REQUEST: {
                    JDebug.out.info("--- DP_MSG_ALERT_TIME/PERIOD_REQUEST ---");
                    JDebug.out.info(msg.toString());
                    //throw new UnsupportedOperationException("Not supported yet."); 
                    return false;                    
                }
                case DP_MSG_FILTER_REQUEST: {
                    JDebug.out.info("--- DP_MSG_FILTER_REQUEST ---");
                    JDebug.out.info(msg.toString());
                    //throw new UnsupportedOperationException("Not supported yet."); 
                    return false;
                }                
                case DP_MSG_HOTLINK: {
                    JDebug.out.info("--- DP_MSG_HOTLINK ---");
                    JDebug.out.info(msg.toString());
                    return true;
                }
                case DP_MSG_FILTER_HL: { // dpQueryConnects
                    return false;
                }
                case DP_MSG_COMPLEX_VC: {
                    JDebug.out.info("--- DP_MSG_COMPLEX_VC ---");
                    JDebug.out.info(msg.toString());
                    return true;
                }                                
                case DP_MSG_ALERT_HL: {
                    JDebug.out.info("--- DP_MSG_ALERT_HL ---");
                    JDebug.out.info(msg.toString());
                    return true;
                }                        
                case DP_MSG_ALERT_VC: {
                    JDebug.out.info("--- DP_MSG_ALERT_VC ---");
                    JDebug.out.info(msg.toString());
                    return true;
                }
                
                default: 
                    //JDebug.out.info("--- MESSAGE DEFAULT ---");
                    //JDebug.out.log(Level.INFO, "isA => {0}", msg.isA());
                    //JDebug.out.info(msg.toDebug(99));
                    return false;
            }
        } catch (Exception ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return false;
        }
    }                 
    
    public void alertConnect() {
        well.alertConnect(collector);
    }
    
    public void queryConnect(String query) {
        well.queryConnect(query, collector, archives);
    }            
}

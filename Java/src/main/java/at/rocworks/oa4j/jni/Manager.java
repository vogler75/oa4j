/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.jni;

import at.rocworks.oa4j.base.JDpVCItem;
import at.rocworks.oa4j.base.JHotLinkWaitForAnswer;
import at.rocworks.oa4j.var.DpIdentifierVar;
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.LangTextVar;
import at.rocworks.oa4j.var.TimeVar;
import at.rocworks.oa4j.var.Variable;

/**
 *
 * @author vogler
 */
public abstract class Manager {
    public native String apiGetLogPath();
    public native String apiGetDataPath();
    public native String apiGetConfigValue(String key);
    
    public int apiStartup(int manType, String[] argv) { return apiStartup(manType, argv, true, true); }
    public native int apiStartup(int manType, String[] argv, boolean connectToData, boolean connectToEvent);
    public native int apiShutdown();
    
    public native void apiDispatch(int sec, int usec);    
    
    public native int apiDpGet(JHotLinkWaitForAnswer hdl, DpIdentifierVar[] dps);
    public native int apiDpSet(JHotLinkWaitForAnswer hdl, JDpVCItem[] dps);
    public native int apiDpSetTimed(JHotLinkWaitForAnswer hdl, TimeVar originTime, JDpVCItem[] dps);
    public native int apiDpQuery(JHotLinkWaitForAnswer hdl, String query);
    public native int apiDpGetPeriod(JHotLinkWaitForAnswer hdl, TimeVar start, TimeVar stop, int num, DpIdentifierVar[] dps);

    public native int apiDpConnect(JHotLinkWaitForAnswer hdl, String dp);
    public native int apiDpDisconnect(JHotLinkWaitForAnswer hdl, String dp);
    
    public native int apiDpConnectArray(JHotLinkWaitForAnswer hdl, String[] dps);    
    public native int apiDpDisconnectArray(JHotLinkWaitForAnswer hdl, String[] dps);    
    
    public native int apiDpQueryConnectSingle(JHotLinkWaitForAnswer hdl, boolean values, String query);
    public native int apiDpQueryConnectAll(JHotLinkWaitForAnswer hdl, boolean values, String query);
    public native int apiDpQueryDisonnect(JHotLinkWaitForAnswer hdl);    
    
    public native int apiAlertConnect(JHotLinkWaitForAnswer hdl, String[] dps);
    public native int apiAlertDisconnect(JHotLinkWaitForAnswer hdl, String[] dps);
    
    public native String[] apiGetIdSet(String pattern);    
    public native String[] apiGetIdSetOfType(String pattern, String type);    
    
    public native LangTextVar apiDpGetComment(DpIdentifierVar dp);
    
    public native void apiDoReceiveSysMsg(long cPtrSysMsg);
    public native void apiDoReceiveDpMsg(long cPtrDpMsg);
    
    public native int apiSendArchivedDPs(DynVar elements, boolean isAlert);
    
    private native void apiSetManagerState(int state);
    public void apiSetManagerState(ManagerState state) { apiSetManagerState(state.value); }
       
    // callbacks from API   
    public abstract boolean doReceiveSysMsg(long cPtrSysMsg);    
    public abstract boolean doReceiveDpMsg(long cPtrDpMsg);
    
    public int callbackAnswer(int id, int idx) {    
        return callbackAnswer(id, idx, null, null);
    }
    abstract public int callbackAnswer(int id, int idx, DpIdentifierVar dpid, Variable var);    
    
    public int callbackHotlink(int id, int idx) {
        return callbackHotlink(id, idx, null, null);
    }
    abstract public int callbackHotlink(int id, int idx, DpIdentifierVar dpid, Variable var);  
    
    public native int apiProcessHotlinkGroup(int id, long ptrDpHlGroup);
    public abstract int callbackHotlinkGroup(int id, long ptrDpHlGroup);

    public native int apiGetConnectionState();
    public native int apiIsActiveConnection();
}

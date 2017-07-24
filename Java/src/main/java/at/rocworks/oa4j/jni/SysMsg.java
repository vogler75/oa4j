/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.jni;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author vogler
 */
public class SysMsg extends Msg {
    public SysMsg() {
        super();
    }
    
    public SysMsg(long cptr) {
        super(cptr);
    }   
    
    private native int getSysMsgType(long cptr);
    private native int getSysMsgSubType(long cptr);

    /**
     * get the SysMsgType
     * @return SysMsgType of message
     */
    public SysMsgType getSysMsgType() {
        return SysMsgType.values()[getSysMsgType(cptr)];
    }

    /**
     * get the SysMsgRedundancySubType if the message is of type REDUNDANCY_SYS_MSG
     * @return SysMsgRedundancySubType of message or null if it is not of type REDUNDANCY_SYS_MSG
     */
    public SysMsgRedundancySubType getSysMsgRedundancySubType() {
        if (getSysMsgType() == SysMsgType.REDUNDANCY_SYS_MSG)
            return SysMsgRedundancySubType.values()[getSysMsgSubType(cptr)];
        else
            return SysMsgRedundancySubType.NONE;
    }

    /**
     * get the data of the InitSysMsg if the message is a InitSysMsg
     * @return InitSysMsg data (key, value)
     */
    public native Map<String, String> getInitSysMsgData();
}

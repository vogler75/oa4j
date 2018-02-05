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

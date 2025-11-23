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
import at.rocworks.oa4j.base.JDebug;
import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.jni.DpMsg;
import at.rocworks.oa4j.jni.DpMsgManipDp;

import java.util.logging.Level;
import at.rocworks.oa4j.jni.ErrCode;
import at.rocworks.oa4j.jni.ErrPrio;

/**
 *
 * @author vogler
 */
public class ApiTestManager extends JManager {

    @Override
    public boolean doReceiveDpMsg(long cPtrDpMsg) {
        DpMsg msg = new DpMsg(cPtrDpMsg);
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "------------DPMSG DEBUG BEGIN-------------------------");
        JDebug.out.log(Level.INFO, "isA => {0}", msg.getType());
        JDebug.out.info(msg.toDebug(99));
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "------------DPMSG DEBUG END  -------------------------");
        try {
            if (msg.getType()== msg.getMsgTypes().DP_MSG_MANIP_DP()) {
                DpMsgManipDp x = new DpMsgManipDp(msg);
                JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "isDeleteDpMsg: " + x.isDeleteDpMsg());
                JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "DpId: " + x.getDpId());
                JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "DpName: " + x.getDpName());
                JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "DpTypeId: " + x.getDpTypeId());
                JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "DpTypeName: " + x.getDpTypeName());
            } else if (msg.getType() == msg.getMsgTypes().DP_MSG_IDENTIFICATION()) {
                JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "It's a DP_MSG message...");
            }
        } catch (Exception ex) {
            JManager.stackTrace(ErrPrio.PRIO_SEVERE, ErrCode.UNEXPECTEDSTATE, ex);
            return false;
        }
        return false;
    }

    @Override
    protected void becameActive() {
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "becameActive");
        super.becameActive();
    }

    @Override
    protected void becamePassive() {
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "becamePassive");
        super.becamePassive();
    }

    public static void main(String[] args) throws Exception {
        // add path to WCCOAjava.dll to your path environment!
        // logs are printed to WCCOAjava<num>.0.log and WCCOAjava10.err
        JManager m = new ApiTestManager();
        m.init(args).start();
        m.setDebugOutput();
        while (true) {
            JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "isActive=" + m.isActive());
            Thread.sleep(3000);
        }
        //m.stop();
    }
}
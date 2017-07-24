import at.rocworks.oa4j.base.JDebug;
import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.jni.DpMsg;
import at.rocworks.oa4j.jni.DpMsgManipDp;

import java.util.logging.Level;

/**
 * Created by vogler on 4/27/2017.
 */
public class ApiTestManager extends JManager {

    @Override
    public boolean doReceiveDpMsg(long cPtrDpMsg) {
        DpMsg msg = new DpMsg(cPtrDpMsg);
        JDebug.out.info("------------DPMSG DEBUG BEGIN-------------------------");
        JDebug.out.log(Level.INFO, "isA => {0}", msg.isA());
        JDebug.out.info(msg.toDebug(99));
        JDebug.out.info("------------DPMSG DEBUG END  -------------------------");
        try {
            switch (msg.isA()) {
                case DP_MSG_MANIP_DP: {
                    DpMsgManipDp x = new DpMsgManipDp(msg);
                    JDebug.out.info("isDeleteDpMsg: " + x.isDeleteDpMsg());
                    JDebug.out.info("DpId: " + x.getDpId());
                    JDebug.out.info("DpName: " + x.getDpName());
                    JDebug.out.info("DpTypeId: " + x.getDpTypeId());
                    JDebug.out.info("DpTypeName: " + x.getDpTypeName());
                }
            }
        } catch (Exception ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return false;
        }
        return false;
    }

    @Override
    protected void becameActive() {
        JDebug.out.info("becameActive");
        super.becameActive();
    }

    @Override
    protected void becamePassive() {
        JDebug.out.info("becamePassive");
        super.becamePassive();
    }

    public static void main(String[] args) throws Exception {
        // add path to WCCOAjava.dll to your path environment!
        // logs are printed to WCCOAjava<num>.0.log and WCCOAjava10.err
        JManager m = new ApiTestManager();
        m.init(args).start();
        m.setDebugOutput();
        while (true) {
            JDebug.out.info("isActive=" + m.isActive());
            Thread.sleep(3000);
        }
        //m.stop();
    }
}
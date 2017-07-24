package at.rocworks.oa4j.jni;

import at.rocworks.oa4j.var.DpIdentifierVar;

/**
 * Created by vogler on 4/27/2017.
 */
public class DpMsgManipDp extends Msg {
    public DpMsgManipDp() {
        super();
    }

    public DpMsgManipDp(long cptr) {
        super.setPointer(cptr);
    }

    public DpMsgManipDp(Msg msg) {
        super.setPointer(msg.getPointer());
    }

    public native boolean isDeleteDpMsg();
    public native String getDpName();
    public native DpIdentifierVar getDpId();
    public native int getDpTypeId();
    public native String getDpTypeName();

    @Override
    public native String toString();

    @Override
    public native String toDebug(int level);
}

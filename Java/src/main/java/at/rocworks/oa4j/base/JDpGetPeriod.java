package at.rocworks.oa4j.base;

import at.rocworks.oa4j.var.DpIdentifierVar;
import at.rocworks.oa4j.var.TimeVar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vogler on 01.06.2017.
 */
public class JDpGetPeriod extends JHotLinkWaitForAnswer {
    private TimeVar tStart, tStop;
    private int num;
    private List<DpIdentifierVar> dps;
    private boolean sent = false;

    public JDpGetPeriod(TimeVar tStart, TimeVar tStop, int num) {
        super();
        this.tStart=tStart;
        this.tStop=tStop;
        this.num=num;
        this.dps=new ArrayList<>();
    }

    public JDpGetPeriod add(String dp) {
        dps.add(new DpIdentifierVar(dp, "_offline.._value"));
        return this;
    }

    public JDpGetPeriod add(DpIdentifierVar dpid) {
        dps.add(dpid);
        return this;
    }

    public JDpGetPeriod async() {
        this.setAsync();
        return this;
    }

    public JDpGetPeriod send() {
        this.sent = true;
        JManager.getInstance().enqueueHotlink(this);
        return this;
    }

    public JDpMsgAnswer await() {
        if (!sent) this.send();
        this.waitForAnswer();
        this.sent = false;
        return this.getAnswer();
    }

    @Override
    public JDpGetPeriod action(IAnswer answer) {
        super.action(answer);
        return this;
    }

    @Override
    protected int execute() {
        int ret;
        DpIdentifierVar[] arr = new DpIdentifierVar[dps.size()];
        for (int i=0; i<dps.size(); i++) arr[i]=dps.get(i);
        if ((ret = JManager.getInstance().apiDpGetPeriod(this, tStart, tStop, num, arr)) == 0)
            this.register();
        return ret;
    }

    @Override
    protected void answer(JDpMsgAnswer answer) {
        this.deregister();
        super.answer(answer);
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.base;

import at.rocworks.oa4j.var.*;
//import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author vogler
 */
public class JClient {
    
    public static boolean isConnected() {
        return JManager.getInstance()!=null && JManager.getInstance().isConnected();
    }

    // dpGet
    
    public static JDpGet dpGet() {
        return (new JDpGet());
    }

    public static Variable dpGet(String dp) {
        JDpMsgAnswer answer = (new JDpGet()).add(dp).await();
        return ( answer.size()>0 ) ? answer.getItem(0).getVariable() : null;
    }

    public static int dpGet(String dp, VariablePtr var) {
        JDpMsgAnswer answer = (new JDpGet()).add(dp).await();
        if ( answer.size()>0 ) var.set(answer.getItem(0).getVariable());
        return answer.getRetCode();
    }

    public static List<Variable> dpGet(List<String> dps) {
        JDpGet dpGet = new JDpGet();
        dps.forEach((dp)->dpGet.add(dp));
        JDpMsgAnswer answer = dpGet.await();
        List<Variable> ret = new ArrayList<Variable>(answer.size());
        answer.forEach((item)->ret.add(item.getVariable()));
        return ret;
    }

    // dpGetPeriod
    public static JDpGetPeriod dpGetPeriod(TimeVar start, TimeVar stop, int num) {
        return (new JDpGetPeriod(start, stop, num));
    }

    public static JDpGetPeriod dpGetPeriod(Date start, Date stop, int num) {
        return dpGetPeriod(new TimeVar(start), new TimeVar(stop), num);
    }

    public static JDpGetPeriod dpGetPeriod(Long start, long stop, int num) {
        return dpGetPeriod(new TimeVar(start), new TimeVar(stop), num);
    }

    // dpSet
    
    public static JDpSet dpSet() {
        return (new JDpSet());
    }

    public static JDpSet dpSet(String dp, Object var) {
        return (new JDpSet()).add(dp, Variable.newVariable(var)).send();
    }

    //public static JDpSet dpSet(List<Pair<String, Object>> vals) {
    //    JDpSet dpSet = new JDpSet();
    //    vals.forEach((val)->dpSet.add(val.getKey(), Variable.newVariable(val.getValue())));
    //    return dpSet.send();
    //}

    public static int dpSetWait(String dp, Object var) {
        return dpSet(dp, var).await().getRetCode();
    }

    // dpConnect
            
    public static JDpConnect dpConnect() {
        return (new JDpConnect());
    }
    
    // alertConnect
            
    public static JAlertConnect alertConnect() {
        return (new JAlertConnect());
    }    
    
    // dpQuery
    
    public static JDpQuery dpQuery(String query) {
        return (new JDpQuery(query).send());
    }

    // dpQueryConnect
    
    public static JDpQueryConnect dpQueryConnectSingle(String query) {
        return (new JDpQueryConnectSingle(query));
    }
    
    public static JDpQueryConnect dpQueryConnectAll(String query) {
        return (new JDpQueryConnectAll(query));
    }

    // dpNames
    
    public static String[] dpNames(String pattern) {
        return (String[])JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiGetIdSet(pattern);
        });
    }
    
    public static String[] dpNames(String pattern, String type) {
        return (String[])JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiGetIdSetOfType(pattern, type);
        });
    }    
    
    // dpComment   
    
    public static LangTextVar dpGetComment(DpIdentifierVar dpid) {
        return (LangTextVar)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiDpGetComment(dpid);
        });
    }
}

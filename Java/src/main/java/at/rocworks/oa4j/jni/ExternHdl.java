/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.jni;

import java.io.IOException;
import at.rocworks.oa4j.base.JDebug;
import at.rocworks.oa4j.var.Variable;

/**
 *
 * @author vogler
 */
public class ExternHdl {
      
    public static native String apiGetLogDir();
    public static native int apiGetManType();        
    public static native int apiGetManNum();
    public static native int apiAddResult(long cptr, Variable result);

    /**
     * Start Control Function
     * @param cptr WaitCondPtr
     * @param name Functionname
     * @param args Arguments
     * @return EXEC_OK=0, EXEC_ERROR=1, EXEC_DONE=2
     */
    public static native int apiStartFunc(long cptr, String name, Variable args);

    public static void init() throws IOException {
        String name;
        int type = apiGetManType();
        switch (type) {
            case 4: name="ui"; break;
            case 5: name="ctrl"; break;
            case 7: name="api"; break;
            default: name="java";
        }
        JDebug.setOutput(apiGetLogDir(), "WCCOA"+name+apiGetManNum()+"java");
        //JDebug.out.info("java ExternHdl init done. ");
    }            
}
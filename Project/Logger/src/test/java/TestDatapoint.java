
import at.rocworks.oa4j.logger.data.Dp;

import java.net.URL;
import java.net.URLClassLoader;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author vogler
 */
public class TestDatapoint {
    public static void main(String[] args)  {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader)cl).getURLs();
        for(URL url: urls) if ( !url.getFile().startsWith("/C:/Program%20Files")) System.out.println(url.getFile().substring(1));

        test(new Dp("System1:ExampleDP_Arg1.:_online.._value"));
        test(new Dp("System1:ExampleDP_Arg1.Node:_alert_hdl.2._system_time"));
        test(new Dp("System1:ExampleDP_Arg1.Node.Leaf:_alert_hdl.2._system_time"));
        test(new Dp("System1:ExampleDP_Arg1.Node.Leaf"));
        
    }
    
    public static void test(Dp dp) {
        System.out.println("-------------------------------------------------");
        System.out.println("toString: "+dp.toString());        
        System.out.println("getFQN: "+dp.getFQN());
        
        System.out.println("getSystem: "+dp.getSystem());
        System.out.println("getSysDp: "+dp.getSysDp());
        System.out.println("getSysDpEl: "+dp.getSysDpEl());
        
        System.out.println("getDp: "+dp.getDp());
        System.out.println("getElement: "+dp.getElement());
        System.out.println("getConfig: "+dp.getConfig());
        System.out.println("getAttribute: "+dp.getAttribute());                        
       
        System.out.println("getDetail: "+dp.getDetail());
    }
    
}

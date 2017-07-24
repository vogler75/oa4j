/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.zeromq;

/**
 *
 * @author vogler
 */
public interface IZMQCallback {    
    public Object message(String topic, Object message);
}

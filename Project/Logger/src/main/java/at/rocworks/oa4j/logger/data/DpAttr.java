/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.data;

/**
 *
 * @author vogler
 */
public enum DpAttr {
    Value(1),
    Stime(2),
    Status(3),
    Manager(4),
    User(5),
    Status64(6),
    SystemTime(7), // _alert_hdl
    Unknown(99);
    DpAttr(int n) {
        value = n;
    }
    public final int value;
}

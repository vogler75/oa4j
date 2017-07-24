/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.base;

/**
 *
 * @author vogler
 */
public enum JDpAttrAddrDirection {
    UNDEFINED(0),
    OUTPUT(1),
    INPUT_SPONT(2),
    INPUT_SQUERY(3),
    INPUT_POLL(4),
    OUTPUT_SINGLE(5),
    IO_SPONT(6),
    IO_POLL(7),
    IO_SQUERY(8),
    AM_ALERT(9),
    INPUT_ON_DEMAND(10),
    INPUT_CYCLIC_ON_USE(11),
    IO_ON_DEMAND(12),
    IO_CYCLIC_ON_USE(13),
    INTERNAL(32),
    LOW_LEVEL_FLAG(64);

    JDpAttrAddrDirection(int n) {
        value = n;
    }

    public final int value;
}

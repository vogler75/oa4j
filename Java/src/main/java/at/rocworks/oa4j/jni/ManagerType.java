/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.jni;

/**
 *
 * @author vogler
 */
public enum ManagerType {
    NO_MAN(0),
    EVENT_MAN(1),
    DRIVER_MAN(2),
    DB_MAN(3),
    UI_MAN(4),
    CTRL_MAN(5),
    ASCII_MAN(6),
    API_MAN(7),
    DEVICE_MAN(8),
    REDU_MAN(9),
    REPORT_MAN(10),
    DDE_MAN(11),
    DIST_MAN(12);

    ManagerType(int value) {
        this.value = value;
    }
    public final int value;
}

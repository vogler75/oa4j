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
public enum SysMsgType {
    NO_SYS_MSG(1),
    KEEP_ALIVE(2),
    INIT_SYS_MSG(3),
    START_DP_INIT(4),
    END_DP_INIT(5),
    START_MANAGER(6),
    SHUT_DOWN_MANAGER(7),
    NAMESERVER_SYS_MSG(8),
    OPEN_SERVER(9),
    CLOSE_SERVER(10),
    LICENSE_SYS_MSG(11),
    RECOVERY_SYS_MSG(12),
    REDUNDANCY_SYS_MSG(13),
    DIST_SYS_MSG(14),
    FILETRANSFER_SYS_MSG(15),
    MAX_SYS_MSG(16);

    SysMsgType(int value) {
        this.value = value;
    }
    public int value;       
}

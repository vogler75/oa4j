package at.rocworks.oa4j.jni;

/**
 * Created by vogler on 7/22/2017.
 */
public enum SysMsgRedundancySubType {
    NONE(0),

    REDUNDANCY_ACTIVE(1),     // Manager goes active
    REDUNDANCY_PASSIVE(2),        // Manager goes passive
    REDUNDANCY_REFRESH(3),        // Tell manager to refresh the connects to origSource
    REDUNDANCY_DISCONNECT(4),     // Tell event to clear all connects to origSource
    DM_START_TOUCHING(5),         // Tell data to start touching
    DM_STOP_TOUCHING(6),          // Tell data to stop touching
    REDUNDANCY_ACTIVE_REQ(7),     // Initiate Redu-State-Switch
    REDUNDANCY_PASSIVE_REQ(8);     // Initiate Redu-State-Switch

    SysMsgRedundancySubType(int value) {
        this.value = value;
    }
    public int value;
}

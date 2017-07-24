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
public enum ManagerState {
    /** (0). The manager has been started and is trying to connect to DM */
    STATE_JUST_STARTED(0),
    /** (1). The manager has connection to DM and is receiving init data */
    STATE_INIT(1),
    /** (2). Init from database finished. Manager is now ready to finish
      manager specific initialisation and to connect to event.
     */
    STATE_ADJUST(2),
    /** (3). Connection to EM established. The manager is now ready for operation
     */
    STATE_RUNNING(3);

    ManagerState(int value) {
        this.value = value;
    }
    public int value;       
}

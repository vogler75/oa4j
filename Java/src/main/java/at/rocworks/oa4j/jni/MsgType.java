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
public enum MsgType {
    PVSS_MSG(0),
    NO_MSG(1),
    /// system specific message
    SYS_MSG(2),
    /// start dpinit sequence sys message
    SYS_MSG_START_DPINIT(3),
    /// name server sys message @see NameServerSysMsg
    SYS_MSG_NAMESERVER(4),
    /// license information sys message
    SYS_MSG_LICENSE(5),
    /// initialization of manager 
    SYS_MSG_INIT(6),
    /// transfer / sync of files
    SYS_MSG_FILE_TRANSFER(7),
    /// common datapoint message @see DpMsg
    DP_MSG(8),
    /// config initialisation message
    DP_MSG_INITCONFIG(9),
    /// the identification message
    DP_MSG_IDENTIFICATION(10),
    /// the type container message
    DP_MSG_TYPECONTAINER(11),
    /// request to create a datapoint type
    DP_MSG_DPTYPE_REQ(12),
    /// notification of a new / deleted datapoint type
    DP_MSG_MANIP_DPTYPE(13),
    /// request to create a datapoint
    DP_MSG_DP_REQ(14),
    //DP_MSG_REQ_NEW_DP = DP_MSG_DP_REQ,
    /// notification of a new / deleted datapoint
    DP_MSG_MANIP_DP(15),
    //DP_MSG_CMD_NEWDEL_DP = DP_MSG_MANIP_DP,
    /// request to create / change / delete an alias or comment
    DP_MSG_DPALIAS_REQ(16),
    /// notification of a new / changed / deleted alias or comment
    DP_MSG_MANIP_DPALIAS(17),
    /// generic connection type message
    DP_MSG_CONNECTION(18),
    /// connect message
    DP_MSG_CONNECT(19),
    /// connect message without answer
    DP_MSG_CONNECT_RET(20),
    /// connect message; no hotlink if this manager changed the values 
    DP_MSG_CONNECT_NOSOURCE(21),
    /// disconnect message
    DP_MSG_DISCONNECT(22),
    /// hotlink message
    DP_MSG_HOTLINK(23),
    /// simple value change message
    DP_MSG_VALUECHANGE(24),
    /// optimized driver value change message
    DP_MSG_DRIVER_VC(25),
    /// complex value change message
    DP_MSG_COMPLEX_VC(26),
    /// common request message
    DP_MSG_REQUEST(27),
    /// common answer message
    DP_MSG_ANSWER(28),
    /// request value 
    DP_MSG_SIMPLE_REQUEST(29),
    /// request value in the past
    DP_MSG_ASYNCH_REQUEST(30),
    /// request value 
    DP_MSG_SYNCH_REQUEST(31),
    /// request values of a timerange
    DP_MSG_PERIOD_REQUEST(32),
    // base class DpMsgAlert, see below. 
    // It is at the end so the IDs won't change now. 
    // DP_MSG_ALERT,
    /// change alert values
    DP_MSG_ALERT_VC(33),
    /// connect to alert values
    DP_MSG_ALERT_CONNECT(34),
    /// connect to visible alert values
    DP_MSG_ALERT_CONNECT_VISIBLE(35),
    //DP_MSG_ALERT_CONN_RET_VISIBLE = DP_MSG_ALERT_CONNECT_VISIBLE,
    /// disconnect from alert values
    DP_MSG_ALERT_DISCONNECT(36),
    //DP_MSG_ALERT_DISCONN = DP_MSG_ALERT_DISCONNECT,
    /// notification messagse for alert values
    DP_MSG_ALERT_HL(37),
    /// request alert values of a timerange
    DP_MSG_ALERT_TIME_REQUEST(38),
    //DP_MSG_ALERT_TIME_REQU = DP_MSG_ALERT_TIME_REQUEST,
    /// request alert values of a timerange
    DP_MSG_ALERT_PERIOD_REQUEST(39),
    //DP_MSG_ALERT_PERIOD_REQU = DP_MSG_ALERT_PERIOD_REQUEST,
    /// query message
    DP_MSG_FILTER_REQUEST(40),
    /// query connect message
    DP_MSG_FILTER_CONNECT(41),
    /// query disconnect message
    DP_MSG_FILTER_DISCONNECT(42),
    //DP_MSG_FILTER_DISCONN = DP_MSG_FILTER_DISCONNECT,
    /// query hotlink message
    DP_MSG_FILTER_HL(43),
    /// base class DpMsgAlert
    DP_MSG_ALERT(44),
    /// disconnect all
    DP_MSG_DISCONNECT_ALL(45),
    /// FT LIBS-13b-8 message splitting, abort a outstanding Request (dpQuery, dpGetPeriod, alertGetPeriod)
    DP_MSG_ABORT_REQUEST(46),
    /// request to create / delete a CNS object (view, tree, node, ...)
    DP_MSG_CNS_REQ(47),
    /// notification of a new / deleted CNS object (view, tree, node, ...)
    DP_MSG_MANIP_CNS(48),
    // FT COMDRV-V13-2 dpGetMaxAge functionality
    DP_MSG_MAXAGE_REQUEST(49);

    MsgType(int value) {
        this.value = value;
    }
    public int value;   
}

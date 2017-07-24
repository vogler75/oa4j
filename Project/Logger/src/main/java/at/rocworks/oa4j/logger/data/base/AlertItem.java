/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.data.base;

import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.var.BitVar;
import at.rocworks.oa4j.var.CharVar;
import at.rocworks.oa4j.var.DpIdentifierVar;
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.IntegerVar;
import at.rocworks.oa4j.var.LangTextVar;
import at.rocworks.oa4j.var.TextVar;
import at.rocworks.oa4j.var.TimeVar;
import at.rocworks.oa4j.var.UIntegerVar;
import at.rocworks.oa4j.var.Variable;
import org.json.simple.JSONObject;

/**
 *
 * @author vogler
 */
public abstract class AlertItem extends DataItem {

    public AlertItem(Dp dp) {
        super(dp);
    }
    public abstract TimeVar getSystemTime();
    public abstract LangTextVar getAbbr();
    public abstract IntegerVar getAckState();
    public abstract TimeVar getAckTime();
    public abstract IntegerVar getAckType();
    public abstract UIntegerVar getAckUser();
    public abstract BitVar getAckable();
    public abstract DynVar getAddValues();
    public abstract TextVar getAlertColor();
    public abstract TextVar getAlertFontStyle();
    public abstract TextVar getAlertForeColor();
    public abstract BitVar getArchive();
    public abstract TimeVar getCameTime();
    public abstract IntegerVar getCameTimeIdx();
    public abstract DpIdentifierVar getAlertClass();
    public abstract TextVar getComment();
    public abstract IntegerVar getDest();
    public abstract LangTextVar getDestText();
    public abstract BitVar getDirection();
    public abstract TimeVar getGoneTime();
    public abstract IntegerVar getGoneTimeIdx();
    public abstract BitVar getInactAck();
    public abstract TextVar getPanel();
    public abstract TimeVar getPartner();
    public abstract IntegerVar getPartnIdx();
    public abstract CharVar getPrior();
    public abstract BitVar getSingleAck();
    public abstract LangTextVar getText();
    public abstract BitVar getVisible();
    public abstract Variable getValue();
    
    @Override
    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        JSONObject ret = new JSONObject();
        ret.put("Data", super.toJSONObject());
        JSONObject obj = new JSONObject();      
        ret.put("Alert", obj);        
        obj.put("SystemTime", getSystemTime());
        obj.put("Abbr", getAbbr());
        obj.put("AckState", getAckState());
        obj.put("AckTime", getAckTime());
        obj.put("AckType", getAckType());
        obj.put("AckUser", getAckUser());
        obj.put("Ackable", getAckable());
        obj.put("AddValues", getAddValues());
        obj.put("AlertColor", getAlertColor());
        obj.put("AlertFontStyle", getAlertFontStyle());
        obj.put("AlertForeColor", getAlertForeColor());
        obj.put("Archive", getArchive());
        obj.put("CameTime", getCameTime());
        obj.put("CameTimeIdx", getCameTimeIdx());
        obj.put("AlertClass", getAlertClass());
        obj.put("Comment", getComment());
        obj.put("Dest", getDest());
        obj.put("DestText", getDestText());
        obj.put("Direction", getDirection());
        obj.put("GoneTime", getGoneTime());
        obj.put("GoneTimeIdx", getGoneTimeIdx());
        obj.put("InactAck", getInactAck());
        obj.put("Panel", getPanel());
        obj.put("Partner", getPartner());
        obj.put("PartnIdx", getPartnIdx());
        obj.put("Prior", getPrior());
        obj.put("SingleAck", getSingleAck());
        obj.put("Text", getText());
        obj.put("Value", getValue());
        obj.put("Visible", getVisible()); 
        return ret;
    }
}

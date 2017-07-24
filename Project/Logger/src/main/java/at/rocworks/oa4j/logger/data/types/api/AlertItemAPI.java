/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.data.types.api;

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
import at.rocworks.oa4j.logger.data.base.AlertItem;

import java.io.Serializable;

/**
 *
 * @author vogler
 */
public class AlertItemAPI extends AlertItem implements Serializable {

    private final TimeVar varSystemTime;
    private final LangTextVar varAbbr;
    private final IntegerVar varAckState;
    private final TimeVar varAckTime;
    private final IntegerVar varAckType;
    private final UIntegerVar varAckUser;
    private final BitVar varAckable;
    private final DynVar varAddValues;
    private final TextVar varAlertColor;
    private final TextVar varAlertFontStyle;
    private final TextVar varAlertForeColor;
    private final BitVar varArchive;
    private final TimeVar varCameTime;
    private final IntegerVar varCameTimeIdx;
    private final DpIdentifierVar varAlertClass;
    private final TextVar varComment;
    private final IntegerVar varDest;
    private final LangTextVar varDestText;
    private final BitVar varDirection;
    private final TimeVar varGoneTime;
    private final IntegerVar varGoneTimeIdx;
    private final BitVar varInactAck;
    private final TextVar varPanel;
    private final TimeVar varPartner;
    private final IntegerVar varPartnIdx;
    private final CharVar varPrior;
    private final BitVar varSingleAck;
    private final LangTextVar varText;
    private final Variable varValue;
    private final BitVar varVisible;

    public AlertItemAPI(
            Dp dp,
            TimeVar varSystemTime,
            LangTextVar varAbbr,
            IntegerVar varAckState,
            TimeVar varAckTime,
            IntegerVar varAckType,
            UIntegerVar varAckUser,
            BitVar varAckable,
            DynVar varAddValues,
            TextVar varAlertColor,
            TextVar varAlertFontStyle,
            TextVar varAlertForeColor,
            BitVar varArchive,
            TimeVar varCameTime,
            IntegerVar varCameTimeIdx,
            DpIdentifierVar varAlertClass,
            TextVar varComment,
            IntegerVar varDest,
            LangTextVar varDestText,
            BitVar varDirection,
            TimeVar varGoneTime,
            IntegerVar varGoneTimeIdx,
            BitVar varInactAck,
            TextVar varPanel,
            TimeVar varPartner,
            IntegerVar varPartnIdx,
            CharVar varPrior,
            BitVar varSingleAck,
            LangTextVar varText,
            Variable varValue,
            BitVar varVisible) {
        super(dp);
        this.varSystemTime = varSystemTime;
        this.varAbbr = varAbbr;
        this.varAckState = varAckState;
        this.varAckTime = varAckTime;
        this.varAckType = varAckType;
        this.varAckUser = varAckUser;
        this.varAckable = varAckable;
        this.varAddValues = varAddValues;
        this.varAlertColor = varAlertColor;
        this.varAlertFontStyle = varAlertFontStyle;
        this.varAlertForeColor = varAlertForeColor;
        this.varArchive = varArchive;
        this.varCameTime = varCameTime;
        this.varCameTimeIdx = varCameTimeIdx;
        this.varAlertClass = varAlertClass;
        this.varComment = varComment;
        this.varDest = varDest;
        this.varDestText = varDestText;
        this.varDirection = varDirection;
        this.varGoneTime = varGoneTime;
        this.varGoneTimeIdx = varGoneTimeIdx;
        this.varInactAck = varInactAck;
        this.varPanel = varPanel;
        this.varPartner = varPartner;
        this.varPartnIdx = varPartnIdx;
        this.varPrior = varPrior;
        this.varSingleAck = varSingleAck;
        this.varText = varText;
        this.varValue = varValue;
        this.varVisible = varVisible;        
    }   
    
    /**
     * @return the varSystemTime
     */
    @Override
    public TimeVar getSystemTime() {
        return varSystemTime;
    }

    /**
     * @return the varAbbr
     */
    @Override
    public LangTextVar getAbbr() {
        return varAbbr;
    }

    /**
     * @return the varAckState
     */
    @Override
    public IntegerVar getAckState() {
        return varAckState;
    }

    /**
     * @return the varAckTime
     */
    @Override
    public TimeVar getAckTime() {
        return varAckTime;
    }

    /**
     * @return the varAckType
     */
    @Override
    public IntegerVar getAckType() {
        return varAckType;
    }

    /**
     * @return the varAckUser
     */
    @Override
    public UIntegerVar getAckUser() {
        return varAckUser;
    }

    /**
     * @return the varAckable
     */
    @Override
    public BitVar getAckable() {
        return varAckable;
    }

    /**
     * @return the varAddValues
     */
    @Override
    public DynVar getAddValues() {
        return varAddValues;
    }

    /**
     * @return the varAlertColor
     */
    @Override
    public TextVar getAlertColor() {
        return varAlertColor;
    }

    /**
     * @return the varAlertFontStyle
     */
    @Override
    public TextVar getAlertFontStyle() {
        return varAlertFontStyle;
    }

    /**
     * @return the varAlertForeColor
     */
    @Override
    public TextVar getAlertForeColor() {
        return varAlertForeColor;
    }

    /**
     * @return the varArchive
     */
    @Override
    public BitVar getArchive() {
        return varArchive;
    }

    /**
     * @return the varCameTime
     */
    @Override
    public TimeVar getCameTime() {
        return varCameTime;
    }

    /**
     * @return the varCameTimeIdx
     */
    @Override
    public IntegerVar getCameTimeIdx() {
        return varCameTimeIdx;
    }

    /**
     * @return the varAlertClass
     */
    @Override
    public DpIdentifierVar getAlertClass() {
        return varAlertClass;
    }

    /**
     * @return the varComment
     */
    @Override
    public TextVar getComment() {
        return varComment;
    }

    /**
     * @return the varDest
     */
    @Override
    public IntegerVar getDest() {
        return varDest;
    }

    /**
     * @return the varDestText
     */
    @Override
    public LangTextVar getDestText() {
        return varDestText;
    }

    /**
     * @return the varDirection
     */
    @Override
    public BitVar getDirection() {
        return varDirection;
    }

    /**
     * @return the varGoneTime
     */
    @Override
    public TimeVar getGoneTime() {
        return varGoneTime;
    }

    /**
     * @return the varGoneTimeIdx
     */
    @Override
    public IntegerVar getGoneTimeIdx() {
        return varGoneTimeIdx;
    }

    /**
     * @return the varInactAck
     */
    @Override
    public BitVar getInactAck() {
        return varInactAck;
    }

    /**
     * @return the varPanel
     */
    @Override
    public TextVar getPanel() {
        return varPanel;
    }

    /**
     * @return the varPartner
     */
    @Override
    public TimeVar getPartner() {
        return varPartner;
    }

    /**
     * @return the varPartnIdx
     */
    @Override
    public IntegerVar getPartnIdx() {
        return varPartnIdx;
    }

    /**
     * @return the varPrior
     */
    @Override
    public CharVar getPrior() {
        return varPrior;
    }

    /**
     * @return the varSingleAck
     */
    @Override
    public BitVar getSingleAck() {
        return varSingleAck;
    }

    /**
     * @return the varText
     */
    @Override
    public LangTextVar getText() {
        return varText;
    }

    /**
     * @return the varValue
     */
    @Override
    public Variable getValue() {
        return varValue;
    }

    /**
     * @return the varVisible
     */
    @Override
    public BitVar getVisible() {
        return varVisible;
    }

    @Override
    public long getTimeMS() {
        return getSystemTime().getTime();
    }

    @Override
    public long getTimeNS() {
        return getTimeMS()*1000000;
    }
}

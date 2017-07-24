/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.data.types.protobuf;

import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.logger.data.base.EventItem;
import at.rocworks.oa4j.logger.data.base.ValueItem;
import at.rocworks.oa4j.base.JDebug;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */
public class EventItemProtobuf extends EventItem {
    DpeValueProtos.DpeValueList.DpeValue item;
    
    public static final String FMT_TIME = "yyyy.MM.dd HH:mm:ss.SSS"; 
    private final SimpleDateFormat fmt = new SimpleDateFormat(FMT_TIME);    
    
    public EventItemProtobuf(DpeValueProtos.DpeValueList.DpeValue item) {
        super(new Dp(item.getDpName()));
        this.item=item;
    }

    @Override
    public ValueItem getValue() {
        return new ValueItemProtobuf(item);
    }

    @Override
    public boolean hasAttributes() {
        return item.hasOriginalManager() && item.hasOriginalStatus64() && item.hasOriginalUser();
    }

    @Override
    public long getStatus() {
        return item.getOriginalStatus64();
    }

    @Override
    public int getManager() {
        return item.getOriginalManager();        
    }

    @Override
    public int getUser() {
        return item.getOriginalUser();
    }

    @Override
    public long getTimeMS() {
        try {
            return (fmt.parse(item.getOriginalTime())).getTime();
        } catch (ParseException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return 0;
        }
    }

    @Override
    public long getTimeNS() {
        return getTimeMS()*1000;
    }
}

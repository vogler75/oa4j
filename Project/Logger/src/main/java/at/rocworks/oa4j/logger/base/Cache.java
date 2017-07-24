/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.base;

import at.rocworks.oa4j.logger.data.base.DataItem;
import at.rocworks.oa4j.base.JDebug;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */
public class Cache {
    private final ConcurrentHashMap<String, CacheItem> cache = new ConcurrentHashMap<>();

    private CacheItem getCacheItem(String key) {
        CacheItem item;
        if (!cache.containsKey(key)) {
            cache.put(key, item = new CacheItem());
        } else {
            item = cache.get(key);
        }
        return item;
    }

    private String getKeyOfEvent(DataItem item) {
        return item.getDp().getFQN();
    }

    public void setThreadNr(DataItem item, int threadNr) {
        getCacheItem(getKeyOfEvent(item)).threadNr = threadNr;
    }

    public int getThreadNr(DataItem item) {
        return getCacheItem(getKeyOfEvent(item)).threadNr;
    }
    
    public void setTimeMS(DataItem item) {
        CacheItem c = getCacheItem(getKeyOfEvent(item));
        
//        if (item.getDp().getFQN().equals("System1:LoggerQuery."))
//            JDebug.out.info(c.timeMS+"/"+c.addNanos+" => " +item.getTimeMS()+"/"+item.getAddedNanos());

        //if (item.getTimeNS()%1000000==0 && c.timeMS==item.getTimeMS()) { 
        if (c.timeMS==item.getTimeMS() && c.nanos==item.getNanos()) { 
            item.setAddedNanos(c.addNanos+=1000);
            JDebug.out.log(Level.FINER, "added nano! {0} {1} {2} {3} c={4} {5}",
                    new Object[]{item.getDp().toString(), item.getTimeNS(), item.getNanos(), item.getAddedNanos(), c.timeMS, c.addNanos});            
        } else {
            c.timeMS=item.getTimeMS();   
            c.nanos=item.getNanos();
            c.addNanos=0;
        }
    }
}

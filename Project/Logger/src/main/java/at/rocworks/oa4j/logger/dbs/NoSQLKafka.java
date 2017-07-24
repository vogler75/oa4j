package at.rocworks.oa4j.logger.dbs;

import at.rocworks.oa4j.base.JDebug;

import at.rocworks.oa4j.logger.data.base.AlertItem;

import at.rocworks.oa4j.logger.base.INoSQLInterface;
import at.rocworks.oa4j.logger.base.NoSQLServer;
import at.rocworks.oa4j.logger.base.NoSQLSettings;
import at.rocworks.oa4j.logger.data.base.DataItem;
import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.logger.data.base.EventItem;
import at.rocworks.oa4j.logger.query.DpGetPeriodResult;

import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 *
 * @author vogler
 */
public class NoSQLKafka extends NoSQLServer {

    private final KafkaProducer<String, String> producer;
    private final String topic;    
    private final int maxFutures;    
    
    public static NoSQLServer createServer(NoSQLSettings srvcfg, String srvprefix) {
        String broker = srvcfg.getStringProperty(srvprefix, "broker", "localhost:9092");
        String topic = srvcfg.getStringProperty(srvprefix, "topic", "event");
        int futures = srvcfg.getIntProperty(srvprefix, "futures", 0);
        JDebug.out.log(Level.CONFIG, "{0}.type: {1} \n{2}\nbroker: {3} topic: {4} futures: {5}",
                new Object[]{srvprefix, NoSQLKafka.class.getName(), srvcfg, broker, topic, futures});
        return new NoSQLKafka(srvcfg, broker, topic, futures);
    }
    
    public NoSQLKafka(NoSQLSettings settings, String broker, String topic, int futures) {
        super(settings);
        this.topic = topic;        
        this.maxFutures = futures;
        
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        
        JDebug.out.log(Level.INFO, "kafka init storage...{0} topic={1}", new Object[]{broker, this.topic}); 
        producer = new KafkaProducer<>(props);
        JDebug.out.info("kafka init storage...done");
    }

    @Override
    public int storeData(DataList list) {
        boolean ev=false;
        boolean al=false;
        
        for ( int i=0; i<=list.getHighWaterMark(); i++ ) {
            DataItem item = list.getItem(i);
            if ( item != null ) {
                if ( item instanceof EventItem )
                    ev=true;
                else if ( item instanceof AlertItem)
                    al=true;
            }
        }
        
        int ret;
        if ( (!ev || (ret=storeDataEvents(list))==INoSQLInterface.OK) &&
             (!al || (ret=storeDataAlerts(list))==INoSQLInterface.OK) )
            return INoSQLInterface.OK;
        else
            return ret;                
    }    
    
    public int storeDataEvents(DataList events) {
        Date t1 = new Date();        
        
        DataItem item;
        EventItem event;  
        
//        List<Future> futures = new LinkedList<>();
//        try {        
            for (int i = 0; i <= events.getHighWaterMark()&& (item = events.getItem(i)) != null; i++) {           
                if ( !(item instanceof EventItem) ) continue;
                event=(EventItem)item;
                
                producer.send(
                        new ProducerRecord(
                                topic.isEmpty() ? event.getDp().getDp() : topic,
                                event.getDp().getFQN(), 
                                event.toJSONObject().toJSONString()));
                
//                futures.add(
//                        producer.send(
//                                new ProducerRecord(
//                                        topic.isEmpty() ? event.getDp().getDp() : topic,
//                                        event.getDp().getFQN(), event.toJSONObject().toJSONString()))
//                );                                  
                
                // future insert - wait until all futures are complete
//                if ( this.maxFutures>0 && futures.size()>this.maxFutures ) {
//                    while ( futures.size() > this.maxFutures/2 ) {
//                        futures.get(0).get();
//                        futures.remove(0);
//                    }                    
//                }
            }
            
//            while (futures.size()>0) {
//                futures.get(0).get();
//                futures.remove(0);
//            }            
            
//        } catch (InterruptedException | ExecutionException ex) {
//            Logger.getLogger(NoSQLKafka.class.getName()).log(Level.SEVERE, null, ex);
//        }
        

        //producer.send(bulk); // This publishes message on given topic                    
        
        Date t2 = new Date();
        addServerStats(events.getHighWaterMark(), t2.getTime()-t1.getTime());               
        
        return INoSQLInterface.OK;
    }
    
    private int storeDataAlerts(DataList alerts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }

    @Override
    public boolean dpGetPeriod(Date t1, Date t2, Dp dp, Set<String> configs, DpGetPeriodResult result) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

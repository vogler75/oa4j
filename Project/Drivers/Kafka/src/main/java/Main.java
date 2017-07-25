import at.rocworks.oa4j.driver.*;
import at.rocworks.oa4j.jni.Transformation;

import at.rocworks.oa4j.base.JDebug;

import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;

import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author vogler
 */
public class Main {
    public static void main(String[] args) throws Exception {                        
        new Main().start(args);
    }            
    
    public void start(String[] args) {
        try {
            Driver driver = new Driver(args);
            JDebug.setLevel(Level.INFO);
            driver.startup();
            JDebug.out.info("ok");
        } catch ( Exception ex ) {
            JDebug.StackTrace(Level.SEVERE, ex);
        }
    }
    
    public class Driver extends JDriverSimple {
        private String broker;
        private String topic;
        private String groupid;
        
        private KafkaProducer<String, String> producer;
        private KafkaConsumer<String, String> consumer;

        public Driver(String[] args) throws Exception {
            super(args);
            broker="";
            topic="";
            for (int i=0; i<args.length; i++) {
                if (args[i].equals("-broker") && args.length>i+1) {
                    broker=args[i+1];
                }
                if (args[i].equals("-topic") && args.length>i+1) {
                    topic=args[i+1];
                }
                if (args[i].equals("-groupid") && args.length>i+1) {
                    groupid=args[i+1];
                }
            }
        }            

        @Override
        public Transformation newTransformation(String name, int type) {
            switch (type) {
                case 1000:
                    return new JTransTextVarJson(name, type);
                case 1001:
                    return new JTransIntegerVarJson(name, type);
                case 1002:
                    return new JTransFloatVarJson(name, type);
                default:
                    JDebug.out.log(Level.WARNING, "unhandled transformation type {0} for {1}", new Object[]{type, name});
                    return null;
            }
        }
        
        @Override
        public boolean start() {
            try {
                JDebug.out.log(Level.INFO, "broker={0} topic={1} groupid={2}", new Object[]{broker, topic, groupid});

                if (broker.isEmpty() || topic.isEmpty() || groupid.isEmpty())  {
                    JDebug.out.severe("No broker and/or topic and/or groupid configured!");
                    return false;
                }
                
                // Producer
                Properties propsProducer = new Properties();
                propsProducer.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
                propsProducer.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                propsProducer.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                propsProducer.put(ProducerConfig.ACKS_CONFIG, "1");
                producer = new KafkaProducer<>(propsProducer);
                
                // Consumer
                Properties propsConsumer = new Properties();
                propsConsumer.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
                propsConsumer.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                propsConsumer.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                propsConsumer.put(ConsumerConfig.GROUP_ID_CONFIG, groupid);
                propsConsumer.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 1024*64);
                consumer = new KafkaConsumer<>(propsConsumer);
                consumer.subscribe(Arrays.asList(topic));

                new Thread(()->readInputBlocks()).start();

                return super.start();
            } catch ( Exception ex ) {
                JDebug.StackTrace(Level.SEVERE, ex);
                return false;
            }                        
        }

        @Override
        public void sendOutputBlock(JDriverItemList data) {
            JDriverItem item;           
            
            while ( (item=data.pollFirst())!=null ) {
//                JDebug.out.log(Level.INFO, "output: {0}", item.getName());

                // enrich the data with name and timestamp
                // unfortunatley we have to convert it back to json and 
                // convert it again to json...
                // OA-Driver ==> Transformation ==> JSON-String ==> sendOutputBlock 
                // ==> toJSONObject ==> enricht => JSON-String ==> Kafka
                
                String jsonText = new String(item.getData());
                JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonText);
                jsonObject.put("Name", item.getName());
                jsonObject.put("TimeMS", item.getTime().getTime());
                
                ProducerRecord record = new ProducerRecord(
                        topic,
                        item.getName(),
                        //new String(item.getData())
                        jsonObject.toJSONString()
                );
                try {
                    producer.send(record);
                } catch ( Exception ex ) {
                    JDebug.StackTrace(Level.SEVERE, ex);
                }
            }
        }
        
        public void readInputBlocks() {
            try {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(100);
                    JDriverItemList list = new JDriverItemList();
                    for (ConsumerRecord<String, String> record : records) {
                        //JDebug.out.info("record: "+record.toString());
                        
                        JSONObject jsonObject = (JSONObject)JSONValue.parse(record.value());
                        if ( jsonObject == null) continue;
                        
                        // if we just want the value for old/new comparision
                        //Object valueObj = jsonObject.get("value");
                        //JSONObject jsonValue = new JSONObject();
                        //jsonValue.put("value", valueObj);
                                                
                        Date time = null;
                        Object timeObj = jsonObject.get("TimeMS");
                        if ((timeObj instanceof Long)) 
                            time=new Date((Long)timeObj);

                        //JDebug.out.info(record.key());
                        list.addItem(new JDriverItem(
                                record.key(),
                                record.value().getBytes(), // everything for old/new comparision
                                //jsonValue.toJSONString().getBytes(), // only value for old/new comparison
                                time
                        ));
                    }
                    if (!list.isEmpty()) {
                        sendInputBlock(list);
                    }
                    consumer.commitAsync();
                }
            } catch (WakeupException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }
        }
        
        @Override
        public boolean attachInput(String addr) {
            return true;
        }

        @Override
        public boolean detachInput(String addr) {
            return true;
        }

        @Override
        public boolean attachOutput(String addr) {
            return true;
        }

        @Override
        public boolean detachOutput(String addr) {
            return true;
        }
    }

    public static class JTransTextVarJson extends JTransTextVar {
        private static final int SIZE=4096;

        public JTransTextVarJson(String name, int type) {
            super(name, type, SIZE);
        }

        @Override
        protected byte[] toPeriph_(String val) { return toPeriph(val); }
        public static byte[] toPeriph(String val) {
            JSONObject json = new JSONObject();
            json.put("Value", val);
            return json.toJSONString().getBytes();
        }

        @Override
        protected String toVal_(byte[] data) { return toVal(data); }
        public static String toVal(byte[] data) throws IllegalArgumentException {
            JSONObject json = (JSONObject)JSONValue.parse(new String(data));
            Object val = json.get("Value");
            if ( val instanceof String )
                return (String)val;
            else {
                throw new IllegalArgumentException("unhandled value type " + val.getClass().getName());
            }
        }
    }

    public static class JTransFloatVarJson extends JTransFloatVar {

        private static final int SIZE=1024;

        public JTransFloatVarJson(String name, int type) {
            super(name, type, SIZE);
        }

        @Override
        protected byte[] toPeriph_(Double val) { return toPeriph(val); }
        public static byte[] toPeriph(Double val) {
            JSONObject json = new JSONObject();
            json.put("Value", val);
            return json.toJSONString().getBytes();
        }

        @Override
        protected Double toVal_(byte[] data) { return toVal(data); }
        public static Double toVal(byte[] data) throws IllegalArgumentException {
            JSONObject json = (JSONObject) JSONValue.parse(new String(data));
            Object val = json.get("Value");
            if (val == null) {
                throw new IllegalArgumentException("no key \"Value\" in json object!");
            } else if (val instanceof Double) {
                return (Double)val;
            } else if (val instanceof Long) { // if there is no dot in the string
                return ((Long)val).doubleValue();
            }
            else {
                throw new IllegalArgumentException("unhandled value type " + val.getClass().getName());
            }
        }
    }

    public static class JTransIntegerVarJson extends JTransIntegerVar {
        private static final int SIZE=1024;

        public JTransIntegerVarJson(String name, int type) {
            super(name, type, SIZE);
        }

        @Override
        protected byte[] toPeriph_(Integer val) { return toPeriph(val); }
        public static byte[] toPeriph(Integer val) {
            JSONObject json = new JSONObject();
            json.put("Value", val);
            return json.toJSONString().getBytes();
        }

        @Override
        protected Integer toVal_(byte[] data) { return toVal(data); }
        public static Integer toVal(byte[] data) throws IllegalArgumentException {
            JSONObject json = (JSONObject)JSONValue.parse(new String(data));
            Object val = json.get("Value");
            if ( val instanceof Long )
                return ((Long)val).intValue();
            else {
                throw new IllegalArgumentException("unhandled value type " + val.getClass().getName());
            }
        }
    }
}

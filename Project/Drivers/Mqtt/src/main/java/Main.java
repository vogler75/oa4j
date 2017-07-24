import java.util.Date;
import java.util.logging.Level;

import at.rocworks.oa4j.driver.*;
import at.rocworks.oa4j.jni.Transformation;
import at.rocworks.oa4j.base.JDebug;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Created by vogler on 3/25/2017.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        new Main().start(args);
    }

    public void start(String[] args) {
        try {
            MqttDriver driver = new MqttDriver(args);
            JDebug.setLevel(Level.INFO);
            driver.startup();
            JDebug.out.info("ok");
        } catch ( Exception ex ) {
            JDebug.StackTrace(Level.SEVERE, ex);
        }
    }

    public class MqttDriver extends JDriverSimple {

        private MqttClient mqtt;
        private String url; // connect url (e.g. tcp://iot.eclipse.org)
        private String cid; // client id
        private Boolean json = false;
        private Boolean clean = true;
        private String username = null;
        private String password = null;
        private Integer qos = 0;

        public MqttDriver(String[] args) throws Exception {
            super(args, 10);
            url="";
            cid="";

            // here we read the arguments (config entries are not yet available at this point, see function start())
            for (int i=0; i<args.length; i++) {
                if (args[i].equals("-url") && args.length>i+1) {
                    url=args[i+1];
                }
                if (args[i].equals("-cid") && args.length>i+1) {
                    cid=args[i+1];
                }
                if (args[i].equals("-json")) {
                    json=true;
                }
                if (args[i].equals("-clean") && args.length>i+1) {
                    if (args[i+1].equals("1") || args[i+1].equals("true"))
                        clean=true;
                    else if (args[i+1].equals("0") || args[i+1].equals("false"))
                        clean=false;
                    else
                        JDebug.out.warning("unknown value for -clean given (true|false)!");
                }
                if (args[i].equals("-login") && args.length>i+1) {
                    String[] login=args[i+1].split("/");
                    if (login.length!=2) {
                        JDebug.out.warning("unknown value for -login given (username/password)!");
                        username=login[0];
                        password=login[1];
                    }
                }
                if (args[i].equals("-qos") && args.length>i+1) {
                    qos=Integer.parseInt(args[i+1]);
                }
            }
        }

        @Override
        public Transformation newTransformation(String name, int type) {
            switch (type) {
                case 1000: return json ? new JTransTextVarJson(name, type) : new JTransTextVar(name, type);
                case 1001: return json ? new JTransIntegerVarJson(name, type) : new JTransIntegerVar(name, type);
                case 1002: return json ? new JTransFloatVarJson(name, type) : new JTransFloatVar(name, type);
                default:
                    JDebug.out.log(Level.WARNING, "unhandled transformation type {0} for {1}", new Object[]{type, name});
                    return null;
            }
        }

        @Override
        public boolean start() {
            try {
                // read config values
                url=getConfigValueOrDefault("url", url);
                cid=getConfigValueOrDefault("cid", cid);
                username=getConfigValueOrDefault("username", username);
                password=getConfigValueOrDefault("password", password);
                qos=Integer.parseInt(getConfigValueOrDefault("qos", qos.toString()));

                // startup mqtt connection
                JDebug.out.log(Level.INFO, "connect to mqtt...{0}", System.getProperty("java.io.tmpdir"));
                MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(System.getProperty("java.io.tmpdir"));
                mqtt = new MqttClient(url, cid, dataStore);

                // receive values
                mqtt.setCallback(new MqttCallbackImpl());

                // connect to mqtt
                MqttConnectOptions options  = new MqttConnectOptions();
                options.setAutomaticReconnect(true);
                options.setCleanSession(clean);
                if (username!=null && !username.isEmpty() && password!=null && !password.isEmpty()) {
                    options.setUserName(username);
                    options.setPassword(password.toCharArray());
                }
                mqtt.connect(options);
                JDebug.out.info("connect to mqtt...done");

                return super.start();
            } catch (MqttException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
                return false;
            }
        }

        @Override
        public void sendOutputBlock(JDriverItemList data) {
            JDriverItem item;
            while ((item=data.pollFirst())!=null) {
                byte[] bytes;
                if ( json ) {
                    JSONObject json = new JSONObject();

                    // add the time to the message
                    json.put("TimeMS", item.getTime().getTime());

                    // we have to decode the raw/byte data and encode it again
                    switch ( item.getTransTypeNr() ) {
                        case 1000: json.put("Value", JTransTextVarJson.toVal(item.getData())); break;
                        case 1001: json.put("Value", JTransIntegerVarJson.toVal(item.getData())); break;
                        case 1002: json.put("Value", JTransFloatVarJson.toVal(item.getData())); break;
                    }
                    bytes = json.toJSONString().getBytes();
                    //JDebug.out.info("sendOutput "+json.toJSONString());
                } else {
                    bytes = item.getData();
                }
                MqttMessage message = new MqttMessage(bytes);
                message.setQos(qos);
                try {
                    mqtt.publish(item.getName(), message);
                } catch (MqttException ex) {
                    JDebug.StackTrace(Level.SEVERE, ex);
                }
            }
        }

        @Override
        public void stop() {
            JDebug.out.info("disconnect to mqtt...");
            try {
                mqtt.disconnect();
            } catch (MqttException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }
            JDebug.out.info("disconnect to mqtt...done");
        }

        @Override
        public boolean attachInput(String addr) {
            if ( mqtt != null && mqtt.isConnected() ) {
                try {
                    JDebug.out.log(Level.INFO, "attachInput addr={0} ... subscribe", new Object[]{addr});
                    mqtt.subscribe(addr);
                    return true;
                } catch (MqttException ex) {
                    JDebug.StackTrace(Level.SEVERE, ex);
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public boolean detachInput(String addr) {
            if ( mqtt != null && mqtt.isConnected() ) {
                try {
                    JDebug.out.log(Level.INFO, "detachInput addr={0} ... unsubscribe", new Object[]{addr});
                    mqtt.unsubscribe(addr);
                    return true;
                } catch (MqttException ex) {
                    JDebug.StackTrace(Level.SEVERE, ex);
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public boolean attachOutput(String addr) {
            if ( mqtt != null && mqtt.isConnected() ) {
                JDebug.out.log(Level.INFO, "attachOutput addr={0} ... subscribe", new Object[]{addr});
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean detachOutput(String addr) {
            if ( mqtt != null && mqtt.isConnected() ) {
                JDebug.out.log(Level.INFO, "detachOutput addr={0} ... unsubscribe", new Object[]{addr});
                return true;
            } else {
                return false;
            }
        }

        private class MqttCallbackImpl implements MqttCallbackExtended {

            public MqttCallbackImpl() {
            }

            @Override
            public void connectionLost(Throwable thrwbl) {
                JDebug.out.info("mqtt connection lost");
                lostAllAddresses();
            }

            @Override
            public void messageArrived(String tag, MqttMessage mm) throws Exception {
                //JDebug.out.log(Level.INFO, "{0}", new Object[]{tag});
                String msg = new String(mm.getPayload());
                if ( json ) {
                    try {
                        Object obj = (new JSONParser()).parse(msg);
                        if (obj==null || !(obj instanceof JSONObject)) {
                            throw new IllegalArgumentException("cannot json decode given data");
                        }
                        JSONObject json = (JSONObject)obj;
                        Long ms = (Long) json.getOrDefault("TimeMS", 0L);
                        JDriverItem item = ms == 0 ? new JDriverItem(tag, mm.getPayload())
                                : new JDriverItem(tag, mm.getPayload(), new Date(ms));
                        sendInputBlock(new JDriverItemList(item));
                    } catch ( Exception ex ) {
                        JDebug.out.log(Level.INFO, "{0} => \"{1}\"", new Object[]{tag, msg});
                        JDebug.StackTrace(Level.WARNING, ex);
                    }
                } else {
                    JDriverItem item = new JDriverItem(tag, mm.getPayload());
                    sendInputBlock(new JDriverItemList(item));
                }
                //JDebug.out.log(Level.INFO, "{0} => \"{1}\"", new Object[]{tag, msg});
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken imdt) {
            }

            @Override
            public void connectComplete(boolean reconnect, String url) {
                JDebug.out.info("mqtt connection complete reconnect="+reconnect+" url="+url);
                if (reconnect) {
                    attachAddresses();
                }
            }
        }
    }
}

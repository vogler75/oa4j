import at.rocworks.oa4j.base.JDebug;
import at.rocworks.oa4j.jni.ExternHdlFunction;
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.IntegerVar;
import at.rocworks.oa4j.var.TextVar;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Created by vogler on 02.06.2017.
 */
public class CtrlExtMqtt extends ExternHdlFunction {
    static private MqttClient mqtt = null;
    static private ConcurrentHashMap<String, ArrayList<CtrlExtMqtt>> subscriptions = new ConcurrentHashMap<>();

    public String workFunction;

    public CtrlExtMqtt(long waitCondPtr) {
        super(waitCondPtr);
    }

    /**
     *
     * @param function Name of a (sub)program unit, must be handled by yourself (e.g. case statement)
     * @param parameter List of parameter values
     * @return List of values
     */
    @Override
    public DynVar execute(TextVar function, DynVar parameter) {
        try {
            JDebug.out.log(Level.INFO, "execute function={0} parameter={1}", new Object[] { function, parameter.formatValue() });
            switch (function.toString()) {
                case "connect": {
                    int ret = connect(
                            parameter.get(0).getTextVar().getValue(),
                            parameter.get(1).getTextVar().getValue(),
                            parameter.get(2).getTextVar().getValue(),
                            parameter.get(3).getTextVar().getValue(),
                            parameter.get(4).getBitVar().getValue()
                    );
                    setDone();
                    return new DynVar(new IntegerVar(ret));
                }
                case "disconnect": {
                    int ret = disconnect();
                    setDone();
                    return new DynVar(new IntegerVar(ret));
                }
                case "subscribe": {
                    if (isAsync()) {
                        int ret = subscribe(parameter.get(0).getTextVar().getValue(), parameter.get(1).getTextVar().getValue());
                        return new DynVar(new IntegerVar(ret));
                    } else {
                        JDebug.out.severe("subscribe must be called async!");
                        return new DynVar(new IntegerVar(-99));
                    }
                }
                case "unsubscribe": {
                    int ret = unsubscribe(parameter.get(0).getTextVar().getValue());
                    setDone();
                    return new DynVar(new IntegerVar(ret));
                }
                default: {
                    JDebug.out.info("unhandled function " + function.toString());
                    setDone();
                    return new DynVar(new IntegerVar(-1));
                }
            }
        } catch (Exception e) {
            JDebug.StackTrace(Level.SEVERE, e);
            setDone();
            return new DynVar(new IntegerVar(-2));
        }
    }

    public int connect(String url, String cid, String username, String password, boolean clean)
    {
        try {
            // startup mqtt connection
            JDebug.out.log(Level.INFO, "connect to mqtt...{0}", System.getProperty("java.io.tmpdir"));
            if ( mqtt != null ) {
                JDebug.out.log(Level.SEVERE, "already connected");
                return -1;
            }

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
            return 0;
        } catch (MqttException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return -2;
        }
    }

    public int disconnect() {
        JDebug.out.log(Level.INFO, "disconnect from mqtt");
        if ( mqtt == null ) {
            JDebug.out.log(Level.SEVERE, "not connected");
            return -1;
        }
        try {
            mqtt.disconnect();
            mqtt.close();
            mqtt=null;
            JDebug.out.info("disconnect from mqtt...done");
            return 0;
        } catch (MqttException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return -2;
        }
    }

    public int subscribe(String work, String addr) {
        try {
            JDebug.out.log(Level.INFO, "subscribe addr={0}", new Object[]{addr});
            this.workFunction=work;

            ArrayList<CtrlExtMqtt> list = subscriptions.get(addr);
            if (list==null) {
                list=new ArrayList<CtrlExtMqtt>();
                subscriptions.put(addr, list);
            }
            if (list.size()==0) {
                mqtt.subscribe(addr);
            }
            list.add(this);

            return 0;
        } catch (MqttException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return -1;
        }
    }

    public int unsubscribe(String addr) {
        try {
            JDebug.out.log(Level.INFO, "unsubscribe addr={0}", new Object[]{addr});

            ArrayList<CtrlExtMqtt> list = subscriptions.get(addr);
            if (list!=null && list.size()>0) {
                list.forEach((cb)->cb.setDone());
                list.clear();
                mqtt.unsubscribe(addr);
            }

            return 0;
        } catch (MqttException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return -1;
        }
    }

    private class MqttCallbackImpl implements MqttCallbackExtended {

        public MqttCallbackImpl() {
        }

        @Override
        public void connectionLost(Throwable thrwbl) {
            JDebug.out.info("mqtt connection lost");
        }

        @Override
        public void messageArrived(String addr, MqttMessage mm) throws Exception {
            JDebug.out.log(Level.INFO, "{0}", new Object[]{addr});
            String msg = new String(mm.getPayload());
            ArrayList<CtrlExtMqtt> list = subscriptions.get(addr);
            list.forEach((cb)->{
                if (!cb.workFunction.isEmpty() && cb.isAsync()) {
                    JDebug.out.log(Level.INFO, "{0} => {1} => {2}", new Object[]{addr, cb.workFunction, msg});
                    cb.startFunc(cb.workFunction, new DynVar(new TextVar(msg)));
                }
            });
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken imdt) {
        }

        @Override
        public void connectComplete(boolean reconnect, String url) {
            JDebug.out.info("mqtt connection complete reconnect="+reconnect+" url="+url);
            if (reconnect) {
            }
        }
    }
}
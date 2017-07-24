import at.rocworks.oa4j.driver.JDriverItem;
import at.rocworks.oa4j.driver.JDriverItemList;
import at.rocworks.oa4j.driver.JDriverSimple;

import at.rocworks.oa4j.base.JDebug;

import java.util.logging.Level;

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

        public Driver(String[] args) throws Exception {
            super(args, 60);
        }

        @Override
        public boolean start() {
            // TODO
            return true;
        }

        @Override
        public void sendOutputBlock(JDriverItemList data) {
            try {
                JDriverItem item;
                while ((item = data.pollFirst()) != null) {
                    // TODO
                }
            } catch (Exception ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }
        }

        @Override
        protected boolean attachInput(String addr) {
            // TODO
            return true;
        }

        @Override
        protected boolean attachOutput(String addr) {
            // TODO
            return true;
        }

        @Override
        public boolean detachInput(String addr) {
            // TODO
            return true;
        }

        @Override
        public boolean detachOutput(String addr) {
            // TODO
            return true;
        }
    }
}

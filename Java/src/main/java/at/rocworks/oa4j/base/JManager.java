/*
    OA4J - WinCC Open Architecture for Java
    Copyright (C) 2017 Andreas Vogler

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package at.rocworks.oa4j.base;

import at.rocworks.oa4j.jni.*;
import at.rocworks.oa4j.var.DpIdentifierVar;
import at.rocworks.oa4j.var.Variable;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Main manager class for connecting Java applications to WinCC OA.
 * <p>
 * JManager is a singleton that handles the connection lifecycle, message dispatching,
 * and task queue management for WinCC OA communication. It runs the main event loop
 * in a separate thread and processes callbacks from the native API.
 * </p>
 *
 * <h3>Basic Usage:</h3>
 * <pre>{@code
 * public static void main(String[] args) throws Exception {
 *     JManager m = new JManager();
 *     m.init(args).start();
 *     // Your code here - use JClient for datapoint operations
 *     m.stop();
 * }
 * }</pre>
 *
 * <h3>Command Line Arguments:</h3>
 * <ul>
 *   <li>{@code -proj <name>} - WinCC OA project name (required)</li>
 *   <li>{@code -num <number>} - Manager number (default: 1)</li>
 *   <li>{@code -path <dir>} - Project directory path</li>
 *   <li>{@code -db} - Connect as database manager type</li>
 *   <li>{@code -noinit} - Skip resource initialization</li>
 *   <li>{@code -debug} - Enable debug output</li>
 * </ul>
 *
 * @author vogler
 * @see JClient
 */
public class JManager extends Manager implements Runnable {
    public static final int DB_MAN = 3;        
    public static final int API_MAN = 7;

    public static int MAX_ENQUEUE_SIZE_HIGH = 100000;
    public static int MAX_ENQUEUE_SIZE_LOW = 50000;
    public static int MAX_DEQUEUE_SIZE_HIGH = 10000;  // Message queue high threshold (full capacity)
    public static int MAX_DEQUEUE_SIZE_LOW = 50000;    // Message queue low threshold (recovery point)

    private boolean connectToData = true;
    private boolean connectToEvent = true;

    private volatile int maxEnqueueSizeReached = 0;
    private volatile long lastEnqueueFullLogTime = 0;
    private static final long ENQUEUE_LOG_INTERVAL_MS = 1000;

    private static JManager instance = null; // Singleton
    
    protected JSemaphore loopPaused = new JSemaphore(true);
    protected volatile boolean apiEnabled = false;
    protected volatile boolean apiConnected = false;    
    protected volatile boolean loopBreak = false;

    private final ConcurrentHashMap<Integer, JHotLinkWaitForAnswer> hotlinkList = new ConcurrentHashMap<>();    
    //private final ConcurrentLinkedQueue<Callable> hotlinkQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Callable> taskQueue = new ConcurrentLinkedQueue<>();
    
    private int loopWaitUSec=10000;
    
    private String projName="<unknown>";
    private String projDir=".";        
    private String confDir="."; 
    
    private int manType=API_MAN;
    private int manNum=1;

    private boolean initResources=true;
    private boolean debugFlag=false;

    private Map<String, String> initSysMsgData;

    /**
     * Sets the maximum enqueue size thresholds for the task queue.
     * <p>
     * When the queue reaches the high threshold, new hotlinks are discarded.
     * When it drops below the low threshold, processing resumes normally.
     * </p>
     *
     * @param high Upper threshold - queue rejects new items above this size (default: 100000)
     * @param low  Lower threshold - queue recovers when size drops below this (default: 50000)
     * @return this manager instance for method chaining
     */
    public JManager setMaxEnqueueSize(int high, int low) {
        MAX_ENQUEUE_SIZE_HIGH = high;
        MAX_ENQUEUE_SIZE_LOW = low;
        return this;
    }

    /**
     * Sets the maximum dequeue size thresholds for message processing.
     *
     * @param high Upper threshold for dequeue operations (default: 10000)
     * @param low  Lower threshold for dequeue operations (default: 50000)
     * @return this manager instance for method chaining
     */
    public JManager setMaxDequeueSize(int high, int low) {
        MAX_DEQUEUE_SIZE_HIGH = high;
        MAX_DEQUEUE_SIZE_LOW = low;
        return this;
    }

    /**
     * Returns the current number of tasks waiting in the queue.
     *
     * @return number of pending tasks in the task queue
     */
    public int getEnqueueSize() {
        return taskQueue.size();
    }

    /**
     * Returns the singleton instance of the manager.
     * <p>
     * The instance is created when {@link #init(String[])} is first called.
     * Returns null if the manager has not been initialized.
     * </p>
     *
     * @return the JManager singleton instance, or null if not initialized
     */
    public static JManager getInstance() {
        return JManager.instance;
    }
    
    /**
     * Returns the project directory path.
     *
     * @return absolute path to the WinCC OA project directory
     */
    public String getProjPath() { return projDir; }

    private JManager setProjPath(String projDir) {
        this.projDir=projDir;
        this.confDir=this.projDir+"/config";
        return this;
    }

    /**
     * Sets the WinCC OA project name.
     *
     * @param projName the project name/identifier
     * @return this manager instance for method chaining
     */
    public JManager setProjName(String projName) {
        this.projName=projName;
        return this;
    }

    /**
     * Returns the project configuration directory path.
     *
     * @return absolute path to the project's config directory
     */
    public String getConfigDir() { return confDir; }

    /**
     * Returns the log directory path from the WinCC OA API.
     *
     * @return absolute path to the log directory
     */
    public String getLogDir() { return apiGetLogPath(); }

    /**
     * Returns the full path for this manager's log file.
     *
     * @return log directory path combined with manager name
     */
    public String getLogFile() { return getLogDir()+getManName(); }

    /**
     * Retrieves a configuration value from the WinCC OA config file.
     *
     * @param key the configuration key (e.g., "general.managerStartMode")
     * @return the configuration value, or null if not found
     */
    public String getConfigValue(String key) { return apiGetConfigValue(key); }

    /**
     * Retrieves a configuration value with a default fallback.
     *
     * @param key the configuration key
     * @param def default value to return if key is not found or empty
     * @return the configuration value, or the default if not found/empty
     */
    public String getConfigValueOrDefault(String key, String def) {
        String val = apiGetConfigValue(key);
        return (val==null || val.isEmpty()) ? def : val;
    }

    /**
     * Checks if the native WinCC OA library was successfully loaded.
     *
     * @return true if the native library is loaded and API is enabled
     */
    public boolean isEnabled() { return apiEnabled; }

    /**
     * Checks if the manager is currently connected to WinCC OA.
     *
     * @return true if connected and the event loop is running
     */
    public boolean isConnected() { return apiConnected; }

    /**
     * Returns the manager type.
     *
     * @return manager type constant ({@link #API_MAN} or {@link #DB_MAN})
     */
    public int getManType() { return manType; }

    private JManager setManType(int manType) { this.manType=manType; return this; }

    /**
     * Returns the manager number.
     *
     * @return the manager instance number (default: 1)
     */
    public int getManNum() { return manNum; }

    /**
     * Sets the manager number.
     * <p>
     * Multiple Java managers can run simultaneously with different numbers.
     * </p>
     *
     * @param manNum the manager instance number
     * @return this manager instance for method chaining
     */
    public JManager setManNum(int manNum) { this.manNum=manNum; return this; }

    /**
     * Sets the event loop wait time in microseconds.
     * <p>
     * This controls how long the manager waits between dispatch cycles.
     * Lower values increase responsiveness but use more CPU.
     * </p>
     *
     * @param usec wait time in microseconds (default: 10000 = 10ms)
     * @return this manager instance for method chaining
     */
    public JManager setLoopWaitUSec(int usec) {
        this.loopWaitUSec=usec;
        return this;
    }

    /**
     * Returns the event loop wait time in microseconds.
     *
     * @return current loop wait time in microseconds
     */
    public int getLoopWaitUSec() {
        return this.loopWaitUSec;
    }

    /**
     * Initializes the manager by parsing command line arguments.
     * <p>
     * This method parses WinCC OA standard arguments and loads the native library.
     * It must be called before {@link #start()}.
     * </p>
     *
     * <h4>Supported arguments:</h4>
     * <ul>
     *   <li>{@code -proj <name>} - Project name (required)</li>
     *   <li>{@code -num <n>} - Manager number</li>
     *   <li>{@code -path <dir>} - Project directory</li>
     *   <li>{@code -db} - Use database manager type</li>
     *   <li>{@code -noinit} - Skip resource initialization</li>
     *   <li>{@code -debug} - Enable debug output</li>
     * </ul>
     *
     * @param args command line arguments
     * @return this manager instance for method chaining
     * @throws Exception if native library cannot be loaded or version mismatch
     */
    public JManager init(String args[]) throws Exception {
        for ( int i=0; i<args.length; i++ ) {
            // projDir & configDir
            if ( args[i].equals("-path") && args.length>i+1 ) {
                setProjPath(args[i+1]);        
            }
            
            if ( args[i].equals("-proj") && args.length>i+1 ) {
                setProjName(args[i+1]);
            }            
                        
            // managerNum
            if ( args[i].equals("-num") && args.length>i+1 ) {
                setManNum(Integer.valueOf(args[i+1]));
            }
            
            // managerType
            if ( args[i].equals("-db") ) {
                setManType(DB_MAN);
            }

            // initResources
            if ( args[i].equals("-noinit") ) {
                initResources=false;
            }

            // debug
            if ( args[i].equals("-debug")) {
                debugFlag=true;
            }
        }        
        return init();
    }

    /**
     * Initializes the manager with explicit parameters.
     * <p>
     * Alternative to {@link #init(String[])} when not using command line arguments.
     * </p>
     *
     * @param projName WinCC OA project name
     * @param manType  manager type ({@link #API_MAN} or {@link #DB_MAN})
     * @param manNum   manager instance number
     * @return this manager instance for method chaining
     * @throws Exception if native library cannot be loaded or version mismatch
     */
    public JManager init(String projName, int manType, int manNum) throws Exception {
        setProjName(projName);        
        setManType(manType);
        setManNum(manNum);    
        return init();
    }    

    private JManager init() throws Exception {
        if (JManager.instance == null) {
            JManager.instance = this;
        } else {
            throw new IllegalStateException("There can only be one manager!");
        }   
        
        apiEnabled=false;        
        String errmsg1="";
        String errmsg2="";
        try {
            System.loadLibrary("WCCOAjava");
            apiEnabled=true;
        } catch ( java.lang.UnsatisfiedLinkError ex1 ) {
            errmsg1=ex1.getMessage();
            try {
                System.loadLibrary("WCCILjava"); // this was for ManagerV4 (IOWA)
                apiEnabled=true;
            } catch ( java.lang.UnsatisfiedLinkError ex2 ) {
                errmsg2=ex2.getMessage();
            }            
        }

        if ( apiEnabled ) {
            // Set log file settings
            setDebugOutput();
            if (!isV3() && !isV4()) {
                throw new Exception("Manager Version "+apiGetVersion()+" is not V3 and not V4!");
            }
        } else {
            setDebugConsole();
            if (!errmsg1.equals(""))
                JDebug.out.warning(errmsg1);
            if (!errmsg2.equals(""))
                JDebug.out.warning(errmsg2);                
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Thread.sleep(200);
                stop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stackTrace(ErrPrio.PRIO_WARNING, ErrCode.UNEXPECTEDSTATE, e);
            }
        }));

        return this;
    }

    /**
     * Configures debug output to write to log files.
     * <p>
     * Log files are written to the project's log directory.
     * </p>
     */
    public void setDebugOutput() {
        JDebug.setOutput(getLogDir(), getManName());
    }

    /**
     * Configures debug output to write to the console (stdout/stderr).
     */
    public void setDebugConsole() {
        JDebug.setConsole();
    }

    /**
     * Returns the manager name used for logging and identification.
     *
     * @return manager name in format "WCCOAjava{num}" (e.g., "WCCOAjava1")
     */
    public String getManName() {
        return "WCCOAjava"+manNum;
    }

    /**
     * Starts the manager and connects to both Data and Event managers.
     * <p>
     * This is equivalent to calling {@code start(true, true)}.
     * The method blocks until the connection is established.
     * </p>
     *
     * @see #start(boolean, boolean)
     */
    public void start() {
        start(true, true);
    }

    /**
     * Starts the manager with specific connection options.
     * <p>
     * Starts the event loop in a separate thread and connects to WinCC OA.
     * The method blocks until the connection is established.
     * </p>
     *
     * @param connectToData  true to connect to the Data manager
     * @param connectToEvent true to connect to the Event manager
     */
    public void start(boolean connectToData, boolean connectToEvent) {
        if ( apiEnabled ) {
            log(ErrPrio.PRIO_INFO, ErrCode.MANAGER_START, "Manager start...");
            this.connectToData=connectToData;
            this.connectToEvent=connectToEvent;
            new Thread(this).start();
            loopPaused.awaitFalse();
            log(ErrPrio.PRIO_INFO, ErrCode.MANAGER_INITIALIZED, "Manager started.");
        }
    }

    /**
     * Stops the manager and disconnects from WinCC OA.
     * <p>
     * Gracefully shuts down the event loop and releases native resources.
     * This method should be called before application exit.
     * </p>
     */
    public void stop() {
        if ( apiEnabled ) {
            log(ErrPrio.PRIO_INFO, ErrCode.MANAGER_STOP, "Manager stop.");
            apiConnected = false; // stop run loop
            pause();
            apiShutdown();
        }
    }

    /**
     * Main event loop - runs in a separate thread.
     * <p>
     * This method is called internally by {@link #start()} and should not
     * be called directly. It dispatches WinCC OA messages and processes
     * the task queue.
     * </p>
     */
    @Override
    public void run() {
        apiStartup(manType,
                new String[]{"WCCILjava", "-proj", projName, "-num", Integer.toString(manNum)},
                connectToData, connectToEvent,
                initResources, debugFlag);
        loopPaused.sendFalse();
        apiConnected=true;
        while (apiConnected) {
            log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "Manager loop waiting.");
            loopPaused.awaitFalse();
            log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "Manager loop started.");
            while ( !loopBreak ) {
                synchronized ( this ) {
                    apiDispatch(0, loopWaitUSec);
                    queueWorker();
                }
            }
            loopPaused.sendTrue();
            log(ErrPrio.PRIO_INFO, ErrCode.NOERR, "Manager loop stopped.");
        }
    }

    /**
     * Pauses the event loop.
     * <p>
     * The manager remains connected but stops processing messages.
     * Use {@link #resume()} to continue processing.
     * </p>
     */
    public void pause() {
        loopBreak=true;
        loopPaused.awaitTrue();
    }

    /**
     * Resumes the event loop after a pause.
     *
     * @see #pause()
     */
    public void resume() {
        loopBreak=false;
        loopPaused.sendFalse();
    }

    protected void enqueueHotlink(JHotLinkWaitForAnswer hl) {
        if (taskQueue.size() >= MAX_ENQUEUE_SIZE_HIGH) {
            // OVERLOAD: Discard hotlink, log once per second
            maxEnqueueSizeReached++;
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastEnqueueFullLogTime >= ENQUEUE_LOG_INTERVAL_MS) {
                log(ErrPrio.PRIO_WARNING, ErrCode.NOERR,
                    "Max enqueue size reached " + taskQueue.size() + " discarding hotlink...");
                lastEnqueueFullLogTime = currentTime;
            }
        } else if (maxEnqueueSizeReached > 0 && taskQueue.size() <= MAX_ENQUEUE_SIZE_LOW) {
            // RECOVERY: Below threshold, add hotlink and report recovery
            taskQueue.add(hl);
            maxEnqueueSizeReached = 0;
            lastEnqueueFullLogTime = 0;
            log(ErrPrio.PRIO_WARNING, ErrCode.NOERR,
                "Enqueue below threshold size " + taskQueue.size() + " processing hotlink...");
        } else if (maxEnqueueSizeReached == 0) {
            // NORMAL: Not in overload, add hotlink
            taskQueue.add(hl);
        }
        // IMPLICIT: else {} when overload && size > LOW - hotlink silently discarded during recovery phase
    }   
    
    protected void register(JHotLinkWaitForAnswer hl) {
        hotlinkList.put(hl.getHdlId(), hl);
    }
    
    protected void deregister(JHotLinkWaitForAnswer hl) {
        hotlinkList.remove(hl.getHdlId());
    }
    
    private void queueWorker() {        
        int k;
        try {
            // Maximum of loop, otherwise it could happen that we are in this
            // loop forever, if concurrently hotlink-requests are added...
            Callable task;
            for ( k=0; k<=100 && (task=taskQueue.poll()) != null; k++ ) {
                task.call();
            }
        } catch (Exception ex) {
            stackTrace(ErrPrio.PRIO_SEVERE, ErrCode.UNEXPECTEDSTATE, ex);
        }
    }

    /**
     * Adds a task to the asynchronous task queue.
     * <p>
     * The task will be executed in the manager's event loop thread.
     * Use this for fire-and-forget operations.
     * </p>
     *
     * @param task the callable to execute
     * @return true if the task was successfully added to the queue
     * @see #executeTask(Callable)
     */
    public boolean enqueueTask(Callable task) {
        return taskQueue.add(task);
    }

    /**
     * Executes a task synchronously in the manager's event loop thread.
     * <p>
     * The calling thread blocks until the task completes and returns the result.
     * Use this when you need the result of an operation that must run in the
     * manager thread context.
     * </p>
     *
     * @param task the callable to execute
     * @return the result of the callable, or null if the task returned nothing
     * @see #enqueueTask(Callable)
     */
    public Object executeTask(Callable task) {
        ArrayList<Object> res = new ArrayList<>();
        JSemaphore sem = new JSemaphore(false);
        enqueueTask(()->{
            res.add(task.call());
            sem.sendTrue();
            return true;
        });        
        sem.awaitTrue();
        return ( res.size() > 0 ? res.get(0) : null);        
    }
    
    @Override
    public int callbackAnswer(int id, int idx, DpIdentifierVar dpid, Variable var, long time) {
        JHotLinkWaitForAnswer hdl;
        hdl = hotlinkList.get(id);
        if (hdl != null) {                      
            switch (idx) {
                case -1:
                    hdl.answerInit();
                    break;
                case -2:
                    hdl.callbackDone();
                    break;
                default:
                    hdl.callbackItem(new JDpVCItem(dpid, var, time));
                    break;
            }
            return 0;
        } else 
            return -1;
    }

    @Override
    public int callbackAnswerError(int id, int code, String text) {
        JHotLinkWaitForAnswer hdl;
        hdl = hotlinkList.get(id);
        if (hdl != null) {
            hdl.answerError(code, text);
        }
        return 0;
    }

    @Override
    public int callbackHotlink(int id, int idx, DpIdentifierVar dpid, Variable var) {
        JHotLinkWaitForAnswer hdl;
        hdl = hotlinkList.get(id);
        if (hdl != null) {
            switch (idx) {
                case -1:
                    hdl.hotlinkInit();
                    break;
                case -2:
                    hdl.callbackDone();
                    break;
                default:
                    hdl.callbackItem(new JDpVCItem(dpid, var, 0));
                    break;
            }
            return 0;
        } else {
            return -1;
        }
    }           
           
    @Override
    public boolean doReceiveSysMsg(long cPtrSysMsg) {
        SysMsg msg = new SysMsg(cPtrSysMsg);
//        JDebug.out.info("------------SYSMSG DEBUG BEGIN-------------------------");
//        JDebug.out.log(Level.INFO, "isA => {0}", msg.isA());
//        JDebug.out.info(msg.toDebug(99));
//        JDebug.out.info("------------SYSMSG DEBUG END  -------------------------");
        if (msg.getSysMsgType() == msg.getSysMsgTypes().INIT_SYS_MSG()) {
            initSysMsgData = msg.getInitSysMsgData();
        } else if (msg.getSysMsgType() == msg.getSysMsgTypes().REDUNDANCY_SYS_MSG()) {
            if (msg.getSourceManType()== ManagerType.EVENT_MAN) { // msg comes twice, from data and event manager
                switch (msg.getSysMsgRedundancySubType()) {
                    case REDUNDANCY_ACTIVE: becameActive(); break;
                    case REDUNDANCY_PASSIVE: becamePassive(); break;
                }
            }
        }
        return false;
    }

    private int isActive=-1; // -1...unknown, 0...no, 1...true

    /**
     * @return true if manager is connected to the active host
     */
    public Boolean isActive()
    {
        if (isActive==-1) // if it is unknown take the state from the InitSysMsg
            return (getInitSysMsgData().getOrDefault("general:active", "1").equals("1"));
        else
            return isActive==1;
    }

    /**
     * executed when manager becomes actigve
     */
    protected void becameActive() {
        isActive=1; // active
    }

    /**
     * executed when manager becomes passive
     */
    protected void becamePassive() {
        isActive=0; // passive
    }

    /**
     * @return Data of the InitSysMsg
     */
    public Map<String, String> getInitSysMsgData()
    {
        return initSysMsgData==null ? new HashMap<String, String>() : initSysMsgData;
    }


    @Override
    public boolean doReceiveDpMsg(long cPtrDpMsg) {
//        DpMsg msg = new DpMsg(cPtrDpMsg);
//        JDebug.out.info("------------DPMSG DEBUG BEGIN-------------------------");
//        JDebug.out.log(Level.INFO, "isA => {0}", msg.isA());
//        JDebug.out.info(msg.toDebug(99));
//        JDebug.out.info("------------DPMSG DEBUG END  -------------------------");
        return false;
    }
    
    // NOT USED
    
    @Override
    public int callbackHotlinkGroup(int id, long ptrDpHlGroup) {
        apiProcessHotlinkGroup(id, ptrDpHlGroup);
        return 0;
    }

    /**
     * Static convenience method to log a message
     * @param prio Priority level
     * @param code Error code/state
     * @param text Message text to log
     */
    public static void log(ErrPrio prio, ErrCode code, String text) {
        JManager instance = getInstance();
        if (instance != null) {
            instance.apiLog(prio.getValue(), code.getValue(), text);
        }
    }

    /**
     * Static convenience method to log exception stack traces
     * @param prio Priority level
     * @param code Error code/state
     * @param exception The exception to log
     */
    public static void stackTrace(ErrPrio prio, ErrCode code, Throwable exception) {
        if (exception == null) {
            return;
        }

        // Get the stack trace as a string
        StringBuilder sb = new StringBuilder();
        sb.append(exception.getClass().getName()).append(": ").append(exception.getMessage()).append("\n");

        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append("  at ").append(element).append("\n");
        }

        // Remove trailing newline
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }

        log(prio, code, sb.toString());
    }

    /**
     * Convenience method to log exception stack traces with WARNING priority
     * @param exception The exception to log
     */
    public static void stackTrace(Throwable exception) {
        stackTrace(ErrPrio.PRIO_WARNING, ErrCode.UNEXPECTEDSTATE, exception);
    }
}

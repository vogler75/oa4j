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
package at.rocworks.oa4j;

import at.rocworks.oa4j.base.*;
import at.rocworks.oa4j.jni.ErrCode;
import at.rocworks.oa4j.jni.ErrPrio;
import at.rocworks.oa4j.var.*;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * Unified facade class for WinCC OA operations.
 * <p>
 * This class provides a simplified, single entry point for all WinCC OA operations,
 * hiding the internal complexity of JManager and JClient. It follows the singleton
 * pattern - only one connection per JVM is supported.
 * </p>
 *
 * <h3>Basic Usage:</h3>
 * <pre>{@code
 * // Connect to WinCC OA
 * WinCCOA oa = WinCCOA.connect(args);
 *
 * // Read a datapoint
 * Variable value = oa.dpGet("ExampleDP_Arg1.");
 *
 * // Write a datapoint
 * oa.dpSet("ExampleDP_Arg1.", 42);
 *
 * // Subscribe to changes
 * oa.dpConnect()
 *     .add("ExampleDP_Arg1.")
 *     .action(hlg -> System.out.println("Changed!"))
 *     .connect();
 *
 * // Disconnect when done
 * oa.disconnect();
 * }</pre>
 *
 * @author vogler
 */
public class WinCCOA {

    // ========== Singleton Instance ==========

    private static WinCCOA instance;
    private final JManager manager;

    /**
     * Private constructor - use connect() factory methods.
     */
    private WinCCOA(JManager manager) {
        this.manager = manager;
    }

    // ========== Connection & Lifecycle ==========

    /**
     * Connects to WinCC OA using command-line arguments.
     * <p>
     * Supported arguments: -proj, -path, -num, -db, -noinit, -debug
     * </p>
     *
     * @param args Command-line arguments
     * @return The WinCCOA instance
     * @throws Exception If connection fails
     */
    public static synchronized WinCCOA connect(String[] args) throws Exception {
        if (instance != null) {
            throw new IllegalStateException("Already connected. Call disconnect() first.");
        }
        JManager m = new JManager();
        m.init(args).start();
        instance = new WinCCOA(m);
        return instance;
    }

    /**
     * Connects to WinCC OA with a project name.
     *
     * @param project The WinCC OA project name
     * @return The WinCCOA instance
     * @throws Exception If connection fails
     */
    public static synchronized WinCCOA connect(String project) throws Exception {
        return connect(project, 1);
    }

    /**
     * Connects to WinCC OA with a project name and manager number.
     *
     * @param project The WinCC OA project name
     * @param managerNumber The manager number
     * @return The WinCCOA instance
     * @throws Exception If connection fails
     */
    public static synchronized WinCCOA connect(String project, int managerNumber) throws Exception {
        if (instance != null) {
            throw new IllegalStateException("Already connected. Call disconnect() first.");
        }
        JManager m = new JManager();
        m.init(project, JManager.API_MAN, managerNumber).start();
        instance = new WinCCOA(m);
        return instance;
    }

    /**
     * Returns the current WinCCOA instance.
     *
     * @return The instance, or null if not connected
     */
    public static WinCCOA getInstance() {
        return instance;
    }

    /**
     * Disconnects from WinCC OA and releases resources.
     */
    public synchronized void disconnect() {
        if (manager != null) {
            manager.stop();
        }
        instance = null;
    }

    // ========== Status & Information ==========

    /**
     * Checks if connected to WinCC OA.
     *
     * @return true if connected
     */
    public boolean isConnected() {
        return manager != null && manager.isConnected();
    }

    /**
     * Checks if connected to the active host in a redundant system.
     *
     * @return true if connected to active host
     */
    public Boolean isActive() {
        return manager.isActive();
    }

    /**
     * Returns the project directory path.
     *
     * @return The absolute path to the project directory
     */
    public String getProjectPath() {
        return manager.getProjPath();
    }

    /**
     * Returns the config directory path.
     *
     * @return The absolute path to the config directory
     */
    public String getConfigDir() {
        return manager.getConfigDir();
    }

    /**
     * Returns the log directory path.
     *
     * @return The absolute path to the log directory
     */
    public String getLogDir() {
        return manager.getLogDir();
    }

    /**
     * Returns the manager name.
     *
     * @return The manager name (e.g., "WCCOAjava1")
     */
    public String getManagerName() {
        return manager.getManName();
    }

    /**
     * Returns the manager number.
     *
     * @return The manager number
     */
    public int getManagerNumber() {
        return manager.getManNum();
    }

    /**
     * Retrieves a configuration value.
     *
     * @param key The configuration key
     * @return The configuration value, or null if not found
     */
    public String getConfigValue(String key) {
        return manager.getConfigValue(key);
    }

    /**
     * Retrieves a configuration value with a default fallback.
     *
     * @param key The configuration key
     * @param defaultValue The default value if not found
     * @return The configuration value, or the default
     */
    public String getConfigValue(String key, String defaultValue) {
        return manager.getConfigValueOrDefault(key, defaultValue);
    }

    // ========== Datapoint Read Operations ==========

    /**
     * Creates a fluent builder for reading multiple datapoints.
     *
     * @return A new JDpGet builder
     */
    public JDpGet dpGet() {
        return JClient.dpGet();
    }

    /**
     * Reads a single datapoint value.
     *
     * @param dp The datapoint name
     * @return The datapoint value, or null if not found
     */
    public Variable dpGet(String dp) {
        return JClient.dpGet(dp);
    }

    /**
     * Reads multiple datapoint values.
     *
     * @param dps List of datapoint names
     * @return List of values in the same order
     */
    public List<Variable> dpGet(List<String> dps) {
        return JClient.dpGet(dps);
    }

    // ========== Datapoint Write Operations ==========

    /**
     * Creates a fluent builder for writing multiple datapoints.
     *
     * @return A new JDpSet builder
     */
    public JDpSet dpSet() {
        return JClient.dpSet();
    }

    /**
     * Writes a single datapoint value (fire-and-forget).
     *
     * @param dp The datapoint name
     * @param value The value to write
     * @return The JDpSet instance
     */
    public JDpSet dpSet(String dp, Object value) {
        return JClient.dpSet(dp, value);
    }

    /**
     * Writes a single datapoint value and waits for confirmation.
     *
     * @param dp The datapoint name
     * @param value The value to write
     * @return 0 on success, non-zero on failure
     */
    public int dpSetWait(String dp, Object value) {
        return JClient.dpSetWait(dp, value);
    }

    // ========== Datapoint Subscriptions ==========

    /**
     * Creates a fluent builder for subscribing to datapoint changes.
     *
     * @return A new JDpConnect builder
     */
    public JDpConnect dpConnect() {
        return JClient.dpConnect();
    }

    /**
     * Creates a fluent builder for subscribing to alerts.
     *
     * @return A new JAlertConnect builder
     */
    public JAlertConnect alertConnect() {
        return JClient.alertConnect();
    }

    // ========== Datapoint Query ==========

    /**
     * Executes a datapoint query.
     *
     * @param query The query string in WinCC OA SQL-like syntax
     * @return A JDpQuery instance
     */
    public JDpQuery dpQuery(String query) {
        return JClient.dpQuery(query);
    }

    /**
     * Creates a query-based subscription that triggers on the first match.
     *
     * @param query The query string
     * @return A JDpQueryConnect instance
     */
    public JDpQueryConnect dpQueryConnectSingle(String query) {
        return JClient.dpQueryConnectSingle(query);
    }

    /**
     * Creates a query-based subscription that triggers on all matches.
     *
     * @param query The query string
     * @return A JDpQueryConnect instance
     */
    public JDpQueryConnect dpQueryConnectAll(String query) {
        return JClient.dpQueryConnectAll(query);
    }

    // ========== Historical Data ==========

    /**
     * Creates a historical data query using TimeVar.
     *
     * @param start Start time
     * @param stop End time
     * @param maxCount Maximum number of values (0 = unlimited)
     * @return A JDpGetPeriod builder
     */
    public JDpGetPeriod dpGetPeriod(TimeVar start, TimeVar stop, int maxCount) {
        return JClient.dpGetPeriod(start, stop, maxCount);
    }

    /**
     * Creates a historical data query using Date objects.
     *
     * @param start Start time
     * @param stop End time
     * @param maxCount Maximum number of values (0 = unlimited)
     * @return A JDpGetPeriod builder
     */
    public JDpGetPeriod dpGetPeriod(Date start, Date stop, int maxCount) {
        return JClient.dpGetPeriod(start, stop, maxCount);
    }

    /**
     * Creates a historical data query using millisecond timestamps.
     *
     * @param start Start time in milliseconds
     * @param stop End time in milliseconds
     * @param maxCount Maximum number of values (0 = unlimited)
     * @return A JDpGetPeriod builder
     */
    public JDpGetPeriod dpGetPeriod(long start, long stop, int maxCount) {
        return JClient.dpGetPeriod(start, stop, maxCount);
    }

    // ========== Datapoint Metadata ==========

    /**
     * Returns datapoint names matching a pattern.
     *
     * @param pattern Wildcard pattern (e.g., "ExampleDP_*")
     * @return Array of matching datapoint names
     */
    public String[] dpNames(String pattern) {
        return JClient.dpNames(pattern);
    }

    /**
     * Returns datapoint names matching a pattern and type.
     *
     * @param pattern Wildcard pattern
     * @param type Datapoint type name
     * @return Array of matching datapoint names
     */
    public String[] dpNames(String pattern, String type) {
        return JClient.dpNames(pattern, type);
    }

    /**
     * Checks if a datapoint exists.
     *
     * @param dpName The datapoint name
     * @return true if the datapoint exists
     */
    public boolean dpExists(String dpName) {
        return JClient.dpExists(dpName);
    }

    /**
     * Retrieves the comment for a datapoint.
     *
     * @param dpid The datapoint identifier
     * @return The language-dependent comment
     */
    public LangTextVar dpGetComment(DpIdentifierVar dpid) {
        return JClient.dpGetComment(dpid);
    }

    // ========== Datapoint Type Management ==========

    /**
     * Creates a new datapoint type.
     *
     * @param definition The root element of the type definition
     * @return 0 on success, non-zero on failure
     */
    public int dpTypeCreate(DpTypeElement definition) {
        return JClient.dpTypeCreate(definition);
    }

    /**
     * Modifies an existing datapoint type.
     *
     * @param typeId The type ID
     * @param definition The element to add/modify
     * @param append If true, append to root; if false, replace
     * @return 0 on success, non-zero on failure
     */
    public int dpTypeChange(int typeId, DpTypeElement definition, boolean append) {
        return JClient.dpTypeChange(typeId, definition, append);
    }

    /**
     * Deletes a datapoint type by name.
     *
     * @param typeName The type name
     * @return 0 on success, non-zero on failure
     */
    public int dpTypeDelete(String typeName) {
        return JClient.dpTypeDelete(typeName);
    }

    /**
     * Deletes a datapoint type by ID.
     *
     * @param typeId The type ID
     * @return 0 on success, non-zero on failure
     */
    public int dpTypeDelete(int typeId) {
        return JClient.dpTypeDelete(typeId);
    }

    /**
     * Gets the type ID for a type name.
     *
     * @param typeName The type name
     * @return The type ID, or negative value if not found
     */
    public int dpTypeNameToId(String typeName) {
        return JClient.dpTypeNameToId(typeName);
    }

    /**
     * Gets the type definition as a tree structure.
     *
     * @param typeName The type name
     * @return The root DpTypeElement, or null if not found
     */
    public DpTypeElement dpTypeGet(String typeName) {
        return manager.dpTypeGetTree(typeName);
    }

    // ========== Datapoint Management ==========

    /**
     * Creates a new datapoint.
     *
     * @param dpName The datapoint name
     * @param typeName The datapoint type name
     * @return 0 on success, non-zero on failure
     */
    public int dpCreate(String dpName, String typeName) {
        return JClient.dpCreate(dpName, typeName);
    }

    /**
     * Deletes a datapoint.
     *
     * @param dpName The datapoint name
     * @return 0 on success, non-zero on failure
     */
    public int dpDelete(String dpName) {
        return JClient.dpDelete(dpName);
    }

    // ========== CNS View Management ==========

    /**
     * Creates a new CNS view.
     *
     * @param system System name (null for default)
     * @param viewId The view identifier
     * @param separator Path separator character
     * @param displayNames Multi-language display names
     * @return 0 on success, non-zero on failure
     */
    public int cnsCreateView(String system, String viewId, String separator, LangTextVar displayNames) {
        return JClient.cnsCreateView(system, viewId, separator, displayNames);
    }

    /**
     * Deletes a CNS view.
     *
     * @param system System name (null for default)
     * @param viewId The view identifier
     * @return 0 on success, non-zero on failure
     */
    public int cnsDeleteView(String system, String viewId) {
        return JClient.cnsDeleteView(system, viewId);
    }

    /**
     * Gets all views in a system.
     *
     * @param system System name (null for default)
     * @return Array of view identifiers
     */
    public String[] cnsGetViews(String system) {
        return JClient.cnsGetViews(system);
    }

    /**
     * Gets view display names.
     *
     * @param system System name (null for default)
     * @param viewId The view identifier
     * @return Multi-language display names
     */
    public LangTextVar cnsGetViewDisplayNames(String system, String viewId) {
        return JClient.cnsGetViewDisplayNames(system, viewId);
    }

    /**
     * Changes view display names.
     *
     * @param system System name (null for default)
     * @param viewId The view identifier
     * @param displayNames New display names
     * @return 0 on success, non-zero on failure
     */
    public int cnsChangeViewDisplayNames(String system, String viewId, LangTextVar displayNames) {
        return JClient.cnsChangeViewDisplayNames(system, viewId, displayNames);
    }

    /**
     * Gets view path separators.
     *
     * @param system System name (null for default)
     * @param viewId The view identifier
     * @return The separator string
     */
    public String cnsGetViewSeparators(String system, String viewId) {
        return JClient.cnsGetViewSeparators(system, viewId);
    }

    /**
     * Changes view path separators.
     *
     * @param system System name (null for default)
     * @param viewId The view identifier
     * @param separator New separator
     * @return 0 on success, non-zero on failure
     */
    public int cnsChangeViewSeparators(String system, String viewId, String separator) {
        return JClient.cnsChangeViewSeparators(system, viewId, separator);
    }

    // ========== CNS Tree Management ==========

    /**
     * Adds a tree to a view.
     *
     * @param system System name (null for default)
     * @param viewId The view identifier
     * @param nodeId The tree root node ID
     * @param nodeType Node type (see CnsDataIdentifier.Types)
     * @param dpId Datapoint identifier (null for structure node)
     * @param displayNames Multi-language display names
     * @return 0 on success, non-zero on failure
     */
    public int cnsAddTree(String system, String viewId, String nodeId, int nodeType,
                          DpIdentifierVar dpId, LangTextVar displayNames) {
        return JClient.cnsAddTree(system, viewId, nodeId, nodeType, dpId, displayNames);
    }

    /**
     * Deletes a tree or subtree.
     *
     * @param cnsPath The CNS path
     * @return 0 on success, non-zero on failure
     */
    public int cnsDeleteTree(String cnsPath) {
        return JClient.cnsDeleteTree(cnsPath);
    }

    /**
     * Gets all trees in a view.
     *
     * @param system System name (null for default)
     * @param viewId The view identifier
     * @return Array of tree root paths
     */
    public String[] cnsGetTrees(String system, String viewId) {
        return JClient.cnsGetTrees(system, viewId);
    }

    /**
     * Gets the root node of a tree.
     *
     * @param cnsPath A CNS path within the tree
     * @return The root node path
     */
    public String cnsGetRoot(String cnsPath) {
        return JClient.cnsGetRoot(cnsPath);
    }

    // ========== CNS Node Management ==========

    /**
     * Adds a node to a parent.
     *
     * @param parentPath The parent CNS path
     * @param nodeId The node identifier
     * @param nodeType Node type (see CnsDataIdentifier.Types)
     * @param dpId Datapoint identifier (null for structure node)
     * @param displayNames Multi-language display names
     * @return 0 on success, non-zero on failure
     */
    public int cnsAddNode(String parentPath, String nodeId, int nodeType,
                          DpIdentifierVar dpId, LangTextVar displayNames) {
        return JClient.cnsAddNode(parentPath, nodeId, nodeType, dpId, displayNames);
    }

    /**
     * Gets a CNS node by path.
     *
     * @param cnsPath The CNS path
     * @return The CnsNode, or null if not found
     */
    public CnsNode cnsGetNode(String cnsPath) {
        return JClient.cnsGetNode(cnsPath);
    }

    /**
     * Gets the data identifier for a CNS path.
     *
     * @param cnsPath The CNS path
     * @return The CnsDataIdentifier, or null if not found
     */
    public CnsDataIdentifier cnsGetId(String cnsPath) {
        return JClient.cnsGetId(cnsPath);
    }

    /**
     * Gets child nodes.
     *
     * @param cnsPath The parent CNS path
     * @return Array of child paths
     */
    public String[] cnsGetChildren(String cnsPath) {
        return JClient.cnsGetChildren(cnsPath);
    }

    /**
     * Gets the parent node.
     *
     * @param cnsPath The CNS path
     * @return The parent path, or null if at root
     */
    public String cnsGetParent(String cnsPath) {
        return JClient.cnsGetParent(cnsPath);
    }

    /**
     * Changes node data (datapoint link and type).
     *
     * @param cnsPath The CNS path
     * @param dpId New datapoint identifier
     * @param nodeType New node type
     * @return 0 on success, non-zero on failure
     */
    public int cnsChangeNodeData(String cnsPath, DpIdentifierVar dpId, int nodeType) {
        return JClient.cnsChangeNodeData(cnsPath, dpId, nodeType);
    }

    /**
     * Changes node display names.
     *
     * @param cnsPath The CNS path
     * @param displayNames New display names
     * @return 0 on success, non-zero on failure
     */
    public int cnsChangeNodeDisplayNames(String cnsPath, LangTextVar displayNames) {
        return JClient.cnsChangeNodeDisplayNames(cnsPath, displayNames);
    }

    // ========== CNS Search ==========

    /**
     * Searches nodes by name pattern.
     *
     * @param system System name (null for default)
     * @param viewId The view identifier
     * @param pattern Search pattern
     * @param searchMode Search mode (0=NAME, 1=DISPLAY_NAME, 2=ALL_NAMES)
     * @param langIdx Language index for display name search
     * @return Array of matching CNS paths
     */
    public String[] cnsGetNodesByName(String system, String viewId, String pattern,
                                       int searchMode, int langIdx) {
        return JClient.cnsGetNodesByName(system, viewId, pattern, searchMode, langIdx);
    }

    /**
     * Finds nodes by datapoint.
     *
     * @param system System name (null for default)
     * @param viewId The view identifier
     * @param dpId The datapoint identifier
     * @return Array of matching CNS paths
     */
    public String[] cnsGetNodesByData(String system, String viewId, DpIdentifierVar dpId) {
        return JClient.cnsGetNodesByData(system, viewId, dpId);
    }

    /**
     * Gets datapoint identifiers matching a pattern.
     *
     * @param system System name (null for default)
     * @param viewId The view identifier
     * @param pattern Search pattern
     * @param searchMode Search mode (0=NAME, 1=DISPLAY_NAME, 2=ALL_NAMES)
     * @param langIdx Language index for display name search
     * @return Array of matching datapoint identifiers
     */
    public DpIdentifierVar[] cnsGetIdSet(String system, String viewId, String pattern,
                                          int searchMode, int langIdx) {
        return JClient.cnsGetIdSet(system, viewId, pattern, searchMode, langIdx);
    }

    // ========== CNS Utilities ==========

    /**
     * Extracts parts of a CNS path.
     *
     * @param cnsPath The CNS path
     * @param mask Bitmask specifying which parts to extract
     * @param resolve If true, resolve display names
     * @return The extracted path parts
     */
    public String cnsSubStr(String cnsPath, int mask, boolean resolve) {
        return JClient.cnsSubStr(cnsPath, mask, resolve);
    }

    /**
     * Gets system display names.
     *
     * @param system System name (null for default)
     * @return Multi-language display names
     */
    public LangTextVar cnsGetSystemNames(String system) {
        return JClient.cnsGetSystemNames(system);
    }

    /**
     * Sets system display names.
     *
     * @param system System name (null for default)
     * @param displayNames New display names
     * @return 0 on success, non-zero on failure
     */
    public int cnsSetSystemNames(String system, LangTextVar displayNames) {
        return JClient.cnsSetSystemNames(system, displayNames);
    }

    /**
     * Validates a node ID.
     *
     * @param id The node ID to validate
     * @return true if valid
     */
    public boolean cnsCheckId(String id) {
        return JClient.cnsCheckId(id);
    }

    /**
     * Validates a display name.
     *
     * @param displayName The display name to validate
     * @return true if valid
     */
    public boolean cnsCheckName(String displayName) {
        return JClient.cnsCheckName(displayName);
    }

    /**
     * Validates a separator character.
     *
     * @param separator The separator to validate
     * @return true if valid
     */
    public boolean cnsCheckSeparator(char separator) {
        return JClient.cnsCheckSeparator(separator);
    }

    // ========== CNS Observers ==========

    /**
     * Adds a CNS change observer.
     *
     * @param observer The observer to add
     * @return The observer ID
     */
    public int cnsAddObserver(CnsObserver observer) {
        return JClient.cnsAddObserver(observer);
    }

    /**
     * Removes a CNS change observer.
     *
     * @param observerId The observer ID
     * @return 0 on success, non-zero on failure
     */
    public int cnsRemoveObserver(int observerId) {
        return JClient.cnsRemoveObserver(observerId);
    }

    // ========== User & Security ==========

    /**
     * Verifies user credentials.
     *
     * @param username The username
     * @param password The password
     * @return 0 if valid, -1 if invalid user, -2 if wrong password
     */
    public int checkPassword(String username, String password) {
        return JClient.checkPassword(username, password);
    }

    /**
     * Sets the current user ID.
     *
     * @param username The username
     * @param password The password
     * @return true if successful
     */
    public boolean setUserId(String username, String password) {
        return JClient.setUserId(username, password);
    }

    // ========== Logging (Static) ==========

    /**
     * Logs a message to the WinCC OA log system.
     *
     * @param prio Priority level
     * @param code Error code
     * @param text Message text
     */
    public static void log(ErrPrio prio, ErrCode code, String text) {
        JManager.log(prio, code, text);
    }

    /**
     * Logs an info message.
     *
     * @param message The message text
     */
    public static void log(String message) {
        JManager.log(ErrPrio.PRIO_INFO, ErrCode.NOERR, message);
    }

    /**
     * Logs an error message.
     *
     * @param message The error message
     */
    public static void logError(String message) {
        JManager.log(ErrPrio.PRIO_SEVERE, ErrCode.UNEXPECTEDSTATE, message);
    }

    /**
     * Logs an exception's stack trace.
     *
     * @param prio Priority level
     * @param code Error code
     * @param exception The exception to log
     */
    public static void logStackTrace(ErrPrio prio, ErrCode code, Throwable exception) {
        JManager.stackTrace(prio, code, exception);
    }

    /**
     * Logs an exception's stack trace with SEVERE priority.
     *
     * @param exception The exception to log
     */
    public static void logStackTrace(Throwable exception) {
        JManager.stackTrace(exception);
    }

    // ========== Redundancy State Listeners ==========

    /**
     * Adds a redundancy state listener.
     * <p>
     * The callback receives a boolean: true when becoming active, false when becoming passive.
     * </p>
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * oa.onRedundancyStateChanged(isActive -> {
     *     if (isActive) {
     *         System.out.println("Now ACTIVE");
     *     } else {
     *         System.out.println("Now PASSIVE");
     *     }
     * });
     * }</pre>
     *
     * @param callback The callback to invoke on state change (true=active, false=passive)
     */
    public void onRedundancyStateChanged(Consumer<Boolean> callback) {
        manager.addRedundancyStateListener(callback);
    }

    /**
     * Removes a redundancy state listener.
     *
     * @param callback The callback to remove
     */
    public void removeRedundancyStateListener(Consumer<Boolean> callback) {
        manager.removeRedundancyStateListener(callback);
    }
}

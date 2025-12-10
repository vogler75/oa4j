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

import at.rocworks.oa4j.var.*;
//import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Static utility class providing the main API for WinCC OA datapoint operations.
 * <p>
 * JClient provides a fluent API for reading, writing, and subscribing to WinCC OA
 * datapoints. All methods are static and thread-safe. The manager must be initialized
 * via {@link JManager#init(String[])} before using JClient methods.
 * </p>
 *
 * <h3>Reading datapoints:</h3>
 * <pre>{@code
 * // Simple read
 * Variable value = JClient.dpGet("System1:ExampleDP_Arg1.");
 *
 * // Fluent API for multiple reads
 * JDpMsgAnswer answer = JClient.dpGet()
 *     .add("ExampleDP_Arg1.")
 *     .add("ExampleDP_Arg2.")
 *     .await();
 * }</pre>
 *
 * <h3>Writing datapoints:</h3>
 * <pre>{@code
 * // Simple write (fire and forget)
 * JClient.dpSet("ExampleDP_Arg1.", 42);
 *
 * // Write and wait for confirmation
 * int retCode = JClient.dpSetWait("ExampleDP_Arg1.", 42);
 *
 * // Fluent API for multiple writes
 * JClient.dpSet()
 *     .add("ExampleDP_Arg1.", 100)
 *     .add("ExampleDP_Arg2.", "text")
 *     .send();
 * }</pre>
 *
 * <h3>Subscribing to changes (hotlink):</h3>
 * <pre>{@code
 * JClient.dpConnect()
 *     .add("ExampleDP_Arg1.")
 *     .action((JDpHLGroup hlg) -> {
 *         hlg.forEach(item -> System.out.println(item.getVariable()));
 *     })
 *     .connect();
 * }</pre>
 *
 * <h3>Querying datapoints:</h3>
 * <pre>{@code
 * JDpMsgAnswer answer = JClient.dpQuery("SELECT '_online.._value' FROM 'ExampleDP_*'").await();
 * }</pre>
 *
 * @author vogler
 * @see JManager
 * @see JDpGet
 * @see JDpSet
 * @see JDpConnect
 * @see JDpQuery
 */
public class JClient {

    /**
     * Checks if the manager is connected to WinCC OA.
     *
     * @return true if the manager is initialized and connected
     */
    public static boolean isConnected() {
        return JManager.getInstance()!=null && JManager.getInstance().isConnected();
    }

    // ========== dpGet ==========

    /**
     * Creates a new datapoint get request builder.
     * <p>
     * Use the fluent API to add datapoints and execute the request.
     * </p>
     *
     * <pre>{@code
     * JDpMsgAnswer answer = JClient.dpGet()
     *     .add("dp1.")
     *     .add("dp2.")
     *     .await();
     * }</pre>
     *
     * @return a new JDpGet builder instance
     */
    public static JDpGet dpGet() {
        return (new JDpGet());
    }

    /**
     * Reads a single datapoint value synchronously.
     *
     * @param dp the datapoint name (e.g., "System1:ExampleDP_Arg1.")
     * @return the datapoint value, or null if not found or error
     */
    public static Variable dpGet(String dp) {
        JDpMsgAnswer answer = (new JDpGet()).add(dp).await();
        return ( answer.size()>0 ) ? answer.getItem(0).getVariable() : null;
    }

    /**
     * Reads a single datapoint value into a variable pointer.
     *
     * @param dp  the datapoint name
     * @param var output parameter that receives the value
     * @return return code (0 = success)
     */
    public static int dpGet(String dp, VariablePtr var) {
        JDpMsgAnswer answer = (new JDpGet()).add(dp).await();
        if ( answer.size()>0 ) var.set(answer.getItem(0).getVariable());
        return answer.getRetCode();
    }

    /**
     * Reads multiple datapoint values synchronously.
     *
     * @param dps list of datapoint names to read
     * @return list of values in the same order as the input
     */
    public static List<Variable> dpGet(List<String> dps) {
        JDpGet dpGet = new JDpGet();
        dps.forEach((dp)->dpGet.add(dp));
        JDpMsgAnswer answer = dpGet.await();
        List<Variable> ret = new ArrayList<Variable>(answer.size());
        answer.forEach((item)->ret.add(item.getVariable()));
        return ret;
    }

    // ========== dpGetPeriod ==========

    /**
     * Creates a historical data query for a time period.
     * <p>
     * Retrieves archived values between start and stop times.
     * </p>
     *
     * @param start start time of the period
     * @param stop  end time of the period
     * @param num   maximum number of values to retrieve (0 = unlimited)
     * @return a new JDpGetPeriod builder instance
     */
    public static JDpGetPeriod dpGetPeriod(TimeVar start, TimeVar stop, int num) {
        return (new JDpGetPeriod(start, stop, num));
    }

    /**
     * Creates a historical data query using Java Date objects.
     *
     * @param start start time as Date
     * @param stop  end time as Date
     * @param num   maximum number of values (0 = unlimited)
     * @return a new JDpGetPeriod builder instance
     */
    public static JDpGetPeriod dpGetPeriod(Date start, Date stop, int num) {
        return dpGetPeriod(new TimeVar(start), new TimeVar(stop), num);
    }

    /**
     * Creates a historical data query using millisecond timestamps.
     *
     * @param start start time in milliseconds since epoch
     * @param stop  end time in milliseconds since epoch
     * @param num   maximum number of values (0 = unlimited)
     * @return a new JDpGetPeriod builder instance
     */
    public static JDpGetPeriod dpGetPeriod(long start, long stop, int num) {
        return dpGetPeriod(new TimeVar(start), new TimeVar(stop), num);
    }

    // ========== dpSet ==========

    /**
     * Creates a new datapoint set request builder.
     * <p>
     * Use the fluent API to add datapoint/value pairs and execute.
     * </p>
     *
     * <pre>{@code
     * JClient.dpSet()
     *     .add("dp1.", 100)
     *     .add("dp2.", "text")
     *     .send();
     * }</pre>
     *
     * @return a new JDpSet builder instance
     */
    public static JDpSet dpSet() {
        return (new JDpSet());
    }

    /**
     * Writes a single datapoint value asynchronously (fire and forget).
     *
     * @param dp  the datapoint name
     * @param var the value to write (automatically converted to Variable)
     * @return the JDpSet instance (already sent)
     */
    public static JDpSet dpSet(String dp, Object var) {
        return (new JDpSet()).add(dp, Variable.newVariable(var)).send();
    }

    /**
     * Writes a single datapoint value synchronously and waits for confirmation.
     *
     * @param dp  the datapoint name
     * @param var the value to write
     * @return return code (0 = success)
     */
    public static int dpSetWait(String dp, Object var) {
        return dpSet(dp, var).await().getRetCode();
    }

    /**
     * Writes multiple datapoint values asynchronously (fire and forget).
     *
     * @param dps    array of datapoint names
     * @param values array of values to write (must match length of dps)
     * @return the JDpSet instance (already sent)
     * @throws IllegalArgumentException if arrays have different lengths
     */
    public static JDpSet dpSet(String[] dps, Object[] values) {
        if (dps.length != values.length) {
            throw new IllegalArgumentException("Arrays must have same length: dps=" + dps.length + ", values=" + values.length);
        }
        JDpSet builder = new JDpSet();
        for (int i = 0; i < dps.length; i++) {
            builder.add(dps[i], Variable.newVariable(values[i]));
        }
        return builder.send();
    }

    /**
     * Writes multiple datapoint values synchronously and waits for confirmation.
     *
     * @param dps    array of datapoint names
     * @param values array of values to write (must match length of dps)
     * @return return code (0 = success)
     * @throws IllegalArgumentException if arrays have different lengths
     */
    public static int dpSetWait(String[] dps, Object[] values) {
        if (dps.length != values.length) {
            throw new IllegalArgumentException("Arrays must have same length: dps=" + dps.length + ", values=" + values.length);
        }
        JDpSet builder = new JDpSet();
        for (int i = 0; i < dps.length; i++) {
            builder.add(dps[i], Variable.newVariable(values[i]));
        }
        return builder.await().getRetCode();
    }

    /**
     * Writes multiple datapoint values asynchronously (fire and forget).
     *
     * @param pairs list of datapoint name and value pairs
     * @return the JDpSet instance (already sent)
     */
    public static JDpSet dpSet(List<Map.Entry<String, Object>> pairs) {
        JDpSet builder = new JDpSet();
        for (Map.Entry<String, Object> pair : pairs) {
            builder.add(pair.getKey(), Variable.newVariable(pair.getValue()));
        }
        return builder.send();
    }

    /**
     * Writes multiple datapoint values synchronously and waits for confirmation.
     *
     * @param pairs list of datapoint name and value pairs
     * @return return code (0 = success)
     */
    public static int dpSetWait(List<Map.Entry<String, Object>> pairs) {
        JDpSet builder = new JDpSet();
        for (Map.Entry<String, Object> pair : pairs) {
            builder.add(pair.getKey(), Variable.newVariable(pair.getValue()));
        }
        return builder.await().getRetCode();
    }

    // ========== dpConnect ==========

    /**
     * Creates a new datapoint connection (hotlink) builder.
     * <p>
     * Hotlinks provide real-time notifications when datapoint values change.
     * </p>
     *
     * <pre>{@code
     * JClient.dpConnect()
     *     .add("ExampleDP_Arg1.")
     *     .action(hlg -> {
     *         hlg.forEach(item -> {
     *             System.out.println(item.getDpName() + " = " + item.getVariable());
     *         });
     *     })
     *     .connect();
     * }</pre>
     *
     * @return a new JDpConnect builder instance
     */
    public static JDpConnect dpConnect() {
        return (new JDpConnect());
    }

    // ========== alertConnect ==========

    /**
     * Creates a new alert connection builder.
     * <p>
     * Subscribes to alarm/alert notifications from WinCC OA.
     * </p>
     *
     * @return a new JAlertConnect builder instance
     */
    public static JAlertConnect alertConnect() {
        return (new JAlertConnect());
    }

    // ========== dpQuery ==========

    /**
     * Executes a datapoint query using WinCC OA SQL-like syntax.
     * <p>
     * The query is sent immediately. Call {@code .await()} to get results.
     * </p>
     *
     * <pre>{@code
     * JDpMsgAnswer answer = JClient.dpQuery(
     *     "SELECT '_online.._value' FROM 'ExampleDP_*'"
     * ).await();
     * }</pre>
     *
     * @param query the query string in WinCC OA query syntax
     * @return a JDpQuery instance (already sent)
     */
    public static JDpQuery dpQuery(String query) {
        return (new JDpQuery(query).send());
    }

    // ========== dpQueryConnect ==========

    /**
     * Creates a query-based hotlink that triggers only on the first matching change.
     *
     * @param query the query string
     * @return a new JDpQueryConnectSingle builder instance
     */
    public static JDpQueryConnect dpQueryConnectSingle(String query) {
        return (new JDpQueryConnectSingle(query));
    }

    /**
     * Creates a query-based hotlink that triggers on all matching changes.
     *
     * @param query the query string
     * @return a new JDpQueryConnectAll builder instance
     */
    public static JDpQueryConnect dpQueryConnectAll(String query) {
        return (new JDpQueryConnectAll(query));
    }

    // ========== dpNames ==========

    /**
     * Returns datapoint names matching a pattern.
     *
     * @param pattern wildcard pattern (e.g., "ExampleDP_*")
     * @return array of matching datapoint names
     */
    public static String[] dpNames(String pattern) {
        return (String[])JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiGetIdSet(pattern);
        });
    }

    /**
     * Returns datapoint names matching a pattern and type.
     *
     * @param pattern wildcard pattern (e.g., "ExampleDP_*")
     * @param type    datapoint type name to filter by
     * @return array of matching datapoint names
     */
    public static String[] dpNames(String pattern, String type) {
        return (String[])JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiGetIdSetOfType(pattern, type);
        });
    }

    // ========== dpComment ==========

    /**
     * Retrieves the comment/description for a datapoint.
     *
     * @param dpid the datapoint identifier
     * @return the language-dependent comment text
     */
    public static LangTextVar dpGetComment(DpIdentifierVar dpid) {
        return (LangTextVar)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiDpGetComment(dpid);
        });
    }

    /**
     * Verfiy password. Check if the given passwd is valid for the requested user id
     * @param username
     * @param password
     * @return 0...Ok, -1...invalid user, -2...wrong password
     */
    public static int checkPassword(String username, String password) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().checkPassword(username, password);
        });
    }

    /**
     * A new user id is set when (id matches passwd) or
     * (currentId is ROOT_USER and newUserId exists) or
     * (newUserId is DEFAULT_USER).
     * @param username
     * @param password
     * @return true if user has been set
     */
    public static boolean setUserId(String username, String password) {
        return (boolean)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().setUserId(username, password);
        });
    }

    // ========== CNS (Common Name Service) ==========

    /**
     * Create a new CNS view.
     * @param system System name (e.g., "System1:") or null for default system
     * @param viewId The unique identifier for the view
     * @param separator The separator character for display paths
     * @param displayNames Display names for the view (LangTextVar), or null
     * @return 0 on success, -1 on failure
     */
    public static int cnsCreateView(String system, String viewId, String separator, LangTextVar displayNames) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsCreateView(system, viewId, separator, displayNames);
        });
    }

    /**
     * Delete a CNS view.
     * @param system System name or null for default system
     * @param viewId The view identifier to delete
     * @return 0 on success, -1 on failure
     */
    public static int cnsDeleteView(String system, String viewId) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsDeleteView(system, viewId);
        });
    }

    /**
     * Get all views in a system.
     * @param system System name or null for default system
     * @return Array of view identifiers, or null on failure
     */
    public static String[] cnsGetViews(String system) {
        return (String[])JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsGetViews(system);
        });
    }

    /**
     * Get display names of a view.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @return LangTextVar with display names, or null on failure
     */
    public static LangTextVar cnsGetViewDisplayNames(String system, String viewId) {
        return (LangTextVar)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsGetViewDisplayNames(system, viewId);
        });
    }

    /**
     * Change display names of a view.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @param displayNames New display names
     * @return 0 on success, -1 on failure
     */
    public static int cnsChangeViewDisplayNames(String system, String viewId, LangTextVar displayNames) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsChangeViewDisplayNames(system, viewId, displayNames);
        });
    }

    /**
     * Get separators of a view.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @return Separator string, or null on failure
     */
    public static String cnsGetViewSeparators(String system, String viewId) {
        return (String)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsGetViewSeparators(system, viewId);
        });
    }

    /**
     * Change separators of a view.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @param separator New separator string
     * @return 0 on success, -1 on failure
     */
    public static int cnsChangeViewSeparators(String system, String viewId, String separator) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsChangeViewSeparators(system, viewId, separator);
        });
    }

    /**
     * Add a tree to a view.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @param nodeId The tree root node identifier
     * @param nodeType Node type (CnsDataIdentifier.Types value)
     * @param dpId Datapoint identifier to link, or null
     * @param displayNames Display names for the tree root
     * @return 0 on success, -1 on failure
     */
    public static int cnsAddTree(String system, String viewId, String nodeId, int nodeType,
                                  DpIdentifierVar dpId, LangTextVar displayNames) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsAddTree(system, viewId, nodeId, nodeType, dpId, displayNames);
        });
    }

    /**
     * Delete a tree or node from CNS.
     * @param cnsPath Full CNS path to the tree/node
     * @return 0 on success, -1 on failure
     */
    public static int cnsDeleteTree(String cnsPath) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsDeleteTree(cnsPath);
        });
    }

    /**
     * Get all trees in a view.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @return Array of tree root paths, or null on failure
     */
    public static String[] cnsGetTrees(String system, String viewId) {
        return (String[])JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsGetTrees(system, viewId);
        });
    }

    /**
     * Get the root node of a tree.
     * @param cnsPath Any CNS path within the tree
     * @return Path to root node, or null on failure
     */
    public static String cnsGetRoot(String cnsPath) {
        return (String)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsGetRoot(cnsPath);
        });
    }

    /**
     * Add a node to a parent node.
     * @param parentPath CNS path to the parent node
     * @param nodeId The new node identifier
     * @param nodeType Node type (CnsDataIdentifier.Types value)
     * @param dpId Datapoint identifier to link, or null
     * @param displayNames Display names for the node
     * @return 0 on success, -1 on failure
     */
    public static int cnsAddNode(String parentPath, String nodeId, int nodeType,
                                  DpIdentifierVar dpId, LangTextVar displayNames) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsAddNode(parentPath, nodeId, nodeType, dpId, displayNames);
        });
    }

    /**
     * Get children of a node.
     * @param cnsPath CNS path to the parent node
     * @return Array of child node paths, or null on failure
     */
    public static String[] cnsGetChildren(String cnsPath) {
        return (String[])JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsGetChildren(cnsPath);
        });
    }

    /**
     * Get parent of a node.
     * @param cnsPath CNS path to the node
     * @return Path to parent node, or null on failure
     */
    public static String cnsGetParent(String cnsPath) {
        return (String)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsGetParent(cnsPath);
        });
    }

    /**
     * Change node data (datapoint and type).
     * @param cnsPath CNS path to the node
     * @param dpId New datapoint identifier, or null
     * @param nodeType New node type
     * @return 0 on success, -1 on failure
     */
    public static int cnsChangeNodeData(String cnsPath, DpIdentifierVar dpId, int nodeType) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsChangeNodeData(cnsPath, dpId, nodeType);
        });
    }

    /**
     * Change node display names.
     * @param cnsPath CNS path to the node
     * @param displayNames New display names
     * @return 0 on success, -1 on failure
     */
    public static int cnsChangeNodeDisplayNames(String cnsPath, LangTextVar displayNames) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsChangeNodeDisplayNames(cnsPath, displayNames);
        });
    }

    /**
     * Get a CNS node by path.
     * @param cnsPath Full CNS path
     * @return CnsNode object, or null if not found
     */
    public static CnsNode cnsGetNode(String cnsPath) {
        return (CnsNode)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsGetNode(cnsPath);
        });
    }

    /**
     * Get datapoint identifier for a CNS path.
     * @param cnsPath Full CNS path
     * @return CnsDataIdentifier object, or null if not found
     */
    public static CnsDataIdentifier cnsGetId(String cnsPath) {
        return (CnsDataIdentifier)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsGetId(cnsPath);
        });
    }

    /**
     * Search nodes by name pattern.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @param pattern Name pattern to search
     * @param searchMode Search mode (0=NAME, 1=DISPLAY_NAME, 2=ALL_NAMES)
     * @param langIdx Language index for display name search
     * @return Array of matching node paths, or null on failure
     */
    public static String[] cnsGetNodesByName(String system, String viewId, String pattern,
                                              int searchMode, int langIdx) {
        return (String[])JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsGetNodesByName(system, viewId, pattern, searchMode, langIdx);
        });
    }

    /**
     * Find nodes by datapoint.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @param dpId Datapoint identifier to search for
     * @return Array of matching node paths, or null on failure
     */
    public static String[] cnsGetNodesByData(String system, String viewId, DpIdentifierVar dpId) {
        return (String[])JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsGetNodesByData(system, viewId, dpId);
        });
    }

    /**
     * Extract parts of a CNS path.
     * @param cnsPath Full CNS path
     * @param mask Bitmask for parts to extract
     * @param resolve Whether to resolve display names
     * @return Extracted path substring, or null on failure
     */
    public static String cnsSubStr(String cnsPath, int mask, boolean resolve) {
        return (String)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsSubStr(cnsPath, mask, resolve);
        });
    }

    /**
     * Get system display names.
     * @param system System name or null for default system
     * @return LangTextVar with system display names, or null on failure
     */
    public static LangTextVar cnsGetSystemNames(String system) {
        return (LangTextVar)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsGetSystemNames(system);
        });
    }

    /**
     * Set system display names.
     * @param system System name or null for default system
     * @param displayNames New display names
     * @return 0 on success, -1 on failure
     */
    public static int cnsSetSystemNames(String system, LangTextVar displayNames) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsSetSystemNames(system, displayNames);
        });
    }

    /**
     * Check if a node ID is valid.
     * @param id Node ID to validate
     * @return true if valid
     */
    public static boolean cnsCheckId(String id) {
        return (boolean)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsCheckId(id);
        });
    }

    /**
     * Check if a display name is valid.
     * @param displayName Display name to validate
     * @return true if valid
     */
    public static boolean cnsCheckName(String displayName) {
        return (boolean)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsCheckName(displayName);
        });
    }

    /**
     * Check if a separator character is valid.
     * @param separator Separator character to validate
     * @return true if valid
     */
    public static boolean cnsCheckSeparator(char separator) {
        return (boolean)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsCheckSeparator(separator);
        });
    }

    /**
     * Get datapoint identifiers matching a pattern.
     * This is more efficient than cnsGetNodesByName + cnsGetId when you only need the DpIdentifiers.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @param pattern Name pattern to search
     * @param searchMode Search mode (0=NAME, 1=DISPLAY_NAME, 2=ALL_NAMES)
     * @param langIdx Language index for display name search
     * @return Array of DpIdentifierVar objects, or null on failure
     */
    public static DpIdentifierVar[] cnsGetIdSet(String system, String viewId, String pattern,
                                                 int searchMode, int langIdx) {
        return (DpIdentifierVar[])JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsGetIdSet(system, viewId, pattern, searchMode, langIdx);
        });
    }

    /**
     * Add an observer to receive CNS change notifications.
     * The observer object must implement a method: void onCnsChange(String path, int changeType)
     * @param observer Object with onCnsChange(String, int) method (typically implements CnsObserver)
     * @return Observer ID (for removal), or -1 on failure
     */
    public static int cnsAddObserver(Object observer) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsAddObserver(observer);
        });
    }

    /**
     * Remove a CNS observer.
     * @param observerId The observer ID returned by cnsAddObserver
     * @return 0 on success, -1 on failure
     */
    public static int cnsRemoveObserver(int observerId) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().apiCnsRemoveObserver(observerId);
        });
    }

    // ========== Datapoint Type Management ==========

    /**
     * Create a new datapoint type.
     * @param definition The type definition tree (root DpTypeElement)
     * @return 0 on success, -1 on failure
     */
    public static int dpTypeCreate(DpTypeElement definition) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().dpTypeCreate(definition);
        });
    }

    /**
     * Create a new datapoint type.
     * @param definition The type definition tree (root DpTypeElement)
     * @param system System name or null for default system
     * @return 0 on success, -1 on failure
     */
    public static int dpTypeCreate(DpTypeElement definition, String system) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().dpTypeCreate(definition, system);
        });
    }

    /**
     * Modify an existing datapoint type by appending elements under root.
     * @param typeId The datapoint type ID to modify
     * @param definition The new type definition to append
     * @return 0 on success, -1 on failure
     */
    public static int dpTypeChange(int typeId, DpTypeElement definition) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().dpTypeChange(typeId, definition);
        });
    }

    /**
     * Modify an existing datapoint type.
     * @param typeId The datapoint type ID to modify
     * @param definition The new type definition
     * @param append If true, append definition under root; if false, replace entire type
     * @return 0 on success, -1 on failure
     */
    public static int dpTypeChange(int typeId, DpTypeElement definition, boolean append) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().dpTypeChange(typeId, definition, append);
        });
    }

    /**
     * Delete a datapoint type by ID.
     * @param typeId The datapoint type ID to delete
     * @return 0 on success, -1 on failure
     */
    public static int dpTypeDelete(int typeId) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().dpTypeDelete(typeId);
        });
    }

    /**
     * Delete a datapoint type by name.
     * @param typeName The datapoint type name to delete
     * @return 0 on success, -1 on failure
     */
    public static int dpTypeDelete(String typeName) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().dpTypeDelete(typeName);
        });
    }

    /**
     * Get the type ID for a datapoint type name.
     * @param typeName The datapoint type name
     * @return The type ID, or -1 if not found
     */
    public static int dpTypeNameToId(String typeName) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().dpTypeNameToId(typeName);
        });
    }

    // ========== Datapoint Management ==========

    /**
     * Create a new datapoint.
     * @param dpName The name of the datapoint to create
     * @param dpTypeName The datapoint type name
     * @return 0 on success, -1 on failure
     */
    public static int dpCreate(String dpName, String dpTypeName) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().dpCreate(dpName, dpTypeName);
        });
    }

    /**
     * Create a new datapoint.
     * @param dpName The name of the datapoint to create
     * @param dpTypeName The datapoint type name
     * @param system System name or null for default system
     * @return 0 on success, -1 on failure
     */
    public static int dpCreate(String dpName, String dpTypeName, String system) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().dpCreate(dpName, dpTypeName, system);
        });
    }

    /**
     * Delete a datapoint.
     * @param dpName The name of the datapoint to delete
     * @return 0 on success, -1 on failure
     */
    public static int dpDelete(String dpName) {
        return (int)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().dpDelete(dpName);
        });
    }

    /**
     * Check if a datapoint exists.
     * @param dpName The datapoint name to check
     * @return true if the datapoint exists
     */
    public static boolean dpExists(String dpName) {
        return (boolean)JManager.getInstance().executeTask(()->{
            return JManager.getInstance().dpExists(dpName);
        });
    }
}

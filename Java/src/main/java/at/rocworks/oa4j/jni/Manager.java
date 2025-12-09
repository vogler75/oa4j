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
package at.rocworks.oa4j.jni;

import at.rocworks.oa4j.base.JDpVCItem;
import at.rocworks.oa4j.base.JHotLinkWaitForAnswer;
import at.rocworks.oa4j.var.DpIdentifierVar;
import at.rocworks.oa4j.var.DpTypeElement;
import at.rocworks.oa4j.var.DpTypeResult;
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.LangTextVar;
import at.rocworks.oa4j.var.TimeVar;
import at.rocworks.oa4j.var.Variable;

/**
 *
 * @author vogler
 */
public abstract class Manager {
    public static native String apiGetVersion(); // Runtime Version (PVSS_VERSION)

    private static Boolean _isV4 = null;
    private static Boolean _isV3 = null;

    public static boolean isV3() {
        return (_isV3==null ? _isV3=(apiGetVersion().startsWith("3.")) : _isV3);
    }

    public static boolean isV4() {
        return (_isV4==null ? _isV4=(apiGetVersion().startsWith("4.")) : _isV4);
    }

    public native String apiGetLogPath();
    public native String apiGetDataPath();
    public native String apiGetConfigValue(String key);

    /**
     * Get the system number for a system name.
     * @param systemName System name (e.g., "System1" or "System1:"), or null for default system
     * @return System number, or default system number if name is null/empty
     */
    public native int apiGetSystemNum(String systemName);

    public int apiStartup(int manType, String[] argv) {
        return apiStartup(manType, argv, true, true, true, false);
    }
    public native int apiStartup(int manType, String[] argv,
                                 boolean connectToData, boolean connectToEvent,
                                 boolean initResources, boolean debugFlag);
    public native int apiShutdown();
    
    public native void apiDispatch(int sec, int usec);    
    
    public native int apiDpGet(JHotLinkWaitForAnswer hdl, DpIdentifierVar[] dps);
    public native int apiDpSet(JHotLinkWaitForAnswer hdl, JDpVCItem[] dps);
    public native int apiDpSetTimed(JHotLinkWaitForAnswer hdl, TimeVar originTime, JDpVCItem[] dps);
    public native int apiDpQuery(JHotLinkWaitForAnswer hdl, String query);
    public native int apiDpGetPeriod(JHotLinkWaitForAnswer hdl, TimeVar start, TimeVar stop, int num, DpIdentifierVar[] dps);

    public native int apiDpConnect(JHotLinkWaitForAnswer hdl, String dp);
    public native int apiDpDisconnect(JHotLinkWaitForAnswer hdl, String dp);
    
    public native int apiDpConnectArray(JHotLinkWaitForAnswer hdl, String[] dps);    
    public native int apiDpDisconnectArray(JHotLinkWaitForAnswer hdl, String[] dps);    
    
    public native int apiDpQueryConnectSingle(JHotLinkWaitForAnswer hdl, boolean values, String query);
    public native int apiDpQueryConnectAll(JHotLinkWaitForAnswer hdl, boolean values, String query);
    public native int apiDpQueryDisconnect(JHotLinkWaitForAnswer hdl);
    
    public native int apiAlertConnect(JHotLinkWaitForAnswer hdl, String[] dps);
    public native int apiAlertDisconnect(JHotLinkWaitForAnswer hdl, String[] dps);
    
    public native String[] apiGetIdSet(String pattern);    
    public native String[] apiGetIdSetOfType(String pattern, String type);    
    
    public native LangTextVar apiDpGetComment(DpIdentifierVar dp);

    /**
     * Get the type definition of a datapoint type by type name as a tree structure.
     * Returns the complete type structure including all elements and their types.
     *
     * @param typeName The name of the datapoint type
     * @param includeTypeRef If true, include elements from referenced types
     * @return The root element of the type definition tree, or null if the type does not exist
     */
    public native DpTypeElement apiDpTypeGet(String typeName, boolean includeTypeRef);

    /**
     * Get the type definition of a datapoint type by type name.
     * Returns element names and types organized by hierarchy level, matching the
     * WinCC OA Control script function:
     * int dpTypeGet(string name, dyn_dyn_string &elements, dyn_dyn_int &types, bool includeSubTypes)
     *
     * @param typeName The name of the datapoint type
     * @param includeSubTypes If true, include elements from referenced sub-types
     * @return DpTypeResult containing elements and types by level, or null if the type does not exist
     */
    public native DpTypeResult apiDpTypeGetFlat(String typeName, boolean includeSubTypes);

    /**
     * Verfiy password. Check if the given passwd is valid for the requested user id
     * @param username
     * @param password
     * @return 0...Ok, -1...invalid user, -2...wrong password
     */
    public native int checkPassword(String username, String password);

    /**
     * A new user id is set when (id matches passwd) or
     * (currentId is ROOT_USER and newUserId exists) or
     * (newUserId is DEFAULT_USER).
     * @param username
     * @param password
     * @return true if user has been set
     */
    public native boolean setUserId(String username, String password);
    
    public native void apiDoReceiveSysMsg(long cPtrSysMsg);
    public native void apiDoReceiveDpMsg(long cPtrDpMsg);
    
    public native int apiSendArchivedDPs(DynVar elements, boolean isAlert);
    
    private native void apiSetManagerState(int state);
    public void apiSetManagerState(ManagerState state) {
        apiSetManagerState(state.value);
    }
       
    // callbacks from API   
    public abstract boolean doReceiveSysMsg(long cPtrSysMsg);    
    public abstract boolean doReceiveDpMsg(long cPtrDpMsg);

    public int callbackAnswer(int id, int idx) {    
        return callbackAnswer(id, idx, null, null, 0);
    }
    abstract public int callbackAnswer(int id, int idx, DpIdentifierVar dpid, Variable var, long time);
    abstract public int callbackAnswerError(int id, int code, String text);

    public int callbackHotlink(int id, int idx) {
        return callbackHotlink(id, idx, null, null);
    }
    abstract public int callbackHotlink(int id, int idx, DpIdentifierVar dpid, Variable var);  
    
    public native int apiProcessHotlinkGroup(int id, long ptrDpHlGroup);
    public abstract int callbackHotlinkGroup(int id, long ptrDpHlGroup);

    public native int apiGetConnectionState();
    public native int apiIsActiveConnection();

    public native void apiLog(int prio, long state, String text);

    // ========== CNS - View Management ==========

    /**
     * Create a new CNS view.
     * @param system System name (e.g., "System1:") or null for default system
     * @param viewId The unique identifier for the view
     * @param separator The separator character for display paths
     * @param displayNames Display names for the view (LangTextVar), or null
     * @return 0 on success, -1 on failure
     */
    public native int apiCnsCreateView(String system, String viewId, String separator, LangTextVar displayNames);

    /**
     * Delete a CNS view.
     * @param system System name or null for default system
     * @param viewId The view identifier to delete
     * @return 0 on success, -1 on failure
     */
    public native int apiCnsDeleteView(String system, String viewId);

    /**
     * Get all views in a system.
     * @param system System name or null for default system
     * @return Array of view identifiers, or null on failure
     */
    public native String[] apiCnsGetViews(String system);

    /**
     * Get display names of a view.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @return LangTextVar with display names, or null on failure
     */
    public native LangTextVar apiCnsGetViewDisplayNames(String system, String viewId);

    /**
     * Change display names of a view.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @param displayNames New display names
     * @return 0 on success, -1 on failure
     */
    public native int apiCnsChangeViewDisplayNames(String system, String viewId, LangTextVar displayNames);

    /**
     * Get separators of a view.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @return Separator string, or null on failure
     */
    public native String apiCnsGetViewSeparators(String system, String viewId);

    /**
     * Change separators of a view.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @param separator New separator string
     * @return 0 on success, -1 on failure
     */
    public native int apiCnsChangeViewSeparators(String system, String viewId, String separator);

    // ========== CNS - Tree Management ==========

    /**
     * Add a tree to a view.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @param nodeId The tree root node identifier
     * @param nodeType Node type (CNSDataIdentifier.Types value)
     * @param dpId Datapoint identifier to link, or null
     * @param displayNames Display names for the tree root
     * @return 0 on success, -1 on failure
     */
    public native int apiCnsAddTree(String system, String viewId, String nodeId, int nodeType,
                                    DpIdentifierVar dpId, LangTextVar displayNames);

    /**
     * Delete a tree or node from CNS.
     * @param cnsPath Full CNS path to the tree/node
     * @return 0 on success, -1 on failure
     */
    public native int apiCnsDeleteTree(String cnsPath);

    /**
     * Get all trees in a view.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @return Array of tree root paths, or null on failure
     */
    public native String[] apiCnsGetTrees(String system, String viewId);

    /**
     * Get the root node of a tree.
     * @param cnsPath Any CNS path within the tree
     * @return Path to root node, or null on failure
     */
    public native String apiCnsGetRoot(String cnsPath);

    // ========== CNS - Node Management ==========

    /**
     * Add a node to a parent node.
     * @param parentPath CNS path to the parent node
     * @param nodeId The new node identifier
     * @param nodeType Node type (CNSDataIdentifier.Types value)
     * @param dpId Datapoint identifier to link, or null
     * @param displayNames Display names for the node
     * @return 0 on success, -1 on failure
     */
    public native int apiCnsAddNode(String parentPath, String nodeId, int nodeType,
                                    DpIdentifierVar dpId, LangTextVar displayNames);

    /**
     * Get children of a node.
     * @param cnsPath CNS path to the parent node
     * @return Array of child node paths, or null on failure
     */
    public native String[] apiCnsGetChildren(String cnsPath);

    /**
     * Get parent of a node.
     * @param cnsPath CNS path to the node
     * @return Path to parent node, or null on failure
     */
    public native String apiCnsGetParent(String cnsPath);

    /**
     * Change node data (datapoint and type).
     * @param cnsPath CNS path to the node
     * @param dpId New datapoint identifier, or null
     * @param nodeType New node type
     * @return 0 on success, -1 on failure
     */
    public native int apiCnsChangeNodeData(String cnsPath, DpIdentifierVar dpId, int nodeType);

    /**
     * Change node display names.
     * @param cnsPath CNS path to the node
     * @param displayNames New display names
     * @return 0 on success, -1 on failure
     */
    public native int apiCnsChangeNodeDisplayNames(String cnsPath, LangTextVar displayNames);

    // ========== CNS - Query & Navigation ==========

    /**
     * Get a CNS node by path.
     * @param cnsPath Full CNS path
     * @return CnsNode object, or null if not found
     */
    public native Object apiCnsGetNode(String cnsPath);

    /**
     * Get datapoint identifier for a CNS path.
     * @param cnsPath Full CNS path
     * @return CnsDataIdentifier object, or null if not found
     */
    public native Object apiCnsGetId(String cnsPath);

    /**
     * Search nodes by name pattern.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @param pattern Name pattern to search
     * @param searchMode Search mode (0=NAME, 1=DISPLAY_NAME, 2=ALL_NAMES)
     * @param langIdx Language index for display name search
     * @return Array of matching node paths, or null on failure
     */
    public native String[] apiCnsGetNodesByName(String system, String viewId, String pattern,
                                                 int searchMode, int langIdx);

    /**
     * Find nodes by datapoint.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @param dpId Datapoint identifier to search for
     * @return Array of matching node paths, or null on failure
     */
    public native String[] apiCnsGetNodesByData(String system, String viewId, DpIdentifierVar dpId);

    /**
     * Extract parts of a CNS path.
     * @param cnsPath Full CNS path
     * @param mask Bitmask for parts to extract
     * @param resolve Whether to resolve display names
     * @return Extracted path substring, or null on failure
     */
    public native String apiCnsSubStr(String cnsPath, int mask, boolean resolve);

    // ========== CNS - System Names ==========

    /**
     * Get system display names.
     * @param system System name or null for default system
     * @return LangTextVar with system display names, or null on failure
     */
    public native LangTextVar apiCnsGetSystemNames(String system);

    /**
     * Set system display names.
     * @param system System name or null for default system
     * @param displayNames New display names
     * @return 0 on success, -1 on failure
     */
    public native int apiCnsSetSystemNames(String system, LangTextVar displayNames);

    // ========== CNS - Validation ==========

    /**
     * Check if a node ID is valid.
     * @param id Node ID to validate
     * @return true if valid
     */
    public native boolean apiCnsCheckId(String id);

    /**
     * Check if a display name is valid.
     * @param displayName Display name to validate
     * @return true if valid
     */
    public native boolean apiCnsCheckName(String displayName);

    /**
     * Check if a separator character is valid.
     * @param separator Separator character to validate
     * @return true if valid
     */
    public native boolean apiCnsCheckSeparator(char separator);

    // ========== CNS - getIdSet ==========

    /**
     * Get datapoint identifiers matching a pattern.
     * This is more efficient than getNodesByName + getId when you only need the DpIdentifiers.
     * @param system System name or null for default system
     * @param viewId The view identifier
     * @param pattern Name pattern to search
     * @param searchMode Search mode (0=NAME, 1=DISPLAY_NAME, 2=ALL_NAMES)
     * @param langIdx Language index for display name search
     * @return Array of DpIdentifierVar objects, or null on failure
     */
    public native DpIdentifierVar[] apiCnsGetIdSet(String system, String viewId, String pattern,
                                                    int searchMode, int langIdx);

    // ========== CNS - Observer ==========

    /**
     * CNS change types for observer notifications.
     */
    public static final int CNS_STRUCTURE_CHANGED = 0;
    public static final int CNS_NAMES_CHANGED = 1;
    public static final int CNS_DATA_CHANGED = 2;
    public static final int CNS_VIEW_SEPARATOR_CHANGED = 3;
    public static final int CNS_SYSTEM_NAMES_CHANGED = 4;

    /**
     * Add an observer to receive CNS change notifications.
     * The observer object must implement a method: void onCnsChange(String path, int changeType)
     * @param observer Object with onCnsChange(String, int) method
     * @return Observer ID (for removal), or -1 on failure
     */
    public native int apiCnsAddObserver(Object observer);

    /**
     * Remove a CNS observer.
     * @param observerId The observer ID returned by apiCnsAddObserver
     * @return 0 on success, -1 on failure
     */
    public native int apiCnsRemoveObserver(int observerId);

    // ========== Datapoint Type Management ==========

    /**
     * Create a new datapoint type.
     * @param definition The type definition tree (root DpTypeElement)
     * @param system System name or null for default system
     * @return 0 on success, -1 on failure
     */
    public native int apiDpTypeCreate(DpTypeElement definition, String system);

    /**
     * Modify an existing datapoint type.
     * @param typeId The datapoint type ID to modify
     * @param definition The new type definition
     * @param append If true, append definition under root; if false, replace entire type
     * @param system System name or null for default system
     * @return 0 on success, -1 on failure
     */
    public native int apiDpTypeChange(int typeId, DpTypeElement definition, boolean append, String system);

    /**
     * Delete a datapoint type.
     * @param typeId The datapoint type ID to delete
     * @param system System name or null for default system
     * @return 0 on success, -1 on failure
     */
    public native int apiDpTypeDelete(int typeId, String system);

    /**
     * Get the type ID for a datapoint type name.
     * @param typeName The datapoint type name
     * @param system System name or null for default system
     * @return The type ID, or -1 if not found
     */
    public native int apiDpTypeNameToId(String typeName, String system);

    // ========== Datapoint Management ==========

    /**
     * Create a new datapoint.
     * @param dpName The name of the datapoint to create
     * @param dpTypeName The datapoint type name
     * @param system System name or null for default system
     * @return 0 on success, -1 on failure
     */
    public native int apiDpCreate(String dpName, String dpTypeName, String system);

    /**
     * Delete a datapoint.
     * @param dpName The name of the datapoint to delete
     * @return 0 on success, -1 on failure
     */
    public native int apiDpDelete(String dpName);

    /**
     * Check if a datapoint exists.
     * @param dpName The datapoint name to check
     * @return true if the datapoint exists
     */
    public native boolean apiDpExists(String dpName);
}

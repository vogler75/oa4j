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
package at.rocworks.oa4j.var;

/**
 * Represents a CNS (Common Name Service) node in WinCC OA.
 * A CNS node is part of a hierarchical naming structure that provides
 * alternative names for datapoints.
 *
 * @author vogler
 */
public class CnsNode {

    private String path;
    private String name;
    private String system;
    private String view;
    private LangTextVar displayNames;
    private LangTextVar displayPaths;
    private DpIdentifierVar dpId;
    private int nodeType;
    private byte[] userData;

    /**
     * Default constructor.
     */
    public CnsNode() {
    }

    /**
     * Get the full CNS path of this node.
     * @return The CNS path (e.g., "System1:view1/tree/node")
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the full CNS path of this node.
     * Called from JNI.
     * @param path The CNS path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get the node identifier name.
     * @return The node name (ID)
     */
    public String getName() {
        return name;
    }

    /**
     * Set the node identifier name.
     * Called from JNI.
     * @param name The node name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the system name this node belongs to.
     * @return The system name (e.g., "System1")
     */
    public String getSystem() {
        return system;
    }

    /**
     * Set the system name.
     * Called from JNI.
     * @param system The system name
     */
    public void setSystem(String system) {
        this.system = system;
    }

    /**
     * Get the view name this node belongs to.
     * @return The view name
     */
    public String getView() {
        return view;
    }

    /**
     * Set the view name.
     * Called from JNI.
     * @param view The view name
     */
    public void setView(String view) {
        this.view = view;
    }

    /**
     * Get the display names for this node (multi-language).
     * @return LangTextVar with display names, or null if not set
     */
    public LangTextVar getDisplayNames() {
        return displayNames;
    }

    /**
     * Set the display names for this node.
     * Called from JNI.
     * @param displayNames Multi-language display names
     */
    public void setDisplayNames(LangTextVar displayNames) {
        this.displayNames = displayNames;
    }

    /**
     * Get the display paths for this node (multi-language).
     * The display path is the resolved path using display names.
     * @return LangTextVar with display paths, or null if not set
     */
    public LangTextVar getDisplayPaths() {
        return displayPaths;
    }

    /**
     * Set the display paths for this node.
     * Called from JNI.
     * @param displayPaths Multi-language display paths
     */
    public void setDisplayPaths(LangTextVar displayPaths) {
        this.displayPaths = displayPaths;
    }

    /**
     * Get the datapoint identifier linked to this node.
     * @return DpIdentifierVar, or null if this is a structure node without linked DP
     */
    public DpIdentifierVar getDpId() {
        return dpId;
    }

    /**
     * Set the datapoint identifier.
     * Called from JNI.
     * @param dpId The datapoint identifier
     */
    public void setDpId(DpIdentifierVar dpId) {
        this.dpId = dpId;
    }

    /**
     * Get the node type.
     * @return Node type value (see CNSDataIdentifier.Types in C++)
     * @see CnsDataIdentifier.Types
     */
    public int getNodeType() {
        return nodeType;
    }

    /**
     * Set the node type.
     * Called from JNI.
     * @param nodeType The node type value
     */
    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * Get the user data attached to this node.
     * @return User data bytes, or null if not set
     */
    public byte[] getUserData() {
        return userData;
    }

    /**
     * Set the user data.
     * Called from JNI.
     * @param userData User data bytes
     */
    public void setUserData(byte[] userData) {
        this.userData = userData;
    }

    @Override
    public String toString() {
        return "CnsNode{" +
                "path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", system='" + system + '\'' +
                ", view='" + view + '\'' +
                ", nodeType=" + nodeType +
                ", dpId=" + (dpId != null ? dpId.formatValue() : "null") +
                '}';
    }
}

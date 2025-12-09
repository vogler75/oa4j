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
 * Represents a CNS (Common Name Service) data identifier in WinCC OA.
 * A CNS data identifier links a CNS node to a datapoint with a specific type.
 *
 * @author vogler
 */
public class CnsDataIdentifier {

    /**
     * CNS node types matching CNSDataIdentifier::Types in C++.
     */
    public static final class Types {
        /** Node without data link (structure node) */
        public static final int NO_TYPE = 0;
        /** Node linked to a datapoint */
        public static final int DATAPOINT = 1;
        /** Node linked to a datapoint type */
        public static final int DP_TYPE = 2;
        /** Node linked to an OPC item */
        public static final int OPC_ITEM = 3;
        /** All types (for search) */
        public static final int ALL_TYPES = 4;

        private Types() {}
    }

    private DpIdentifierVar dpId;
    private int type;
    private byte[] userData;

    /**
     * Default constructor.
     */
    public CnsDataIdentifier() {
        this.type = Types.NO_TYPE;
    }

    /**
     * Constructor with datapoint and type.
     * @param dpId The datapoint identifier
     * @param type The node type
     */
    public CnsDataIdentifier(DpIdentifierVar dpId, int type) {
        this.dpId = dpId;
        this.type = type;
    }

    /**
     * Get the datapoint identifier.
     * @return DpIdentifierVar, or null if type is NO_TYPE
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
     * @return Type value from {@link Types}
     */
    public int getType() {
        return type;
    }

    /**
     * Set the node type.
     * Called from JNI.
     * @param type The type value
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Get the user data.
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

    /**
     * Check if this identifier is a datapoint type.
     * @return true if type is DATAPOINT
     */
    public boolean isDatapoint() {
        return type == Types.DATAPOINT;
    }

    /**
     * Check if this identifier is a structure node (no data link).
     * @return true if type is NO_TYPE
     */
    public boolean isStructureNode() {
        return type == Types.NO_TYPE;
    }

    @Override
    public String toString() {
        String typeName;
        switch (type) {
            case Types.NO_TYPE: typeName = "NO_TYPE"; break;
            case Types.DATAPOINT: typeName = "DATAPOINT"; break;
            case Types.DP_TYPE: typeName = "DP_TYPE"; break;
            case Types.OPC_ITEM: typeName = "OPC_ITEM"; break;
            default: typeName = "UNKNOWN(" + type + ")"; break;
        }
        return "CnsDataIdentifier{" +
                "type=" + typeName +
                ", dpId=" + (dpId != null ? dpId.formatValue() : "null") +
                '}';
    }
}

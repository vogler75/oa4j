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
 * Datapoint element types as defined in WinCC OA.
 * These types define the structure and data types of datapoint elements.
 */
public enum DpElementType {
    // No element / invalid
    NOELEMENT(0),

    // Structure container types
    RECORD(1),
    ARRAY(2),

    // Primitive types
    CHAR(3),
    UINT(4),
    INT(5),
    FLOAT(6),
    BIT(7),
    BIT32(8),
    TEXT(9),
    TIME(10),
    DPID(11),
    NOVALUE(12),
    STRUCTURE(13),
    LANGTEXT(14),
    BLOB(15),
    BIT64(16),
    LONG(17),

    // Type reference
    TYPEREFERENCE(18),

    // Dynamic array types
    DYNCHAR(19),
    DYNUINT(20),
    DYNINT(21),
    DYNFLOAT(22),
    DYNBIT(23),
    DYN32BIT(24),
    DYNTEXT(25),
    DYNTIME(26),
    DYNDPID(27),
    DYNLANGTEXT(28),
    DYNBLOB(29),
    DYN64BIT(30),
    DYNLONG(31),

    // Static array types
    CHARARRAY(32),
    UINTARRAY(33),
    INTARRAY(34),
    FLOATARRAY(35),
    BITARRAY(36),
    BIT32ARRAY(37),
    TEXTARRAY(38),
    TIMEARRAY(39),
    DPIDARRAY(40),
    LANGTEXTARRAY(41),
    BLOBARRAY(42),
    BIT64ARRAY(43),
    LONGARRAY(44),

    // Dynamic static array types
    DYNCHARARRAY(45),
    DYNUINTARRAY(46),
    DYNINTARRAY(47),
    DYNFLOATARRAY(48),
    DYNBITARRAY(49),
    DYN32BITARRAY(50),
    DYNTEXTARRAY(51),
    DYNTIMEARRAY(52),
    DYNDPIDARRAY(53),
    DYNLANGTEXTARRAY(54),
    DYNBLOBARRAY(55),
    DYN64BITARRAY(56),
    DYNLONGARRAY(57),

    // Unknown type (fallback)
    UNKNOWN(-1);

    private final int value;

    DpElementType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * Get DpElementType from its numeric value.
     * @param value The numeric element type value
     * @return The corresponding DpElementType, or UNKNOWN if not found
     */
    public static DpElementType fromValue(int value) {
        for (DpElementType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return UNKNOWN;
    }

    /**
     * Check if this element type is a leaf (primitive) type that holds actual values.
     * @return true if this is a primitive/leaf type
     */
    public boolean isLeafType() {
        switch (this) {
            case CHAR:
            case UINT:
            case INT:
            case FLOAT:
            case BIT:
            case BIT32:
            case BIT64:
            case TEXT:
            case TIME:
            case DPID:
            case LANGTEXT:
            case BLOB:
            case LONG:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if this element type is a dynamic array type.
     * @return true if this is a dynamic array type
     */
    public boolean isDynType() {
        return value >= DYNCHAR.value && value <= DYNLONG.value;
    }

    /**
     * Check if this element type is a static array type.
     * @return true if this is a static array type
     */
    public boolean isArrayType() {
        return this == ARRAY || (value >= CHARARRAY.value && value <= LONGARRAY.value);
    }

    /**
     * Check if this element type is a record/structure container.
     * @return true if this is a record type
     */
    public boolean isRecordType() {
        return this == RECORD || this == STRUCTURE;
    }

    /**
     * Check if this element type is a type reference.
     * @return true if this is a type reference
     */
    public boolean isReferenceType() {
        return this == TYPEREFERENCE;
    }
}

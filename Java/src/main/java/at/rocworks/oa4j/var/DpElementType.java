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
 * Values must match the C++ DpElementType enum in DpElementType.hxx.
 */
public enum DpElementType {
    // No element / invalid (0)
    NOELEMENT(0),

    // Structure container types
    RECORD(1),      // Container for children of different types
    ARRAY(2),       // Container for children of same type

    // Dynamic array types (3-10)
    DYNCHAR(3),
    DYNUINT(4),
    DYNINT(5),
    DYNFLOAT(6),
    DYNBIT(7),
    DYN32BIT(8),
    DYNTEXT(9),
    DYNTIME(10),

    // Static array types (11-18)
    CHARARRAY(11),
    UINTARRAY(12),
    INTARRAY(13),
    FLOATARRAY(14),
    BITARRAY(15),
    BIT32ARRAY(16),
    TEXTARRAY(17),
    TIMEARRAY(18),

    // Primitive types (19-28)
    CHAR(19),
    UINT(20),
    INT(21),
    FLOAT(22),
    BIT(23),
    BIT32(24),
    TEXT(25),
    TIME(26),
    DPID(27),
    NOVALUE(28),

    // More dynamic types (29)
    DYNDPID(29),

    // More dynamic array types (30-38)
    DYNCHARARRAY(30),
    DYNUINTARRAY(31),
    DYNINTARRAY(32),
    DYNFLOATARRAY(33),
    DYNBITARRAY(34),
    DYN32BITARRAY(35),
    DYNTEXTARRAY(36),
    DYNTIMEARRAY(37),
    DYNDPIDARRAY(38),

    // More static arrays (39-40)
    DPIDARRAY(39),
    NOVALUEARRAY(40),

    // Type reference (41)
    TYPEREFERENCE(41),

    // LangText types (42-45)
    LANGTEXT(42),
    LANGTEXTARRAY(43),
    DYNLANGTEXT(44),
    DYNLANGTEXTARRAY(45),

    // Blob types (46-49)
    BLOB(46),
    BLOBARRAY(47),
    DYNBLOB(48),
    DYNBLOBARRAY(49),

    // 64-bit types (50-53)
    BIT64(50),
    DYN64BIT(51),
    BIT64ARRAY(52),
    DYN64BITARRAY(53),

    // Long types (54-57)
    LONG(54),
    DYNLONG(55),
    LONGARRAY(56),
    DYNLONGARRAY(57),

    // Unsigned long types (58-61)
    ULONG(58),
    DYNULONG(59),
    ULONGARRAY(60),
    DYNULONGARRAY(61),

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
        // Leaf types are the primitive scalar types (19-28) and some special ones
        return (value >= CHAR.value && value <= NOVALUE.value) ||
               this == LANGTEXT || this == BLOB || this == BIT64 || this == LONG || this == ULONG;
    }

    /**
     * Check if this element type is a dynamic array type.
     * @return true if this is a dynamic array type
     */
    public boolean isDynType() {
        return (value >= DYNCHAR.value && value <= DYNTIME.value) ||
               this == DYNDPID ||
               (value >= DYNCHARARRAY.value && value <= DYNDPIDARRAY.value) ||
               this == DYNLANGTEXT || this == DYNLANGTEXTARRAY ||
               this == DYNBLOB || this == DYNBLOBARRAY ||
               this == DYN64BIT || this == DYN64BITARRAY ||
               this == DYNLONG || this == DYNLONGARRAY ||
               this == DYNULONG || this == DYNULONGARRAY;
    }

    /**
     * Check if this element type is a static array type.
     * @return true if this is a static array type
     */
    public boolean isArrayType() {
        return this == ARRAY ||
               (value >= CHARARRAY.value && value <= TIMEARRAY.value) ||
               this == DPIDARRAY || this == NOVALUEARRAY ||
               this == LANGTEXTARRAY || this == BLOBARRAY ||
               this == BIT64ARRAY || this == LONGARRAY || this == ULONGARRAY;
    }

    /**
     * Check if this element type is a record/structure container.
     * @return true if this is a record type
     */
    public boolean isRecordType() {
        return this == RECORD;
    }

    /**
     * Check if this element type is a type reference.
     * @return true if this is a type reference
     */
    public boolean isReferenceType() {
        return this == TYPEREFERENCE;
    }
}

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an element in a datapoint type definition tree structure.
 * Each element can have child elements, forming a hierarchical structure
 * that describes the complete datapoint type.
 */
public class DpTypeElement implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final int elementId;
    private final DpElementType elementType;
    private final int referencedTypeId;
    private final List<DpTypeElement> children;

    /**
     * Creates a new datapoint type element.
     *
     * @param name The name of the element
     * @param elementId The element's ID within the type
     * @param elementType The type of the element
     * @param referencedTypeId The referenced type ID (if this is a type reference), 0 otherwise
     */
    public DpTypeElement(String name, int elementId, DpElementType elementType, int referencedTypeId) {
        this.name = name;
        this.elementId = elementId;
        this.elementType = elementType;
        this.referencedTypeId = referencedTypeId;
        this.children = new ArrayList<>();
    }

    /**
     * Creates a new datapoint type element without a type reference.
     *
     * @param name The name of the element
     * @param elementId The element's ID within the type
     * @param elementType The type of the element
     */
    public DpTypeElement(String name, int elementId, DpElementType elementType) {
        this(name, elementId, elementType, 0);
    }

    /**
     * Returns the name of this element.
     * @return The element name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the element ID.
     * @return The element ID
     */
    public int getElementId() {
        return elementId;
    }

    /**
     * Returns the element type.
     * @return The DpElementType of this element
     */
    public DpElementType getElementType() {
        return elementType;
    }

    /**
     * Returns the referenced type ID if this is a type reference.
     * @return The referenced type ID, or 0 if not a reference
     */
    public int getReferencedTypeId() {
        return referencedTypeId;
    }

    /**
     * Returns the list of child elements.
     * @return An unmodifiable list of child elements
     */
    public List<DpTypeElement> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Adds a child element to this element.
     * @param child The child element to add
     */
    public void addChild(DpTypeElement child) {
        children.add(child);
    }

    /**
     * Returns the number of child elements.
     * @return The number of children
     */
    public int getChildCount() {
        return children.size();
    }

    /**
     * Check if this element has children.
     * @return true if this element has child elements
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Check if this element is a leaf (has no children and is a primitive type).
     * @return true if this is a leaf element
     */
    public boolean isLeaf() {
        return children.isEmpty() && elementType.isLeafType();
    }

    /**
     * Check if this element is a type reference.
     * @return true if this is a type reference
     */
    public boolean isTypeReference() {
        return elementType.isReferenceType();
    }

    /**
     * Recursively finds a child element by name path.
     * @param path The path to the element (e.g., "parent.child.grandchild")
     * @return The found element, or null if not found
     */
    public DpTypeElement findElement(String path) {
        if (path == null || path.isEmpty()) {
            return this;
        }

        String[] parts = path.split("\\.", 2);
        String childName = parts[0];

        for (DpTypeElement child : children) {
            if (child.getName().equals(childName)) {
                if (parts.length == 1) {
                    return child;
                } else {
                    return child.findElement(parts[1]);
                }
            }
        }
        return null;
    }

    /**
     * Returns a flat list of all element names with their full paths.
     * @return List of element path strings
     */
    public List<String> getElementPaths() {
        List<String> paths = new ArrayList<>();
        collectPaths(paths, "");
        return paths;
    }

    private void collectPaths(List<String> paths, String prefix) {
        String currentPath = prefix.isEmpty() ? name : prefix + "." + name;
        paths.add(currentPath);
        for (DpTypeElement child : children) {
            child.collectPaths(paths, currentPath);
        }
    }

    @Override
    public String toString() {
        return toString(0);
    }

    /**
     * Returns a formatted string representation with indentation.
     * @param indent The indentation level
     * @return Formatted string representation
     */
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String indentStr = "  ".repeat(indent);

        sb.append(indentStr)
          .append(name)
          .append(" (id=").append(elementId)
          .append(", type=").append(elementType);

        if (referencedTypeId != 0) {
            sb.append(", refTypeId=").append(referencedTypeId);
        }
        sb.append(")");

        if (!children.isEmpty()) {
            sb.append(" {\n");
            for (DpTypeElement child : children) {
                sb.append(child.toString(indent + 1)).append("\n");
            }
            sb.append(indentStr).append("}");
        }

        return sb.toString();
    }
}

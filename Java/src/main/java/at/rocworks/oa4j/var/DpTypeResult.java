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
import java.util.List;

/**
 * Result of dpTypeGet operation, containing element names and types
 * organized by hierarchy level, matching the WinCC OA Control script function:
 * int dpTypeGet(string name, dyn_dyn_string &elements, dyn_dyn_int &types, bool includeSubTypes)
 *
 * The elements and types are organized as 2D arrays where:
 * - First dimension: hierarchy level (0 = root type, 1 = first level children, etc.)
 * - Second dimension: elements at that level
 */
public class DpTypeResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<List<String>> elements;
    private final List<List<Integer>> types;

    /**
     * Creates a new DpTypeResult with empty lists.
     */
    public DpTypeResult() {
        this.elements = new ArrayList<>();
        this.types = new ArrayList<>();
    }

    /**
     * Creates a new DpTypeResult with the given elements and types.
     * @param elements 2D list of element names by level
     * @param types 2D list of element types by level
     */
    public DpTypeResult(List<List<String>> elements, List<List<Integer>> types) {
        this.elements = elements != null ? elements : new ArrayList<>();
        this.types = types != null ? types : new ArrayList<>();
    }

    /**
     * Returns the element names organized by hierarchy level.
     * @return 2D list of element names
     */
    public List<List<String>> getElements() {
        return elements;
    }

    /**
     * Returns the element types organized by hierarchy level.
     * @return 2D list of element type values (DpElementType enum values)
     */
    public List<List<Integer>> getTypes() {
        return types;
    }

    /**
     * Returns the number of hierarchy levels.
     * @return Number of levels
     */
    public int getLevelCount() {
        return elements.size();
    }

    /**
     * Returns the element names at a specific hierarchy level.
     * @param level The hierarchy level (0-based)
     * @return List of element names at that level, or empty list if level is invalid
     */
    public List<String> getElementsAtLevel(int level) {
        if (level >= 0 && level < elements.size()) {
            return elements.get(level);
        }
        return new ArrayList<>();
    }

    /**
     * Returns the element types at a specific hierarchy level.
     * @param level The hierarchy level (0-based)
     * @return List of element types at that level, or empty list if level is invalid
     */
    public List<Integer> getTypesAtLevel(int level) {
        if (level >= 0 && level < types.size()) {
            return types.get(level);
        }
        return new ArrayList<>();
    }

    /**
     * Adds a new level with the given elements and types.
     * @param levelElements Element names for this level
     * @param levelTypes Element types for this level
     */
    public void addLevel(List<String> levelElements, List<Integer> levelTypes) {
        elements.add(levelElements != null ? levelElements : new ArrayList<>());
        types.add(levelTypes != null ? levelTypes : new ArrayList<>());
    }

    /**
     * Returns a flat list of all element names with their full dot-separated paths.
     * @return List of all element paths
     */
    public List<String> getAllElementPaths() {
        List<String> paths = new ArrayList<>();
        for (List<String> level : elements) {
            paths.addAll(level);
        }
        return paths;
    }

    /**
     * Returns the DpElementType for a given element path.
     * @param elementPath The full element path (e.g., "TypeName.element.subelement")
     * @return The DpElementType, or UNKNOWN if not found
     */
    public DpElementType getTypeForElement(String elementPath) {
        for (int level = 0; level < elements.size(); level++) {
            List<String> levelElements = elements.get(level);
            for (int i = 0; i < levelElements.size(); i++) {
                if (levelElements.get(i).equals(elementPath)) {
                    if (level < types.size() && i < types.get(level).size()) {
                        return DpElementType.fromValue(types.get(level).get(i));
                    }
                }
            }
        }
        return DpElementType.UNKNOWN;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DpTypeResult {\n");
        for (int level = 0; level < elements.size(); level++) {
            sb.append("  Level ").append(level).append(":\n");
            List<String> levelElements = elements.get(level);
            List<Integer> levelTypes = types.get(level);
            for (int i = 0; i < levelElements.size(); i++) {
                sb.append("    ").append(levelElements.get(i));
                if (i < levelTypes.size()) {
                    DpElementType type = DpElementType.fromValue(levelTypes.get(i));
                    sb.append(" (").append(type).append(")");
                }
                sb.append("\n");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}

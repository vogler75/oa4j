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
 * Interface for receiving CNS (Common Name Service) change notifications.
 * Implement this interface and register with Manager.apiCnsAddObserver() to
 * receive callbacks when CNS structure changes.
 *
 * @author vogler
 */
public interface CnsObserver {

    /**
     * CNS change types matching CNSChanges enum in C++.
     * These values match the constants in Manager class.
     */
    public static final class ChangeType {
        /** CNS structure was changed (nodes added/removed) */
        public static final int STRUCTURE_CHANGED = 0;
        /** Node names were changed */
        public static final int NAMES_CHANGED = 1;
        /** Node data (datapoint link) was changed */
        public static final int DATA_CHANGED = 2;
        /** View separator was changed */
        public static final int VIEW_SEPARATOR_CHANGED = 3;
        /** System display names were changed */
        public static final int SYSTEM_NAMES_CHANGED = 4;

        private ChangeType() {}
    }

    /**
     * Called when a CNS change occurs.
     * This method is called from the WinCC OA event thread.
     *
     * @param path The CNS path that was affected
     * @param changeType The type of change (see {@link ChangeType})
     */
    void onCnsChange(String path, int changeType);
}

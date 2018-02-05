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

/**
 *
 * @author vogler
 */
public enum SysMsgType {
    NO_SYS_MSG(1),
    KEEP_ALIVE(2),
    INIT_SYS_MSG(3),
    START_DP_INIT(4),
    END_DP_INIT(5),
    START_MANAGER(6),
    SHUT_DOWN_MANAGER(7),
    NAMESERVER_SYS_MSG(8),
    OPEN_SERVER(9),
    CLOSE_SERVER(10),
    LICENSE_SYS_MSG(11),
    RECOVERY_SYS_MSG(12),
    REDUNDANCY_SYS_MSG(13),
    DIST_SYS_MSG(14),
    FILETRANSFER_SYS_MSG(15),
    MAX_SYS_MSG(16);

    SysMsgType(int value) {
        this.value = value;
    }
    public int value;       
}

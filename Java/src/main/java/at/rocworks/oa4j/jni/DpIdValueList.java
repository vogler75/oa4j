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

import at.rocworks.oa4j.var.DpIdentifierVar;
import at.rocworks.oa4j.var.Variable;

/**
 *
 * @author vogler
 */
public class DpIdValueList extends Malloc {
    public DpIdValueList() {
        super();
    }
    
    public DpIdValueList(long cptr) {
        super.setPointer(cptr);
    }         
    
    public native String toDebug(int level);        
    
    public native DpVCItem getFirstItem();
    public native DpVCItem getNextItem();
    public native DpVCItem cutFirstItem();
    public native int getNumberOfItems();
    public native boolean appendItem(DpIdentifierVar dpid, Variable var);
    
    @Override
    protected native long malloc();

    @Override
    protected native void free(long cptr);    
    
}

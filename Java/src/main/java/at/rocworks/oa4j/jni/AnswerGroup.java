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

import at.rocworks.oa4j.var.TimeVar;
import at.rocworks.oa4j.var.DpIdentifierVar;
import at.rocworks.oa4j.var.Variable;

/**
 *
 * @author vogler
 */
public class AnswerGroup extends Malloc {    
    public AnswerGroup() {
        super();
    }
    
    public AnswerGroup(long cptr) {
        super.setPointer(cptr);
    }    
    
    public native boolean insertItem(DpIdentifierVar id, Variable value, TimeVar time);
    
    @Override
    protected native long malloc();

    @Override
    protected native void free(long cptr);    
}

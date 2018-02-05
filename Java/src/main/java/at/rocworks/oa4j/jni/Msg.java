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
public class Msg extends Malloc {         
    public Msg() {
        super();
    }
    
    public Msg(long cptr) {
        super.setPointer(cptr);
    }        
    
    public MsgType isA() {
        return MsgType.values()[isA(cptr)];
    }            
        
    protected native int isA(long cptr);

    public native long getMsgId();

    public void forwardMsg(ManagerType manType, int manNum) {
        forwardMsg(manType.value, manNum);
    }

    public native void forwardMsg(int manType, int manNum);

    public native void forwardMsgToData();

    public native int getSourceManTypeNr();

    public ManagerType getSourceManType() {
        return ManagerType.values()[getSourceManTypeNr()];
    }

    public native int getSourceManNum();
    
    @Override
    public native String toString();
    
    public native String toDebug(int level);

    @Override
    protected long malloc() {
        throw new UnsupportedOperationException("cannot instantiate abstract class"); 
    }

    @Override
    protected native void free(long cptr);
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.base;

import at.rocworks.oa4j.var.DpIdentifierVar;
import at.rocworks.oa4j.var.NullVar;
import at.rocworks.oa4j.var.Variable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author vogler
 */
public class JDpIdValueList implements Iterable<JDpVCItem> {
    private final ArrayList<JDpVCItem> list = new ArrayList<>();
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(this.getClass().getSimpleName()).append(":").append(list.size()).append("\n");
        list.forEach((JDpVCItem item)->s.append(item.toString()).append("\n"));
        return s.toString();
    }
    
    public void addItem(JDpVCItem item) {
        this.list.add(item);
    }
    
    public JDpVCItem getItem(int idx) {
        return list.get(idx);
    }

    public Variable getItemVar(int idx) {
        JDpVCItem item = list.size()>idx ? list.get(idx) : null;
        return item==null ? NullVar.NULL : item.getVariable();
    }
    
    public ArrayList<JDpVCItem> getItems() {
        return list;
    }
    
    public int getNumberOfItems() { // wincc oa api like
        return list.size();
    } // WinCC OA API Style

    public int size() { return list.size(); } // Java Style
    
    public List<JDpVCItem> asList() {
        return list;
    }
        
    public void clear() {
        list.clear();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<JDpVCItem> iterator() {
        return asList().iterator();
    }
}

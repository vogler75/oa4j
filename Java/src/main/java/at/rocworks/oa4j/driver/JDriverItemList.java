/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.driver;

import java.util.LinkedList;

/**
 *
 * @author vogler
 */
public class JDriverItemList {

    private final LinkedList<JDriverItem> collection;

    public JDriverItemList() {
        collection=new LinkedList<>();
    }

    public JDriverItemList(JDriverItem item) {
        collection=new LinkedList<>();
        collection.add(item);
    }

    public void addItem(JDriverItem item) {
        collection.add(item);
    }

    public JDriverItem pollFirst() {
        return collection.pollFirst(); 
    }
    
    public JDriverItem pollLast() {
        return collection.pollLast(); 
    }    

    public int getSize() {
        return collection.size();
    }

    public boolean isEmpty() {
        return collection.isEmpty();
    }
}    

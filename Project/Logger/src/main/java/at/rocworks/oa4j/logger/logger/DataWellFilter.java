/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.logger;

import at.rocworks.oa4j.var.DpIdentifierVar;
import at.rocworks.oa4j.var.VariableType;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author vogler
 */
public class DataWellFilter implements IDatapointFilter {
    private final ConcurrentHashMap<DpIdentifierVar, Boolean> archives = new ConcurrentHashMap<>();
    
    private final boolean enabled;
    private final boolean internaldps;
    
    public DataWellFilter(boolean enabled, boolean internaldps) {
        this.enabled=enabled;
        this.internaldps=internaldps;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public void set(ArrayList<DpIdentifierVar> dyn) {
        archives.clear();
        add(dyn);
    }
    
    public void add(DpIdentifierVar dpid) {
        archives.put(dpid.getDpIdentifierVar(), Boolean.TRUE);
    }
    
    public void remove(DpIdentifierVar dpid) {
        archives.remove(dpid);
    }
    
    public void add(ArrayList<DpIdentifierVar> dyn) {
        dyn.stream()
                .filter(dpid -> dpid.isA() == VariableType.DpIdentifierVar)
                .forEach(dpid -> archives.put(dpid.getDpIdentifierVar(), Boolean.TRUE));
    }
    
    public void remove(ArrayList<DpIdentifierVar> dyn) {
        dyn.stream()
                .filter(dpid -> dpid.isA() == VariableType.DpIdentifierVar)
                .forEach(dpid -> archives.remove((DpIdentifierVar)dpid));                
    }
    
    @Override
    public boolean isArchived(DpIdentifierVar dpid) {
        return enabled ? archives.containsKey(dpid) : !internaldps ? !dpid.isInternal() : true;
    }
}

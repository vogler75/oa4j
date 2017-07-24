/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.var;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.simple.JSONArray;

/**
 *
 * @author vogler
 */
public class DynVar extends Variable implements Serializable, Iterable<Variable> {
    private final ArrayList<Variable> value = new ArrayList<>();

    public DynVar(Variable... vars) {
        for(Variable var: vars)
            value.add(var);
    }

    public DynVar(Iterator<Variable> iterator) {
        iterator.forEachRemaining((var)->value.add(var));
    }
       
    public void add(Variable value) {
        this.value.add(value);
    }
    
    public Variable remove(int index) {
        return this.value.remove(index);
    }
    
    public Variable get(int index) {
        return this.value.get(index);
    }   
    
    public int size() {
        return this.value.size();
    }
    
    @Override
    public String formatValue() {
        JSONArray arr = new JSONArray();
        arr.addAll(value);
        return arr.toString();
    }

    @Override
    public VariableType isA() {
        return VariableType.DynVar;
    }        
    
    @Override
    public Object getValueObject() {
        return value; 
    }        
    
    public List<Variable> asList() {
        return value;
    }

    public Variable[][] asTable() {
        Variable[][] table = new Variable[value.size()][];
        for ( int rownr = 0; rownr<value.size(); rownr++ ) {
            if ( value.get(rownr).isA() == VariableType.DynVar ) {
                DynVar row=(DynVar)value.get(rownr);
                table[rownr]=new Variable[row.size()];
                for ( int colnr=0; colnr<row.size(); colnr++ ) {
                    table[rownr][colnr]=row.get(colnr);
                }
            } else {
                table[rownr]=new Variable[1];
                table[rownr][0]=value.get(rownr);
            }            
        }
        return table;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Variable> iterator() {
        return asList().iterator();
    }

    public boolean contains(Variable var) { return value.contains(var); }
    public int indexOf(Variable var) { return value.indexOf(var); }
}

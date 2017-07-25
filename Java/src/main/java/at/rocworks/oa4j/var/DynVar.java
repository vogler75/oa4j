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

/**
 *
 * @author vogler
 */
public class DynVar extends Variable implements Serializable, Iterable<Variable> {
    private final ArrayList<Variable> value = new ArrayList<>();
    private VariableType type = VariableType.Unknown;

    public int getElementsTypeAsNr() {
        return type.value;
    }

    public DynVar(Variable... vars) {
        for(Variable var: vars) {
            add(var);
        }
    }

    public DynVar(Object... vars) {
        for(Object var: vars) {
            add(Variable.newVariable(var));
        }
    }

    public DynVar(Iterator<Variable> iterator) {
        iterator.forEachRemaining((var)->add(var));
    }

    public void add(Object value) {
        add(Variable.newVariable(value));
    }

    public void add(Variable value) {
        if (value!=null) {
            if (type == VariableType.Unknown)
                type = value.isA();
            else if (type != value.isA())
                type = VariableType.AnyTypeVar;
        }
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
        ArrayList<String> arr = new ArrayList<>(value.size());
        value.forEach(var->arr.add(var.toString()));
        return "["+String.join(",", arr)+"]";
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

    public boolean contains(Variable var) {
        return value.contains(var);
    }

    public int indexOf(Variable var) {
        return value.indexOf(var);
    }
}

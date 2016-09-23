/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.data;



/**
 *
 * @author gavalian
 */
public enum HipoNodeType {
    
    UNDEFINED ( 0, 0, "UNDEFINED"),
    BYTE      ( 1, 1, "BYTE"),
    SHORT     ( 2, 2, "SHORT"),
    INT       ( 3, 4, "INT"),    
    FLOAT     ( 4, 4, "FLOAT"),
    DOUBLE    ( 5, 8, "DOUBLE"),
    STRING    ( 6, 1, "STRING");
    
    private final int typeid;
    private final int sizeOf;
    private final String typename;
    
    HipoNodeType(){
        typeid = 0;
        sizeOf = 0;
        typename = "UNDEFINED";
    }
    
    HipoNodeType(int id, int s, String name){
        typeid = id;
        sizeOf = s;
        typename = name;
    }

    public String getName() {
        return typename;
    }
    
     /**
     * Returns the id number of the detector.
     * @return the id number of the detector
     */
    public int getType() {
        return typeid;
    }
    
    public int getSize(){
        return sizeOf;
    }
    
    public static HipoNodeType getType(String name) {
        name = name.trim();
        for(HipoNodeType id: HipoNodeType.values())
            if (id.getName().equalsIgnoreCase(name)) 
                return id;
        return UNDEFINED;
    }
    
    public static HipoNodeType getType(int tid) {        
        for(HipoNodeType id: HipoNodeType.values())
            if (id.typeid==tid) 
                return id;
        return UNDEFINED;
    }
}

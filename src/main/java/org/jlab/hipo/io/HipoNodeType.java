/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.io;



/**
 *
 * @author gavalian
 */
public enum HipoNodeType {
    
    UNDEFINED ( 0, "UNDEFINED"),
    BYTE      ( 1, "BYTE"),
    SHORT     ( 2, "SHORT"),
    INT       ( 3, "INT"),    
    FLOAT     ( 4, "FLOAT"),
    DOUBLE    ( 5, "DOUBLE");
    
    private final int typeid;
    private final String typename;
    
    HipoNodeType(){
        typeid = 0;
        typename = "UNDEFINED";
    }
    
    HipoNodeType(int id, String name){
        typeid = id;
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
    
    public static HipoNodeType getType(String name) {
        name = name.trim();
        for(HipoNodeType id: HipoNodeType.values())
            if (id.getName().equalsIgnoreCase(name)) 
                return id;
        return UNDEFINED;
    }
}

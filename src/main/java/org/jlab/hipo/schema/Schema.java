/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.schema;

import java.util.HashMap;
import java.util.Map;
import org.jlab.hipo.data.HipoNodeType;

/**
 *
 * @author gavalian
 */
public class Schema {
    
    private Integer group = 1;
    private String  name  = "default";
    
    private Map<Integer,SchemaEntry>    idEntries = new HashMap<Integer,SchemaEntry>();
    private Map<String,SchemaEntry>   nameEntries = new HashMap<String,SchemaEntry>();
    
    public Schema(){
        
    }
    
    public Schema(String n, int grp){
        this.setName(n);
        this.setGroup(grp);
    }
    
    public void addEntry(SchemaEntry entry){
        this.idEntries.put(entry.getId(), entry);
        this.nameEntries.put(entry.getName(), entry);
    }
    
    public void addEntry(String n, int id, HipoNodeType type){
        this.addEntry(new SchemaEntry(n,id,type));
    }
    
    
    public final void   setName(String n){name = n;}
    public final void   setGroup(int grp){group = grp;}
    public final String getName(){return name;}
    public final int    getGroup(){return group;}
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format(" Schema {%12s} , group = {%d}\n", name,group));
        for(Map.Entry<Integer,SchemaEntry> entry : this.idEntries.entrySet()){
            str.append(entry.getValue().toString());
            str.append("\n");
        }
        return str.toString();
    }
    /**
     * Schema entry class for keeping information on each entry
     */
    public static class SchemaEntry {
        
        private String  name = "x";
        private Integer id   = 1;
        private HipoNodeType type = HipoNodeType.UNDEFINED;               
        
        public SchemaEntry(){
            
        }
        
        
        public SchemaEntry(String n, int i, HipoNodeType t){
          name = n;
          id = i;
          type = t;
        }
        
        public void setName(String n) {name = n;}
        public void setId(Integer i) {id = i;}
        public void setType(HipoNodeType t) {type = t;}
        public int  getId(){return id;}
        public String getName(){return name;}
        public HipoNodeType getType(){return type;}
        
        @Override
        public String toString(){
            return String.format("%4d : %24s %12s", id,name,type.getName());
        }
    }
    
    public static void main(String[] args){
        
        Schema schema = new Schema("DC::dgtz",300);
        schema.addEntry("sector", 1, HipoNodeType.INT);
        schema.addEntry("layer",  2, HipoNodeType.BYTE);
        schema.addEntry("ADC",    3, HipoNodeType.INT);
        schema.addEntry("TDC",    4, HipoNodeType.INT);        
        System.out.println(schema);
    }
}

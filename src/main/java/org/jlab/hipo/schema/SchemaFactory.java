/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.schema;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author gavalian
 */
public class SchemaFactory {
    
    private Boolean  overrideMode = false;
    
    private Map<String,Schema> schemaStore        = new LinkedHashMap<String,Schema>();
    private Map<Integer,Schema> schemaStoreGroups = new LinkedHashMap<Integer,Schema>();
    
    
    public SchemaFactory(){
        
    }
    
    public void addSchema(Schema schema){
        if(this.schemaStore.containsKey(schema.getName())==true){
            System.out.println("[SchemaFactory] ---> warning : schema with name "+
                    schema.getName() + " already exists.");
            if(this.overrideMode==false){
                System.out.println("[SchemaFactory] ---> warning : new schema "+
                        " is not added");
                return;
            }
        }
        this.schemaStore.put(schema.getName(), schema);
        this.schemaStoreGroups.put(schema.getGroup(), schema);
    }
    
    
    
    public boolean hasSchema(String name){
        return this.schemaStore.containsKey(name);
    }
    
    public boolean hasSchema(int group){
        return this.schemaStoreGroups.containsKey(group);
    }
    
    public Schema getSchema(String name){
        return this.schemaStore.get(name);
    }
    
    
    public Schema getSchema(int group){
        return this.schemaStoreGroups.get(group);
    }
    
    public SchemaFactory copy(){
        SchemaFactory factory = new SchemaFactory();
        for(Map.Entry<String,Schema> entry : this.schemaStore.entrySet()){
            factory.addSchema(entry.getValue());
        }
        return factory;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.schema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jlab.hipo.data.HipoEvent;
import org.jlab.hipo.data.HipoGroup;
import org.jlab.hipo.data.HipoNode;

/**
 *
 * @author gavalian
 */
public class SchemaFactory {
    
    private Boolean  overrideMode = false;
    
    private Map<String,Schema>        schemaStore = new LinkedHashMap<String,Schema>();
    private Map<Integer,Schema> schemaStoreGroups = new LinkedHashMap<Integer,Schema>();
    private List<String>             schemaFilter = new ArrayList<String>();
    
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
    
    public void copy(SchemaFactory factory){
        this.schemaStore.clear();
        this.schemaStoreGroups.clear();
        for(Map.Entry<Integer,Schema> entry : factory.schemaStoreGroups.entrySet()){
            this.addSchema(entry.getValue());
        }
    }
    
    public void show(){
        for(Map.Entry<Integer,Schema> entry : this.schemaStoreGroups.entrySet()){
            System.out.println(entry.getValue().toString());
        }
    }
    
    public void setFromEvent(HipoEvent event){
        Map<Integer,HipoNode>  schemaGroup = event.getGroup(32111);
        this.schemaStore.clear();
        this.schemaStoreGroups.clear();
        System.out.println(" SCHEMA FACTORY EVENT SIZE = " + schemaGroup.size());
        for(Map.Entry<Integer,HipoNode> items : schemaGroup.entrySet()){
            Schema schema = new Schema(items.getValue().getString());
            this.addSchema(schema);
        }
    }
    
    public HipoEvent getSchemaEvent(){
        
        HipoEvent event = new HipoEvent();
        
        List<HipoNode> nodes = new ArrayList<HipoNode>();
        int counter = 1;
        for(Map.Entry<Integer,Schema> entry : this.schemaStoreGroups.entrySet()){
            HipoNode nodeSchema = new HipoNode(32111,counter,entry.getValue().getText());
            nodes.add(nodeSchema);
            counter++;
            if(counter>120) break;
        }
        System.out.println("SCHEMA NODES SIZE = " + nodes.size());
        event.addNodes(nodes);
        //event.updateNodeIndex();
        System.out.println(event.toGroupListString());
        return event;
    }
    
    public void addFilter(String name){
        if(this.schemaStore.containsKey(name)==false){
            System.out.println("[addFilter] error -> can not find schema with name " + name);
        } else {
            this.schemaFilter.add(name);
        }
    }
    
    public void addFilter(int id){
        if(this.schemaStoreGroups.containsKey(id)==false){
            System.out.println("[addFilter] error -> can not find schema with id = " + id);
        } else {
            this.schemaFilter.add(this.schemaStoreGroups.get(id).getName());
        }
    }
    
    public HipoEvent getFilteredEvent(HipoEvent event){
        HipoEvent filtered = new HipoEvent(this);
        for(String bank : this.schemaFilter){
            HipoGroup group = event.getGroup(bank);
            filtered.addNodes(group.getNodes());
        }
        return filtered;
    }
    
    public static void main(String[] args){
        SchemaFactory factory = new SchemaFactory();
        factory.addSchema(new Schema("{1302,FTOF::dgtz}[1,px,FLOAT][2,py,FLOAT][3,pz,FLOAT]"));
        factory.addSchema(new Schema("{1304,DC::dgtz}[1,px,FLOAT][2,py,FLOAT][3,pz,FLOAT]"));
        factory.addSchema(new Schema("{1306,ECAL::dgtz}[1,px,FLOAT][2,py,FLOAT][3,pz,FLOAT]"));
        //factory.show();
        
        HipoEvent event = factory.getSchemaEvent();
        
        System.out.println(event.toString());
        SchemaFactory ff = new SchemaFactory();
        
        ff.setFromEvent(event);
        ff.show();
    }
}

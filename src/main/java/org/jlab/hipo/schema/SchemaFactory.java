/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.schema;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.hipo.data.HipoEvent;
import org.jlab.hipo.data.HipoGroup;
import org.jlab.hipo.data.HipoNode;
import org.jlab.hipo.data.HipoNodeType;
import org.jlab.hipo.json.Json;
import org.jlab.hipo.json.JsonArray;
import org.jlab.hipo.json.JsonObject;
import org.jlab.hipo.json.JsonValue;
import org.jlab.hipo.utils.FileUtils;

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
    
    public List<Schema>  getSchemaList(){
        List<Schema> schemas = new ArrayList<Schema>();
        for(Map.Entry<String,Schema> entry : this.schemaStore.entrySet()){
            schemas.add(entry.getValue());
        }
        return schemas;
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
    /**
     * Copies Schemas from the original factory including only descriptors
     * passed by the list.
     * @param factory original SchemaFactory
     * @param descriptors list of descriptors to copy
     */
    public void copy(SchemaFactory factory, String... descriptors){
        this.schemaStore.clear();
        this.schemaStoreGroups.clear();
        for(String item : descriptors){
            if(this.schemaStore.containsKey(item)==true){
                this.addSchema(this.schemaStore.get(item));
            }
        }
    }
    
    public void copy(SchemaFactory factory, List<String> descriptors){
        this.schemaStore.clear();
        this.schemaStoreGroups.clear();
        for(String item : descriptors){
            if(this.schemaStore.containsKey(item)==true){
                this.addSchema(this.schemaStore.get(item));
            }
        }
        /*for(Map.Entry<Integer,Schema> entry : factory.schemaStoreGroups.entrySet()){
            
            this.addSchema(entry.getValue());
        }*/
    }
    
    public void show(){
        for(Map.Entry<Integer,Schema> entry : this.schemaStoreGroups.entrySet()){
            System.out.println(entry.getValue().toString());
        }
    }
    /**
     * Reads the event and initializes the factory with Schema's
     * @param event HipoEvent
     */
    public void setFromEvent(HipoEvent event){
        Map<Integer,HipoNode>  schemaGroup = event.getGroup(32111);
        this.schemaStore.clear();
        this.schemaStoreGroups.clear();
        //System.out.println(" SCHEMA FACTORY EVENT SIZE = " + schemaGroup.size());
        for(Map.Entry<Integer,HipoNode> items : schemaGroup.entrySet()){
            Schema schema = new Schema(items.getValue().getString());
            this.addSchema(schema);
        }
    }
    /**
     * returns a HipoEvent containing a bank with descriptors.
     * @return HipoEvent
     */
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
        //System.out.println("SCHEMA NODES SIZE = " + nodes.size());
        event.addNodes(nodes);
        //event.updateNodeIndex();
        //System.out.println(event.toGroupListString());
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
            if(event.hasGroup(bank)==true){
                HipoGroup group = event.getGroup(bank);
                filtered.addNodes(group.getNodes());
            }
        }
        return filtered;
    }
    
    public HipoNodeType  getNodeType(String desc){
        if(desc.compareTo("int32")==0) return HipoNodeType.INT;
        if(desc.compareTo("int8")==0) return HipoNodeType.BYTE;
        if(desc.compareTo("int16")==0) return HipoNodeType.SHORT;
        if(desc.compareTo("float")==0) return HipoNodeType.FLOAT;
        if(desc.compareTo("double")==0) return HipoNodeType.DOUBLE;
        if(desc.compareTo("int64")==0) return HipoNodeType.LONG;
        if(desc.compareTo("vector3f")==0) return HipoNodeType.VECTOR3F;
        return HipoNodeType.UNDEFINED;
    }
    
    public void initFromDirectory(String directory){
        this.schemaStore.clear();
        this.schemaStoreGroups.clear();
        List<Schema> dirSchemas = this.readSchemaDirectory(directory);
        for(Schema schema : dirSchemas){
            this.addSchema(schema);
        }
    }
    
    public void initFromDirectory(String env,String directory){
        this.schemaStore.clear();
        this.schemaStoreGroups.clear();
        String fullPath = FileUtils.getEnvironmentPath(env, directory);
        if(fullPath!=null){
            List<Schema> dirSchemas = this.readSchemaDirectory(fullPath);
            for(Schema schema : dirSchemas){
                this.addSchema(schema);
            }
        }
    }
    
    public List<Schema> readSchemaDirectory(String directory){
        List<String> fileList = FileUtils.getFileListInDir(directory, ".json");
        List<Schema> dirScemas = new ArrayList<Schema>();
        for(String file : fileList){
            System.out.println("[readSchemaDirectory] processing file -> " + file);
            List<Schema> fileSchemas = this.readSchemaFile(file);
            dirScemas.addAll(fileSchemas);
        }
        return dirScemas;
    }
    
    public List<Schema>  readSchemaFile(String filename){
        
        List<Schema>  schemas = new ArrayList<Schema>();
        try {
            
            Reader reader = new FileReader(filename);
            JsonArray  object = Json.parse(reader).asArray();
            
            for(JsonValue value : object.values()){
                JsonObject bankDesc = value.asObject();
                String bankName = bankDesc.get("bank").asString();
                Integer groupId = bankDesc.get("group").asInt();
                
                //System.out.println("----> processing bank : " + bankName + "  group = " + groupId);
                JsonArray  items = bankDesc.get("items").asArray();
                Schema desc = new Schema(bankName,groupId);
                for(JsonValue item : items.values()){
                    JsonObject entry = item.asObject();
                    String  itemName = entry.get("name").asString();
                    Integer itemId   = entry.get("id").asInt();
                    String  itemType = entry.get("type").asString();
                    HipoNodeType type = this.getNodeType(itemType);
                    if(type==HipoNodeType.UNDEFINED){
                        System.out.println(" error parsing type = " + itemType);
                    } else {
                        //System.out.println("\t----> processing entry " + itemName + " id = " + itemId + " type = " + type );
                        desc.addEntry(itemName, itemId, type);
                    }
                }
                //this.addSchema(desc);
                schemas.add(desc);
            }
            
            
            //System.out.println(object);
            //String name = object.get("bank").asString();
            /*System.out.println("=======> NAME = " + name);
            JsonArray  array = object.get("items").asArray();
            int counter = 0;
            for( JsonValue values : array.values()){
                JsonObject entry = values.asObject();                
                System.out.println(counter + ":" + entry.get("name").asString() + ":" + 
                        entry.get("type").asString() + " id = " + entry.get("id").asInt());
                counter++;
            }*/
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SchemaFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SchemaFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return schemas;
    }
    
    public static void main(String[] args){
        SchemaFactory factory = new SchemaFactory();
        System.setProperty("CLAS12DIR", "/Users/gavalian/Work/Software/Release-4a.0.0/COATJAVA/coatjava");
        factory.initFromDirectory("CLAS12DIR","etc/bankdefs/hipo");
//.readSchemaDirectory("/Users/gavalian/Work/Software/Release-4a.0.0/COATJAVA/coatjava/etc/bankdefs/hipo");
        //factory.show();
        
        HipoEvent event = new HipoEvent(factory);
        
        HipoGroup group = factory.getSchema("ECAL::clusters").createGroup(5);
        
        event.writeGroup(group);
        event.show();
        
        event.removeGroup("ECAL::clusters");
        event.show();
        
        event.writeGroup(group);
        event.show();
        /*
        SchemaFactory factory = new SchemaFactory();
        factory.addSchema(new Schema("{1302,FTOF::dgtz}[1,px,FLOAT][2,py,FLOAT][3,pz,FLOAT]"));
        factory.addSchema(new Schema("{1304,DC::dgtz}[1,px,FLOAT][2,py,FLOAT][3,pz,FLOAT]"));
        factory.addSchema(new Schema("{1306,ECAL::dgtz}[1,px,FLOAT][2,py,FLOAT][3,pz,FLOAT]"));
        //factory.show();
        
        factory.readSchemaFile("/Users/gavalian/Work/Software/Release-9.0/COATJAVA/coatjava/etc/bankdefs/hipo/RAW.json");
        HipoEvent event = factory.getSchemaEvent();
        
        System.out.println(event.toString());
        SchemaFactory ff = new SchemaFactory();
        
        ff.setFromEvent(event);
        ff.show();*/
    }
}

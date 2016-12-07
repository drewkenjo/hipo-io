/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.hipo.schema.Schema;

/**
 *
 * @author gavalian
 */
public class HipoGroup {
    
    private Map<Integer,HipoNode>  groupNodes  = null;
    private Schema                 groupSchema = null;
    
    public HipoGroup(Schema schema){
        this.groupSchema = schema;
        groupNodes = new HashMap<Integer,HipoNode>();
    }
    
    public HipoGroup(Map<Integer,HipoNode> nodes){
        this.groupNodes = nodes;
    }
    
    public HipoGroup(Map<Integer,HipoNode> nodes, Schema schema){
        this.groupNodes = nodes;
        this.groupSchema = schema;
    }
    /**
     * add node to the group. The schema is checked to validate the nodes added
     * @param node node to be added to the group
     */
    public void addNode(HipoNode node){
        if(node.getGroup()!=groupSchema.getGroup()){
            System.out.println("[addNode] error --> group id is not consistent with " + groupSchema.getGroup());
            return;
        }        
        Schema.SchemaEntry entry = groupSchema.getEntry(node.getItem());
        if(entry==null){ 
            System.out.println("[addNode] error --> there is no entry in schema with id = " + node.getItem());
            return;
        }
        if(entry.getType()!=node.getType()){
            System.out.println("[addNode] error --> Schema entry item = " + node.getItem()
            + " has type = " + entry.getType().getName() + ". Node has type = " + node.getType().getName());
            return;
        }
        groupNodes.put(node.getItem(), node);
    }
    /**
     * returns a node with the name taken from the schema.
     * @param name name of the node according to the schema
     * @return HipoNode
     */
    public HipoNode getNode(String name){
        if(groupSchema==null) return null;
        Schema.SchemaEntry entry = this.groupSchema.getEntry(name);
        if(entry==null) return null;
        return this.groupNodes.get(entry.getId());
    }
    /**
     * returns a list of nodes. Used for writing into event.
     * @return List of nodes
     */
    public List<HipoNode> getNodes(){
        List<HipoNode> nodes = new ArrayList<HipoNode>();
        for(Map.Entry<Integer,HipoNode> entry : this.groupNodes.entrySet()){
            nodes.add(entry.getValue());
        }
        return nodes;
    }
    
    public int getMaxSize(){
        int size = 0;
        for(Map.Entry<Integer,HipoNode> entry : this.groupNodes.entrySet()){
            if(entry.getValue().getDataSize()>size) size = entry.getValue().getDataSize();
        }
        return size;
    }
    
    public Schema getSchema(){ return this.groupSchema;}
    public void show(){
        System.out.println("------------------------+---------------------------+");
        System.out.println(String.format(">>>> GROUP (group=%6d) (name=%s):", this.groupSchema.getGroup(),this.groupSchema.getName()));
        System.out.println("------------------------+---------------------------+");
        for(Map.Entry<Integer,HipoNode> entry : this.groupNodes.entrySet()){
            int key = entry.getKey();

            String name = this.groupSchema.getEntry(key).getName();
            System.out.println(String.format("%12s (%8s) : %s", name,entry.getValue().getType().getName(),
                    entry.getValue().getDataString()));
        }
        System.out.println("------------------------+---------------------------+");

    }
}

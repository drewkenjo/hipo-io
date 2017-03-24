/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jlab.hipo.schema.Schema;
import org.jlab.hipo.schema.SchemaFactory;
import org.jlab.hipo.utils.TextTable;

/**
 *
 * @author gavalian
 */
public class HipoEvent {
    
    ByteBuffer           eventBuffer = null;
    
    //List<HipoNodeIndex>   eventIndex = new ArrayList<HipoNodeIndex>();    
    private final Map<Integer,GroupNodeIndexList>  groupsIndex = new HashMap<Integer,GroupNodeIndexList>();
    
    
    private final SchemaFactory    eventSchemaFactory = new SchemaFactory();
        
    
    public HipoEvent(){
        byte[] header = new byte[8];
        header[0] = 'E';
        header[1] = 'V';
        header[2] = 'N';
        header[3] = 'T';
        
        eventBuffer = ByteBuffer.wrap(header);
        eventBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }
    
    public HipoEvent(SchemaFactory factory){        
        byte[] header = new byte[8];
        header[0] = 'E';
        header[1] = 'V';
        header[2] = 'N';
        header[3] = 'T';
        
        eventBuffer = ByteBuffer.wrap(header);
        eventBuffer.order(ByteOrder.LITTLE_ENDIAN);
        eventSchemaFactory.copy(factory);
        this.updateNodeIndex();
    }
    
    /**
     * Initialize HipoEvent from a byte array. 
     * @param buffer 
     */
    public HipoEvent(byte[] buffer){        
        eventBuffer = ByteBuffer.wrap(buffer);
        eventBuffer.order(ByteOrder.LITTLE_ENDIAN);
        updateNodeIndex();
    }
        
    public HipoEvent(byte[] buffer, SchemaFactory factory){        
        eventBuffer = ByteBuffer.wrap(buffer);
        eventBuffer.order(ByteOrder.LITTLE_ENDIAN);
        updateNodeIndex();
        eventSchemaFactory.copy(factory);
//        this.eventSchemaFactory = factory;
    }
    /**
     * Add a single node to the event.
     * @param node HipoNode to add to the event.
     */
    public void addNode(HipoNode node){
        
        int nodeLength  = node.getBufferSize();
        int eventLength = eventBuffer.capacity();
        
        byte[] dataBuffer = new byte[nodeLength+eventLength];
        byte[]       data = node.getBufferData();
        int      position = eventLength;
        
        System.arraycopy(eventBuffer.array(), 0, dataBuffer, 0, eventLength);
        System.arraycopy(data, 0, dataBuffer, position, node.getBufferSize());
        /*
        for(int i = 0; i < dataBuffer.length; i++){
            System.out.print(String.format("%3X", dataBuffer[i]));
        }*/
        eventBuffer = ByteBuffer.wrap(dataBuffer);
        eventBuffer.order(ByteOrder.LITTLE_ENDIAN);        
    }
    /**
     * Add the nodes in the list to the event.
     * @param nodes list of HipoNode objects to add to the event.
     */
    public void addNodes(List<HipoNode> nodes){
        
        int nodesLength = 0;
        for(HipoNode node : nodes){
            nodesLength += node.getBufferSize();
        }
        
        int eventLength = eventBuffer.capacity();
        
        byte[] dataBuffer = new byte[nodesLength+eventLength];
        // copy previous event, into the new byte array
        System.arraycopy(eventBuffer.array(), 0, dataBuffer, 0, eventLength);
        int position = eventLength;
        for(HipoNode node : nodes){
            byte[] data = node.getBufferData();
            System.arraycopy(data, 0, dataBuffer, position, node.getBufferSize());
            position += node.getBufferSize();
        }
        eventBuffer = ByteBuffer.wrap(dataBuffer);
        eventBuffer.order(ByteOrder.LITTLE_ENDIAN);
        this.updateNodeIndex();
    }
    /**
     * updates the index of the 
     */
    public final void updateNodeIndex(){
        
        int position = 8;
        int capacity = eventBuffer.capacity();
        
        //eventIndex.clear();
        this.groupsIndex.clear();
        int counter = 0;
        
        while((position+8)<capacity){
            
            short group = eventBuffer.getShort( position    );
            //System.out.println(" group = " + group);
            byte  item  = eventBuffer.get(      position + 2);
            byte  type  = eventBuffer.get(      position + 3);
            int   size  = eventBuffer.getInt(   position + 4);
            //System.out.println( counter + " : position = " + );
            NodeIndexList  idx = new NodeIndexList(item,position,size);            
            idx.setType(type);
            Integer groupInt = (int) group;            
            
            if(this.groupsIndex.containsKey(groupInt)==false){
                //System.out.println("--> adding group " + groupInt);
                this.groupsIndex.put( groupInt, new GroupNodeIndexList(group));
            }
            
            this.groupsIndex.get(groupInt).addNodeIndex(idx);
            //HipoNodeIndex index = new HipoNodeIndex();
            //index.nodeGroup  = group;
            //index.nodeItem   = item;
            //index.nodeOffset = position;
            //index.nodeLength = size;            
            //eventIndex.add(index);
            position += 8 + size;
        }
    }
    
    
    public void reset(){
        byte[] header = new byte[8];
        header[0] = 'E';
        header[1] = 'V';
        header[2] = 'N';
        header[3] = 'T';
        
        eventBuffer = ByteBuffer.wrap(header);
        eventBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }
    /**
     * writes all the nodes in the group into the event.
     * @param group group containing nodes
     */
    public void writeGroup(HipoGroup group){
        if(this.hasGroup(group.getSchema().getGroup())==true){
            System.out.println("[HipoEvent] warning : groups is not added. The event contains group id = " 
            + group.getSchema().getGroup() + " name = " + group.getSchema().getName());
        } else {
            List<HipoNode> nodes = group.getNodes();
            this.addNodes(nodes);
        }
        this.updateNodeIndex();
    }
    /**
     * returns a group of nodes for given Schema.
     * @param name name of the Schema
     * @return group containing HipoNodes
     */
    public HipoGroup getGroup(String name){
        if(this.eventSchemaFactory.hasSchema(name)==true){
            Schema schema = this.eventSchemaFactory.getSchema(name);
            Map<Integer,HipoNode> nodes = getGroup(schema.getGroup());
            return new HipoGroup(nodes,schema);
        }
        return null;
    }
    /**
     * Return string representation of the event.
     * @return string printout of the event
     */
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        /*
        str.append(String.format("EVENT (SIZE = %12d)\n",eventBuffer.capacity()));
        for(HipoNodeIndex index : eventIndex){
            str.append(index.toString());
            str.append("\n");
        }*/
        str.append(toGroupListString());
        return str.toString();
    }
    
    public byte[] getDataBuffer(){
        return this.eventBuffer.array();
    }
    
    public void show(){
        TextTable table = new TextTable("id:name:entries:group:items","4:36:12:9:9");
        //System.out.println("+------------------------------------------------------------+");
        Integer counter = 0;
        for(Map.Entry<Integer,GroupNodeIndexList>  entry : this.groupsIndex.entrySet()){
            String name = "N/A";
            if(this.eventSchemaFactory.hasSchema(entry.getKey())==true){
                name = this.eventSchemaFactory.getSchema(entry.getKey()).getName();
            }
            Integer size = entry.getValue().getItemList().size();
            HipoGroup group = this.getGroup(this.eventSchemaFactory.getSchema(entry.getKey()).getName());
            Integer rows = group.getMaxSize();
            table.addData(new String[]{counter.toString(), name, rows.toString(), entry.getKey().toString(),size.toString()});
            //System.out.println(String.format("|%24d | %-24s | %5d |", 
            //        entry.getKey(), name, entry.getValue().getItemList().size()));
            counter++;
        }
        //System.out.println("+------------------------------------------------------------+");
        System.out.println(table.toString());
        
    }
        
    public void showGroupByOrder(int order){
        Integer counter = 0;
        for(Map.Entry<Integer,GroupNodeIndexList>  entry : this.groupsIndex.entrySet()){
            if(counter==order){
                int id = entry.getValue().getGroup();
                this.showGroup(id);
            }
            counter++;
        }
    }
    
    public void showGroup(int group){
        Schema schema = getSchemaFactory().getSchema(group);
        this.showGroup(schema.getName());
    }
    
    public void showGroup(String group){
        if(this.hasGroup(group)==true){
            HipoGroup bank = this.getGroup(group);
            bank.show();
        }
    }
    
    public HipoNode getNode(int group, int item){
        
        if(this.groupsIndex.containsKey(group)==true){
            GroupNodeIndexList list = groupsIndex.get(group);
            if(list.hasItem(item)==true){
                NodeIndexList nL = list.getItemIndex(item);
                int nodeLength = 8 + nL.nodeLength;
                byte[] array = new byte[nodeLength];
                System.arraycopy(eventBuffer.array(), nL.nodeOffset, array, 0, nodeLength);
                return new HipoNode(array);
            }
        }
        /*
        for(HipoNodeIndex index : eventIndex){
            if(index.nodeGroup==group&&index.nodeItem==item){
                int nodeLength = 8 + index.nodeLength;
                byte[] array = new byte[nodeLength];
                System.arraycopy(eventBuffer.array(), index.nodeOffset, array, 0, nodeLength);
                return new HipoNode(array);
            }
        }*/
        System.out.println("---> error : requested node (" 
                + group + "," + item + ") is not found.");
        return null;
    }
    
    public boolean hasGroup(int group){
        return this.groupsIndex.containsKey(group);
    }
    
    public boolean hasGroup(String group){
        if(this.eventSchemaFactory.hasSchema(group)){
            if (this.eventSchemaFactory.getSchema(group) == null){
                System.out.println("---> warning : schema for the group = "+ group +" is null.");
            } else {
                int groupId = this.eventSchemaFactory.getSchema(group).getGroup();
                return this.groupsIndex.containsKey(groupId);
            }
        }
        return false;
    }
    
    public SchemaFactory  getSchemaFactory(){ return this.eventSchemaFactory;}
    
    public List<HipoGroup>  getGroups(){
        List<HipoGroup> groups = new ArrayList<HipoGroup>();
        for(Map.Entry<Integer,GroupNodeIndexList> entry : this.groupsIndex.entrySet()){
            Map<Integer,HipoNode> nodes = this.getGroup(entry.getKey());
            Schema               schema = this.getSchemaFactory().getSchema(entry.getKey());
            if(schema!=null){
                groups.add(new HipoGroup(nodes,schema));
            } else {
                groups.add(new HipoGroup(nodes));
            }
        }
        return groups;
    }
    /**
     * Removes given group with given group id from the event
     * @param group 
     */
    public void removeGroup(int group){
        if(this.hasGroup(group)==false) return;
        List<HipoGroup> groups = getGroups();
        reset();
        this.groupsIndex.clear();
        for(HipoGroup entry : groups){
            if(entry.getSchema().getGroup()!=group){
                //System.out.println("writing group " + entry.getSchema().getName());
                this.writeGroup(entry);
            }
        }
        this.updateNodeIndex();
    }
    /**
     * removes group with given name from the event
     * @param name 
     */
    public void removeGroup(String name){
        if(this.hasGroup(name)==false) return;
        Schema schema = this.eventSchemaFactory.getSchema(name);
        if(schema!=null){
            this.removeGroup(schema.getGroup());
        }
    }
    
    public Map<Integer,HipoNode>  getGroup(int group){
        Map<Integer,HipoNode> groupNodes = new LinkedHashMap<Integer,HipoNode>();
        if(this.groupsIndex.containsKey(group)==true){
            GroupNodeIndexList list = groupsIndex.get(group);
            for(Map.Entry<Integer,NodeIndexList> entry : list.getItemList().entrySet()){
                int itemID = entry.getValue().nodeItem;
                HipoNode node = this.getNode(group, itemID);
                groupNodes.put(itemID, node);
            }
        }
        /*for(HipoNodeIndex index : eventIndex){
            if(index.nodeGroup==group){                
                HipoNode node = getNode(group,index.nodeItem);
                groupNodes.put(index.nodeItem,node);
            }
        }*/
        return groupNodes;
    }
    
    public String toGroupListString(){
        StringBuilder str = new StringBuilder();
        for(Map.Entry<Integer,GroupNodeIndexList> entry : this.groupsIndex.entrySet()){
            str.append(entry.getValue().toString());
        }
        return str.toString();
    }
    
   
    public static class GroupNodeIndexList {
        
        private int groupid = 0;
        
        private final Map<Integer,NodeIndexList>  nodesIndex = new LinkedHashMap<Integer,NodeIndexList>();
        
        public GroupNodeIndexList(int gr){
            groupid = gr;
        }
        
        public int getGroup(){
            return groupid;
        }
        /**
         * add a node index into the list. if the item already exists in the
         * list a warning will be printed, and new item will not be added.
         * @param indx node index list
         */
        public void addNodeIndex(NodeIndexList indx){
            //System.out.println("----------> adding " + indx.nodeItem);
            if(nodesIndex.containsKey(indx.nodeItem)==true){
                //System.out.println("[GroupNodeIndexList] --> error : group = "
                //        + groupid + "  already has an item with id="+indx.nodeItem);
                return;
            }
            nodesIndex.put(indx.nodeItem, indx);
        }
        /**
         * returns map containing items in the group.
         * @return map
         */
        public Map<Integer,NodeIndexList>  getItemList(){
            return this.nodesIndex;    
        }
        
        public NodeIndexList getItemIndex(int item){
            return this.nodesIndex.get(item);
        }
        
        public boolean hasItem(int item){
            return this.nodesIndex.containsKey(item);
        }
        
        @Override
        public String toString(){
            StringBuilder str = new StringBuilder();
            str.append(String.format("GROUP (%6d) \n", groupid));
            for(Map.Entry<Integer,NodeIndexList> entry : nodesIndex.entrySet()){
                str.append("\t");
                str.append(entry.getValue().toString());
                str.append("\n");
            }
            return str.toString();
        }
    }
    
    public static class NodeIndexList {
        
        int  nodeItem   = 0;
        int  nodeOffset = 0;
        int  nodeLength = 0;
        int  nodeType   = 0;
        
        public NodeIndexList(int item, int offset, int length){
            nodeItem   = item;
            nodeOffset = offset;
            nodeLength = length;
        }
        
        @Override
        public String toString(){
            StringBuilder str = new StringBuilder();
            str.append(String.format("ITEM (%4d) TYPE (%2d) LENGTH (%6d)  OFFSET (%6d)", 
                    nodeItem,nodeType,nodeLength,nodeOffset));
            return str.toString();
        }
        
        public int getType(){return nodeType;}        
        public NodeIndexList setType(int type){ nodeType = type; return this;}
    }
    
    public static class HipoNodeIndex {
        
        int nodeGroup  = 0;
        int nodeItem   = 0;
        int nodeOffset = 0;
        int nodeLength = 0;
        
        @Override
        public String toString(){
            StringBuilder str = new StringBuilder();
            str.append(String.format("( %6d, %3d, %8d, %6d)",nodeGroup,nodeItem,nodeOffset,nodeLength));
            return str.toString();
        }
    }
    
    public static void main(String[] args){
        
        HipoNode node = new HipoNode(1200,25,HipoNodeType.SHORT,5);
        for(int i = 0; i < 5; i++) { node.setShort(i, (short) ((i+1)*2) );}
        System.out.println(node.getHeaderString() + " : " + node.getDataString());
        
        HipoNode nodeF = new HipoNode(1200,2,HipoNodeType.FLOAT,8);
        for(int i = 0; i < 8; i++) { nodeF.setFloat(i,  (float)  ((i+1)*2.0+0.5) );}
        System.out.println(nodeF.getHeaderString() + " : " + nodeF.getDataString());
        List<HipoNode>  nodes = new ArrayList<HipoNode>();
        nodes.add(nodeF);
        nodes.add(node);
        
        
        //nodes.add(node);
        HipoEvent event = new HipoEvent();
        event.addNodes(nodes);
        //event.addNode(node);
        //event.addNode(nodeF);
        
        event.updateNodeIndex();
        System.out.println(event);
        
        HipoNode nodeFLOAT = event.getNode(1200, 2);
        System.out.println(nodeFLOAT.getHeaderString() + " : " + nodeFLOAT.getDataString());
        HipoNode nodeSHORT = event.getNode(1200, 25);
        System.out.println(nodeSHORT.getHeaderString() + " : " + nodeSHORT.getDataString());
        
        Map<Integer,HipoNode>  group1200 = event.getGroup(1200);
        System.out.println("-----------");
        for(Map.Entry<Integer,HipoNode>  entry : group1200.entrySet()){
            System.out.println(entry.getValue().getHeaderString() + " : " + entry.getValue().getDataString());
        }
    }
}

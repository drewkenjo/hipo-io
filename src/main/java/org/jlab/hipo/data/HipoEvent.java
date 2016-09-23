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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gavalian
 */
public class HipoEvent {
    
    ByteBuffer           eventBuffer = null;
    List<HipoNodeIndex>   eventIndex = new ArrayList<HipoNodeIndex>();
    
    
    public HipoEvent(){
        byte[] header = new byte[8];
        header[0] = 'E';
        header[1] = 'V';
        header[2] = 'N';
        header[3] = 'T';
        
        eventBuffer = ByteBuffer.wrap(header);
        eventBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }
    /**
     * Initialize HipoEvent from a byte array. 
     * @param buffer 
     */
    public HipoEvent(byte[] buffer){        
        eventBuffer = ByteBuffer.wrap(buffer);
        createNodeIndex();
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
        
        for(int i = 0; i < dataBuffer.length; i++){
            System.out.print(String.format("%3X", dataBuffer[i]));
        }
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
    }
    
    public final void createNodeIndex(){
        
        int position = 8;
        int capacity = eventBuffer.capacity();
        
        eventIndex.clear();
        
        while((position+8)<capacity){
            short group = eventBuffer.getShort( position    );
            //System.out.println(" group = " + group);
            byte  item  = eventBuffer.get(      position + 2);
            byte  type  = eventBuffer.get(      position + 3);
            int   size  = eventBuffer.getInt(   position + 4);
            
            HipoNodeIndex index = new HipoNodeIndex();
            index.nodeGroup  = group;
            index.nodeItem   = item;
            index.nodeOffset = position;
            index.nodeLength = size;
            
            eventIndex.add(index);
            position += 8 + size;
        }
    }
    /**
     * Return string representation of the event.
     * @return string printout of the event
     */
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("EVENT (SIZE = %12d)\n",eventBuffer.capacity()));
        for(HipoNodeIndex index : eventIndex){
            str.append(index.toString());
            str.append("\n");
        }
        return str.toString();
    }
    
    
    
    public HipoNode getNode(int group, int item){
        for(HipoNodeIndex index : eventIndex){
            if(index.nodeGroup==group&&index.nodeItem==item){
                int nodeLength = 8 + index.nodeLength;
                byte[] array = new byte[nodeLength];
                System.arraycopy(eventBuffer.array(), index.nodeOffset, array, 0, nodeLength);
                return new HipoNode(array);
            }
        }
        System.out.println("---> error : requested node (" 
                + group + "," + item + ") is not found.");
        return null;
    }
    
    public Map<Integer,HipoNode>  getGroup(int group){
        Map<Integer,HipoNode> groupNodes = new LinkedHashMap<Integer,HipoNode>();
        for(HipoNodeIndex index : eventIndex){
            if(index.nodeGroup==group){                
                HipoNode node = getNode(group,index.nodeItem);
                groupNodes.put(index.nodeItem,node);
            }
        }
        return groupNodes;
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
        nodes.add(node);
        nodes.add(nodeF);
        HipoEvent event = new HipoEvent();
        event.addNodes(nodes);
        //event.addNode(node);
        //event.addNode(nodeF);
        
        event.createNodeIndex();
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

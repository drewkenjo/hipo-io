/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gavalian
 */
public class HipoNodeBuilder<T extends Number> {
    private List<T>  container = new ArrayList<T>();
    private int      containerLimit = -1;
    
    public HipoNodeBuilder(){
        
    }
    
    public HipoNodeBuilder(int limit){
        this.containerLimit = limit;
    }
    
    public void push(T value){
        if(containerLimit<0){
            container.add(value);
        } else {
            if(containerLimit<container.size()){
             container.add(value);
            } else {
                System.out.println("[HipoNodeBuilder] warning : container is full, no value added");
            }
        }
    }
    
    public boolean isFull(){
        if(container.size()>=containerLimit) return true;
        return false;
    }
    
    public HipoNode buildNode(int group, int item){
        
        if(container.size()>0){
            T value = container.get(0);
            /**
             * Create a node with Long type fill it an return the node
             */
            if(value instanceof Long){
                HipoNode nodeLong = new HipoNode(group,item,
                        HipoNodeType.LONG,container.size());
                for(int i = 0; i < container.size(); i++){
                    Long itemValue = (Long) container.get(i);
                    nodeLong.setLong(i, itemValue);
                }
                return nodeLong;
            }
            /**
             * Create a node with Integer type fill it an return the node
             */
            if(value instanceof Integer){
                HipoNode nodeInt = new HipoNode(group,item,
                        HipoNodeType.INT,container.size());
                for(int i = 0; i < container.size(); i++){
                    Integer itemValue = (Integer) container.get(i);
                    nodeInt.setInt(i, itemValue);
                }
                return nodeInt;
            }
            /**
             * Create a node with Float type fill it an return the node
             */
            if(value instanceof Float){
                HipoNode nodeFloat = new HipoNode(group,item,
                        HipoNodeType.FLOAT,container.size());
                for(int i = 0; i < container.size(); i++){
                    Float itemValue = (Float) container.get(i);
                    nodeFloat.setFloat(i, itemValue);
                }
                return nodeFloat;
            }
        }
        return null;
    }
}

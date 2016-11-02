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
 * @param <T> node type to build, accepts classes extending Number
 */
public class HipoNodeBuilder<T extends Number> {
    
    private final List<T>  container = new ArrayList<T>();
    private int       containerLimit = -1;
    /**
     * Default Constructor. Sets the maximum limit of elements to
     * negative number, therefore there is no restrictions on how many
     * elements can be stored.
     */
    public HipoNodeBuilder(){
        this.containerLimit = -1;
    }
    /**
     * Constructor with setting a limit on how many elements
     * maximum can be stored in the builder.
     * @param limit maximum number of elements to store
     */
    public HipoNodeBuilder(int limit){
        this.containerLimit = limit;
    }
    /**
     * adds an element of given type to the end of the list.
     * the check is performed to see if the array has reached the
     * maximum allowed size set by user.
     * @param value element to add to the array.
     */
    public void push(T value){
        if(containerLimit<0){
            container.add(value);
        } else {
            if(containerLimit>container.size()){
             container.add(value);
            } else {
                System.out.println("[HipoNodeBuilder] warning : container is full, no value added "
                + " size = " + container.size() + "  limit = " + containerLimit);
            }
        }
    }
    /**
     * returns false if the array capacity is less than predetermined
     * maximum size of the node, if limit is negative it always returns false.
     * @return true if maximum size is reached, false otherwise
     */
    public boolean isFull(){
        if(container.size()>=containerLimit) return true;
        return false;
    }
    /**
     * returns current size of the container.
     * @return 
     */
    public int getSize(){
        return this.container.size();
    }
    /**
     * Resets the content of the node builder, the array is cleared.
     * the maximum size limit is not changed.
     */
    public void reset(){
        this.container.clear();
    }
    /**
     * Builds a node from the array, type is determined by Template.
     * @param group group is for the node
     * @param item item id for the node
     * @return HipoNode class 
     */
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
            if(value instanceof Double){
                HipoNode nodeFloat = new HipoNode(group,item,
                        HipoNodeType.DOUBLE,container.size());
                for(int i = 0; i < container.size(); i++){
                    Double itemValue = (Double) container.get(i);
                    nodeFloat.setDouble(i, itemValue);
                }
                return nodeFloat;
            }
        }
        return null;
    }
    
}

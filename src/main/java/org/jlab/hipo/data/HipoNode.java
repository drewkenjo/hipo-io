/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.jlab.hipo.io.HipoByteUtils;

/**
 *
 * @author gavalian
 */
public class HipoNode {
    
    ByteBuffer   nodeBuffer   = null;
    HipoNodeType nodeType     = null;
    
    /**
     * Description of header bytes order.
     */
    private int            headerLength = 8;
    private int  headerLengthDataOffset = 6;
    private int     hederTypeDataOffset = 2;
    
    public HipoNode(int group, int item, String value){
        createNode(group,item,value);
    }
    
    public HipoNode(int group, int item, HipoNodeType type, int length){
        createNode(group, item, type, length);
    }
    /**
     * Initialize HipoNode from a byte array. 
     * @param buffer byte array with the data
     */
    public HipoNode(byte[] buffer){
        nodeBuffer = ByteBuffer.wrap(buffer);
        nodeBuffer.order(ByteOrder.LITTLE_ENDIAN);
        nodeType = getType();
    }
    
    public final void createNode(int group, int item, String value){
        byte[] array = value.getBytes();
        createNode(group,item,HipoNodeType.STRING,array.length);
        System.arraycopy(array, 0, nodeBuffer.array(), 8, array.length);
        //for(int i = 0; i < array.length; i++) { setByte(i,array[i]);}
    }
    /**
     * Creates a node for given type
     * @param group group id
     * @param item item id
     * @param type type of the node
     * @param length number of elements in the node
     */
    public final void createNode(int group, int item, HipoNodeType type, int length){
        int bytesPerEntry = type.getSize(); 
        int totalLength = this.headerLength + length*bytesPerEntry;
        byte[] array = new byte[totalLength];
        nodeBuffer   = ByteBuffer.wrap(array);
        nodeBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        short groupID  = HipoByteUtils.getShortFromInt(group);
        int   lengthID = length*bytesPerEntry;
        byte  itemID   = HipoByteUtils.getByteFromInt(item);
        byte  typeID   = HipoByteUtils.getByteFromInt(type.getType());
        
        nodeBuffer.putShort( 0,  groupID); // byte 0 and 1 (16 bits) are group ID
        nodeBuffer.put(      2,   itemID); // byte 2 ( 8 bits) is item id
        nodeBuffer.put(      3,   typeID); // byte 3 describes the type 
        nodeBuffer.putInt(   4, lengthID);
        
        nodeType = getType();
    }
    /**
     * returns number of elements in the data array. This is not the buffer length.
     * @return n elements of the array if the node is a primitive data.
     */
    public int getDataSize(){
        HipoNodeType   type = getType();
        int    bufferLength = nodeBuffer.getInt(4);
        int           ndata = bufferLength/type.getSize();
        return ndata;
    }
    
    public int getBufferSize(){
        return nodeBuffer.capacity();
    }
    
    public byte[] getBufferData(){
        return nodeBuffer.array();
    }
    /**
     * returns the group id of the node.
     * @return group id
     */
    public int getGroup(){
        short groupid = nodeBuffer.getShort(0);
        return (int) groupid;
    }
    /**
     * returns the item id of the node
     * @return item id
     */
    public int getItem(){
        byte itemid = nodeBuffer.get(2);
        return (int) itemid;
    }
    
    /**
     * returns string representation of the data.
     * @return data string
     */
    public String getDataString(){
        StringBuilder str = new StringBuilder();
        HipoNodeType  type = getType();
        
        if(type==HipoNodeType.BYTE){
            int ndata = getDataSize();
            for(int i = 0; i < ndata; i++){
                str.append(String.format(" %8d", getByte(i)));
            }
        }
        
        if(type==HipoNodeType.SHORT){
            int ndata = getDataSize();
            for(int i = 0; i < ndata; i++){
                str.append(String.format(" %8d", getShort(i)));
            }
        }
        
        if(type==HipoNodeType.INT){
            int ndata = getDataSize();
            for(int i = 0; i < ndata; i++){
                str.append(String.format(" %8d", getInt(i)));
            }
        }
        
        if(type==HipoNodeType.FLOAT){
            int ndata = getDataSize();
            for(int i = 0; i < ndata; i++){
                str.append(String.format(" %8.3f", getFloat(i)));
            }
        }
        return str.toString();
    }    
    /**
     * returns a String with header information.
     * @return string representation of the header.
     */
    public String getHeaderString(){
        StringBuilder str = new StringBuilder();
        short group = nodeBuffer.getShort(0);
        byte  item  = nodeBuffer.get(2);
        byte  type  = nodeBuffer.get(3);
        int   len   = nodeBuffer.getInt(4);
        str.append(String.format("(%8d,%4d) <%2d> [%6d]", group,item,type,len));
        return str.toString();
    }
    /**
     * returns type of the elements stored in the buffer.
     * @return 
     */
    public final HipoNodeType  getType(){
        int type = (int) nodeBuffer.get(3);
        return HipoNodeType.getType(type);
    }    
    
    private int getOffset(int index){
        HipoNodeType type = getType();
        return this.headerLength + type.getSize()*index;
    }        
    
    
    /**
     * returns a String object from the node. Strings are stored 
     * as byte[]. The data is copied into String.
     * @return String representation of byte array.
     */
    public String getString(){
        if(nodeType!=HipoNodeType.STRING){
            printWrongTypeMessage(HipoNodeType.STRING);
            return "";
        }
        int offset = getOffset(0);
        int length = getDataSize();
        byte[] array = new byte[length];
        System.arraycopy(nodeBuffer.array(), offset, array, 0, length);
        return new String(array);
    }
    
    public byte getByte(int index){
        if(nodeType!=HipoNodeType.BYTE){
            printWrongTypeMessage(HipoNodeType.BYTE);
            return (byte) 0;
        }
        int offset = getOffset(index);
        return nodeBuffer.get(offset);
    }
    
    public float getFloat(int index){
        if(nodeType!=HipoNodeType.FLOAT){
            printWrongTypeMessage(HipoNodeType.FLOAT);
            return 0.0f;
        }
        int offset = getOffset(index);
        return nodeBuffer.getFloat(offset);
    }
    /*
    public short[] getDataShort(){
        
    }*/
    
    public short getShort(int index){
        if(nodeType!=HipoNodeType.SHORT){
            printWrongTypeMessage(HipoNodeType.SHORT);
            return (short) 0;
        }
        int offset = getOffset(index);
        return nodeBuffer.getShort(offset);
    }
    
    public int getInt(int index){
        if(nodeType!=HipoNodeType.INT){
            printWrongTypeMessage(HipoNodeType.INT);
            return 0;
        }
        int offset = getOffset(index);
        return nodeBuffer.getInt(offset);
    }
    
    public long getLong(int index){
        if(nodeType!=HipoNodeType.LONG){
            printWrongTypeMessage(HipoNodeType.LONG);
            return 0;
        }
        int offset = getOffset(index);
        return nodeBuffer.getLong(offset);
    }
    
    public double getDouble(int index){
        if(nodeType!=HipoNodeType.DOUBLE){
            printWrongTypeMessage(HipoNodeType.DOUBLE);
            return 0.0;
        }
        int offset = getOffset(index);
        return nodeBuffer.getDouble(offset);
    }
    /**
     * Set content of the node with type BYTE, for element index
     * @param index element index
     * @param value byte value to set
     */
    public void setByte(int index, byte value){
        if(nodeType!=HipoNodeType.BYTE){
            printWrongTypeMessage(HipoNodeType.BYTE);
            return;
        }
        int offset = getOffset(index);
        nodeBuffer.put(offset, value);
    }
    
    public void setShort(int index, short value){
        if(nodeType!=HipoNodeType.SHORT){
            printWrongTypeMessage(HipoNodeType.SHORT);
            return;
        }
        int offset = getOffset(index);
        nodeBuffer.putShort(offset, value);
    }        
    
    public void setInt(int index, int value){
        if(nodeType!=HipoNodeType.INT){
            printWrongTypeMessage(HipoNodeType.INT);
            return;
        }
        int offset = getOffset(index);
        nodeBuffer.putInt(offset, value);
    } 
    
    public void setInt(int index, long value){
        if(nodeType!=HipoNodeType.LONG){
            printWrongTypeMessage(HipoNodeType.LONG);
            return;
        }
        int offset = getOffset(index);
        nodeBuffer.putLong(offset, value);
    }
    
    public void setFloat(int index, float value){
        if(nodeType!=HipoNodeType.FLOAT){
            printWrongTypeMessage(HipoNodeType.FLOAT);
            return;
        }
        int offset = getOffset(index);
        nodeBuffer.putFloat(offset, value);
    }
    
    public void setDouble(int index, double value){
        if(nodeType!=HipoNodeType.DOUBLE){
            printWrongTypeMessage(HipoNodeType.DOUBLE);
            return;
        }
        int offset = getOffset(index);
        nodeBuffer.putDouble(offset, value);
    }
    /**
     * prints error message when wrong type is selected for the node.
     * @param type mistaken type
     */
    private void printWrongTypeMessage(HipoNodeType type){
        System.out.print("[hipo node] --> error : ");
        System.out.print(String.format("(%d,%d)", getGroup(), getItem()));
        System.out.println(" requested type="+type.getName()+" has type="+nodeType.getName());
    }
    /**
     * main program for tests
     * @param args 
     */
    public static void main(String[] args){
        HipoNode node = new HipoNode(1200,1,HipoNodeType.SHORT,5);
        for(int i = 0; i < 5; i++) { node.setShort(i, (short) ((i+1)*2) );}
        System.out.println(node.getHeaderString() + " : " + node.getDataString());
        
        HipoNode nodeF = new HipoNode(1200,1,HipoNodeType.FLOAT,8);
        for(int i = 0; i < 8; i++) { nodeF.setFloat(i,  (float)  ((i+1)*2.0+0.5) );}
        System.out.println(nodeF.getHeaderString() + " : " + nodeF.getDataString());
        
        HipoNode nodeString = new HipoNode(20,1,"Histogram");
        
        System.out.println("VALUE = [" + nodeString.getString() + "]");
        
    }
}

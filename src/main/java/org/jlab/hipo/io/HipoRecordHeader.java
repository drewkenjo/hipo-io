/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author gavalian
 */
public class HipoRecordHeader {
    
    public static final int RECORD_HEADER_SIZE = 40;
    
    public static final byte[] RECORD_IDENTIFIER_STRING  = new byte[]{'H','R','E','C'};
    public static final int    RECORD_IDENTIFIER_INTEGER = 0x43455248; // INTEGER REPRESENTING STRING 'HREC'
    /**
     * Define offsets for each WORD in the header
     */
    public static final int  OFFSET_RECORD_LENGTH_WORD = 4;
    public static final int  OFFSET_DATA_LENGTH_WORD_UNCOMPRESSED = 8;
    public static final int  OFFSET_DATA_LENGTH_WORD_COMPRESSED   = 12;
    public static final int  OFFSET_NUMBER_OF_EVENTS    = 16;
    public static final int  OFFSET_HEADER_LENGTH_WORD  = 20;
    public static final int  OFFSET_INDEX_LENGTH_WORD   = 24;
    
    
    
    ByteBuffer  recordHeaderBuffer = null;
    
    
    public HipoRecordHeader(){
        byte[] headerBytes = new byte[HipoRecordHeader.RECORD_HEADER_SIZE];
        recordHeaderBuffer = ByteBuffer.wrap(headerBytes);
        recordHeaderBuffer.order(ByteOrder.LITTLE_ENDIAN);
        for(int i = 0; i < 4; i++) recordHeaderBuffer.put(i, HipoRecordHeader.RECORD_IDENTIFIER_STRING[i]);
        reset();
    }
    
    public final void initBinary(byte[] array){
        recordHeaderBuffer = ByteBuffer.wrap(array);
        recordHeaderBuffer.order(ByteOrder.LITTLE_ENDIAN);
        /*System.out.println(String.format(" FIRST BYTE = %X %X ", 
                recordHeaderBuffer.getInt(0), 
                recordHeaderBuffer.getInt(0)&0x4FFFFFFF));*/
    }
    
    public final boolean isValid(){
        int magicWord = recordHeaderBuffer.getInt(0);
        return (magicWord&0x4FFFFFFF)==HipoRecordHeader.RECORD_IDENTIFIER_INTEGER;
    }
    
    public int getDataSize(){
        return recordHeaderBuffer.getInt(HipoRecordHeader.OFFSET_DATA_LENGTH_WORD_UNCOMPRESSED);
    }
    
    public int getDataSizeCompressed(){
        int result = recordHeaderBuffer.getInt(HipoRecordHeader.OFFSET_DATA_LENGTH_WORD_COMPRESSED);
        int size = HipoByteUtils.getInteger(result, 0,23);
        return size;
    }
    
    public int getRecordHeaderLength(){
        return HipoRecordHeader.RECORD_HEADER_SIZE;
    }
    
    public int getNumberOfEvents(){
        return recordHeaderBuffer.getInt(HipoRecordHeader.OFFSET_NUMBER_OF_EVENTS);
    }
    
    public int getRecordSize(){
        return recordHeaderBuffer.getInt(HipoRecordHeader.OFFSET_RECORD_LENGTH_WORD);
    }
    
    public int getHeaderSize(){
         return recordHeaderBuffer.getInt(HipoRecordHeader.OFFSET_HEADER_LENGTH_WORD);
    }

    public final int getIndexArraySize(){
        int result = recordHeaderBuffer.getInt(HipoRecordHeader.OFFSET_INDEX_LENGTH_WORD);
        int size   = HipoByteUtils.getInteger(result, 0, 23);
        return size;
    }
    
    public int getCompressionType(){
        int result = recordHeaderBuffer.getInt(HipoRecordHeader.OFFSET_DATA_LENGTH_WORD_COMPRESSED);
        int type = HipoByteUtils.getInteger(result, 24,30);
        return type;
    }
    
    public byte[] getRecordHeaderData(){
        return recordHeaderBuffer.array();
    }
    /* Setter functions */
    
    public final void setDataSize(int size){
        int result = recordHeaderBuffer.getInt(HipoRecordHeader.OFFSET_DATA_LENGTH_WORD_UNCOMPRESSED);
        result     = HipoByteUtils.write(result, size, 0, 23);
        recordHeaderBuffer.putInt(HipoRecordHeader.OFFSET_DATA_LENGTH_WORD_UNCOMPRESSED, result);
    }
    
    public final void setDataSizeCompressed(int size){
        int result = recordHeaderBuffer.getInt(HipoRecordHeader.OFFSET_DATA_LENGTH_WORD_COMPRESSED);
        result     = HipoByteUtils.write(result, size, 0, 23);
        recordHeaderBuffer.putInt(HipoRecordHeader.OFFSET_DATA_LENGTH_WORD_COMPRESSED, result);
    }
    
    public final void setIndexArraySize(int size){
        int result = recordHeaderBuffer.getInt(HipoRecordHeader.OFFSET_INDEX_LENGTH_WORD);
        result     = HipoByteUtils.write(result, size, 0, 23);
        recordHeaderBuffer.putInt(HipoRecordHeader.OFFSET_INDEX_LENGTH_WORD, result);
    }
    
    public final void setRecordHeaderLength(int length){
        recordHeaderBuffer.putInt(HipoRecordHeader.OFFSET_RECORD_LENGTH_WORD, length);
    }
    
    public final void setNumberOfEvents(int nevents){
        recordHeaderBuffer.putInt(HipoRecordHeader.OFFSET_NUMBER_OF_EVENTS, nevents);
    }
    
    public final void setHeaderSize(int size){
        this.recordHeaderBuffer.putInt(HipoRecordHeader.OFFSET_HEADER_LENGTH_WORD, size);
    }
    
    public void setCompressionType(int type){
        int result = recordHeaderBuffer.getInt(HipoRecordHeader.OFFSET_DATA_LENGTH_WORD_COMPRESSED);
        result     = HipoByteUtils.write(result, type, 24, 30);
        recordHeaderBuffer.putInt(HipoRecordHeader.OFFSET_DATA_LENGTH_WORD_COMPRESSED, result);
    }
    
    public void setRecordSize(int size){
        recordHeaderBuffer.putInt(HipoRecordHeader.OFFSET_RECORD_LENGTH_WORD,size);
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append("RECORD : ");
        str.append(String.format("SIZE = %8d, # EVENTS = %9d, DATA SIZE (COMP) = (%8d , %8d), HEADER %8d, TYPE = %2d, INDEX SIZE %8d",
                getRecordSize(),
                this.getNumberOfEvents(),
                this.getDataSize(), this.getDataSizeCompressed(), this.getHeaderSize(),
                this.getCompressionType(),this.getIndexArraySize()));
        return str.toString();
    }
    
    public final void reset(){
        setDataSize(0);
        setDataSizeCompressed(0);
        setHeaderSize(0);
        setNumberOfEvents(0);
        setIndexArraySize(0);
        setRecordHeaderLength(0);
    }
    
    public static void main(String[] args){
        HipoRecordHeader header = new HipoRecordHeader();
        header.setRecordHeaderLength(819);
        header.setDataSize(40);
        header.setHeaderSize(120);
        System.out.println(header);
        System.out.println("Magic = " + header.isValid());
    }
}

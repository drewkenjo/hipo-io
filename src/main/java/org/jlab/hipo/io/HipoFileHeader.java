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
public class HipoFileHeader {
    
    public static final int FILE_HEADER_LENGTH = 72;
    public static final int FILE_IDENTIFIER    = 0x4F504948;
    public static final int FILE_VERSION       = 0x312E3056;
    
    public static final int OFFSET_FILE_SIZE = 8;
    public static final int OFFSET_FILE_HEADER_SIZE = 12;
    public static final byte[] HIPO_FILE_SIGNATURE_BYTES = new byte[]{'H','I','P','O','V','0','.','2'};

    
    ByteBuffer  fileHeader = null;
    
    public HipoFileHeader(){
        byte[] header = new byte[HipoFileHeader.FILE_HEADER_LENGTH];
        fileHeader = ByteBuffer.wrap(header);
        fileHeader.order(ByteOrder.LITTLE_ENDIAN);
        for(int i = 0 ; i < 8; i++) fileHeader.put(i, HipoFileHeader.HIPO_FILE_SIGNATURE_BYTES[i]);
        this.setHeaderSize(0);
    }
    
    public HipoFileHeader(byte[] header){
        fileHeader = ByteBuffer.wrap(header);
        fileHeader.order(ByteOrder.LITTLE_ENDIAN);
    }
    
    public int getRecordStart(){
        return HipoFileHeader.FILE_HEADER_LENGTH + getHeaderSize();
    }
    
    public int getHeaderSize(){
        int headerSize = fileHeader.getInt(HipoFileHeader.OFFSET_FILE_HEADER_SIZE);
        return headerSize;
    }
    
    public int getIdentifier(){
        return fileHeader.getInt(0);
    }
    
    public int getVersion(){
        return fileHeader.getInt(4);
    }
    
    public int getHeaderStart(){
        return HipoFileHeader.FILE_HEADER_LENGTH;
    }
    
    public final void setHeaderSize(int size){
        fileHeader.putInt(HipoFileHeader.OFFSET_FILE_HEADER_SIZE, size);
    }
    
    public ByteBuffer build(){
        return this.fileHeader;
    }
}

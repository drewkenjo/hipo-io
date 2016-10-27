/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gavalian
 */
public class HipoRecord {
    
    int  headerL = 0; // Record Marker Word
    int  headerM = 0; // 24-31 Options, 0-23 - number of events
    int  headerH = 0; // Data Buffer Length ()
    int  headerC = 0; // Data buffer Length befor compression
    
    boolean  isEditable = true;
    
    private List<Integer>     recordIndex   = new ArrayList<Integer>();
    private List<byte[]>      recordEvents  = new ArrayList<byte[]>();
    private int            compressionType  = 0;
    private HipoRecordHeader   recordHeader = new HipoRecordHeader();
    private Integer            recordBytesWritten = 0;
    /**
     * creates and empty record ready for adding events and removing events.
     */
    public HipoRecord(){
        reset();
        recordHeader.reset();
    }
    
    /**
     * Initializes a record from it's binary form and creates arrays as events
     * that can be accessed through the interface. also contains options flags
     * such as version, type of data stored an compression flag.
     * @param array 
     */
    
    public HipoRecord(byte[] array){
        this.initFromBinary(array);
    }
    /**
     * initializes an empty record. writes the identifying string "RC_G" to
     * the first byte, this makes it easy to search in binary buffer for record
     * start bytes in case there is a corruption in the stream.
     */
    public final void reset(){        
        this.recordIndex.clear();
        this.recordEvents.clear();
        this.recordHeader.reset();
        this.recordBytesWritten = 0;
    }
    /**
     * add an byte array into the record.
     * @param array 
     */
    public void addEvent(byte[] array){
        
        int eventLength = array.length;        
        
        recordEvents.add(array);                
        recordHeader.setNumberOfEvents(recordEvents.size());
        
        recordBytesWritten += eventLength;
      
    }
    
    public int getBytesWritten(){
        return this.recordBytesWritten;
    }
    /**
     * Builds the record into a ByteBuffer includes the Header, Index Array and Event Buffer.
     * If compression type is specified the event buffer will be compressed, and header will
     * contain different sizes for event size compressed and event size uncompressed.
     * NOTE: the record Header is never compressed.
     * @return 
     */
    public ByteBuffer build(){
        
        int totalSize = this.recordHeader.getRecordHeaderLength();        
        int eventsSize = this.getDataBytesSize();
        
        recordHeader.setDataSize(eventsSize);
        
        byte[] eventBytes = this.buildDataBytes();
        byte[] indexBytes = this.buildIndexBytes();
        
        //totalSize += eventsSize;
        totalSize += indexBytes.length;
        totalSize += eventBytes.length;
        
        recordHeader.setRecordSize(totalSize);
        recordHeader.setDataSizeCompressed(eventBytes.length);
        recordHeader.setCompressionType(this.compressionType);
        recordHeader.setIndexArraySize(indexBytes.length);
        
        byte[]  buffer = new byte[totalSize];
        
        System.arraycopy(recordHeader.getRecordHeaderData(), 0, buffer, 0, recordHeader.getRecordHeaderLength());
        System.arraycopy(indexBytes, 0, buffer, 
                recordHeader.getRecordHeaderLength() + recordHeader.getHeaderSize(), indexBytes.length);
        
        int position = recordHeader.getRecordHeaderLength() + recordHeader.getHeaderSize() + indexBytes.length;
        
        System.arraycopy(eventBytes, 0, buffer, position, eventBytes.length);
        /*
        for(int i = 0; i < this.recordEvents.size(); i++){
            byte[] event = recordEvents.get(i);
            System.arraycopy(event, 0, buffer, position, event.length);
            position += event.length;
        }*/
        
        ByteBuffer nioBuffer = ByteBuffer.wrap(buffer);        
        nioBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return nioBuffer;
    }
        
    /**
     * sets the compression type for the record.
     * 0 - no compression
     * 1 - GZIP compression
     * 2 - LZ4 compression
     * @param type 
     */
    public void setCompressionType(int type){
        this.compressionType = type;
        if(this.compressionType==0){
            this.recordHeader.setCompressionType(0);
            return;
        }
        
        if(this.compressionType>0&&this.compressionType<4){
            this.recordHeader.setCompressionType(type);
        } else {
            System.out.println("[HipoRecord::compression] -----> unknown "
            + " compression type " + type + 
                    " use 1 - for GZIP and 2 - for LZ4.");
            this.compressionType = 0;
            this.recordHeader.setCompressionType(0);
        }
    }
    
    private void initFromBinary(byte[] binary){

        reset();
        
        byte[] header = new byte[HipoRecordHeader.RECORD_HEADER_SIZE];
        System.arraycopy(binary, 0, header, 0, HipoRecordHeader.RECORD_HEADER_SIZE);
        
        recordHeader.initBinary(header);
        
        //System.out.println(recordHeader.toString());
        

        if(recordHeader.isValid()==false){
            System.out.println("[HipoRecord::initBinary] ---> error : something went wrong with a record.");
            return;
        }
        
        int indexPosition  = recordHeader.getRecordHeaderLength() + recordHeader.getHeaderSize();
        int     indexSize  = recordHeader.getIndexArraySize();
        byte[]  indexBytes = new byte[indexSize];
        System.arraycopy(binary, indexPosition, indexBytes, 0, indexBytes.length);
        ByteBuffer indexBuffer = ByteBuffer.wrap(indexBytes);
        indexBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        System.out.println(" INDEX SIZE = " + indexSize);
        List<Integer>  indexList = new ArrayList<Integer>();
        for(int i = 0; i < indexSize; i+=4){
            Integer index = indexBuffer.getInt(i);
            //System.out.println("index " + i + "  size = " + index);
            indexList.add(index);
        }
        
        int    dataPosition = indexPosition + indexSize;
        int  compressedSize = recordHeader.getDataSizeCompressed();
        int            type = recordHeader.getCompressionType();
        
        compressionType = type;
        
        byte[]    dataBytesCompressed = new byte[compressedSize];
        System.arraycopy(binary, dataPosition, dataBytesCompressed, 0, dataBytesCompressed.length);
        byte[]    dataBytes = null;
        
        if(type==0){
            dataBytes = dataBytesCompressed;
        } else if(type==1) {
            dataBytes = HipoByteUtils.ungzip(dataBytesCompressed);
        } else if(type==2||type==3) {
            int uncompressedSize = recordHeader.getDataSize();
            dataBytes = new byte[uncompressedSize];
            HipoByteUtils.uncompressLZ4(dataBytesCompressed, dataBytes);
        }
                
        int eventPosition = 0;
        
        for(int i = 0; i < indexList.size(); i++){
            byte[] event = new byte[indexList.get(i)];
            System.arraycopy(dataBytes, eventPosition, event, 0, event.length);
            this.addEvent(event);
            eventPosition += indexList.get(i);
        }
        /*
        int lastLength = dataBytes.length - eventPosition;
        byte[]   event = new byte[lastLength];
        System.arraycopy(dataBytes, eventPosition, event, 0, event.length);
        this.addEvent(event);*/
        //int   indexLength = recordHeader.
        
        /*
        ByteBuffer  buffer = ByteBuffer.wrap(binary);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        this.headerL = buffer.getInt(0);
        this.headerM = buffer.getInt(4);
        this.headerH = buffer.getInt(8);
        this.headerC = buffer.getInt(12);
        
        //System.out.println(" " + this.headerL + " " + this.headerM
        //+ " " + this.headerH + " " + this.headerC);
        
        int isCompressed = HipoByteUtils.read(headerM, 
                HipoHeader.LOWBYTE_RECORD_COMPRESSION,
                HipoHeader.HIGHBYTE_RECORD_COMPRESSION);
        int compressionType = HipoByteUtils.read(headerM, 
                HipoHeader.LOWBYTE_RECORD_COMPRESSION_TYPE,
                HipoHeader.HIGHBYTE_RECORD_COMPRESSION_TYPE);
        
        //System.out.println("compressed = " + isCompressed + "  type = " + compressionType);
        
        int indexCount = HipoByteUtils.read(headerM, 
                HipoHeader.LOWBYTE_RECORD_EVENTCOUNT,
                HipoHeader.HIGHBYTE_RECORD_EVENTCOUNT);
        int dataLength = HipoByteUtils.read(headerH,
                HipoHeader.LOWBYTE_RECORD_SIZE,
                HipoHeader.HIGHBYTE_RECORD_SIZE);
        
        int uncompressedLength = HipoByteUtils.read(headerC,
                HipoHeader.LOWBYTE_RECORD_SIZE,
                HipoHeader.HIGHBYTE_RECORD_SIZE);
        //int indexOffset = BioHeaderConstants.RECORD_HEADER_SIZE;
        
        int  position = HipoHeader.RECORD_HEADER_SIZE;
        
        this.index.clear();
        this.events.clear();
        
        for(int i = 0; i < indexCount; i++){
            int nextIndex = buffer.getInt(position);
            this.index.add(nextIndex);
            position +=4;
        }
        
        position =  HipoHeader.RECORD_HEADER_SIZE;
                
        byte[] eventdata = new byte[dataLength];
        System.arraycopy( binary, position + indexCount * 4, eventdata, 0, dataLength);
        */
        /**
         * Check if the buffer was compressed. then uncompress the data array.
         * and retrieve byte arrays from indecies. 
         */
        /*
        if(isCompressed==1){
            
            if(compressionType==2){
                byte[] gunzipped = new byte[uncompressedLength];
                HipoByteUtils.uncompressLZ4(eventdata, gunzipped);
                eventdata = gunzipped;
            } else {
                byte[] gunzipped = HipoByteUtils.ungzip(eventdata);
                eventdata = gunzipped;
            }
        }
        
        int totalDataLength = eventdata.length;
        int datapos         = 0;
        
        for(int i = 0 ; i < indexCount; i++){
            int end = 0;
            
            if(i!=(indexCount-1)){
                end = this.index.get(i+1);
            } else {
                end = totalDataLength;
            }
            
            int size = end - this.index.get(i);
            //System.out.println( i + " size = " + size);
            byte[] event = new byte[size];
            System.arraycopy(eventdata, datapos, event, 0, event.length);
            this.events.add(event);
            datapos += size;
        }*/
    }

    /**
     * returns the total size of the all events combined
     * @return 
     */
    public int    getDataBytesSize(){
        int size = 0;
        for(byte[] array : recordEvents) size += array.length;
        return size;
    }
    /**
     * returns a byte[] array with index for each event.
     * @return 
     */
    public byte[] buildIndexBytes(){
        int indexSize = 4*recordEvents.size();
        byte[] indexBytes = new byte[indexSize];
        ByteBuffer buffer = ByteBuffer.wrap(indexBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for(int i = 0; i < recordEvents.size(); i++){
            buffer.putInt(i*4, recordEvents.get(i).length);
        }
        return buffer.array();
    }
    /**
     * returns one byte[] containing all the events chained together
     * @return 
     */
    public byte[] buildDataBytes(){
        
       int size = this.getDataBytesSize();
       byte[]  dataBytes = new byte[size];
       int position = 0;
       for(int i = 0; i < recordEvents.size(); i++){
           int len = recordEvents.get(i).length;
           System.arraycopy(recordEvents.get(i), 0, dataBytes, position, len);
           position += len;
       }
       
       if(this.compressionType>0){
           
           if(this.compressionType==1){
               byte[]  dataBytesCompressed = HipoByteUtils.gzip(dataBytes);
               return dataBytesCompressed;
           }
           
           if(this.compressionType==2){
               byte[]  dataBytesCompressed = HipoByteUtils.compressLZ4(dataBytes);
               return dataBytesCompressed;
           }
           
           if(this.compressionType==3){
               byte[]  dataBytesCompressed = HipoByteUtils.compressLZ4max(dataBytes);
               return dataBytesCompressed;
           }
       }       
       return dataBytes;
    }
    /**
     * returns number of events contained in the record
     * @return 
     */
    public int getEventCount(){
        return recordEvents.size();
    }
    
    public byte[] getEvent(int index){
        return this.recordEvents.get(index);
    }
    /**
     * prints on the screen information about record.
     */
    public void show(){        
        System.out.println(this.recordHeader.toString());
        for(int i = 0; i < recordEvents.size(); i++){
            System.out.println(String.format("\t %3d : SIZE = %8d", i,recordEvents.get(i).length));
        }
    }
    
    /**
     * returns the value of the compression flag bit
     * @return 0 if compression flag is not set, 1 - if set
     */
    public boolean  compressed(){       
        return (this.recordHeader.getCompressionType()>0);
    }
    
    public static void main(String[] args){
        
        HipoRecord  record = new HipoRecord();        
        //record.show();
        record.addEvent(HipoByteUtils.generateByteArray(2250));
        record.addEvent(HipoByteUtils.generateByteArray(5580));
        record.addEvent(HipoByteUtils.generateByteArray(4580));
        record.addEvent(HipoByteUtils.generateByteArray(7962));
        record.setCompressionType(2);
        

        ByteBuffer  buffer = record.build();
        //System.out.println("BUFFER SIZE = " + buffer.array().length);
        record.show();
        
        HipoRecord  restored = new HipoRecord(buffer.array());
        restored.show();
        //record.compressed(true);
        /*byte[]  record_bytes_u = record.getByteBuffer().array();
        
        System.out.println(" DATA RECORD LENGTH = " + record_bytes_u.length);
        
        HipoRecord  restored = new HipoRecord(record_bytes_u);
        */
        //byte[]  record_bytes_c = record.getByteBuffer(true,1).array();        
        //record.show();        
        //System.out.println(" SIZE = " + record_bytes_u.length + "  " +
        //        record_bytes_c.length);
                
        //System.out.println("----------->   Record uncompressed");
        //HipoRecord  rru = new HipoRecord(record_bytes_u);
        //rru.show();
        //System.out.println("----------->   Record  compressed");
        //HipoRecord  rrc = new HipoRecord(record_bytes_c);
        //rrc.show();
        
        //ByteBuffer  rb = record.getByteBuffer(true,0);
        //System.out.println(" LEN = " + rb.getInt(8) + "  UNC = " + rb.getInt(12) );
        /*
        record.addEvent(new byte[]{1,2,3,4,5});
        record.addEvent(new byte[]{11,12,13,14,15,16,17,18,19});
        record.addEvent(new byte[]{21,22,23,24,25});
        record.show();
        ByteBuffer  buffer = record.getByteBuffer();
        System.out.println("LENGTH = " + buffer.capacity());
        
        BioRecord  rec = new BioRecord(buffer.array());
        rec.show();*/
    }
}

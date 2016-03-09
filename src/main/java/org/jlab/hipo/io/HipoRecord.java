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
    
    List<Integer>  index  = new ArrayList<Integer>();
    List<byte[]>   events = new ArrayList<byte[]>();
    int            compressionType = 0;
    
    /**
     * creates and empty record ready for adding events and removing events.
     */
    public HipoRecord(){
        reset();
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
        
        this.index.clear();
        this.events.clear();

        //byte[]  recs = HipoHeader.//new byte[]{'R','C','_','G'};

        this.headerL = HipoHeader.RECORD_ID_STRING;//HipoHeader.getStringInt(recs);
        //headerL = headerL|(recs[0]);
        //headerL = headerL|(recs[1]<<8);
        //headerL = headerL|(recs[2]<<16);
        //headerL = headerL|(recs[3]<<24);
        this.headerM = 0;
        this.headerH = 0;
        this.headerC = 0;
    }
    /**
     * add an byte array into the record.
     * @param array 
     */
    public void addEvent(byte[] array){
        
        int length = array.length;
        //System.out.println(BioByteUtils.getByteString(headerH));
        
        int previousLength = HipoByteUtils.read(headerH, 
                HipoHeader.LOWBYTE_RECORD_SIZE ,
                HipoHeader.HIGHBYTE_RECORD_SIZE);
        
        int previousCount  = HipoByteUtils.read(headerM, 
                HipoHeader.LOWBYTE_RECORD_EVENTCOUNT,
                HipoHeader.HIGHBYTE_RECORD_EVENTCOUNT);
        
        
        //System.out.println("PREVIOUS LENGTH =  " + previousLength);
        events.add(array);
        index.add(previousLength);
        previousCount++;
        
        headerH = HipoByteUtils.write(headerH, previousLength+length,
                HipoHeader.LOWBYTE_RECORD_SIZE ,
                HipoHeader.HIGHBYTE_RECORD_SIZE);
        
        headerM = HipoByteUtils.write(headerM, previousCount,
                HipoHeader.LOWBYTE_RECORD_EVENTCOUNT,
                HipoHeader.HIGHBYTE_RECORD_EVENTCOUNT);
        
        headerC = HipoByteUtils.write(headerC, previousLength+length,
                HipoHeader.LOWBYTE_RECORD_SIZE ,
                HipoHeader.HIGHBYTE_RECORD_SIZE);
        //System.out.println(BioByteUtils.getByteString(headerH));
    }
    
    public ByteBuffer  getByteBuffer(boolean compressed, int compressionType){
        
        if(compressed == false) return this.getByteBuffer();
        
        byte[]  dataBytes = this.getDataBytes();
        //byte[]  dataBytesCompressed = HipoByteUtils.gzip(dataBytes);
        byte[]  dataBytesCompressed = HipoByteUtils.compressLZ4(dataBytes);
        
        int  size = HipoHeader.RECORD_HEADER_SIZE + 
                this.index.size()*4 + 
                dataBytesCompressed.length;
        
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        int ihH = HipoByteUtils.write(0, dataBytesCompressed.length, 
                HipoHeader.LOWBYTE_RECORD_SIZE, 
                HipoHeader.HIGHBYTE_RECORD_SIZE
                );
        
        int ihM = HipoByteUtils.write(0, this.index.size(),
                HipoHeader.LOWBYTE_RECORD_EVENTCOUNT, 
                HipoHeader.HIGHBYTE_RECORD_EVENTCOUNT
                );
        
        int ihC = HipoByteUtils.write(0, headerC, 
                HipoHeader.LOWBYTE_RECORD_SIZE, 
                HipoHeader.HIGHBYTE_RECORD_SIZE
                );
        
        ihM = HipoByteUtils.write(ihM, 1, 24, 24);
        
        buffer.putInt( 0, this.headerL);
        buffer.putInt( 4, ihM);
        buffer.putInt( 8, ihH);
        buffer.putInt(12, ihC);

        int initPos = HipoHeader.RECORD_HEADER_SIZE;
        
        for(int i = 0; i < this.index.size(); i++){
            buffer.putInt(initPos, this.index.get(i));
            initPos += 4;
        }
        
        buffer.position(initPos);
        buffer.put(dataBytesCompressed);
        //System.out.println(" POSITION = " + initPos);

        return buffer;
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
            this.compressed(false);
            return;
        }
        if(this.compressionType>0&&this.compressionType<3){
            this.compressed(true);
            headerM = HipoByteUtils.write(headerM, compressionType, 
                    HipoHeader.LOWBYTE_RECORD_COMPRESSION_TYPE,
                    HipoHeader.HIGHBYTE_RECORD_COMPRESSION_TYPE);
        } else {
            System.out.println("[HipoRecord::compression] -----> unknown "
            + " compression type " + type + 
                    " use 1 - for GZIP and 2 - for LZ4.");
            this.compressionType = 0;
        }
    }
    
    private void initFromBinary(byte[] binary){
                
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
        /**
         * Check if the buffer was compressed. then uncompress the data array.
         * and retrieve byte arrays from indecies. 
         */
        if(isCompressed==1){
            /*
            byte[]  gunzipped = HipoByteUtils.ungzip(eventdata);
            if(gunzipped.length==0){
                System.out.println("[BioRecord] ---> error : something went wrong with unzip.");
                this.reset();
                return;
            }*/
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
        }
    }
    /**
     * Returns record as a byte array. depending on the setting of
     * setCompressionType the buffer will be uncompressed, GZIP or LZ4.
     * @return 
     */
    public ByteBuffer getByteBuffer(){
        
        byte[]  dataBytes = this.getDataBytes();
        
        int compressionByte = HipoByteUtils.read(headerM,
                HipoHeader.LOWBYTE_RECORD_COMPRESSION,
                HipoHeader.HIGHBYTE_RECORD_COMPRESSION);
        
        int compressionType = HipoByteUtils.read(headerM,
                HipoHeader.LOWBYTE_RECORD_COMPRESSION_TYPE,
                HipoHeader.HIGHBYTE_RECORD_COMPRESSION_TYPE);
        
        int ihM = HipoByteUtils.write(this.headerM, this.index.size(),
                HipoHeader.LOWBYTE_RECORD_EVENTCOUNT, 
                HipoHeader.HIGHBYTE_RECORD_EVENTCOUNT
                );
        
        int ihC = HipoByteUtils.write(0, dataBytes.length,
                HipoHeader.LOWBYTE_RECORD_SIZE, 
                HipoHeader.HIGHBYTE_RECORD_SIZE
                );
        
        int ihH = HipoByteUtils.write(0, dataBytes.length,
                HipoHeader.LOWBYTE_RECORD_SIZE, 
                HipoHeader.HIGHBYTE_RECORD_SIZE
                );
        
        if(compressionByte>0){
            if(compressionType==1){
                byte[] compressed = HipoByteUtils.gzip(dataBytes);
                dataBytes = compressed;
                ihH = HipoByteUtils.write(0, dataBytes.length,
                        HipoHeader.LOWBYTE_RECORD_SIZE, 
                        HipoHeader.HIGHBYTE_RECORD_SIZE
                );
            } else {
                byte[] compressed = HipoByteUtils.compressLZ4(dataBytes);
                dataBytes = compressed;
                ihH = HipoByteUtils.write(0, dataBytes.length,
                        HipoHeader.LOWBYTE_RECORD_SIZE, 
                        HipoHeader.HIGHBYTE_RECORD_SIZE
                );
            }
        }
        
        int  size = HipoHeader.RECORD_HEADER_SIZE + 
                this.index.size()*4 + 
                dataBytes.length;
        
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt( 0, this.headerL);
        buffer.putInt( 4, ihM);
        buffer.putInt( 8, ihH);
        buffer.putInt(12, ihC);

        int initPos = HipoHeader.RECORD_HEADER_SIZE;
        
        for(int i = 0; i < this.index.size(); i++){
            buffer.putInt(initPos, this.index.get(i));
            initPos += 4;
        }
        
        buffer.position(initPos);
        buffer.put(dataBytes);
        return buffer;
    }
    /**
     * return ByteBuffer representation of the Record
     * @return 
     */
    /*
    public ByteBuffer getByteBuffer(){
        
        byte[]  dataBytes = this.getDataBytes();
        
        int  size = HipoHeader.RECORD_HEADER_SIZE + this.index.size()*4 + dataBytes.length;
        
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        //System.out.println("WR L = " + BioByteUtils.getByteString(headerL));
        //System.out.println("WR H = " + BioByteUtils.getByteString(headerH));
        buffer.putInt( 0, this.headerL);
        buffer.putInt( 4, this.headerM);
        buffer.putInt( 8, this.headerH);
        buffer.putInt(12, this.headerC);
        
        int initPos = HipoHeader.RECORD_HEADER_SIZE;
        
        for(int i = 0; i < this.index.size(); i++){
            buffer.putInt(initPos, this.index.get(i));
            initPos += 4;
        }
        
        buffer.position(initPos);
        buffer.put(dataBytes);
        //System.out.println(" POSITION = " + initPos);
        return buffer;
    }*/
    /**
     * returns the total size of the all events combined
     * @return 
     */
    public int    getDataBytesSize(){
        int size = 0;
        for(byte[] array : this.events) size += array.length;
        return size;
    }
    /**
     * returns one byte[] containing all the events chained together
     * @return 
     */
    public byte[] getDataBytes(){
       int size = this.getDataBytesSize();
       byte[]  dataBytes = new byte[size];
       int position = 0;
       for(int i = 0; i < this.events.size(); i++){
           int len = this.events.get(i).length;
           System.arraycopy(this.events.get(i), 0, dataBytes, position, len);
           position += len;
       }
       return dataBytes;
    }
    /**
     * returns number of events contained in the record
     * @return 
     */
    public int getEventCount(){
        return this.events.size();
    }
    /**
     * returns event byte array for given index
     * @param index
     * @return 
     */
    public byte[] getEvent(int index){
        byte[]  eventArray = new byte[this.events.get(index).length];
        System.arraycopy(this.events.get(index), 0, eventArray, 0, eventArray.length);
        return eventArray;
    }
    /**
     * prints on the screen information about record.
     */
    public void show(){
        
        int count = HipoByteUtils.read(headerM, 
                HipoHeader.LOWBYTE_RECORD_EVENTCOUNT,
                HipoHeader.HIGHBYTE_RECORD_EVENTCOUNT
                );
        System.out.println(
                "HL = " + HipoByteUtils.getByteString(headerL) + "\n"
                        + "HM = " + HipoByteUtils.getByteString(headerM) + "\n"
                        + "HH = " + HipoByteUtils.getByteString(headerH)
        );
        System.out.println(String.format("HEADER WORDS = %5d %5d %5d %5d", this.headerL,
                this.headerM, this.headerH,this.headerC));
        System.out.println(String.format(" H L/H %X %X",headerL,headerH));
        System.out.println("RECORD ELEMENTS = " + count + "  LENGTH = " + HipoByteUtils.read(headerH,0,23));
        for(int i = 0 ; i < this.events.size();i++){
            System.out.println(" EVENT " + i + "  LENGTH = " + 
                    this.events.get(i).length + "  OFFSET = " + this.index.get(i));
        }
    }
    
    /**
     * set compression flag for the record
     * @param flag 
     */
    public void     compressed(boolean flag){
        if(flag==true){
            headerM = HipoByteUtils.write(headerM, 1, 
                    HipoHeader.LOWBYTE_RECORD_COMPRESSION,
                    HipoHeader.HIGHBYTE_RECORD_COMPRESSION);
        } else {
            headerM = HipoByteUtils.write(headerM, 0, 
                    HipoHeader.LOWBYTE_RECORD_COMPRESSION,
                    HipoHeader.HIGHBYTE_RECORD_COMPRESSION);
        }
    }
    /**
     * returns the value of the compression flag bit
     * @return 0 if compression flag is not set, 1 - if set
     */
    public boolean  compressed(){
        int ic = HipoByteUtils.read(headerM,
                HipoHeader.LOWBYTE_RECORD_COMPRESSION,
                    HipoHeader.HIGHBYTE_RECORD_COMPRESSION);
        return (ic==1);
    }
    
    public static void main(String[] args){
        
        HipoRecord  record = new HipoRecord();        
        //record.show();
        record.addEvent(HipoByteUtils.generateByteArray(2250));
        record.addEvent(HipoByteUtils.generateByteArray(5580));
        record.addEvent(HipoByteUtils.generateByteArray(4580));
        record.setCompressionType(2);
        //record.compressed(true);
        byte[]  record_bytes_u = record.getByteBuffer().array();
        
        System.out.println(" DATA RECORD LENGTH = " + record_bytes_u.length);
        
        HipoRecord  restored = new HipoRecord(record_bytes_u);
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

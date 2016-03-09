/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gavalian
 */
public class HipoWriter {
    
    /**
     * output stream used for writing binary data to the file.
     */
    FileOutputStream  outStream    = null;
    /**
     * Internal record which is purged to the file based on the
     * maximum record size and/or maximum events desired in the 
     * record.
     */
    HipoRecord         outputRecord = null;
    /**
     * maximum number of bytes allowed in the record. if the newly added
     * event exceeds the maximum size, the record will be written to the file. 
     */
    int               MAX_RECORD_SIZE  = 8*1024*1024;
    /**
     * Maximum number of the events to fit into one record. Once this limit
     * is reached the record will be flushed to the file stream. NOTE: the 
     * writer flushes the record to the file when either condition is met
     * record_size>MAX_RECORD_SIZE or n_records>MAX_RECORD_COUNT
     */
    int               MAX_RECORD_COUNT = 5000;
    
    /**
     * Create a writer object with initialized empty record, but no association
     * with a file. BioWriter.open(filename) - has to be called to be able to
     * write events to the file.
     */
    
    /**
     * parameters to keep track of the data that passes through the writing process.
     */
    long     totalByteWritten       = (long) 0;
    long     totalBytesInRecords    = (long) 0;
    long     numberOfRecords        = (long) 0;
    long     timeSpendOnWriting     = (long) 0;
    long     timeSpendOnCompression = (long) 0;
    int      compressionAlgorithm   = 0;
    
    boolean  streamCompression = false;
    
    public HipoWriter(){
        this.outputRecord = new HipoRecord(); 
    }
    /**
     * creates new Writer with an empty record store and associates with 
     * an external file with given name.
     * @param file 
     */
    public HipoWriter(String file){
        this.outputRecord = new HipoRecord();
        this.open(file);
    }
    /**
     * open a file and empty the record store.
     * @param name file name to write data in
     */
    public final void open(String name){
        this.open(name, new byte[]{'E','M','P','T','Y'});
        /*
        try {
            outStream = new FileOutputStream(new File(name));            
            this.outputRecord.reset();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BioWriter.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
    /**
     * Opens a file, initializes it with a header. and appends provided
     * array to the header. the array content is upto the user, and it 
     * can be accessed in the reader once the file is opened for reading.
     * @param name name of the file to open.
     * @param array array to write as a header.
     */
    public final void open(String name, byte[] array) {
        try {
            outStream = new FileOutputStream(new File(name));
            byte[]  bytes = new byte[HipoHeader.FILE_HEADER_SIZE + array.length];
            System.arraycopy(array, 0, bytes, HipoHeader.FILE_HEADER_SIZE, array.length);
            
            ByteBuffer  buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            buffer.putInt(0, HipoHeader.FILE_ID_STRING);
            buffer.putInt(4, HipoHeader.FILE_VER_STRING);
            int  headerLength = HipoByteUtils.write(0, array.length, 
                    HipoHeader.FILE_HEADER_LENGTH_LB, 
                    HipoHeader.FILE_HEADER_LENGTH_HB
                    );
            buffer.putInt(8, headerLength);
            buffer.putInt(12,23);
            outStream.write(buffer.array());
            this.outputRecord.reset();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HipoWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HipoWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Writes the content of the record into the file and resets the record buffer
     * so new data can be added.
     */
    public void write(){
        //this.outputRecord.show();
        if(this.outputRecord.getEventCount()!=0){
            try {
                long stime_compress = System.currentTimeMillis();
                this.outputRecord.setCompressionType(this.compressionAlgorithm);
                byte[] array = this.outputRecord.getByteBuffer().array();
                long etime_compress = System.currentTimeMillis();
                this.timeSpendOnCompression += (etime_compress-stime_compress);
                //System.out.println("purging record with size = " + array.length );
                long stime_write = System.currentTimeMillis();
                this.outStream.write(array);
                long etime_write = System.currentTimeMillis();
                this.timeSpendOnWriting += (etime_write-stime_write);
                this.numberOfRecords++;
                this.totalByteWritten += array.length;
                this.outputRecord.reset();
            } catch (IOException ex) {
                Logger.getLogger(HipoWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * adds an event to the records, not actually written to the disk at this
     * point. if the record size or record length exceed the default maximum
     * values the record will be written to the disk and the record container
     * will be reset.
     * @param event byte array to add to the events record.
     */
    public void writeEvent(byte[] event){
        this.outputRecord.addEvent(event);
        int size = this.outputRecord.getDataBytesSize();
        if(size>this.MAX_RECORD_SIZE){
            this.write();
            this.outputRecord.reset();
        }
    }
    /**
     * destructor substitute. it has to be called at the end of program
     * to make sure the incomplete record is flushed to the file, and file stream
     * is properly closed.
     */
    public void close(){
        if(this.outputRecord.getEventCount()>0){
            this.write();
        }
        try {
            this.outStream.close();
        } catch (IOException ex) {
            Logger.getLogger(HipoWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(this.getStatusString());
    }
    /**
     * returns a status string containing statistics for all interactions with
     * the writer. number of records written, number of bytes written, and the
     * performance statistics.
     * @return 
     */
    public String getStatusString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("NRECORDS = %12d, ", this.numberOfRecords));
        double mb = ( (double) this.totalByteWritten)/1024/1024;
        str.append(String.format("BYTES = %8.2f Mb, ",mb));
        double wtime = (this.timeSpendOnWriting)/1000.0;
        double ctime = (this.timeSpendOnCompression)/1000.0;
        str.append(String.format("WTIME = %7.3f sec, ",wtime));
        str.append(String.format("CTIME = %7.3f sec, ",ctime));
        return str.toString();
    }
    /**
     * set compression flag for the writer, if set TRUE all records will be compressed
     * before being written into the file.
     * @param flag 
     */
    public void setCompression(boolean flag){
        this.streamCompression = flag;
    }
    /**
     * sets maximum allowed events in the record until it's flushed into
     * the stream.
     * @param maxCount maximum number of records.
     */
    public void setMaxRecordCount(int maxCount){
        this.MAX_RECORD_COUNT = maxCount;
    }
    /**
     * Sets the compression type for the records. 
     * 0 - sets compression flag to false
     * 1 - sets compression algorithm to GZIP
     * 2 - sets compression algorithm to LZ4
     * @param type 
     */
    public void setCompressionType(int type){
        this.compressionAlgorithm = 0;
        if(type>0&&type<3){
            this.compressionAlgorithm = type;            
        }
    }
    /**
     * sets the maximum buffer size for the records. this value is used
     * to check size of the record uncompressed, actual record size written 
     * on the disk will be compressed size.
     * @param maxSize maximum size in bytes for the record
     */
    public void setMaxRecordSize(int maxSize){
        this.MAX_RECORD_SIZE = maxSize;
    }
    /**
     * Main program to run internal tests and validations.
     * @param args 
     */
    public static void main(String[] args){
        HipoWriter writer = new HipoWriter();
        //writer.setCompression(true);
        writer.setCompressionType(2);
        writer.open("testfile.bio");
        writer.setMaxRecordSize(8*1024*1024);
        for(int i = 0; i < 140000; i++){
            byte[] buffer = HipoByteUtils.generateByteArray(45000);
            writer.writeEvent(buffer);
        }
        writer.close();
    }
}

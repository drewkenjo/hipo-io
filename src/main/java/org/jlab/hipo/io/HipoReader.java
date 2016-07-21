/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gavalian
 */
public class HipoReader {
    
    
    FileInputStream  inputStream = null;
    List<Long>    recordList   = new ArrayList<Long>();
    List<Long>    recordLength = new ArrayList<Long>(); 
    
    List<HipoRecordIndex>  inRecords        = new ArrayList<HipoRecordIndex>();
    List<HipoRecordIndex>  corruptedRecords = new ArrayList<HipoRecordIndex>();
    
    int                   debugMode = 10;
    
    
    /**
     * parameters to keep statistics information about the performance
     * of the reader
     */
    
    long  timeSpendOnIndexing   = (long) 0;
    long  timeSpendOnReading    = (long) 0;
    long  timeSpendOnInflating  = (long) 0;
    
    /**
     * Keeping track of the events in the file
     */
    
    private int  readerCurrentRecord       = 0;
    private int  readerCurrentRecordLength = 0;
    private int  readerCurrentEvent        = 0;
    private int  numberOfEventsInFile      = 0;
    
    private HipoRecord readerRecord        = null;
    
    public HipoReader(){
        
    }
    /**
     * Open file for reading. automatically reads file index in of records.
     * the event index is not actually read.
     * @param name file names to read
     */
    public void open(String name){
        if(this.debugMode>0) System.out.println("[bio-reader] ---> openning file : " + name);
        try {
            this.inputStream = new FileInputStream(new File(name));
            this.readRecordIndex(inRecords);
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HipoReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(this.debugMode>0) System.out.println("[bio-reader] ---> recovered records : " + this.inRecords.size());
    }
    /**
     * Reads the index of the records from the file. if problem occurs, the 
     * then the problematic record is written to the list containing corrupt
     * records. This records will contain a string describing the problem.
     * @param index 
     */
    public void readRecordIndex(List<HipoRecordIndex>  index){
        
        byte[]  fileHeader   = new byte[HipoHeader.FILE_HEADER_SIZE];
        
        int     firstRecordOffset = 0;
        int     headerLength      = 0;
        try {
            this.inputStream.read(fileHeader);
            
            ByteBuffer  hb =  ByteBuffer.wrap(fileHeader);
            hb.order(ByteOrder.LITTLE_ENDIAN);
            
            int identifier   = hb.getInt(0);
            int sizeWord     = hb.get(8);
            
            headerLength = HipoByteUtils.read(sizeWord,
                    HipoHeader.FILE_HEADER_LENGTH_LB,
                    HipoHeader.FILE_HEADER_LENGTH_HB);
            
            if(identifier==HipoHeader.FILE_ID_STRING){
                firstRecordOffset = HipoHeader.FILE_HEADER_SIZE + headerLength;
            } else {
                System.out.println("[bio-reader] ---> errors. the provided file is not HIPO format.");
                return;
            }
        } catch (IOException ex) {
            Logger.getLogger(HipoReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(this.debugMode>4){
            System.out.println("[debug][read-index] ---->  first record index = " + firstRecordOffset);
            System.out.println("[debug][read-index] ---->  file header length = " + headerLength);
        }
        // Assumin that we passed successfully the event 
        
        byte[]  recordHeader = new byte[HipoHeader.RECORD_HEADER_SIZE];
        //System.out.println("[bio-reader] --->  reading file index ");
        long stime_indexing = System.currentTimeMillis();
        index.clear();
        try {
            this.inputStream.getChannel().position(firstRecordOffset);    
            
            int nread = 1;
            ByteBuffer ib = null;
            
            while(nread>0){
                nread = this.inputStream.read(recordHeader); 
                if(nread>0){    
                    HipoRecordIndex ri = new HipoRecordIndex(
                            this.inputStream.getChannel().position()-
                                    HipoHeader.RECORD_HEADER_SIZE);
                    
                    ib = ByteBuffer.wrap(recordHeader);
                    ib.order(ByteOrder.LITTLE_ENDIAN);

                    int headerH = ib.getInt(8);
                    int headerM = ib.getInt(4);
                    int headerL = ib.getInt(0);
                                        
                    if(ri.parseHeader(headerL, headerM, headerH)==false) return;
                    index.add(ri);
                    this.inputStream.skip(ri.getLength() + ri.getNumberOfEvents()*4);

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(HipoReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        long etime_indexing = System.currentTimeMillis();
        this.timeSpendOnIndexing = (etime_indexing-stime_indexing);
        this.numberOfEventsInFile = 0;
        for(HipoRecordIndex ri : this.inRecords){
            this.numberOfEventsInFile += ri.getNumberOfEvents();
        }
    }
    
    private int  getRecordByEvent(int event){
        int nevents = 0;
        int icount  = 0;
        for(HipoRecordIndex ri : this.inRecords){
            nevents += ri.getNumberOfEvents();
            if(nevents>event) return icount;
            icount++;
        }
        return -1;
    }
    
    private int  getEventOffsetInRecord(int event){
        int nevents = 0;
        int icount  = 0;
        for(HipoRecordIndex ri : this.inRecords){
            if(nevents + ri.getNumberOfEvents() > event){
                return event - nevents;
            }
            nevents += ri.getNumberOfEvents();            
            //if(nevents>event) return icount;
            icount++;
        }
        return -1;
    }
    
    public byte[] readEvent(int pos){
        int nrecord = getRecordByEvent(pos);
        if(nrecord!=this.readerCurrentRecord){
            this.readerRecord = this.readRecord(nrecord);
            this.readerCurrentRecord = nrecord;
        }
        
        if(this.readerRecord==null){
            this.readerRecord = this.readRecord(nrecord);
            this.readerCurrentRecord = nrecord;
        }
        int nevoffset = this.getEventOffsetInRecord(pos);
        
        byte[]  eventBytes = this.readerRecord.getEvent(nevoffset);
        return eventBytes;
    }
    
    public int  getEventCount(){
        return this.numberOfEventsInFile;
    }
    
    public int  getPosition(){
        return this.readerCurrentEvent;
    }
    
    public void show(){
        for(int i = 0; i < this.inRecords.size();i++){
            System.out.println(String.format(" %5d : %s" ,i, this.inRecords.get(i).toString()));
        }
    }
        
    public void showHeader(){
        HipoRecord header = this.readRecord(0);
        int  nevents = header.getEventCount();
        for(int ev = 0; ev < nevents; ev++){
            byte[] buffer = header.getEvent(ev);
            String descriptor = new String(buffer);
            System.out.println(String.format("%5d : %s", ev,descriptor));
        }
    }
    
    public String getStatusString(){
        StringBuilder str = new StringBuilder();
        double time  = ((double) this.timeSpendOnIndexing)/1000.0;
        double timer = ((double) this.timeSpendOnReading)/1000.0;
        double timei = ((double) this.timeSpendOnInflating)/1000.0;
        str.append(String.format("[BIO-READER]  NRECORDS = %8d, ",this.inRecords.size() ));
        str.append(String.format(" INDEXING = %7.3f sec",time));
        str.append(String.format(" READING = %7.3f sec",timer));
        str.append(String.format(" INFLATING = %7.3f sec",timei));
        return str.toString();
    }
    
    
    public int getRecordCount(){
        return this.inRecords.size();
    }
    
    public HipoRecord  readRecord(int record){
        //System.out.println("[Hipo::readRecord] --->  reading record " + record);

        try {
            //long record_offset = this.recordList.get(record);
            //long record_length = this.recordLength.get(record);            
            long record_offset = this.inRecords.get(record).getPosition();
            long record_length = this.inRecords.get(record).getLength();
            long record_nindex = this.inRecords.get(record).getNumberOfEvents();
            long record_size   = HipoHeader.RECORD_HEADER_SIZE + 
                    record_length + 4*record_nindex;
            
            this.inputStream.getChannel().position(record_offset);
            
            byte[]  buffer = new byte[(int) record_size];
            long stime_reading = System.currentTimeMillis();
            this.inputStream.read(buffer);
            long etime_reading = System.currentTimeMillis();
            this.timeSpendOnReading += (etime_reading-stime_reading);
            
            stime_reading = System.currentTimeMillis();
            HipoRecord newRecord = new HipoRecord(buffer);
            etime_reading = System.currentTimeMillis();
            this.timeSpendOnInflating += (etime_reading-stime_reading);
            
            return newRecord;

        } catch (IOException ex) {
            Logger.getLogger(HipoReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static void main(String[] args){
        HipoReader reader = new HipoReader();
        reader.open("testfile.bio");
        //reader.show();
        reader.showHeader();
        System.out.println(reader.getStatusString());
        int nevt = reader.getEventCount();
        System.out.println("N-EVENTS = " + nevt);
        //reader.readRecordTable();
        
        int nrecords = reader.getRecordCount();
        System.out.println(" RECORDS = " + nrecords);
        for(int loop = 0; loop < nrecords;loop++){
            HipoRecord record = reader.readRecord(loop);
        }
        System.out.println(reader.getStatusString());
        //record.show();
    }
}

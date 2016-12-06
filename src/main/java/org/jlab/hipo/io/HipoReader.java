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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.hipo.data.HipoEvent;
import org.jlab.hipo.schema.SchemaFactory;
import org.jlab.hipo.utils.HipoLogo;

/**
 *
 * @author gavalian
 */
public class HipoReader {
    
    
    FileInputStream  inputStream = null;
    
    List<Long>    recordList   = new ArrayList<Long>();
    List<Long>    recordLength = new ArrayList<Long>(); 
    
    //List<HipoRecordIndex>  inRecords        = new ArrayList<HipoRecordIndex>();
    List<HipoRecordIndex>  corruptedRecords = new ArrayList<HipoRecordIndex>();
    
    int                     debugMode = 10;
    List<HipoRecordHeader>  readerRecords = new ArrayList<HipoRecordHeader>();
    
    private final SchemaFactory  schemaFactory = new SchemaFactory();
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
    
    private HipoRecord headerRecord        = null;
    private HipoRecord readerRecord        = null;
    
    public HipoReader(){
        
    }
    
    public void open(InputStream inStream){
        //this.inputStream = inStream;                
    }
    
    
    private List<HipoRecordHeader>  readRecordIndex(InputStream stream){
        try {
            //stream.reset();
            byte[]  fileHeader   = new byte[HipoFileHeader.FILE_HEADER_LENGTH];
            stream.read(fileHeader);
            HipoFileHeader header = new HipoFileHeader(fileHeader);
            System.out.println(header.toString());
        } catch (IOException ex) {
            Logger.getLogger(HipoReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
            this.readRecordIndex(readerRecords);
            //System.out.println(" HEADER RECORD = " + this.headerRecord.getEventCount());
            this.initSchemaFactory();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HipoReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(this.debugMode>0){
            System.out.println();
            System.out.println("[hipo-reader] ---> recovered records : " + this.readerRecords.size()
                + " # events = " + this.numberOfEventsInFile);
            System.out.println();
        }
        //HipoLogo.showVersion(0);
        //show();
    }
    
    private void initSchemaFactory(){
        if(this.headerRecord!=null){
            for(int i = 0; i < this.headerRecord.getEventCount(); i++){
                byte[] buffer = this.headerRecord.getEvent(i);
                HipoEvent event = new HipoEvent(buffer);
                this.schemaFactory.setFromEvent(event);
            }
            //this.schemaFactory.show();
        }
    }
    
    public SchemaFactory getSchemaFactory(){ return this.schemaFactory;}
    /**
     * Reads the index of the records from the file. if problem occurs, the 
     * then the problematic record is written to the list containing corrupt
     * records. This records will contain a string describing the problem.
     * @param index 
     */
    public void readRecordIndex(List<HipoRecordHeader>  index){
        
        byte[]  fileHeader   = new byte[HipoFileHeader.FILE_HEADER_LENGTH];
        
        long     firstRecordOffset = 0;
        int      headerLength      = 0;
        try {
            this.inputStream.read(fileHeader);
            
            HipoFileHeader header = new HipoFileHeader(fileHeader);
            
            int identifier   = header.getIdentifier();
            int sizeWord     = header.getHeaderSize();
            
            /*headerLength = HipoByteUtils.read(sizeWord,
                    HipoHeader.FILE_HEADER_LENGTH_LB,
                    HipoHeader.FILE_HEADER_LENGTH_HB);
            */
            
            if(identifier==HipoFileHeader.FILE_IDENTIFIER){
                firstRecordOffset = header.getRecordStart();
                headerLength      = header.getHeaderSize();
                 
                if(sizeWord>HipoRecordHeader.RECORD_HEADER_SIZE){
                    byte[] fh = new byte[sizeWord];                    
                    this.inputStream.getChannel().position(HipoFileHeader.FILE_HEADER_LENGTH);
                    this.inputStream.read(fh);
                    this.headerRecord = new HipoRecord(fh);
                }
                System.out.println("[hipo-reader] ---> header record is read successfully : # events = " + headerRecord.getEventCount());
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
        
        
        
        //System.out.println("[bio-reader] --->  reading file index ");
        long stime_indexing = System.currentTimeMillis();
        index.clear();
        try {
        
            this.inputStream.getChannel().position(firstRecordOffset);
            
            int nread = 1;
            ByteBuffer ib = null;
            
            while(nread>0){
                byte[]  recordHeader = new byte[HipoRecordHeader.RECORD_HEADER_SIZE];
                nread = this.inputStream.read(recordHeader); 
                if(nread>0){    
                    HipoRecordHeader record = new HipoRecordHeader(recordHeader);
                    if(record.isValid()==true){
                        record.setPositionInFile(firstRecordOffset);
                        this.readerRecords.add(record);
                        //System.out.println(record.toString());
                        //System.out.println(" found record valid at position " + firstRecordOffset);
                        firstRecordOffset += record.getRecordSize();
                        long skip = record.getRecordSize() - HipoRecordHeader.RECORD_HEADER_SIZE;
                        //System.out.println("Skipping now " + skip);
                        
                        this.inputStream.skip(skip);
                    } else {
                        System.out.println(" invalid record at : " + firstRecordOffset);
                        return;
                    }
                    
                    /*
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
                            */
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(HipoReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        long etime_indexing = System.currentTimeMillis();
        this.timeSpendOnIndexing = (etime_indexing-stime_indexing);
        this.numberOfEventsInFile = 0;
        
        System.out.println("Number of records recovered = " + readerRecords.size());
        for(HipoRecordHeader ri : this.readerRecords){
            System.out.println(ri.toString());
            this.numberOfEventsInFile += ri.getNumberOfEvents();
        }
    }
    
    private int  getRecordByEvent(int event){
        int nevents = 0;
        int icount  = 0;
        for(HipoRecordHeader ri : this.readerRecords){
            nevents += ri.getNumberOfEvents();
            if(nevents>event) return icount;
            icount++;
        }
        return -1;
    }
    
        
    private int  getEventOffsetInRecord(int event){
        int nevents = 0;
        int icount  = 0;
        for(HipoRecordHeader ri : this.readerRecords){
            if(nevents + ri.getNumberOfEvents() > event){
                return event - nevents;
            }
            nevents += ri.getNumberOfEvents();            
            //if(nevents>event) return icount;
            icount++;
        }
        return -1;
    }
    
    public HipoEvent readHipoEvent(int pos){
        byte[] buffer = this.readEvent(pos);
        HipoEvent  event = new HipoEvent(buffer,this.schemaFactory);
        return event;
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
        for(int i = 0; i < this.readerRecords.size();i++){
            System.out.println(String.format(" %5d : %s" ,i, this.readerRecords.get(i).toString()));
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
        str.append(String.format("[BIO-READER]  NRECORDS = %8d, ",this.readerRecords.size() ));
        str.append(String.format(" INDEXING = %7.3f sec",time));
        str.append(String.format(" READING = %7.3f sec",timer));
        str.append(String.format(" INFLATING = %7.3f sec",timei));
        return str.toString();
    }
    
    
    public int getRecordCount(){
        return this.readerRecords.size();
    }
    
    public HipoRecord  getHeaderRecord(){
        return this.headerRecord;
    }
    
    public HipoRecord  readRecord(int record){
        
        //System.out.println("[Hipo::readRecord] --->  reading record " + record);
        
        try {
            //long record_offset = this.recordList.get(record);
            //long record_length = this.recordLength.get(record);            
            long record_offset = this.readerRecords.get(record).getPositionInFile();
            long record_length = this.readerRecords.get(record).getRecordSize();
            long record_nindex = this.readerRecords.get(record).getNumberOfEvents();
            
            /*long record_size   = HipoHeader.RECORD_HEADER_SIZE + 
                    record_length + 4*record_nindex;
            */
            
            //System.out.println("reading record from position = " + record_offset + "  record Length = " + record_length);
            
            this.inputStream.getChannel().position(record_offset);
                       
            
            byte[]  buffer = new byte[(int) record_length];
            long stime_reading = System.currentTimeMillis();
            int bytes_read = this.inputStream.read(buffer);
            //System.out.println(" BYTES being read = " + bytes_read  + String.format("%x %x %x %x", buffer[0],buffer[1],buffer[2],buffer[3]));
            //for(int i = 0; i < 20 ; i++) System.out.print(String.format("  [x%X] ", buffer[i]));
            //System.out.println();
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
        reader.open("testfile.hipo");
        
        reader.show();
        int nrecs = reader.getRecordCount();
        
        for(int i = 0; i < nrecs; i++){
            HipoRecord rec = reader.readRecord(i);
            for(int k = 0; k < rec.getEventCount(); k++){
                byte[] event = rec.getEvent(k);
                //System.out.println("Record " + i + " event " + k +  " event size = " + event.length);
            }
        }
        //reader.show();
        /*
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
                */
    }
}

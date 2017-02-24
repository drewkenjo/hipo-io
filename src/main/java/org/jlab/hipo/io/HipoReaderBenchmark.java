/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.io;

import org.jlab.hipo.data.HipoEvent;

/**
 *
 * @author gavalian
 */
public class HipoReaderBenchmark {
    
    public static void recordReadingBenchmark(String filename){        
        HipoReader reader = new HipoReader();
        Long start_time_open = System.currentTimeMillis();
        reader.open(filename);
        Long   end_time_open = System.currentTimeMillis();
        int nRecords = reader.getRecordCount();
        int nEventsFile = reader.getEventCount();
        System.out.println(" RECORDS = " + nRecords + "  EVENTS = " + nEventsFile);
        int nEventCounter = 0;
        Long start_time_read = System.currentTimeMillis();
        for(int i = 0; i < nRecords; i++){
            HipoRecord record = reader.readRecord(i);
            int nEvents = record.getEventCount();
            
            for(int k = 0; k < nEvents; k++){
                //System.out.println("--readin event " + k + " out of " + nEvents);
                byte[] bytes = record.getEvent(k);
                HipoEvent event = new HipoEvent(bytes);//,reader.getSchemaFactory());
                nEventCounter++;
            }
            
        }
        Long end_time_read = System.currentTimeMillis();
        reader.close();
        
        double time_open = (end_time_open-start_time_open);
        double time_read = (end_time_read-start_time_read);
        double rate_read = ( (double) nEventCounter)/time_read;
        System.out.println(
                String.format(" READER [RECORDS] OPEN TIME %6.3f READ = %6.3f %6.2f %12d",
                        time_open/1000.0,time_read/1000.0, rate_read*1000.0, nEventCounter));
    }
    
    public static void eventReadingBenchmark(String filename){ 
        HipoReader reader = new HipoReader();
        Long start_time_open = System.currentTimeMillis();
        reader.open(filename);
        Long   end_time_open = System.currentTimeMillis();
        int nEvents = reader.getEventCount();
        Long start_time_read = System.currentTimeMillis();
        int nEventCounter = 0;
        for(int i = 0; i < nEvents; i++){
            HipoEvent event = reader.readHipoEvent(i);
            nEventCounter++;
        }
        Long end_time_read = System.currentTimeMillis();
        reader.close();
        
        double time_open = (end_time_open-start_time_open);
        double time_read = (end_time_read-start_time_read);
        double rate_read = ( (double) nEventCounter)/time_read;
        System.out.println(
                String.format(" READER [EVENTS] OPEN TIME %6.3f READ = %6.3f %6.2f",
                        time_open/1000.0,time_read/1000.0, rate_read*1000.0));

    }
    
    
    public static void main(String[] args){
        //String file = args[0];
        String filename = "/Users/gavalian/Work/Software/Release-4a.0/COATJAVA/coatjava/clas12dst_000809.hipo";
        //HipoBenchmark.recordReadingBenchmark(filename);
        HipoReaderBenchmark.eventReadingBenchmark(filename);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.benchmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.hipo.io.HipoReader;
import org.jlab.hipo.io.HipoRecord;
import org.jlab.hipo.io.HipoWriter;

/**
 *
 * @author gavalian
 */
public class HipoBenchmark {
    
    public static void writingBenchmark(String[] args) throws IOException{
        int compression      = 0;
        int filename_offset  = 1;
        int chunksize        = 1*1024*1024;
        
        if(args[1].startsWith("-")==true){
            filename_offset++;
            if(args[1].compareTo("-gzip")==0){
                compression = 1;
            }
            if(args[1].compareTo("-lz4")==0){
                compression = 2;
            }
        }
        
        if(args[2].startsWith("-")==true){
            filename_offset += 2;
            chunksize = Integer.parseInt(args[3]);
        }
        
        String outputFile = args[filename_offset];
        String  inputFile = args[filename_offset+1];
        HipoWriter  writer = new HipoWriter();
        writer.setCompressionType(compression);
        writer.open(outputFile);
        
        System.out.println("input file : " + inputFile);
        System.out.println("output file : " + outputFile);
        try {
            FileInputStream  inputStream = new FileInputStream(new File(inputFile));
            byte[]           readBytes   =  new byte[chunksize];
            int bytesRead = inputStream.read(readBytes);
            if(bytesRead>0) writer.writeEvent(readBytes);
            
            System.out.println("FIRST READ = " + bytesRead + "  chunk size = " + chunksize);
            while(bytesRead>0){
                
                bytesRead = inputStream.read(readBytes);
                
                writer.writeEvent(readBytes);
            }
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HipoBenchmark.class.getName()).log(Level.SEVERE, null, ex);
        }
;
        
    }
    
    public static void readingBenchmark(String[] args){
        String inputFile = args[1];
        HipoReader reader = new HipoReader();
        reader.open(inputFile);
        int nrecords = reader.getRecordCount();
       
        for(int loop = 0; loop < nrecords; loop++){
            HipoRecord record = reader.readRecord(loop);
        }
        System.out.println(reader.getStatusString());
    }
    
    public static void main(String[] args){
        int compression = 0;
        if(args[0].compareTo("read")==0){
            HipoBenchmark.readingBenchmark(args);
        }
        
        if(args[0].compareTo("write")==0){
            
            try {
                HipoBenchmark.writingBenchmark(args);
            } catch (IOException ex) {
                Logger.getLogger(HipoBenchmark.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
    }
}

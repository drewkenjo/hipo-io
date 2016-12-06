/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.tests;

import org.jlab.hipo.data.HipoEvent;
import org.jlab.hipo.data.HipoGroup;
import org.jlab.hipo.io.HipoReader;
import org.jlab.hipo.io.HipoWriter;
import org.jlab.hipo.schema.SchemaFactory;

/**
 *
 * @author gavalian
 */
public class WriterTests {
    
    public static void writeWithDictionary(){
        HipoWriter writer = new HipoWriter();
        writer.setCompressionType(2);
        writer.getSchemaFactory().initFromDirectory("/Users/gavalian/Work/Software/Release-9.0/COATJAVA/coatjava/etc/bankdefs/hipo");
        writer.open("hipo_test_dictionary.hipo");
        for(int i = 0; i < 4600; i++){
            HipoGroup group = writer.getSchemaFactory().getSchema("FTOF::dgtz").createGroup(345);
            HipoEvent event = writer.createEvent();
            event.writeGroup(group);
            writer.writeEvent(event);
        }
        writer.close();
    }
    
    public static void writeNtuple(){
    
        HipoWriter writer = new HipoWriter();
        
        writer.defineSchema("Event", 20, "id/I:px/F:py/F:pz/F");
        
        writer.setCompressionType(2);
        writer.open("hipo_test_ntuple.hipo");
        int nevents = 500;
        
        for(int i = 0; i < nevents; i++){
            HipoGroup bank = writer.getSchemaFactory().getSchema("Event").createGroup(1);
            bank.getNode("id").setInt(0, 211);
            bank.getNode("px").setFloat(0, (float) Math.random());
            bank.getNode("py").setFloat(0, (float) Math.random());
            bank.getNode("pz").setFloat(0, (float) Math.random());
            
            HipoEvent event = writer.createEvent();
            event.writeGroup(bank);
            writer.writeEvent(event);
        }
        writer.close();
    }
    
    public static void readSchemaFactory(){
        SchemaFactory factory = new SchemaFactory();
        factory.initFromDirectory("/Users/gavalian/Work/Software/Release-9.0/COATJAVA/coatjava/etc/bankdefs/hipo");
        factory.show();
        System.out.println("-------> end of schema read test");
    }
    
    public static void main(String[] args){
        
        WriterTests.writeWithDictionary();
        /*
        WriterTests.readSchemaFactory();
        
        WriterTests.writeNtuple();
        
        HipoReader reader = new HipoReader();
        reader.open("hipo_test_ntuple.hipo");
        
        for(int i = 0; i < 5; i++){
            HipoEvent event = reader.readHipoEvent(i);
            event.show();
        }*/
    }
}

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

/**
 *
 * @author gavalian
 */
public class WriterTests {
    
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
    
    
    public static void main(String[] args){
        WriterTests.writeNtuple();
        
        HipoReader reader = new HipoReader();
        reader.open("hipo_test_ntuple.hipo");
        
        for(int i = 0; i < 5; i++){
            HipoEvent event = reader.readHipoEvent(i);
            event.show();
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.tests;

import org.jlab.hipo.data.HipoEvent;
import org.jlab.hipo.io.HipoReader;

/**
 *
 * @author gavalian
 */
public class EventTest {
    public static void main(String[] args){
        
        HipoReader reader = new HipoReader();
        reader.open("/Users/gavalian/Work/Software/Release-4a.0.0/../Release-9.0/COATJAVA/kppData/big.809.0-19.hipo");
        for(int i = 0; i < 10 ; i++){
            HipoEvent event = reader.readHipoEvent(i);
            System.out.println("-------->   EVENT # " + i);
            //event.show();
            event.removeGroup("ECAL::clusters");
            //event.updateNodeIndex();
            //event.show();
            
            //event.show();
        }
    }
}

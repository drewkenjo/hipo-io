/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gavalian
 */
public class ArrayTable {
    
    public static List<String> getStringByteArray(Integer indent, byte[] array){
        //StringBuilder str = new StringBuilder();
        List<String>  list = new ArrayList<String>();
        String indentString = "%" + indent.toString() + "s";
        for(int i = 0; i < array.length;i++){
            
        }
        //return str.toString();
        return list;
    }
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.io;

/**
 *
 * @author gavalian
 */
public class HipoHeader {
    
    /**
     * All constants for record bit definitions
     */
    public static int  RECORD_HEADER_SIZE          = 16;
    public static int  LOWBYTE_RECORD_SIZE         = 0;
    public static int  HIGHBYTE_RECORD_SIZE        = 23;
    public static int  LOWBYTE_RECORD_EVENTCOUNT   = 0;
    public static int  HIGHBYTE_RECORD_EVENTCOUNT  = 23;
    public static int  LOWBYTE_RECORD_COMPRESSION  = 24;
    public static int  HIGHBYTE_RECORD_COMPRESSION = 24;
    
    public static int  LOWBYTE_RECORD_COMPRESSION_TYPE  = 25;
    public static int  HIGHBYTE_RECORD_COMPRESSION_TYPE = 26;
    
    public static int  RECORD_ID_STRING            = HipoHeader.getStringInt(new byte[]{'H','R','E','C'});
    /**
     * Constants describing file  HEADER constants .
     */
    public static int  FILE_ID_STRING              = HipoHeader.getStringInt(new byte[]{'H','I','P','O'});
    public static int  FILE_VER_STRING             = HipoHeader.getStringInt(new byte[]{'V','0','.','1'});
    public static int  FILE_HEADER_SIZE            = 32;
    public static int  FILE_HEADER_LENGTH_LB       = 0;
    public static int  FILE_HEADER_LENGTH_HB       = 23;
    
    public static int  LOWBYTE_RECORD_STRINGID  = 0;
    public static int  HIGHBYTE_RECORD_STRINGID = 0;
    

    
    public static int  getStringInt(byte[] line){
        int result = 0;
        int[] offsets = new int[]{0,8,16,24};
        for(int i = 0; i < line.length; i++){
            result = (result)|(line[i]<<offsets[i]);
        }
        return result;
    }
    
}

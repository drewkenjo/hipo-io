/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.vfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.lang.System.in;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.jlab.hipo.io.HipoReader;

/**
 *
 * @author gavalian
 */
public class HipoServer {
    public HipoServer(){
        
    }
    
    public void open(String server){
        try {
            FileSystemManager fsManager = VFS.getManager();
            FileObject jarFile = fsManager.resolveFile(server);
            System.out.println("FILE ATTACHED = " + jarFile.getName().getURI());
            System.out.println(" STATUS = " + jarFile.isAttached() 
                    + "  READABLE = " + jarFile.isReadable()
            + "  WRITABLE = " + jarFile.isWriteable());
            
            InputStream stream = jarFile.getContent().getInputStream();
            
            System.out.println(" MARKABLE " + stream.markSupported());
            HipoReader reader = new HipoReader();
            reader.readRecordIndex(stream);
            //byte[] buffer = new byte[20];            
            //while( (stream.read(buffer)==20)){
            //    System.out.println(new String(buffer));
            //}
            //BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            /*
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream)); 
            
            System.out.println("READER READY = " + reader.ready());
            String line = null;
             while ((line = reader.readLine()) != null) {
                 System.out.println(line);
             }
             reader.close();*/
            /*
            FileObject[] children = jarFile.getChildren();
            System.out.println( "Children of " + jarFile.getName().getURI());
            for ( int i = 0; i < children.length; i++ )
            {
                System.out.println( children[ i ].getName().getBaseName() );
            }*/
        } catch (FileSystemException ex) {
            Logger.getLogger(HipoServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HipoServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args){
        HipoServer server = new HipoServer();
        //server.open("https://userweb.jlab.org/~gavalian/DataMiningTools/TupleRead.py");
        server.open("https://userweb.jlab.org/~gavalian/data/reco_eklambda_dst.hipo");
        //server.open("sftp://gavalian:S4t:Urn5@ftp.jlab.org/pim_datatables_5D.txt");
        
    }
}

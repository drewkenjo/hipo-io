/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.vfs;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

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
            /*
            FileObject[] children = jarFile.getChildren();
            System.out.println( "Children of " + jarFile.getName().getURI());
            for ( int i = 0; i < children.length; i++ )
            {
                System.out.println( children[ i ].getName().getBaseName() );
            }*/
        } catch (FileSystemException ex) {
            Logger.getLogger(HipoServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args){
        HipoServer server = new HipoServer();
        server.open("http://userweb.jlab.org/~gavalian/DataMiningTools/TupleRead.py");
    }
}

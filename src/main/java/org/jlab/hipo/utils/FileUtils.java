package org.jlab.hipo.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileUtils {
    
    public static List<String>  getFileListInDir(String directory, String ext){
        List<String> files = FileUtils.getFileListInDir(directory);
        List<String> accepted = new ArrayList<String>();
        for(String file : files){
            if(file.endsWith(ext)==true) accepted.add(file);
        }
        return accepted;
    }
    
    public static List<String>  readFile(String filename){
        List<String> items = new ArrayList<String>();
        try {
            File file = new File(filename);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                //System.out.println(scanner.next());
                items.add(scanner.next());
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    public static String getEnvironmentPath(String env, String dir){
        String envDir = System.getenv(env);
        String proDir = System.getProperty(env);
        if(proDir!=null){
            StringBuilder str = new StringBuilder();
            str.append(proDir);
            if(proDir.endsWith("/")==false) str.append("/");
            str.append(dir);
            String fullPath = str.toString();
            File dirFile = new File(fullPath);
            if(dirFile.exists()==false){
                System.out.println("[FileUtils] ---> directory does not exist : " + fullPath);
                return null;
            }
            return str.toString();
        }
        
        if(envDir!=null){
            StringBuilder str = new StringBuilder();
            str.append(envDir);
            if(envDir.endsWith("/")==false) str.append("/");
            str.append(dir);
            String fullPath = str.toString();
            File dirFile = new File(fullPath);
            if(dirFile.exists()==false){
                System.out.println("[FileUtils] ---> directory does not exist : " + fullPath);
                return null;
            }
            return str.toString();
        }
        System.out.println("[FileUtils] ----> error : Environment variable " + env + " is not set");
        return null;
    }
    
    public static List<String>  getFileListInDir(String env, String directory, String ext){
        String envDirectory = FileUtils.getEnvironmentPath(env, directory);
        if(envDirectory==null){
            return new ArrayList<String>();
        }
        return FileUtils.getFileListInDir(envDirectory, ext);
    }
    
   public static List<String>  getFileListInDir(String directory){        
       
       System.out.println(">>> scanning directory : " + directory);
        List<String> fileList = new ArrayList<String>();
        File[] files = new File(directory).listFiles();
        if(files==null){
            System.out.println(">>> scanning directory : directory does not exist");
            return fileList;
        }
        //System.out.println("FILE LIST LENGTH = " + files.length);
        for (File file : files) {
            if (file.isFile()) {
                if(file.getName().startsWith(".")==true||
                        file.getName().endsWith("~")){
                    System.out.println("[FileUtils] ----> skipping file : " + file.getName());
                } else {
                    fileList.add(file.getAbsolutePath());
                }
            }
        }
        return fileList;
    }

    
    
    public static ArrayList<String> filesInFolder(File folder, String ext)
    {
        ArrayList<String> file_list = new ArrayList<String>();
        for (File f : folder.listFiles())
        {
            if (f.isDirectory())
            {
                file_list.addAll(FileUtils.filesInFolder(f, ext));
            }
            else
            {
                String[] file_split = f.getName().split("[.]+");
                String file_ext = file_split[file_split.length - 1];
                if (file_ext.equalsIgnoreCase(ext))
                {
                    file_list.add(f.getAbsolutePath());
                }
            }
        }
        return file_list;
    }
    
    public static ArrayList<String> filesInFolder(File folder, String ext, ArrayList<String> ignore_prefixes)
    {
        ArrayList<String> file_list = new ArrayList<String>();
        for (File f : folder.listFiles())
        {
            if (f.isDirectory())
            {
                file_list.addAll(FileUtils.filesInFolder(f, ext));
            }
            else
            {
                String[] file_split = f.getName().split("[.]+");
                String file_ext = file_split[file_split.length - 1];
                if (file_ext.equalsIgnoreCase(ext))
                {
                	boolean ignore = false;
                	for (String p : ignore_prefixes)
                	{
                		if (f.getName().startsWith(p))
                		{
                			ignore = true;
                			break;
                		}
                	}
                	if (!ignore)
                	{
                		file_list.add(f.getAbsolutePath());
                	}
                }
            }
        }
        return file_list;
    }
}

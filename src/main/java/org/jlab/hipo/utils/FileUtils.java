package org.jlab.hipo.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static List<String>  getFileListInDir(String directory, String ext){
        List<String> files = FileUtils.getFileListInDir(directory);
        List<String> accepted = new ArrayList<String>();
        for(String file : files){
            if(file.endsWith(ext)==true) accepted.add(file);
        }
        return accepted;
    }
   public static List<String>  getFileListInDir(String directory){        

        List<String> fileList = new ArrayList<String>();
        File[] files = new File(directory).listFiles();
        System.out.println("FILE LIST LENGTH = " + files.length);
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

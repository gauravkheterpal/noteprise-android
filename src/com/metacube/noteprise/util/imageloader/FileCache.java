package com.metacube.noteprise.util.imageloader;

import java.io.File;

import android.content.Context;

public class FileCache 
{    
    private File cacheDir;  
    
    public FileCache(Context context)
    {
        //Use External Storage If Avaliable otherwise internal storage.
		/*if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) 
		{
			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "Android/data/com.techmo.sportspass/fileCache/");
		} 
		else 
		{*/
			cacheDir = context.getCacheDir();
		//}
		
		if (!cacheDir.exists()) 
		{
			cacheDir.mkdirs();
		}			
    }
    
    public File getFile(String url)
    {
		// Use ImageURL + hashCode as file name
    	//String name = url.split(".com/")[1].replace("/", "_");
		String name = url.split("/")[url.split("/").length - 1];
		String filename = String.valueOf(url.hashCode() + name);
		File f = new File(cacheDir, filename);
		return f;       
    }
    
    public void clear()
    {
		File[] files = cacheDir.listFiles();
		for (File f : files)
		{
			f.delete();
		}			
    }
}
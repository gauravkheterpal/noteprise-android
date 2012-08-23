package com.metacube.noteprise.util.imageloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

import javax.net.ssl.HttpsURLConnection;

import com.metacube.noteprise.util.NotepriseLogger;
import com.metacube.noteprise.util.Utilities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class ImageLoader 
{    
	MemoryCache memoryCache = new MemoryCache();
	FileCache fileCache;
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	public static int UNCOMPRESSED = 0;
	public static int REQ_SIZE_70 = 70;
	public static int REQ_SIZE_100 = 100;
	public static int REQ_SIZE_150 = 150;
	public static int REQ_SIZE_200 = 200;
	String sfAuthToken = null;
    
    public ImageLoader(Context context, String sfAuthToken)
    {       
        photoLoaderThread.setPriority(Thread.NORM_PRIORITY-1); //Make the background thead low priority. This way it will not affect the UI performance  
		fileCache = new FileCache(context);
		this.sfAuthToken = sfAuthToken;
    }
    
    public void DisplayImage(String path, Activity activity, ImageView imageView, int compress)
    {
        imageViews.put(imageView, path);
        Bitmap bitmap = memoryCache.get(path);
        if(bitmap != null)
        {
        	imageView.setImageBitmap(bitmap);
        }            
        else
        {
            queuePhoto(path, activity, imageView, compress);
            //imageView.setImageResource(stub_id);   // Set a default image for the view.(Not needed)
        }    
    }
        
    private void queuePhoto(String path, Activity activity, ImageView imageView, int compress)
    {
        //This ImageView may be used for other images before. So there may be some old tasks in the queue. We need to discard them. 
        photosQueue.Clean(imageView);
        PhotoToLoad p = new PhotoToLoad(path, imageView, compress);
        synchronized(photosQueue.photosToLoad)
        {
            photosQueue.photosToLoad.push(p);
            photosQueue.photosToLoad.notifyAll();
        }        
        if( photoLoaderThread.getState() == Thread.State.NEW) //start thread if it's not started yet
        {
        	photoLoaderThread.start();
        }            
    }
    
    private Bitmap getBitmap(String path, int compress) 
    {
        File f = fileCache.getFile(path);        
        Bitmap b = decodeFile(f, compress); //from SD cache
        if(b != null) 
        {
        	return b;
        }        
        try 
        { 
        	//from web
            Bitmap bitmap = null;
            if (sfAuthToken != null)
            {
            	path = path + "?oauth_token=" + sfAuthToken;
            }
            URL imageUrl = new URL(path);
			NotepriseLogger.logMessage("Getting image from server for URL=" + imageUrl.toString());
			InputStream is = null;
			if (path.contains("https"))
			{
				HttpsURLConnection conn = (HttpsURLConnection)imageUrl.openConnection();
				conn.setConnectTimeout(30000);
	            conn.setReadTimeout(30000);
	            is = conn.getInputStream();
			}
			else
			{
				 HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
				 conn.setConnectTimeout(30000);
		         conn.setReadTimeout(30000);
		         is = conn.getInputStream();
			}
            OutputStream os = new FileOutputStream(f);
            Utilities.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f, compress);
            return bitmap;
        } 
        catch (Exception ex)
        {
        	NotepriseLogger.logError("Exception in loading image from server.", NotepriseLogger.WARNING, ex);
        	return null;
        }
    }
    
    private Bitmap decodeFile(File f, int compress) //decodes image and scales it to reduce memory consumption
    {
        try 
        { 
        	if(compress == UNCOMPRESSED)
        	{
        		return BitmapFactory.decodeStream(new FileInputStream(f), null, null);
        	}
        	else
        	{
				// decode image size
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(new FileInputStream(f), null, o);
				// Find the correct scale value. It should be the power of 2.
				final int REQUIRED_SIZE = compress;
				int width_tmp = o.outWidth, height_tmp = o.outHeight;
				int scale = 1;
				while (true) 
				{
					if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) 
					{
						break;
					}
					width_tmp /= 2;
					height_tmp /= 2;
					scale *= 2;
				}
				// decode with inSampleSize
				BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = scale;
				return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
			}
        } 
        catch (FileNotFoundException e) 
        {
        	NotepriseLogger.logMessage("FileNotFound in cacheDir in ImageLoader for file=" + f.getName());
        }
        return null;
    }
    
    //Task for the queue
    private class PhotoToLoad
    {
        public String path;
        public ImageView imageView;
        public int compress;
        public PhotoToLoad(String p, ImageView i, int c)
        {
            path = p; 
            imageView = i;
            compress = c;
        }
    }
    
    PhotosQueue photosQueue=new PhotosQueue();
    
    public void stopThread()
    {
        photoLoaderThread.interrupt();
    }
    
    //stores list of photos to download
    class PhotosQueue
    {
        private Stack<PhotoToLoad> photosToLoad=new Stack<PhotoToLoad>();
        
        //removes all instances of this ImageView
        public void Clean(ImageView image)
        {
			for (int j = 0; j < photosToLoad.size();) 
			{
				if (photosToLoad.get(j).imageView == image)
				{
					photosToLoad.remove(j);
				}					
				else
				{
					++j;
				}					
			}
        }
    }
    
    class PhotosLoader extends Thread 
    {
        public void run() 
        {
            try 
            {
                while(true)
                {
                    //thread waits until there are any images to load in the queue
                    if(photosQueue.photosToLoad.size()==0)
                    {
                    	synchronized(photosQueue.photosToLoad)
                    	{
                            photosQueue.photosToLoad.wait();
                        }
                    }                        
                    if(photosQueue.photosToLoad.size() != 0)
                    {
                        PhotoToLoad photoToLoad;
                        synchronized(photosQueue.photosToLoad)
                        {
                            photoToLoad = photosQueue.photosToLoad.pop();
                        }
                        Bitmap bmp = getBitmap(photoToLoad.path, photoToLoad.compress);
                        memoryCache.put(photoToLoad.path, bmp);
                        String tag = imageViews.get(photoToLoad.imageView);
                        if(tag != null && tag.equals(photoToLoad.path))
                        {
                            BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad.imageView);
                            Activity a = (Activity)photoToLoad.imageView.getContext();
                            a.runOnUiThread(bd);
                        }
                    }
                    if(Thread.interrupted())
                    {
                    	break;
                    }                        
                }
            } 
            catch (InterruptedException e) 
            {
                //allow thread to exit
            	NotepriseLogger.logMessage("InterruptedException in ImageLoader queue");
            }
        }
    }
    
    PhotosLoader photoLoaderThread=new PhotosLoader();
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        ImageView imageView;
        public BitmapDisplayer(Bitmap b, ImageView i)
        {
        	bitmap = b;
        	imageView = i;
        }
        
        public void run()
        {
            if(bitmap!=null)
            {
                imageView.setImageBitmap(bitmap);
            }            
            else
            {
            	//imageView.setImageResource(stub_id);
            }                
        }
    }

    public void clearCache() 
    {
        memoryCache.clear();
        fileCache.clear();
    }
}
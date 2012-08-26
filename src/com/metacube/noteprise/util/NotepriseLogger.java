package com.metacube.noteprise.util;

import android.util.Log;

import com.metacube.noteprise.common.Constants;

public class NotepriseLogger 
{
	public static Boolean LOGGING_ENABLED		=	Constants.DEBUGGING_ENABLED;
	public static Boolean PRINT_STACK_TRACE		=	Constants.STACKTRACE_ENABLED;
	
	public static final int VERBOSE	=	0;
	public static final int ERROR	=	1;
	public static final int WARNING	=	2;
	public static final int DEBUG	=	3;
	public static final int INFO	=	4;
	
	public static void logMessage(String message)
	{
		if(LOGGING_ENABLED)
		{
			Log.v(Constants.LOG_TAG, message);
		}		
	}
	
	public static void logError(String message, int logType)
	{
		if (LOGGING_ENABLED) 
		{
			switch (logType) 
			{
				case VERBOSE: 
				{
					Log.v(Constants.LOG_TAG, message);
					break;
				}
				case ERROR: 
				{
					Log.e(Constants.LOG_TAG, message);
					break;
				}
				case WARNING: 
				{
					Log.w(Constants.LOG_TAG, message);
					break;
				}
				case DEBUG: 
				{
					Log.d(Constants.LOG_TAG, message);
					break;
				}
				case INFO:
				{
					Log.i(Constants.LOG_TAG, message);
					break;
				}				
				default:
				{
					Log.v(Constants.LOG_TAG, message);
				}
			}
		}
	}
	
	public static void logError(String message, int logType, Exception exception)
	{
		if (LOGGING_ENABLED) 
		{
			switch (logType) 
			{
				case VERBOSE: 
				{
					Log.v(Constants.LOG_TAG, message);
					break;
				}
				case ERROR: 
				{
					Log.e(Constants.LOG_TAG, message);
					break;
				}
				case WARNING: 
				{
					Log.w(Constants.LOG_TAG, message);
					break;
				}
				case DEBUG: 
				{
					Log.d(Constants.LOG_TAG, message);
					break;
				}
				case INFO:
				{
					Log.i(Constants.LOG_TAG, message);
					break;
				}				
				default:
				{
					Log.v(Constants.LOG_TAG, message);
				}
			}
			if (PRINT_STACK_TRACE) 
			{
				exception.printStackTrace();
			}			
		}
	}
}
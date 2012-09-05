package com.metacube.noteprise.common;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CommonCustomDialog extends DialogFragment implements OnShowListener
{
	public int layoutId;
	public OnClickListener listener;
	public View dialogView;
	
	public CommonCustomDialog(int layoutId, OnClickListener listener) 
	{
		this.layoutId = layoutId;
		this.listener = listener;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setCancelable(Boolean.TRUE);
		setStyle(STYLE_NO_TITLE, getTheme());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(layoutId, container);
		this.dialogView = view;
		return view;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
		
		return super.onCreateDialog(savedInstanceState);
	}
	
	@Override
	public void onCancel(DialogInterface dialog) 
	{
		super.onCancel(dialog);
		
	}

	@Override
	public void onShow(DialogInterface dialog) 
	{		
		
	}
}
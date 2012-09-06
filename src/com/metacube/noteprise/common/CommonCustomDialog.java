package com.metacube.noteprise.common;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint("ValidFragment")
public class CommonCustomDialog extends DialogFragment
{
	public int layoutId;
	public BaseFragment fragment;
	public String customTag = null;
	
	public CommonCustomDialog(int layoutId, BaseFragment fragment) 
	{
		this.layoutId = layoutId;
		this.fragment = fragment;
	}
	
	public CommonCustomDialog(int layoutId, BaseFragment fragment, String customTag) 
	{
		this.layoutId = layoutId;
		this.fragment = fragment;
		this.customTag = customTag;
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
		if (customTag != null)
		{
			view.setTag(customTag);
		}
		else
		{
			view.setTag(layoutId);
		}		
		fragment.instantiateCustomDialog(view);
		return view;
	}
}
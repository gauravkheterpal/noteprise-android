package com.metacube.noteprise.common;

import com.metacube.noteprise.R;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CommonProgressDialog extends DialogFragment 
{
	String title, message;
	TextView titleText, messageText;
	
	public CommonProgressDialog(String dialogTitle, String dialogMessage) 
	{
		this.title = dialogTitle;
		this.message = dialogMessage;
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
		View view = inflater.inflate(R.layout.common_progress_dialog_layout, container);
		titleText = (TextView) view.findViewById(R.id.progress_dialog_title);
		messageText = (TextView) view.findViewById(R.id.progress_dialog_message);
		titleText.setText(title);
		messageText.setText(message);
		return view;
	}
}
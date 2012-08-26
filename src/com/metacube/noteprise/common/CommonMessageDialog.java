package com.metacube.noteprise.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;

import com.evernote.edam.notestore.NoteStore.Client;
import com.metacube.noteprise.R;

public class CommonMessageDialog implements OnClickListener
{
	Builder alertDialogBuilder;
	public AlertDialog messageDialog;
	Context context;
	Resources resources;
	Handler dialogButtonHandler = null;
	
	public CommonMessageDialog(Context context) 
	{
		this.context = context;
		resources = context.getResources();
	}
	
	public Boolean isAlreadyShowing() 
	{
		if (messageDialog != null) 
		{
			if (messageDialog.isShowing()) 
			{
				return true;
			}
		}
		return false;
	}
	
	public void showMessageDialog(String message)
	{
		if (isAlreadyShowing())
		{
			return;
		}
		alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setNeutralButton(resources.getString(R.string.dialog_neutral_button_text), this);
		messageDialog = alertDialogBuilder.create();
		messageDialog.show();
	}
	
	public void showMessageDialog(String message, String buttonText)
	{
		if (isAlreadyShowing())
		{
			return;
		}
		alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setNeutralButton(buttonText, this);
		messageDialog = alertDialogBuilder.create();
		messageDialog.show();
	}
	
	public void showFinishActivityDialog(String message)
	{
		if (isAlreadyShowing())
		{
			return;
		}
		alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setNeutralButton(resources.getString(R.string.dialog_neutral_button_text),
				new OnClickListener() 
			{
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();
					((Activity) context).finish();
				}
			});
		messageDialog = alertDialogBuilder.create();
		messageDialog.show();
	}
	
	@Override
	public void onClick(DialogInterface dialog, int id) 
	{
		dialog.dismiss();
	}
	
	public void showChangeActivityDialog(final Intent intent, String message)
	{
		if (isAlreadyShowing())
		{
			return;
		}
		alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setNeutralButton(resources.getString(R.string.dialog_neutral_button_text), 
				new OnClickListener() 
				{
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						dialog.dismiss();
						context.startActivity(intent);
					}
				});
		messageDialog = alertDialogBuilder.create();
		messageDialog.show();
	}
	
	public void showDeleteNoteDialog(String authToken, Client client, OnClickListener listener)
	{
		if (isAlreadyShowing())
		{
			return;
		}
		alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setMessage(resources.getString(R.string.note_delete_confirm_message));
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setPositiveButton(resources.getString(R.string.dialog_positive_button_text), listener);
		alertDialogBuilder.setNegativeButton(resources.getString(R.string.dialog_negative_button_text), listener);
		messageDialog = alertDialogBuilder.create();
		messageDialog.show();
	}
}
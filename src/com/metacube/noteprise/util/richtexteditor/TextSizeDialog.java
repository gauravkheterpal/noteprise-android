package com.metacube.noteprise.util.richtexteditor;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.view.MotionEvent;
import android.view.View;

public class TextSizeDialog extends Dialog {

    public interface OnSizeChangedListener 
    {
        void sizeChanged(int size);
    }

    private OnSizeChangedListener mListener;

    private static class TextSizeView extends View 
    {
        private Paint mPaint;
        private OnSizeChangedListener mListener;

        TextSizeView(Context c, OnSizeChangedListener l) 
        {
            super(c);
            mListener = l;

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setTypeface(Typeface.DEFAULT_BOLD);
            mPaint.setTextAlign(Align.LEFT);
            mPaint.setColor(Color.WHITE);
        }

        @Override 
        protected void onDraw(Canvas canvas) 
        {      
            mPaint.setTextSize(10);
        	canvas.drawText("A", 10, 30, mPaint);
        	
        	mPaint.setTextSize(15);
        	canvas.drawText("A", 40, 30, mPaint);
        	
        	mPaint.setTextSize(20);
        	canvas.drawText("A", 70, 30, mPaint);
        	
        	mPaint.setTextSize(25);
        	canvas.drawText("A", 100, 30, mPaint);
        	
        	mPaint.setTextSize(30);
        	canvas.drawText("A", 130, 30, mPaint);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
        {
            setMeasuredDimension(160, 40);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) 
        {
        	if (event.getAction() == MotionEvent.ACTION_DOWN)
        	{
	        	int c = 18;
	        	
	        	if(event.getX() >= 10 && event.getX() < 40)
	        		c = 6;
	        	else if(event.getX() >= 40 && event.getX() < 70)
	        		c = 12;
	        	else if(event.getX() >= 70 && event.getX() < 100)
	        		c = 18;
	        	else if(event.getX() >= 100 && event.getX() < 130)
	        		c = 24;
	        	else if(event.getX() >= 130 && event.getX() < 160)
	        		c = 30;
	        	
	            mListener.sizeChanged(c);
        	}

            return true;
        }
    }

    public TextSizeDialog(Context context, OnSizeChangedListener listener) 
    {
        super(context);

        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        OnSizeChangedListener l = new OnSizeChangedListener() {
            public void sizeChanged(int size) {
                mListener.sizeChanged(size);
                dismiss();
            }
        };

        setContentView(new TextSizeView(getContext(), l));
        setTitle("Set text size:");
    }
}

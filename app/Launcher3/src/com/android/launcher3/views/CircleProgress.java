/*
 	author : chenchuanliang@unicair.cn
	data   : 2014.12
	effect : for the design of the status bar
 */

package com.android.launcher3.views;

import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.android.launcher3.R;

public class CircleProgress extends View{

	private static final int DEFAULT_MAX_VALUE = 100;				
	private static final int DEFAULT_PAINT_WIDTH = 3;
	private static final int DEFAULT_PAINT_COLOR = 0xff53b900;
	private static final int DEFAULT_GRAY_COLOR = 0x3febebeb;
	private static final boolean DEFAULT_FILL_MODE = false;
	private static final int DEFAULT_INSIDE_VALUE = 7;
	
	private CircleAttribute mCircleAttribute;
	private int mMaxProgress; 	 																												
	private int mMainCurProgress;
	private int mSubCurProgress;
	private CartoomEngine mCartoomEngine;
	private Drawable mBackgroundPicture;
	 
	public CircleProgress(Context context) {
		super(context);		
		defaultParam();
	}
	
	public CircleProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
   
		defaultParam();
		
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);      
	        
	    mMaxProgress = array.getInteger(R.styleable.CircleProgressBar_max, DEFAULT_MAX_VALUE); 
	    
	    boolean bFill = array.getBoolean(R.styleable.CircleProgressBar_fill, DEFAULT_FILL_MODE);
	    int paintWidth = array.getInt(R.styleable.CircleProgressBar_Paint_Width, DEFAULT_PAINT_WIDTH);    
        mCircleAttribute.setFill(bFill);
        if (bFill == false)
        {
        	mCircleAttribute.setPaintWidth(paintWidth);
        }
        
        int paintColor = array.getColor(R.styleable.CircleProgressBar_Paint_Color, DEFAULT_PAINT_COLOR);

        mCircleAttribute.setPaintColor(paintColor);
        
        
	    mCircleAttribute.mSidePaintInterval = array.getInt(R.styleable.CircleProgressBar_Inside_Interval, DEFAULT_INSIDE_VALUE);
	    array.recycle(); 
	}

	private void defaultParam()
	{
		mCircleAttribute = new CircleAttribute();
		mCartoomEngine = new CartoomEngine();
		mMaxProgress = DEFAULT_MAX_VALUE;								 																												
		mMainCurProgress = 0;						
	    mSubCurProgress = 0;					
	}
	 
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {		
		// TODO Auto-generated method stub
	//	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		
		mBackgroundPicture = getBackground();
		if (mBackgroundPicture != null)
		{
			width = mBackgroundPicture.getMinimumWidth();
			height = mBackgroundPicture.getMinimumHeight();
		}
		setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(width, heightMeasureSpec));
	}
	 
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		mCircleAttribute.autoFix(w, h);
	}

	public void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		//if (mBackgroundPicture == null)								
		//{
			canvas.drawArc(mCircleAttribute.mRoundOval, 0, 360, mCircleAttribute.mBRoundPaintsFill, mCircleAttribute.mBottomPaint);
		//}
		float subRate = (float)mSubCurProgress / mMaxProgress;
		float subSweep = 360 * subRate;
		canvas.drawArc(mCircleAttribute.mRoundOval, mCircleAttribute.mDrawPos, subSweep, mCircleAttribute.mBRoundPaintsFill, mCircleAttribute.mSubPaint);

		float rate = (float)mMainCurProgress / mMaxProgress;
		float sweep = 360 * rate;
		canvas.drawArc(mCircleAttribute.mRoundOval, mCircleAttribute.mDrawPos, sweep, mCircleAttribute.mBRoundPaintsFill, mCircleAttribute.mMainPaints);
	}
	
	 public synchronized void setMainProgress (int progress)
	 {
    	mMainCurProgress = progress;
    	if (mMainCurProgress < 0)
    	{
    		mMainCurProgress = 0;
    	}
    	if (mMainCurProgress > mMaxProgress)
    	{
    		mMainCurProgress = mMaxProgress;
    	}
    	invalidate();
	}
	    
    public synchronized int getMainProgress()
    {
    	return mMainCurProgress;
    }
	   
    public synchronized void setSubProgress (int progress)
    {
    	mSubCurProgress = progress;
    	if (mSubCurProgress < 0)
    	{
    		mSubCurProgress = 0;
    	}
    	if (mSubCurProgress > mMaxProgress)
    	{
    		mSubCurProgress = mMaxProgress;
    	}
    	invalidate();
    }
	    
    public synchronized int getSubProgress()
    {
    	return mSubCurProgress;
    }
    
	public void startCartoom(int time)
	{
		mCartoomEngine.startCartoom(time);
	}

	public void stopCartoom()
	{
		mCartoomEngine.stopCartoom();
	}
	
	class CircleAttribute
	{
		 public RectF mRoundOval;
		 public boolean mBRoundPaintsFill;
		 public int mSidePaintInterval;			
		 public int mPaintWidth;			
		 public int mPaintColor;			
		 public int mDrawPos;	
		 public Paint mMainPaints;    
		 public Paint mSubPaint;  
		 public Paint mBottomPaint;

		 public CircleAttribute()
		 {
			mRoundOval = new RectF();
			mBRoundPaintsFill = DEFAULT_FILL_MODE;
			mSidePaintInterval = DEFAULT_INSIDE_VALUE;
			mPaintWidth = 0;
			mPaintColor = DEFAULT_PAINT_COLOR;
			mDrawPos = -90;
			 
			mMainPaints = new Paint();		
			mMainPaints.setAntiAlias(true);
			mMainPaints.setStyle(Paint.Style.FILL);
			mMainPaints.setStrokeWidth(mPaintWidth);
			mMainPaints.setColor(mPaintColor);
		     
			mSubPaint = new Paint();
			mSubPaint.setAntiAlias(true);
			mSubPaint.setStyle(Paint.Style.FILL);
			mSubPaint.setStrokeWidth(mPaintWidth);
			mSubPaint.setColor(mPaintColor);
				
			mBottomPaint = new Paint();
			mBottomPaint.setAntiAlias(true);
			mBottomPaint.setStyle(Paint.Style.FILL);
			mBottomPaint.setStrokeWidth(mPaintWidth);
			mBottomPaint.setColor(DEFAULT_GRAY_COLOR);
			
		 }
	
		 
		 public void setPaintWidth(int width)
		 {
			 mMainPaints.setStrokeWidth(width);
			 mSubPaint.setStrokeWidth(width);
			 mBottomPaint.setStrokeWidth(width);
		 }
		 
		 public void setPaintColor(int color)
		 {
			 mMainPaints.setColor(color);
		     int color1 = color & 0x00ffffff | 0x66000000;
		     mSubPaint.setColor(color1);	        
		 }
		 
		 public void setFill(boolean fill)
		 {
			 mBRoundPaintsFill = fill;
			 if (fill)
			 {
				 mMainPaints.setStyle(Paint.Style.FILL);
				 mSubPaint.setStyle(Paint.Style.FILL);	 
				 mBottomPaint.setStyle(Paint.Style.FILL);	
			 }else{
				 mMainPaints.setStyle(Paint.Style.STROKE);
				 mSubPaint.setStyle(Paint.Style.STROKE);	
				 mBottomPaint.setStyle(Paint.Style.STROKE);	
			 }	
		 }
		 
		 public void autoFix(int w, int h)
		 {
			if (mSidePaintInterval != 0)
			{
				mRoundOval.set(mPaintWidth/2 + mSidePaintInterval, mPaintWidth/2 + mSidePaintInterval,
				w - mPaintWidth/2 - mSidePaintInterval, h - mPaintWidth/2 - mSidePaintInterval);	
			}else{
				int sl = getPaddingLeft();
				int sr = getPaddingRight();
				int st = getPaddingTop();
				int sb = getPaddingBottom();	
				mRoundOval.set(sl + mPaintWidth/2, st + mPaintWidth/2, w - sr - mPaintWidth/2, h - sb - mPaintWidth/2);	
			}	
		 }
	}
	
	class CartoomEngine
	{
		public Handler mHandler; 
		public boolean mBCartoom;	
		public Timer mTimer;	
		public MyTimerTask mTimerTask;	
		public int mSaveMax;		 
		public int mTimerInterval;	
		public float mCurFloatProcess;	
		private long timeMil;
		
		public CartoomEngine()
		{
			mHandler = new Handler()
			{
				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					switch(msg.what)
					{
						case TIMER_ID:
						{
							if (mBCartoom == false)
							{
								return ;
							}
							
							mCurFloatProcess += 1;
							setMainProgress((int) mCurFloatProcess);
							
							long curtimeMil = System.currentTimeMillis();
									
							timeMil = curtimeMil;
							
							if (mCurFloatProcess >= mMaxProgress)
							{
								stopCartoom();
							}
						}
						break;
					}
				}
			};
			mBCartoom = false;
			mTimer = new Timer();
			mSaveMax = 0;
			mTimerInterval = 50;
			mCurFloatProcess = 0;
		}
		
		public synchronized void startCartoom(int time)
		{
			if (time <= 0 || mBCartoom == true)
			{
				return ;
			}
			
			timeMil = 0;
			mBCartoom = true;

			setMainProgress(0);
			setSubProgress(0);
			
			mSaveMax = mMaxProgress;
			mMaxProgress = (1000 / mTimerInterval) * time;
			mCurFloatProcess = 0;
		
			mTimerTask = new MyTimerTask();
			mTimer.schedule(mTimerTask, mTimerInterval, mTimerInterval);
		}
		
		public synchronized void stopCartoom()
		{
			if (mBCartoom == false)
			{
				return ;
			}
			
			mBCartoom = false;
			mMaxProgress = mSaveMax;
			
			setMainProgress(0);
			setSubProgress(0);
			
			if (mTimerTask != null)
			{
				mTimerTask.cancel();
				mTimerTask = null;
			}
		}
		
		private final static int TIMER_ID = 0x0010;
		
		class MyTimerTask extends TimerTask{

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = mHandler.obtainMessage(TIMER_ID);
				msg.sendToTarget();
			}
		}
	}
}

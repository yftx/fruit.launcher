package com.fruit.launcher;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Scroller;

public class ItemAnimate {

	private static final String TAG = "ItemAnimate";

	private Context mContext;
	private float mFromXValue = 0.0f;
    private float mToXValue = 0.0f;
    private float mFromYValue = 0.0f;
    private float mToYValue = 0.0f;

    private int mDuration;
    private ScrollRunnable mScrollRunnable ;
    private View mChild;
    private int mWidth;
    private int mHeight;
    public  boolean animateEnd = true;

    public ItemAnimate(Context context) {
    	mContext = context;
    	mScrollRunnable = new ScrollRunnable();
    }

	public ItemAnimate(float fromXValue, float toXValue, float fromYValue, float toYValue, View child) {
        mFromXValue = fromXValue;
        mToXValue = toXValue;
        mFromYValue = fromYValue;
        mToYValue = toYValue;
        mChild = child;
        mContext = child.getContext();
        mScrollRunnable = new ScrollRunnable();
    }

	public void setAnimateTarget(float fromXValue, float toXValue, float fromYValue, float toYValue, View child) {
		mFromXValue = fromXValue;
        mToXValue = toXValue;
        mFromYValue = fromYValue;
        mToYValue = toYValue;
        mChild = child;
	}

    public void setDuration(int durationMillis) {
        if (durationMillis < 0) {
            throw new IllegalArgumentException("Animation duration cannot be negative");
        }
        mDuration = durationMillis;
    }

    public void setSquare(int width, int height) {
         mWidth = width;
         mHeight = height;
    }

    public void start() {
    	animateEnd = false;
    	Log.d(TAG, "start " + "animateEnd= " + animateEnd +" mFromXValue= " + mFromXValue + " mToXValue= " + mToXValue);
    	mScrollRunnable.startMove((int) Math.abs(mFromXValue - mToXValue),
    			            (int) Math.abs(mFromYValue - mToYValue),mDuration);
    }

    public void stop() {
    	animateEnd = true;
    	Log.d(TAG, "stop " + "animateEnd=" + animateEnd);
    	if (mChild != null) {
    		mChild.removeCallbacks(mScrollRunnable);
    	}
    }

    private class ScrollRunnable implements Runnable {

    	private static final String TAG = "ScrollRunnable";

        private Scroller mScroller;

        int xDirect;
        int yDirect;

        public ScrollRunnable() {
            mScroller = new Scroller(mContext, null);
        }

        public void startMove(int xDistance, int yDistance, int duration) {
        	Log.d(TAG,"startMove,xDistance="+xDistance+",yDistance="+yDistance+",duration="+duration);
        	
            mScroller.startScroll(0, 0, xDistance, yDistance, duration);

            if (mToXValue == mFromXValue) {
            	xDirect = 0;
            } else {
            	xDirect = mToXValue > mFromXValue ? 1 : -1;	
            }

            if (mToYValue == mFromYValue) {
            	yDirect = 0;
            } else {
            	yDirect = mToYValue > mFromYValue ? 1 : -1;
            }

            Log.d(TAG,"startMove,xDirect="+xDirect+",yDirect="+yDirect);
            
            mChild.post(this);
        }

		@Override
		public void run() {  	
			boolean more = mScroller.computeScrollOffset();
			
			Log.d(TAG,"run,more="+more);

			if (more) {
				final int x = mScroller.getCurrX();
				final int y = mScroller.getCurrY();
				int left = (int) (mFromXValue + x * xDirect);
				int top  = (int) (mFromYValue + y * yDirect);
				Log.d(TAG, "run left= " + left + " top= " + top + " mWidth= " + mWidth + " mHeight= "+mHeight);
				mChild.layout(left, top, left + mWidth, top + mHeight);	
				mChild.post(this);
			} else {
				mChild.removeCallbacks(this);
				animateEnd = true;
			}
        }
    }
}
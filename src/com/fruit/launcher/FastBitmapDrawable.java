/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fruit.launcher;

import android.graphics.drawable.Drawable;
import android.graphics.PixelFormat;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;

class FastBitmapDrawable extends Drawable {
    private Bitmap mBitmap;
    private int mWidth;
    private int mHeight;

    FastBitmapDrawable(Bitmap b) {
    	if (b == null) {
    		mWidth = mHeight = 0;
    		mBitmap = null;
    	} else {
	    	//if (b.getWidth() == 96 && b.getHeight() == 96) {
	    	//	mBitmap = b;
				//mBitmap = Utilities.createCompoundBitmap(IconCache.getAppBgIcon(), b);//b;
	    	//} else {
//				final ThemeManager mThemeMgr;
//				mThemeMgr = ThemeManager.getInstance();
//		        //mBitmap = Utilities.createCompoundBitmap(mThemeMgr.getAppBgIcon(Integer.toString(b.hashCode())), b);//b;
//
//				float sxo = 1.0f;
//            	float syo = 1.0f;
//            	Bitmap oldIcon = b;//entry.icon;
//            	if (oldIcon != null) {
//            		if (oldIcon.getWidth() > 85) 
//            			sxo = 1.0f / (oldIcon.getWidth()/85.0f); //0.86f;
//            		
//            		if (oldIcon.getHeight() > 85)
//            			syo = 1.0f / (oldIcon.getWidth()/85.0f);//0.86f;
//            	}    	
//        		Bitmap theIcon = Utilities.scaleBitmap(oldIcon, sxo, syo);
				
		        //mBitmap = Utilities.createCompoundBitmap(mThemeMgr.getRandomAppBgIcon(), theIcon);//b;
	    		mBitmap = b;//Utilities.createBitmap4Launcher(b);//Utilities.changeBitmap4Launcher(b);
	    	//}
			
	        mWidth = mBitmap.getWidth();
	        mHeight = mBitmap.getHeight();

        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0.0f, 0.0f, null);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }

    @Override
    public int getMinimumWidth() {
        return mWidth;
    }

    @Override
    public int getMinimumHeight() {
        return mHeight;
    }

    public void setBitmap(Bitmap b) {
        mBitmap = b;
        if (b != null) {
            mWidth = mBitmap.getWidth();
            mHeight = mBitmap.getHeight();
        } else {
            mWidth = mHeight = 0;
        }
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}
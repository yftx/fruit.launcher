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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.TextView;

/**
 * TextView that draws a bubble behind the text. We cannot use a
 * LineBackgroundSpan because we want to make the bubble taller than the text
 * and TextView's clip is too aggressive.
 */
public class BubbleTextView extends TextView {

	static final float CORNER_RADIUS = 8.0f;
	static final float PADDING_H = 5.0f;
	static final float PADDING_V = 1.0f;

	private boolean mBackgroundSizeChanged;
	private Drawable mBackground;

	private final RectF mRect = new RectF();
	private Paint mPaint;

	private float mCornerRadius;
	private float mPaddingH;
	private float mPaddingV;

	private Launcher mLauncher;

	private boolean mIsDrawShadow = false;

	public BubbleTextView(Context context) {
		super(context);
		init(context, null, -1);
	}

	public BubbleTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, -1);
	}

	public BubbleTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		setFocusable(true);
		mBackground = getBackground();
		setBackgroundDrawable(null);

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(getContext().getResources().getColor(
				R.color.bubble_dark_background));

		final float scale = getContext().getResources().getDisplayMetrics().density;
		mCornerRadius = CORNER_RADIUS * scale;
		mPaddingH = PADDING_H * scale;
		// noinspection PointlessArithmeticExpression
		mPaddingV = PADDING_V * scale;
		final DisplayMetrics metrics = getResources().getDisplayMetrics();

		if (DisplayMetrics.DENSITY_DEVICE <= 120) {
			setTextSize(16.f);
		}

		if (attrs != null) {
			TypedArray typedArray = context.obtainStyledAttributes(attrs,
					R.styleable.BubbleTextView, defStyle, 0);
			mIsDrawShadow = typedArray.getBoolean(
					R.styleable.BubbleTextView_drawShadow, false);
		}
	}

	@Override
	protected boolean setFrame(int left, int top, int right, int bottom) {
		if (mLeft != left || mRight != right || mTop != top
				|| mBottom != bottom) {
			mBackgroundSizeChanged = true;
		}
		return super.setFrame(left, top, right, bottom);
	}

	@Override
	protected boolean verifyDrawable(Drawable who) {
		return who == mBackground || super.verifyDrawable(who);
	}

	@Override
	protected void drawableStateChanged() {
		Drawable d = mBackground;
		if (d != null && d.isStateful()) {
			d.setState(getDrawableState());
		}
		super.drawableStateChanged();
	}

	@Override
	public void draw(Canvas canvas) {
		final Drawable background = mBackground;
		if (background != null) {
			final int scrollX = mScrollX;
			final int scrollY = mScrollY;

			if (mBackgroundSizeChanged) {
				background.setBounds(0, 0, mRight - mLeft, mBottom - mTop);
				mBackgroundSizeChanged = false;
			}

			if ((scrollX | scrollY) == 0) {
				background.draw(canvas);
			} else {
				canvas.translate(scrollX, scrollY);
				background.draw(canvas);
				canvas.translate(-scrollX, -scrollY);
			}
		}

		final Layout layout = getLayout();
		final RectF rect = mRect;
		final int left = getCompoundPaddingLeft();
		final int top = getExtendedPaddingTop();
		if (mIsDrawShadow) {
			rect.set(left + layout.getLineLeft(0) - mPaddingH,
					top + layout.getLineTop(0) - mPaddingV, Math.min(left
							+ layout.getLineRight(0) + mPaddingH, mScrollX
							+ mRight - mLeft), top + layout.getLineBottom(0)
							+ mPaddingV);
			canvas.drawRoundRect(rect, mCornerRadius, mCornerRadius, mPaint);

		}

		if (false) {
			Paint debugPaint = new Paint();
			debugPaint.setColor(0xffcccc00);
			canvas.drawRect(rect, debugPaint);
		}

		super.draw(canvas);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mBackground != null) {
			mBackground.setCallback(this);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mBackground != null) {
			mBackground.setCallback(null);
		}
	}

	public final int getHeightOfText() {
		int height = 0;
		Layout layout = getLayout();

		if (layout != null) {
			int top = layout.getLineTop(0);
			int bottom = layout.getLineBottom(0);
			height = (bottom - top) + (int) (8.0f * Utilities.sDensity);
		}
		return height;
	}

	public void setDrawShadow(boolean shadow) {
		mIsDrawShadow = shadow;
	}
}
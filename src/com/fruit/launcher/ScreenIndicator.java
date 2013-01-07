package com.fruit.launcher;

import com.fruit.launcher.setting.SettingUtils;
import com.fruit.launcher.theme.ThemeManager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ScreenIndicator extends ViewGroup {

	private int mScreenCount;
	private int mCurrentScreen;
	private int mDotSeparate;
	private ThemeManager mThemeMgr;

	public ScreenIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mScreenCount = SettingUtils.mScreenCount;
		mCurrentScreen = SettingUtils.mHomeScreenIndex;
		initIndicator();
	}

	public ScreenIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScreenIndicator(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initIndicator();
	}

	private void initIndicator() {
		setFocusable(false);
		setWillNotDraw(false);

		mDotSeparate = 10;
		mThemeMgr = ThemeManager.getInstance();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (mScreenCount <= 0) {
			return;
		}
		createLayout();
	}

	private void updateLayout() {
		for (int i = 0; i < getChildCount(); i++) {
			final ImageView img = (ImageView) getChildAt(i);
			img.setSelected((i == mCurrentScreen));
		}
	}

	private void createLayout() {
		detachAllViewsFromParent();

		// int dotWidth = Launcher.mScreenWidth / SettingUtils.mScreenCount;
		int dotWidth = mThemeMgr.loadScreenIndicatorIcon().getIntrinsicWidth();

		int marginLeft = getWidth()
				/ 2
				- (mScreenCount * dotWidth / 2 + (mScreenCount - 1)
						* mDotSeparate / 2);
		int marginTop = getHeight() / 2 - dotWidth / 2;
		View currentView = null;

		for (int i = 0; i < mScreenCount; i++) {
			ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			ImageView dot = new ImageView(getContext());
			Drawable drawable = mThemeMgr.loadScreenIndicatorIcon();
			dot.setImageDrawable(drawable);
			dot.setLayoutParams(lp);
			int childHeightSpec = getChildMeasureSpec(
					MeasureSpec.makeMeasureSpec(dotWidth,
							MeasureSpec.UNSPECIFIED), 0, lp.height);
			int childWidthSpec = getChildMeasureSpec(
					MeasureSpec.makeMeasureSpec(dotWidth, MeasureSpec.EXACTLY),
					0, lp.width);
			dot.measure(childWidthSpec, childHeightSpec);

			int left = marginLeft + i * (dotWidth + mDotSeparate);

			dot.layout(left, marginTop, left + dotWidth, marginTop + dotWidth);
			addViewInLayout(dot, getChildCount(), lp, true);
			if (i == mCurrentScreen) {
				currentView = dot;
			}
		}

		if (currentView != null) {
			currentView.setSelected(true);
		}
		postInvalidate();
	}

	final int getScreenCount() {
		return mScreenCount;
	}

	final void setScreenCount(int count) {
		if (count != mScreenCount) {
			mScreenCount = count;
			createLayout();
		}
	}

	final int getCurrentScreen() {
		return mCurrentScreen;
	}

	final void setCurrentScreen(int currentScreen) {
		if (currentScreen != mCurrentScreen) {
			mCurrentScreen = currentScreen;
			updateLayout();
		}
	}

	final void apllyTheme() {
		removeAllViews();
		createLayout();
	}


	final void switchScreenMode(boolean bIsFullscreen, int value) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
		lp.topMargin = bIsFullscreen ? value : 0;
		setLayoutParams(lp);
	}
}
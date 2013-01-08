package com.fruit.launcher;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.util.XmlUtils;
import com.fruit.launcher.LauncherSettings.BaseLauncherColumns;
import com.fruit.launcher.LauncherSettings.Favorites;

public class DockBar extends ViewGroup {

	public static final String TAG = "DockBar";

	public static final int DEFAULT_CELL_NUM_IDEAL = 4;// yfzhao//5;
	public static final int DEFAULT_CELL_NUM_ALL_APP = 3;
	private static final int ORIENTATION_HORIZONTAL = 1;

	public static final int MAX_CELL_NUM_ALL_APP = 4;

	private static final String TAG_Favorite_All_Apps = "favorite_allapps";

	public int mIdealHomeIndex = -1; // yfzhao
	public int mAllAppHomeIndex = -1; // yfzhao

	private float mDensity;

	private boolean mFirstMeasure;
	private boolean mPortrait;
	private int mCellNum;
	private int mLeftPadding;
	private int mRightPadding;
	private int mTopPadding;
	private int mBottomPadding;

	private int mWidthGap;
	private int mHeightGap;

	private int mCellNumAllApp = 0;

	private DragController mDragController;
	private Paint mTrashPaint;

	private boolean mIsDockItemShow;
	private DockButton[] mDockButtons;
	private DockButton[] mAllAppDockButtons;
	private boolean mInAllAppMode;
	private View.OnClickListener mDockButtonClickListener;

	private IconCache mIconCache;
	
	private ArrayList<ShortcutInfo> mAllAppHomeBar;

	public DockBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public DockBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mDensity = Utilities.sDensity;
		mFirstMeasure = true;

		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.DockBar, defStyle, 0);
		int orientation = typedArray.getInt(R.styleable.DockBar_direction,
				ORIENTATION_HORIZONTAL);
		if (orientation == ORIENTATION_HORIZONTAL) {
			mPortrait = true;
		} else {
			mPortrait = false;
		}

		mCellNum = typedArray.getInt(R.styleable.DockBar_cellNum,
				DEFAULT_CELL_NUM_IDEAL);
		
		mLeftPadding = typedArray.getDimensionPixelOffset(
				R.styleable.DockBar_leftPadding, 10);
		mRightPadding = typedArray.getDimensionPixelOffset(
				R.styleable.DockBar_rightPadding, 10);

		mTopPadding = typedArray.getDimensionPixelOffset(
				R.styleable.DockBar_topPadding, 0);
		mBottomPadding = typedArray.getDimensionPixelOffset(
				R.styleable.DockBar_bottomPadding, 0);
	
		// mIdealHomeIndex = mCellNum / 2;
		// mAllAppHomeIndex = mCellNumAllApp / 2;
		mIsDockItemShow = true;
		mDockButtons = new DockButton[DEFAULT_CELL_NUM_IDEAL];

		final int srcColor = context.getResources().getColor(
				R.color.dock_color_filter);
		mTrashPaint = new Paint();
		mTrashPaint.setColorFilter(new PorterDuffColorFilter(srcColor,
				PorterDuff.Mode.SRC_ATOP));

		LauncherApplication app = (LauncherApplication) context
				.getApplicationContext();
		mIconCache = app.getIconCache();

		// loadAllAppHomeBar(getContext());
		// mAllAppHomeIndex = getAppHomeBarCount() / 2;
		// mCellNumAllApp = getAppHomeBarCount() + 1;
		// mAllAppDockButtons = new DockButton[mCellNumAllApp];
		// initAllAppDockButton();
		// initIdealDockButton();

		// setBackgroundDrawable(null);
		setBackgroundDrawable(mIconCache.getLocalIcon(
				R.drawable.dock_background, "dock_background"));
	}

	// private void initAllAppDockButton() {
	// // TODO Auto-generated method stub
	// LayoutInflater inflater = LayoutInflater.from(mContext);
	//
	// mInAllAppMode = false;
	// mDockButtonClickListener = new View.OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// // TODO Auto-generated method stub
	// if (mInAllAppMode && v.getTag() != null) {
	// ShortcutInfo appInfo = (ShortcutInfo) v.getTag();
	// if (appInfo.intent != null) {
	// mContext.startActivity(appInfo.intent);
	// }
	// }
	// }
	// };
	//
	// int dockBarCount = mAllAppHomeBar.size() + 1;
	//
	// for (int i = 0; i < dockBarCount; i++) {
	//
	// DockButton dockButton = (DockButton)
	// inflater.inflate(R.layout.dock_button, null);
	// ShortcutInfo info = null;
	// if(i == mAllAppHomeIndex){
	// info = new ShortcutInfo();
	//
	// dockButton.mIsHome = true;
	// dockButton.setImageDrawable(mIconCache.getLocalIcon(R.drawable.home_button,
	// "ic_home_button"));
	// }else{
	// int allAppHomeIndex = i > mAllAppHomeIndex ? i-1 : i;
	//
	// info = mAllAppHomeBar.get(allAppHomeIndex);
	// dockButton.mIsHome = false;
	// dockButton.setImageBitmap(mIconCache.getIcon(info.intent));
	// }
	//
	// info.cellX = i;
	// info.cellY = -1;
	// info.container = Favorites.CONTAINER_DOCKBAR;
	// info.screen = -1;
	//
	// dockButton.mIsHold = true;
	// dockButton.mIsEmpty = false;
	// dockButton.setTag(info);
	// dockButton.setPaint(mTrashPaint);
	// dockButton.setClickable(true);
	// dockButton.setOnClickListener(mDockButtonClickListener);
	// mAllAppDockButtons[i] = dockButton;
	// }
	// }
	//
	// private void initIdealDockButton(){
	// LayoutInflater inflater = LayoutInflater.from(mContext);
	// // Initialize Home button
	// ShortcutInfo info = new ShortcutInfo();
	// info.container = Favorites.CONTAINER_DOCKBAR;
	// info.cellX = mIdealHomeIndex;
	// info.cellY = -1;
	// info.screen = -1;
	// info.itemType = BaseLauncherColumns.ITEM_TYPE_APPLICATION;
	//
	// try {
	// DockButton dockButton =(DockButton)
	// inflater.inflate(R.layout.dock_button, null);
	//
	// dockButton.mIsHome = true;
	// dockButton.mIsHold = true;
	// dockButton.mIsEmpty = false;
	// dockButton.setPaint(mTrashPaint);
	// dockButton.setClickable(true);
	// dockButton.setTag(info);
	// dockButton.setImageDrawable(mIconCache.getLocalIcon(R.drawable.all_apps_button,
	// "ic_all_apps_button"));
	//
	// mDockButtons[mIdealHomeIndex] = dockButton;
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * Loads the default set of default to packages from an xml file.
	 * 
	 * @modify guo
	 * @param context
	 *            The context
	 */
	// private boolean loadAllAppHomeBar(Context context) {
	// boolean bRet = false;
	//
	// if (mAllAppHomeBar == null) {
	// mAllAppHomeBar = new ArrayList<ShortcutInfo>();
	// } else {
	// return true;
	// }
	//
	// PackageManager packageManager = mContext.getPackageManager();
	// ActivityInfo actInfo;
	// try {
	// XmlResourceParser parser =
	// context.getResources().getXml(R.xml.default_allapp_slider);
	// AttributeSet attrs = Xml.asAttributeSet(parser);
	// XmlUtils.beginDocument(parser, TAG_Favorite_All_Apps);
	//
	// final int depth = parser.getDepth();
	//
	// int type;
	// int i = 0;
	// while (((type = parser.next()) != XmlPullParser.END_TAG ||
	// parser.getDepth() > depth)
	// && type != XmlPullParser.END_DOCUMENT) {
	//
	// if (type != XmlPullParser.START_TAG) {
	// continue;
	// }
	//
	// TypedArray a = context.obtainStyledAttributes(attrs,
	// R.styleable.Favorite_Allapp);
	// String packageName =
	// a.getString(R.styleable.Favorite_Allapp_AllappPackageName);
	// String className =
	// a.getString(R.styleable.Favorite_Allapp_AllappClassName);
	// try {
	// ComponentName cn;
	// try {
	// cn = new ComponentName(packageName, className);
	// actInfo = packageManager.getActivityInfo(cn, 0);
	// } catch (PackageManager.NameNotFoundException nnfe) {
	// String[] packages = packageManager.currentToCanonicalPackageNames(
	// new String[] { packageName });
	// cn = new ComponentName(packages[0], className);
	// actInfo = packageManager.getActivityInfo(cn, 0);
	// }
	//
	// ShortcutInfo info = new ShortcutInfo();
	// Intent intent = new Intent();
	// intent.setAction(Intent.ACTION_MAIN);
	// intent.setComponent(cn);
	// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
	// Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
	// info.intent = intent;
	// mAllAppHomeBar.add(info);
	// } catch (PackageManager.NameNotFoundException e) {
	// Log.w(TAG, "Unable to add favorite: " + packageName + "/" + className,
	// e);
	// continue;
	// }
	//
	// a.recycle();
	// i++;
	// if(i > MAX_CELL_NUM_ALL_APP){
	// break;
	// }
	// }
	// } catch (XmlPullParserException e) {
	// Log.w(TAG, "Got exception parsing AllAppHomeBar.", e);
	// } catch (IOException e) {
	// Log.w(TAG, "Got exception parsing AllAppHomeBar.", e);
	// }
	//
	// return bRet;
	// }

	// private int getAppHomeBarCount() {
	// return mAllAppHomeBar.size();
	// }

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		for (int i = 0; i < getChildCount(); i++) {
			View view = getChildAt(i);
			view.clearAnimation();
			if (view.getVisibility() != View.GONE) {
				DockBar.LayoutParams layoutParams = (DockBar.LayoutParams) view
						.getLayoutParams();
				view.layout(layoutParams.left, layoutParams.top,
						layoutParams.width + layoutParams.left,
						layoutParams.height + layoutParams.top);
			}
		}
	}

	public boolean onLongClick(View v) {
		if (v.isInTouchMode()) {
			if (v.getTag() != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthSpecMode == MeasureSpec.UNSPECIFIED
				|| heightSpecMode == MeasureSpec.UNSPECIFIED) {
			throw new RuntimeException(
					"DockBar cannot have UNSPECIFIED dimensions");
		}

		if (mFirstMeasure) {
			if (mPortrait) {
				mWidthGap = (widthSpecSize - mLeftPadding - mRightPadding)
						/ mCellNum;
				mHeightGap = heightSpecSize - (int) (0.0f * mDensity)
						- mTopPadding - mBottomPadding;
			} else {
				mWidthGap = widthSpecSize - (int) (0.0f * mDensity)
						- mTopPadding - mBottomPadding;
				mHeightGap = (heightSpecSize - mLeftPadding - mRightPadding)
						/ mCellNum;
			}
			mFirstMeasure = false;
		}

		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			DockBar.LayoutParams lp = (DockBar.LayoutParams) child
					.getLayoutParams();

			lp.width = mWidthGap - lp.leftMargin - lp.rightMargin;
			lp.height = mHeightGap - lp.topMargin - lp.bottomMargin;
			if (mPortrait) {
				lp.left = mWidthGap * lp.index + lp.leftMargin + mLeftPadding;
				// if (mDensity == 1.0f) {
				lp.top = lp.topMargin + (int) (2.0f * mDensity) + mTopPadding;
				// } else if (mDensity >= 1.5f) {
				// lp.top = lp.topMargin - (int) (4.0f * mDensity) +
				// mTopPadding;
				// }
			} else {
				lp.left = lp.leftMargin + (int) (4.0f * mDensity) + mTopPadding;
				lp.top = mHeightGap * lp.index + lp.topMargin + mLeftPadding;
			}

			int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width,
					MeasureSpec.EXACTLY);
			int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height,
					MeasureSpec.EXACTLY);
			child.measure(childWidthMeasureSpec, childheightMeasureSpec);
		}
		setMeasuredDimension(widthSpecSize, heightSpecSize);
	}

	final static class LayoutParams extends ViewGroup.MarginLayoutParams {

		public int index;
		int left;
		int top;

		public LayoutParams(int index) {
			super(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			// TODO Auto-generated constructor stub
			this.index = index;
		}

		public LayoutParams(Context context, AttributeSet attrs) {
			super(context, attrs);
			// TODO Auto-generated constructor stub
		}

		public LayoutParams(ViewGroup.LayoutParams layoutParams) {
			super(layoutParams);
			// TODO Auto-generated constructor stub
		}
	}

	public void setItemVisibity(boolean bIsShow) {
		// TODO Auto-generated method stub
		if (mIsDockItemShow != bIsShow) {
			mIsDockItemShow = bIsShow;

			int visible = bIsShow ? View.VISIBLE : View.GONE;

			for (int i = 0; i < getChildCount(); i++) {
				if (i != mIdealHomeIndex) {
					View child = getChildAt(i);
					child.setVisibility(visible);
				}
			}
		}
	}

	public void setCellInfo(int num) {
		if (mCellNum != num) {
			mCellNum = num;
			invalidate();
		}
	}

	public void switchDisplay(boolean isAllAppMode) {
		DockBar.LayoutParams lp = null;

		if (isAllAppMode) {
			// Remove all dock buttons
			removeAllViews();

			// Add mAllAppDockButtons to dock bar
			for (int i = 0; i < mCellNumAllApp; i++) {
				lp = new DockBar.LayoutParams(i);
				if (mAllAppDockButtons[i] != null) {
					addView(mAllAppDockButtons[i], -1, lp);
				}
			}
			mCellNum = mCellNumAllApp;
		} else {
			// Remove all dock buttons
			removeAllViews();

			// Add mDockButtons to dock bar
			for (int i = 0; i < DEFAULT_CELL_NUM_IDEAL; i++) {
				lp = new DockBar.LayoutParams(i);
				if (mDockButtons[i] != null) {
					addView(mDockButtons[i], -1, lp);
				}
			}
			mCellNum = DEFAULT_CELL_NUM_IDEAL;
		}

		mInAllAppMode = isAllAppMode;
		processDragController(!isAllAppMode);
		// Force to let dock bar re-measure size
		mFirstMeasure = true;
		invalidate();
	}

	public Paint getPaint() {
		return mTrashPaint;
	}

	public void setIdealButton(View view, int index) {
		if (index < 0 || index >= DEFAULT_CELL_NUM_IDEAL
				&& !(view instanceof DockButton)) {
			return;
		}
		if (mDockButtons[index] != null) {
			DockButton button = mDockButtons[index];
			button.setVisibility(View.GONE);
			mDragController.removeDropTarget(button);

			button.setTag(null);
			removeView(button);
		}
		mDockButtons[index] = (DockButton) view;
	}

	public DockButton getIdealButtons(int index) {
		if (index < 0 || index >= DEFAULT_CELL_NUM_IDEAL) {
			return null;
		}
		return mDockButtons[index];
	}

	public DockButton getAllAppButtons(int index) {
		if (index < 0 || index >= mCellNumAllApp) {
			return null;
		}
		return mAllAppDockButtons[index];
	}

	private void processDragController(boolean isAdd) {
		// TODO Auto-generated method stub
		if (mDragController == null) {
			return;
		}
		for (int i = 0; i < DEFAULT_CELL_NUM_IDEAL; i++) {
			DockButton dockButton = mDockButtons[i];
			if (dockButton != null && !dockButton.mIsHome) {
				if (isAdd) {
					mDragController.removeDropTarget(dockButton);
					mDragController.addDropTarget(dockButton);
				} else {
					mDragController.removeDropTarget(dockButton);
				}
			}
		}
	}

	public void setDragController(DragController dragController) {
		// TODO Auto-generated method stub
		mDragController = dragController;
	}

	public void applyTheme() {
		DockButton button = null;
		ShortcutInfo info = null;

		for (int i = 0; i < DockBar.DEFAULT_CELL_NUM_IDEAL; i++) {
			if (i != mIdealHomeIndex) {
				button = getIdealButtons(i);
				info = (ShortcutInfo) button.getTag();
				if (info != null && !button.mIsEmpty) {
					info.setIcon(null);
					button.setImageBitmap(info.getIcon(mIconCache));
				}
			}
		}

		// jinzhimin add for apply theme in all app 2010-06-15
		for (int i = 0; i < DockBar.DEFAULT_CELL_NUM_ALL_APP; i++) {
			if (i != mAllAppHomeIndex) {
				button = getIdealButtons(i);
				info = (ShortcutInfo) button.getTag();
				if (info != null && !button.mIsEmpty) {
					info.setIcon(null);
					button.setImageBitmap(info.getIcon(mIconCache));
				}
			}
		}

		applyThemeOnHolderBar();
	}

	public void applyThemeOnHolderBar() {
		// TODO Auto-generated method stub
		final IconCache iconCache = mIconCache;
		// DockButton allAppLeftButton = getAllAppButtons(0);
		// DockButton allAppRightButton = getAllAppButtons(2);
		// DockButton allAppButton = getAllAppButtons(mAllAppHomeIndex);
		// DockButton homeButton = getIdealButtons(mIdealHomeIndex);

		// Drawable allAppIcon =
		// iconCache.getLocalIcon(R.drawable.all_apps_button,
		// "ic_all_apps_button");
		// Drawable homeIcon = iconCache.getLocalIcon(R.drawable.home_button,
		// "ic_home_button");

		// allAppButton.setImageDrawable(homeIcon);
		// homeButton.setImageDrawable(allAppIcon);

		// Drawable leftIcon =
		// iconCache.getLocalIcon(R.drawable.ic_launcher_market_normal,
		// "ic_market_button");
		// allAppLeftButton.setImageDrawable(leftIcon);
		//
		// Drawable rightIcon =
		// iconCache.getLocalIcon(R.drawable.ic_launcher_search_normal,
		// "ic_search_button");
		// allAppRightButton.setImageDrawable(rightIcon);

		Drawable dockBg = iconCache.getLocalIcon(R.drawable.dock_background,
				"dock_background");
		setBackgroundDrawable(dockBg);
	}
}

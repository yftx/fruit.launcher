/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

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

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import com.fruit.launcher.LauncherSettings.Applications;
import com.fruit.launcher.LauncherSettings.BaseLauncherColumns;
import com.fruit.launcher.theme.ThemeManager;
import com.fruit.launcher.theme.ThemeUtils;

public class AllApps2D_Slide extends RelativeLayout
		implements AllAppsView,
			AdapterView.OnItemClickListener,
			AdapterView.OnItemLongClickListener,
			DragSource, DropTarget {

	private static final String TAG = "AllApps2D_Slide";

	private static final int MSG_APPS_UPDATED = 1;

	private static Launcher mLauncher;

	private SlidingView mGrid;
	private AllAppsHomeBar mAllAppsHomeBar;
	private int mGridEndYPos;

	private ArrayList<ApplicationInfo> mAllAppsList = new ArrayList<ApplicationInfo>();
	ArrayList<ItemInfo> mSlideAppsList = new ArrayList<ItemInfo>();

	private boolean mVisible;
	private float mZoom;

	private AppsAdapter mAppsAdapter;

	private DragController mDragController;

	private int nPos = 0;

	private ContentResolver mContentResolver;

	//private HomeButton homeButton;

	private int mCurrentScreen;
	private ScreenIndicator mScreenIndicator;

	private int mBGColor;

	private IconCache mIconCache;

	private Handler mHandler;
	
	private boolean mbLockApps = false;
	private int mLockAppsNum = 0;

	private Bitmap resizeBitmap(Bitmap refer, Bitmap src) {
		// get the height of bitmap
		int width = src.getWidth();
		int height = src.getHeight();
		// destination size
		float newWidth = (refer.getWidth() - 13) / 2;
		float newHeight = (refer.getHeight() - 13) / 2;
		// percent of resize
		float scaleWidth = newWidth / width;
		float scaleHeight = newHeight / height;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		// resize bitmap
		Bitmap newbm = Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);

		return newbm;
	}

	public class AppsAdapter extends ArrayAdapter<ItemInfo> {

		private final LayoutInflater mInflater;

		public AppsAdapter(Context context, ArrayList<ItemInfo> apps) {
			super(context, 0, apps);
			mInflater = LayoutInflater.from(context);
		}

		@SuppressWarnings("deprecation")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ItemInfo info = getItem(position);

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.application_boxed, parent, false);
				convertView.setFocusable(false);
			}

			final TextView textView = (TextView) convertView;
			if (info.itemType == Applications.APPS_TYPE_APP) {
				//ApplicationInfoEx : packages ,isSystemApp
				ApplicationInfoEx appInfoEx = (ApplicationInfoEx) info;
				appInfoEx.iconBitmap.setDensity(Bitmap.DENSITY_NONE);

				textView.setCompoundDrawablesWithIntrinsicBounds(
						null,
						new BitmapDrawable(appInfoEx.iconBitmap),
						null,
						null);
				textView.setText(appInfoEx.title);
				textView.setTag(null);
			} else {
				//folder app
				ApplicationFolderInfo folderInfo = (ApplicationFolderInfo) info;
				final int count = folderInfo.getSize();
				LayerDrawable ld;
				Bitmap bm = mIconCache.getFolderLocalIcon(true);	
//				Bitmap bm = Utilities.createIconBitmap(getResources().getDrawable(R.drawable.ic_launcher_folder), mContext);
				
				bm.setDensity(Bitmap.DENSITY_NONE);
				Drawable[] array = new Drawable[Math.min(4, count) + 1];
				array[0] = new BitmapDrawable(bm); //bit resource

				// Generate drawable array when count > 0
				if (count > 0) {
					for (int i = 0; i < Math.min(4, count); i++) {
						ApplicationInfoEx subAppInfo = folderInfo.contents.get(i);
						Bitmap photo = resizeBitmap(bm, subAppInfo.iconBitmap);
						photo.setDensity(Bitmap.DENSITY_NONE);
						array[i + 1] = new BitmapDrawable(photo);
					}

					ld = new LayerDrawable(array);
					if (count >= 1) { 
						ld.setLayerInset(1, 6, 6, 1 + bm.getWidth() / 2, 1 + bm.getHeight() / 2);
					}

					if (count >= 2) {
						ld.setLayerInset(2, 1 + bm.getWidth() / 2, 6, 6, 1 + bm.getHeight() / 2);
					}

					if (count >= 3) {
						ld.setLayerInset(3, 6, 1 + bm.getHeight() / 2, 1 + bm.getWidth() / 2, 6);
					}

					if (count >= 4) {
						ld.setLayerInset(4, 1 + bm.getWidth() / 2, 1 + bm.getWidth() / 2, 6, 6);
					}
				} else {
					ld = new LayerDrawable(array);
				}

				textView.setCompoundDrawablesWithIntrinsicBounds(
				 		null,
				 		ld,
				  		null,
				  		null);
				textView.setText(folderInfo.title);
				textView.setTag(folderInfo);
			}

			convertView.setFocusable(false);
			return convertView;
		}
	}

	public AllApps2D_Slide(Context context, AttributeSet attrs) {
		super(context, attrs);
		setVisibility(View.GONE);
		setSoundEffectsEnabled(false);

		mContentResolver = mContext.getContentResolver();
		//mAppsAdapter = new AppsAdapter(getContext(), mAllAppsList);
		mAppsAdapter = new AppsAdapter(getContext(), mSlideAppsList);
		mAppsAdapter.setNotifyOnChange(false);

		mBGColor = context.getResources().getColor(R.color.transparent_background);
		
		mbLockApps = context.getResources().getBoolean(R.bool.config_lock_apps);
		mLockAppsNum = context.getResources().getInteger(R.integer.config_lock_apps_num);

		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case MSG_APPS_UPDATED:
					if (mAppsAdapter != null) {
						mAppsAdapter.notifyDataSetChanged();
					}
					break;
				}
			}
		};
	}

	@Override
	protected void onFinishInflate() {
//		setBackgroundColor(mBGColor);
		LauncherApplication app = (LauncherApplication) getContext().getApplicationContext();
		mIconCache = app.getIconCache();

		try {
			mScreenIndicator = (ScreenIndicator) findViewById(R.id.screenIndicatorInAllApp);
			mGrid = (SlidingView) findViewById(R.id.all_apps_2d_grid);
			if (mGrid == null) {
				throw new Resources.NotFoundException();
			}
			mGrid.setOnItemClickListener(this);
			mGrid.setOnItemLongClickListener(this);
//			mGrid.setLauncher(mLauncher);
			mGrid.setScreenIndicator(mScreenIndicator);
			mAllAppsHomeBar = (AllAppsHomeBar) findViewById(R.id.all_apps_home_bar);
			// Do not load database here,
			// called in theme manager to start loading all app data
			updateAllData();
		} catch (Resources.NotFoundException e) {
			Log.e(TAG, "Can't find necessary layout elements for AllApps2D_Slide");
		}
	}

	public AllApps2D_Slide(Context context, AttributeSet attrs, int defStyle) {
		this(context, attrs);
	}

	@Override
	public void setLauncher(Launcher launcher) {
		mLauncher = launcher;
		mAllAppsHomeBar.setLauncher(launcher);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		ItemInfo info = (ItemInfo) parent.getItemAtPosition(position);
		if (info.itemType == Applications.APPS_TYPE_APP) {
            mLauncher.startActivitySafely(((ApplicationInfoEx) info).intent, info);

            // [[add by liujian at 2012-5-19
            // recent application
            saveRecentApplication(((ApplicationInfoEx) info).intent.getComponent());
            // ]]at 2012-5-19
		} else {
			mCurrentScreen = mGrid.mCurrentScreen;

			ApplicationFolderInfo folderInfo = (ApplicationFolderInfo) info;
			Object tag = v.getTag();
			// Be sure to find the correct view of the folder's icon
			if (tag == null || !(tag instanceof ApplicationFolderInfo)
					|| ((ApplicationFolderInfo) tag).id != info.id) {
				ViewGroup gridView = null;
				if (mCurrentScreen == 0) {
					gridView = (ViewGroup) parent.getChildAt(1);
				} else {
					gridView = (ViewGroup) parent.getChildAt(2);
				}
				for (int i = 0; i < gridView.getChildCount(); i++) {
					View child = gridView.getChildAt(i);
					Object tagNew = child.getTag();
					if (tagNew != null && (tagNew instanceof ApplicationFolderInfo)) {
						if (((ApplicationFolderInfo) tagNew).id == info.id) {
							folderInfo.folderIcon = (BubbleTextView) child;
							continue;
						}
					}
				}
			} else {
				folderInfo.folderIcon = (BubbleTextView) v;
			}
			if (folderInfo.folderIcon != null) {
				folderInfo.allAppsHomeBars = mAllAppsHomeBar;
				mLauncher.openFolder(folderInfo);
			} else {
				Log.i(TAG, "folder view not found, do not perform click action");
			}
			nPos = position;
		}
	}
	
	//[[add by liujian at 2012-5-19
    //recent application 
	private static final String LIU_TAG = "saveRecentApplication";
	private static final int RECENT_MAX_NUM = 16;
	private static final String KEY_RECENT_APP_LIST = "recentAppList";
    private void saveRecentApplication(ComponentName componentName){
        try {
            PackageManager pm = mContext.getPackageManager();
            pm.getApplicationInfo("com.huaqin.taskmanager", 0);
            
            Log.d(LIU_TAG, "Found Application");
            
            int mode = Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE;
            SharedPreferences shared = mContext.getSharedPreferences("recentAppShared",mode);
            SharedPreferences.Editor editor = shared.edit();
            
            String recentAppString = shared.getString(KEY_RECENT_APP_LIST,"");
            if (recentAppString.equals("")){
                String sort = componentName.getPackageName() + "*" 
                                + componentName.getClassName() + " ";
                editor.putString(KEY_RECENT_APP_LIST, sort);
                editor.commit();
                return;
            } 
            
            Log.d(LIU_TAG,recentAppString);

            String component = componentName.getPackageName() + "*" + componentName.getClassName();
            String [] recentApps = recentAppString.split("\\s+");
            List<String> recentAppList = new ArrayList<String>(Arrays.asList(recentApps));
            
            recentAppList.remove(component);
            recentAppList.add(0,component);
            if (recentAppList.size() > RECENT_MAX_NUM) {
                recentAppList.remove(RECENT_MAX_NUM);
            }
            
            saveRecentAppToSharedPreference(editor,recentAppList);
            
        } catch (NameNotFoundException e) {
            Log.d(LIU_TAG, "NameNotFoundException");
            return;
        }
    }
    
    private void saveRecentAppToSharedPreference(SharedPreferences.Editor editor, List<String> list){
        String value = new String();
        for (int i=0; i<list.size(); i++){
            value += list.get(i)+" ";
        }
        
        Log.d(LIU_TAG, value);
        
        editor.putString(KEY_RECENT_APP_LIST, value);
        editor.commit();
    }
    //]]at 2012-5-19

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (!view.isInTouchMode()) {
			Log.e(TAG, "onItemLongClick !view.isInTouchMode()");
			return false;
		}
		
		synchronized (mSlideAppsList){
			nPos = mGrid.getPositionForView(view);
			if(mbLockApps && nPos < mLockAppsNum ){
				view.setPressed(false);
				return false;
			}
			
			mAllAppsHomeBar.showHomeBar();
			ItemInfo info = (ItemInfo) parent.getItemAtPosition(position);
			view.setVisibility(INVISIBLE);
			mDragController.startDrag(view, this, info, DragController.DRAG_ACTION_COPY);
			//mDragController.startDrag(view, this, findAppByItem(info), DragController.DRAG_ACTION_COPY);
			//mDragController.addDropTarget(homeButton);
			//mDragController.addDropTarget(mLauncher.getHomeButton());
			mDragController.addDropTarget(mLauncher.mDeleteZone);
			mDragController.addDropTarget(mAllAppsHomeBar);
			nPos = mGrid.getPositionForView(view);
			mCurrentScreen = mGrid.mCurrentScreen;
			
			mGrid.setDragViewIndex(nPos + 1);
			//mLauncher.closeAllApps(true);
			return true;
		}
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, android.graphics.Rect prev) {
		if (gainFocus) {
			mGrid.requestFocus();
		}
	}

	@Override
	public void setDragController(DragController dragger) {
		mDragController = dragger;
	}

	@Override
	public void onDropCompleted(View target, boolean success) {
		// TODO Auto-generated method stub
		mAllAppsHomeBar.hideHomeBar();
		mDragController.removeDropTarget(mLauncher.mDeleteZone);
		mDragController.removeDropTarget(mAllAppsHomeBar);
		mLauncher.mDockBar.setVisibility(View.VISIBLE);
		mGrid.setDragViewIndex(0);
	}

	/**
	 * Zoom to the specifed level.
	 *
	 * @param zoom [0..1] 0 is hidden, 1 is open
	 */
	@Override
	public void zoom(float zoom, boolean animate) {
		mVisible = (zoom == 1.0f);
		Log.d(TAG, "zooming " + (mVisible ? "open" : "closed"));
		cancelLongPress();

		mZoom = zoom;

		if (isVisible()) {
			getParent().bringChildToFront(this);
			getParent().bringChildToFront(mLauncher.mDeleteZone);
			getParent().bringChildToFront(mLauncher.mDockBar);
			setVisibility(View.VISIBLE);
			mGrid.setAdapter(mAppsAdapter);
			mDragController.addDropTarget(this);
			mAllAppsHomeBar.hideHomeBar();
			if (animate) {
				startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.pull_in_from_top));
			} else {
				onAnimationEnd();
			}
		} else {
			mDragController.removeDropTarget(this);
			//mDragController.removeDropTarget(homeButton);
			//mDragController.removeDropTarget(mLauncher.getHomeButton());
			mDragController.removeDropTarget(mAllAppsHomeBar);
			//mDragController.removeDropTarget(mLauncher.mDeleteZone);
			mScreenIndicator.setVisibility(View.INVISIBLE);
			mAllAppsHomeBar.hideHomeBar();
			if (animate) {
				startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.pull_out_to_top));
			} else {
				onAnimationEnd();
			}
		}
	}

	@Override
	protected void onAnimationEnd() {
		if (!isVisible()) {
			setVisibility(View.GONE);
			mGrid.setAdapter(null);
			mZoom = 0.0f;
		} else {
			mZoom = 1.0f;
			//updateAppcationList();
			mScreenIndicator.setVisibility(View.VISIBLE);
			if (mGrid != null) {
				int[] position = new int[2];
				mGrid.getLocationOnScreen(position);
				mGridEndYPos = position[1] + mGrid.getHeight();
			}
		}

		mLauncher.zoomed(mZoom);
	}

	@Override
	public boolean isVisible() {
		return mVisible;
	}

	@Override
	public boolean isOpaque() {
		return mZoom > 0.999f;
	}

	@Override
	public void setApps(ArrayList<ApplicationInfo> list) {
		mAllAppsList.clear();
		addApps(list);
	}

	public void reorderApps() {

	}

	@Override
	public void addApps(ArrayList<ApplicationInfo> list) {
		//Log.d(TAG, "addApps: " + list.size() + " apps: " + list.toString());

		final int N = list.size();

		for (int i = 0; i < N; i++) {
			final ApplicationInfo item = list.get(i);
			int index = Collections.binarySearch(mAllAppsList, item,
					LauncherModel.APP_NAME_COMPARATOR);
			if (index < 0) {
				index = -(index + 1);
			}
			mAllAppsList.add(index, item);
		}
		mAppsAdapter.notifyDataSetChanged();
	}

	@Override
	public void removeApps(ArrayList<ApplicationInfo> list) {
		final int N = list.size();
		for (int i = 0; i < N; i++) {
			final ApplicationInfo item = list.get(i);
			int index = findAppByComponent(mAllAppsList, item);
			if (index >= 0) {
				mAllAppsList.remove(index);
			} else {
				Log.w(TAG, "couldn't find a match for item \"" + item + "\"");
				// Try to recover.  This should keep us from crashing for now.
			}
		}
		mAppsAdapter.notifyDataSetChanged();
	}

	@Override
	//App drag in delete zone ,after revert uninstall UI
	public void removePackage(ArrayList<ApplicationInfo> apps) {
		final int N = apps.size();

		for (int i = 0; i < N; i++) {
			final ApplicationInfo item = apps.get(i);

			if (item == null) {
				Log.w(TAG, "removePackage couldn't find a match for item \"" + item + "\"");
				continue;
			}

			for (int j = 0; j < mSlideAppsList.size(); j++) {
				ItemInfo info = mSlideAppsList.get(j);
				if (info.itemType == Applications.APPS_TYPE_FOLDER) {
					ApplicationFolderInfo folderInfo = (ApplicationFolderInfo) info;
					for (int k = 0; k < folderInfo.getSize(); k++) {
						ApplicationInfoEx y = folderInfo.contents.get(k);
						if (item.componentName.getPackageName().equals(y.packageName)) {
							mGrid.adjustOrderIdInFolder(folderInfo, y.orderId);
							folderInfo.remove(y);
							final Uri deleteUri = Applications.CONTENT_URI;
							final ContentResolver cr = getContext().getContentResolver();
							cr.delete(deleteUri,
									 Applications.PACKAGENAME+ "=?",
									 new String[]{item.intent.getComponent().getPackageName()});
						}
					}
				} else {
					ApplicationInfoEx appInfoEx = (ApplicationInfoEx) info;
					if (item.componentName.getPackageName().equals(appInfoEx.packageName)) {
						mGrid.removePackage(appInfoEx);
						mSlideAppsList.remove(appInfoEx);
				 	}
				}
			}
		}

		//updateAllData();
		mHandler.sendEmptyMessage(MSG_APPS_UPDATED);
	}

	@Override
	public void addPackage(ArrayList<ApplicationInfo> apps) {
		final int N = apps.size();

		for (int i = 0; i < N; i++) {
			final ApplicationInfo item = apps.get(i);
			if (item != null) {
				final String pkgName = item.componentName.getPackageName();
				if (!pkgName.startsWith(ThemeUtils.THEME_PACKAGE_TOKEN)) {
					long id = mGrid.addPackage(item);
					ApplicationInfoEx appInfoEx = createApplicationInfoEx(item);
					if (id != -1) {
						appInfoEx.id = id;
					}
					mSlideAppsList.add(appInfoEx);
				} else {
					Log.i(TAG, "find launcherEx theme package " + pkgName);
				}
			} else {
				Log.w(TAG, "addPackage couldn't find a match for item \"" + item + "\"");
			}
		}

		//updateAllData();
		mHandler.sendEmptyMessage(MSG_APPS_UPDATED);
	}

	private ApplicationInfoEx createApplicationInfoEx(ApplicationInfo item) {
		// TODO Auto-generated method stub
		ApplicationInfoEx appInfoEx = new ApplicationInfoEx();
		appInfoEx.id = item.id;
		appInfoEx.title = item.title;
		appInfoEx.container = Applications.CONTAINER_APPS;
		appInfoEx.position = mSlideAppsList.size();
		appInfoEx.itemType = Applications.APPS_TYPE_APP;
		appInfoEx.packageName = item.componentName.getPackageName();
		appInfoEx.intent =
			Utilities.orgnizeAppIconIntent(item.componentName.getPackageName(),
					item.componentName.getClassName());

		if (Utilities.isSystemApplication(mLauncher, appInfoEx.packageName)) {
			appInfoEx.isSysApp = true;
		} else {
			appInfoEx.isSysApp = false;
		}

		if (appInfoEx.itemType != Applications.APPS_TYPE_FOLDER) {
			appInfoEx.iconBitmap = mIconCache.getIcon(item.intent);
		}

		return appInfoEx;
	}

	@Override
	public void deleteFolder(ApplicationFolderInfo foldInfo) {
		synchronized (mSlideAppsList) {
			int nLast = mAppsAdapter.getCount() - 1;
			mGrid.deleteFolder(foldInfo, foldInfo.contents);
			mSlideAppsList.remove(foldInfo.position);
			if (foldInfo.getSize() > 0) {
				ApplicationInfoEx infoEx = foldInfo.contents.get(0);
				infoEx.position = foldInfo.position;
				infoEx.container  = Applications.CONTAINER_APPS;
				infoEx.itemType   = Applications.APPS_TYPE_APP;
				mSlideAppsList.add(foldInfo.position, infoEx);
	
				for (int i = 1; i < foldInfo.getSize(); i++ ) {
					ApplicationInfoEx  itemInfoEx = foldInfo.contents.get(i);
					itemInfoEx.position = nLast + i;
					itemInfoEx.container = Applications.CONTAINER_APPS;
					itemInfoEx.itemType = Applications.APPS_TYPE_APP;
					mSlideAppsList.add(nLast + i, itemInfoEx);
				}
				mAppsAdapter.notifyDataSetChanged();
			} else {
				for (int i = nPos; i < mSlideAppsList.size(); i++) {
					mSlideAppsList.get(i).position--;
				}
			}
		}
	}

	@Override
	public void addFolder(ApplicationFolderInfo foldInfo) {
		synchronized (mSlideAppsList) {
			for (int i = foldInfo.position ; i < mSlideAppsList.size(); i++) {
				ItemInfo info = mSlideAppsList.get(i);
				switch (info.itemType) {
				case Applications.APPS_TYPE_APP:
					((ApplicationInfoEx) info).position++;
					break;
				case Applications.APPS_TYPE_FOLDER:
					((ApplicationFolderInfo) info).position++;
					break;
				default:
					break;
				}
			}
			mSlideAppsList.add(foldInfo.position, foldInfo);
			mAppsAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void updateFolder(int index, String title) {
		((ApplicationFolderInfo) mSlideAppsList.get(index)).title = title;
		mAppsAdapter.notifyDataSetChanged();
	}

	public DropTarget getDropTarget() {
		return mLauncher.getHomeButton();
	}

	@Override
	public void updateApps(ArrayList<ApplicationInfo> list) {
		// Just remove and add, because they may need to be re-sorted.
		removeApps(list);
		addApps(list);
	}

	private static int findAppByComponent(ArrayList<ApplicationInfo> list, ApplicationInfo item) {
		ComponentName component = item.intent.getComponent();
		final int N = list.size();
		for (int i = 0; i < N; i++) {
			ApplicationInfo x = list.get(i);
			if (x.intent.getComponent().equals(component)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void dumpState() {
		ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList", mAllAppsList);
	}

	@Override
	public void surrender() {

	}

	@Override
	public int getCurrentPage() {
		return mGrid.mCurrentScreen;
	}

	@Override
	public void updateAllData() {
		new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateAppcationList();
				mHandler.sendEmptyMessage(MSG_APPS_UPDATED);
			}
		}.start();
	}

	private void exchangePos(int nPos, int nLastPos) {
		if (nPos < nLastPos) {
			for (int i = nPos + 1; i <= nLastPos; i++) {
				mSlideAppsList.get(i).position--;
			}
			ItemInfo info = mSlideAppsList.get(nPos);
			info.position = nLastPos;
			mSlideAppsList.remove(nPos);
			mSlideAppsList.add(nLastPos, info);
		} else if (nPos > nLastPos) {
			for (int i = nLastPos; i <= nPos; i++) {
				mSlideAppsList.get(i).position++;
			}
			ItemInfo info = mSlideAppsList.get(nPos);
			info.position = nLastPos;
			mSlideAppsList.remove(nPos);
			mSlideAppsList.add(nLastPos, info);
		}
	}

	public void moveItemCurrentPage(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		final int[] mCoordinatesTemp = new int[2];
		mGrid.getLocationOnScreen(mCoordinatesTemp);
		View lastView = mGrid.pointToViewEx(x - mCoordinatesTemp[0], y - mCoordinatesTemp[1],nPos);
		int nLastPos = -1;
		if (lastView == null) {
			nLastPos = mSlideAppsList.size() - 1;
		} else {
			nLastPos = mGrid.getPositionForView(lastView);
		}

		ItemInfo info = mSlideAppsList.get(nLastPos);
		if (info.itemType == Applications.APPS_TYPE_FOLDER) {
			// Drag into Application Folder
			ApplicationFolderInfo lastSlideInfo = (ApplicationFolderInfo) info;

			if (((ItemInfo) dragInfo).itemType == Applications.APPS_TYPE_FOLDER) {
				// Drag source is also an application folder, just update their position
				mGrid.moveItemToPosition(nPos, nLastPos, ((ApplicationFolderInfo) dragInfo).id);
				exchangePos(nPos, nLastPos);
			} else if (((ItemInfo) dragInfo).itemType == Applications.APPS_TYPE_FOLDERAPP) {
				// Drag from another application folder
				// delete the application info from old folder, and insert to new folder
				// If source folder is the drag target, do nothing
				final ApplicationInfoEx appInfo = (ApplicationInfoEx) dragInfo;
				if (lastView == null) {
					nLastPos++;

					// Remove appInfo from old folder
					ApplicationFolderInfo initFolderInfo = (ApplicationFolderInfo) ((UserFolder) source).mInfo;
					mGrid.adjustOrderIdInFolder(initFolderInfo, appInfo.orderId);
					initFolderInfo.remove(appInfo);

					// Add appInfo to applications last position
					appInfo.container = Applications.CONTAINER_APPS;
					appInfo.position = nLastPos;
					appInfo.itemType = Applications.APPS_TYPE_APP;
					mSlideAppsList.add(nLastPos, appInfo);
					mAppsAdapter.notifyDataSetChanged();
				} else {
					if (((ItemInfo) dragInfo).container != lastSlideInfo.id) {
						final ApplicationFolderInfo initFolderInfo =
							(ApplicationFolderInfo) ((UserFolder) source).mInfo;

						// Remove appInfo from old folder
						mGrid.adjustOrderIdInFolder(initFolderInfo, appInfo.orderId);
						initFolderInfo.remove(appInfo);

						// Add appInfo to new folder
						appInfo.orderId = lastSlideInfo.getSize();
						mGrid.moveFolderItemToFolder(appInfo, lastSlideInfo);
						lastSlideInfo.add(appInfo);

						mAppsAdapter.notifyDataSetChanged();
					}
				}
			} else {
				// Drag from all application grid to application folder
				if (lastView == null) {
					mGrid.moveItemToPosition(nPos, nLastPos, ((ItemInfo) dragInfo).id);
					exchangePos(nPos, nLastPos);
				} else {
					ApplicationInfoEx appInfo = (ApplicationInfoEx) dragInfo;

					appInfo.itemType = Applications.APPS_TYPE_FOLDERAPP;
					appInfo.orderId = lastSlideInfo.getSize();

					// Remove appInfo from applications
					for (int i = nPos + 1; i < mSlideAppsList.size(); i++) {
						mSlideAppsList.get(i).position--;
					}

					mGrid.moveItemToFolder(appInfo, lastSlideInfo.id);
					mSlideAppsList.remove(appInfo);

					// Add appInfo to folder
					lastSlideInfo.add(appInfo);

					View itemView = mGrid.getViewAtIndex(lastSlideInfo.position);
					if (itemView != null) {
						mAppsAdapter.getView(lastSlideInfo.position, itemView, null);
					}
				}
			}
		} else {
			// Drag to all application grid
			if (((ItemInfo) dragInfo).itemType == Applications.APPS_TYPE_FOLDERAPP) {
				// Drag from application folder to all application grid
				ApplicationInfoEx appInfo = (ApplicationInfoEx) dragInfo;
				final long folderId = appInfo.container;

				if (lastView == null) {
					nLastPos++;
				}

				// Add appInfo to applications
				mGrid.moveFolderItemToPosition(nLastPos, appInfo);
				for (int i = nLastPos; i < mSlideAppsList.size(); i++) {
					mSlideAppsList.get(i).position++;
				}
				appInfo.container = Applications.CONTAINER_APPS;
				appInfo.position = nLastPos;
				appInfo.itemType = Applications.APPS_TYPE_APP;
				mSlideAppsList.add(nLastPos, appInfo);

				// Remove appInfo from old folder
				final ApplicationFolderInfo srcFolderInfo = (ApplicationFolderInfo) ((UserFolder) source).mInfo;
				if (srcFolderInfo.itemType == Applications.APPS_TYPE_FOLDER
						&& srcFolderInfo.id == folderId) {
					mGrid.adjustOrderIdInFolder(srcFolderInfo, appInfo.orderId);
					srcFolderInfo.remove(appInfo);
				}

				// Force update folder item view with new contents
				View itemView = mGrid.getViewAtIndex(srcFolderInfo.position);
				if (itemView != null) {
					mAppsAdapter.getView(srcFolderInfo.position, itemView, null);
				}
			} else {
				// update two item' position
				mGrid.moveItemToPosition(nPos, nLastPos, ((ItemInfo) dragInfo).id);
				exchangePos(nPos, nLastPos);
			}
		}
	}

	public void moveItemDifferentPage(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		final int[] mCoordinatesTemp = new int[2];
		mGrid.getLocationOnScreen(mCoordinatesTemp);
		View lastView = mGrid.pointToViewEx(x - mCoordinatesTemp[0], y - mCoordinatesTemp[1],nPos);
		int nLastPos = -1;
		if (lastView == null) {
			nLastPos = mSlideAppsList.size() - 1;
		} else {
			nLastPos = mGrid.getPositionForView(lastView);
		}

		ItemInfo info = mSlideAppsList.get(nLastPos);
		if (info.itemType == Applications.APPS_TYPE_FOLDER) {
			// Drag into application folder
			ApplicationFolderInfo lastSlideInfo = (ApplicationFolderInfo) info;

			if (((ItemInfo) dragInfo).itemType == Applications.APPS_TYPE_FOLDER) {
				// Drag source is also an application folder, just update their position
				View mClickView = mAppsAdapter.getView(((ApplicationFolderInfo) dragInfo).position, null, null);
				mGrid.moveItemToPositionSlide(nPos, nLastPos, ((ApplicationFolderInfo) dragInfo).id, mClickView);
				exchangePos(nPos, nLastPos);
			} else if (((ItemInfo) dragInfo).itemType == Applications.APPS_TYPE_FOLDERAPP) {
				// Drag from another application folder
				// delete the application info from old folder, and insert to new folder
				ApplicationInfoEx appInfo = (ApplicationInfoEx) dragInfo;
				if (lastView == null) {
					nLastPos++;

					// Remove appInfo from old folder
					ApplicationFolderInfo initFolderInfo = (ApplicationFolderInfo) ((UserFolder) source).mInfo;
					mGrid.adjustOrderIdInFolder(initFolderInfo, appInfo.orderId);
					initFolderInfo.remove(appInfo);

					// Add appInfo to applications last position
					appInfo.container = Applications.CONTAINER_APPS;
					appInfo.position = nLastPos;
					appInfo.itemType = Applications.APPS_TYPE_APP;
					mGrid.moveFolderItemToPosition(nLastPos, appInfo);
					mSlideAppsList.add(nLastPos, appInfo);
					mAppsAdapter.notifyDataSetChanged();
				} else {
					ApplicationFolderInfo initFolderInfo = (ApplicationFolderInfo) ((UserFolder) source).mInfo;

					// Remove appInfo from old folder
					mGrid.adjustOrderIdInFolder(initFolderInfo, appInfo.orderId);
					initFolderInfo.remove(appInfo);

					// Add appInfo to new folder
					appInfo.orderId = lastSlideInfo.getSize();
					mGrid.moveFolderItemToFolder((ApplicationInfoEx) dragInfo, lastSlideInfo);
					lastSlideInfo.add((ApplicationInfoEx) dragInfo);

					View lastFolderView = mGrid.getViewAtIndex(nLastPos);
					if (lastFolderView != null) {
						mAppsAdapter.getView(nLastPos, lastFolderView, null);
					}
					mAppsAdapter.notifyDataSetChanged();
				}
			} else {
				// Drag from all application grid to application folder
				ApplicationInfoEx appInfo = (ApplicationInfoEx) dragInfo;
				if (lastView == null) {
					View mClickView = mAppsAdapter.getView(appInfo.position, null, null);
					
					// Remove appInfo from applications
					for (int i = nPos + 1; i < mSlideAppsList.size(); i++) {
						mSlideAppsList.get(i).position--;
					}
					
					mGrid.moveItemToPositionSlide(nPos, nLastPos, appInfo.id, mClickView);
					mSlideAppsList.remove(appInfo);
					appInfo.position = nLastPos;
					appInfo.itemType = Applications.APPS_TYPE_APP;
					mSlideAppsList.add(nLastPos, appInfo);
				} else {
					View nextView = null;
					int index = (mGrid.mCurrentScreen + 1) * mGrid.getPerPageCount();
					if (mAppsAdapter.getCount() > index) {
						nextView = mAppsAdapter.getView(index, null, null);
					}

					appInfo.itemType = Applications.APPS_TYPE_FOLDERAPP;
					appInfo.orderId = lastSlideInfo.getSize();

					// Remove appInfo from applications
					for (int i = nPos + 1; i < mSlideAppsList.size(); i++) {
						mSlideAppsList.get(i).position--;
					}

                    mGrid.moveItemToFolderSlide(appInfo, lastSlideInfo.position, lastSlideInfo.id, nextView);
					mSlideAppsList.remove(appInfo);

					// Add appInfo to folder
					lastSlideInfo.add(appInfo);

					View lastFolderView = mGrid.getViewAtIndex(nLastPos);
					if (lastFolderView != null) {
						if (nLastPos > nPos) {
							mAppsAdapter.getView(nLastPos - 1, lastFolderView, null);
						} else {
							mAppsAdapter.getView(nLastPos, lastFolderView, null);	
						}
					}
				}
			}
		} else {
			// Drag to all application grid
			if (((ItemInfo) dragInfo).itemType == Applications.APPS_TYPE_FOLDERAPP) {
				ApplicationInfoEx appInfo = (ApplicationInfoEx) dragInfo;
				final long folderId = appInfo.container;

				if (lastView == null) {
					nLastPos++;
				}

				// Add appInfo to applications
				mGrid.moveFolderItemToPosition(nLastPos, (ApplicationInfoEx) dragInfo);
				for (int i = nLastPos; i < mSlideAppsList.size(); i++) {
					mSlideAppsList.get(i).position++;
				}
				appInfo.itemType = Applications.APPS_TYPE_APP;
				appInfo.container = Applications.CONTAINER_APPS;
				appInfo.position = nLastPos;
				mSlideAppsList.add(nLastPos, appInfo);

				// Remove appInfo from old folder
				final ApplicationFolderInfo srcFolderInfo = (ApplicationFolderInfo) ((UserFolder) source).mInfo;
				if (srcFolderInfo.itemType == Applications.APPS_TYPE_FOLDER
						&& srcFolderInfo.id == folderId) {
					mGrid.adjustOrderIdInFolder(srcFolderInfo, appInfo.orderId);
					srcFolderInfo.remove(appInfo);
				}
			} else {
				View mClickView = mAppsAdapter.getView(((ItemInfo) dragInfo).position, null, null);
				mGrid.moveItemToPositionSlide(nPos, nLastPos, ((ItemInfo) dragInfo).id, mClickView);
				exchangePos(nPos, nLastPos);
			}
		}
	}

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		synchronized (mSlideAppsList) {
			mGrid.setDragViewIndex(0);
			if (mCurrentScreen == mGrid.mCurrentScreen) {
				moveItemCurrentPage(source, x, y, xOffset, yOffset, dragView, dragInfo);
			} else {
				moveItemDifferentPage(source, x, y, xOffset, yOffset, dragView, dragInfo);
			}
		}
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		if (y > mGridEndYPos) {
			return false;
		}
		synchronized (mSlideAppsList) {
			final int[] mCoordinatesTemp = new int[2];
			mGrid.getLocationOnScreen(mCoordinatesTemp);
			View lastView = mGrid.pointToView(x - mCoordinatesTemp[0], y - mCoordinatesTemp[1]);
			if (lastView == null) {
				Log.w(TAG, "acceptDrop lastView null! x="+x+", y="+y);
				return false;
			} 
			
			int nLastPos = mGrid.getPositionForView(lastView);
			ItemInfo info = mSlideAppsList.get(nLastPos);
			ItemInfo dragItemInfo = (ItemInfo) dragInfo;
	
			if(mbLockApps && nLastPos < mLockAppsNum ){
				return false;
			}
			
			if (info.itemType == Applications.APPS_TYPE_FOLDER
					&& (dragItemInfo.itemType == Applications.APPS_TYPE_APP
							|| dragItemInfo.itemType == Applications.APPS_TYPE_FOLDERAPP)) {
				ApplicationFolderInfo lastSlideInfo = (ApplicationFolderInfo) info;
				if (lastSlideInfo.getSize() >= 12
						&& !lastSlideInfo.contents.contains(dragItemInfo)) {
					Toast.makeText(mLauncher, R.string.folder_is_full, Toast.LENGTH_SHORT).show();
					return false;
				}
				return ((lastView == null) || (dragItemInfo.container != info.id));
			}
			return true;
		}
	}

	@Override
	public Rect estimateDropLocation(DragSource source, int x, int y,
			int xOffset, int yOffset, DragView dragView, Object dragInfo,
			Rect recycle) {
		// TODO Auto-generated method stub
		return null;
	}

	private void updateAppcationList() {
		try {
		synchronized (mSlideAppsList) {
			mSlideAppsList.clear();

			final String[] selection = new String[]{BaseColumns._ID,
					BaseLauncherColumns.TITLE,
					BaseLauncherColumns.INTENT,
					Applications.CONTAINER,
					Applications.POSITION,
					BaseLauncherColumns.ORDERID,
					BaseLauncherColumns.ITEM_TYPE,
					Applications.SYSAPP,
					Applications.PACKAGENAME,
					Applications.INSTALL,
					Applications.STARTNUM};
			final PackageManager pkgManager = mContext.getPackageManager();

			//add none-grouped applications and folders
			Cursor c = mContentResolver.query(Applications.CONTENT_URI_NO_NOTIFICATION,
					selection,
					Applications.CONTAINER + "=?",
					new String[]{String.valueOf(Applications.CONTAINER_APPS)},
					Applications.DEFAULT_SORT_ORDER);
			if (c != null) {
				Log.i(TAG, "updateAppcationList count=" + c.getCount());
				final int idIndex = c.getColumnIndexOrThrow(BaseColumns._ID);
				final int titleIndex = c.getColumnIndexOrThrow(BaseLauncherColumns.TITLE);
				final int intentIndex = c.getColumnIndexOrThrow(BaseLauncherColumns.INTENT);
				final int containerIndex = c.getColumnIndexOrThrow(Applications.CONTAINER);
				final int positionIndex = c.getColumnIndexOrThrow(Applications.POSITION);
				final int itemTypeIndex = c.getColumnIndexOrThrow(BaseLauncherColumns.ITEM_TYPE);
				final int sysAppIndex = c.getColumnIndexOrThrow(Applications.SYSAPP);
				final int pkgNameIndex = c.getColumnIndexOrThrow(Applications.PACKAGENAME);
				final int installIndex = c.getColumnIndexOrThrow(Applications.INSTALL);
				final int startNumIndex = c.getColumnIndexOrThrow(Applications.STARTNUM);

				final String selfPkgName = mContext.getPackageName();
				while (c.moveToNext()) {
					String pkgName = c.getString(pkgNameIndex);
					// Do not display launcher custom theme applications
					if (pkgName != null
							&& (pkgName.startsWith(ThemeUtils.THEME_PACKAGE_TOKEN) ||
									pkgName.equals(selfPkgName))) {
						continue;
					}
					int itemType = c.getInt(itemTypeIndex);
					ItemInfo item;
					final String intentEx = c.getString(intentIndex);

					if (itemType == Applications.APPS_TYPE_APP) {
						item = new ApplicationInfoEx();
						((ApplicationInfoEx) item).isSysApp = (c.getInt(sysAppIndex) == 1);
						((ApplicationInfoEx) item).packageName = pkgName;
						((ApplicationInfoEx) item).installTime = c.getInt(installIndex);
						((ApplicationInfoEx) item).startNum = c.getInt(startNumIndex);
					} else {
						item = new ApplicationFolderInfo();
						// Folder's name is saved in db
						((ApplicationFolderInfo) item).title = c.getString(titleIndex);
					}
					item.id = c.getInt(idIndex);
					item.container = c.getInt(containerIndex);
					item.position = c.getInt(positionIndex);
					item.itemType = itemType;

					if (item.itemType != Applications.APPS_TYPE_FOLDER) {
						final String[] intentInfo = intentEx.split("\\|");
						Intent intent = Utilities.orgnizeAppIconIntent(intentInfo[0], intentInfo[1]);
						final ResolveInfo resolveInfo = pkgManager.resolveActivity(intent, 0);
						if(resolveInfo == null ){
							Log.e(TAG, "updateAppcationList, resolveInfo null! intent="+intent);
							continue;
						}else{
							((ApplicationInfoEx) item).intent = intent;
							((ApplicationInfoEx) item).iconBitmap = mIconCache.getIcon(((ApplicationInfoEx) item).intent);
							((ApplicationInfoEx) item).title = resolveInfo.loadLabel(pkgManager);
						}
					}
					mSlideAppsList.add(item);
				}
				c.close();

				// add all grouped applications to corresponding folder
				if (mSlideAppsList.size() > 0) {
					for (int i = 0; i < mSlideAppsList.size(); i++) {
						final ItemInfo itemInfo = mSlideAppsList.get(i);

						if (itemInfo.itemType != Applications.APPS_TYPE_FOLDER) {
							continue;
						}
						Cursor subAppCursor = mContentResolver.query(Applications.CONTENT_URI_NO_NOTIFICATION,
								selection,
								Applications.CONTAINER + "=?",
								new String[] { String.valueOf(itemInfo.id) },
								BaseLauncherColumns.DEFAULT_SORT_ORDER_IN_FOLDER);
						if (subAppCursor != null) {
							final int orderIdIndex = subAppCursor.getColumnIndexOrThrow(BaseLauncherColumns.ORDERID);
							ApplicationFolderInfo folderInfo = (ApplicationFolderInfo) itemInfo;
							if (folderInfo.contents == null) {
								folderInfo.contents = new ArrayList<ApplicationInfoEx>();
							}
							while (subAppCursor.moveToNext()) {
								ApplicationInfoEx subAppInfoEx = new ApplicationInfoEx();
								final String intentEx = subAppCursor.getString(intentIndex);

								subAppInfoEx.id = subAppCursor.getInt(idIndex);
								subAppInfoEx.container = subAppCursor.getInt(containerIndex);
								subAppInfoEx.position = subAppCursor.getInt(positionIndex);
								subAppInfoEx.orderId = subAppCursor.getInt(orderIdIndex);
								subAppInfoEx.itemType = subAppCursor.getInt(itemTypeIndex);
								subAppInfoEx.isSysApp = (subAppCursor.getInt(sysAppIndex) == 1);
								subAppInfoEx.packageName = subAppCursor.getString(pkgNameIndex);
								subAppInfoEx.installTime = subAppCursor.getInt(installIndex);
								subAppInfoEx.startNum = subAppCursor.getInt(startNumIndex);

								if (subAppInfoEx.itemType != Applications.APPS_TYPE_FOLDER) {
									final String[] intentInfo = intentEx.split("\\|");
									Intent intent = Utilities.orgnizeAppIconIntent(intentInfo[0], intentInfo[1]);
									final ResolveInfo resolveInfo = pkgManager.resolveActivity(intent, 0);
									if(resolveInfo == null ){
										Log.e(TAG, "updateAppcationList, APPS_TYPE_FOLDER, resolveInfo null! intent="+intent);
										continue;
									}else{
										subAppInfoEx.intent = intent;
										subAppInfoEx.iconBitmap = mIconCache.getIcon(subAppInfoEx.intent);
										subAppInfoEx.title = resolveInfo.loadLabel(pkgManager);
									}
								}
								folderInfo.contents.add(subAppInfoEx);
							}
							subAppCursor.close();
						}
					}
				}
			}
		}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean checkAppExist(String packageName) {
		if (packageName == null || packageName.length() == 0) {
			return false;
		}
		try {
			mLauncher.getPackageManager().getApplicationInfo(
					packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	@Override
	public void scrollLeft() {
		if (mGrid.mCurrentScreen > 0) {
			mGrid.snapToScreen(mGrid.mCurrentScreen - 1);
		}
	}

	@Override
	public void scrollRight() {
		if (mGrid.mCurrentScreen < (mGrid.mTotalScreens - 1)) {
			mGrid.snapToScreen(mGrid.mCurrentScreen + 1);
		}
	}

	@Override
	public void switchScreenMode(boolean bIsFullScreen, int paddingTop) {
		// TODO Auto-generated method stub
		if (bIsFullScreen) {
			setPadding(getPaddingLeft(), getPaddingTop() + paddingTop, getPaddingRight(), getPaddingBottom());
		} else {
			setPadding(getPaddingLeft(), getPaddingTop() - paddingTop, getPaddingRight(), getPaddingBottom());
		}
	}

	@Override
	public AdapterView<?> getGridView() {
		// TODO Auto-generated method stub
		return mGrid;
	}

	@Override
	public ScreenIndicator getScreenIndicator() {
		// TODO Auto-generated method stub
		return mScreenIndicator;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mGrid.getPerPageCount();
	}

	@Override
	public void applyTheme() {
		// TODO Auto-generated method stub
		ThemeManager themeMgr = ThemeManager.getInstance();
		int color = themeMgr.loadColor("allapp_background");
		if(color >= 0){
			setBackgroundColor(color);
		}
		
		updateAllData();
	}	
}

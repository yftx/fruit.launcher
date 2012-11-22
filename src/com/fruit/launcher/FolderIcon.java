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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

//import com.fruit.launcher.theme.ThemeManager;
import com.fruit.launcher.LauncherSettings.Applications;
import com.fruit.launcher.LauncherSettings.BaseLauncherColumns;
import com.fruit.launcher.LauncherSettings.Favorites;

/**
 * An icon that can appear on in the workspace representing an {@link UserFolder}.
 */
public class FolderIcon extends BubbleTextView implements DropTarget {

	private UserFolderInfo mUserFolderInfo;
	private ApplicationFolderInfo mAppFolderInfo;
	private Launcher mLauncher;
	private IconCache mIconCache;
	private Bitmap mCloseIcon;
	private Bitmap mOpenIcon;
	private LayerDrawable mFolderIcon;
	private LayerDrawable mFolderOpenIcon;

	public FolderIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		mIconCache =
			((LauncherApplication) context.getApplicationContext()).getIconCache();
	}

	public FolderIcon(Context context) {
		super(context);
		mIconCache =
			((LauncherApplication) context.getApplicationContext()).getIconCache();
	}

	static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
			FolderInfo folderInfo) {
		FolderIcon icon = (FolderIcon) LayoutInflater.from(launcher).inflate(resId, group, false);

		if (folderInfo instanceof UserFolderInfo) {
			icon.mUserFolderInfo = (UserFolderInfo) folderInfo;
		} else if (folderInfo instanceof ApplicationFolderInfo) {
			icon.mAppFolderInfo = (ApplicationFolderInfo) folderInfo;
		}

		IconCache iconCache = ((LauncherApplication) launcher.getApplicationContext()).getIconCache();
		
//    	int w = 0;
//    	int h = 0;

    	icon.mCloseIcon = 	iconCache.getFolderLocalIcon(true);
//    	Bitmap closeicon = 	iconCache.getFolderLocalIcon(true);
//    	w = closeicon.getWidth();
//    	h = closeicon.getHeight();
//    	if (w!=96 && h!=96) {
//    		icon.mCloseIcon = Utilities.scaleBitmap(closeicon, 96.0f/w, 96.0f/h);
//    	} else {
//    		icon.mCloseIcon = closeicon;
//    	}

    	icon.mOpenIcon = iconCache.getFolderLocalIcon(false);
//		Bitmap openicon = iconCache.getFolderLocalIcon(false);
//    	w = openicon.getWidth();
//    	h = openicon.getHeight();
//    	if (w!=96 && h!=96) {
//    		icon.mOpenIcon = Utilities.scaleBitmap(closeicon, 96.0f/w, 96.0f/h);
//    	} else {
//    		icon.mOpenIcon = openicon;
//    	}
		
		icon.updateFolderIcon();
		icon.setCompoundDrawablesWithIntrinsicBounds(null, icon.mFolderIcon, null, null);
		folderInfo.folderIcon = icon;
		icon.setText(folderInfo.title);
		icon.setTag(folderInfo);
		icon.setOnClickListener(launcher);
		icon.mLauncher = launcher;
		
    	boolean shadow = Configurator.getBooleanConfig(launcher, Configurator.CONFIG_IDLEICONSHADOW, false);
    	icon.setDrawShadow(shadow);

		return icon;
	}

	@SuppressWarnings("deprecation")
	void updateFolderIcon() {
		// TODO Auto-generated method stub
		LayerDrawable ld = null;
		LayerDrawable ldOpen = null;
		if (mUserFolderInfo != null) {
			Bitmap bm = mCloseIcon;
			Bitmap bmOpen = mOpenIcon;
			
			if (mCloseIcon == null) {
				mCloseIcon = mIconCache.getFolderLocalIcon(true);	
				bm = mCloseIcon;
			}
			if (mOpenIcon == null) {
				mOpenIcon = mIconCache.getFolderLocalIcon(false);		
				bmOpen = mOpenIcon;
			}
			bm.setDensity(Bitmap.DENSITY_NONE);
			bmOpen.setDensity(Bitmap.DENSITY_NONE);
			
			//folder close state icon
			ld = createUserFloderLayer(bm);
			//floder open state icon
			ldOpen = createUserFloderLayer(bmOpen);
		}
		mFolderIcon = ld;
		mFolderOpenIcon = ldOpen;
	}
	
	private LayerDrawable createUserFloderLayer(Bitmap bm){
		if(mUserFolderInfo == null){
			return null;
		}
		
		LayerDrawable ld = null;
		final int count = mUserFolderInfo.getSize();
		Drawable[] array = new Drawable[Math.min(4, count) + 1];
		array[0] = new BitmapDrawable(mContext.getResources(),bm);
		if (count > 0) {
			for (int i = 0; i < Math.min(4, count); i++) {
				ShortcutInfo appInfo = mUserFolderInfo.contents.get(i);
				//Bitmap photo = resizeBitmap(bm, appInfo.getIcon(mIconCache));
				Bitmap photo = resizeBitmap(bm, Utilities.createCompoundBitmapEx(appInfo.title.toString(), appInfo.getIcon(mIconCache)));
				photo.setDensity(Bitmap.DENSITY_NONE);
				array[i + 1] = new BitmapDrawable(mContext.getResources(),photo);
			}

			ld = new LayerDrawable(array);
			if (count >= 1) { 
				ld.setLayerInset(1, 8, 8, 1 + bm.getWidth() / 2, 1 + bm.getHeight() / 2);
			}

			if (count >= 2) {
				ld.setLayerInset(2, 1 + bm.getWidth() / 2, 8, 8, 1 + bm.getHeight() / 2);
			}

			if (count >= 3) {
				ld.setLayerInset(3, 8, 1 + bm.getHeight() / 2, 1 + bm.getWidth() / 2, 8);
			}

			if (count >= 4) {
				ld.setLayerInset(4, 1 + bm.getWidth() / 2, 1 + bm.getWidth() / 2, 8, 8);
			}
		} else {
			ld = new LayerDrawable(array);
		}
		
		return ld;
	}

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

	public void addItemInfo(ShortcutInfo item) {
		if (mUserFolderInfo != null) {
			mUserFolderInfo.add(item);
		}
	}

	public void addItemInfo(ApplicationInfoEx item) {
		if (mAppFolderInfo != null) {
			mAppFolderInfo.add(item);
		}
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		final ItemInfo item = (ItemInfo) dragInfo;
		final int itemType = item.itemType;

		if (mUserFolderInfo != null) {
			// Applications folder can not move into user folder
			if (itemType == Applications.APPS_TYPE_FOLDER || 
					itemType == Favorites.ITEM_TYPE_USER_FOLDER) {
				return false;
			}
			if (itemType == Applications.APPS_TYPE_APP
					|| itemType == Applications.APPS_TYPE_FOLDERAPP) {
				Intent dragIntent = ((ApplicationInfoEx) item).intent;
				for (int i = 0; i < mUserFolderInfo.getSize(); i++) {
					Intent appIntent = mUserFolderInfo.contents.get(i).intent;
					if(appIntent !=null && appIntent.getComponent() != null
							&& appIntent.getComponent().getPackageName() != null){
						if (appIntent.getComponent().toString().equals(dragIntent.getComponent().toString())) {
							Toast.makeText(mContext, R.string.folder_contain_item, Toast.LENGTH_SHORT).show();
							return false;
					}
				}
			}
			} else if (item instanceof ShortcutInfo && itemType == BaseLauncherColumns.ITEM_TYPE_APPLICATION) {
				if (item.container != mUserFolderInfo.id) {
					Intent dragIntent = ((ShortcutInfo) item).intent;
					for (int i = 0; i < mUserFolderInfo.contents.size(); i++) {
						Intent appIntent = mUserFolderInfo.contents.get(i).intent;
						ComponentName itemComName = appIntent.getComponent();
						if (itemComName != null
								&& itemComName.toString().equals(dragIntent.getComponent().toString())) {
							Toast.makeText(mContext, R.string.folder_contain_item, Toast.LENGTH_SHORT).show();
							return false;
						}
					}
				}
			}
	
			if (mUserFolderInfo.getSize() >= 12
					&& !mUserFolderInfo.contents.contains(item)) {
				Toast.makeText(mContext, R.string.folder_is_full, Toast.LENGTH_SHORT).show();
				return false;
			}

			return (itemType == BaseLauncherColumns.ITEM_TYPE_APPLICATION
						|| itemType == BaseLauncherColumns.ITEM_TYPE_SHORTCUT
						|| itemType == Applications.APPS_TYPE_APP
						|| itemType == Applications.APPS_TYPE_FOLDERAPP) 
					&& item.container != mUserFolderInfo.id;
		}
		// Applications folder's acceptDrop processing is in AllApps2D_Slide
		return false;
	}

	@Override
	public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, Rect recycle) {
		return null;
	}

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		ItemInfo dragItem = (ItemInfo) dragInfo;
		ShortcutInfo item;

		if (dragItem.itemType == Applications.APPS_TYPE_APP
				|| dragItem.itemType == Applications.APPS_TYPE_FOLDERAPP) {
			// Came from all applications -- make a copy as shortcut info
			//item = ((ApplicationInfo)dragInfo).makeShortcut();
			ApplicationInfoEx infoEx = (ApplicationInfoEx) dragInfo;

			item = mLauncher.getLauncherModel().getShortcutInfo(getContext().getPackageManager(),
					infoEx.intent, getContext());
			item.setActivity(infoEx.intent.getComponent(),
					Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			item.container = ItemInfo.NO_ID;
		} else {
			item = (ShortcutInfo) dragInfo;
		}
		if (mUserFolderInfo != null) {
			if (item.container >= 0) {
				mLauncher.removeItemFromFolder(item);
			}
			item.orderId = mUserFolderInfo.getSize();
			mUserFolderInfo.add(item);
			LauncherModel.addOrMoveItemInDatabase(mLauncher, item, mUserFolderInfo.id, 0, 0, 0);

			// Refresh folder icon with new contents
			if (mUserFolderInfo.getSize() < 5) { 
				updateFolderIcon();
				setCompoundDrawablesWithIntrinsicBounds(null, mFolderIcon, null, null);
			}
		}
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		setCompoundDrawablesWithIntrinsicBounds(null, mFolderOpenIcon, null, null);
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		setCompoundDrawablesWithIntrinsicBounds(null, mFolderIcon, null, null);
	}
	
	void refreshFolderIcon() {
		mCloseIcon = null;
		mOpenIcon = null;
		updateFolderIcon();
		setCompoundDrawablesWithIntrinsicBounds(null, mFolderIcon, null, null);
	}
	
	public void onFolderOpen(){
		if(mFolderOpenIcon != null){
			setCompoundDrawablesWithIntrinsicBounds(null, mFolderOpenIcon, null, null);
		}
	}
	
	public void onFolderClose(){
		if(mFolderIcon != null){
			setCompoundDrawablesWithIntrinsicBounds(null, mFolderIcon, null, null);
		}
	}
}
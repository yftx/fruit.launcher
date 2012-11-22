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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
//import android.content.res.Resources;
import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.HashMap;
//import java.util.Random;

import com.fruit.launcher.theme.ThemeManager;

/**
 * Cache of application icons. Icons can be made from any thread.
 */
public class IconCache {

	private static final int INITIAL_ICON_CACHE_CAPACITY = 50;
	private static final int INITIAL_LOCALICON_CACHE_CAPACITY = 25;

	// private Bitmap[] mAppBgIcons;

	private static class CacheEntry {
		public Bitmap icon;
		public String title;
		public Bitmap titleBitmap;
	}

	private static class CacheLocalEntry {
		public Drawable drawable;
		public String title;
		public boolean isDefault;
	}

	private final Bitmap mDefaultIcon;
	private final LauncherApplication mContext;
	private final PackageManager mPackageManager;
	private final Utilities.BubbleText mBubble;
	private final HashMap<ComponentName, CacheEntry> mCache = new HashMap<ComponentName, CacheEntry>(
			INITIAL_ICON_CACHE_CAPACITY);

	private final HashMap<Integer, CacheLocalEntry> mCacheLocal = new HashMap<Integer, CacheLocalEntry>(
			INITIAL_LOCALICON_CACHE_CAPACITY);

	private final ThemeManager mThemeMgr;

	public IconCache(LauncherApplication context) {
		mContext = context;
		mPackageManager = context.getPackageManager();
		mBubble = new Utilities.BubbleText(context);
		mDefaultIcon = makeDefaultIcon();
		mThemeMgr = ThemeManager.getInstance();
	}

	// private void applyAppBgIcons() {
	// // TODO Auto-generated method stub
	// if (mAppBgIcons != null) {
	// for (int i = 0; i < mAppBgIcons.length; i++) {
	// mAppBgIcons[i] = null;
	// }
	// mAppBgIcons = null;
	// }
	//
	// Bitmap[] bitmaps = new Bitmap[10];
	// int i = 0;
	// int size = 0;
	// for (i = 0; i < 10; i++) {
	// String resName = "theme_icon_bg_" + i;
	// Bitmap tmpBmp =
	// Utilities.drawable2bmp(mContext.getResources().getDrawable(R.drawable.theme_icon_bg_1));
	// if (tmpBmp != null) {
	// bitmaps[i] = tmpBmp;
	// size++;
	// } else {
	// // As tmpBmp is null, think as no more bitmap can be loaded
	// break;
	// }
	// }
	//
	// mAppBgIcons = new Bitmap[size];
	// for (i = 0; i < size; i++) {
	// mAppBgIcons[i] = bitmaps[i];
	// }
	//
	// }

	private Bitmap makeDefaultIcon() {
		Drawable d = mPackageManager.getDefaultActivityIcon();
		Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1),
				Math.max(d.getIntrinsicHeight(), 1), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		d.setBounds(0, 0, b.getWidth(), b.getHeight());
		d.draw(c);
		return b;
	}

	/**
	 * Remove any records for the supplied ComponentName.
	 */
	public void remove(ComponentName componentName) {
		synchronized (mCache) {
			mCache.remove(componentName);
		}
	}

	public void removeLocal(int drawableId) {
		synchronized (mCache) {
			mCacheLocal.remove(new Integer(drawableId));
		}
	}

	/**
	 * Empty out the cache.
	 */
	public void flush() {
		synchronized (mCache) {
			mCache.clear();
			mCacheLocal.clear();
		}
	}

	/**
	 * Fill in "application" with the icon and label for "info."
	 */
	public void getTitleAndIcon(ApplicationInfo application, ResolveInfo info) {
		synchronized (mCache) {
			CacheEntry entry = cacheLocked(application.componentName, info);
			if (entry.titleBitmap == null) {
				entry.titleBitmap = mBubble.createTextBitmap(entry.title);
			}

			application.title = entry.title;
			application.titleBitmap = entry.titleBitmap;
			application.iconBitmap = entry.icon;
		}
	}

	public Bitmap getIcon(Intent intent) {
		synchronized (mCache) {
			final ResolveInfo resolveInfo = mPackageManager.resolveActivity(
					intent, 0);
			ComponentName component = intent.getComponent();

			if (resolveInfo == null || component == null) {
				return mDefaultIcon;
			}

			CacheEntry entry = cacheLocked(component, resolveInfo);
			return entry.icon;
		}
	}

	public Bitmap getIcon(ComponentName component, ResolveInfo resolveInfo) {
		synchronized (mCache) {
			if (resolveInfo == null || component == null) {
				return null;
			}

			CacheEntry entry = cacheLocked(component, resolveInfo);
			return entry.icon;
		}
	}

	public Drawable getLocalIcon(int drawableId, String resName) {
		synchronized (mCache) {
			if (resName == null || drawableId < 0) {
				return null;
			}

			CacheLocalEntry entry = cacheLocalLocked(drawableId, resName);
			return entry.drawable;
		}
	}

	public Bitmap getFolderLocalIcon(boolean isFolserClose) {
		synchronized (mCache) {
			CacheLocalEntry entry = null;
			if (isFolserClose) {
				entry = cacheLocalLocked(R.drawable.ic_launcher_folder,
						"folder_close");
			} else {
				entry = cacheLocalLocked(R.drawable.ic_launcher_folder_open,
						"folder_open");
			}

			if (entry.isDefault) {
				return Utilities.createIconBitmap(entry.drawable, mContext);
			} else {
				BitmapDrawable bd = (BitmapDrawable) entry.drawable;
				return bd.getBitmap();
			}
		}
	}

	// public Bitmap getFolderLocalIcon(boolean isFolserClose) {
	// synchronized (mCache) {
	// CacheLocalEntry entry = null;
	// if(isFolserClose){
	// entry = cacheLocalLocked(R.drawable.ic_launcher_folder,
	// "ic_launcher_folder"); //yfzhao
	// }else{
	// entry = cacheLocalLocked(R.drawable.ic_launcher_folder_open,
	// "ic_launcher_folder_open");
	// }
	//
	// Bitmap bmp = null;
	// Bitmap icon = null;
	//
	// if(entry.isDefault){
	// bmp = Utilities.createIconBitmap(entry.drawable, mContext);
	// }else{
	// BitmapDrawable bd = (BitmapDrawable) entry.drawable;
	// bmp = bd.getBitmap();
	// }
	//
	// if (bmp != null)
	// icon = Utilities.scaleBitmap(bmp, 96.0f/bmp.getWidth(),
	// 96.0f/bmp.getHeight());
	//
	// return icon;
	// }
	// }

	private CacheEntry cacheLocked(ComponentName componentName, ResolveInfo info) {
		CacheEntry entry = mCache.get(componentName);
		if (entry == null) {
			entry = new CacheEntry();

			mCache.put(componentName, entry);

			entry.title = info.loadLabel(mPackageManager).toString();
			if (entry.title == null) {
				entry.title = info.activityInfo.name;
			}

			// Convert application package name to fixed format
			final String resName = info.activityInfo.name.replace('.', '_')
					.toLowerCase();
			Bitmap icon = mThemeMgr.loadBitmap(resName);
			// If theme manager returned null, use application's original icon
			if (icon == null) {
				Bitmap origIcon = Utilities.createIconBitmap(
						info.activityInfo.loadIcon(mPackageManager), mContext);
				if (!mThemeMgr.isDefaultTheme()) {
					// If current is custom theme, combine the icon with special
					// bg
					Bitmap newIcon = null;
					if (Utilities.isSystemApplication(mContext,
							info.activityInfo.packageName)
							&& mContext.getResources().getBoolean(
									R.bool.config_systemIcon_unuseTheme)) {
						newIcon = origIcon;
					} else {
						newIcon = Utilities.createCompoundBitmap(
								mThemeMgr.getAppBgIcon(info.activityInfo.name),
								origIcon);
					}
					if (newIcon != null) {
						entry.icon = newIcon;
					} else {
						entry.icon = origIcon;
					}
				} else {
					entry.icon = origIcon;
				}
			} else {
				entry.icon = icon;
			}

		}

		return entry;
	}

	// private CacheEntry cacheLocked(ComponentName componentName, ResolveInfo
	// info) {
	// CacheEntry entry = mCache.get(componentName);
	// if (entry == null) {
	// entry = new CacheEntry();
	//
	// mCache.put(componentName, entry);
	//
	// entry.title = info.loadLabel(mPackageManager).toString();
	// if (entry.title == null) {
	// entry.title = info.activityInfo.name;
	// }
	//
	// // Convert application package name to fixed format
	// final String resName = info.activityInfo.name.replace('.',
	// '_').toLowerCase();
	// Bitmap icon = mThemeMgr.loadBitmap(resName);
	// // If theme manager returned null, use application's original icon
	// if (icon == null) {
	// Bitmap origIcon = Utilities.createIconBitmap(
	// info.activityInfo.loadIcon(mPackageManager), mContext);
	// // if (!mThemeMgr.isDefaultTheme()) {
	// // // If current is custom theme, combine the icon with special bg
	// // Bitmap newIcon = null;
	// // if(Utilities.isSystemApplication(mContext,
	// info.activityInfo.packageName) &&
	// //
	// mContext.getResources().getBoolean(R.bool.config_systemIcon_unuseTheme)){
	// // newIcon = origIcon;
	// // }else{
	// // newIcon =
	// origIcon;//Utilities.createCompoundBitmap(mThemeMgr.getAppBgIcon(info.activityInfo.name),
	// origIcon);
	// // }
	// // if (newIcon != null) {
	// // entry.icon = newIcon;
	// // } else {
	// // entry.icon = origIcon;
	// // }
	// // } else {
	// // entry.icon = origIcon;
	// // }
	//
	// // float sxo = 1.0f;
	// // float syo = 1.0f;
	// // Bitmap oldIcon = origIcon;//entry.icon;
	// // if (oldIcon != null) {
	// // if (oldIcon.getWidth() > 85)
	// // sxo = 1.0f / (oldIcon.getWidth()/85.0f); //0.86f;
	// //
	// // if (oldIcon.getHeight() > 85)
	// // syo = 1.0f / (oldIcon.getWidth()/85.0f);//0.86f;
	// // }
	// // Bitmap theIcon = Utilities.scaleBitmap(oldIcon, sxo, syo);
	// //
	// // final ThemeManager mThemeMgr;
	// // mThemeMgr = ThemeManager.getInstance();
	// // Bitmap newIcon =
	// Utilities.createCompoundBitmap(mThemeMgr.getRandomAppBgIcon(), theIcon);
	// //=============================
	// //default icon
	// Resources r = mContext.getResources();
	// String infoName = info.activityInfo.name;
	// Bitmap bmp = null;
	// if (infoName.equals("com.android.browser.BrowserActivity")) {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_web);
	// } else if (infoName.equals("com.huaqin.filemanager.FileManager")) {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_floder);
	// } else if
	// (infoName.equals("com.uucun105517.android.cms.activity.MarketLoginAndRegisterActivity"))
	// {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_store);
	// } else if (infoName.equals("com.skymobi.appstore.activity.LogoActivity"))
	// {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_store);
	// } else if (infoName.equals("com.mediatek.StkSelection.StkSelection")) {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_sim);
	// } else if (infoName.equals("com.mediatek.FMRadio.FMRadioActivity")) {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_radio);
	// } else if (infoName.equals("com.huaqin.videoplayer")) {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_media);
	// //} else if
	// (infoName.equals("com.mediatek.mtkvideoplayer.GalleryPicker")) {
	// // bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_media);
	// } else if (infoName.equals("com.android.soundrecorder.SoundRecorder")) {
	// bmp = BitmapFactory.decodeResource(r,
	// R.drawable.com_android_soundrecorder_soundrecorder);
	// } else if (infoName.equals("com.android.settings.Settings")) {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_settings);
	// } else if (infoName.equals("com.android.quicksearchbox.SearchActivity"))
	// {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_search);
	// } else if
	// (infoName.equals("com.android.providers.downloads.ui.DownloadList")) {
	// bmp = BitmapFactory.decodeResource(r,
	// R.drawable.com_android_providers_downloads_ui_downloadlist);
	// } else if (infoName.equals("com.android.music.MusicBrowserActivity")) {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_music);
	// } else if (infoName.equals("com.android.mms.ui.ConversationList")) {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_messages);
	// } else if (infoName.equals("com.cooliris.media.Gallery")) {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_images);
	// } else if (infoName.equals("com.android.email.activity.Welcome")) {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_mail);
	// } else if (infoName.equals("com.android.deskclock.DeskClock")) {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_clock);
	// } else if
	// (infoName.equals("com.android.contacts.DialtactsContactsEntryActivity"))
	// {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_contacts);
	// } else if (infoName.equals("com.android.contacts.DialtactsActivity")) {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_phone);
	// } else if
	// (infoName.equals("com.android.contacts.DialtactsCallLogEntryActivity")) {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_calllogs);
	// } else if (infoName.equals("com.baidu.BaiduMap.BaiduMap")) {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_maps);
	// } else if (infoName.equals("com.mediatek.camera.Camera")) {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_camera);
	// } else if (infoName.equals("com.android.calendar.LaunchActivity")) {
	// bmp = BitmapFactory.decodeResource(r, R.drawable.mainmenu_icon_calendar);
	// } else if (infoName.equals("com.android.calculator2.Calculator")) {
	// bmp = BitmapFactory.decodeResource(r,
	// R.drawable.mainmenu_icon_calculator);
	// } else {
	// bmp = Utilities.createCompoundBitmapEx(info.activityInfo.name, origIcon);
	// }
	// //bmp = BitmapFactory.decodeResource(r,
	// R.drawable.mainmenu_icon_calculator);
	// entry.icon =
	// bmp;//yfzhao//Utilities.createIconBitmap(getFullResIcon(info), mContext);
	//
	//
	// //==================
	//
	//
	//
	//
	// } else {
	// entry.icon = icon;
	// }
	// }
	//
	// return entry;
	// }

	private CacheLocalEntry cacheLocalLocked(int drawableId, String resName) {
		Integer localId = new Integer(drawableId);
		CacheLocalEntry entry = mCacheLocal.get(localId);
		if (entry == null) {
			entry = new CacheLocalEntry();

			mCacheLocal.put(localId, entry);

			entry.title = new String(resName);

			// Convert application package name to fixed format
			Drawable icon = mThemeMgr.loadDrawable(resName);
			// If theme manager returned null, use application's original icon
			if (icon == null) {
				// If current is custom theme, combine the icon with special bg
				// entry.drawable =
				// mContext.getResources().getDrawable(drawableId);
				entry.drawable = getDrawable(drawableId, resName);
				entry.isDefault = true;
			} else {
				entry.drawable = icon;
				entry.isDefault = false;
			}
		}
		return entry;
	}

	private Drawable getDrawable(int drawableId, String resName) {
		Drawable drawable = Configurator.getConfigPackageDrawable(mContext,
				resName);
		if (drawable == null) {
			drawable = mContext.getResources().getDrawable(drawableId);
		}

		return drawable;
	}

}

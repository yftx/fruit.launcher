package com.fruit.launcher.theme;

import java.io.IOException;
import java.util.Random;

import com.fruit.launcher.Configurator;
import com.fruit.launcher.Launcher;
import com.fruit.launcher.LauncherApplication;
import com.fruit.launcher.R;
import com.fruit.launcher.Utilities;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;

public final class ThemeManager {

	static final String TAG = "ThemeManager";
	static final boolean DEBUG = true;

	private static ThemeManager sInstance;
	private final Context mContext;
	private Launcher mLauncher;
	private Bitmap[] mAppBgIcons;

	private boolean mIsDefaultTheme;

	private ShareResourceLoader mDefaultLoader;
	private ShareResourceLoader mWallpaperLoader;

	private Handler mHandler;
	private ContentObserver mThemeChangeObserver;

	enum ThemeType {
		THEME_DEFALT, THEME_ICON
	}

	public ThemeManager() {
		mContext = LauncherApplication.getContext();

		if (DEBUG)
			Log.e(TAG, "ThemeManager on create");
		if (Utilities.isPackageInstall(mContext,
				ThemeUtils.THEME_MANAGER_PACKAGE)) {
			updateTheme();
			updateWallpaperTheme();
		} else {
			applyConfigTheme();
		}

		applyAppBgIcons(mDefaultLoader);

		mHandler = new Handler();
		mThemeChangeObserver = new ThemeObserver(mHandler);

		if (DEBUG) {
			Log.d(TAG,
					"ThemeManager, thempkg="
							+ (mDefaultLoader != null ? mDefaultLoader
									.getResourcePkgName() : "not set theme"));
		}
		mLauncher = null;
	}

	public static ThemeManager getInstance() {
		if (sInstance == null) {
			sInstance = new ThemeManager();
		}
		return sInstance;
	}

	public void startListener() {
		if (sInstance == null) {
			return;
		}
		mContext.getContentResolver().registerContentObserver(
				ThemeUtils.CONTENT_URI, true, mThemeChangeObserver);
	}

	public void stopListener() {
		if (sInstance == null) {
			return;
		}
		mContext.getContentResolver().unregisterContentObserver(
				mThemeChangeObserver);
	}

	private void applyTheme() {
		if (DEBUG)
			Log.d(TAG, "applyTheme");
		// apply theme for icon
		applyAppBgIcons(mDefaultLoader);
		if (mLauncher != null) {
			mLauncher.getIconCache().flush();
			mLauncher.applyTheme();
		}
	}

	private void applyWallpaper() {
		if (DEBUG)
			Log.d(TAG, "applyWallpaper");
		try {
			WallpaperManager wpm = (WallpaperManager) mContext
					.getSystemService(Context.WALLPAPER_SERVICE);

			Bitmap themeWallpaper = null;
			if (mWallpaperLoader != null) {
				themeWallpaper = mWallpaperLoader.loadBitmap("theme_wallpaper");
			}
			if (themeWallpaper != null) {
				wpm.setBitmap(themeWallpaper);
			} else {
				wpm.setResource(R.drawable.wallpaper_default);
				Log.w(TAG, "applyWallpaper bitmap null, aplly defalt!");
			}
		} catch (IOException e) {
			Log.e(TAG, "applyWallpaper fail! IOException");
		}
	}

	private void applyAppBgIcons(ShareResourceLoader loader) {
		// TODO Auto-generated method stub
		if (mAppBgIcons != null) {
			for (int i = 0; i < mAppBgIcons.length; i++) {
				mAppBgIcons[i] = null;
			}
			mAppBgIcons = null;
		}

		if (loader != null) {
			Bitmap[] bitmaps = new Bitmap[ThemeUtils.APP_ICON_BG_MAX_COUNT];
			int i = 0;
			int size = 0;
			for (i = 0; i < ThemeUtils.APP_ICON_BG_MAX_COUNT; i++) {
				String resName = "theme_icon_bg_" + i;
				Bitmap tmpBmp = loader.loadBitmap(resName);
				if (tmpBmp != null) {
					bitmaps[i] = tmpBmp;
					size++;
				} else {
					// As tmpBmp is null, think as no more bitmap can be loaded
					break;
				}
			}

			mAppBgIcons = new Bitmap[size];
			for (i = 0; i < size; i++) {
				mAppBgIcons[i] = bitmaps[i];
			}
		}
	}

	public boolean isDefaultTheme() {
		return mIsDefaultTheme;
	}

	public Drawable loadDrawable(String resName) {
		// TODO Auto-generated method stub
		final ShareResourceLoader loader = mDefaultLoader;
		if (loader != null) {
			return loader.loadDrawable(resName);
		}
		return null;
	}

	public Bitmap loadBitmap(String resName) {
		// TODO Auto-generated method stub
		final ShareResourceLoader loader = mDefaultLoader;
		if (loader != null) {
			return loader.loadBitmap(resName);
		}
		return null;
	}

	public int loadColor(String resName) {
		// TODO Auto-generated method stub
		final ShareResourceLoader loader = mDefaultLoader;
		if (loader != null) {
			return loader.loadColor(resName);
		}
		return -1;
	}

	public Bitmap[] loadFolderIcons() {
		// TODO Auto-generated method stub
		final ShareResourceLoader loader = mDefaultLoader;
		if (loader == null) {
			return null;
		}

		Bitmap[] folderIcons = new Bitmap[2];

		folderIcons[0] = loader.loadBitmap("folder_bg");
		folderIcons[1] = loader.loadBitmap("folder_shade_close");
		return folderIcons;
	}

	// drawable not clone when set to imageview, so it must be recreate
	public Drawable loadScreenIndicatorIcon() {
		// TODO Auto-generated method stub
		final Resources res = mContext.getResources();
		final ShareResourceLoader loader = mDefaultLoader;
		Drawable dot = null;
		if (loader != null) {
			dot = loader.loadDrawable("nav_screen");
		}
		if (dot == null) {
			dot = res.getDrawable(R.drawable.screen_indicator);
		}

		return dot;
	}

	public void setLauncher(Launcher launcher) {
		if (launcher != null) {
			mLauncher = launcher;
		}
	}

	public Bitmap getRandomAppBgIcon(String className) {
		return getAppBgIcon(className);
	}

	public Bitmap getAppBgIcon(String className) {
		if (mAppBgIcons == null || mAppBgIcons.length == 0) {
			return null;
		}

		int length = mAppBgIcons.length;
		int index = -1;

		if (length == 1) {
			return mAppBgIcons[0];
		} else if (className == null) {
			index = (new Random()).nextInt(length);
		} else {			
			index = Math.max(0, Math.abs(className.hashCode()) % length);
			Log.d(TAG, "getAppBgIcon:hashcode="+className.hashCode()+",len="+length+",index="+index);
		}

		if (index < 0 || index > mAppBgIcons.length) {
			return null;
		}
		return mAppBgIcons[index];
	}

	public String getThemePkgName() {
		String pkg = null;
		if (mDefaultLoader != null) {
			pkg = mDefaultLoader.getResourcePkgName();
		}
		return pkg;
	}

	public void applyDefaultTheme() {
		mDefaultLoader = null;
		mWallpaperLoader = null;
		applyTheme();
		applyWallpaper();
	}

	private String getCurrentTheme(String slectionType) {
		String theme = null;
		Cursor cursor = null;
		try {
			cursor = mContext.getContentResolver().query(
					ThemeUtils.CONTENT_URI, null, slectionType, null, null);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		if (cursor != null) {
			cursor.moveToFirst();
			theme = cursor.getString(cursor
					.getColumnIndexOrThrow(ThemeUtils.PACKAGE_NAME));
			cursor.close();
		}

		if (theme != null && theme.equals(ThemeUtils.DEFAULT_THEME_PACKAGENAME)) {
			theme = null;
		}

		if (DEBUG)
			Log.d(TAG, "getCurTheme , theme=" + theme);

		return theme;
	}

	private boolean applyConfigTheme() {
		String defThemeName = Configurator.getStringConfig(mContext,
				Configurator.CONFIG_DEFAULTTHME);
		if (defThemeName != null
				&& Utilities.isPackageInstall(mContext, defThemeName)) {
			try {
				mDefaultLoader = new ShareResourceLoader(mContext, defThemeName);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mDefaultLoader = null;
			}
		}
		return mDefaultLoader != null;
	}

	private boolean updateTheme() {
		String themeNew = getCurrentTheme(ThemeUtils.SLECTION_LAUNCHER);
		String themeCur = null;
		ShareResourceLoader oldLoader = mDefaultLoader;

		if (oldLoader != null) {
			themeCur = oldLoader.getResourcePkgName();
		}
		if (DEBUG)
			Log.d(TAG, "updateTheme, new theme=" + themeNew + ", cur theme="
					+ themeCur);
		if (themeNew == null) {
			mDefaultLoader = null;
		} else {
			if (themeCur == null || !themeNew.equals(themeCur)) {
				try {
					mDefaultLoader = new ShareResourceLoader(mContext, themeNew);
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					mDefaultLoader = null;
				}
			} else {
				if (DEBUG)
					Log.w(TAG, "updateTheme, theme not change");
			}
		}

		return oldLoader != mDefaultLoader;
	}

	private boolean updateWallpaperTheme() {
		String themeNew = getCurrentTheme(ThemeUtils.SLECTION_WALLPAPER);
		String themeCur = null;
		ShareResourceLoader oldLoader = mWallpaperLoader;

		if (oldLoader != null) {
			themeCur = oldLoader.getResourcePkgName();
		}
		if (DEBUG)
			Log.d(TAG, "updateWallpaperTheme, new theme=" + themeNew
					+ ", cur theme=" + themeCur);
		if (themeNew == null) {
			mWallpaperLoader = null;
		} else {
			if (themeCur == null || !themeNew.equals(themeCur)) {
				try {
					mWallpaperLoader = new ShareResourceLoader(mContext,
							themeNew);
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					mWallpaperLoader = null;
				}
			} else {
				if (DEBUG)
					Log.w(TAG, "updateWallpaperTheme, wallpaper not change");
			}
		}

		return oldLoader != mWallpaperLoader;
	}

	private class ThemeObserver extends ContentObserver {
		public ThemeObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onChange(boolean selfChange) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (updateTheme()) {
						applyTheme();
					}
				}
			});
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (updateWallpaperTheme()) {
						applyWallpaper();
					}
				}
			});
		}
	}
}
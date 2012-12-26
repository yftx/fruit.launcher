package com.fruit.launcher.setting;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingUtils {

	public static final String LAUNCHER_SETTINGS_NAME = "launcher_config";
	public static final String LAUNCHER_THEME_SETTINGS_NAME = "launcher_theme_config";
	public static final String LAUNCHER_OTHER_SETTINGS_NAME = "launcher_other_config";

	public static final String KEY_SWITCH_EFFECT = "settings_key_screen_switcheffects";
	public static final String KEY_HIGH_QUALITY = "settings_key_high_quality";
	public static final String KEY_PERMANENT_MEMORY = "settings_key_permanent_memory";
	public static final String KEY_LOCK_SCREEN = "settings_key_lockscreen";

	public static final String KEY_SCREEN_COUNT = "screen_count";
	public static final String KEY_HOME_SCREEN = "homescreen_index";

	public static int mTransitionEffect;
	public static boolean mHighQuality;
	public static boolean mPermanentMemory;

	public static final int MIN_SCREEN_COUNT = 5;
	public static final int DEFAULT_SCREEN_COUNT = 7;
	public static final int DEFAULT_HOME_SCREEN_INDEX = 2;

	public static int mScreenCount = DEFAULT_SCREEN_COUNT;
	public static int mHomeScreenIndex = DEFAULT_HOME_SCREEN_INDEX;

	public static void loadLauncherSettings(Context context) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences(
				LAUNCHER_SETTINGS_NAME, 0);

		// mShowStatusbar = mSharedPreferences.getBoolean(KEY_SHOW_STATUSBAR,
		// true);
		// mShowAppName = mSharedPreferences.getBoolean(KEY_SHOW_APPNAME, true);

		mTransitionEffect = Integer.parseInt(mSharedPreferences.getString(
				KEY_SWITCH_EFFECT, "0"));
		mHighQuality = mSharedPreferences.getBoolean(KEY_HIGH_QUALITY, false);
		mPermanentMemory = mSharedPreferences.getBoolean(KEY_PERMANENT_MEMORY,
				false);

		loadOtherSettings(context);
	}

	private static void loadOtherSettings(Context context) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences(
				LAUNCHER_OTHER_SETTINGS_NAME, 0);

		mScreenCount = mSharedPreferences.getInt(KEY_SCREEN_COUNT,
				DEFAULT_SCREEN_COUNT);
		mHomeScreenIndex = mSharedPreferences.getInt(KEY_HOME_SCREEN,
				DEFAULT_HOME_SCREEN_INDEX);
	}

	public static void saveScreenSettings(Context context) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences(
				LAUNCHER_OTHER_SETTINGS_NAME, 0);

		mSharedPreferences.edit().putInt(KEY_SCREEN_COUNT, mScreenCount)
				.putInt(KEY_HOME_SCREEN, mHomeScreenIndex).commit();
	}

}
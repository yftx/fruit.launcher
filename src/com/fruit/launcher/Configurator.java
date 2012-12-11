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

import java.util.HashMap;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * Various utilities shared amongst the Launcher's classes.
 */
public final class Configurator {

	private static final String TAG = "Launcher.Configuration";
	private static final boolean DEBUG = false;

	private static final String CONFIG_PACKAGE = "com.fruit.launcher.config";

	public static final int INVALIDATE_CONFIG = -10000;

	// array
	public static final String CONFIG_ARRAY_WALLPAPER = "wallpapers";

	// dimens
	public static final String CONFIG_CELL_STARTPADDING = "workspace_cell_top_padding";
	public static final String CONFIG_CELL_ENDPADDING = "workspace_cell_bottom_padding";

	// config
	public static final String CONFIG_SHORTAXISCELLS = "config_shortAxisCells";
	public static final String CONFIG_LONGAXISCELLS = "config_longAxisCells";
	public static final String CONFIG_SLIDENUMROWS = "config_slideNumRows";
	public static final String CONFIG_SLIDENUMCLOUMS = "config_slideNumCloums";
	public static final String CONFIG_IDLEICONSHADOW = "config_idleIconShadow";
	public static final String CONFIG_DEFAULTTHME = "config_defalt_theme";

	public static final String CONFIG_HIDETHEME = "config_hideTheme";
	public static final String CONFIG_HIDELOCKSCREEN = "config_hideLockscreen";

	private static Resources mConfigResources = null;
	private static Context mConfigContext = null;

	private enum BoolValue {
		BTRUE, BFALSE, BINVALIDATE
	}

	// favorate attrs
	static final String CLASSNAME = "className";
	static final String PACKAGENAME = "packageName";
	static final String SCREEN = "screen";
	static final String CONTAINER = "container";
	static final String CELLX = "x";
	static final String CELLY = "y";
	static final String SPANX = "spanX";
	static final String SPANY = "spanY";
	static final String ICON = "icon";
	static final String TITLE = "title";
	static final String TYPE = "type";
	static final String ACTION = "action";
	static final String URI = "uri";
	static final String DATATYPE = "dataType";

	static final String sFavorite[] = { CLASSNAME, PACKAGENAME, SCREEN,
			CONTAINER, CELLX, CELLY, SPANX, SPANY, ICON, TITLE, TYPE, ACTION,
			URI, DATATYPE };

	// TopPackage attrs
	static final String TOPPACKAGENAME = "topPackageName";
	static final String TOPCLASSNAME = "topClassName";
	static final String TOPORDER = "topOrder";
	static final String sTopPackage[] = { TOPPACKAGENAME, TOPCLASSNAME,
			TOPORDER };

	private static void initConfiguration(Context context) {
		try {
			mConfigContext = context.createPackageContext(CONFIG_PACKAGE,
					Context.CONTEXT_INCLUDE_CODE
							| Context.CONTEXT_IGNORE_SECURITY);
			if (mConfigContext != null) {
				mConfigResources = mConfigContext.getResources();
			} else {
				mConfigResources = null;
			}
		} catch (NameNotFoundException e) {
			mConfigResources = null;
			mConfigContext = null;
		}
	}

	public static Resources getConfigResources(Context context) {
		if (mConfigResources == null) {
			initConfiguration(context);
		}

		return mConfigResources;
	}

	public static Context getConfigContext(Context context) {
		if (mConfigContext == null) {
			initConfiguration(context);
		}

		return mConfigContext;
	}

	public static String getConfigPackageName() {
		return CONFIG_PACKAGE;
	}

	public static Drawable getConfigPackageDrawable(Context context,
			String drawableName) {
		Drawable drawable = null;
		final String packageName = CONFIG_PACKAGE;
		final Resources res = getConfigResources(context);

		if (res != null) {
			int resId = res
					.getIdentifier(drawableName, "drawable", packageName);

			if (resId > 0) {
				drawable = res.getDrawable(resId);
			}
		}

		return drawable;
	}

	public static String[] getConfigPackageArray(Resources resources,
			String arrayName) {
		String[] array = null;
		final String packageName = CONFIG_PACKAGE;
		final Resources res = resources;

		if (res != null) {
			int listId = res.getIdentifier(arrayName, "array", packageName);
			if (listId > 0) {
				array = res.getStringArray(listId);
			}
		}

		return array;
	}

	public static XmlResourceParser getConfigPackageXml(Context context,
			String xmlName) {
		final String packageName = CONFIG_PACKAGE;
		final Resources res = getConfigResources(context);
		if (res != null) {
			int resId = res.getIdentifier(xmlName, "xml", packageName);

			if (resId > 0) {
				return res.getXml(resId);
			}
		}
		return null;
	}

	private static int getPackageAttrID(Context context, String attrName) {
		final String packageName = context.getPackageName();
		final Resources res = context.getResources();
		int resId = res.getIdentifier(attrName, "attr", packageName);
		if (resId > 0) {
			return resId;
		}
		return 0;
	}

	public static int[] getPackageAttrs(Context context, String[] attrsName) {
		int length = attrsName.length;
		int[] value = new int[length];
		for (int i = 0; i < length; i++) {
			String item = attrsName[i];
			value[i] = getPackageAttrID(context, item);
		}
		return value;
	}

	private static void swap(int[] data, int x, int y) {
		int temp = data[x];
		data[x] = data[y];
		data[y] = temp;
	}

	private static void swap(String[] data, int x, int y) {
		String temp = data[x];
		data[x] = data[y];
		data[y] = temp;
	}

	private static void selectSort(int[] baseData, String[] data) {
		int index;
		int length = baseData.length;
		for (int i = 1; i < length; i++) {
			index = 0;
			for (int j = 1; j <= length - i; j++) {
				if (baseData[j] > baseData[index]) {
					index = j;
				}
			}
			swap(baseData, length - i, index);
			swap(data, length - i, index);
		}
	}

	// attrs be sort at the same time;
	public static HashMap<String, Integer> getPackageStyleable(Context context,
			int[] attrsId, String[] attrsName) {
		int length = attrsName.length;
		String[] attrs = new String[length];
		for (int i = 0; i < length; i++) {
			attrs[i] = attrsName[i];
		}

		// In Ascending Order
		selectSort(attrsId, attrs);

		HashMap<String, Integer> value = new HashMap<String, Integer>(length);
		for (int i = 0; i < length; i++) {
			value.put(attrs[i], i);
		}
		return value;
	}

	private static int getPackgeIntConfig(Context context, String name) {
		final String packageName = context.getPackageName();
		final Resources res = context.getResources();

		int resId = res.getIdentifier(name, "integer", packageName);
		if (resId > 0) {
			return res.getInteger(resId);
		}
		return INVALIDATE_CONFIG;
	}

	private static String getPackgeStrConfig(Context context, String name) {
		final String packageName = context.getPackageName();
		final Resources res = context.getResources();

		int resId = res.getIdentifier(name, "string", packageName);
		if (resId > 0) {
			return res.getString(resId);
		}
		return null;
	}

	private static BoolValue getPackgeBoolConfig(Context context, String name) {
		final String packageName = context.getPackageName();
		final Resources res = context.getResources();

		int resId = res.getIdentifier(name, "bool", packageName);
		if (resId > 0) {
			boolean value = res.getBoolean(resId);
			if (value) {
				return BoolValue.BTRUE;
			} else {
				return BoolValue.BFALSE;
			}
		}
		return BoolValue.BINVALIDATE;
	}

	private static int getPackgeDimensPixel(Context context, String name) {
		final String packageName = context.getPackageName();
		final Resources res = context.getResources();

		int resId = res.getIdentifier(name, "dimen", packageName);
		if (resId > 0) {
			return res.getDimensionPixelSize(resId);
		}
		return INVALIDATE_CONFIG;
	}

	public static int getConfigPackgeIntConfig(Context context, String name) {
		Context configCtx = getConfigContext(context);
		if (configCtx != null) {
			return getPackgeIntConfig(configCtx, name);
		}
		return INVALIDATE_CONFIG;
	}

	public static String getConfigPackgeStrConfig(Context context, String name) {
		Context configCtx = getConfigContext(context);
		if (configCtx != null) {
			return getPackgeStrConfig(configCtx, name);
		}
		return null;
	}

	public static BoolValue getConfigPackgeBoolConfig(Context context,
			String name) {
		Context configCtx = getConfigContext(context);
		if (configCtx != null) {
			return getPackgeBoolConfig(configCtx, name);
		}
		return BoolValue.BINVALIDATE;
	}

	public static int getConfigPackgeDimensPixel(Context context, String name) {
		Context configCtx = getConfigContext(context);
		if (configCtx != null) {
			return getPackgeDimensPixel(configCtx, name);
		}
		return INVALIDATE_CONFIG;
	}

	public static int getIntegerConfig(Context context, String name,
			int defValue) {
		int value = getConfigPackgeIntConfig(context, name);
		if (value == INVALIDATE_CONFIG) {
			value = getPackgeIntConfig(context, name);
		}

		if (value == INVALIDATE_CONFIG) {
			value = defValue;
		}
		return value;
	}

	public static String getStringConfig(Context context, String name) {
		String value = getConfigPackgeStrConfig(context, name);
		if (value == null) {
			value = getPackgeStrConfig(context, name);
		}
		return value;
	}

	public static boolean getBooleanConfig(Context context, String name,
			boolean defValue) {
		BoolValue value = getConfigPackgeBoolConfig(context, name);
		if (value == BoolValue.BINVALIDATE) {
			value = getPackgeBoolConfig(context, name);
		}

		boolean reValue = defValue;
		if (value != BoolValue.BINVALIDATE) {
			if (value == BoolValue.BTRUE) {
				reValue = true;
			} else {
				reValue = false;
			}
		}
		return reValue;
	}

	public static int getDimensPixelSize(Context context, String name,
			int defValue) {
		int value = getConfigPackgeDimensPixel(context, name);
		if (value == INVALIDATE_CONFIG) {
			value = getPackgeDimensPixel(context, name);
		}

		if (value == INVALIDATE_CONFIG) {
			value = defValue;
		}
		return value;
	}
}
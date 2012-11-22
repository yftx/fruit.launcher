package com.fruit.launcher;

import com.fruit.launcher.LauncherSettings.Favorites;

public class CustomAppWidgetInfo extends ItemInfo {

	int title;
	int icon;

	static CustomAppWidgetInfo getLockScreenWidgetInfo() {
		CustomAppWidgetInfo info = new CustomAppWidgetInfo();

		info.itemType = Favorites.ITEM_TYPE_WIDGET_LOCK_SCREEN;
		info.spanX = 1;
		info.spanY = 1;
		info.title = R.string.widget_lock_screen;
		info.icon = R.drawable.ic_widget_lock_screen;

		return info;
	}

	static CustomAppWidgetInfo getCleanMemoryWidgetInfo() {
		CustomAppWidgetInfo info = new CustomAppWidgetInfo();

		info.itemType = Favorites.ITEM_TYPE_WIDGET_CLEAN_MEMORY;
		info.spanX = 1;
		info.spanY = 1;
		info.title = R.string.widget_clean_memory;
		info.icon = R.drawable.ic_widget_clean_memory;

		return info;
	}

	static CustomAppWidgetInfo getWidgetInfoByType(int itemType) {
		switch (itemType) {
		case Favorites.ITEM_TYPE_WIDGET_LOCK_SCREEN:
			return getLockScreenWidgetInfo();
		case Favorites.ITEM_TYPE_WIDGET_CLEAN_MEMORY:
			return getCleanMemoryWidgetInfo();
		default:
			return new CustomAppWidgetInfo();
		}
	}
}
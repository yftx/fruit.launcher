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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fruit.launcher.LauncherSettings.BaseLauncherColumns;
import com.fruit.launcher.LauncherSettings.Favorites;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * Represents an item in the launcher.
 */
class ItemInfo {

	public static int ROW = 4;
	public static int COL = 4;

	static final int NO_ID = -1;

	/**
	 * The id in the settings database for this item
	 */
	long id = NO_ID;

	/**
	 * One of {@link LauncherSettings.Favorites#ITEM_TYPE_APPLICATION},
	 * {@link LauncherSettings.Favorites#ITEM_TYPE_SHORTCUT},
	 * {@link LauncherSettings.Favorites#ITEM_TYPE_USER_FOLDER}, or
	 * {@link LauncherSettings.Favorites#ITEM_TYPE_APPWIDGET}.
	 */
	int itemType;

	/**
	 * The id of the container that holds this item. For the desktop, this will
	 * be {@link LauncherSettings.Favorites#CONTAINER_DESKTOP}. For the all
	 * applications folder it will be {@link #NO_ID} (since it is not stored in
	 * the settings DB). For user folders it will be the id of the folder.
	 */
	long container = NO_ID;

	/**
	 * Iindicates the screen in which the shortcut appears.
	 */
	int screen = -1;

	/**
	 * Indicates the X position of the associated cell.
	 */
	int cellX = -1;

	/**
	 * Indicates the Y position of the associated cell.
	 */
	int cellY = -1;

	/**
	 * Indicates the X cell span.
	 */
	int spanX = 1;

	/**
	 * Indicates the Y cell span.
	 */
	int spanY = 1;

	/**
	 * Indicates whether the item is a gesture.
	 */
	boolean isGesture = false;

	/**
	 * Indicates item's position in application
	 */
	int position;

	/**
	 * Indicates item's position in desktop
	 */
	int seqNo;

	/**
	 * Indicates item's order if it is in folder
	 */
	int orderId = -1;

	ItemInfo() {

	}

	ItemInfo(ItemInfo info) {
		id = info.id;
		cellX = info.cellX;
		cellY = info.cellY;
		spanX = info.spanX;
		spanY = info.spanY;
		screen = info.screen;
		itemType = info.itemType;
		container = info.container;
		position = info.position;
		orderId = info.orderId;
		seqNo = info.screen * (ROW * COL) + info.cellY * (COL) + info.cellX;
	}

	/**
	 * Write the fields of this item to the DB
	 * 
	 * @param values
	 */
	void onAddToDatabase(ContentValues values) {
		values.put(BaseLauncherColumns.ITEM_TYPE, itemType);
		if (!isGesture) {
			values.put(Favorites.CONTAINER, container);
			values.put(Favorites.SCREEN, screen);
			values.put(Favorites.CELLX, cellX);
			values.put(Favorites.CELLY, cellY);
			values.put(Favorites.SPANX, spanX);
			values.put(Favorites.SPANY, spanY);
			if (orderId >= 0) {
				values.put(BaseLauncherColumns.ORDERID, orderId);
			}
		}
	}

	static byte[] flattenBitmap(Bitmap bitmap) {
		// Try go guesstimate how much space the icon will take when serialized
		// to avoid unnecessary allocations/copies during the write.
		int size = bitmap.getWidth() * bitmap.getHeight() * 4;
		ByteArrayOutputStream out = new ByteArrayOutputStream(size);
		try {
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			return out.toByteArray();
		} catch (IOException e) {
			Log.w("Favorite", "Could not write icon");
			return null;
		}
	}

	static void writeBitmap(ContentValues values, Bitmap bitmap) {
		if (bitmap != null) {
			byte[] data = flattenBitmap(bitmap);
			values.put(BaseLauncherColumns.ICON, data);
		}
	}

	void unbind() {

	}

	@Override
	public String toString() {
	    StringBuffer str = new StringBuffer();
	    
		try {
			str.append("Item(id=" + this.id + ",type=" + this.itemType + ")"
					+",("+this.cellX+","+this.cellY+"," +this.spanX+","+this.spanY+")");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return str.toString();
	}

	/**
	 * @return the seqNo
	 */
	public int getSeqNo() {
		return (this.screen * (ROW * COL) + this.cellY * (COL) + this.cellX);
	}

}
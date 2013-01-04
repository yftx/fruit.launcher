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

import android.app.SearchManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ContentProvider;
import android.content.Context;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.content.res.TypedArray;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Xml;
import android.util.AttributeSet;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.provider.BaseColumns;
import android.provider.Settings;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParser;

import com.android.internal.util.XmlUtils;
import com.fruit.launcher.LauncherSettings.Applications;
import com.fruit.launcher.LauncherSettings.BaseLauncherColumns;
import com.fruit.launcher.LauncherSettings.Favorites;
import com.fruit.launcher.theme.ThemeUtils;


public class LauncherProvider extends ContentProvider {

	private static final String TAG = "LauncherProvider";
	private static final boolean LOGD = false;

	private static final String DATABASE_NAME = "launcher.db";

	// Used to identify shortcuts in favorite is intent with action or intent
	// with component name
	private static final String FAVORITE_ACTION = "action";

	private static final int DATABASE_VERSION = 10;

	static final String AUTHORITY = "com.fruit.launcher.settings";

	static final String TABLE_FAVORITES = "favorites";
	static final String TABLE_APPLICATIONS = "applications";
	static final String PARAMETER_NOTIFY = "notify";

	/**
	 * {@link Uri} triggered at any registered
	 * {@link android.database.ContentObserver} when
	 * {@link AppWidgetHost#deleteHost()} is called during database creation.
	 * Use this to recall {@link AppWidgetHost#startListening()} if needed.
	 */
	static final Uri CONTENT_APPWIDGET_RESET_URI = Uri.parse("content://"
			+ AUTHORITY + "/appWidgetReset");

	static final Uri CONTENT_DELETE_SCREEN_URI = Uri.parse("content://"
			+ AUTHORITY + "/favorites/deletescreen");

	static final Uri CONTENT_MOVE_FORWARD_SCREEN_URI = Uri.parse("content://"
			+ AUTHORITY + "/favorites/moveforwardscreen");

	static final Uri CONTENT_MOVE_BACKWARD_SCREEN_URI = Uri.parse("content://"
			+ AUTHORITY + "/favorites/movebackwardscreen");

	private static final UriMatcher mUriMatcher;

	private static final int FAVORITES = 101;
	private static final int FAVORITE_ID = 102;
	private static final int FAVORITE_DELETE_SCREEN = 103;
	private static final int FAVORITE_MOVE_FORWARD_SCREEN = 104;
	private static final int FAVORITE_MOVE_BACKWARD_SCREEN = 105;
	private static final int FAVORITE_ADJUST_ORDERID = 106;
	private static final int APPLICATIONS = 201;
	private static final int APPLICATION_ID = 202;
	private static final int ADD_FOLDER = 203;
	private static final int INSERT_FOLDER = 204;
	private static final int MOVE_FRONT = 205;
	private static final int MOVE_BEHIND = 206;
	private static final int ADJUST_ORDERID = 207;
	private static final int ADD_STARTNUM = 208;

	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(AUTHORITY, "favorites", FAVORITES);
		mUriMatcher.addURI(AUTHORITY, "favorites/#", FAVORITE_ID);
		mUriMatcher.addURI(AUTHORITY, "favorites/deletescreen",
				FAVORITE_DELETE_SCREEN);
		mUriMatcher.addURI(AUTHORITY, "favorites/moveforwardscreen",
				FAVORITE_MOVE_FORWARD_SCREEN);
		mUriMatcher.addURI(AUTHORITY, "favorites/movebackwardscreen",
				FAVORITE_MOVE_BACKWARD_SCREEN);
		mUriMatcher.addURI(AUTHORITY, "favorites/adjustOrderId",
				FAVORITE_ADJUST_ORDERID);
		mUriMatcher.addURI(AUTHORITY, "applications", APPLICATIONS);
		mUriMatcher.addURI(AUTHORITY, "applications/#", APPLICATION_ID);
		mUriMatcher.addURI(AUTHORITY, "applications/addfolder", ADD_FOLDER);
		mUriMatcher.addURI(AUTHORITY, "applications/insertfolder",
				INSERT_FOLDER);
		mUriMatcher.addURI(AUTHORITY, "applications/movefront", MOVE_FRONT);
		mUriMatcher.addURI(AUTHORITY, "applications/movebehind", MOVE_BEHIND);
		mUriMatcher.addURI(AUTHORITY, "applications/adjustOrderId",
				ADJUST_ORDERID);
		mUriMatcher.addURI(AUTHORITY, "applications/addStartNum", ADD_STARTNUM);
	}

	private SQLiteOpenHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		SqlArguments args = new SqlArguments(uri, null, null);
		if (TextUtils.isEmpty(args.where)) {
			return "vnd.android.cursor.dir/" + args.table;
		} else {
			return "vnd.android.cursor.item/" + args.table;
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

		qb.setTables(args.table);
		Cursor result = qb.query(db, projection, args.where, args.args, null,
				null, sortOrder);

		result.setNotificationUri(getContext().getContentResolver(), uri);
		return result;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		SqlArguments args = new SqlArguments(uri);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final long rowId = db.insert(args.table, null, initialValues);
		if (rowId <= 0) {
			return null;
		}

		uri = ContentUris.withAppendedId(uri, rowId);
		sendNotify(uri);

		return uri;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		SqlArguments args = new SqlArguments(uri);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			int numValues = values.length;
			for (int i = 0; i < numValues; i++) {
				if (db.insert(args.table, null, values[i]) < 0) {
					return 0;
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		sendNotify(uri);
		return values.length;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count = db.delete(args.table, args.where, args.args);
		if (count > 0) {
			sendNotify(uri);
		}

		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		final int match = mUriMatcher.match(uri);
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count = 0;

		switch (match) {
		case FAVORITES:
			count = db
					.update(TABLE_FAVORITES, values, selection, selectionArgs);
			break;
		case FAVORITE_ID:
			SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

			count = db.update(args.table, values, args.where, args.args);
			break;
		case FAVORITE_DELETE_SCREEN:
			Object[] paramsDelete = new Object[5];
			paramsDelete[0] = TABLE_FAVORITES;
			paramsDelete[1] = Favorites.SCREEN;
			paramsDelete[2] = Favorites.SCREEN;
			paramsDelete[3] = Favorites.SCREEN;
			paramsDelete[4] = selectionArgs[0];

			String sqlDelete = String.format(
					"update %s set %s=%s-1 where %s>%s", paramsDelete);
			db.execSQL(sqlDelete);
			break;
		case FAVORITE_MOVE_FORWARD_SCREEN:
			Object[] paramsForward = new Object[7];
			paramsForward[0] = TABLE_FAVORITES;
			paramsForward[1] = Favorites.SCREEN;
			paramsForward[2] = Favorites.SCREEN;
			paramsForward[3] = Favorites.SCREEN;
			paramsForward[4] = selectionArgs[0];
			paramsForward[5] = Favorites.SCREEN;
			paramsForward[6] = selectionArgs[1];

			String sqlForward = String.format(
					"update %s set %s=%s-1 where %s>%s and %s<=%s",
					paramsForward);
			db.execSQL(sqlForward);
			break;
		case FAVORITE_MOVE_BACKWARD_SCREEN:
			Object[] paramsBackward = new Object[7];
			paramsBackward[0] = TABLE_FAVORITES;
			paramsBackward[1] = Favorites.SCREEN;
			paramsBackward[2] = Favorites.SCREEN;
			paramsBackward[3] = Favorites.SCREEN;
			paramsBackward[4] = selectionArgs[1];
			paramsBackward[5] = Favorites.SCREEN;
			paramsBackward[6] = selectionArgs[0];

			String sqlBackford = String.format(
					"update %s set %s=%s+1 where %s>=%s and %s<%s",
					paramsBackward);
			db.execSQL(sqlBackford);
			break;
		case FAVORITE_ADJUST_ORDERID:
			Object[] paramsAdjustFav = new Object[8];
			paramsAdjustFav[0] = TABLE_FAVORITES;
			paramsAdjustFav[1] = BaseLauncherColumns.ORDERID;
			paramsAdjustFav[2] = BaseLauncherColumns.ORDERID;
			paramsAdjustFav[3] = Integer.valueOf(1);
			paramsAdjustFav[4] = Applications.CONTAINER;
			paramsAdjustFav[5] = selectionArgs[0];
			paramsAdjustFav[6] = BaseLauncherColumns.ORDERID;
			paramsAdjustFav[7] = selectionArgs[1];
			String sqlAdjustFav = String.format(
					"update %s set %s=%s-%d where %s=%s and %s>%s",
					paramsAdjustFav);

			db.execSQL(sqlAdjustFav);
			break;
		case APPLICATIONS:
			count = db.update(TABLE_APPLICATIONS, values, selection,
					selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case APPLICATION_ID:
			SqlArguments argsApps = new SqlArguments(uri, selection,
					selectionArgs);

			count = db.update(argsApps.table, values, argsApps.where,
					argsApps.args);
			break;
		case ADD_FOLDER:
			Object[] paramsAdd = new Object[6];
			paramsAdd[0] = TABLE_APPLICATIONS;
			paramsAdd[1] = Applications.POSITION;
			paramsAdd[2] = Applications.POSITION;
			paramsAdd[3] = Integer.valueOf(1);
			paramsAdd[4] = Applications.POSITION;
			paramsAdd[5] = selectionArgs[0];
			String sqlAdd = String.format(
					"update %s set %s=%s+%d where %s>=%s", paramsAdd);

			db.execSQL(sqlAdd);
			break;
		case INSERT_FOLDER:
			Object[] paramsInsert = new Object[6];
			paramsInsert[0] = TABLE_APPLICATIONS;
			paramsInsert[1] = Applications.POSITION;
			paramsInsert[2] = Applications.POSITION;
			paramsInsert[3] = Integer.valueOf(1);
			paramsInsert[4] = Applications.POSITION;
			paramsInsert[5] = selectionArgs[0];
			String sqlInsert = String.format(
					"update %s set %s=%s-%d where %s>%s", paramsInsert);

			db.execSQL(sqlInsert);
			break;
		case MOVE_FRONT:
			Object[] paramsFront = new Object[8];
			paramsFront[0] = TABLE_APPLICATIONS;
			paramsFront[1] = Applications.POSITION;
			paramsFront[2] = Applications.POSITION;
			paramsFront[3] = Integer.valueOf(1);
			paramsFront[4] = Applications.POSITION;
			paramsFront[5] = selectionArgs[0];
			paramsFront[6] = Applications.POSITION;
			paramsFront[7] = selectionArgs[1];
			String sqlFront = String.format(
					"update %s set %s=%s+%d where %s>=%s and %s<%s",
					paramsFront);

			db.execSQL(sqlFront);
			break;
		case MOVE_BEHIND:
			Object[] paramsBehind = new Object[8];
			paramsBehind[0] = TABLE_APPLICATIONS;
			paramsBehind[1] = Applications.POSITION;
			paramsBehind[2] = Applications.POSITION;
			paramsBehind[3] = Integer.valueOf(1);
			paramsBehind[4] = Applications.POSITION;
			paramsBehind[5] = selectionArgs[0];
			paramsBehind[6] = Applications.POSITION;
			paramsBehind[7] = selectionArgs[1];
			String sqlBehind = String.format(
					"update %s set %s=%s-%d where %s>%s and %s<=%s",
					paramsBehind);

			db.execSQL(sqlBehind);
			break;
		case ADJUST_ORDERID:
			Object[] paramsAdjust = new Object[8];
			paramsAdjust[0] = TABLE_APPLICATIONS;
			paramsAdjust[1] = BaseLauncherColumns.ORDERID;
			paramsAdjust[2] = BaseLauncherColumns.ORDERID;
			paramsAdjust[3] = Integer.valueOf(1);
			paramsAdjust[4] = Applications.CONTAINER;
			paramsAdjust[5] = selectionArgs[0];
			paramsAdjust[6] = BaseLauncherColumns.ORDERID;
			paramsAdjust[7] = selectionArgs[1];
			String sqlAdjust = String.format(
					"update %s set %s=%s-%d where %s=%s and %s>%s",
					paramsAdjust);

			db.execSQL(sqlAdjust);
			break;
		case ADD_STARTNUM:
			Object[] paramsAddNum = new Object[6];
			paramsAddNum[0] = TABLE_APPLICATIONS;
			paramsAddNum[1] = Applications.STARTNUM;
			paramsAddNum[2] = Applications.STARTNUM;
			paramsAddNum[3] = Integer.valueOf(1);
			paramsAddNum[4] = BaseLauncherColumns.INTENT;
			paramsAddNum[5] = selectionArgs[0];
			String sqlAddNum = String.format(
					"update %s set %s=%s+%d where %s='%s'", paramsAddNum);

			db.execSQL(sqlAddNum);
			break;
		}

		if (count > 0) {
			sendNotify(uri);
		}

		return count;
	}

	private void sendNotify(Uri uri) {
		String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
		if (notify == null || "true".equals(notify)) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		private static final String TAG_FAVORITES = "favorites";
		private static final String TAG_FAVORITE = "favorite";
		private static final String TAG_CLOCK = "clock";
		private static final String TAG_SEARCH = "search";
		private static final String TAG_APPWIDGET = "appwidget";
		private static final String TAG_LOCKSCREENAPPWIDGET = "lockscreenappwidget";
		private static final String TAG_CLEANMEMAPPWIDGET = "cleanmemappwidget";
		private static final String TAG_SHORTCUT = "shortcut";

		private final Context mContext;
		private final AppWidgetHost mAppWidgetHost;

		private static final int DEFAULT_APPLICATIONS_NUMBER = 42;

		private static final String TAG_TOPPACKAGES = "toppackages";
		private static final String TAG_TOPPACKAGE = "TopPackage";

		private static final boolean DEBUG_LOADERS_REORDER = false;

		static ArrayList<TopPackage> mTopPackages;
		//private Logger mLogger;

		private static class TopPackage {
			public TopPackage(String packagename, String classname, int order) {
				mPackageName = packagename;
				mClassName = classname;
				mOrder = order;
				mIndex = -1;

			}

			String mPackageName;
			String mClassName;
			int mOrder;

			int mIndex;
		}

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
			mAppWidgetHost = new AppWidgetHost(context,
					Launcher.APPWIDGET_HOST_ID);
		}

		/**
		 * Send notification that we've deleted the {@link AppWidgetHost},
		 * probably as part of the initial database creation. The receiver may
		 * want to re-call {@link AppWidgetHost#startListening()} to ensure
		 * callbacks are correctly set.
		 */
		private void sendAppWidgetResetNotify() {
			final ContentResolver resolver = mContext.getContentResolver();
			resolver.notifyChange(CONTENT_APPWIDGET_RESET_URI, null);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			if (LOGD) {
				Log.d(TAG, "creating new launcher database");
			}

			db.execSQL("CREATE TABLE " + TABLE_FAVORITES + " (" + "_id INTEGER PRIMARY KEY,"
					+ "title TEXT," + "intent TEXT," + "container INTEGER,"
					+ "screen INTEGER," + "cellX INTEGER," + "cellY INTEGER,"
					+ "spanX INTEGER," + "spanY INTEGER," + "orderId INTEGER,"
					+ "itemType INTEGER,"
					+ "appWidgetId INTEGER NOT NULL DEFAULT -1,"
					+ "isShortcut INTEGER," + "iconType INTEGER,"
					+ "iconPackage TEXT," + "iconResource TEXT," + "icon BLOB,"
					+ "uri TEXT," + "displayMode INTEGER" + ");");

			db.execSQL("CREATE TABLE " + TABLE_APPLICATIONS + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY,"
					+ BaseLauncherColumns.TITLE + " TEXT,"
					+ BaseLauncherColumns.INTENT + " TEXT,"
					+ Applications.CONTAINER + " INTEGER,"
					+ Applications.POSITION + " INTEGER,"
					+ BaseLauncherColumns.ORDERID + " INTEGER,"
					+ BaseLauncherColumns.ITEM_TYPE + " INTEGER,"
					+ Applications.SYSAPP + " INTEGER,"
					+ Applications.PACKAGENAME + " TEXT,"
					+ Applications.INSTALL + " INTEGER,"
					+ Applications.STARTNUM + " INTEGER" + ");");

			// Database was just created, so wipe any previous widgets
			if (mAppWidgetHost != null) {
				mAppWidgetHost.deleteHost();
				sendAppWidgetResetNotify();
			}

			if (!convertDatabase(db)) {
				// Populate favorites table with initial favorites
				loadFavorites(db);
			}
			
			loadTopPackage(mContext);
			loadAllApps(db);
		}

		private boolean convertDatabase(SQLiteDatabase db) {
			if (LOGD) {
				Log.d(TAG,
						"converting database from an older format, but not onUpgrade");
			}
			boolean converted = false;

			final Uri uri = Uri.parse("content://" + Settings.AUTHORITY
					+ "/old_favorites?notify=true");
			final ContentResolver resolver = mContext.getContentResolver();
			Cursor cursor = null;

			try {
				cursor = resolver.query(uri, null, null, null, null);
			} catch (Exception e) {
				// Ignore
			}

			// We already have a favorites database in the old provider
			if (cursor != null && cursor.getCount() > 0) {
				try {
					converted = copyFromCursor(db, cursor) > 0;
				} finally {
					cursor.close();
				}

				if (converted) {
					resolver.delete(uri, null, null);
				}
			}

			if (converted) {
				// Convert widgets from this import into widgets
				if (LOGD) {
					Log.d(TAG, "converted and now triggering widget upgrade");
				}
				convertWidgets(db);
			}

			Log.d(TAG, "converted result=" + converted);
			return converted;
		}

		private int copyFromCursor(SQLiteDatabase db, Cursor c) {
			final int idIndex = c.getColumnIndexOrThrow(BaseColumns._ID);
			final int intentIndex = c
					.getColumnIndexOrThrow(BaseLauncherColumns.INTENT);
			final int titleIndex = c
					.getColumnIndexOrThrow(BaseLauncherColumns.TITLE);
			final int iconTypeIndex = c
					.getColumnIndexOrThrow(BaseLauncherColumns.ICON_TYPE);
			final int iconIndex = c
					.getColumnIndexOrThrow(BaseLauncherColumns.ICON);
			final int iconPackageIndex = c
					.getColumnIndexOrThrow(BaseLauncherColumns.ICON_PACKAGE);
			final int iconResourceIndex = c
					.getColumnIndexOrThrow(BaseLauncherColumns.ICON_RESOURCE);
			final int containerIndex = c
					.getColumnIndexOrThrow(Favorites.CONTAINER);
			final int itemTypeIndex = c
					.getColumnIndexOrThrow(BaseLauncherColumns.ITEM_TYPE);
			final int screenIndex = c.getColumnIndexOrThrow(Favorites.SCREEN);
			final int cellXIndex = c.getColumnIndexOrThrow(Favorites.CELLX);
			final int cellYIndex = c.getColumnIndexOrThrow(Favorites.CELLY);
			final int uriIndex = c.getColumnIndexOrThrow(Favorites.URI);
			final int displayModeIndex = c
					.getColumnIndexOrThrow(Favorites.DISPLAY_MODE);

			ContentValues[] rows = new ContentValues[c.getCount()];
			int i = 0;
			while (c.moveToNext()) {
				ContentValues values = new ContentValues(c.getColumnCount());
				values.put(BaseColumns._ID, c.getLong(idIndex));
				values.put(BaseLauncherColumns.INTENT, c.getString(intentIndex));
				values.put(BaseLauncherColumns.TITLE, c.getString(titleIndex));
				values.put(BaseLauncherColumns.ICON_TYPE,
						c.getInt(iconTypeIndex));
				values.put(BaseLauncherColumns.ICON, c.getBlob(iconIndex));
				values.put(BaseLauncherColumns.ICON_PACKAGE,
						c.getString(iconPackageIndex));
				values.put(BaseLauncherColumns.ICON_RESOURCE,
						c.getString(iconResourceIndex));
				values.put(Favorites.CONTAINER, c.getInt(containerIndex));
				values.put(BaseLauncherColumns.ITEM_TYPE,
						c.getInt(itemTypeIndex));
				values.put(Favorites.APPWIDGET_ID, -1);
				values.put(Favorites.SCREEN, c.getInt(screenIndex));
				values.put(Favorites.CELLX, c.getInt(cellXIndex));
				values.put(Favorites.CELLY, c.getInt(cellYIndex));
				values.put(Favorites.URI, c.getString(uriIndex));
				values.put(Favorites.DISPLAY_MODE, c.getInt(displayModeIndex));
				rows[i++] = values;
			}

			db.beginTransaction();
			int total = 0;
			try {
				int numValues = rows.length;
				for (i = 0; i < numValues; i++) {
					if (db.insert(TABLE_FAVORITES, null, rows[i]) < 0) {
						return 0;
					} else {
						total++;
					}
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			return total;
		}

		/**
		 * Loads the default set of default to packages from an xml file.
		 * 
		 * @modify guo
		 * @param context
		 *            The context
		 */
		static boolean loadTopPackage(Context context) {
			boolean bRet = false;

			if (mTopPackages == null) {
				mTopPackages = new ArrayList<TopPackage>();
			} else {
				return true;
			}

			try {
				Context ctx = Configurator.getConfigContext(context);
				XmlResourceParser parser = Configurator.getConfigPackageXml(
						context, "default_toppackage");
				if (parser == null) {
					ctx = context;
					parser = context.getResources().getXml(
							R.xml.default_toppackage);
				}

				AttributeSet set = Xml.asAttributeSet(parser);
				XmlUtils.beginDocument(parser, TAG_TOPPACKAGES);

				final int depth = parser.getDepth();

				int type;
				int[] attrsTopPackage = Configurator.getPackageAttrs(ctx,
						Configurator.sTopPackage);
				HashMap<String, Integer> attrMap = Configurator
						.getPackageStyleable(ctx, attrsTopPackage,
								Configurator.sTopPackage);

				while (((type = parser.next()) != XmlPullParser.END_TAG || parser
						.getDepth() > depth)
						&& type != XmlPullParser.END_DOCUMENT) {

					if (type != XmlPullParser.START_TAG) {
						continue;
					}

					TypedArray a = ctx.obtainStyledAttributes(set,
							attrsTopPackage);

					mTopPackages.add(new TopPackage(a.getString(attrMap
							.get(Configurator.TOPPACKAGENAME)), a
							.getString(attrMap.get(Configurator.TOPCLASSNAME)),
							a.getInt(attrMap.get(Configurator.TOPORDER), 0)));
					if (LOGD) {
						Log.d(TAG,
								"loadTopPackage packageName=="
										+ a.getString(R.styleable.TopPackage_topPackageName));
						Log.d(TAG,
								"loadTopPackage className=="
										+ a.getString(R.styleable.TopPackage_topClassName));
					}
					a.recycle();
				}
			} catch (XmlPullParserException e) {
				Log.w(TAG, "Got exception parsing toppackage.", e);
			} catch (IOException e) {
				Log.w(TAG, "Got exception parsing toppackage.", e);
			}

			return bRet;
		}

		List<ResolveInfo> reorderApplist(List<ResolveInfo> added) {
			final long sortTime = DEBUG_LOADERS_REORDER ? SystemClock
					.uptimeMillis() : 0;

			if (mTopPackages == null) {
				return added;
			}

			ArrayList<ResolveInfo> dataReorder = new ArrayList<ResolveInfo>(
					DEFAULT_APPLICATIONS_NUMBER);

			List<ResolveInfo> data = new ArrayList<ResolveInfo>();
			for (int i = 0; i < added.size(); i++) {
				data.add(added.get(i));
			}

			for (TopPackage tp : mTopPackages) {
				int loop = 0;
				int newIndex = 0;
				for (ResolveInfo ai : added) {
					if (DEBUG_LOADERS_REORDER) {
						Log.d(TAG, "reorderApplist remove loop==" + loop);
					}

					if (ai.activityInfo.applicationInfo.packageName
							.equals(tp.mPackageName)
							&& ai.activityInfo.name.equals(tp.mClassName)) {
						if (DEBUG_LOADERS_REORDER) {
							Log.d(TAG, "reorderApplist remove newIndex=="
									+ newIndex);
						}

						data.remove(ai);
						dataReorder.add(ai);

						break;
					}
					loop++;
				}
			}
			dumpData(data);

			for (TopPackage tp : mTopPackages) {
				int loop = 0;
				int newIndex = 0;
				for (ResolveInfo ai : dataReorder) {
					if (DEBUG_LOADERS_REORDER) {
						Log.d(TAG, "reorderApplist added loop==" + loop);
					}

					if (ai.activityInfo.applicationInfo.packageName
							.equals(tp.mPackageName)
							&& ai.activityInfo.name.equals(tp.mClassName)) {
						newIndex = Math.min(Math.max(tp.mOrder, 0),
								added.size());
						if (DEBUG_LOADERS_REORDER) {
							Log.d(TAG, "reorderApplist added newIndex=="
									+ newIndex);
						}
						newIndex = Math.min(newIndex, data.size());
						if (DEBUG_LOADERS_REORDER) {
							Log.d(TAG, "reorderApplist added newIndex=="
									+ newIndex);
						}
						data.add(newIndex, ai);

						break;
					}
					loop++;
				}
			}
			dumpData(data);

			if (DEBUG_LOADERS_REORDER) {
				Log.d(TAG,
						"sort and reorder took "
								+ (SystemClock.uptimeMillis() - sortTime)
								+ "ms");
			}

			return data;
		}

		void dumpData(List<ResolveInfo> data) {
			int loop2 = 0;
			for (ResolveInfo ai : data) {
				if (DEBUG_LOADERS_REORDER) {
					Log.d(TAG, "reorderApplist data loop2==" + loop2);
				}
				loop2++;
			}
		}
		
//		void runtime_log4j(){
//			LogConfigurator logConfigurator = new LogConfigurator();   
//			   
//			  
//            logConfigurator.setFileName(Environment.getExternalStorageDirectory()  
//
//
//                            + File.separator + "launcher" + File.separator  
//
//
//                           + "log4j.txt");  
//
//
//            logConfigurator.setRootLevel(Level.DEBUG);  
//
//
//            logConfigurator.setLevel("org.apache", Level.ERROR);  
//
//
//            logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");  
//
//
//            logConfigurator.setMaxFileSize(1024 * 1024 * 5);  
//
//
//            logConfigurator.setImmediateFlush(true);  
//
//
//            logConfigurator.configure();  
//
//
//            mLogger = Logger.getLogger(LauncherProvider.class);  
//
//
//		}

		private void loadAllApps(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

			final PackageManager packageManager = mContext.getPackageManager();
			List<ResolveInfo> apps = null;
			apps = packageManager.queryIntentActivities(mainIntent, 0);
			
			if (apps.size() == 0) {
				return;
			} /*else {
				//runtime_log4j();
				String str = new String();
				//String str2 = Environment.getExternalStorageDirectory() + File.separator + "launcher"+ File.separator+"log4j.properties";
				//PropertyConfigurator.configure(str2); 
				//Logger logger = Logger.getLogger(TAG);
				
				for (int i = 0; i < apps.size(); i++){
					ResolveInfo info = apps.get(i);	
					str="";
					str+= i+ "::ResolveInfo="+info.resolvePackageName+","
					+info.activityInfo.name+","+info.activityInfo.packageName+","
					+info.activityInfo.processName+","+info.activityInfo.applicationInfo.className+","
					+info.activityInfo.applicationInfo.name+","+info.activityInfo.applicationInfo.packageName+","
					+info.activityInfo.applicationInfo.processName+","+info.activityInfo.applicationInfo.manageSpaceActivityName+","
					+info.activityInfo.applicationInfo.backupAgentName+","+info.activityInfo.applicationInfo.installLocation+","
					+info.activityInfo.applicationInfo.dataDir+"\n";
					
					//mLogger.info(str);
					Log.d(TAG,str);
				}
			}*/

			Collections.sort(apps, new ResolveInfo.DisplayNameComparator(
					packageManager));

			
			List<ResolveInfo> data = reorderApplist(apps);
			int position = 0;
			int position2 = 0;
			int relativePosition = 0;

			final String selfPkgName = mContext.getPackageName();
			for (int j = 0; j < data.size(); j++) {
				// This builds the icon bitmaps.
				ResolveInfo info = data.get(j);

				final ApplicationInfo appInfo = info.activityInfo.applicationInfo;
				// Do not add custom theme package to all application
				if (appInfo.packageName
						.startsWith(ThemeUtils.THEME_PACKAGE_TOKEN)
						|| appInfo.packageName.equals(selfPkgName)) {
					continue;
				}

				String intentInfo = appInfo.packageName + "|"
						+ info.activityInfo.name;
				ContentValues values = new ContentValues();

				values.put(BaseLauncherColumns.TITLE,
						info.loadLabel(packageManager).toString());
				values.put(BaseLauncherColumns.INTENT, intentInfo);
				values.put(Applications.CONTAINER, Applications.CONTAINER_APPS);
				values.put(Applications.POSITION, position);
				values.put(BaseLauncherColumns.ITEM_TYPE,
						Applications.APPS_TYPE_APP);
				Log.d(TAG, "appInfo: " + appInfo.packageName + " flag="
						+ appInfo.flags);
				if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
					values.put(Applications.SYSAPP, 1);
				} else {
					values.put(Applications.SYSAPP, 0);
				}
				// [[add by liujian at 2012-6-14
				// set stk1/stk2 sysapp flag
				if (appInfo.packageName == "com.android.stk"
						|| appInfo.packageName == "com.android.stk2") {
					values.put(Applications.SYSAPP, 1);
				}
				// ]]at 2012-6-14
				values.put(Applications.PACKAGENAME,
						info.activityInfo.applicationInfo.packageName);
				// values.put(Applications.INSTALL, info.activityInfo.la)
				values.put(Applications.STARTNUM, 0);

				long res = db.insert(TABLE_APPLICATIONS, null, values);

				if (res == -1) {
					Log.d(TAG, "loadAllApps insert db error");
				} else {
					position++;
				}

				// yfzhao.start
				String actInfoName = info.activityInfo.name;

				if (!(actInfoName
						.equals("com.android.contacts.activities.DialtactsActivity")
						|| actInfoName
								.equals("com.android.contacts.activities.PeopleActivity")
						|| actInfoName
								.equals("com.android.mms.ui.BootActivity")
						|| actInfoName
								.equals("com.android.browser.BrowserActivity")

						|| actInfoName
								.equals("com.fruit.thememanager.ThemeSettingActivity")
						|| actInfoName
								.equals("com.android.calculator2.Calculator")
						|| actInfoName
								.equals("com.android.soundrecorder.SoundRecorder")
						|| actInfoName
								.equals("com.mediatek.StkSelection.StkSelection")

						|| actInfoName
								.equals("com.mediatek.FMRadio.FMRadioActivity")
						|| actInfoName
								.equals("com.android.email.activity.Welcome")
						|| actInfoName
								.equals("com.android.calendar.AllInOneActivity")
						|| actInfoName
								.equals("com.android.deskclock.DeskClock")

						|| actInfoName.equals("com.android.camera.Camera")
						|| actInfoName.equals("com.mapbar.android.mapbarmap.MapViewActivity")
						|| actInfoName
								.equals("com.android.music.MusicBrowserActivity")
						|| actInfoName
								.equals("com.mediatek.videoplayer.MovieListActivity")
								
						|| actInfoName.equals("com.android.gallery3d.app.Gallery")
						|| actInfoName.equals("com.mediatek.filemanager.FileManagerOperationActivity")
						|| actInfoName
								.equals("com.android.providers.downloads.ui.DownloadList") 
					    || actInfoName
							.equals("com.android.settings.Settings")

				)) {

					if ((info.activityInfo.name.indexOf(appInfo.packageName)) >= 0) {
						intentInfo = "#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component="
								+ appInfo.packageName
								+ "/"
								+ info.activityInfo.name
										.substring(info.activityInfo.name
												.indexOf(appInfo.packageName)
												+ appInfo.packageName.length())
								+ ";end";

					} else {
						intentInfo = "#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component="
								+ appInfo.packageName
								+ "/"
								+ info.activityInfo.name + ";end";
					}

					ContentValues values2 = new ContentValues();

					values2.put(BaseLauncherColumns.TITLE,
							info.loadLabel(packageManager).toString());

					values2.put(BaseLauncherColumns.INTENT, intentInfo);
					values2.put(Favorites.CONTAINER,
							Favorites.CONTAINER_DESKTOP);
					// values2.put(Favorites.POSITION, position);
					values2.put(Favorites.SCREEN, position2 / 16 + 5);
					relativePosition = position2 % 16;
					values2.put(Favorites.CELLX, relativePosition % 4);
					values2.put(Favorites.CELLY, relativePosition / 4);
					values2.put(Favorites.SPANX, 1);
					values2.put(Favorites.SPANY, 1);
					values2.put(BaseLauncherColumns.ITEM_TYPE,
							BaseLauncherColumns.ITEM_TYPE_APPLICATION);
					// Log.d(TAG, "appInfo: " + appInfo.packageName + " flag=" +
					// appInfo.flags);
					// if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) ==
					// ApplicationInfo.FLAG_SYSTEM) {
					// values2.put(Favorites.SYSAPP, 1);
					// } else {
					// values2.put(Favorites.SYSAPP, 0);
					// }
					// values2.put(Favorites.PACKAGENAME,
					// info.activityInfo.applicationInfo.packageName);
					// values.put(Applications.INSTALL, info.activityInfo.la)
					// values2.put(Favorites.STARTNUM, 0);
					values2.put(BaseLauncherColumns.ICON_TYPE,
							BaseLauncherColumns.ICON_TYPE_RESOURCE);
					// values2.put(Favorites.ICON_PACKAGE,
					// info.activityInfo.applicationInfo.packageName);
					// values2.put(Favorites.ICON_RESOURCE,
					// info.getIconResource());

					long res2 = db.insert(TABLE_FAVORITES, null, values2); // yfzhao

					if (res2 == -1) {
						Log.d(TAG,
								"loadAllApps TABLE_FAVORITES insert db error");
					} else {
						position2++;
					}
				}
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (LOGD) {
				Log.d(TAG, "onUpgrade triggered");
			}

			int version = oldVersion;
			if (version < 3) {
				// upgrade 1,2 -> 3 added appWidgetId column
				db.beginTransaction();
				try {
					// Insert new column for holding appWidgetIds
					db.execSQL("ALTER TABLE favorites "
							+ "ADD COLUMN appWidgetId INTEGER NOT NULL DEFAULT -1;");
					db.setTransactionSuccessful();
					version = 3;
				} catch (SQLException ex) {
					// Old version remains, which means we wipe old data
					Log.e(TAG, ex.getMessage(), ex);
				} finally {
					db.endTransaction();
				}

				// Convert existing widgets only if table upgrade was successful
				if (version == 3) {
					convertWidgets(db);
				}
			}

			if (version < 4) {
				version = 4;
			}

			// Where's version 5?
			// - Donut and sholes on 2.0 shipped with version 4 of launcher1.
			// - Passion shipped on 2.1 with version 6 of launcher2
			// - Sholes shipped on 2.1r1 (aka Mr. 3) with version 5 of launcher
			// 1
			// but version 5 on there was the updateContactsShortcuts change
			// which was version 6 in launcher 2 (first shipped on passion
			// 2.1r1).
			// The updateContactsShortcuts change is idempotent, so running it
			// twice
			// is okay so we'll do that when upgrading the devices that shipped
			// with it.
			if (version < 6) {
				// We went from 3 to 5 screens. Move everything 1 to the right
				db.beginTransaction();
				try {
					db.execSQL("UPDATE favorites SET screen=(screen + 1);");
					db.setTransactionSuccessful();
				} catch (SQLException ex) {
					// Old version remains, which means we wipe old data
					Log.e(TAG, ex.getMessage(), ex);
				} finally {
					db.endTransaction();
				}

				// We added the fast track.
				if (updateContactsShortcuts(db)) {
					version = 6;
				}
			}

			if (version < 7) {
				// Version 7 gets rid of the special search widget.
				convertWidgets(db);
				version = 7;
			}

			if (version < 8) {
				// Version 8 (froyo) has the icons all normalized. This should
				// already be the case in practice, but we now rely on it and
				// don't
				// resample the images each time.
				normalizeIcons(db);
				version = 8;
			}

			if (version != DATABASE_VERSION) {
				Log.w(TAG, "Destroying all old data.");
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPLICATIONS);
				onCreate(db);
			}
		}

		private boolean updateContactsShortcuts(SQLiteDatabase db) {
			Cursor c = null;
			final String selectWhere = buildOrWhereString(
					BaseLauncherColumns.ITEM_TYPE,
					new int[] { BaseLauncherColumns.ITEM_TYPE_SHORTCUT });

			db.beginTransaction();
			try {
				// Select and iterate through each matching widget
				c = db.query(TABLE_FAVORITES, new String[] { BaseColumns._ID,
						BaseLauncherColumns.INTENT }, selectWhere, null, null,
						null, null);

				if (LOGD) {
					Log.d(TAG, "found upgrade cursor count=" + c.getCount());
				}

				final ContentValues values = new ContentValues();
				final int idIndex = c.getColumnIndex(BaseColumns._ID);
				final int intentIndex = c
						.getColumnIndex(BaseLauncherColumns.INTENT);

				while (c != null && c.moveToNext()) {
					long favoriteId = c.getLong(idIndex);
					final String intentUri = c.getString(intentIndex);
					if (intentUri != null) {
						try {
							Intent intent = Intent.parseUri(intentUri, 0);
							android.util.Log.d("Home", intent.toString());
							final Uri uri = intent.getData();
							final String data = uri.toString();
							if (Intent.ACTION_VIEW.equals(intent.getAction())
									&& (data.startsWith("content://contacts/people/") || data
											.startsWith("content://com.android.contacts/contacts/lookup/"))) {

								intent = new Intent(
										"com.android.contacts.action.QUICK_CONTACT");
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
										| Intent.FLAG_ACTIVITY_CLEAR_TOP
										| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

								intent.setData(uri);
								intent.putExtra("mode", 3);
								intent.putExtra("exclude_mimes",
										(String[]) null);

								values.clear();
								values.put(BaseLauncherColumns.INTENT,
										intent.toUri(0));

								String updateWhere = BaseColumns._ID + "="
										+ favoriteId;
								db.update(TABLE_FAVORITES, values, updateWhere,
										null);
							}
						} catch (RuntimeException ex) {
							Log.e(TAG, "Problem upgrading shortcut", ex);
						} catch (URISyntaxException e) {
							Log.e(TAG, "Problem upgrading shortcut", e);
						}
					}
				}

				db.setTransactionSuccessful();
			} catch (SQLException ex) {
				Log.w(TAG, "Problem while upgrading contacts", ex);
				return false;
			} finally {
				db.endTransaction();
				if (c != null) {
					c.close();
				}
			}

			return true;
		}

		private void normalizeIcons(SQLiteDatabase db) {
			Log.d(TAG, "normalizing icons");

			db.beginTransaction();
			Cursor c = null;
			SQLiteStatement update = null;
			try {
				boolean logged = false;
				update = db.compileStatement("UPDATE favorites "
						+ "SET icon=? WHERE _id=?");

				c = db.rawQuery(
						"SELECT _id, icon FROM favorites WHERE iconType="
								+ BaseLauncherColumns.ICON_TYPE_BITMAP, null);

				final int idIndex = c.getColumnIndexOrThrow(BaseColumns._ID);
				final int iconIndex = c
						.getColumnIndexOrThrow(BaseLauncherColumns.ICON);

				while (c.moveToNext()) {
					long id = c.getLong(idIndex);
					byte[] data = c.getBlob(iconIndex);
					try {
						Bitmap bitmap = Utilities.resampleIconBitmap(
								BitmapFactory.decodeByteArray(data, 0,
										data.length), mContext);
						if (bitmap != null) {
							update.bindLong(1, id);
							data = ItemInfo.flattenBitmap(bitmap);
							if (data != null) {
								update.bindBlob(2, data);
								update.execute();
							}
							bitmap.recycle();
						}
					} catch (Exception e) {
						if (!logged) {
							Log.e(TAG, "Failed normalizing icon " + id, e);
						} else {
							Log.e(TAG, "Also failed normalizing icon " + id);
						}
						logged = true;
					}
				}
				db.setTransactionSuccessful();
			} catch (SQLException ex) {
				Log.w(TAG,
						"Problem while allocating appWidgetIds for existing widgets",
						ex);
			} finally {
				db.endTransaction();
				if (update != null) {
					update.close();
				}
				if (c != null) {
					c.close();
				}
			}
		}

		/**
		 * Upgrade existing clock and photo frame widgets into their new widget
		 * equivalents.
		 */
		private void convertWidgets(SQLiteDatabase db) {
			final AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(mContext);
			final int[] bindSources = new int[] {
					Favorites.ITEM_TYPE_WIDGET_CLOCK,
					Favorites.ITEM_TYPE_WIDGET_PHOTO_FRAME,
					Favorites.ITEM_TYPE_WIDGET_SEARCH, };

			final String selectWhere = buildOrWhereString(
					BaseLauncherColumns.ITEM_TYPE, bindSources);

			Cursor c = null;

			db.beginTransaction();
			try {
				// Select and iterate through each matching widget
				c = db.query(TABLE_FAVORITES, new String[] { BaseColumns._ID,
						BaseLauncherColumns.ITEM_TYPE }, selectWhere, null,
						null, null, null);

				if (LOGD) {
					Log.d(TAG, "found upgrade cursor count=" + c.getCount());
				}

				final ContentValues values = new ContentValues();
				while (c != null && c.moveToNext()) {
					long favoriteId = c.getLong(0);
					int favoriteType = c.getInt(1);

					// Allocate and update database with new appWidgetId
					try {
						int appWidgetId = mAppWidgetHost.allocateAppWidgetId();

						if (LOGD) {
							Log.d(TAG, "allocated appWidgetId=" + appWidgetId
									+ " for favoriteId=" + favoriteId);
						}
						values.clear();
						values.put(BaseLauncherColumns.ITEM_TYPE,
								Favorites.ITEM_TYPE_APPWIDGET);
						values.put(Favorites.APPWIDGET_ID, appWidgetId);

						// Original widgets might not have valid spans when
						// upgrading
						if (favoriteType == Favorites.ITEM_TYPE_WIDGET_SEARCH) {
							values.put(Favorites.SPANX, 4);
							values.put(Favorites.SPANY, 1);
						} else {
							values.put(Favorites.SPANX, 2);
							values.put(Favorites.SPANY, 2);
						}

						String updateWhere = BaseColumns._ID + "=" + favoriteId;
						db.update(TABLE_FAVORITES, values, updateWhere, null);

						if (favoriteType == Favorites.ITEM_TYPE_WIDGET_CLOCK) {
							appWidgetManager
									.bindAppWidgetId(
											appWidgetId,
											new ComponentName(
													"com.android.alarmclock",
													"com.android.alarmclock.AnalogAppWidgetProvider"));
						} else if (favoriteType == Favorites.ITEM_TYPE_WIDGET_PHOTO_FRAME) {
							appWidgetManager
									.bindAppWidgetId(
											appWidgetId,
											new ComponentName(
													"com.android.camera",
													"com.android.camera.PhotoAppWidgetProvider"));
						} else if (favoriteType == Favorites.ITEM_TYPE_WIDGET_SEARCH) {
							appWidgetManager.bindAppWidgetId(appWidgetId,
									getSearchWidgetProvider());
						}
					} catch (RuntimeException ex) {
						Log.e(TAG, "Problem allocating appWidgetId", ex);
					}
				}

				db.setTransactionSuccessful();
			} catch (SQLException ex) {
				Log.w(TAG,
						"Problem while allocating appWidgetIds for existing widgets",
						ex);
			} finally {
				db.endTransaction();
				if (c != null) {
					c.close();
				}
			}
		}

		/**
		 * Loads the default set of favorite packages from an xml file.
		 * 
		 * @param db
		 *            The database to write the values into
		 */
		private int loadFavorites(SQLiteDatabase db) {
			ContentValues values = new ContentValues();

			PackageManager packageManager = mContext.getPackageManager();
			int i = 0;
			try {
				Context context = Configurator.getConfigContext(mContext);
				XmlResourceParser parser = Configurator.getConfigPackageXml(
						mContext, "default_workspace");
				if (parser == null) {
					context = mContext;
					parser = mContext.getResources().getXml(
							R.xml.default_workspace);
				}

				AttributeSet set = Xml.asAttributeSet(parser);
				XmlUtils.beginDocument(parser, TAG_FAVORITES);

				final int depth = parser.getDepth();

				int type;
				int[] attrs = Configurator.getPackageAttrs(context,
						Configurator.sFavorite);
				HashMap<String, Integer> attrMap = Configurator
						.getPackageStyleable(context, attrs,
								Configurator.sFavorite);

				while (((type = parser.next()) != XmlPullParser.END_TAG || parser
						.getDepth() > depth)
						&& type != XmlPullParser.END_DOCUMENT) {

					Intent intent = new Intent(Intent.ACTION_MAIN, null);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					if (type != XmlPullParser.START_TAG) {
						continue;
					}

					boolean added = false;
					final String name = parser.getName();
					TypedArray a = context.obtainStyledAttributes(set, attrs);

					values.clear();
					values.put(Favorites.CONTAINER, Favorites.CONTAINER_DESKTOP);
					values.put(Favorites.SCREEN,
							a.getString(attrMap.get(Configurator.SCREEN)));
					values.put(Favorites.CONTAINER, a.getInt(
							attrMap.get(Configurator.CONTAINER),
							Favorites.CONTAINER_DESKTOP));
					values.put(Favorites.CELLX,
							a.getString(attrMap.get(Configurator.CELLX)));
					values.put(Favorites.CELLY,
							a.getString(attrMap.get(Configurator.CELLY)));

					if (TAG_FAVORITE.equals(name)) {
						added = addAppShortcut(db, values, a, attrMap,
								packageManager, intent);
					} else if (TAG_SEARCH.equals(name)) {
						added = addSearchWidget(db, values);
					} else if (TAG_CLOCK.equals(name)) {
						added = addClockWidget(db, values);
					} else if (TAG_APPWIDGET.equals(name)) {
						added = addAppWidget(db, values, a, attrMap,
								packageManager);
					} else if (TAG_SHORTCUT.equals(name)) {
						added = addUriShortcut(db, values, a, attrMap);
					} else if (TAG_LOCKSCREENAPPWIDGET.equals(name)) {
						added = addLockScreenAppWidget(db, values);
					} else if (TAG_CLEANMEMAPPWIDGET.equals(name)) {
						added = addCleanMemAppWidget(db, values);
					}

					if (added) {
						i++;
					}

					a.recycle();
				}
			} catch (XmlPullParserException e) {
				Log.w(TAG, "Got exception parsing favorites.", e);
			} catch (IOException e) {
				Log.w(TAG, "Got exception parsing favorites.", e);
			}

			return i;
		}

		private boolean addAppShortcut(SQLiteDatabase db, ContentValues values,
				TypedArray a, HashMap<String, Integer> attrMap,
				PackageManager packageManager, Intent intent) {
			ActivityInfo info;
			String type = a.getString(attrMap.get(Configurator.TYPE));

			if (type != null && type.equals(FAVORITE_ACTION)) {
				Log.d(TAG_SHORTCUT, "find a shortcut with action");
				String action = a.getString(attrMap.get(Configurator.ACTION));
				String uri = a.getString(attrMap.get(Configurator.URI));
				String dataType = a.getString(attrMap
						.get(Configurator.DATATYPE));

				if (action == null || (uri == null && dataType == null)) {
					return false;
				}
				try {
					Intent actionIntent = new Intent();
					actionIntent.setAction(action);
					if (uri != null && dataType != null) {
						actionIntent.setDataAndType(Uri.parse(uri), dataType);
					} else if (uri != null && dataType == null) {
						actionIntent.setData(Uri.parse(uri));
					} else if (uri == null && dataType != null) {
						actionIntent.setDataAndType(Uri.parse(""), dataType);
					}

					List<ResolveInfo> list = packageManager
							.queryIntentActivities(actionIntent, 0);
					if (list == null || list.size() == 0) {
						return false;
					}
					// Always use the first matched activity for actionIntent
					ResolveInfo appInfo = list.get(0);
					if (appInfo != null) {
						ComponentName cn = new ComponentName(
								appInfo.activityInfo.packageName,
								appInfo.activityInfo.name);

						intent.setComponent(cn);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

						values.put(BaseLauncherColumns.INTENT, intent.toUri(0));
						values.put(BaseLauncherColumns.TITLE,
								appInfo.activityInfo.loadLabel(packageManager)
										.toString());
						values.put(BaseLauncherColumns.ITEM_TYPE,
								BaseLauncherColumns.ITEM_TYPE_APPLICATION);
						values.put(Favorites.SPANX, 1);
						values.put(Favorites.SPANY, 1);

						db.insert(TABLE_FAVORITES, null, values);
					}
				} catch (Exception e) {
					Log.w(TAG, "Unable to add favorite: " + action + "/" + uri,
							e);
					return false;
				}
			} else {
				String packageName = a.getString(attrMap
						.get(Configurator.PACKAGENAME));
				String className = a.getString(attrMap
						.get(Configurator.CLASSNAME));

				try {
					ComponentName cn;
					try {
						cn = new ComponentName(packageName, className);
						info = packageManager.getActivityInfo(cn, 0);
					} catch (PackageManager.NameNotFoundException nnfe) {
						String[] packages = packageManager
								.currentToCanonicalPackageNames(new String[] { packageName });
						cn = new ComponentName(packages[0], className);
						info = packageManager.getActivityInfo(cn, 0);
					}

					intent.setComponent(cn);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					values.put(BaseLauncherColumns.INTENT, intent.toUri(0));
					values.put(BaseLauncherColumns.TITLE,
							info.loadLabel(packageManager).toString());
					values.put(BaseLauncherColumns.ITEM_TYPE,
							BaseLauncherColumns.ITEM_TYPE_APPLICATION);
					values.put(Favorites.SPANX, 1);
					values.put(Favorites.SPANY, 1);
					db.insert(TABLE_FAVORITES, null, values);
				} catch (PackageManager.NameNotFoundException e) {
					Log.w(TAG, "Unable to add favorite: " + packageName + "/"
							+ className, e);
					return false;
				}
			}
			return true;
		}

		private ComponentName getSearchWidgetProvider() {
			SearchManager searchManager = (SearchManager) mContext
					.getSystemService(Context.SEARCH_SERVICE);
			ComponentName searchComponent = searchManager
					.getGlobalSearchActivity();
			if (searchComponent == null) {
				return null;
			}
			return getProviderInPackage(searchComponent.getPackageName());
		}

		/**
		 * Gets an appwidget provider from the given package. If the package
		 * contains more than one appwidget provider, an arbitrary one is
		 * returned.
		 */
		private ComponentName getProviderInPackage(String packageName) {
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(mContext);
			List<AppWidgetProviderInfo> providers = appWidgetManager
					.getInstalledProviders();
			if (providers == null) {
				return null;
			}
			final int providerCount = providers.size();
			for (int i = 0; i < providerCount; i++) {
				ComponentName provider = providers.get(i).provider;
				if (provider != null
						&& provider.getPackageName().equals(packageName)) {
					return provider;
				}
			}
			return null;
		}

		private boolean addSearchWidget(SQLiteDatabase db, ContentValues values) {
			ComponentName cn = getSearchWidgetProvider();
			return addAppWidget(db, values, cn, 4, 1);
		}

		private boolean addClockWidget(SQLiteDatabase db, ContentValues values) {
			ComponentName cn = new ComponentName("com.android.alarmclock",
					"com.android.alarmclock.AnalogAppWidgetProvider");
			return addAppWidget(db, values, cn, 2, 2);
		}

		private boolean addLockScreenAppWidget(SQLiteDatabase db,
				ContentValues values) {
			values.put(BaseLauncherColumns.ITEM_TYPE,
					Favorites.ITEM_TYPE_WIDGET_LOCK_SCREEN);
			db.insert(TABLE_FAVORITES, null, values);
			return true;
		}

		private boolean addCleanMemAppWidget(SQLiteDatabase db,
				ContentValues values) {
			values.put(BaseLauncherColumns.ITEM_TYPE,
					Favorites.ITEM_TYPE_WIDGET_CLEAN_MEMORY);
			db.insert(TABLE_FAVORITES, null, values);
			return true;
		}

		private boolean addAppWidget(SQLiteDatabase db, ContentValues values,
				TypedArray a, HashMap<String, Integer> attrMap,
				PackageManager packageManager) {
			String packageName = a.getString(attrMap
					.get(Configurator.PACKAGENAME));
			String className = a.getString(attrMap.get(Configurator.CLASSNAME));

			if (packageName == null || className == null) {
				return false;
			}

			boolean hasPackage = true;
			ComponentName cn = new ComponentName(packageName, className);
			try {
				packageManager.getReceiverInfo(cn, 0);
			} catch (Exception e) {
				String[] packages = packageManager
						.currentToCanonicalPackageNames(new String[] { packageName });
				cn = new ComponentName(packages[0], className);
				try {
					packageManager.getReceiverInfo(cn, 0);
				} catch (Exception e1) {
					hasPackage = false;
				}
			}

			if (hasPackage) {
				int spanX = a.getInt(R.styleable.Favorite_spanX, 0);
				int spanY = a.getInt(R.styleable.Favorite_spanY, 0);
				return addAppWidget(db, values, cn, spanX, spanY);
			}

			return false;
		}

		private boolean addAppWidget(SQLiteDatabase db, ContentValues values,
				ComponentName cn, int spanX, int spanY) {
			boolean allocatedAppWidgets = false;
			final AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(mContext);

			try {
				int appWidgetId = mAppWidgetHost.allocateAppWidgetId();

				values.put(BaseLauncherColumns.ITEM_TYPE,
						Favorites.ITEM_TYPE_APPWIDGET);
				values.put(Favorites.SPANX, spanX);
				values.put(Favorites.SPANY, spanY);
				values.put(Favorites.APPWIDGET_ID, appWidgetId);
				db.insert(TABLE_FAVORITES, null, values);

				allocatedAppWidgets = true;

				appWidgetManager.bindAppWidgetId(appWidgetId, cn);
			} catch (RuntimeException ex) {
				Log.e(TAG, "Problem allocating appWidgetId", ex);
			}

			return allocatedAppWidgets;
		}

		private boolean addUriShortcut(SQLiteDatabase db, ContentValues values,
				TypedArray a, HashMap<String, Integer> attrMap) {
			Resources r = mContext.getResources();

			final int iconResId = a.getResourceId(
					attrMap.get(Configurator.ICON), 0);
			final int titleResId = a.getResourceId(
					attrMap.get(Configurator.TITLE), 0);

			Intent intent;
			String uri = null;
			try {
				uri = a.getString(R.styleable.Favorite_uri);
				intent = Intent.parseUri(uri, 0);
			} catch (URISyntaxException e) {
				Log.w(TAG, "Shortcut has malformed uri: " + uri);
				return false; // Oh well
			}

			if (iconResId == 0 || titleResId == 0) {
				Log.w(TAG, "Shortcut is missing title or icon resource ID");
				return false;
			}

			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			values.put(BaseLauncherColumns.INTENT, intent.toUri(0));
			values.put(BaseLauncherColumns.TITLE, r.getString(titleResId));
			values.put(BaseLauncherColumns.ITEM_TYPE,
					BaseLauncherColumns.ITEM_TYPE_SHORTCUT);
			values.put(Favorites.SPANX, 1);
			values.put(Favorites.SPANY, 1);
			values.put(BaseLauncherColumns.ICON_TYPE,
					BaseLauncherColumns.ICON_TYPE_RESOURCE);
			values.put(BaseLauncherColumns.ICON_PACKAGE,
					mContext.getPackageName());
			values.put(BaseLauncherColumns.ICON_RESOURCE,
					r.getResourceName(iconResId));

			db.insert(TABLE_FAVORITES, null, values);

			return true;
		}
	}

	/**
	 * Build a query string that will match any row where the column matches
	 * anything in the values list.
	 */
	static String buildOrWhereString(String column, int[] values) {
		StringBuilder selectWhere = new StringBuilder();
		for (int i = values.length - 1; i >= 0; i--) {
			selectWhere.append(column).append("=").append(values[i]);
			if (i > 0) {
				selectWhere.append(" OR ");
			}
		}
		return selectWhere.toString();
	}

	static class SqlArguments {
		public final String table;
		public final String where;
		public final String[] args;

		SqlArguments(Uri url, String where, String[] args) {
			if (url.getPathSegments().size() == 1) {
				this.table = url.getPathSegments().get(0);
				this.where = where;
				this.args = args;
			} else if (url.getPathSegments().size() != 2) {
				throw new IllegalArgumentException("Invalid URI: " + url);
			} else if (!TextUtils.isEmpty(where)) {
				throw new UnsupportedOperationException(
						"WHERE clause not supported: " + url);
			} else {
				this.table = url.getPathSegments().get(0);
				this.where = "_id=" + ContentUris.parseId(url);
				this.args = null;
			}
		}

		SqlArguments(Uri url) {
			if (url.getPathSegments().size() == 1) {
				table = url.getPathSegments().get(0);
				where = null;
				args = null;
			} else {
				throw new IllegalArgumentException("Invalid URI: " + url);
			}
		}
	}
}

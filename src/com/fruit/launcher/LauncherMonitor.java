package com.fruit.launcher;

import java.util.ArrayList;

import com.google.android.collect.Lists;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.Telephony;
import android.provider.CallLog.Calls;
import android.util.Log;

public class LauncherMonitor {

	static final String TAG = "LauncherMonitor";

	public static final int MONITOR_NONE = 10;
	public static final int MONITOR_PHONE = 11;
	public static final int MONITOR_MESSAGE = 12;
	public static final int MONITOR_UPDATE = 13;

	private Handler mHandler = new Handler();
	private DataObserver mDataObserver;

	private Context mContext;
	private boolean mbEnable;
	private int mCount;
	private int mMonitorType;
	private ArrayList<InfoCallback> mInfoCallbacks = Lists.newArrayList();

	interface InfoCallback {
		void onInfoCountChanged(int count);
	}

	public LauncherMonitor(Context context, int type) {
		mContext = context;
		mCount = 0;
		mMonitorType = type;
		mbEnable = false;
	}

	public void registerInfoCallback(InfoCallback callback) {
		if (!mInfoCallbacks.contains(callback)) {
			mInfoCallbacks.add(callback);
			callback.onInfoCountChanged(mCount);
		} else {
			Log.e(TAG, "Object tried to add another INFO callback",
					new Exception("Whoops"));
		}

		if (!mbEnable) {
			startMonitor();
		}
	}

	public void removeCallback(Object observer) {
		mInfoCallbacks.remove(observer);

		if (mInfoCallbacks.size() == 0) {
			stopMonitor();
		}
	}

	public void removeAllCallback() {
		mInfoCallbacks.clear();
		stopMonitor();
	}

	private Uri getMoinitorUri(int type) {
		switch (type) {
		case MONITOR_PHONE:
			return Calls.CONTENT_URI;
		case MONITOR_MESSAGE:
			return Telephony.MmsSms.CONTENT_URI;
		case MONITOR_UPDATE:
			return null; // update uri
		default:
			break;
		}

		return null;
	}

	private void startMonitor() {
		ContentResolver resolver = mContext.getContentResolver();
		final Uri uri = getMoinitorUri(mMonitorType);

		if (uri != null) {
			mbEnable = true;
			mDataObserver = new DataObserver(mHandler);
			resolver.registerContentObserver(uri, true, mDataObserver);

			mDataObserver.onChange(true);
		}
	}

	private void stopMonitor() {
		if (mDataObserver != null) {
			ContentResolver resolver = mContext.getContentResolver();
			resolver.unregisterContentObserver(mDataObserver);
			mDataObserver.stop();
			mDataObserver = null;
			mbEnable = false;
		}
	}

	public int getInfoCount() {
		return mCount;
	}

	private void updateMissNumber() {
		int count = queryMissCount(mMonitorType);
		if (count != mCount) {
			mCount = count;
			for (int i = 0; i < mInfoCallbacks.size(); i++) {
				mInfoCallbacks.get(i).onInfoCountChanged(count);
			}
		}
	}

	private int queryMissCount(int type) {
		int count = -1;
		switch (type) {
		case MONITOR_PHONE:
			count = queryMissCallCount(mContext);
			break;
		case MONITOR_MESSAGE:
			count = queryMissMssCount(mContext);
			break;
		case MONITOR_UPDATE:
			count = 1;// queryMissUpdCount(mContext);
		default:
			break;
		}

		return count;
	}

	public static int queryMissMssCount(Context context) {
		int smsCount = queryMissMssCount(context,
				Uri.parse("content://sms/inbox"));
		int mmsCount = queryMissMssCount(context,
				Uri.parse("content://mms/inbox"));
		Log.d(TAG, "getMissMssCount count=" + (smsCount + mmsCount));
		return smsCount + mmsCount;
	}

	private static int queryMissMssCount(Context context, Uri uri) {
		int count = 0;
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(uri,
					new String[] { "_id" }, "read=0 AND seen=0", null, null);
		} catch (IllegalStateException e) {
			System.gc();
		}
		if (cursor != null) {
			count = cursor.getCount();
			cursor.close();
		}
		return count;
	}

	private static int queryMissCallCount(Context context) {
		int count = 0;

		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(Calls.CONTENT_URI,
					null, "type=3 and new=1", null, null);
		} catch (IllegalStateException e) {
			System.gc();
		}
		if (cursor != null) {
			count = cursor.getCount();
			cursor.close();
		}
		Log.d(TAG, "getMissCallCount count=" + count);
		return count;
	}

	private class DataObserver extends ContentObserver {
		Runnable mRunnable;

		public DataObserver(Handler handler) {
			super(handler);
			mRunnable = new Runnable() {
				@Override
				public void run() {
					synchronized (mRunnable) {
						mHandler.removeCallbacks(mRunnable);
						updateMissNumber();
					}
				}
			};
		}

		@Override
		public void onChange(boolean selfChange) {
			synchronized (mRunnable) {
				mHandler.post(mRunnable);
			}
		}

		public void stop() {
			mHandler.removeCallbacks(mRunnable);
		}
	}

}

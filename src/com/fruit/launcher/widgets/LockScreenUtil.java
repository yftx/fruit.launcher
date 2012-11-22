package com.fruit.launcher.widgets;

import com.fruit.launcher.R;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class LockScreenUtil {

	private static LockScreenUtil mInstance;
	private Context mContext;
	private DevicePolicyManager mDevicePolicyManager;
	private ComponentName mLockScreenAdmin;

	private LockScreenUtil(Context context) {
		this.mContext = context;
		mDevicePolicyManager =
			(DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		mLockScreenAdmin = new ComponentName(context, LockScreenAdmin.class); 
	}

	public static final LockScreenUtil getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new LockScreenUtil(context);
		}
		return mInstance;
	}

	public final boolean isAdminActive() {
		return mDevicePolicyManager.isAdminActive(mLockScreenAdmin);
	}

	public final void lockScreen() {
		if (mDevicePolicyManager.isAdminActive(mLockScreenAdmin)) {
			mDevicePolicyManager.lockNow();
		} else {
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mLockScreenAdmin);
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
					mContext.getString(R.string.widget_lock_screen_tips));

			mContext.startActivity(intent);
		}
	}

	public final void removeAdmin() {
		mDevicePolicyManager.removeActiveAdmin(mLockScreenAdmin);
	}
}
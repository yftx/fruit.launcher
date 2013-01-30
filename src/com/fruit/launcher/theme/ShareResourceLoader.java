package com.fruit.launcher.theme;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

public final class ShareResourceLoader {

	private static final String TAG = "ShareResourceLoader";
	public static final int INVALID_INT = -999999;

	public static enum BoolVal {
		BTRUE, BFALSE, BINVALID
	}

	private Context mContext;
	private Resources mResources;
	private String mResPkgName;

	// private static ShareResourceLoader sInstance;

	public ShareResourceLoader(Context context, String packageName)
			throws NameNotFoundException {
		this.mContext = context.createPackageContext(packageName,
				Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

		if (mContext != null) {
			this.mResources = mContext.getResources();
			this.mResPkgName = packageName;
		} else {
			Log.w(TAG, "ShareResourceLoader create fail! packageName="
					+ packageName);
		}
	}

	// public static ShareResourceLoader getInstance(Context context, String
	// packageName) {
	// if (sInstance == null) {
	// try {
	// sInstance = new ShareResourceLoader(context, packageName);
	// } catch (NameNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// return sInstance;
	// }

	public String getResourcePkgName() {
		return mResPkgName;
	}

	public final String loadString(String resName) {
		final Resources res = mResources;
		final String pkgName = mResPkgName;
		if (res != null && pkgName != null) {
			int resId = res.getIdentifier(resName, "string", mResPkgName);

			if (resId > 0) {
				return res.getString(resId);
			} else {
				Log.w(TAG, "loadString fail! resName=" + resName);
			}
		}
		return null;
	}

	public final Drawable loadDrawable(String resName) {
		final Resources res = mResources;
		final String pkgName = mResPkgName;
		if (res != null && pkgName != null) {
			int resId = res.getIdentifier(resName, "drawable", mResPkgName);

			if (resId > 0) {
				return mResources.getDrawable(resId);
			} else {
				Log.w(TAG, "loadDrawable fail! resName=" + resName);
			}
		}
		return null;
	}

	public final Bitmap loadBitmap(String resName) {
		final Resources res = mResources;
		final String pkgName = mResPkgName;
		Bitmap bitmap = null;

		if (res != null && pkgName != null) {
			int resId = res.getIdentifier(resName, "drawable", mResPkgName);

			if (resId > 0) {
				BitmapFactory.Options option = new BitmapFactory.Options();
				option.inDither = false;
				option.inPreferredConfig = Bitmap.Config.ARGB_8888;
				//option.inSampleSize = 4;
				try {
					bitmap = BitmapFactory.decodeResource(res, resId, option);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					bitmap = null;
					Log.w(TAG, "loadDrawable fail! OutOfMemoryError");
				}
			} else {
				Log.w(TAG, "loadDrawable fail! resName=" + resName);
			}

		}
		return bitmap;
	}

	public final String[] loadStringArray(String resName) {
		final Resources res = mResources;
		final String pkgName = mResPkgName;
		String[] array = null;

		if (res != null && pkgName != null) {
			int listId = res.getIdentifier(resName, "array", pkgName);
			if (listId > 0) {
				array = res.getStringArray(listId);
			} else {
				Log.w(TAG, "loadStringArray fail! resName=" + resName);
			}
		}
		return array;
	}

	public final int loadColor(String resName) {
		final Resources res = mResources;
		final String pkgName = mResPkgName;

		if (res != null && pkgName != null) {
			int resId = res.getIdentifier(resName, "color", mResPkgName);

			if (resId > 0) {
				return res.getColor(resId);
			} else {
				Log.w(TAG, "loadColor fail! resName=" + resName);
			}
		}
		return INVALID_INT;
	}

	public final XmlResourceParser loadXml(String resName) {
		final Resources res = mResources;
		final String pkgName = mResPkgName;
		if (res != null && pkgName != null) {
			int resId = res.getIdentifier(resName, "xml", mResPkgName);

			if (resId > 0) {
				return mResources.getXml(resId);
			} else {
				Log.w(TAG, "loadXml fail! resName=" + resName);
			}
		}
		return null;
	}

	public final int loadAttrID(String resName) {
		final Resources res = mResources;
		final String pkgName = mResPkgName;
		if (res != null && pkgName != null) {
			int resId = res.getIdentifier(resName, "attr", mResPkgName);
			if (resId > 0) {
				return resId;
			} else {
				Log.w(TAG, "loadAttrID fail! resName=" + resName);
			}
		}
		return INVALID_INT;
	}

	public final int[] loadAttrIDArray(String[] attrsName) {
		int length = attrsName.length;
		int[] value = new int[length];
		for (int i = 0; i < length; i++) {
			value[i] = loadAttrID(attrsName[i]);
		}
		return value;
	}

	public final int loadIntegerConfig(String resName) {
		final Resources res = mResources;
		final String pkgName = mResPkgName;

		if (res != null && pkgName != null) {
			int resId = res.getIdentifier(resName, "integer", pkgName);
			if (resId > 0) {
				return res.getInteger(resId);
			} else {
				Log.w(TAG, "loadIntegerConfig fail! resName=" + resName);
			}
		}
		return INVALID_INT;
	}

	public final String loadStringConfig(String resName) {
		final Resources res = mResources;
		final String pkgName = mResPkgName;
		if (res != null && pkgName != null) {
			int resId = res.getIdentifier(resName, "string", pkgName);
			if (resId > 0) {
				return res.getString(resId);
			} else {
				Log.w(TAG, "loadStringConfig fail! resName=" + resName);
			}
		}
		return null;
	}

	public final BoolVal loadBoolConfig(String name) {
		final Resources res = mResources;
		final String pkgName = mResPkgName;
		if (res != null && pkgName != null) {
			int resId = res.getIdentifier(name, "bool", pkgName);
			if (resId > 0) {
				boolean value = res.getBoolean(resId);
				if (value) {
					return BoolVal.BTRUE;
				} else {
					return BoolVal.BFALSE;
				}
			} else {
				Log.w(TAG, "loadBoolConfig fail! name=" + name);
			}
		}
		return BoolVal.BINVALID;
	}

	public final int loadDimensPixel(String resName) {
		final Resources res = mResources;
		final String pkgName = mResPkgName;
		if (res != null && pkgName != null) {
			int resId = res.getIdentifier(resName, "dimen", pkgName);
			if (resId > 0) {
				return res.getDimensionPixelSize(resId);
			} else {
				Log.w(TAG, "loadDimensPixel fail! resName=" + resName);
			}
		}
		return INVALID_INT;
	}
}
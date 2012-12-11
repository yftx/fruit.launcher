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

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.TableMaskFilter;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.fruit.launcher.theme.ThemeManager;

/**
 * Various utilities shared amongst the Launcher's classes.
 */
public final class Utilities {

	private static final String TAG = "Launcher.Utilities";
	private static final boolean DEBUG = false;

	private static final boolean TEXT_BURN = false;

	public static float sDensity = 0.0f;

	private static int sIconWidth = -1;
	private static int sIconHeight = -1;
	private static int sIconTextureWidth = -1;
	private static int sIconTextureHeight = -1;

	private static final Paint sBlurPaint = new Paint();
	private static final Paint sGlowColorPressedPaint = new Paint();
	private static final Paint sGlowColorFocusedPaint = new Paint();
	private static final Paint sDisabledPaint = new Paint();
	private static final Rect sOldBounds = new Rect();
	private static final Canvas sCanvas = new Canvas();

	static {
		sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
				Paint.FILTER_BITMAP_FLAG));
	}

	static Bitmap centerToFit(Bitmap bitmap, int width, int height,
			Context context) {
		final int bitmapWidth = bitmap.getWidth();
		final int bitmapHeight = bitmap.getHeight();

		if (bitmapWidth < width || bitmapHeight < height) {
			int color = context.getResources().getColor(
					R.color.window_background);

			Bitmap centered = Bitmap.createBitmap(bitmapWidth < width ? width
					: bitmapWidth, bitmapHeight < height ? height
					: bitmapHeight, Bitmap.Config.RGB_565);
			centered.setDensity(bitmap.getDensity());
			Canvas canvas = new Canvas(centered);
			canvas.drawColor(color);
			canvas.drawBitmap(bitmap, (width - bitmapWidth) / 2.0f,
					(height - bitmapHeight) / 2.0f, null);

			bitmap = centered;
		}

		return bitmap;
	}

	static int sColors[] = { 0xffff0000, 0xff00ff00, 0xff0000ff };
	static int sColorIndex = 0;

	/**
	 * Returns a bitmap suitable for the all apps view. The bitmap will be a
	 * power of two sized ARGB_8888 bitmap that can be used as a gl texture.
	 */
	public static Bitmap createIconBitmap(Drawable icon, Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconWidth == -1) {
				initStatics(context);
			}

			int width = sIconWidth;
			int height = sIconHeight;

			if (icon instanceof PaintDrawable) {
				PaintDrawable painter = (PaintDrawable) icon;
				painter.setIntrinsicWidth(width);
				painter.setIntrinsicHeight(height);
			} else if (icon instanceof BitmapDrawable) {
				// Ensure the bitmap has a density.
				BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
				Bitmap bitmap = bitmapDrawable.getBitmap();
				if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
					bitmapDrawable.setTargetDensity(context.getResources()
							.getDisplayMetrics());
				}
			}
			int sourceWidth = icon.getIntrinsicWidth();
			int sourceHeight = icon.getIntrinsicHeight();

			if (sourceWidth > 0 && sourceHeight > 0) {
				// There are intrinsic sizes.
				if (width < sourceWidth || height < sourceHeight) {
					// It's too big, scale it down.
					final float ratio = (float) sourceWidth / sourceHeight;
					if (sourceWidth > sourceHeight) {
						height = (int) (width / ratio);
					} else if (sourceHeight > sourceWidth) {
						width = (int) (height * ratio);
					}
				} else if (sourceWidth < width && sourceHeight < height) {
					// It's small, use the size they gave us.
					width = sourceWidth;
					height = sourceHeight;
				}
			}

			// no intrinsic size --> use default size
			int textureWidth = sIconTextureWidth;
			int textureHeight = sIconTextureHeight;

			final Bitmap bitmap = Bitmap.createBitmap(textureWidth,
					textureHeight, Bitmap.Config.ARGB_8888);
			final Canvas canvas = sCanvas;
			canvas.setBitmap(bitmap);

			final int left = (textureWidth - width) / 2;
			final int top = (textureHeight - height) / 2;

			if (DEBUG) {
				// draw a big box for the icon for debugging
				canvas.drawColor(sColors[sColorIndex]);
				if (++sColorIndex >= sColors.length) {
					sColorIndex = 0;
				}
				Paint debugPaint = new Paint();
				debugPaint.setColor(0xffcccc00);
				canvas.drawRect(left, top, left + width, top + height,
						debugPaint);
			}

			sOldBounds.set(icon.getBounds());
			icon.setBounds(left, top, left + width, top + height);
			icon.draw(canvas);
			icon.setBounds(sOldBounds);

			return bitmap;
		}
	}

	static void drawSelectedAllAppsBitmap(Canvas dest, int destWidth,
			int destHeight, boolean pressed, Bitmap src) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconWidth == -1) {
				// We can't have gotten to here without src being initialized,
				// which
				// comes from this file already. So just assert.
				// initStatics(context);
				throw new RuntimeException(
						"Assertion failed: Utilities not initialized");
			}

			dest.drawColor(0, PorterDuff.Mode.CLEAR);

			int[] xy = new int[2];
			Bitmap mask = src.extractAlpha(sBlurPaint, xy);

			float px = (destWidth - src.getWidth()) / 2;
			float py = (destHeight - src.getHeight()) / 2;
			dest.drawBitmap(mask, px + xy[0], py + xy[1],
					pressed ? sGlowColorPressedPaint : sGlowColorFocusedPaint);

			mask.recycle();
		}
	}

	/**
	 * Returns a Bitmap representing the thumbnail of the specified Bitmap. The
	 * size of the thumbnail is defined by the dimension
	 * android.R.dimen.launcher_application_icon_size.
	 * 
	 * @param bitmap
	 *            The bitmap to get a thumbnail of.
	 * @param context
	 *            The application's context.
	 * 
	 * @return A thumbnail for the specified bitmap or the bitmap itself if the
	 *         thumbnail could not be created.
	 */
	static Bitmap resampleIconBitmap(Bitmap bitmap, Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconWidth == -1) {
				initStatics(context);
			}

			if (bitmap.getWidth() == sIconWidth
					&& bitmap.getHeight() == sIconHeight) {
				return bitmap;
			} else {
				return createIconBitmap(new BitmapDrawable(bitmap), context);
			}
		}
	}

	static Bitmap drawDisabledBitmap(Bitmap bitmap, Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconWidth == -1) {
				initStatics(context);
			}
			final Bitmap disabled = Bitmap.createBitmap(bitmap.getWidth(),
					bitmap.getHeight(), Bitmap.Config.ARGB_8888);
			final Canvas canvas = sCanvas;
			canvas.setBitmap(disabled);
			canvas.drawBitmap(bitmap, 0.0f, 0.0f, sDisabledPaint);

			return disabled;
		}
	}

	private static void initStatics(Context context) {
		final Resources resources = context.getResources();
		final DisplayMetrics metrics = resources.getDisplayMetrics();
		final float density = metrics.density;

		sIconWidth = sIconHeight = (int) resources
				.getDimension(android.R.dimen.app_icon_size);
		sIconTextureWidth = sIconTextureHeight = sIconWidth + 2;

		sBlurPaint.setMaskFilter(new BlurMaskFilter(5 * density,
				BlurMaskFilter.Blur.NORMAL));
		sGlowColorPressedPaint.setColor(0xffffc300);
		sGlowColorPressedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0,
				30));
		sGlowColorFocusedPaint.setColor(0xffff8e00);
		sGlowColorFocusedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0,
				30));

		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0.2f);
		sDisabledPaint.setColorFilter(new ColorMatrixColorFilter(cm));
		sDisabledPaint.setAlpha(0x88);
	}

	static class BubbleText {

		private static final int MAX_LINES = 2;

		private final TextPaint mTextPaint;

		private final RectF mBubbleRect = new RectF();

		private final float mTextWidth;
		private final int mLeading;
		private final int mFirstLineY;
		private final int mLineHeight;

		private final int mBitmapWidth;
		private final int mBitmapHeight;
		private final int mDensity;

		BubbleText(Context context) {
			final Resources resources = context.getResources();

			final DisplayMetrics metrics = resources.getDisplayMetrics();
			final float scale = metrics.density;
			mDensity = metrics.densityDpi;

			final float paddingLeft = 2.0f * scale;
			final float paddingRight = 2.0f * scale;
			final float cellWidth = resources
					.getDimension(R.dimen.title_texture_width);

			RectF bubbleRect = mBubbleRect;
			bubbleRect.left = 0;
			bubbleRect.top = 0;
			bubbleRect.right = (int) cellWidth;

			mTextWidth = cellWidth - paddingLeft - paddingRight;

			TextPaint textPaint = mTextPaint = new TextPaint();
			textPaint.setTypeface(Typeface.DEFAULT);
			textPaint.setTextSize(13 * scale);
			textPaint.setColor(0xffffffff);
			textPaint.setAntiAlias(true);
			if (TEXT_BURN) {
				textPaint.setShadowLayer(8, 0, 0, 0xff000000);
			}

			float ascent = -textPaint.ascent();
			float descent = textPaint.descent();
			float leading = 0.0f;// (ascent+descent) * 0.1f;
			mLeading = (int) (leading + 0.5f);
			mFirstLineY = (int) (leading + ascent + 0.5f);
			mLineHeight = (int) (leading + ascent + descent + 0.5f);

			mBitmapWidth = (int) (mBubbleRect.width() + 0.5f);
			mBitmapHeight = roundToPow2((int) ((MAX_LINES * mLineHeight)
					+ leading + 0.5f));

			mBubbleRect.offsetTo((mBitmapWidth - mBubbleRect.width()) / 2, 0);

			if (DEBUG) {
				Log.d(TAG, "mBitmapWidth=" + mBitmapWidth + " mBitmapHeight="
						+ mBitmapHeight + " w="
						+ ((int) (mBubbleRect.width() + 0.5f)) + " h="
						+ ((int) ((MAX_LINES * mLineHeight) + leading + 0.5f)));
			}
		}

		/** You own the bitmap after this and you must call recycle on it. */
		Bitmap createTextBitmap(String text) {
			Bitmap b = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight,
					Bitmap.Config.ALPHA_8);
			b.setDensity(mDensity);
			Canvas c = new Canvas(b);

			StaticLayout layout = new StaticLayout(text, mTextPaint,
					(int) mTextWidth, Alignment.ALIGN_CENTER, 1, 0, true);
			int lineCount = layout.getLineCount();
			if (lineCount > MAX_LINES) {
				lineCount = MAX_LINES;
			}
			// if (!TEXT_BURN && lineCount > 0) {
			// RectF bubbleRect = mBubbleRect;
			// bubbleRect.bottom = height(lineCount);
			// c.drawRoundRect(bubbleRect, mCornerRadius, mCornerRadius,
			// mRectPaint);
			// }
			for (int i = 0; i < lineCount; i++) {
				// int x = (int)((mBubbleRect.width() - layout.getLineMax(i)) /
				// 2.0f);
				// int y = mFirstLineY + (i * mLineHeight);
				final String lineText = text.substring(layout.getLineStart(i),
						layout.getLineEnd(i));
				int x = (int) (mBubbleRect.left + ((mBubbleRect.width() - mTextPaint
						.measureText(lineText)) * 0.5f));
				int y = mFirstLineY + (i * mLineHeight);
				c.drawText(lineText, x, y, mTextPaint);
			}

			return b;
		}

		private int height(int lineCount) {
			return (int) ((lineCount * mLineHeight) + mLeading + mLeading + 0.0f);
		}

		int getBubbleWidth() {
			return (int) (mBubbleRect.width() + 0.5f);
		}

		int getMaxBubbleHeight() {
			return height(MAX_LINES);
		}

		int getBitmapWidth() {
			return mBitmapWidth;
		}

		int getBitmapHeight() {
			return mBitmapHeight;
		}
	}

	/** Only works for positive numbers. */
	static int roundToPow2(int n) {
		int orig = n;
		n >>= 1;
		int mask = 0x8000000;
		while (mask != 0 && (n & mask) == 0) {
			mask >>= 1;
		}
		while (mask != 0) {
			n |= mask;
			mask >>= 1;
		}
		n += 1;
		if (n != orig) {
			n <<= 1;
		}
		return n;
	}

	public static Intent orgnizeAppIconIntent(String pkg, String cls) {
		ComponentName componentName = new ComponentName(pkg, cls);
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(componentName);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		return intent;
	}

	public static byte[] getBitmapByte(Bitmap bitmap) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}

	// public static Bitmap scaleBitmap4Launcher(Bitmap b) {
	// if (b == null) {
	// return null;
	// } else {
	// float sxo = 1.0f;
	// float syo = 1.0f;
	// float base = 1.0f;
	// Bitmap oldIcon = b;
	// int width = oldIcon.getWidth();
	// int height = oldIcon.getHeight();
	//
	// //if (DisplayMetrics.DENSITY_DEVICE <= 120) {
	// base = 0.86f;//160.0f / DisplayMetrics.DENSITY_DEVICE;
	// //}
	//
	// if (width > 96)
	// sxo = base / (width/85.0f); //0.86f;
	//
	// if (height > 96)
	// syo = base / (height/85.0f); //0.86f;
	//
	// Bitmap theIcon = Utilities.scaleBitmap(oldIcon, sxo, syo);
	//
	// Log.d(TAG, theIcon.getWidth() + " " + theIcon.getHeight());
	//
	// return theIcon;
	// }
	//
	//
	// }

	public static Bitmap scaleBitmapAfterInstalled(Bitmap b) {
		if (b == null) {
			return null;
		} else {
			float sxo = 1.0f;
			float syo = 1.0f;
			float base = 1.0f;

			// Bitmap oldIcon = b;
			int w = b.getWidth();
			int h = b.getHeight();

			// if (DisplayMetrics.DENSITY_DEVICE <= 120) {
			// base = 0.86f;//160.0f / DisplayMetrics.DENSITY_DEVICE;
			// }
			if ((w < 72) || (w > 85))
				sxo = base / (w / 72.0f); // 0.86f;

			if ((h < 72) || (h > 85))
				syo = base / (h / 72.0f); // 0.86f;

			Bitmap theIcon = Utilities.scaleBitmap(b, sxo, syo);

			Log.d(TAG, theIcon.getWidth() + " " + theIcon.getHeight());

			return theIcon;
		}

	}

	/*
	 * public static Bitmap changeBitmap(Bitmap b) { //return b; if (true) {//
	 * (b.getWidth() == 96 && b.getHeight() == 96) { return b; }
	 * 
	 * final ThemeManager mThemeMgr = ThemeManager.getInstance();
	 * 
	 * Bitmap theIcon = Utilities.scaleBitmap4Launcher(b);
	 * 
	 * Bitmap mBitmap =
	 * Utilities.createCompoundBitmap(mThemeMgr.getRandomAppBgIcon(),
	 * theIcon);//b;
	 * 
	 * return mBitmap; }
	 */

	// public static Bitmap createBitmap4Launcher(Bitmap b) {
	// if (b == null)
	// return null;
	//
	// int w = b.getWidth();
	// int h = b.getHeight();
	//
	// if ((w == 96) && (h == 96)) {
	// return b;
	// }
	//
	// final ThemeManager mThemeMgr = ThemeManager.getInstance();
	//
	// Bitmap theIcon = b;//Utilities.scaleBitmapAfterInstalled(b);
	//
	// Bitmap mBitmap =
	// Utilities.createCompoundBitmap(mThemeMgr.getRandomAppBgIcon(b),
	// theIcon);//b;
	//
	// return mBitmap;
	// }

	public static Bitmap changeBitmap4Launcher(Bitmap b) {
		return b;
		/*
		 * if (b == null) return null;
		 * 
		 * int w = b.getWidth(); int h = b.getHeight();
		 * 
		 * if ((w == 96) && (h == 96)) { return b; }
		 * 
		 * final ThemeManager mThemeMgr = ThemeManager.getInstance();
		 * 
		 * Bitmap mBitmap =
		 * Utilities.createCompoundBitmap(mThemeMgr.getRandomAppBgIcon(b),
		 * b);//b;
		 * 
		 * return mBitmap;
		 */
	}

	static public Bitmap createCompoundBitmapEx(String title, Bitmap icon) {
		final ThemeManager mThemeMgr = ThemeManager.getInstance();
		return createCompoundBitmap(mThemeMgr.getRandomAppBgIcon(title), icon);
	}

	static public Bitmap createCompoundBitmap(Bitmap bgBitmap, Bitmap iconBitmap) {
		float sxo = 1.0f;
		float syo = 1.0f;
		//
		// if (iconBitmap != null) {
		// if (iconBitmap.getWidth() > 72)
		// sxo = 0.86f;
		//
		// if (iconBitmap.getHeight() > 72)
		// syo = 0.86f;
		// }

		if (bgBitmap == null) {
			// final Bitmap icon2 = scaleBitmap(iconBitmap, sxo, syo);
			return iconBitmap;
		}

		float sx = 1.0f;
		float sy = 1.0f;

		if (DisplayMetrics.DENSITY_DEVICE <= 120) {
			sx = 0.86f;
			sy = 0.86f;
			sxo = 0.86f;
			syo = 0.86f;
		}
		/*
		 * if(mIndicator.getOrientation() == Indicator.ORIENTATION_VERTICAL){ sx
		 * = 0.95f; sy = 0.95f; }
		 */
		final Bitmap bg = scaleBitmap(bgBitmap, sx, sy);
		// if (iconBitmap != null) {
		// if (iconBitmap.getWidth() > 72)
		// sxo = 0.86f;
		//
		// if (iconBitmap.getHeight() > 72)
		// syo = 0.86f;
		// }
		final Bitmap icon = scaleBitmap(iconBitmap, sxo, syo);
		final int bgWidth = bg.getWidth();
		final int bgHeight = bg.getHeight();
		final int iconWidth = icon.getWidth();
		final int iconHeight = icon.getHeight();

		Bitmap compoundBitmap = Bitmap.createBitmap(bgWidth, bgWidth,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(compoundBitmap);
		canvas.drawBitmap(bg, 0.0f, 0.0f, null);
		canvas.drawBitmap(icon, (bgWidth - iconWidth) / 2.0f,
				(bgHeight - iconHeight) / 2.0f, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();

		return compoundBitmap;
	}

	/**
	 * scale a bitmap with with*sx, height*sy
	 * 
	 * @param bm
	 * @param sx
	 * @param sy
	 * @return The sacled bitmap
	 */
	public static Bitmap scaleBitmap(Bitmap bm, float sx, float sy) {
		if (sx == 1.0f && sy == 1.0f) {
			return bm;
		}
		Matrix matrix = new Matrix();
		matrix.postScale(sx, sy);
		return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(),
				matrix, true);
	}

	public static boolean isSystemApplication(Context context, String pkgName) {
		final PackageManager pkgManager = context.getPackageManager();
		try {
			PackageInfo pkgInfo = pkgManager.getPackageInfo(pkgName, 0);
			if ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
				return true;
			}
			return false;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean isPackageInstall(Context context, String pkgName) {
		if (pkgName == null || "".equals(pkgName)) {
			return false;
		}

		PackageManager packageManager = context.getPackageManager();
		try {
			packageManager.getPackageInfo(pkgName,
					PackageManager.GET_ACTIVITIES);
			return true;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	// public static Bitmap drawable2bmp(Drawable d) {
	// if (d == null)
	// return null;
	//
	// Bitmap b = null;
	//
	// if (d instanceof FastBitmapDrawable) {
	// b = ((FastBitmapDrawable) d).getBitmap();
	// } else if (d instanceof BitmapDrawable) {
	// b = ((BitmapDrawable)d).getBitmap(); //yfzhao
	// } /*else if (d instanceof LayerDrawable) {
	// b = ((LayerDrawable)d).getBitmap(); //yfzhao
	// } */
	//
	// return b;
	// }

	public static Bitmap drawable2bmp(Drawable d) {
		if (d == null) {
			return null;
		} else {
			return drawableToBitmap(d);
		}
	}

	public static Drawable bmp2drawable(Bitmap b) {
		BitmapDrawable bd = new BitmapDrawable(b);
		return bd;
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		try {
			Bitmap bitmap = Bitmap
					.createBitmap(
							drawable.getIntrinsicWidth(),
							drawable.getIntrinsicHeight(),
							drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
									: Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			// canvas.setBitmap(bitmap);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight());
			drawable.draw(canvas);

			return bitmap;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		}
	}

}
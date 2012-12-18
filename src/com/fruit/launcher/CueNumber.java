package com.fruit.launcher;

import java.net.URISyntaxException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class CueNumber {

	public String mCueNum;
	public boolean mbNumber;
	public int mMonitorType;

	public static final String CUE_MAX = "N";
	public static final double CUE_YPERCENT = 0.68;

	private static final String URI_MMS = "#Intent;"
			+ "action=android.intent.action.MAIN;"
			+ "category=android.intent.category.LAUNCHER;"
			+ "launchFlags=0x10200000;"
			+ "component=com.android.mms/.ui.ConversationList" + ";end";
	private static final String URI_PHONE = "#Intent;"
			+ "action=android.intent.action.MAIN;"
			+ "category=android.intent.category.LAUNCHER;"
			+ "launchFlags=0x10200000;"
			+ "component=com.android.contacts/.DialtactsActivity" + ";end";
	private static final String URI_UPDATE = "#Intent;"
			+ "action=android.intent.action.MAIN;"
			+ "category=android.intent.category.LAUNCHER;"
			+ "launchFlags=0x10200000;"
			+ "component=com.android.contacts/.DialtactsActivity" + ";end";

	public void drawCueNumber(Canvas canvas, Paint mPaint, int w, int h,
			Bitmap mCueBitmap) {
		final Paint paint = mPaint;
		final Bitmap cueIcon = mCueBitmap;
		final String cueText = mCueNum;

		if (cueText != null && cueIcon != null) {
			int top = h - cueIcon.getHeight()*15/8;
			int left = w - cueIcon.getWidth()*11/8;
			float textWidth = paint.measureText(cueText);

			canvas.drawBitmap(cueIcon, left, top, null);
			canvas.drawText(cueText,
					(left + (cueIcon.getWidth() - textWidth) / 2),
					(float) (top + cueIcon.getHeight() * CUE_YPERCENT), paint);
		}
	}

	public void setCueNumber(int number) {
		if (number <= 0) {
			mCueNum = null;
			return;
		} else if (number >= 100) {
			mCueNum = new String(CUE_MAX);
		} else {
			mCueNum = String.valueOf(number);
		}
		// invalidate();
	}

	public int getMonitorType(Intent intent) {
		int type = LauncherMonitor.MONITOR_NONE;
		try {
			if (intent.filterEquals(Intent.parseUri(URI_MMS, 0))) {
				type = LauncherMonitor.MONITOR_MESSAGE;
			} else if (intent.filterEquals(Intent.parseUri(URI_PHONE, 0))) {
				type = LauncherMonitor.MONITOR_PHONE;
			} else if (intent.filterEquals(Intent.parseUri(URI_UPDATE, 0))) {
				type = LauncherMonitor.MONITOR_UPDATE;
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return type;
	}
}

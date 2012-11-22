package com.fruit.launcher;

//import com.fruit.launcher.theme.ThemeManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class CustomAppWidget extends BubbleTextView {

	public CustomAppWidget(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CustomAppWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	static CustomAppWidget fromXml(int resId, Launcher launcher,
			ViewGroup group, CustomAppWidgetInfo widgetInfo) {
		//final ThemeManager mThemeMgr = ThemeManager.getInstance();
		//Bitmap icon = null;

		CustomAppWidget view = (CustomAppWidget) LayoutInflater.from(launcher)
				.inflate(resId, group, false);
		Bitmap icon = Utilities.createIconBitmap(launcher.getResources()
				.getDrawable(widgetInfo.icon), launcher);

//		if ((widgetInfo.cellX <= 1) && (widgetInfo.cellY <= 1)) {
//			// Bitmap icon2 = Utilities.scaleBitmap4Launcher(icon1);
//			// Bitmap icon2 = Utilities.changeBitmap4Launcher(icon1);
////			icon = Utilities.createCompoundBitmap(mThemeMgr
////					.getRandomAppBgIcon(Integer.toString(widgetInfo.title)),
////					icon1);
//			icon = Utilities.createCompoundBitmapEx(
//					Integer.toString(widgetInfo.title), icon1);
//		} else {
//			icon = icon1;
//		}

		view.setCompoundDrawablesWithIntrinsicBounds(null,
				new FastBitmapDrawable(icon), null, null);
		view.setText(widgetInfo.title);
		view.setTag(widgetInfo);
		view.setOnClickListener(launcher);

		return view;
	}
}

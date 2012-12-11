package com.fruit.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

/*  2012-5-10 add for FourLeafsWidget's SettingView start*/
public class SettingView {
	private Launcher mLauncher;
	private Workspace mWorkspace;
	private int mLeafIndex;
	private WindowManager mManager;
	private View mView;
	private ViewContainer mViewContainer;
	private LauncherAppWidgetInfo mAppWidgetInfo;
	private CellLayout mCellLayout;
	private LauncherAppWidgetHostView mLauncherAppWidgetHostView;
	Boolean flag = false;

	public static final String POPUPWINDOW_START_SETTING = "intent.action.START_SETTING";

	public SettingView(Launcher launcher, Workspace workspace, int index,
			LauncherAppWidgetInfo info, LauncherAppWidgetHostView view,
			CellLayout cellLayout) {
		mLauncher = launcher;
		mWorkspace = workspace;
		mLeafIndex = index;
		mAppWidgetInfo = info;
		mCellLayout = cellLayout;
		mLauncherAppWidgetHostView = view;
		mManager = (WindowManager) mLauncher
				.getSystemService(Context.WINDOW_SERVICE);

		initalizeUI();
	}

	private void initalizeUI() {
		mView = mLauncher.getLayoutInflater().inflate(
				R.layout.app_setting_list, null, true);
		ImageView setView = (ImageView) mView.findViewById(R.id.icon1);
		setView.setOnClickListener(new SettingOnclickListener());
		ImageView delView = (ImageView) mView.findViewById(R.id.icon2);
		delView.setOnClickListener(new DeleteOnClickListener());

		mViewContainer = new ViewContainer(mLauncher);
		ViewContainer.LayoutParams listParams = new ViewContainer.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		mViewContainer.addView(mView, listParams);
	}

	public void show(int x, int y) {
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(90,
				70, x - 45, y - 70 - 20,
				WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL,
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
						| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.token = mWorkspace.getWindowToken();

		mManager.addView(mViewContainer, params);
		flag = true;
	}

	private class SettingOnclickListener implements View.OnClickListener {

		@Override
		public void onClick(View arg0) {

		}
	}

	private class DeleteOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View arg0) {
			mLauncher.removeAppWidget(mAppWidgetInfo);

			final LauncherAppWidgetHost appWidgetHost = mLauncher
					.getAppWidgetHost();
			if (appWidgetHost != null) {
				appWidgetHost.deleteAppWidgetId(mAppWidgetInfo.appWidgetId);
			}

			LauncherModel.deleteItemFromDatabase(mLauncher, mAppWidgetInfo);

			mCellLayout.removeView(mLauncherAppWidgetHostView);

			dismiss();
		}
	}

	public void dismiss() {
		if (flag) {
			mManager.removeView(mViewContainer);
			flag = false;
		}
	}

	public Boolean distance(float intiX, float intiY, float endX, float endY) {
		float dirX = Math.abs(intiX - endX);
		float dirY = Math.abs(intiY - endY);
		float radius = (float) Math.sqrt(dirX * dirX + dirY * dirY);
		if (radius < 20) {
			return false;
		}
		return true;
	}

	private class ViewContainer extends FrameLayout {

		public ViewContainer(Context context) {
			super(context);
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent arg0) {
			float touchedX = arg0.getX();
			float touchedY = arg0.getY();
			Rect rect = new Rect();
			this.getDrawingRect(rect);
			Boolean isContain = rect.contains((int) touchedX, (int) touchedY);

			if (!isContain) {
				dismiss();
			}

			return super.dispatchTouchEvent(arg0);
		}
	}
}
/* 2012-5-10 add for FourLeafsWidget's SettingView end */
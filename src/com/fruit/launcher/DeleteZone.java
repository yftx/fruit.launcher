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

import android.widget.ImageView;
import android.widget.Toast;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.graphics.RectF;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;

import com.fruit.launcher.LauncherSettings.Applications;
import com.fruit.launcher.LauncherSettings.BaseLauncherColumns;
import com.fruit.launcher.LauncherSettings.Favorites;
import com.fruit.launcher.widgets.LockScreenUtil;

public class DeleteZone extends ImageView implements DropTarget,
		DragController.DragListener {

	private static final int ORIENTATION_HORIZONTAL = 1;
	private static final int TRANSITION_DURATION = 250;
	private static final int ANIMATION_DURATION = 200;

	private final int[] mLocation = new int[2];

	private Launcher mLauncher;
	private boolean mTrashMode;

	private AnimationSet mInAnimation;
	private AnimationSet mOutAnimation;
	// private Animation mHandleInAnimation;
	// private Animation mHandleOutAnimation;
	private Animation.AnimationListener mAnimationListener;

	private int mOrientation;
	private DragController mDragController;

	private final RectF mRegion = new RectF();
	private TransitionDrawable mTransition;
	// private View mHandle;
	private final Paint mTrashPaint = new Paint();

	public DeleteZone(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DeleteZone(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		final int srcColor = context.getResources().getColor(
				R.color.delete_color_filter);
		mTrashPaint.setColorFilter(new PorterDuffColorFilter(srcColor,
				PorterDuff.Mode.SRC_ATOP));

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.DeleteZone, defStyle, 0);
		mOrientation = a.getInt(R.styleable.DeleteZone_direction,
				ORIENTATION_HORIZONTAL);
		a.recycle();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mTransition = (TransitionDrawable) getBackground();
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		return true;
	}

	@Override
	public Rect estimateDropLocation(DragSource source, int x, int y,
			int xOffset, int yOffset, DragView dragView, Object dragInfo,
			Rect recycle) {
		return null;
	}

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		final ItemInfo item = (ItemInfo) dragInfo;

		if (item.container == -1) {
			String string = getContext().getString(R.string.out_of_space);
			string += String.valueOf(item.container);
			Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
			return;
		}

		if (item instanceof UserFolderInfo) {
			if (((UserFolderInfo) item).contents.size() > 0) {
				dragView.setmCallbackFlag(false); 
			} else {
				dragView.setmCallbackFlag(true); 
			}
			deleteFromWorkspace(source, item);
			return;
		} else if (item instanceof ShortcutInfo || item instanceof ApplicationInfo || item instanceof ApplicationInfoEx) {
			ShortcutInfo info = null;
			
			if (item instanceof ShortcutInfo) {
				info = (ShortcutInfo) item;
			}
			
			if (mLauncher.getWorkspace().isDuplicate(info.title.toString())) {
				dragView.setmCallbackFlag(true); 
				deleteFromWorkspace(source, item);
				return;
			}
			
			// if (mLauncher.isAllAppsVisible()) {
			if ((item.itemType == 0) || (item.itemType == 1)) {
				
				boolean isSysApp = false;
	
				// deleteFromAllApps(item);
				ComponentName cn = ((ShortcutInfo) item).intent.getComponent();
				// Intent it =
				// data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
	
				if (cn == null) {
					dragView.setmCallbackFlag(true);
					deleteFromWorkspace(source, item);
					return;
				}
	
				String pkgName = cn.getPackageName();
	
				if (Utilities.isSystemApplication(mLauncher, pkgName)) {
					isSysApp = true;
				}		

				if (isSysApp) {
					dragView.setmCallbackFlag(false); 
					Toast.makeText(mLauncher, R.string.delete_error_system_app,
							Toast.LENGTH_SHORT).show();
					return;
				}
	
				Uri uri = Uri.parse("package:" + pkgName);
				Intent intent = new Intent(Intent.ACTION_DELETE, uri);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	
				mLauncher.startActivity(intent);
				mLauncher.switchScreenMode(false);
	
				dragView.setmCallbackFlag(false); 
			}
		} else {
			dragView.setmCallbackFlag(true); 
			deleteFromWorkspace(source, item);
		}
	}

	@SuppressWarnings("unused")
	private void deleteFromAllApps(ItemInfo itemInfo) {
		// TODO Auto-generated method stub
		if (itemInfo instanceof ApplicationInfoEx) {
			ApplicationInfoEx appInfo = (ApplicationInfoEx) itemInfo;
			if (appInfo.isSysApp) {
				Toast.makeText(mLauncher, R.string.delete_error_system_app,
						Toast.LENGTH_SHORT).show();
				return;
			}
		}

		switch (itemInfo.itemType) {
		case Applications.APPS_TYPE_APP:
		case Applications.APPS_TYPE_FOLDERAPP:
			String pkgName = ((ApplicationInfoEx) itemInfo).intent
					.getComponent().getPackageName();
			Uri uri = Uri.parse("package:" + pkgName);
			Intent intent = new Intent(Intent.ACTION_DELETE, uri);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			mLauncher.startActivity(intent);
			mLauncher.switchScreenMode(false);
			break;

		case Applications.APPS_TYPE_FOLDER:
			mLauncher.deleteFolderInAllApps((ApplicationFolderInfo) itemInfo);
			break;
		}
	}

	private void deleteFromWorkspace(DragSource source, ItemInfo item) {
		// TODO Auto-generated method stub
		if (item.container == Favorites.CONTAINER_DESKTOP) {
			if (item instanceof LauncherAppWidgetInfo) {
				mLauncher.removeAppWidget((LauncherAppWidgetInfo) item);
			} else if (item instanceof CustomAppWidgetInfo) {
				if (((CustomAppWidgetInfo) item).itemType == Favorites.ITEM_TYPE_WIDGET_LOCK_SCREEN) {
					final ContentResolver cr = mLauncher.getContentResolver();
					final String where = BaseLauncherColumns.ITEM_TYPE + "="
							+ Favorites.ITEM_TYPE_WIDGET_LOCK_SCREEN;
					Cursor c = cr.query(Favorites.CONTENT_URI, null, where,
							null, null);
					// should remove administration when no more LOCK_SCREEN
					// widget displayed in launcher
					if (c.getCount() <= 1) {
						LockScreenUtil.getInstance(mLauncher).removeAdmin();
					}
				}
			}
		} else {
			if (source instanceof UserFolder) {
				final UserFolder userFolder = (UserFolder) source;
				final FolderInfo folderInfo = userFolder.getInfo();
				if (folderInfo instanceof UserFolderInfo) {
					// Item must be a ShortcutInfo otherwise it couldn't have
					// been in the folder
					// in the first place.
					if (item instanceof ShortcutInfo
							&& item.container == folderInfo.id) {
						mLauncher.removeItemFromFolder((ShortcutInfo) item);
					}
				}
			}
		}
		if (item instanceof UserFolderInfo) {
			final UserFolderInfo userFolderInfo = (UserFolderInfo) item;
			if (userFolderInfo.contents.size() > 0) {
				Toast.makeText(mLauncher, R.string.folder_is_not_empty,
						Toast.LENGTH_SHORT).show();
				return;
			}

			LauncherModel.deleteUserFolderContentsFromDatabase(mLauncher,
					userFolderInfo);
			mLauncher.removeFolder(userFolderInfo);
		} else if (item instanceof LauncherAppWidgetInfo) {
			final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) item;
			final LauncherAppWidgetHost appWidgetHost = mLauncher
					.getAppWidgetHost();
			if (appWidgetHost != null) {
				appWidgetHost
						.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
			}
		}
		LauncherModel.deleteItemFromDatabase(mLauncher, item);
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		mTransition.reverseTransition(TRANSITION_DURATION);
		dragView.setPaint(mTrashPaint);
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		mTransition.reverseTransition(TRANSITION_DURATION);
		dragView.setPaint(null);
	}

	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		final ItemInfo item = (ItemInfo) info;
		if (item != null) {
			mLauncher.switchScreenMode(true);
			mTrashMode = true;
			createAnimations();

			final int paddingTop = ((ViewGroup) mLauncher.getWindow()
					.getDecorView()).getChildAt(0).getPaddingTop();
			final int[] location = mLocation;
			getLocationOnScreen(location);
			mRegion.set(location[0], location[1] - paddingTop, location[0]
					+ mRight - mLeft, location[1] + mBottom - mTop - paddingTop);
			mDragController.setDeleteRegion(mRegion);
			mTransition.resetTransition();
			startAnimation(mInAnimation);
			// mHandle.startAnimation(mHandleOutAnimation);
			setVisibility(VISIBLE);
		}
	}

	@Override
	public void onDragEnd() {
		if (mTrashMode) {
			mTrashMode = false;
			mDragController.setDeleteRegion(null);
			mOutAnimation.setAnimationListener(mAnimationListener);
			startAnimation(mOutAnimation);
			// mHandle.startAnimation(mHandleInAnimation);
			setVisibility(GONE);
		}
	}

	private void createAnimations() {
		if (mInAnimation == null) {
			mInAnimation = new FastAnimationSet();
			final AnimationSet animationSet = mInAnimation;
			animationSet.setInterpolator(new AccelerateInterpolator());
			animationSet.addAnimation(new AlphaAnimation(0.0f, 1.0f));
			if (mOrientation == ORIENTATION_HORIZONTAL) {
				animationSet.addAnimation(new TranslateAnimation(
						Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f,
						Animation.RELATIVE_TO_SELF, -1.0f,
						Animation.RELATIVE_TO_SELF, 0.0f));
			} else {
				animationSet.addAnimation(new TranslateAnimation(
						Animation.RELATIVE_TO_SELF, 1.0f,
						Animation.RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE,
						0.0f, Animation.ABSOLUTE, 0.0f));
			}
			animationSet.setDuration(ANIMATION_DURATION);
		}
		// if (mHandleInAnimation == null) {
		// mHandleInAnimation = new AlphaAnimation(0.0f, 1.0f);
		// mHandleInAnimation.setDuration(ANIMATION_DURATION);
		// }
		if (mOutAnimation == null) {
			mOutAnimation = new FastAnimationSet();
			final AnimationSet animationSet = mOutAnimation;
			animationSet.setInterpolator(new AccelerateInterpolator());
			animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));
			if (mOrientation == ORIENTATION_HORIZONTAL) {
				animationSet.addAnimation(new FastTranslateAnimation(
						Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f,
						Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, -1.0f));
			} else {
				animationSet.addAnimation(new FastTranslateAnimation(
						Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, 1.0f, Animation.ABSOLUTE,
						0.0f, Animation.ABSOLUTE, 0.0f));
			}
			animationSet.setDuration(ANIMATION_DURATION);
		}
		// if (mHandleOutAnimation == null) {
		// mHandleOutAnimation = new AlphaAnimation(1.0f, 0.0f);
		// mHandleOutAnimation.setFillAfter(true);
		// mHandleOutAnimation.setDuration(ANIMATION_DURATION);
		// }
		if (mAnimationListener == null) {
			mAnimationListener = new Animation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					mLauncher.switchScreenMode(false);
				}
			};
		}
	}

	void setLauncher(Launcher launcher) {
		mLauncher = launcher;
	}

	void setDragController(DragController dragController) {
		mDragController = dragController;
	}

	// void setHandle(View view) {
	// mHandle = view;
	// }

	private static class FastTranslateAnimation extends TranslateAnimation {
		public FastTranslateAnimation(int fromXType, float fromXValue,
				int toXType, float toXValue, int fromYType, float fromYValue,
				int toYType, float toYValue) {
			super(fromXType, fromXValue, toXType, toXValue, fromYType,
					fromYValue, toYType, toYValue);
		}

		@Override
		public boolean willChangeTransformationMatrix() {
			return true;
		}

		@Override
		public boolean willChangeBounds() {
			return false;
		}
	}

	private static class FastAnimationSet extends AnimationSet {
		FastAnimationSet() {
			super(false);
		}

		@Override
		public boolean willChangeTransformationMatrix() {
			return true;
		}

		@Override
		public boolean willChangeBounds() {
			return false;
		}
	}
}

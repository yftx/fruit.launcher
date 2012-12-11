package com.fruit.launcher;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fruit.launcher.LauncherSettings.Applications;
import com.fruit.launcher.LauncherSettings.BaseLauncherColumns;
import com.fruit.launcher.LauncherSettings.Favorites;

/**
 * Folder which contains applications or shortcuts chosen by the user.
 * 
 */
public class UserFolder extends Folder implements DropTarget {

	private static final String TAG = "UserFolder";

	public int mFolderTopPadding;

	private int mFolderIconWidth;
	private int mFolderIconHeight;
	private int mFolderIconLeft;
	private int mFolderIconTop;

	private View mEmptyView;
	private ViewGroup mFolderContent;
	private View mFolderIcon;
	private View mLeftCorner;
	private View mLeftLine;
	private View mArrow;

	public UserFolder(Context context, AttributeSet attrs) {
		super(context, attrs);
		mFolderTopPadding = 0;
		mFolderIconWidth = 0;
		mFolderIconHeight = 0;
		mFolderIconLeft = 0;
		mFolderIconTop = 0;
	}

	/**
	 * Creates a new UserFolder, inflated from R.layout.user_folder.
	 * 
	 * @param context
	 *            The application's context.
	 * 
	 * @return A new UserFolder.
	 */
	static UserFolder fromXml(Context context) {
		return (UserFolder) LayoutInflater.from(context).inflate(
				R.layout.user_folder, null);
	}

	static ViewGroup getFolderContent(UserFolder folder) {
		return folder.mFolderContent;
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		final ItemInfo item = (ItemInfo) dragInfo;
		final int itemType = item.itemType;
		return (itemType == BaseLauncherColumns.ITEM_TYPE_APPLICATION
				|| itemType == BaseLauncherColumns.ITEM_TYPE_SHORTCUT
				|| itemType == Applications.APPS_TYPE_APP || itemType == Applications.APPS_TYPE_FOLDERAPP)
				&& item.container != mInfo.id;
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
		ShortcutInfo item;
		if (dragInfo instanceof ApplicationInfo) {
			// Came from all applications -- make a copy
			// item = ((ApplicationInfo)dragInfo).makeShortcut();
			ApplicationInfoEx infoEx = (ApplicationInfoEx) dragInfo;

			item = mLauncher.getLauncherModel().getShortcutInfo(
					getContext().getPackageManager(), infoEx.intent,
					getContext());
			item.setActivity(infoEx.intent.getComponent(),
					Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			item.container = ItemInfo.NO_ID;
		} else {
			item = (ShortcutInfo) dragInfo;
		}
		((ShortcutsAdapter) mContent.getAdapter()).add(item);
		LauncherModel.addOrMoveItemInDatabase(mLauncher, item, mInfo.id, 0, 0,
				0);
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// Log.d(TAG, "UserFolder onDragEnter");
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// Log.d(TAG, "UserFolder onDragOver");
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// Log.d(TAG, "UserFolder onDragExit");
	}

	@Override
	public void onDropCompleted(View target, boolean success) {
		super.onDropCompleted(target, success);
		if (success) {
			if (mDragItem instanceof ShortcutInfo) {
				ShortcutsAdapter adapter = (ShortcutsAdapter) mContent
						.getAdapter();
				adapter.remove((ShortcutInfo) mDragItem);

				// refresh
				CellLayout cellLayout = (CellLayout) mLauncher.getWorkspace()
						.getChildAt(mLauncher.getCurrentWorkspaceScreen());
				for (int i = 0; i < cellLayout.getChildCount(); i++) {
					ItemInfo itemInfo = (ItemInfo) cellLayout.getChildAt(i)
							.getTag();
					if (itemInfo instanceof UserFolderInfo) {
						// ((FolderIcon)
						// cellLayout.getChildAt(i)).addItemInfo(dockIteminfo);
						((FolderIcon) cellLayout.getChildAt(i))
								.refreshFolderIcon();
					}
				}

			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	void bind(FolderInfo info) {
		LayoutInflater inflater = LayoutInflater.from(mContext);

		removeAllViews();
		if (info.container == Favorites.CONTAINER_DOCKBAR) {

		} else {
			View view = inflater.inflate(R.layout.user_folder_up, null);
			addView(view, android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.FILL_PARENT);
		}

		// Initialize views for folder layout
		mContent = (AbsListView) findViewById(R.id.folder_content);
		if (mContent != null) {
			mContent.setOnItemClickListener(this);
			mContent.setOnItemLongClickListener(this);
		}
		mTitleView = (TextView) findViewById(R.id.folder_name);
		View editNameView = findViewById(R.id.folder_name_edit);
		if (editNameView != null) {
			editNameView.setOnClickListener(this);
		}
		View btnEditName = findViewById(R.id.folder_rename);
		if (btnEditName != null) {
			btnEditName.setOnClickListener(this);
		}
		mEmptyView = findViewById(R.id.folder_empty);
		mFolderContent = (ViewGroup) findViewById(R.id.folder_full_content);
		mFolderIcon = findViewById(R.id.folder_icon);
		mLeftCorner = findViewById(R.id.folder_left);
		mLeftLine = findViewById(R.id.folder_left_line);
		mArrow = findViewById(R.id.folder_arrow);

		super.bind(info);
		if (info instanceof UserFolderInfo) {
			UserFolderInfo folderInfo = (UserFolderInfo) info;
			if (folderInfo.contents == null || folderInfo.getSize() == 0) {
				mContent.setVisibility(View.GONE);
				mEmptyView.setVisibility(View.VISIBLE);
			} else {
				mEmptyView.setVisibility(View.GONE);
				mContent.setVisibility(View.VISIBLE);
				setContentAdapter(new ShortcutsAdapter(mContext,
						folderInfo.contents));
			}
		} else if (info instanceof ApplicationFolderInfo) {
			ApplicationFolderInfo folderInfo = (ApplicationFolderInfo) info;
			if (folderInfo.contents == null || folderInfo.getSize() == 0) {
				mContent.setVisibility(View.GONE);
				mEmptyView.setVisibility(View.VISIBLE);
			} else {
				mEmptyView.setVisibility(View.GONE);
				mContent.setVisibility(View.VISIBLE);
				setContentAdapter(new ApplicationsAdapter(mContext,
						folderInfo.contents));
			}
		}
	}

	// When the folder opens, we need to refresh the GridView's selection by
	// forcing a layout
	@Override
	void onOpen() {
		super.onOpen();
		requestFocus();
	}

	@Override
	void switchScreenMode(boolean bIsFullScreen, int paddingTop) {
		// TODO Auto-generated method stub
		final int padding = bIsFullScreen ? paddingTop : -paddingTop;
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mFolderContent
				.getLayoutParams();
		lp.topMargin += padding;
		mFolderContent.setLayoutParams(lp);

		lp = (RelativeLayout.LayoutParams) mFolderIcon.getLayoutParams();
		lp.topMargin += padding;
		mFolderIcon.setLayoutParams(lp);
		// super.switchScreenMode(bIsFullScreen, paddingTop);
	}

	final void onOpen(FolderInfo folderInfo) {
		super.onOpen();
		requestFocus();
		mLauncher.setUserFolderOpenAndCloseFocus(false);

		if (mInfo.container == Favorites.CONTAINER_DESKTOP) {
			((FolderIcon) folderInfo.folderIcon).onFolderOpen();
		}
		// Create canvas and bitmap to draw original folder's icon
		mFolderContent.setAnimationCacheEnabled(true);
		Bitmap bmp = Bitmap.createBitmap(mFolderIconWidth, mFolderIconHeight,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas();
		canvas.setBitmap(bmp);

		if (folderInfo.container != Favorites.CONTAINER_DOCKBAR) {
			// Folder is in desktop or applications
			BubbleTextView folderIcon = folderInfo.folderIcon;
			boolean cached = folderIcon.isDrawingCacheEnabled();

			// Force to generate folder icon bitmap
			folderIcon.setPressed(false);
			folderIcon.destroyDrawingCache();
			folderIcon.setDrawingCacheEnabled(true);
			canvas.drawBitmap(folderIcon.getDrawingCache(), 0.0f, 0.0f, null);
			folderIcon.setDrawingCacheEnabled(cached);
		} else {
			// Folder is in dock bar
		}

		// Because user_folder_up is a child of UserFolder view,
		// so when click empty areas in UserFolder view, will close the folder
		// self
		setOnClickListener(this);

		// Set folder icon image bitmap and measure layout
		ImageView openFolderIcon = (ImageView) findViewById(R.id.folder_icon);
		openFolderIcon.setImageBitmap(bmp);
		// Close folder when user click on the fake folder icon
		openFolderIcon.setOnClickListener(this);
		// Set the fake folder icon's layout
		RelativeLayout.LayoutParams lpFolderIcon = (RelativeLayout.LayoutParams) openFolderIcon
				.getLayoutParams();
		lpFolderIcon.width = mFolderIconWidth;
		lpFolderIcon.height = mFolderIconHeight;
		lpFolderIcon.leftMargin = mFolderIconLeft;
		int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
				((View) getParent()).getWidth(), MeasureSpec.EXACTLY);
		int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
				((View) getParent()).getHeight(), MeasureSpec.EXACTLY);
		measure(widthMeasureSpec, heightMeasureSpec);
		requestLayout();

		// Set left background line's length
		RelativeLayout.LayoutParams lpFolder = (RelativeLayout.LayoutParams) mFolderContent
				.getLayoutParams();
		LinearLayout.LayoutParams lpFolderBgLine = (LinearLayout.LayoutParams) mLeftLine
				.getLayoutParams();
		if (folderInfo.container != Favorites.CONTAINER_DOCKBAR) {
			lpFolder.topMargin = mFolderIconTop + mFolderIconHeight
					+ mFolderTopPadding;

			int leftWidth = mFolderIconLeft + mFolderIconWidth / 2;
			leftWidth -= (mLeftCorner.getMeasuredWidth() + mArrow
					.getMeasuredWidth() / 2);
			leftWidth -= (lpFolder.leftMargin + mFolderContent.getPaddingLeft());
			lpFolderBgLine.width = leftWidth;
		} else {

		}

		// Initialize the animation
		int count = 0;
		if (folderInfo.container == Applications.CONTAINER_APPS) {
			count = ((ApplicationFolderInfo) folderInfo).getSize();
		} else {
			count = ((UserFolderInfo) folderInfo).getSize();
		}
		final int duration = 200 + (count - 1) / 4 * 50;
		LinearInterpolator lineInterpolator = new LinearInterpolator();
		float scale = (float) (mFolderIconLeft + mFolderIconWidth / 2)
				/ (float) mFolderContent.getMeasuredWidth();
		AnimationSet animSet = new AnimationSet(false);
		animSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				mInfo.folderIcon.setText(null);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}
		});
		AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
		alphaAnim.setDuration(duration);
		alphaAnim.setInterpolator(lineInterpolator);
		alphaAnim.setFillAfter(true);
		ScaleAnimation scaleAnim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
				1, scale, 1, 0.0f);
		scaleAnim.setDuration(duration);
		scaleAnim.setInterpolator(lineInterpolator);
		animSet.addAnimation(alphaAnim);
		animSet.addAnimation(scaleAnim);
		if (mFolderTopPadding == 0) {
			lpFolderIcon.topMargin = mFolderIconTop;
			mFolderContent.startAnimation(animSet);
		} else {
			lpFolderIcon.topMargin = mFolderIconTop + mFolderTopPadding;
			TranslateAnimation transAnim = new TranslateAnimation(0.0f, 0.0f,
					-mFolderTopPadding, 0.0f);
			transAnim.setDuration(duration);
			transAnim.setInterpolator(lineInterpolator);
			((View) openFolderIcon.getParent()).startAnimation(transAnim);

			mFolderContent.startAnimation(animSet);
		}
	}

	final void onClose(Launcher launcher) {
		mFolderContent.setDrawingCacheQuality(DRAWING_CACHE_QUALITY_LOW);
		int count = 0;
		if (mInfo.container == Applications.CONTAINER_APPS) {
			count = ((ApplicationFolderInfo) mInfo).getSize();
		} else {
			count = ((UserFolderInfo) mInfo).getSize();
			((FolderIcon) mInfo.folderIcon).onFolderClose();
		}
		int duration = 250;
		switch ((count - 1) / 4) {
		case 1:
			duration = (int) (duration * 1.15f);
			break;
		case 2:
			duration = (int) (duration * 1.3f);
			break;
		case 0:
		default:
			break;
		}

		AccelerateInterpolator accInterpolator = new AccelerateInterpolator();
		CustomAnimationListener animListener = new CustomAnimationListener(
				launcher, this);
		float scale = (float) (mFolderIconLeft + mFolderIconWidth / 2)
				/ (float) mFolderContent.getMeasuredWidth();
		AnimationSet animSet = new AnimationSet(false);
		AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
		alphaAnim.setDuration(duration);
		alphaAnim.setInterpolator(accInterpolator);
		alphaAnim.setFillAfter(true);
		ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
				1, scale, 1, 0.0f);
		scaleAnim.setDuration(duration);
		scaleAnim.setInterpolator(accInterpolator);
		scaleAnim.setFillAfter(true);
		animSet.addAnimation(alphaAnim);
		animSet.addAnimation(scaleAnim);
		animSet.setFillAfter(true);

		if (mFolderTopPadding == 0) {
			launcher.shadeViewsReserve(this, mInfo);
			animSet.setAnimationListener(animListener);
			mFolderContent.startAnimation(animSet);
		} else {
			launcher.shadeViewsReserve(this, mInfo);
			TranslateAnimation transAnim = new TranslateAnimation(0.0f, 0.0f,
					0.0f, -mFolderTopPadding);
			transAnim.setDuration(duration);
			transAnim.setFillAfter(true);
			transAnim.setAnimationListener(animListener);
			mFolderContent.startAnimation(animSet);
			startAnimation(transAnim);
		}
	}

	public final int mesureHeight(int width, int height) {
		int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width,
				MeasureSpec.EXACTLY);
		int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height,
				MeasureSpec.EXACTLY);

		measure(widthMeasureSpec, heightMeasureSpec);
		requestLayout();
		return mFolderContent.getMeasuredHeight();
	}

	public void setFolderIconParams(int topPadding, int width, int height,
			int left, int top) {
		// TODO Auto-generated method stub
		mFolderTopPadding = topPadding;
		mFolderIconWidth = width;
		mFolderIconHeight = height;
		mFolderIconLeft = left;
		mFolderIconTop = top;
	}

	final class CustomAnimationListener implements Animation.AnimationListener {

		final Launcher mLauncher;
		final UserFolder mUserFolder;

		public CustomAnimationListener(Launcher launcher, UserFolder folder) {
			this.mLauncher = launcher;
			this.mUserFolder = folder;
		}

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			mInfo.folderIcon.setText(mInfo.title);

			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mUserFolder.clearAnimation();
					UserFolder.getFolderContent(mUserFolder).clearAnimation();
					mLauncher.closeFolderByAnim(mUserFolder);
				}
			}, 100);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}
	}

	final void closeFolder() {
		if (mLauncher != null) {
			mLauncher.closeFolder(this);
		}
	}
}

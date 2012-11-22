package com.fruit.launcher;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class AllAppsHomeBar extends FrameLayout implements DropTarget {

	private Launcher mLauncher;

	public AllAppsHomeBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mLauncher = null;
	}

	public final void setLauncher(Launcher launcher) {
		mLauncher = launcher;
	}

	public final void showHomeBar() {
		mLauncher.mDockBar.setVisibility(View.GONE);
		setVisibility(View.VISIBLE);
	}

	public final void hideHomeBar() {
		hideHomeBar(false);
	}

	public final void hideHomeBar(boolean showDockBar) {
		setVisibility(View.GONE);
		if (showDockBar) {
			mLauncher.mDockBar.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		return;
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		return;
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		if (mLauncher != null && mLauncher.isAllAppsVisible()) {
			mLauncher.closeAllApps(true);
		}
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		return;
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Rect estimateDropLocation(DragSource source, int x, int y,
			int xOffset, int yOffset, DragView dragView, Object dragInfo, Rect recycle) {
		// TODO Auto-generated method stub
		return null;
	}
}
package com.fruit.launcher;


import android.graphics.Rect;

public interface BubbleBase {

	public void setFolderInfo(ApplicationInfoEx foldInfo);

	public void setLauncher(Launcher launcher);

	public void setTargetIconRect(Rect rect);

}
package com.fruit.launcher.widgets;

import java.util.ArrayList;

import android.content.pm.ApplicationInfo;

public abstract interface ITaskManagerUpdate {

	public abstract void onUpdateMemInfo(long totalMem, long availMem);
	public abstract void onUpdateProcess(ArrayList<ApplicationInfo> runningProcessList);
}
package com.fruit.launcher.widgets;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fruit.launcher.R;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.widget.Toast;

public class TaskManagerUtil {

	public static final float BYTES_PER_M = 1048576.0f;
	private static final String MEM_TOTAL = "MemTotal:";
	private static final String SPLI_REGULAR = "[ ]+";

	private static TaskManagerUtil mInstance;

	private Context mContext;
	private ActivityManager mActivityMgr;
	private ArrayList<ITaskManagerUpdate> mTaskWidget;

	private TaskManagerUtil(Context context) {
		mContext = context;
		mActivityMgr =
			(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		mTaskWidget = new ArrayList<ITaskManagerUpdate>();
	}

	public static final TaskManagerUtil getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new TaskManagerUtil(context);
		}
		return mInstance;
	}

	public void addTaskManagerWidget(ITaskManagerUpdate taskManagerWidget) {
		// TODO Auto-generated method stub
		mTaskWidget.add(taskManagerWidget);
	}

	public void removeTaskManagerWidget(ITaskManagerUpdate taskManagerWidget) {
		// TODO Auto-generated method stub
		mTaskWidget.remove(taskManagerWidget);
	}

	public final void retrieveMemInfo(boolean bPublishProcess) {
		new OperateProcessProcedure(mContext,
					OperateProcessProcedure.OPERATE_RETRIEVE_MEM,
					bPublishProcess).run();
	}

	private void publishMemInfo(long totalMem, long availMem) {
		Iterator<ITaskManagerUpdate> iterator = mTaskWidget.iterator();

		while (iterator.hasNext()) {
			ITaskManagerUpdate iTaskMgr = iterator.next();
			if (iTaskMgr != null) {
				iTaskMgr.onUpdateMemInfo(totalMem, availMem);
			}
		}
	}

	private void publishProcessList(ArrayList<ApplicationInfo> runningProcessList) {
		Iterator<ITaskManagerUpdate> iterator = mTaskWidget.iterator();

		while (iterator.hasNext()) {
			ITaskManagerUpdate iTaskMgr = iterator.next();
			if (iTaskMgr != null) {
				iTaskMgr.onUpdateProcess(runningProcessList);
			}
		}
	}

	public final void freeMemory(Context context, boolean bPublishProcess) {
		showConfirmDialog(context, bPublishProcess);
	}

	private void showConfirmDialog(Context context, boolean bPublishProcess) {
		// TODO Auto-generated method stub
		DialogInterface.OnClickListener clickListener;
		if (bPublishProcess) {
			clickListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mInstance.startToKillProcess(true);
				}
			};
		} else {
			clickListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mInstance.startToKillProcess(false);
				}
			};
		}

		new AlertDialog.Builder(context)
				.setTitle(R.string.widget_clean_memory)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setMessage(R.string.widget_kill_task)
				.setPositiveButton(android.R.string.ok, clickListener)
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	private void startToKillProcess(boolean bPublishProcess) {
		// TODO Auto-generated method stub
		new OperateProcessProcedure(mContext,
				OperateProcessProcedure.OPERATE_KILL_PROCESS,
				bPublishProcess).run();
	}

	@SuppressWarnings("deprecation")
	public void killProcess(ApplicationInfo info) {
		try {
			Method method =
				ActivityManager.class.getDeclaredMethod("killBackgroundProcesses", new Class[] {String.class});
			method.invoke(mActivityMgr, new Object[] {info.packageName});
		} catch (Exception e) {
			e.printStackTrace();
			mActivityMgr.restartPackage(info.packageName);
		}
	}

	final class OperateProcessProcedure extends Thread {

		static final int OPERATE_RETRIEVE_MEM = 0;
		static final int OPERATE_KILL_PROCESS = 1;
		static final int OPERATE_MAX = 2;

		private Context mContext;
		private ActivityManager mActivityMgr;
		private List<ApplicationInfo> mInstalledApps;
		private ArrayList<ApplicationInfo> mRunningProcess;

		private String mPkgName;
		private int mOperateType;
		private ActivityManager.MemoryInfo mMemInfo;
		private boolean mPublishProcessList;
		private long mTotalMem;
		private long mAvailableMem;

		public OperateProcessProcedure(Context context, int operateType, boolean bPublishProcess) {
			if (operateType < OPERATE_RETRIEVE_MEM
					|| operateType > OPERATE_KILL_PROCESS) {
				mOperateType = OPERATE_RETRIEVE_MEM;
			} else {
				mOperateType = operateType;
			}
			mPublishProcessList = bPublishProcess;

			mContext = context;
			mActivityMgr =
				(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			mRunningProcess = new ArrayList<ApplicationInfo>();
			mPkgName = mContext.getPackageName();
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mInstalledApps != null) {
				mInstalledApps.clear();
				mInstalledApps = null;
			}
			mInstalledApps = mContext.getPackageManager().getInstalledApplications(0);
			mMemInfo = new ActivityManager.MemoryInfo();

			switch (mOperateType) {
			case OPERATE_KILL_PROCESS:
				mActivityMgr.getMemoryInfo(mMemInfo);
				long oldAvailableMem = mMemInfo.availMem;

				List<ActivityManager.RunningAppProcessInfo> appList =
					mActivityMgr.getRunningAppProcesses();
				Iterator<ActivityManager.RunningAppProcessInfo> iterator =
					appList.iterator();

				while (iterator.hasNext()) {
					String processName = iterator.next().processName;

					ApplicationInfo appInfo = getAppInfo(processName);
					if (appInfo != null) {
						killProcess(appInfo);
					}
				}

				mActivityMgr.getMemoryInfo(mMemInfo);
				mAvailableMem = mMemInfo.availMem;
				if (mAvailableMem > oldAvailableMem) {
					long freedByte = (mAvailableMem - oldAvailableMem);
					if (freedByte > (long) BYTES_PER_M) {
						int freedMem = Math.round(freedByte / BYTES_PER_M);
						String tip = String.format(mContext.getString(R.string.widget_clean_memory_report), freedMem);
						Toast.makeText(mContext, tip, Toast.LENGTH_SHORT).show();
					}
				} else {
					if (mPublishProcessList) {
						mPublishProcessList = false;
					}
					Toast.makeText(mContext, mContext.getString(R.string.widget_clean_memory_alert), Toast.LENGTH_SHORT).show();
				}
				// do not break here, cause OPERATE_KILL_PROCESS will always publish memory info
			case OPERATE_RETRIEVE_MEM:
			default:
				try {
					FileReader fileReader = new FileReader("/proc/meminfo");
					BufferedReader bufferedReader = new BufferedReader(fileReader);
					String line = bufferedReader.readLine();
					int total = 0;

					if (line.startsWith(MEM_TOTAL)) {
						total = Integer.parseInt(line.split(SPLI_REGULAR)[1]);
					}
					bufferedReader.close();

					mActivityMgr.getMemoryInfo(mMemInfo);
					mTotalMem = total * 1024;
					mAvailableMem = mMemInfo.availMem;

					publishMemInfo(mTotalMem, mAvailableMem);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}

			if (mPublishProcessList) {
				// retrieve current running process list
				getRunningProcessList();
				publishProcessList(mRunningProcess);
			}
		}

		private void getRunningProcessList() {
			mRunningProcess.clear();

			List<ActivityManager.RunningAppProcessInfo> appList =
				mActivityMgr.getRunningAppProcesses();
			Iterator<ActivityManager.RunningAppProcessInfo> iterator =
				appList.iterator();

			while (iterator.hasNext()) {
				ApplicationInfo appInfo = getAppInfo(iterator.next().processName);
				if (appInfo != null) {
					mRunningProcess.add(appInfo);
				}
			}
		}

		private ApplicationInfo getAppInfo(String processName) {
			// TODO Auto-generated method stub
			Iterator<ApplicationInfo> iteratorApps = mInstalledApps.iterator();
			ApplicationInfo appInfo = null;

			while (iteratorApps.hasNext()) {
				appInfo = iteratorApps.next();
				if (appInfo.processName.equals(processName)
						&& !(appInfo.packageName.equals(mPkgName))) {
					return appInfo;
				}
			}
			return null;
		}
	}
}
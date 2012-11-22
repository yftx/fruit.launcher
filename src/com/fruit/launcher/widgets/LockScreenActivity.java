package com.fruit.launcher.widgets;

import android.app.Activity;
import android.os.Bundle;

public class LockScreenActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		LockScreenUtil.getInstance(this).lockScreen();
		finish();
	}
}
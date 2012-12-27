package com.fruit.launcher.setting;

import com.fruit.launcher.Configurator;
import com.fruit.launcher.Launcher;
import com.fruit.launcher.R;
import com.fruit.launcher.Utilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends PreferenceActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String KEY_SCREEN_EFFECT = "settings_key_screen_switcheffects";
	private static final String KEY_THEME = "settings_theme";
    private static final String KEY_MANAGE_APPS = "settings_manage_apps";
    private static final String KEY_SCREEN_INFO = "settings_screen_info";
    private static final String KEY_APPS_INFO = "settings_apps_info";
	private static final String KEY_HELP = "settings_key_help";
	private static final String KEY_ABOUT = "settings_key_about";
	private static final String KEY_EXIT = "settings_key_exit";
	// private static final String KEY_LOCK_SCREEN = "settings_key_lockscreen";

	private ListPreference mListPreference;
//	private Dialog dialog;

//	static final int DIALOG_SCREEN_INFO_ID = 0;
//	static final int DIALOG_GAMEOVER_ID = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(
				SettingUtils.LAUNCHER_SETTINGS_NAME);

		addPreferencesFromResource(R.xml.launcher_setting);
		mListPreference = (ListPreference) findPreference(KEY_SCREEN_EFFECT);

		findPreference(KEY_ABOUT).setSummary(getVersionName());
		findPreference(KEY_SCREEN_INFO).setSummary(Launcher.mScreenWidth+"x"+Launcher.mScreenHeight);
		
		PreferenceScreen screen = getPreferenceScreen();
		removePreferenceByConfig(screen, KEY_THEME,
				Configurator.CONFIG_HIDETHEME, "com.fruit.thememanager");
		
		removePreferenceByConfig(screen, KEY_MANAGE_APPS,
				Configurator.CONFIG_HIDEMANAGEAPPS, "com.android.settings");
	}

	private String getVersionName() {
		String versionName = null;
		try {
			versionName = getString(R.string.current_desk_version)
					+ getPackageManager().getPackageInfo(getPackageName(), 0).versionName 
					+ "."
					+ getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stub
		final String key = preference.getKey();

		if (key == null) {
			return false;
		}

		Intent intent;
		if (key.equals(KEY_THEME)) {
			callOtherActivity("com.fruit.action.THEME");
        } else if (key.equals(KEY_MANAGE_APPS)) {        	
        	callOtherActivity(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);			
		} else if (key.equals(KEY_HELP)) {

		} else if (key.equals(KEY_SCREEN_INFO)) {
			showScreenInfo();
		} else if (key.equals(KEY_APPS_INFO)) {
			showAppsInfo();
		} else if (key.equals(KEY_ABOUT)) {

		} else if (key.equals(KEY_EXIT)) {
			intent = new Intent(this, Launcher.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("exitFlag", 1);
			startActivity(intent);
		} else {
			return false;
		}
		return true;
	}
	
	private String getAppsInfoString(){

		String str = new String("");
		
		str+=Launcher.mDumpString;
		str+="\n";
		
		return str;
	}
	/**
	 * 
	 */
	private void showAppsInfo() {
		String appsInfo = getAppsInfoString();
		showAlertDialog(appsInfo);
	}

	/**
	 * 
	 */
	private void showScreenInfo() {
		String screenInfo = getScreenInfoString();
		showAlertDialog(screenInfo);
		
//		onCreateDialog(DIALOG_SCREEN_INFO_ID);
//		showDialog(DIALOG_SCREEN_INFO_ID);
	}

	/**
	 * @return
	 */
	private String getScreenInfoString() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
	
		String str = new String("");
		
		str+=getString(R.string.width)+getString(R.string.colon)+String.valueOf(metrics.widthPixels)+getString(R.string.px)+"\n";
		str+=getString(R.string.height)+getString(R.string.colon)+String.valueOf(metrics.heightPixels)+getString(R.string.px)+"\n";
		//str+="------------------------"+"\n";
		str+=getString(R.string.density)+getString(R.string.colon)+String.valueOf(metrics.density)+"\n";
		str+=getString(R.string.dpi)+getString(R.string.colon)+String.valueOf(metrics.densityDpi);
		if(metrics.densityDpi==DisplayMetrics.DENSITY_DEFAULT){
			str+="("+getString(R.string.medium)+")";			
		}else if(metrics.densityDpi<DisplayMetrics.DENSITY_DEFAULT){
			str+="("+getString(R.string.low)+")";		
		}else {
			if(metrics.densityDpi<=DisplayMetrics.DENSITY_HIGH){
				str+="("+getString(R.string.high)+")";		
			}else{
				str+="("+getString(R.string.exhigh)+")";
			}			
		}
		str+="\n";
		
		return str;
	}
	
	/**
	 * @param msg
	 */
	private void showAlertDialog(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg)
		       .setCancelable(true)
		       .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}

//	protected Dialog onCreateDialog(int id) {
//		Context mContext = getApplicationContext();
//	    Dialog dialog;
//	    switch(id) {
//	    case DIALOG_SCREEN_INFO_ID:
//	        // do the work to define the pause Dialog
//			dialog = new Dialog(mContext);
//
//			dialog.setContentView(R.layout.custom_dialog);
//			dialog.setTitle("Custom Dialog");
//
//			TextView text = (TextView) dialog.findViewById(R.id.text);
//			text.setText("Hello, this is a custom dialog!");
//			ImageView image = (ImageView) dialog.findViewById(R.id.image);
//			image.setImageResource(R.drawable.ic_launcher_home);
//
//	        break;
//	    case DIALOG_GAMEOVER_ID:
//	        // do the work to define the game over Dialog
//	    	dialog = null;
//	        break;
//	    default:
//	        dialog = null;
//	    }
//	    return dialog;
//	}
	
	/**
	 * @throws NotFoundException
	 */
	private void callOtherActivity(String action) throws NotFoundException {
		Intent intent;
		intent = new Intent();		
		intent.setAction(action);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		mListPreference.setSummary(mListPreference.getEntry());
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		if (key.equals(KEY_SCREEN_EFFECT)) {
			mListPreference.setSummary(mListPreference.getEntry());
		}
	}

	private boolean removePreferenceByConfig(PreferenceGroup preferenceGroup,
			String preference, String name, String pkgName) {
		boolean bHide = Configurator.getBooleanConfig(this, name, false);		
		boolean bUnInstall = !Utilities.isPackageInstall(this, pkgName);

		if (bHide || bUnInstall) {
			// Property is missing so remove preference from group
			try {
				preferenceGroup.removePreference(findPreference(preference));
			} catch (RuntimeException e) {
				return false;
			}
		}

		return true;
	}
}
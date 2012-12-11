package com.fruit.launcher.setting;

import com.fruit.launcher.Configurator;
import com.fruit.launcher.Launcher;
import com.fruit.launcher.R;
import com.fruit.launcher.Utilities;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class SettingActivity extends PreferenceActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String KEY_SCREEN_EFFECT = "settings_key_screen_switcheffects";
	private static final String KEY_THEME = "settings_theme";
	private static final String KEY_HELP = "settings_key_help";
	private static final String KEY_ABOUT = "settings_key_about";
	private static final String KEY_EXIT = "settings_key_exit";
	// private static final String KEY_LOCK_SCREEN = "settings_key_lockscreen";

	private ListPreference mListPreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(
				SettingUtils.LAUNCHER_SETTINGS_NAME);

		addPreferencesFromResource(R.xml.launcher_setting);
		mListPreference = (ListPreference) findPreference(KEY_SCREEN_EFFECT);

		findPreference(KEY_ABOUT).setSummary(getVersionName());

		PreferenceScreen screen = getPreferenceScreen();

		removePreferenceByConfig(screen, KEY_THEME,
				Configurator.CONFIG_HIDETHEME);
	}

	private String getVersionName() {
		String versionName = null;
		try {
			versionName = getString(R.string.current_desk_version)
					+ getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
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
			intent = new Intent();
			intent.setAction("com.fruit.action.THEME");
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(this, R.string.activity_not_found,
						Toast.LENGTH_SHORT).show();
			}
		} else if (key.equals(KEY_HELP)) {

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
			String preference, String name) {
		boolean bHide = Configurator.getBooleanConfig(this, name, false);
		boolean bUnInstall = !Utilities.isPackageInstall(this,
				"com.fruit.thememanager");

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
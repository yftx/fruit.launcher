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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.widget.Toast;

import java.net.URISyntaxException;

import com.fruit.launcher.LauncherSettings.BaseLauncherColumns;
import com.fruit.launcher.LauncherSettings.Favorites;

public class UninstallShortcutReceiver extends BroadcastReceiver {

	private static final String ACTION_UNINSTALL_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT";

	@Override
	public void onReceive(Context context, Intent data) {
		if (!ACTION_UNINSTALL_SHORTCUT.equals(data.getAction())) {
			return;
		}

		Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
		String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
		boolean duplicate = data.getBooleanExtra(
				Launcher.EXTRA_SHORTCUT_DUPLICATE, true);

		if (intent != null && name != null) {
			final ContentResolver cr = context.getContentResolver();
			Cursor c = cr.query(Favorites.CONTENT_URI, new String[] {
					BaseColumns._ID, BaseLauncherColumns.INTENT },
					BaseLauncherColumns.TITLE + "=?", new String[] { name },
					null);

			final int intentIndex = c
					.getColumnIndexOrThrow(BaseLauncherColumns.INTENT);
			final int idIndex = c.getColumnIndexOrThrow(BaseColumns._ID);

			boolean changed = false;

			try {
				while (c.moveToNext()) {
					try {
						if (intent.filterEquals(Intent.parseUri(
								c.getString(intentIndex), 0))) {
							final long id = c.getLong(idIndex);
							final Uri uri = Favorites.getContentUri(id, false);
							cr.delete(uri, null, null);
							changed = true;
							if (!duplicate) {
								break;
							}
						}
					} catch (URISyntaxException e) {
						// Ignore
					}
				}
			} finally {
				c.close();
			}

			if (changed) {
				cr.notifyChange(Favorites.CONTENT_URI, null);
				Toast.makeText(context,
						context.getString(R.string.shortcut_uninstalled, name),
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
package com.fruit.launcher;

import android.content.ContentValues;

import java.util.ArrayList;

import com.fruit.launcher.LauncherSettings.Applications;
import com.fruit.launcher.LauncherSettings.BaseLauncherColumns;

/**
 * Represents a folder containing applications.
 */
class ApplicationFolderInfo extends FolderInfo {

    /**
     * The applications 
     */
    ArrayList<ApplicationInfoEx> contents = new ArrayList<ApplicationInfoEx>();

    /**
     * Indicates home bar
     */
    AllAppsHomeBar allAppsHomeBars = null;

    ApplicationFolderInfo() {
        itemType = Applications.APPS_TYPE_FOLDER;
    }

    /**
     * Add an app or shortcut
     * 
     * @param item
     */
    public void add(ApplicationInfoEx item) {
    	// Change item's container to folder's id
    	item.container = id;
        contents.add(item);
    }

    /**
     * Remove an app or shortcut. Does not change the DB.
     * 
     * @param item
     */
    public void remove(ApplicationInfoEx item) {
    	// If the item is not the last item in source folder
		// should adjust rest items' orderId
    	if (item.orderId < (contents.size() - 1)) {
    		for (int i = item.orderId + 1; i < contents.size(); i++) {
    			contents.get(i).orderId--;
    		}
    	}
        contents.remove(item);
    }

    public int getSize() {
    	return contents.size();
    }

    @Override
    void onAddToDatabase(ContentValues values) { 
        super.onAddToDatabase(values);
        values.put(Applications.POSITION, position);
        values.put(BaseLauncherColumns.TITLE, title.toString());
    }
}
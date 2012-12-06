package com.fruit.launcher;

import java.io.UnsupportedEncodingException;

import com.fruit.launcher.LauncherSettings.Applications;
import com.fruit.launcher.LauncherSettings.BaseLauncherColumns;
import com.fruit.launcher.LauncherSettings.Favorites;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NewFolderDialog extends Activity {

	private static final int MAX_LENGTH = 24;

	private int mCurrentScreen;
	private int mPerCount;
	private int style;
	private long mContainer;
	private String mTitle;
	private int mID;
	private int mPosition;
	private EditText mFolderNameEditText;
	private InputFilter mInputFilter;

	static final String TAG = "NewFolderDialog";
	public static final String STYLE      = "style";
	public static final String CONTAINER   = "container";
	public static final String PAGE       = "page";
	public static final String PERPAGECOUNT     = "perpagecount";
	public static final String TITLE      = "title";
	public static final String ID         = "id";
	public static final String POSITION   = "position";

	public static final int RENAME = 1;
	public static final int NEW    = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		Intent intent = this.getIntent();
		style = intent.getIntExtra(STYLE, NEW);
		mContainer = intent.getLongExtra(CONTAINER, Favorites.CONTAINER_DESKTOP);

		if (style == NEW) {
			mCurrentScreen = intent.getIntExtra(PAGE, 0);
			mPerCount = intent.getIntExtra(PERPAGECOUNT, 0);
		} else {
			mTitle = intent.getStringExtra(TITLE);
			mID = intent.getIntExtra(ID, -1);
			mPosition = intent.getIntExtra(POSITION, -1);
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setTitle(R.string.new_folder_title);
		setContentView(R.layout.newfolderdialog);
		getWindow().setBackgroundDrawableResource(R.drawable.dialog_folder_bg);

		TextView tvTitle = (TextView) findViewById(R.id.folder_dialog_title);
		mFolderNameEditText = (EditText) findViewById(R.id.folder_dialog_name);
		mInputFilter = new InputFilter() {

			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				// TODO Auto-generated method stub
                try {
//                    int destLen = dest.toString().getBytes("GB18030").length;
//                    int sourceLen = source.toString().getBytes("GB18030").length;
                    
                    int destLen = dest.toString().getBytes("UTF-8").length;
                    int sourceLen = source.toString().getBytes("UTF-8").length;
                    Log.d("inputfilter", String.valueOf(destLen + sourceLen));

                    if ((sourceLen + destLen) > MAX_LENGTH) {
                        Toast.makeText(getBaseContext(), R.string.folder_name_is_full, Toast.LENGTH_SHORT).show();
                        return "";
                    }

                    return source;
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return "";
                }
			}
			
		};
		mFolderNameEditText.setFilters(new InputFilter[] { mInputFilter });

		if (style != NEW) {
			tvTitle.setText(R.string.rename_folder_title);
			mFolderNameEditText.setText(mTitle);
		}

		((Button) findViewById(R.id.btnYes)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int nStart = mCurrentScreen * mPerCount;
				
				if (!mFolderNameEditText.getText().toString().equals("")) {
					final ContentResolver cr = getContentResolver();
					if (style == NEW) {
						Intent createFolder = new Intent();

						if (mContainer == Applications.CONTAINER_APPS) {
							final Uri updateUri = Applications.getCustomUri("/addfolder");

							
							if(getResources().getBoolean(R.bool.config_lock_apps)){
								final int lockappNum = getResources().getInteger(R.integer.config_lock_apps_num);
								nStart = nStart == 0 ? lockappNum : nStart;
							}
							// update position from POSITION >= nStart
							cr.update(updateUri, null, null, new String[] {String.valueOf(nStart)});

							// insert new folder
							ContentValues values = new ContentValues();
							values.put(BaseLauncherColumns.TITLE, mFolderNameEditText.getText().toString());
							values.put(Applications.CONTAINER, mContainer);
							values.put(Applications.POSITION, nStart);
							values.put(BaseLauncherColumns.ORDERID, 0);
							values.put(BaseLauncherColumns.ITEM_TYPE, Applications.APPS_TYPE_FOLDER);

							Uri uri = cr.insert(Applications.CONTENT_URI, values);
							String folderId = uri.getPathSegments().get(1);
							createFolder.putExtra(POSITION, nStart);
							createFolder.putExtra(ID, Integer.parseInt(folderId));
						}

						createFolder.putExtra(STYLE, NewFolderDialog.NEW);
						createFolder.putExtra(CONTAINER, mContainer);
						createFolder.putExtra(TITLE, mFolderNameEditText.getText().toString());

						setResult(RESULT_OK, createFolder);
					} else {
						if (mContainer == Applications.CONTAINER_APPS) {
							ContentValues values = new ContentValues();
	
							values.put(BaseLauncherColumns.TITLE, mFolderNameEditText.getText().toString());
							cr.update(Applications.CONTENT_URI, values, 
									BaseColumns._ID + "=?",
									new String[] { String.valueOf(mID) });
						}

						Intent renameFolder = new Intent();
						renameFolder.putExtra(STYLE, NewFolderDialog.RENAME);
						renameFolder.putExtra(CONTAINER, mContainer);
						renameFolder.putExtra(TITLE, mFolderNameEditText.getText().toString());
						if (mPosition != -1) {
							renameFolder.putExtra(POSITION, mPosition);
						}

						setResult(RESULT_OK, renameFolder);
					}
				} else {
					Toast.makeText(getBaseContext(), R.string.folder_name_is_empty, Toast.LENGTH_SHORT).show();
					return;
				}

				Log.d(TAG, "set result RESULT_OK");
				finish();
			}
		});

		((Button) findViewById(R.id.btnNo)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setResult(RESULT_CANCELED, null);
				finish();
			}
		});

		// Delayed 100ms to display soft keyboard
		mFolderNameEditText.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				InputMethodManager im =
					(InputMethodManager) mFolderNameEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				im.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 100);
	}
}
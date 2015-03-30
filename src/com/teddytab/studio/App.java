package com.teddytab.studio;

import android.app.Application;
import android.os.AsyncTask;

import com.teddytab.common.model.Book;

public class App extends Application {
	
	public static final String ACCOUNT_NAME = "ACCOUNT_NAME";

	public static final int TEXT_COLOR_PICKER_ACTIVITY = 0;
	public static final int BG_COLOR_PICKER_ACTIVITY = 1;
	public static final int OBJECT_SEARCH_ACTIVITY = 2;
	public static final int IMAGE_SEARCH_ACTIVITY = 3;
	public static final int AUDIO_SEARCH_ACTIVITY = 4;
	public static final int BOOK_SEARCH_ACTIVITY = 5;
	
	public final BookBuilder book = new BookBuilder(new Book());
	public int selectedPage = 0;
	public String selectedObject;
	public int selectedEvent;
	public int selectedAnimation;
	
	private Object copiedObject;

	Object paste() {
		return copiedObject;
	}
	
	void copy(Object obj) {
		copiedObject = obj;
	}
	
	public String getAccountName() {
		return getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(ACCOUNT_NAME, null);
	}
	
	public void setAccountName(String accountName) {
		new AsyncTask<String, Void, Void>() {
			@Override
			protected Void doInBackground(String... params) {
				getSharedPreferences(getPackageName(), MODE_PRIVATE).edit()
						.putString(ACCOUNT_NAME, params[0]).commit();
				return null;
			}
		}.execute(accountName);
	}
}

package com.teddytab.studio;

import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.teddytab.common.editor.Utils;
import com.teddytab.common.model.Animation;
import com.teddytab.common.page.ContentTracker;
import com.teddytab.common.page.PageView;

public class BookActivity extends Activity implements ContentTracker {
	private static final String TAG = "BookActivity";

	public static final String BOOK = "book";
	public static final String RESOLUTION_RATIO = "RESOLUTION_RATIO";

	private App app;
	private PageView currentPageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
		setContentView(R.layout.activity_book);
		hideSystemUI();

		app = (App) getApplication();

		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		if ("portrait".equalsIgnoreCase(app.book.book.orientation) && rotation != Surface.ROTATION_0) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if ("landscape".equalsIgnoreCase(app.book.book.orientation)
				&& (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
		int barHeight = getResources().getDimensionPixelSize(resourceId);
		float ratio = getIntent().getFloatExtra(RESOLUTION_RATIO, 0);
		Pair<Integer, Integer> widthHeight = Utils.getWidthHeightForRatio(ratio, size.x, size.y + barHeight);
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
				findViewById(android.R.id.content).getLayoutParams();
		if ("portrait".equalsIgnoreCase(app.book.book.orientation)) {
			params.width = Math.min(widthHeight.first, widthHeight.second);
			params.height = Math.max(widthHeight.first, widthHeight.second);
		} else if ("landscape".equalsIgnoreCase(app.book.book.orientation)) {
			params.width = Math.max(widthHeight.first, widthHeight.second);
			params.height = Math.min(widthHeight.first, widthHeight.second);
		} else {
			params.width = widthHeight.first;
			params.height = widthHeight.second;
		}
		params.gravity = Gravity.CENTER;

		changePage(0);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			hideSystemUI();
		}
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public void hideSystemUI() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			findViewById(android.R.id.content).setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (currentPageView != null) {
			currentPageView.stop();
		}
	}
	
	@Override
	public void track(String eventName, Map<String, String> params) {
		if (Animation.Type.changePage.name().equalsIgnoreCase(eventName)) {
			try {
				String pageNumber = params.get(ContentTracker.Param.pageNumber.name());
				changePage(Integer.parseInt(pageNumber));
			} catch (Exception ex) {
				Log.w(TAG, ex);
			}
		} else if (Animation.Type.closeBook.name().equalsIgnoreCase(eventName)) {
			finish();
		}
	}

	private void changePage(int pageNumber) {
		Log.i(TAG, String.format("Changing page to %d", pageNumber));
		if (currentPageView != null) {
			currentPageView.stop();
		}
		((ViewGroup) findViewById(android.R.id.content)).removeView(currentPageView);
		currentPageView = new PageView(this, null);
		currentPageView.setPage(app.book.book.pages[pageNumber]);
		currentPageView.setCurrentVersion(1.0);
		
		((ViewGroup) findViewById(android.R.id.content)).addView(currentPageView);
	}
}

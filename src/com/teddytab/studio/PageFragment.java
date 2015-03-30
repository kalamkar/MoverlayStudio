package com.teddytab.studio;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.teddytab.common.editor.Config;
import com.teddytab.common.model.Page;
import com.teddytab.common.model.PageObject;
import com.teddytab.common.page.ImageObject;
import com.teddytab.common.page.PaperView;
import com.teddytab.common.page.TextObject;
import com.teddytab.common.page.VideoObject;
import com.teddytab.common.page.WebPageObject;

public class PageFragment extends Fragment implements OnClickListener, OnLongClickListener,
		OnDragListener, DataChangeListener {
	private static final String TAG = "PageFragment";

	public static final String PAGE_NUMBER = "PAGE_NUMBER";

	private App app;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		return inflater.inflate(R.layout.fragment_page, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		notifyDataChange();
	}

	@Override
	public void notifyDataChange() {
		if (app == null) {
			return;
		}
		int pageNumber = getArguments().getInt(PAGE_NUMBER, -1);
		if (pageNumber < 0) {
			return;
		}
		ViewGroup container = (ViewGroup) getView().findViewById(R.id.container);
		container.removeAllViews();
		PageView view = new PageView(app, app.book.getPage(pageNumber));
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(getActivity().findViewById(
				android.R.id.content).getWidth(), getActivity().findViewById(android.R.id.content)
				.getHeight());
		// Disabled enforcing 16:9 or 4:3 ratio and keeping editing space all available.
		// TODO(abhi): Fix this to get the right combination based on book orientation, device
		// orientation etc.
//		if (app.book.isWide() && params.width != 0) {
//			params.height = params.width / 16 * 9;
//			params.width = FrameLayout.LayoutParams.MATCH_PARENT;
//		} else if (params.height != 0) {
//			params.width = params.height / 3 * 4;
//			params.height = FrameLayout.LayoutParams.MATCH_PARENT;
//		} else {
			params.width = params.height = FrameLayout.LayoutParams.MATCH_PARENT;
//		}
		container.addView(view, params);
		// TODO(abhi): Something should make sure here to refresh the view.
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(final View view) {
		int pageNumber = getArguments().getInt(PAGE_NUMBER, -1);
		if (pageNumber < 0) {
			return;
		}
		if (view.getAlpha() == 1.0f) {
			PageObject object = (PageObject) view.getTag();
			((MainActivity) getActivity()).setSelectedObject(pageNumber, object.id);
			view.setAlpha(0.7f);
			final Drawable bkg = view.getBackground();
			if (bkg != null) {
				bkg.setAlpha(128);
				view.setBackgroundDrawable(bkg);
			} else {
				view.setBackgroundColor(0xA0FFFFFF);
			}
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					PageFragment.this.getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							view.setAlpha(1.0f);
							if (bkg != null) {
								bkg.setAlpha(255);
								view.setBackgroundDrawable(bkg);
							} else {
								view.setBackgroundDrawable(null);
							}
						}});
				}}, 3000);
		}
	}

	private class PageView extends FrameLayout {
		private final Page page;
		private int height;
		private int width;

		public PageView(Context context, Page page) {
			super(context);
			this.page = page;
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			width = w;
			height = h;
			Log.i(TAG, String.format("ObjectLayoutParams: screenWidth %d, screenHeight %d", width,
					height));
			render();
			super.onSizeChanged(w, h, oldw, oldh);
		}

		private void render() {
			if (page == null || page.objects == null) {
				return;
			}
			removeAllViews();

			// Create Page objects
			for (PageObject object : page.objects) {
				View view = null;

				if (object.tags == null) {
					object.tags = new String[0];
				}
				Set<String> tags = new HashSet<String>(Arrays.asList(object.tags));

				if (tags.contains("PaintType")) {
					view = getActivity().getLayoutInflater().inflate(R.layout.paint_tools, null);
					((ViewGroup) view).addView(
							new PaperView(getActivity(), null, object, view, null), 0,
							new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
									LayoutParams.MATCH_PARENT));
				} else if (object.webPage != null) {
					view = new WebPageObject(getActivity(), object.webPage);
				} else if (object.text != null) {
					view = new TextObject(getActivity(), object, height);
				} else if (object.image != null && object.image.url != null) {
					view = new ImageObject(getActivity(), object);
				} else if (object.video != null) {
					view = new VideoObject(getActivity(), object, null);
				} else {
					Log.w(TAG, "Unknown PageObject: " + Config.GSON.toJson(object));
					continue;
				}

				if ("yes".equalsIgnoreCase(object.hidden)) {
					view.setAlpha(0.1f);
				}
				view.setTag(object);

				ViewGroup.MarginLayoutParams params = new FrameLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.LEFT
								| Gravity.TOP);
				if (object.getRelativeWidth() == object.getRelativeHeight()
						&& object.getRelativeHeight() != 100) {
					int averageSize = (width + height) / 2;
					params.width = object.getRelativeWidth() * averageSize / 100;
					params.height = params.width;
				} else {
					params.width = object.getRelativeWidth() * width / 100;
					params.height = object.getRelativeHeight() * height / 100;
				}
				params.leftMargin = object.getRelativeX() * width / 100;
				params.topMargin = object.getRelativeY() * height / 100;
				Log.d(TAG, String.format(
						"ObjectLayoutParams: %s w%d h%d l%d t%d %d%% %d%% %d%% %d%%", object.id,
						params.width, params.height, params.leftMargin, params.topMargin,
						object.getRelativeWidth(), object.getRelativeHeight(),
						object.getRelativeX(), object.getRelativeY()));

				view.setOnClickListener(PageFragment.this);
				view.setOnLongClickListener(PageFragment.this);
				addView(view, params);
			}
			setOnDragListener(PageFragment.this);
		}
	}

	@Override
	public boolean onDrag(View receiverView, DragEvent event) {
		View draggedView = (View) event.getLocalState();
		switch (event.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			Log.i(TAG, String.format("Drag started %f %f", event.getX(), event.getY()));
			return true;
		case DragEvent.ACTION_DRAG_ENTERED:
			Log.i(TAG, String.format("Drag entered %f %f", event.getX(), event.getY()));
			return true;
		case DragEvent.ACTION_DROP:
			if (draggedView == null) {
				return false;
			}
			Log.i(TAG, String.format("Dropped %f %f", event.getX(), event.getY()));
			int x = (int) ((event.getX() - (draggedView.getWidth() / 2)) * 100 / receiverView
					.getWidth());
			int y = (int) ((event.getY() - (draggedView.getHeight() / 2)) * 100 / receiverView
					.getHeight());
			Log.i(TAG, String.format("x %d, y %d", x, y));

			int pageNumber = getArguments().getInt(PAGE_NUMBER, -1);
			if (pageNumber < 0) {
				return false;
			}
			app.book.updateLocation(pageNumber, (PageObject) draggedView.getTag(),
					x < 0 ? 0 : x, y < 0 ? 0 : y);
			((MainActivity) getActivity()).updateSidebar();
			notifyDataChange();
			return true;
		}
		return false;
	}

	@Override
	public boolean onLongClick(View view) {
		PageObject object = (PageObject) view.getTag();
		if (object.getRelativeHeight() == 100 && object.getRelativeWidth() == 100) {
			return false;
		}
		view.startDrag(null, new View.DragShadowBuilder(view), view, 0);
		return true;
	}
}

package com.teddytab.studio.toolbar;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.teddytab.common.model.Image;
import com.teddytab.common.model.PageObject;
import com.teddytab.common.model.Text;
import com.teddytab.common.model.WebPage;
import com.teddytab.studio.App;
import com.teddytab.studio.MainActivity;
import com.teddytab.studio.R;
import com.teddytab.studio.Utils;

public class ObjectsToolbar extends Fragment implements OnClickListener {

	private App app;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.toolbar_objects, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.more).setOnClickListener(this);
		view.findViewById(R.id.extra).setVisibility(View.GONE);

		view.findViewById(R.id.add_image).setOnClickListener(this);
		view.findViewById(R.id.add_text).setOnClickListener(this);
		view.findViewById(R.id.add_webpage).setOnClickListener(this);
		view.findViewById(R.id.add_video).setOnClickListener(this);
		view.findViewById(R.id.add_paint).setOnClickListener(this);
		view.findViewById(R.id.add_counter).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.more) {
			View extra = getView().findViewById(R.id.extra);
			extra.setVisibility(extra.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
			return;
		}

		switch(view.getId()) {
		case R.id.add_image:
//			startActivityForResult(new Intent(getActivity(), MediaSearchActivity.class)
//					.setType("image/png"), App.OBJECT_SEARCH_ACTIVITY);
			startActivityForResult(Intent.createChooser(
					new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT),
					getResources().getString(R.string.gallery)), App.OBJECT_SEARCH_ACTIVITY);
			return;
		case R.id.add_text:
			String id = "Text" +
					Utils.getNextIdSuffix("Text", app.book.getPage(app.selectedPage).objects);
			app.book.addObject(app.selectedPage, newText(id, "New Text"));
			((MainActivity) getActivity()).setSelectedObject(app.selectedPage, id);
			return;
		case R.id.add_webpage:
			PageObject object = new PageObject();
			object.tags = new String[] {PageObject.Type.WebPageType.name()};
			object.id = "WebPage"
					+ Utils.getNextIdSuffix("WebPage", app.book.getPage(app.selectedPage).objects);
			object.setRelativeX("10");
			object.setRelativeY("10");
			object.setRelativeWidth("80");
			object.setRelativeHeight("81");
			object.webPage = new WebPage();
			object.webPage.url = "http://";
			app.book.addObject(app.selectedPage, object);
			((MainActivity) getActivity()).setSelectedObject(app.selectedPage, object.id);
			return;
		case R.id.add_video:
			PageObject video = new PageObject();
			video.tags = new String[] {PageObject.Type.VideoType.name()};
			video.id = "Video"
					+ Utils.getNextIdSuffix("Video", app.book.getPage(app.selectedPage).objects);
			video.setRelativeX("25");
			video.setRelativeY("25");
			video.setRelativeWidth("50");
			video.setRelativeHeight("51");
			video.video = "http://www.youtube.com/embed/ZIqWPohGmmM?showinfo=0&rel=0";
			app.book.addObject(app.selectedPage, video);
			((MainActivity) getActivity()).setSelectedObject(app.selectedPage, video.id);
			return;
		case R.id.add_paint:
			PageObject paint = new PageObject();
			paint.id = "Paint";
			paint.tags = new String[] {PageObject.Type.PaintType.name()};
			paint.setRelativeX("0");
			paint.setRelativeY("0");
			paint.setRelativeWidth("100");
			paint.setRelativeHeight("100");
			app.book.addObject(app.selectedPage, paint);
			((MainActivity) getActivity()).setSelectedObject(app.selectedPage, paint.id);
			return;
		case R.id.add_counter:
			String counterId = "Counter"
					+ Utils.getNextIdSuffix("Counter", app.book.getPage(app.selectedPage).objects);
			PageObject counter = newText(counterId, "0", 91, 1, 8, 8);
			counter.tags = new String[] {PageObject.Type.CounterType.name()};
			app.book.addObject(app.selectedPage, counter);
			((MainActivity) getActivity()).setSelectedObject(app.selectedPage, counter.id);
			return;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode != Activity.RESULT_OK || intent == null) {
			return;
		}
		switch (requestCode) {
		case App.OBJECT_SEARCH_ACTIVITY:
			String imageUrl = intent.getDataString();
			String id = "Image" +
					Utils.getNextIdSuffix("Image", app.book.getPage(app.selectedPage).objects);
			app.book.addObject(app.selectedPage, newImage(id, imageUrl));
			((MainActivity) getActivity()).setSelectedObject(app.selectedPage, id);
			((MainActivity) getActivity()).updatePage();
			break;
		}
	}


	public static PageObject newImage(String id, String url) {
		return putImage(newObject(id), url);
	}

	public static PageObject newImage(String id, String url, int x, int y, int width, int height) {
		return putImage(newObject(id, x, y, width, height), url);
	}

	private static PageObject putImage(PageObject object, String url) {
		object.image = new Image();
		object.image.url = url;
		return object;
	}

	public static PageObject newText(String id, String text) {
		return putText(newObject(id,
				com.teddytab.common.model.Utils.maybeGetRandomNumber("15:55", 45),
				com.teddytab.common.model.Utils.maybeGetRandomNumber("15:75", 45),
				com.teddytab.common.model.Utils.maybeGetRandomNumber("15:25", 10),
				com.teddytab.common.model.Utils.maybeGetRandomNumber("5:10", 10) ), text);
	}

	public static PageObject newText(String id, String text, int x, int y, int width, int height) {
		return putText(newObject(id, x, y, width, height), text);
	}

	private static PageObject putText(PageObject object, String text) {
		object.text = new Text();
		object.text.text = text;
		object.image = new Image();
		return object;
	}

	private static PageObject newObject(String id) {
		return newObject(id,
				com.teddytab.common.model.Utils.maybeGetRandomNumber("15:55", 45),
				com.teddytab.common.model.Utils.maybeGetRandomNumber("15:75", 45),
				com.teddytab.common.model.Utils.maybeGetRandomNumber("15:30", 20),
				com.teddytab.common.model.Utils.maybeGetRandomNumber("10:20", 15) );
	}

	private static PageObject newObject(String id, int x, int y, int width, int height) {
		PageObject object = new PageObject();
		object.id = id;
		object.setRelativeX(Integer.toString(x));
		object.setRelativeY(Integer.toString(y));
		object.setRelativeWidth(Integer.toString(width));
		object.setRelativeHeight(Integer.toString(height));
		return object;
	}
}

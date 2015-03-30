package com.teddytab.studio.sidebar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.teddytab.common.editor.ColorPickerActivity;
import com.teddytab.common.editor.Config;
import com.teddytab.common.editor.MediaSearchActivity;
import com.teddytab.common.editor.Utils;
import com.teddytab.common.model.Image;
import com.teddytab.common.model.PageObject;
import com.teddytab.studio.App;
import com.teddytab.studio.DataChangeListener;
import com.teddytab.studio.MainActivity;
import com.teddytab.studio.R;

public class ObjectPropertiesFragment extends Fragment implements OnClickListener,
		DataChangeListener, OnCheckedChangeListener {
	public static final String TAG = "ObjectPropertiesFragment";

	public static final List<String> alignments = Arrays.asList(new String[] { "", "center", "left",
			"right" });

	private App app;

	private static enum ObjectType {
		NONE, IMAGE, TEXT, WEBPAGE, VIDEO, PAINT, COUNTER
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_object, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		((Spinner) view.findViewById(R.id.align)).setAdapter(new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_dropdown_item_1line, alignments));
		setObject(app.book.getObject(app.selectedPage, app.selectedObject));

		view.findViewById(R.id.changeImage).setOnClickListener(this);
		view.findViewById(R.id.textColor).setOnClickListener(this);
		view.findViewById(R.id.bgColor).setOnClickListener(this);

		((TextView) view.findViewById(R.id.object_id)).addTextChangedListener(
				new GenericTextWatcher(R.id.object_id));
		((TextView) view.findViewById(R.id.relativeX)).addTextChangedListener(
				new GenericTextWatcher(R.id.relativeX));
		((TextView) view.findViewById(R.id.relativeY)).addTextChangedListener(
				new GenericTextWatcher(R.id.relativeY));
		((TextView) view.findViewById(R.id.width)).addTextChangedListener(
				new GenericTextWatcher(R.id.width));
		((TextView) view.findViewById(R.id.height)).addTextChangedListener(
				new GenericTextWatcher(R.id.height));
		((TextView) view.findViewById(R.id.required_version)).addTextChangedListener(
				new GenericTextWatcher(R.id.required_version));
		((TextView) view.findViewById(R.id.text)).addTextChangedListener(
				new GenericTextWatcher(R.id.text));
		((TextView) view.findViewById(R.id.textColor)).addTextChangedListener(
				new GenericTextWatcher(R.id.textColor));
		((TextView) view.findViewById(R.id.textRelativeSize)).addTextChangedListener(
				new GenericTextWatcher(R.id.textRelativeSize));
		((TextView) view.findViewById(R.id.textSize)).addTextChangedListener(
				new GenericTextWatcher(R.id.textSize));
		((TextView) view.findViewById(R.id.imageUrl)).addTextChangedListener(
				new GenericTextWatcher(R.id.imageUrl));
		((TextView) view.findViewById(R.id.alpha)).addTextChangedListener(
				new GenericTextWatcher(R.id.alpha));
		((TextView) view.findViewById(R.id.bgColor)).addTextChangedListener(
				new GenericTextWatcher(R.id.bgColor));
		((TextView) view.findViewById(R.id.cornerRadius)).addTextChangedListener(
				new GenericTextWatcher(R.id.cornerRadius));
		((TextView) view.findViewById(R.id.video_url)).addTextChangedListener(
				new GenericTextWatcher(R.id.video_url));
		((TextView) view.findViewById(R.id.url)).addTextChangedListener(
				new GenericTextWatcher(R.id.url));

		((CheckBox) view.findViewById(R.id.hidden)).setOnCheckedChangeListener(this);
		((CheckBox) view.findViewById(R.id.bold)).setOnCheckedChangeListener(this);
		((CheckBox) view.findViewById(R.id.shadow)).setOnCheckedChangeListener(this);

		((Spinner) getView().findViewById(R.id.align)).setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> spinner, View item, int position, long id) {
				PageObject object = app.book.getObject(app.selectedPage, app.selectedObject);
				String align = spinner.getSelectedItem().toString();
				align = align.equals("") ? null : align;
				if (align != object.text.align &&
						(object.text.align != null && !object.text.align.equalsIgnoreCase(align)
						|| align != null && !align.equalsIgnoreCase(object.text.align))) {
					object.text.align = align;
					((MainActivity) getActivity()).updatePage();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
	}

	@Override
	public void notifyDataChange() {
		setObject(app.book.getObject(app.selectedPage, app.selectedObject));
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.changeImage:
			startActivityForResult(new Intent(getActivity(), MediaSearchActivity.class)
					.setType("image/png"), App.IMAGE_SEARCH_ACTIVITY);
//			startActivityForResult(Intent.createChooser(
//					new Intent().setType("image/png").setAction(Intent.ACTION_GET_CONTENT),
//					getResources().getString(R.string.gallery)), App.IMAGE_SEARCH_ACTIVITY);
			return;
		case R.id.textColor:
			startActivityForResult(new Intent(getActivity(), ColorPickerActivity.class).putExtra(
					ColorPickerActivity.COLOR, Utils.getText(getView(), R.id.textColor, null)),
					App.TEXT_COLOR_PICKER_ACTIVITY);
			return;
		case R.id.bgColor:
			startActivityForResult(new Intent(getActivity(), ColorPickerActivity.class).putExtra(
					ColorPickerActivity.COLOR, Utils.getText(getView(), R.id.bgColor, null)),
					App.BG_COLOR_PICKER_ACTIVITY);
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
		case App.IMAGE_SEARCH_ACTIVITY:
			Utils.setText(getView(), R.id.imageUrl, intent.getDataString());
			break;
		case App.TEXT_COLOR_PICKER_ACTIVITY:
			Utils.setText(getView(), R.id.textColor,
					intent.getStringExtra(ColorPickerActivity.COLOR));
			break;
		case App.BG_COLOR_PICKER_ACTIVITY:
			Utils.setText(getView(), R.id.bgColor,
					intent.getStringExtra(ColorPickerActivity.COLOR));
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton button, boolean isChecked) {
		PageObject object = app.book.getObject(app.selectedPage, app.selectedObject);
		switch(button.getId()) {
		case R.id.hidden:
			object.hidden = isChecked ? "YES" : null;
			break;
		case R.id.bold:
			object.text.bold = isChecked ? "YES" : null;
			break;
		case R.id.shadow:
			object.text.shadow = isChecked ? "YES" : null;
			break;
		}
		((MainActivity) getActivity()).updatePage();
	}

	private class GenericTextWatcher implements TextWatcher {
		private final int viewId;

		private GenericTextWatcher(int viewId) {
			this.viewId = viewId;
		}

		@Override
		public void afterTextChanged(Editable text) {
			PageObject object = app.book.getObject(app.selectedPage, app.selectedObject);
			if (object == null || getView() == null || getView().findViewById(viewId) == null) {
				return;
			}
			switch(viewId) {
			case R.id.object_id:
				object.id = Utils.getText(getView(), R.id.object_id, "");
				app.selectedObject = object.id;
				break;
			case R.id.relativeX:
				object.setRelativeX(Utils.getText(getView(), R.id.relativeX, null));
				break;
			case R.id.relativeY:
				object.setRelativeY(Utils.getText(getView(), R.id.relativeY, null));
				break;
			case R.id.width:
				object.setRelativeWidth(Utils.getText(getView(), R.id.width, null));
				break;
			case R.id.height:
				object.setRelativeHeight(Utils.getText(getView(), R.id.height, null));
				break;
			case R.id.required_version:
				object.requiredVersion = Utils.getText(getView(), R.id.required_version, null);
				break;
			case R.id.text:
				object.text.text = Utils.getText(getView(), R.id.text, null);
				break;
			case R.id.textColor:
				object.text.color = Utils.getText(getView(), R.id.textColor, null);
				break;
			case R.id.textRelativeSize:
				object.text.relativeSize = Utils.getText(getView(), R.id.textRelativeSize, null);
				break;
			case R.id.textSize:
				object.text.size = Utils.getText(getView(), R.id.textSize, null);
				break;
			case R.id.imageUrl:
				object.image = object.image == null ? new Image() : object.image;
				object.image.url = Utils.getText(getView(), R.id.imageUrl, null);
				break;
			case R.id.alpha:
				object.image = object.image == null ? new Image() : object.image;
				object.image.alpha = Utils.getText(getView(), R.id.alpha, null);
				break;
			case R.id.bgColor:
				object.image = object.image == null ? new Image() : object.image;
				object.image.backgroundColor = Utils.getText(getView(), R.id.bgColor, null);
				break;
			case R.id.cornerRadius:
				object.image = object.image == null ? new Image() : object.image;
				object.image.cornerRadius = Utils.getText(getView(), R.id.cornerRadius, null);
				break;
			case R.id.video_url:
				object.video = Utils.getText(getView(), R.id.video_url, null);
				break;
			case R.id.url:
				object.webPage.url = Utils.getText(getView(), R.id.url, null);
				break;
			}
			((MainActivity) getActivity()).updatePage();
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
	}

	private void setObject(PageObject object) {
		if (object == null) {
			return;
		}
		Utils.setText(getView(), R.id.object_id, object.id);
		Utils.setText(getView(), R.id.relativeX, object.getRelativeXString());
		Utils.setText(getView(), R.id.relativeY, object.getRelativeYString());
		Utils.setText(getView(), R.id.width, object.getRelativeWidthString());
		Utils.setText(getView(), R.id.height, object.getRelativeHeightString());

		if (object.text != null) {
			Utils.setText(getView(), R.id.text, object.text.text);
			Utils.setText(getView(), R.id.textColor, object.text.color);
			Utils.setText(getView(), R.id.textRelativeSize, object.text.relativeSize);
			Utils.setText(getView(), R.id.textSize, object.text.size);
			int alignIndex = alignments.indexOf(object.text.align);
			if (alignIndex != -1) {
				((Spinner) getView().findViewById(R.id.align)).setSelection(alignIndex);
			}
			((CheckBox) getView().findViewById(R.id.bold))
					.setChecked("yes".equalsIgnoreCase(object.text.bold));
			((CheckBox) getView().findViewById(R.id.shadow))
					.setChecked("yes".equalsIgnoreCase(object.text.shadow));
		}

		if (object.image != null) {
			Utils.setText(getView(), R.id.imageUrl, object.image.url);
			Utils.setText(getView(), R.id.alpha, object.image.alpha);
			Utils.setText(getView(), R.id.bgColor, object.image.backgroundColor);
			Utils.setText(getView(), R.id.cornerRadius, object.image.cornerRadius);
		}

		if (object.webPage != null) {
			Utils.setText(getView(), R.id.url, object.webPage.url);
		}

		Utils.setText(getView(), R.id.video_url, object.video);
		((CheckBox) getView().findViewById(R.id.hidden)).setChecked("yes".equalsIgnoreCase(object.hidden));
		Utils.setText(getView(), R.id.required_version, object.requiredVersion);
		showUiForObjectType(getObjectType(object));
	}

	private void showUiForObjectType(ObjectType objectType) {
		getView().findViewById(R.id.changeImage).setVisibility(View.GONE);
		getView().findViewById(R.id.imageProperties).setVisibility(View.GONE);
		getView().findViewById(R.id.textProperties).setVisibility(View.GONE);
		getView().findViewById(R.id.videoProperties).setVisibility(View.GONE);
		getView().findViewById(R.id.webPageProperties).setVisibility(View.GONE);
		switch (objectType) {
		case IMAGE:
			getView().findViewById(R.id.changeImage).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.imageProperties).setVisibility(View.VISIBLE);
			break;
		case TEXT:
			getView().findViewById(R.id.imageProperties).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.textProperties).setVisibility(View.VISIBLE);
			break;
		case COUNTER:
			getView().findViewById(R.id.imageProperties).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.textProperties).setVisibility(View.VISIBLE);
			break;
		case PAINT:
			break;
		case WEBPAGE:
			getView().findViewById(R.id.webPageProperties).setVisibility(View.VISIBLE);
			break;
		case VIDEO:
			getView().findViewById(R.id.videoProperties).setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	private ObjectType getObjectType(PageObject object) {
		if (object == null) {
			return ObjectType.NONE;
		}

		List<String> tags = new ArrayList<String>();
		Utils.fillList(tags, object.tags);
		if (object.image != null && object.text != null) {
			return ObjectType.TEXT;
		} else if (object.image != null && object.image.url != null) {
			return ObjectType.IMAGE;
		} else if (object.text != null) {
			return ObjectType.TEXT;
		} else if (object.webPage != null) {
			return ObjectType.WEBPAGE;
		} else if (object.video != null) {
			return ObjectType.VIDEO;
		} else if (tags.contains("PaintType")) {
			return ObjectType.PAINT;
		} else if (tags.contains("CounterType")) {
			return ObjectType.COUNTER;
		} else {
			Log.w(TAG, "Unknown PageObject: " + Config.GSON.toJson(object));
			return ObjectType.NONE;
		}
	}
}

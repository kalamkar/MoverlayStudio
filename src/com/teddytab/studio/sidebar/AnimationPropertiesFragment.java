package com.teddytab.studio.sidebar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.teddytab.common.editor.AudioRecorderFragment;
import com.teddytab.common.editor.Utils;
import com.teddytab.common.model.Animation;
import com.teddytab.common.model.Page;
import com.teddytab.common.model.PageEvent;
import com.teddytab.common.model.PageObject;
import com.teddytab.common.net.ImageLoaderTask;
import com.teddytab.studio.App;
import com.teddytab.studio.DataChangeListener;
import com.teddytab.studio.R;

public class AnimationPropertiesFragment extends Fragment implements DataChangeListener,
		OnItemSelectedListener, OnClickListener, OnCheckedChangeListener {

	private static final String TAG = "AnimationPropertiesFragment";

	public static final List<String> directions = Arrays.asList(new String[] { "", "up", "down",
			"left", "right" });
	public static final List<String> pageEffects = Arrays.asList(new String[] { "", "slide",
			"slidedown", "zoom", "none" });

	private App app;

	private BaseAdapter imagesAdapter;

	private final List<String> animationTypes = new ArrayList<String>();
	private final List<String> eventTypes = new ArrayList<String>();
	private final List<String> objectIds = new ArrayList<String>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_animation, null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		for (Animation.Type type : Animation.Type.values()) {
			animationTypes.add(type.name());
		}
		((Spinner) view.findViewById(R.id.animation_type)).setAdapter(new ArrayAdapter(getActivity(),
				android.R.layout.simple_dropdown_item_1line, animationTypes));

		Page page = app.book.getPage(app.selectedPage);
		objectIds.add("");
		if (page != null && page.objects != null) {
			for (PageObject object : page.objects) {
				if (object.id != null) {
					objectIds.add(object.id);
				}
			}
		}
		((Spinner) view.findViewById(R.id.byobject)).setAdapter(new ArrayAdapter(getActivity(),
				android.R.layout.simple_dropdown_item_1line, objectIds));
		((Spinner) view.findViewById(R.id.direction)).setAdapter(new ArrayAdapter(getActivity(),
				android.R.layout.simple_dropdown_item_1line, directions));
		((Spinner) view.findViewById(R.id.pageEffect)).setAdapter(new ArrayAdapter(getActivity(),
				android.R.layout.simple_dropdown_item_1line, pageEffects));
		imagesAdapter = new ImagesAdapter();
		((AbsListView) view.findViewById(R.id.images)).setAdapter(imagesAdapter);

		eventTypes.add("");
		for (PageEvent.Type type : PageEvent.Type.values()) {
			eventTypes.add(type.name());
		}
		((Spinner) view.findViewById(R.id.eventName)).setAdapter(new ArrayAdapter(getActivity(),
				android.R.layout.simple_dropdown_item_1line, eventTypes));

		view.findViewById(R.id.audioPlay).setOnClickListener(this);
		view.findViewById(R.id.audioDelete).setOnClickListener(this);
		view.findViewById(R.id.audioSearch).setOnClickListener(this);
		view.findViewById(R.id.audioRecord).setOnClickListener(this);
		view.findViewById(R.id.audioPlay).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.audioDelete).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.audioSearch).setVisibility(View.VISIBLE);
		view.findViewById(R.id.audioRecord).setVisibility(View.VISIBLE);

		((Spinner) view.findViewById(R.id.animation_type)).setOnItemSelectedListener(this);
		((Spinner) view.findViewById(R.id.byobject)).setOnItemSelectedListener(this);
		((Spinner) view.findViewById(R.id.direction)).setOnItemSelectedListener(this);
		((Spinner) view.findViewById(R.id.pageEffect)).setOnItemSelectedListener(this);
		((Spinner) view.findViewById(R.id.eventName)).setOnItemSelectedListener(this);

		((CheckBox) view.findViewById(R.id.reset)).setOnCheckedChangeListener(this);
		((CheckBox) view.findViewById(R.id.canMute)).setOnCheckedChangeListener(this);
		((CheckBox) view.findViewById(R.id.recordScore)).setOnCheckedChangeListener(this);

		((TextView) view.findViewById(R.id.animation_id)).addTextChangedListener(
				new GenericTextWatcher(R.id.animation_id));
		((TextView) view.findViewById(R.id.xdelta)).addTextChangedListener(
				new GenericTextWatcher(R.id.xdelta));
		((TextView) view.findViewById(R.id.ydelta)).addTextChangedListener(
				new GenericTextWatcher(R.id.ydelta));
		((TextView) view.findViewById(R.id.xpos)).addTextChangedListener(
				new GenericTextWatcher(R.id.xpos));
		((TextView) view.findViewById(R.id.ypos)).addTextChangedListener(
				new GenericTextWatcher(R.id.ypos));
		((TextView) view.findViewById(R.id.delay)).addTextChangedListener(
				new GenericTextWatcher(R.id.delay));
		((TextView) view.findViewById(R.id.duration)).addTextChangedListener(
				new GenericTextWatcher(R.id.duration));
		((TextView) view.findViewById(R.id.angle)).addTextChangedListener(
				new GenericTextWatcher(R.id.angle));
		((TextView) view.findViewById(R.id.repeatCount)).addTextChangedListener(
				new GenericTextWatcher(R.id.repeatCount));
		((TextView) view.findViewById(R.id.pageNumber)).addTextChangedListener(
				new GenericTextWatcher(R.id.pageNumber));
		((TextView) view.findViewById(R.id.audioUrl)).addTextChangedListener(
				new GenericTextWatcher(R.id.audioUrl));
		((TextView) view.findViewById(R.id.volume)).addTextChangedListener(
				new GenericTextWatcher(R.id.volume));
		((TextView) view.findViewById(R.id.audioLoopsCount)).addTextChangedListener(
				new GenericTextWatcher(R.id.audioLoopsCount));
		((TextView) view.findViewById(R.id.increment)).addTextChangedListener(
				new GenericTextWatcher(R.id.increment));
		((TextView) view.findViewById(R.id.count)).addTextChangedListener(
				new GenericTextWatcher(R.id.count));
		((TextView) view.findViewById(R.id.appStoreId)).addTextChangedListener(
				new GenericTextWatcher(R.id.appStoreId));
		((TextView) view.findViewById(R.id.playStoreUrl)).addTextChangedListener(
				new GenericTextWatcher(R.id.playStoreUrl));
		((TextView) view.findViewById(R.id.affiliate)).addTextChangedListener(
				new GenericTextWatcher(R.id.affiliate));
		((TextView) view.findViewById(R.id.campaign)).addTextChangedListener(
				new GenericTextWatcher(R.id.campaign));
		((TextView) view.findViewById(R.id.url)).addTextChangedListener(
				new GenericTextWatcher(R.id.url));

		notifyDataChange();
	}

	@Override
	public void notifyDataChange() {
		PageObject object = app.book.getObject(app.selectedPage, app.selectedObject);
		Animation animation = object.animation[app.selectedAnimation];
		setAnimation(animation);
		showHideWidgets(animation.type);
	}

	private class GenericTextWatcher implements TextWatcher {
		private final int viewId;

		private GenericTextWatcher(int viewId) {
			this.viewId = viewId;
		}

		@Override
		public void afterTextChanged(Editable text) {
			PageObject object = app.book.getObject(app.selectedPage, app.selectedObject);
			Animation animation = object.animation[app.selectedAnimation];
			if (getView() == null || getView().findViewById(viewId) == null) {
				return;
			}
			switch (viewId) {
			case R.id.animation_id:
				animation.id = Utils.getText(getView(), R.id.animation_id, null);
				break;
			case R.id.xdelta:
				animation.xdelta = Utils.getText(getView(), R.id.xdelta, null);
				break;
			case R.id.ydelta:
				animation.ydelta = Utils.getText(getView(), R.id.ydelta, null);
				break;
			case R.id.xpos:
				animation.xpos = Utils.getText(getView(), R.id.xpos, null);
				break;
			case R.id.ypos:
				animation.ypos = Utils.getText(getView(), R.id.ypos, null);
				break;
			case R.id.delay:
				animation.delay = Utils.getText(getView(), R.id.delay, null);
				break;
			case R.id.duration:
				animation.duration = Utils.getText(getView(), R.id.duration, null);
				break;
			case R.id.angle:
				animation.angle = Utils.getText(getView(), R.id.angle, null);
				break;
			case R.id.repeatCount:
				animation.repeatCount = Utils.getText(getView(), R.id.repeatCount, null);
				break;
			case R.id.pageNumber:
				animation.page = Utils.getText(getView(), R.id.pageNumber, null);
				break;
			case R.id.audioUrl:
				animation.audio = Utils.getText(getView(), R.id.audioUrl, null);
				break;
			case R.id.volume:
				animation.volume = Utils.getText(getView(), R.id.volume, null);
				break;
			case R.id.audioLoopsCount:
				animation.audioLoopsCount = Utils.getText(getView(), R.id.audioLoopsCount, null);
				break;
			case R.id.increment:
				animation.increment = Utils.getText(getView(), R.id.increment, null);
				break;
			case R.id.count:
				animation.count = Utils.getText(getView(), R.id.count, null);
				break;
			case R.id.appStoreId:
				animation.appStoreId = Utils.getText(getView(), R.id.appStoreId, null);
				break;
			case R.id.playStoreUrl:
				animation.playStoreUrl = Utils.getText(getView(), R.id.playStoreUrl, null);
				break;
			case R.id.affiliate:
				animation.affiliate = Utils.getText(getView(), R.id.affiliate, null);
				break;
			case R.id.campaign:
				animation.campaign = Utils.getText(getView(), R.id.campaign, null);
				break;
			case R.id.url:
				animation.url = Utils.getText(getView(), R.id.url, null);
				break;
			}
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
	}

	@Override
	public void onCheckedChanged(CompoundButton button, boolean isChecked) {
		PageObject object = app.book.getObject(app.selectedPage, app.selectedObject);
		Animation animation = object.animation[app.selectedAnimation];
		switch(button.getId()) {
		case R.id.reset:
			animation.reset = isChecked ? "YES" : "NO";
			break;
		case R.id.canMute:
			animation.canMute = isChecked ? "YES" : null;
			break;
		case R.id.recordScore:
			animation.recordScore = isChecked ? "YES" : null;
			break;
		}
	}

	private void setAnimation(Animation animation) {
		if (animation == null) {
			return;
		}

		Utils.setText(getView(), R.id.animation_id, animation.id);
		((Spinner) getView().findViewById(R.id.animation_type)).setSelection(animationTypes
				.indexOf(animation.type));

		int directionIndex = directions.indexOf(animation.direction);
		if (directionIndex != -1) {
			((Spinner) getView().findViewById(R.id.direction)).setSelection(directionIndex);
		}

		Utils.setText(getView(), R.id.xdelta, animation.xdelta);
		Utils.setText(getView(), R.id.ydelta, animation.ydelta);
		Utils.setText(getView(), R.id.xpos, animation.xpos);
		Utils.setText(getView(), R.id.ypos, animation.ypos);
		Utils.setText(getView(), R.id.delay, animation.delay);
		Utils.setText(getView(), R.id.duration, animation.duration);
		Utils.setText(getView(), R.id.angle, animation.angle);
		Utils.setText(getView(), R.id.repeatCount, animation.repeatCount);
		Utils.setText(getView(), R.id.pageNumber, animation.page);
		Utils.setText(getView(), R.id.appStoreId, animation.appStoreId);
		Utils.setText(getView(), R.id.playStoreUrl, animation.playStoreUrl);
		Utils.setText(getView(), R.id.affiliate, animation.affiliate);
		Utils.setText(getView(), R.id.campaign, animation.campaign);
		Utils.setText(getView(), R.id.url, animation.url);

		((CheckBox) getView().findViewById(R.id.reset)).setChecked("YES".equalsIgnoreCase(animation.reset));
		Utils.setText(getView(), R.id.audioUrl, animation.audio);
		Utils.setText(getView(), R.id.volume, animation.volume);
		((CheckBox) getView().findViewById(R.id.canMute)).setChecked("YES"
				.equalsIgnoreCase(animation.canMute));
		Utils.setText(getView(), R.id.audioLoopsCount, animation.audioLoopsCount);
		if (animation.audio != null && !"".equals(animation.audio)) {
			getView().findViewById(R.id.audioPlay).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.audioDelete).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.audioSearch).setVisibility(View.INVISIBLE);
			getView().findViewById(R.id.audioRecord).setVisibility(View.INVISIBLE);
		} else {
			getView().findViewById(R.id.audioPlay).setVisibility(View.INVISIBLE);
			getView().findViewById(R.id.audioDelete).setVisibility(View.INVISIBLE);
			getView().findViewById(R.id.audioSearch).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.audioRecord).setVisibility(View.VISIBLE);
		}

		int effectIndex = pageEffects.indexOf(animation.effect);
		if (effectIndex != -1) {
			((Spinner) getView().findViewById(R.id.pageEffect)).setSelection(effectIndex);
		}

		Utils.setText(getView(), R.id.increment, animation.increment);
		Utils.setText(getView(), R.id.count, animation.count);
		((CheckBox) getView().findViewById(R.id.recordScore)).setChecked("YES"
				.equalsIgnoreCase(animation.recordScore));

		int byobjectIndex = objectIds.indexOf(animation.byobject);
		if (byobjectIndex != -1) {
			((Spinner) getView().findViewById(R.id.byobject)).setSelection(byobjectIndex);
		}

		int eventNameIndex = eventTypes.indexOf(animation.eventName);
		if (eventNameIndex != -1) {
			((Spinner) getView().findViewById(R.id.eventName)).setSelection(eventNameIndex);
		}

		imagesAdapter.notifyDataSetChanged();
	}

	private void showHideWidgets(String typeStr) {
		if (typeStr == null) {
			return;
		}
		Animation.Type type = Animation.Type.valueOf(typeStr);

		getView().findViewById(R.id.counter).setVisibility(View.GONE);
		getView().findViewById(R.id.changePage).setVisibility(View.GONE);
		getView().findViewById(R.id.move).setVisibility(View.GONE);
		getView().findViewById(R.id.showApp).setVisibility(View.GONE);
		getView().findViewById(R.id.openUrl).setVisibility(View.GONE);
		getView().findViewById(R.id.byobject).setVisibility(View.GONE);
		getView().findViewById(R.id.images).setVisibility(View.GONE);
		getView().findViewById(R.id.stop).setVisibility(View.GONE);

		if (type == Animation.Type.counter) {
			getView().findViewById(R.id.counter).setVisibility(View.VISIBLE);
		}

		if (type == Animation.Type.changePage) {
			getView().findViewById(R.id.changePage).setVisibility(View.VISIBLE);
		}

		if (type == Animation.Type.bounce || type == Animation.Type.fly
				|| type == Animation.Type.hop || type == Animation.Type.move
				|| type == Animation.Type.rotate || type == Animation.Type.zoom
				|| type == Animation.Type.change) {
			getView().findViewById(R.id.move).setVisibility(View.VISIBLE);
		}

		if (type == Animation.Type.consume || type == Animation.Type.eat) {
			getView().findViewById(R.id.byobject).setVisibility(View.VISIBLE);
		}

		if (type == Animation.Type.change || type == Animation.Type.move) {
			getView().findViewById(R.id.images).setVisibility(View.VISIBLE);
		}

		if (type == Animation.Type.stop) {
			getView().findViewById(R.id.stop).setVisibility(View.VISIBLE);
		}

		if (type == Animation.Type.showApp) {
			getView().findViewById(R.id.showApp).setVisibility(View.VISIBLE);
		}

		if (type == Animation.Type.openUrl) {
			getView().findViewById(R.id.openUrl).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		PageObject object = app.book.getObject(app.selectedPage, app.selectedObject);
		Animation animation = object.animation[app.selectedAnimation];

		switch(parent.getId()) {
		case R.id.animation_type:
			animation.type = parent.getSelectedItem().toString();
			showHideWidgets(animationTypes.get(position));
			break;
		case R.id.direction:
			animation.direction = parent.getSelectedItem().toString();
			animation.direction = "".equals(animation.direction) ? null : animation.direction;
			break;
		case R.id.pageEffect:
			animation.effect = parent.getSelectedItem().toString();
			animation.effect = "".equals(animation.effect) ? null : animation.effect;
			break;
		case R.id.byobject:
			animation.byobject = parent.getSelectedItem().toString();
			animation.byobject = "".equals(animation.byobject) ? null : animation.byobject;
			break;
		case R.id.eventName:
			animation.eventName = parent.getSelectedItem().toString();
			animation.eventName = "".equals(animation.eventName) ? null : animation.eventName;
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.audioPlay:
			MediaPlayer player = new MediaPlayer();
			try {
				player.setDataSource(Utils.getText(getView(), R.id.audioUrl, null));
				player.prepare();
				player.start();
				player.setOnCompletionListener(new OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						mp.release();
					}
				});
			} catch (IOException e) {
				Log.w(TAG, e);
			}
			break;
		case R.id.audioDelete:
			Utils.setText(getView(), R.id.audioUrl, null);
			getView().findViewById(R.id.audioPlay).setVisibility(View.INVISIBLE);
			getView().findViewById(R.id.audioDelete).setVisibility(View.INVISIBLE);
			getView().findViewById(R.id.audioSearch).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.audioRecord).setVisibility(View.VISIBLE);
			break;
		case R.id.audioSearch:
//			startActivityForResult(new Intent(getActivity(),
//					MediaSearchActivity.class).setType("audio/mp3"), App.AUDIO_SEARCH_ACTIVITY);
			startActivityForResult(Intent.createChooser(
					new Intent().setType("audio/*").setAction(Intent.ACTION_GET_CONTENT),
					getResources().getString(R.string.gallery)), App.AUDIO_SEARCH_ACTIVITY);
			break;
		case R.id.audioRecord:
			new AudioRecorderFragment().show(getFragmentManager(), null);
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK || data == null) {
			return;
		}
		switch (requestCode) {
		case App.IMAGE_SEARCH_ACTIVITY:
			app.book.addAnimationImage(app.selectedPage, app.selectedObject, app.selectedAnimation,
					data.getDataString());
			imagesAdapter.notifyDataSetChanged();
			break;
		case App.AUDIO_SEARCH_ACTIVITY:
			Utils.setText(getView(), R.id.audioUrl, data.getDataString());
			getView().findViewById(R.id.audioPlay).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.audioDelete).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.audioSearch).setVisibility(View.INVISIBLE);
			getView().findViewById(R.id.audioRecord).setVisibility(View.INVISIBLE);
			break;
		}
	}

	private class ImagesAdapter extends BaseAdapter implements OnClickListener {

		@Override
		public int getCount() {
			Animation animation = app.book.getObject(app.selectedPage, app.selectedObject)
					.animation[app.selectedAnimation];
			return animation.animationImages == null ? 1 : animation.animationImages.length + 1;
		}

		@Override
		public String getItem(int position) {
			Animation animation = app.book.getObject(app.selectedPage, app.selectedObject)
					.animation[app.selectedAnimation];
			return animation.animationImages == null || position >= animation.animationImages.length
					? null : animation.animationImages[position];
		}

		@Override
		public long getItemId(int position) {
			return getItem(position) == null ? 0 : getItem(position).hashCode();
		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = getActivity().getLayoutInflater().inflate(R.layout.image_item, null);
			view.setOnClickListener(this);
			view.findViewById(R.id.delete).setOnClickListener(this);
			view.findViewById(R.id.menuButtons).setVisibility(View.INVISIBLE);

			String imageUrl = getItem(position);
			if (imageUrl != null) {
				new ImageLoaderTask(getActivity(), imageUrl,
						view.findViewById(R.id.thumbnail), null, null).execute();
				view.setTag(imageUrl);
				view.findViewById(R.id.delete).setTag(imageUrl);
			} else {
				((ImageView) view.findViewById(R.id.thumbnail))
						.setImageResource(android.R.drawable.ic_menu_add);
			}

			return view;
		}

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.delete:
				app.book.removeAnimationImage(app.selectedPage, app.selectedObject,
						app.selectedAnimation, (String) view.getTag());
				notifyDataSetChanged();
				break;
			default:
				View buttons = view.findViewById(R.id.menuButtons);
				if (buttons != null && buttons.getVisibility() == View.INVISIBLE) {
					buttons.setVisibility(View.VISIBLE);
				} else if (buttons != null && buttons.getVisibility() == View.VISIBLE) {
					buttons.setVisibility(View.INVISIBLE);
				}
				if (view.getTag() == null) {
//					startActivityForResult(new Intent(getActivity(), MediaSearchActivity.class)
//							.setType("image/png"), App.IMAGE_SEARCH_ACTIVITY);
					startActivityForResult(Intent.createChooser(
							new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT),
							getResources().getString(R.string.gallery)), App.IMAGE_SEARCH_ACTIVITY);
				}
			}
		}
	}
}

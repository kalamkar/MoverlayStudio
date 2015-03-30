package com.teddytab.studio.sidebar;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.teddytab.common.editor.Utils;
import com.teddytab.common.model.Animation;
import com.teddytab.common.model.Page;
import com.teddytab.common.model.PageEvent;
import com.teddytab.common.model.PageEvent.Type;
import com.teddytab.common.model.PageObject;
import com.teddytab.studio.App;
import com.teddytab.studio.DataChangeListener;
import com.teddytab.studio.R;

@SuppressLint("DefaultLocale")
public class EventPropertiesFragment extends Fragment implements DataChangeListener,
		OnItemSelectedListener, OnItemClickListener {
	private App app;

	private final List<String> eventTypes = new ArrayList<String>();
	private final List<String> objectIds = new ArrayList<String>();
	private final List<String> animationIds = new ArrayList<String>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_event, null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		updateObjectAndAnimationIds(app.book.getPage(app.selectedPage));

		Spinner events = (Spinner) view.findViewById(R.id.event_type);
		Spinner objects = (Spinner) view.findViewById(R.id.object_id);
		AbsListView animations = (AbsListView) view.findViewById(R.id.animations);

		events.setAdapter(new ArrayAdapter(getActivity(),
				android.R.layout.simple_dropdown_item_1line, eventTypes));
		objects.setAdapter(new ArrayAdapter(getActivity(),
				android.R.layout.simple_dropdown_item_1line, objectIds));
		animations.setAdapter(new ArrayAdapter(getActivity(),
				android.R.layout.simple_list_item_multiple_choice, animationIds));

		events.setOnItemSelectedListener(this);
		objects.setOnItemSelectedListener(this);

		animations.setOnItemClickListener(this);

		((CheckBox) view.findViewById(R.id.repeat)).setOnCheckedChangeListener(
				new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton view, boolean isChecked) {
						app.book.book.pages[app.selectedPage].events[app.selectedEvent].repeat
								= isChecked ? "YES" : "NO";
					}
		});

		((TextView) view.findViewById(R.id.interval)).addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable text) {
				app.book.book.pages[app.selectedPage].events[app.selectedEvent].interval
						= text.toString();
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
		});

		notifyDataChange();
	}

	@Override
	public void notifyDataChange() {
		setEvent(app.book.getPage(app.selectedPage).events[app.selectedEvent]);
	}

	private void setEvent(PageEvent event) {
		if (event == null) {
			return;
		}
		if (event.type != null && eventTypes.contains(event.type)) {
			((Spinner) getView().findViewById(R.id.event_type)).setSelection(
					eventTypes.indexOf(event.type));
		}
		if (event.objectid != null && objectIds.contains(event.objectid)) {
			((Spinner) getView().findViewById(R.id.object_id)).setSelection(
					objectIds.indexOf(event.objectid));
		}
		if (event.interval != null) {
			((TextView) getView().findViewById(R.id.interval)).setText(event.interval);
		}
		((CheckBox) getView().findViewById(R.id.repeat)).setChecked(
				"YES".equalsIgnoreCase(event.repeat));
		if (event.animationids != null) {
			for (String animId : event.animationids) {
				((AbsListView) getView().findViewById(R.id.animations)).setItemChecked(
						animationIds.indexOf(animId), true);
			}
		}
		if (event.condition != null) {
			Utils.setText(getView(), R.id.hour, event.condition.hour);
			Utils.setText(getView(), R.id.days, event.condition.days);
			Utils.setText(getView(), R.id.months, event.condition.months);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (((AbsListView) parent).isItemChecked(position)) {
			app.book.addEventAnimation(
					app.selectedPage, app.selectedEvent, animationIds.get(position));
		} else {
			app.book.removeEventAnimation(
					app.selectedPage, app.selectedEvent, animationIds.get(position));
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		switch(parent.getId()) {
		case R.id.event_type:
			app.book.getPage(app.selectedPage).events[app.selectedEvent].type =
					parent.getItemAtPosition(position).toString();
			break;
		case R.id.object_id:
			app.book.getPage(app.selectedPage).events[app.selectedEvent].objectid =
					parent.getItemAtPosition(position).toString();
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	private void updateObjectAndAnimationIds(Page page) {
		for (Type eventType : PageEvent.Type.values()) {
			eventTypes.add(eventType.name().toLowerCase());
		}
		objectIds.add("");
		if (page.objects != null) {
			for (PageObject object : page.objects) {
				if (object.id != null) {
					objectIds.add(object.id);
				}
				if (object.animation == null) {
					continue;
				}
				for (Animation animation : object.animation) {
					if (animation != null && animation.id != null) {
						if (!animationIds.contains(animation.id)) {
							animationIds.add(animation.id);
						}
						if (!objectIds.contains(animation.id)) {
							objectIds.add(animation.id);
						}
					}
				}
			}
		}

		PageEvent event = app.book.getPage(app.selectedPage).events[app.selectedEvent];
		if (event.animationids != null) {
			for (String animId : event.animationids) {
				if (!animationIds.contains(animId)) {
					animationIds.add(animId);
				}
			}
		}
	}
}

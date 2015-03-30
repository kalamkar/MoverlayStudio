package com.teddytab.studio.toolbar;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.teddytab.common.model.PageEvent;
import com.teddytab.studio.App;
import com.teddytab.studio.MainActivity;
import com.teddytab.studio.R;

public class EventsToolbar extends Fragment implements OnClickListener {

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
		return inflater.inflate(R.layout.toolbar_events, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.more).setOnClickListener(this);
		view.findViewById(R.id.extra).setVisibility(View.GONE);

		view.findViewById(R.id.tap).setOnClickListener(this);
		view.findViewById(R.id.done).setOnClickListener(this);
		view.findViewById(R.id.pageload).setOnClickListener(this);
		view.findViewById(R.id.drag).setOnClickListener(this);
		view.findViewById(R.id.drop).setOnClickListener(this);
		view.findViewById(R.id.timer).setOnClickListener(this);
		view.findViewById(R.id.left).setOnClickListener(this);
		view.findViewById(R.id.right).setOnClickListener(this);
		view.findViewById(R.id.up).setOnClickListener(this);
		view.findViewById(R.id.down).setOnClickListener(this);
		view.findViewById(R.id.error).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.more) {
			View extra = getView().findViewById(R.id.extra);
			extra.setVisibility(extra.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
			return;
		}

		PageEvent event = new PageEvent();
		switch(view.getId()) {
		case R.id.tap:
			event.type = PageEvent.Type.touchdown.name();
			break;
		case R.id.done:
			event.type = PageEvent.Type.done.name();
			break;
		case R.id.pageload:
			event.type = PageEvent.Type.pageload.name();
			break;
		case R.id.drag:
			event.type = PageEvent.Type.drag.name();
			break;
		case R.id.drop:
			event.type = PageEvent.Type.drop.name();
			break;
		case R.id.timer:
			event.type = PageEvent.Type.timer.name();
			break;
		case R.id.left:
			event.type = PageEvent.Type.swipeLeft.name();
			break;
		case R.id.right:
			event.type = PageEvent.Type.swipeRight.name();
			break;
		case R.id.up:
			event.type = PageEvent.Type.swipeUp.name();
			break;
		case R.id.down:
			event.type = PageEvent.Type.swipeDown.name();
			break;
		case R.id.error:
			event.type = PageEvent.Type.error.name();
			break;
		}
		int eventIndex = app.book.addEvent(app.selectedPage, event);
		((MainActivity) getActivity()).setSelectedEvent(app.selectedPage, eventIndex);
	}

}

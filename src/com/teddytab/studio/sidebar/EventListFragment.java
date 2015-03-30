package com.teddytab.studio.sidebar;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.teddytab.common.model.PageEvent;
import com.teddytab.studio.App;
import com.teddytab.studio.DataChangeListener;
import com.teddytab.studio.MainActivity;
import com.teddytab.studio.R;
import com.teddytab.studio.Utils;

public class EventListFragment extends Fragment implements DataChangeListener {

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
		return new ListView(app);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		((ListView) view).setAdapter(new EventListAdapter());
	}

	@Override
	public void notifyDataChange() {
		((BaseAdapter) ((ListView) getView()).getAdapter()).notifyDataSetChanged();
	}

	private class EventListAdapter extends BaseAdapter implements OnClickListener {

		@Override
		public int getCount() {
			return app == null || app.book == null || app.book.getPage(app.selectedPage) == null
					|| app.book.getPage(app.selectedPage).events == null ?
							0 : app.book.getPage(app.selectedPage).events.length;
		}

		@Override
		public PageEvent getItem(int position) {
			return app.book.getPage(app.selectedPage).events[position];
		}

		@Override
		public long getItemId(int position) {
			PageEvent item = getItem(position);
			return item == null ? 0 : item.hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView != null) {
				view = convertView;
			} else {
				view = getActivity().getLayoutInflater().inflate(R.layout.list_item, null);
			}
			Utils.setText(view, R.id.position, Integer.toString(position + 1));
			view.findViewById(R.id.menuButtons).setVisibility(View.INVISIBLE);
			view.setOnClickListener(this);
			PageEvent event = getItem(position);
			if (event == null) {
				Utils.setText(view, R.id.title, getResources().getString(R.string.add_event));
				view.findViewById(R.id.tip).setVisibility(View.INVISIBLE);
				return view;
			}
			((TextView) view.findViewById(R.id.title)).setText(event.toString());
			((TextView) view.findViewById(R.id.tip)).setText(getTip(event));
			view.findViewById(R.id.edit).setOnClickListener(this);
			view.findViewById(R.id.delete).setOnClickListener(this);
			view.findViewById(R.id.copy).setOnClickListener(this);
			view.findViewById(R.id.edit).setTag(position);
			view.findViewById(R.id.delete).setTag(event);
			view.findViewById(R.id.copy).setTag(event);
			view.setTag(event);
			return view;
		}

		private String getTip(PageEvent event) {
			if (event == null || event.animationids == null) {
				return "";
			}
			StringBuilder tip = new StringBuilder();
			for (String anim : event.animationids) {
				tip.append(anim).append(", ");
			}
			return tip.toString().replaceAll(", $", "");
		}

		@Override
		public void onClick(View v) {
			final Object tag = v.getTag();
			switch (v.getId()) {
			case R.id.edit:
				((MainActivity) getActivity()).setSelectedEvent(app.selectedPage, (Integer) tag);
				return;
			case R.id.delete:
				Utils.showDelete(getActivity(), new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    	app.book.removeEvent(app.selectedPage, (PageEvent) tag);
				    	notifyDataSetChanged();
				    }
				});
				return;
			case R.id.copy:
				((MainActivity) getActivity()).copy(tag);
				return;
			default:
				View buttons = v.findViewById(R.id.menuButtons);
				if (buttons != null && buttons.getVisibility() == View.VISIBLE) {
					buttons.setVisibility(View.INVISIBLE);
				} else if (buttons != null && buttons.getVisibility() == View.INVISIBLE) {
					buttons.setVisibility(View.VISIBLE);
				}
			}
		}
	}

}

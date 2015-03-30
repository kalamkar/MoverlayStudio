package com.teddytab.studio.sidebar;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.teddytab.common.model.Animation;
import com.teddytab.common.model.PageObject;
import com.teddytab.studio.App;
import com.teddytab.studio.DataChangeListener;
import com.teddytab.studio.MainActivity;
import com.teddytab.studio.R;
import com.teddytab.studio.Utils;

public class ObjectListFragment extends Fragment implements OnDragListener, OnLongClickListener,
		DataChangeListener {

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
		((ListView) view).setAdapter(new ObjectListAdapter());
	}

	@Override
	public boolean onDrag(View receiverView, DragEvent event) {
		final View draggedView = (View) event.getLocalState();
		switch (event.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			return true;
		case DragEvent.ACTION_DROP:
			app.book.moveObject(app.selectedPage, draggedView.getTag(), receiverView.getTag());
			((MainActivity) getActivity()).updatePage();
			((BaseAdapter) ((ListView) getView()).getAdapter()).notifyDataSetChanged();
			return true;
		case DragEvent.ACTION_DRAG_ENDED:
			return true;
		}
		return false;
	}

	@Override
	public boolean onLongClick(View view) {
		view.startDrag(null, new View.DragShadowBuilder(view), view, 0);
		return true;
	}

	@Override
	public void notifyDataChange() {
		View view = getView();
		if (view != null) {
			((BaseAdapter) ((ListView) view).getAdapter()).notifyDataSetChanged();
		}
	}

	private class ObjectListAdapter extends BaseAdapter implements OnClickListener {

		@Override
		public int getCount() {
			return app == null || app.book.getPage(app.selectedPage) == null
					|| app.book.getPage(app.selectedPage).objects == null ? 0
					: app.book.getPage(app.selectedPage).objects.length;
		}

		@Override
		public PageObject getItem(int position) {
			return app.book.getPage(app.selectedPage).objects[position];
		}

		@Override
		public long getItemId(int position) {
			PageObject item = getItem(position);
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
			view.setOnDragListener(ObjectListFragment.this);
			PageObject object = getItem(position);
			if (object == null) {
				Utils.setText(view, R.id.title, getResources().getString(R.string.add_object));
				view.findViewById(R.id.tip).setVisibility(View.INVISIBLE);
				return view;
			}
			((TextView) view.findViewById(R.id.title)).setText(object.id);
			((TextView) view.findViewById(R.id.tip)).setText(getTip(object));
			view.setOnLongClickListener(ObjectListFragment.this);
			view.findViewById(R.id.edit).setOnClickListener(this);
			view.findViewById(R.id.delete).setOnClickListener(this);
			view.findViewById(R.id.copy).setOnClickListener(this);
			view.findViewById(R.id.edit).setTag(object);
			view.findViewById(R.id.delete).setTag(object);
			view.findViewById(R.id.copy).setTag(object);
			view.setTag(object);
			return view;
		}

		String getTip(PageObject object) {
			if (object == null || object.animation == null) {
				return "";
			}
			StringBuilder tip = new StringBuilder();
			for (Animation animation : object.animation) {
				tip.append(animation.id).append(", ");
			}
			return tip.toString().replaceAll(", $", "");
		}

		@Override
		public void onClick(View v) {
			final Object tag = v.getTag();
			switch (v.getId()) {
			case R.id.edit:
				PageObject object = (PageObject) tag;
				((MainActivity) getActivity()).setSelectedObject(app.selectedPage, object.id);
				return;
			case R.id.delete:
				Utils.showDelete(getActivity(), new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    	app.book.removeObject(app.selectedPage, (PageObject) tag);
				    	((MainActivity) getActivity()).updatePage();
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

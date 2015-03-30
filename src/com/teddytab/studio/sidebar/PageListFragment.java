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

import com.teddytab.common.model.Page;
import com.teddytab.studio.App;
import com.teddytab.studio.DataChangeListener;
import com.teddytab.studio.MainActivity;
import com.teddytab.studio.R;
import com.teddytab.studio.Utils;

public class PageListFragment extends Fragment implements OnDragListener, OnLongClickListener,
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
		((ListView) view).setAdapter(new PageListAdapter());
	}

	@Override
	public boolean onDrag(View receiverView, DragEvent event) {
		final View draggedView = (View) event.getLocalState();
		switch (event.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			return true;
		case DragEvent.ACTION_DROP:
			app.book.movePage((Page) draggedView.getTag(), (Page) receiverView.getTag());
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

	private class PageListAdapter extends BaseAdapter implements OnClickListener {

		@Override
		public int getCount() {
			return app == null || app.book.book.pages == null ? 0
					: app.book.book.pages.length;
		}

		@Override
		public Page getItem(int position) {
			return app.book.book.pages[position];
		}

		@Override
		public long getItemId(int position) {
			Page item = getItem(position);
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
			view.setOnDragListener(PageListFragment.this);
			Page page = getItem(position);
			((TextView) view.findViewById(R.id.title)).setText(page.id);
			((TextView) view.findViewById(R.id.tip)).setText(getTip(page));
			view.setOnLongClickListener(PageListFragment.this);
			view.findViewById(R.id.edit).setOnClickListener(this);
			view.findViewById(R.id.delete).setOnClickListener(this);
			view.findViewById(R.id.copy).setOnClickListener(this);
			view.findViewById(R.id.edit).setTag(position);
			view.findViewById(R.id.delete).setTag(page);
			view.findViewById(R.id.copy).setTag(page);
			view.setTag(page);
			return view;
		}

		String getTip(Page page) {
			return page == null ? "" : Utils.join(page.tags, ",");
		}

		@Override
		public void onClick(View v) {
			final Object tag = v.getTag();
			switch (v.getId()) {
			case R.id.edit:
				((MainActivity) getActivity()).setSelectedPage((Integer) tag);
				return;
			case R.id.delete:
				Utils.showDelete(getActivity(), new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    	app.book.deletePage((Page) tag);
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

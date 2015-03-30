package com.teddytab.studio.sidebar;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.teddytab.common.editor.Utils;
import com.teddytab.studio.App;
import com.teddytab.studio.DataChangeListener;
import com.teddytab.studio.R;

public class PagePropertiesFragment extends Fragment implements OnClickListener, DataChangeListener,
		TextWatcher {
	
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
		return inflater.inflate(R.layout.fragment_page_properties, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		((ListView) view.findViewById(R.id.tags)).setAdapter(new TagsListAdapter());
		view.findViewById(R.id.add).setOnClickListener(this);
		Utils.setText(view, R.id.book_id, app.book.book.pages[app.selectedPage].id);
		((TextView) view.findViewById(R.id.book_id)).addTextChangedListener(this);
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.add:
			String tag = Utils.getText(getView(), R.id.tag, null);
			if (tag != null) {
				app.book.addPageTag(app.selectedPage, tag);
				Utils.setText(getView(), R.id.tag, "");
				notifyDataChange();
			}
			break;
		case R.id.delete:
			app.book.deletePageTag(app.selectedPage, (String) view.getTag());
			notifyDataChange();
		}
	}
	
	@Override
	public void notifyDataChange() {
		((BaseAdapter) ((ListView) getView().findViewById(R.id.tags)).getAdapter())
				.notifyDataSetChanged();
	}
	
	private class TagsListAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return app == null || app.book.getPage(app.selectedPage) == null
					|| app.book.getPage(app.selectedPage).tags == null ? 0
					: app.book.getPage(app.selectedPage).tags.length;
		}

		@Override
		public String getItem(int position) {
			return app.book.getPage(app.selectedPage).tags[position];
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = getActivity().getLayoutInflater().inflate(R.layout.list_item, null);
				view.findViewById(R.id.edit).setVisibility(View.GONE);
				view.findViewById(R.id.copy).setVisibility(View.GONE);
				view.findViewById(R.id.delete).setOnClickListener(PagePropertiesFragment.this);
				Utils.setText(view, R.id.tip, null);
			} else {
				view = convertView;
			}
			String tag = getItem(position);
			Utils.setText(view, R.id.title, tag);
			view.findViewById(R.id.delete).setTag(tag);
			return view;
		}
	}

	@Override
	public void afterTextChanged(Editable text) {
		app.book.book.pages[app.selectedPage].id = text.toString();
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
}
